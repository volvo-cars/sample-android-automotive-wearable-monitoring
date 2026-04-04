package main

import (
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/rand"
	"crypto/tls"
	"crypto/x509"
	"crypto/x509/pkix"
	"encoding/json"
	"encoding/pem"
	"flag"
	"fmt"
	"log"
	"math"
	"math/big"
	mrand "math/rand"
	"net"
	"net/http"
	"strconv"
	"sync"
	"time"
)

// Configuration
var (
	port       int
	liveMode   bool
	scenario   string
	delay      int
	errorRate  float64
	useHTTPS   bool
	certFile   string
	keyFile    string
	startTime  time.Time
	reqCount   int64
	reqMutex   sync.Mutex
)

// Simulation state
var (
	currentSgv     int
	trendIndex     int
	simMutex       sync.Mutex
	entriesHistory []GlucoseEntry // Persistent historical entries
	lastUpdateTime int64          // Last time a new entry was added (Unix timestamp)
)

// Trend directions
var directions = []string{
	"DoubleDown",
	"SingleDown",
	"FortyFiveDown",
	"Flat",
	"FortyFiveUp",
	"SingleUp",
	"DoubleUp",
}

var directionTrend = map[string]int{
	"DoubleDown":     1,
	"SingleDown":     2,
	"FortyFiveDown":  3,
	"Flat":           4,
	"FortyFiveUp":    5,
	"SingleUp":       6,
	"DoubleUp":       7,
}

// GlucoseEntry represents a glucose reading
type GlucoseEntry struct {
	ID         string `json:"_id"`
	Sgv        int    `json:"sgv"`
	Date       int64  `json:"date"`
	DateString string `json:"dateString"`
	Trend      string `json:"trend"`
	Direction  string `json:"direction"`
	Type       string `json:"type"`
}

// ServerStatus represents the NightScout server status
type ServerStatus struct {
	APIEnabled        bool              `json:"apiEnabled"`
	Authorized        interface{}       `json:"authorized"`
	BoluscalcEnabled  bool              `json:"boluscalcEnabled"`
	CareportalEnabled bool              `json:"careportalEnabled"`
	ExtendedSettings  map[string]interface{} `json:"extendedSettings"`
	Name              string            `json:"name"`
	ServerTime        string            `json:"serverTime"`
	ServerTimeEpoch   int64             `json:"serverTimeEpoch"`
	Settings          Settings          `json:"settings"`
	Status            string            `json:"status"`
	Version           string            `json:"version"`
}

// Settings represents server settings
type Settings struct {
	AlarmHigh                            bool                   `json:"alarmHigh"`
	AlarmHighMins                        []int                  `json:"alarmHighMins"`
	AlarmLow                             bool                   `json:"alarmLow"`
	AlarmLowMins                         []int                  `json:"alarmLowMins"`
	AlarmPumpBatteryLow                  bool                   `json:"alarmPumpBatteryLow"`
	AlarmTimeagoUrgent                   bool                   `json:"alarmTimeagoUrgent"`
	AlarmTimeagoUrgentMins               int                    `json:"alarmTimeagoUrgentMins"`
	AlarmTimeagoWarn                     bool                   `json:"alarmTimeagoWarn"`
	AlarmTimeagoWarnMins                 int                    `json:"alarmTimeagoWarnMins"`
	AlarmTypes                           []string               `json:"alarmTypes"`
	AlarmUrgentHigh                      bool                   `json:"alarmUrgentHigh"`
	AlarmUrgentHighMins                  []int                  `json:"alarmUrgentHighMins"`
	AlarmUrgentLow                       bool                   `json:"alarmUrgentLow"`
	AlarmUrgentLowMins                   []int                  `json:"alarmUrgentLowMins"`
	AlarmUrgentMins                      []int                  `json:"alarmUrgentMins"`
	AlarmWarnMins                        []int                  `json:"alarmWarnMins"`
	AuthDefaultRoles                     string                 `json:"authDefaultRoles"`
	BaseURL                              string                 `json:"baseURL"`
	CustomTitle                          string                 `json:"customTitle"`
	DefaultFeatures                      []string               `json:"DEFAULT_FEATURES"`
	EditMode                             bool                   `json:"editMode"`
	Enable                               []string               `json:"enable"`
	FocusHours                           int                    `json:"focusHours"`
	Heartbeat                            int                    `json:"heartbeat"`
	InsecureUseHTTP                      bool                   `json:"insecureUseHttp"`
	Language                             string                 `json:"language"`
	NightMode                            bool                   `json:"nightMode"`
	ScaleY                               string                 `json:"scaleY"`
	SecureCSP                            bool                   `json:"secureCsp"`
	SecureHstsHeader                     bool                   `json:"secureHstsHeader"`
	SecureHstsHeaderIncludeSubdomains    bool                   `json:"secureHstsHeaderIncludeSubdomains"`
	SecureHstsHeaderPreload              bool                   `json:"secureHstsHeaderPreload"`
	ShowForecast                         string                 `json:"showForecast"`
	ShowPlugins                          string                 `json:"showPlugins"`
	ShowRawbg                            string                 `json:"showRawbg"`
	Theme                                string                 `json:"theme"`
	Thresholds                           Thresholds             `json:"thresholds"`
	TimeFormat                           int                    `json:"timeFormat"`
	Units                                string                 `json:"units"`
}

// Thresholds represents glucose thresholds
type Thresholds struct {
	BgHigh         int `json:"bgHigh"`
	BgLow          int `json:"bgLow"`
	BgTargetBottom int `json:"bgTargetBottom"`
	BgTargetTop    int `json:"bgTargetTop"`
}

// HealthCheck represents health check response
type HealthCheck struct {
	Status         string  `json:"status"`
	Uptime         float64 `json:"uptime"`
	Timestamp      string  `json:"timestamp"`
	Scenario       string  `json:"scenario"`
	LiveMode       bool    `json:"liveMode"`
	CurrentGlucose int     `json:"currentGlucose"`
	RequestCount   int64   `json:"requestCount"`
}

func init() {
	flag.IntVar(&port, "port", 5001, "Server port")
	flag.BoolVar(&liveMode, "live", false, "Enable live random walk simulation")
	flag.StringVar(&scenario, "scenario", "normal", "Glucose scenario (normal, hypo, hyper, critical-low, critical-high)")
	flag.IntVar(&delay, "delay", 0, "Network delay in milliseconds")
	flag.Float64Var(&errorRate, "error-rate", 0.0, "Error rate (0.0-1.0)")
	flag.BoolVar(&useHTTPS, "https", false, "Use HTTPS with self-signed cert (default: false, use HTTP)")
	flag.StringVar(&certFile, "cert", "", "Path to TLS certificate file (auto-generated if not provided)")
	flag.StringVar(&keyFile, "key", "", "Path to TLS key file (auto-generated if not provided)")
}

func main() {
	flag.Parse()
	startTime = time.Now()

	// Initialize simulation state
	currentSgv = getInitialSgv()
	trendIndex = getInitialTrend()

	// Seed random number generator
	mrand.Seed(time.Now().UnixNano())

	// Setup routes
	http.HandleFunc("/api/v1/entries.json", corsMiddleware(handleEntries))
	http.HandleFunc("/api/v1/status.json", corsMiddleware(handleStatus))
	http.HandleFunc("/health", corsMiddleware(handleHealth))
	http.HandleFunc("/dashboard", handleDashboard)
	http.HandleFunc("/", handleDashboard)

	// Print startup info
	printStartupInfo()

	// Start server
	addr := fmt.Sprintf("0.0.0.0:%d", port)

	if useHTTPS {
		// Generate or load TLS certificate
		cert, err := getTLSCertificate()
		if err != nil {
			log.Fatalf("Failed to get TLS certificate: %v", err)
		}

		tlsConfig := &tls.Config{
			Certificates: []tls.Certificate{cert},
			MinVersion:   tls.VersionTLS12,
		}

		server := &http.Server{
			Addr:      addr,
			TLSConfig: tlsConfig,
		}

		log.Fatal(server.ListenAndServeTLS("", ""))
	} else {
		log.Fatal(http.ListenAndServe(addr, nil))
	}
}

func getInitialSgv() int {
	switch scenario {
	case "hypo":
		return 65
	case "hyper":
		return 220
	case "critical-low":
		return 45
	case "critical-high":
		return 350
	default:
		return 110
	}
}

func getInitialTrend() int {
	switch scenario {
	case "hypo", "critical-low":
		return 0 // DoubleDown
	case "hyper", "critical-high":
		return 5 // DoubleUp
	default:
		return 3 // Flat
	}
}

func nextSgv() int {
	simMutex.Lock()
	defer simMutex.Unlock()

	if liveMode {
		var delta int
		switch scenario {
		case "hypo":
			// Tend to stay low with slight upward bias
			delta = int(math.Round((mrand.Float64() - 0.3) * 8))
			currentSgv = clamp(currentSgv+delta, 40, 400)
		case "hyper":
			// Tend to stay high with slight downward bias
			delta = int(math.Round((mrand.Float64() - 0.7) * 8))
			currentSgv = clamp(currentSgv+delta, 40, 400)
		case "critical-low":
			// Very low, slowly recovering
			delta = int(math.Round(mrand.Float64() * 3))
			currentSgv = clamp(currentSgv+delta, 40, 90)
		case "critical-high":
			// Very high, slowly declining
			delta = int(math.Round(mrand.Float64() * -3))
			currentSgv = clamp(currentSgv+delta, 250, 400)
		default:
			// Normal random walk ±5 mg/dL per reading
			delta = int(math.Round((mrand.Float64() - 0.5) * 10))
			currentSgv = clamp(currentSgv+delta, 40, 400)
		}

		// Adjust trend based on delta
		if delta <= -5 {
			trendIndex = max(0, trendIndex-1)
		} else if delta >= 5 {
			trendIndex = min(6, trendIndex+1)
		} else if abs(delta) < 2 {
			trendIndex = 3 // Flat
		}
	}
	return currentSgv
}

func sampleSgv(i, count int) int {
	switch scenario {
	case "hypo":
		// Low glucose values (55-75 mg/dL)
		return int(math.Round(65 + 10*math.Sin(math.Pi*float64(i)/float64(count))))
	case "hyper":
		// High glucose values (200-280 mg/dL)
		return int(math.Round(240 + 40*math.Sin(math.Pi*float64(i)/float64(count))))
	case "critical-low":
		// Critical low (40-60 mg/dL)
		return int(math.Round(50 + 10*math.Sin(math.Pi*float64(i)/float64(count))))
	case "critical-high":
		// Critical high (300-400 mg/dL)
		return int(math.Round(350 + 50*math.Sin(math.Pi*float64(i)/float64(count))))
	default:
		// Normal S-curve glucose profile (80-170 mg/dL)
		midpoint := float64(count) / 2
		amplitude := 45.0
		base := 110.0
		return int(math.Round(base + amplitude*math.Sin(math.Pi*(float64(i)-midpoint)/float64(count))))
	}
}

func buildEntries(count int) []GlucoseEntry {
	simMutex.Lock()
	defer simMutex.Unlock()

	now := time.Now()

	// Initialize history on first call
	if len(entriesHistory) == 0 {
		// Create initial historical entries going back in time
		baseTime := now.Add(-time.Duration(count) * 5 * time.Minute)
		entriesHistory = make([]GlucoseEntry, 0, count*2) // Extra capacity for growth

		for i := 0; i < count; i++ {
			ts := baseTime.Add(time.Duration(i) * 5 * time.Minute)
			var sgv int
			if liveMode {
				sgv = getInitialSgv() + mrand.Intn(20) - 10 // Some variation
			} else {
				sgv = sampleSgv(i, count)
			}

			dir := directions[trendIndex]
			entry := GlucoseEntry{
				ID:         fmt.Sprintf("mock%d%d", i, ts.UnixMilli()),
				Sgv:        sgv,
				Date:       ts.UnixMilli(),
				DateString: ts.Format(time.RFC3339),
				Trend:      strconv.Itoa(directionTrend[dir]),
				Direction:  dir,
				Type:       "sgv",
			}
			entriesHistory = append(entriesHistory, entry)
		}
		lastUpdateTime = entriesHistory[len(entriesHistory)-1].Date / 1000 // Convert to seconds
		currentSgv = entriesHistory[len(entriesHistory)-1].Sgv
	}

	// Add a new entry if 5 minutes have passed (in live mode) or in static mode on first few calls
	nowUnix := now.Unix()
	if liveMode && (nowUnix-lastUpdateTime) >= 300 { // 300 seconds = 5 minutes
		// Generate new entry
		sgv := nextSgv()
		dir := directions[trendIndex]

		entry := GlucoseEntry{
			ID:         fmt.Sprintf("mock%d%d", len(entriesHistory), now.UnixMilli()),
			Sgv:        sgv,
			Date:       now.UnixMilli(),
			DateString: now.Format(time.RFC3339),
			Trend:      strconv.Itoa(directionTrend[dir]),
			Direction:  dir,
			Type:       "sgv",
		}

		entriesHistory = append(entriesHistory, entry)
		lastUpdateTime = nowUnix

		// Keep only last 1000 entries to prevent unlimited growth
		if len(entriesHistory) > 1000 {
			entriesHistory = entriesHistory[len(entriesHistory)-1000:]
		}
	}

	// Return the most recent 'count' entries
	startIdx := len(entriesHistory) - count
	if startIdx < 0 {
		startIdx = 0
	}

	result := make([]GlucoseEntry, len(entriesHistory)-startIdx)
	copy(result, entriesHistory[startIdx:])

	// Reverse to return newest first (typical API behavior)
	for i, j := 0, len(result)-1; i < j; i, j = i+1, j-1 {
		result[i], result[j] = result[j], result[i]
	}

	return result
}

func buildStatus() ServerStatus {
	return ServerStatus{
		APIEnabled:        true,
		Authorized:        nil,
		BoluscalcEnabled:  false,
		CareportalEnabled: true,
		ExtendedSettings: map[string]interface{}{
			"basal":        map[string]string{"render": "default"},
			"devicestatus": map[string]bool{"advanced": true},
			"profile":      map[string]bool{"multiple": true},
		},
		Name:            "Mock NightScout",
		ServerTime:      time.Now().Format(time.RFC3339),
		ServerTimeEpoch: time.Now().UnixMilli(),
		Settings: Settings{
			AlarmHigh:                            true,
			AlarmHighMins:                        []int{30, 60, 90, 120},
			AlarmLow:                             true,
			AlarmLowMins:                         []int{15, 30, 45, 60},
			AlarmPumpBatteryLow:                  false,
			AlarmTimeagoUrgent:                   true,
			AlarmTimeagoUrgentMins:               30,
			AlarmTimeagoWarn:                     true,
			AlarmTimeagoWarnMins:                 15,
			AlarmTypes:                           []string{"simple"},
			AlarmUrgentHigh:                      true,
			AlarmUrgentHighMins:                  []int{30, 60, 90, 120},
			AlarmUrgentLow:                       true,
			AlarmUrgentLowMins:                   []int{15, 30, 45, 60},
			AlarmUrgentMins:                      []int{30, 60, 90, 120},
			AlarmWarnMins:                        []int{30, 60, 90, 120},
			AuthDefaultRoles:                     "readable",
			BaseURL:                              fmt.Sprintf("http://localhost:%d", port),
			CustomTitle:                          "Mock NightScout",
			DefaultFeatures:                      []string{"bgnow", "delta", "direction", "timeago"},
			EditMode:                             false,
			Enable:                               []string{"careportal", "bwp", "cage", "sage", "iage", "treatmentnotify"},
			FocusHours:                           3,
			Heartbeat:                            60,
			InsecureUseHTTP:                      true,
			Language:                             "en",
			NightMode:                            false,
			ScaleY:                               "log",
			SecureCSP:                            false,
			SecureHstsHeader:                     false,
			SecureHstsHeaderIncludeSubdomains:    false,
			SecureHstsHeaderPreload:              false,
			ShowForecast:                         "ar2",
			ShowPlugins:                          "bgnow delta direction timeago",
			ShowRawbg:                            "never",
			Theme:                                "default",
			Thresholds: Thresholds{
				BgHigh:         180,
				BgLow:          70,
				BgTargetBottom: 80,
				BgTargetTop:    160,
			},
			TimeFormat: 24,
			Units:      "mg/dl",
		},
		Status:  "ok",
		Version: "14.2.6",
	}
}

func corsMiddleware(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")

		if r.Method == "OPTIONS" {
			w.WriteHeader(http.StatusNoContent)
			return
		}

		next(w, r)
	}
}

func shouldSimulateError() bool {
	return errorRate > 0 && mrand.Float64() < errorRate
}

func incrementRequestCount() int64 {
	reqMutex.Lock()
	defer reqMutex.Unlock()
	reqCount++
	return reqCount
}

func handleEntries(w http.ResponseWriter, r *http.Request) {
	incrementRequestCount()

	if shouldSimulateError() {
		log.Printf("[%s] ❌ Simulating error (%d%% rate)", time.Now().Format(time.RFC3339), int(errorRate*100))
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusInternalServerError)
		json.NewEncoder(w).Encode(map[string]string{
			"error":   "Internal Server Error",
			"message": "Simulated error for testing",
		})
		return
	}

	countStr := r.URL.Query().Get("count")
	count := 20
	if countStr != "" {
		if c, err := strconv.Atoi(countStr); err == nil {
			count = c
		}
	}

	entries := buildEntries(count)
	lastEntry := entries[len(entries)-1]

	log.Printf("[%s] GET /api/v1/entries.json?count=%d → %d entries (last sgv=%d %s)",
		time.Now().Format(time.RFC3339), count, len(entries), lastEntry.Sgv, lastEntry.Direction)

	sendJSONWithDelay(w, entries)
}

func handleStatus(w http.ResponseWriter, r *http.Request) {
	incrementRequestCount()

	if shouldSimulateError() {
		log.Printf("[%s] ❌ Simulating error (%d%% rate)", time.Now().Format(time.RFC3339), int(errorRate*100))
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusInternalServerError)
		json.NewEncoder(w).Encode(map[string]string{
			"error":   "Internal Server Error",
			"message": "Simulated error for testing",
		})
		return
	}

	log.Printf("[%s] GET /api/v1/status.json", time.Now().Format(time.RFC3339))

	status := buildStatus()
	sendJSONWithDelay(w, status)
}

func handleHealth(w http.ResponseWriter, r *http.Request) {
	incrementRequestCount()

	log.Printf("[%s] GET /health", time.Now().Format(time.RFC3339))

	health := HealthCheck{
		Status:         "ok",
		Uptime:         time.Since(startTime).Seconds(),
		Timestamp:      time.Now().Format(time.RFC3339),
		Scenario:       scenario,
		LiveMode:       liveMode,
		CurrentGlucose: currentSgv,
		RequestCount:   reqCount,
	}

	sendJSONWithDelay(w, health)
}

func handleDashboard(w http.ResponseWriter, r *http.Request) {
	entries := buildEntries(5)
	latestEntry := entries[len(entries)-1]

	html := generateDashboard(latestEntry)
	w.Header().Set("Content-Type", "text/html")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(html))
}

func sendJSONWithDelay(w http.ResponseWriter, data interface{}) {
	if delay > 0 {
		time.Sleep(time.Duration(delay) * time.Millisecond)
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(data)
}

func generateDashboard(latestEntry GlucoseEntry) string {
	uptime := int(time.Since(startTime).Seconds())
	mode := "STATIC"
	if liveMode {
		mode = "LIVE"
	}

	return fmt.Sprintf(`<!DOCTYPE html>
<html>
<head>
  <title>Mock NightScout Dashboard</title>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
      min-height: 100vh;
      padding: 20px;
      color: #333;
    }
    .container {
      max-width: 1200px;
      margin: 0 auto;
    }
    .header {
      background: white;
      padding: 30px;
      border-radius: 15px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.2);
      margin-bottom: 20px;
    }
    h1 {
      color: #667eea;
      margin-bottom: 10px;
    }
    .status-badge {
      display: inline-block;
      padding: 5px 15px;
      background: #10b981;
      color: white;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 600;
    }
    .cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 20px;
      margin-bottom: 20px;
    }
    .card {
      background: white;
      padding: 25px;
      border-radius: 15px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.2);
    }
    .card h2 {
      color: #667eea;
      font-size: 18px;
      margin-bottom: 15px;
      border-bottom: 2px solid #f3f4f6;
      padding-bottom: 10px;
    }
    .metric {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #f3f4f6;
    }
    .metric:last-child { border-bottom: none; }
    .metric-label {
      color: #6b7280;
      font-weight: 500;
    }
    .metric-value {
      font-weight: 700;
      color: #111827;
    }
    .glucose-display {
      text-align: center;
      padding: 20px;
    }
    .glucose-value {
      font-size: 72px;
      font-weight: 800;
      color: %s;
      line-height: 1;
    }
    .glucose-trend {
      font-size: 24px;
      color: #6b7280;
      margin-top: 10px;
    }
    .glucose-time {
      font-size: 14px;
      color: #9ca3af;
      margin-top: 10px;
    }
    .endpoints {
      background: #1f2937;
      color: white;
      padding: 20px;
      border-radius: 10px;
      font-family: 'Courier New', monospace;
      font-size: 14px;
      margin-top: 10px;
    }
    .endpoints div {
      margin: 5px 0;
    }
    .refresh-info {
      text-align: center;
      color: white;
      margin-top: 20px;
      font-size: 14px;
    }
  </style>
  <script>
    // Auto-refresh every 5 seconds
    setTimeout(() => location.reload(), 5000);
  </script>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>🩺 Mock NightScout Server (Go)</h1>
      <span class="status-badge">● RUNNING</span>
    </div>

    <div class="cards">
      <div class="card">
        <h2>Current Glucose</h2>
        <div class="glucose-display">
          <div class="glucose-value">%d</div>
          <div class="glucose-trend">%s %s</div>
          <div class="glucose-time">%s</div>
        </div>
      </div>

      <div class="card">
        <h2>Server Status</h2>
        <div class="metric">
          <span class="metric-label">Port</span>
          <span class="metric-value">%d</span>
        </div>
        <div class="metric">
          <span class="metric-label">Mode</span>
          <span class="metric-value">%s</span>
        </div>
        <div class="metric">
          <span class="metric-label">Scenario</span>
          <span class="metric-value">%s</span>
        </div>
        <div class="metric">
          <span class="metric-label">Uptime</span>
          <span class="metric-value">%ds</span>
        </div>
        <div class="metric">
          <span class="metric-label">Requests</span>
          <span class="metric-value">%d</span>
        </div>
      </div>

      <div class="card">
        <h2>Configuration</h2>
        <div class="metric">
          <span class="metric-label">Network Delay</span>
          <span class="metric-value">%dms</span>
        </div>
        <div class="metric">
          <span class="metric-label">Error Rate</span>
          <span class="metric-value">%d%%</span>
        </div>
        <div class="metric">
          <span class="metric-label">API Version</span>
          <span class="metric-value">v1</span>
        </div>
      </div>
    </div>

    <div class="card">
      <h2>API Endpoints</h2>
      <div class="endpoints">
        <div>GET http://localhost:%d/api/v1/entries.json?count=20</div>
        <div>GET http://localhost:%d/api/v1/status.json</div>
        <div>GET http://localhost:%d/health</div>
      </div>
      <div style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #f3f4f6;">
        <div class="metric-label" style="margin-bottom: 10px;">Android Configuration:</div>
        <div class="endpoints">
          <div>Emulator: http://10.0.2.2:%d</div>
          <div>Physical Device: http://&lt;your-LAN-IP&gt;:%d</div>
        </div>
      </div>
    </div>

    <div class="refresh-info">
      ⟳ Auto-refreshing every 5 seconds
    </div>
  </div>
</body>
</html>`,
		getGlucoseColor(latestEntry.Sgv),
		latestEntry.Sgv,
		getTrendArrow(latestEntry.Direction),
		latestEntry.Direction,
		time.UnixMilli(latestEntry.Date).Format(time.Kitchen),
		port,
		mode,
		scenario,
		uptime,
		reqCount,
		delay,
		int(errorRate*100),
		port, port, port, port, port,
	)
}

func getGlucoseColor(sgv int) string {
	if sgv < 70 {
		return "#ef4444" // Low - Red
	} else if sgv < 80 {
		return "#f59e0b" // Low-ish - Orange
	} else if sgv <= 160 {
		return "#10b981" // Normal - Green
	} else if sgv <= 180 {
		return "#f59e0b" // High-ish - Orange
	}
	return "#ef4444" // High - Red
}

func getTrendArrow(direction string) string {
	arrows := map[string]string{
		"DoubleDown":     "⇊",
		"SingleDown":     "↓",
		"FortyFiveDown":  "↘",
		"Flat":           "→",
		"FortyFiveUp":    "↗",
		"SingleUp":       "↑",
		"DoubleUp":       "⇈",
	}
	if arrow, ok := arrows[direction]; ok {
		return arrow
	}
	return "→"
}

func getTLSCertificate() (tls.Certificate, error) {
	// If cert and key files are provided, load them
	if certFile != "" && keyFile != "" {
		return tls.LoadX509KeyPair(certFile, keyFile)
	}

	// Generate a self-signed certificate
	priv, err := ecdsa.GenerateKey(elliptic.P256(), rand.Reader)
	if err != nil {
		return tls.Certificate{}, fmt.Errorf("failed to generate private key: %w", err)
	}

	notBefore := time.Now()
	notAfter := notBefore.Add(365 * 24 * time.Hour) // Valid for 1 year

	serialNumber, err := rand.Int(rand.Reader, new(big.Int).Lsh(big.NewInt(1), 128))
	if err != nil {
		return tls.Certificate{}, fmt.Errorf("failed to generate serial number: %w", err)
	}

	template := x509.Certificate{
		SerialNumber: serialNumber,
		Subject: pkix.Name{
			Organization: []string{"Mock NightScout Server"},
			CommonName:   "localhost",
		},
		NotBefore:             notBefore,
		NotAfter:              notAfter,
		KeyUsage:              x509.KeyUsageKeyEncipherment | x509.KeyUsageDigitalSignature,
		ExtKeyUsage:           []x509.ExtKeyUsage{x509.ExtKeyUsageServerAuth},
		BasicConstraintsValid: true,
		DNSNames:              []string{"localhost"},
		IPAddresses:           []net.IP{net.ParseIP("127.0.0.1"), net.ParseIP("::1"), net.ParseIP("10.0.2.2")},
	}

	derBytes, err := x509.CreateCertificate(rand.Reader, &template, &template, &priv.PublicKey, priv)
	if err != nil {
		return tls.Certificate{}, fmt.Errorf("failed to create certificate: %w", err)
	}

	certPEM := pem.EncodeToMemory(&pem.Block{Type: "CERTIFICATE", Bytes: derBytes})

	privBytes, err := x509.MarshalECPrivateKey(priv)
	if err != nil {
		return tls.Certificate{}, fmt.Errorf("failed to marshal private key: %w", err)
	}
	keyPEM := pem.EncodeToMemory(&pem.Block{Type: "EC PRIVATE KEY", Bytes: privBytes})

	return tls.X509KeyPair(certPEM, keyPEM)
}

func printStartupInfo() {
	protocol := "http"
	if useHTTPS {
		protocol = "https"
	}

	fmt.Printf("\n🩺  Mock NightScout server (Go) running on port %d\n", port)
	fmt.Printf("   Protocol: %s\n", map[bool]string{true: "HTTPS (TLS 1.2+)", false: "HTTP"}[useHTTPS])
	fmt.Printf("   Mode    : %s\n", map[bool]string{true: "LIVE (random walk)", false: "STATIC (S-curve)"}[liveMode])
	fmt.Printf("   Scenario: %s\n", scenario)
	if delay > 0 {
		fmt.Printf("   Delay   : %dms\n", delay)
	}
	if errorRate > 0 {
		fmt.Printf("   Errors  : %.0f%% simulated error rate\n", errorRate*100)
	}
	if useHTTPS && certFile == "" {
		fmt.Printf("   TLS     : Self-signed certificate (auto-generated)\n")
	}
	fmt.Printf("\n   Configure the app's Base URL to:\n")
	fmt.Printf("   • Android Emulator  → %s://10.0.2.2:%d\n", protocol, port)
	fmt.Printf("   • Physical device   → %s://<your-LAN-IP>:%d\n", protocol, port)
	fmt.Printf("\n   Endpoints:\n")
	fmt.Printf("   • %s://localhost:%d/api/v1/entries.json?count=20\n", protocol, port)
	fmt.Printf("   • %s://localhost:%d/api/v1/status.json\n", protocol, port)
	fmt.Printf("   • %s://localhost:%d/health\n", protocol, port)
	fmt.Printf("\n   Dashboard:\n")
	fmt.Printf("   • %s://localhost:%d/dashboard\n\n", protocol, port)
	if useHTTPS {
		fmt.Printf("   ⚠️  Note: Self-signed certificate may require accepting security warnings\n")
		fmt.Printf("   or adding trust exception in your app/browser.\n\n")
	}
}

// Utility functions
func clamp(val, min, max int) int {
	if val < min {
		return min
	}
	if val > max {
		return max
	}
	return val
}

func min(a, b int) int {
	if a < b {
		return a
	}
	return b
}

func max(a, b int) int {
	if a > b {
		return a
	}
	return b
}

func abs(x int) int {
	if x < 0 {
		return -x
	}
	return x
}
