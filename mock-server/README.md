# Mock NightScout Server

A high-performance, feature-rich mock NightScout server written in Go for local development and testing of the WearableMonitor Android app.

> **Note**: For automated unit/integration tests, consider using **Ktor MockEngine** instead. See [TESTING-STRATEGIES.md](../TESTING-STRATEGIES.md) for guidance on when to use each approach.

## Features

✅ **HTTP by Default**: Simple setup with optional HTTPS support
✅ **HTTPS Support**: Built-in TLS with auto-generated self-signed certificates
✅ **No Dependencies**: Single binary with no runtime dependencies
✅ **Fast Startup**: Sub-second startup time (<100ms)
✅ **Low Memory**: ~10MB memory footprint
✅ **Easy Distribution**: Single executable file
✅ **Cross-Platform**: Build for any OS/architecture

✅ **Full NightScout API v1 Support**
- `GET /api/v1/entries.json?count=<n>` - Glucose entries
- `GET /api/v1/status.json` - Server status and settings
- `GET /health` - Health check endpoint

✅ **Multiple Simulation Modes**
- **Static Mode**: Deterministic glucose profiles for consistent testing
- **Live Mode**: Random walk simulation with realistic glucose variations

✅ **Testing Scenarios**
- `normal` - Normal glucose range (80-170 mg/dL)
- `hypo` - Hypoglycemia scenario (55-75 mg/dL)
- `hyper` - Hyperglycemia scenario (200-280 mg/dL)
- `critical-low` - Critical low (40-60 mg/dL)
- `critical-high` - Critical high (300-400 mg/dL)

✅ **Network Simulation**
- Configurable network delay
- Configurable error rate for testing error handling

✅ **Web Dashboard**
- Real-time glucose monitoring
- Server status and metrics
- Auto-refreshing every 5 seconds
- Visual trend indicators and color-coded glucose levels

## Installation

### Prerequisites
- Go 1.21 or later

### Build
```bash
cd mock-server
make build
```

Or build manually:
```bash
go build -o mock-server main.go
```

## Usage

### Using Make (Recommended)

```bash
# Start server with HTTP (default)
make run

# Start server with HTTPS
make run-https

# Start server with live simulation
make dev

# Test different scenarios
make test-hypo
make test-hyper
make test-critical-low
make test-critical-high

# Test network conditions
make test-slow
make test-errors
make test-chaos

# See all available commands
make help
```

### Using Go Run

```bash
# Start server with static normal glucose profile
go run main.go

# Start server with live simulation
go run main.go -live

# Test different scenarios
go run main.go -scenario=hypo
go run main.go -scenario=hyper
go run main.go -scenario=critical-low
go run main.go -scenario=critical-high

# Live mode with scenario
go run main.go -live -scenario=hypo
```

### Using the Binary

```bash
# Build first
make build

# Run with various options
./mock-server
./mock-server -live
./mock-server -scenario=hypo -live
```

### Network Simulation

```bash
# Add 1 second network delay
go run main.go -delay=1000

# Simulate 20% error rate
go run main.go -error-rate=0.2

# Combine delay and errors
go run main.go -delay=500 -error-rate=0.1
```

### Custom Port

```bash
# Run on custom port
go run main.go -port=8080
```

### Combined Options

```bash
# Live mode + hypoglycemia + 500ms delay + 10% errors
go run main.go -live -scenario=hypo -delay=500 -error-rate=0.1
```

## Android App Configuration

### Android Emulator

Configure the app's Base URL to:
```
http://10.0.2.2:5001
```

### Physical Device

1. Find your computer's local IP address:
   ```bash
   # macOS
   ipconfig getifaddr en0

   # Linux
   hostname -I | awk '{print $1}'

   # Or use ifconfig
   ifconfig | grep "inet " | grep -v 127.0.0.1
   ```

2. Ensure your phone and computer are on the same Wi-Fi network

3. Configure the app's Base URL to:
   ```
   http://<your-LAN-IP>:5001
   ```
   Example: `http://192.168.1.100:5001`

## HTTPS Configuration (Optional)

For production-like testing, you can enable HTTPS. The server can auto-generate a self-signed certificate or use your own.

### Enabling HTTPS with Self-Signed Certificate

```bash
# Enable HTTPS
./mock-server -https

# Or explicitly
go run main.go -https=true
```

**Android Configuration:**
- Emulator: `https://10.0.2.2:5001`
- Physical: `https://<your-ip>:5001`

**Note**: Self-signed certificates require accepting a security warning or adding a trust exception in your Android app. See `ANDROID-HTTPS-SETUP.md` for configuration details.

### Using Custom TLS Certificate

For production-like testing, provide your own certificate:

```bash
# Generate a certificate (example)
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes

# Run with custom certificate (HTTPS auto-enabled)
./mock-server -cert=cert.pem -key=key.pem
```


## Web Dashboard

Open your browser to view the real-time dashboard:
```
http://localhost:5001/dashboard
```

The dashboard shows:
- Current glucose value with trend arrow
- Server status and configuration
- Request count and uptime
- API endpoint URLs
- Android configuration instructions

## API Endpoints

### Get Glucose Entries
```
GET /api/v1/entries.json?count=20
```

Returns an array of glucose readings with:
- `sgv`: Glucose value (mg/dL)
- `date`: Timestamp (Unix epoch milliseconds)
- `dateString`: ISO 8601 timestamp
- `trend`: Numeric trend (1-7)
- `direction`: Trend direction string
- `type`: Entry type ("sgv")

### Get Server Status
```
GET /api/v1/status.json
```

Returns server configuration including:
- API status
- Alarm settings
- Thresholds (bgHigh, bgLow, etc.)
- Units (mg/dl)
- Server time

### Health Check
```
GET /health
```

Returns:
- Server uptime
- Current scenario
- Live mode status
- Current glucose value
- Request count

## Command Line Options

| Option | Default | Description |
|--------|---------|-------------|
| `-port=<n>` | `5001` | Server port |
| `-https` | `false` | Enable HTTPS with TLS 1.2+ |
| `-cert=<file>` | `""` | Path to TLS certificate file (auto-generated if not provided) |
| `-key=<file>` | `""` | Path to TLS key file (auto-generated if not provided) |
| `-live` | `false` | Enable live random walk simulation |
| `-scenario=<name>` | `normal` | Glucose scenario (normal, hypo, hyper, critical-low, critical-high) |
| `-delay=<ms>` | `0` | Network delay in milliseconds |
| `-error-rate=<n>` | `0` | Error rate (0.0-1.0), e.g., 0.2 = 20% |

## Glucose Scenarios

### Normal (default)
- Range: 80-170 mg/dL
- Pattern: S-curve in static mode, random walk in live mode
- Trend: Mostly Flat
- Use case: Testing normal operation

### Hypoglycemia (hypo)
- Range: 55-75 mg/dL
- Pattern: Low values with slight upward bias
- Trend: Initially DoubleDown, may stabilize
- Use case: Testing low glucose notifications and UI

### Hyperglycemia (hyper)
- Range: 200-280 mg/dL
- Pattern: High values with slight downward bias
- Trend: Initially DoubleUp, may stabilize
- Use case: Testing high glucose notifications and UI

### Critical Low (critical-low)
- Range: 40-60 mg/dL
- Pattern: Very low, slowly recovering
- Trend: DoubleDown
- Use case: Testing emergency alerts

### Critical High (critical-high)
- Range: 300-400 mg/dL
- Pattern: Very high, slowly declining
- Trend: DoubleUp
- Use case: Testing emergency alerts

## Development Tips

### Testing Error Handling
```bash
# Test network errors
go run main.go -error-rate=0.5

# Test slow network
go run main.go -delay=3000

# Test combination of issues
go run main.go -delay=2000 -error-rate=0.3
```

### Testing Live Updates
```bash
# Start in live mode and watch values change
go run main.go -live

# Watch hypoglycemia progression
go run main.go -live -scenario=hypo
```

### Monitoring Requests
The server logs all requests with timestamps:
```
[2026-04-04T10:30:15-07:00] GET /api/v1/entries.json?count=20 → 20 entries (last sgv=110 Flat)
[2026-04-04T10:30:15-07:00] GET /api/v1/status.json
```

## Performance

- **Startup**: <100ms
- **Memory**: ~10MB
- **Response Time**: <1ms (without artificial delay)
- **Binary Size**: ~7.5MB (single file with no dependencies)

## Cross-Compilation

Build for different platforms:

```bash
# macOS (ARM - M1/M2/M3)
GOOS=darwin GOARCH=arm64 go build -o mock-server-darwin-arm64 main.go

# macOS (Intel)
GOOS=darwin GOARCH=amd64 go build -o mock-server-darwin-amd64 main.go

# Linux
GOOS=linux GOARCH=amd64 go build -o mock-server-linux-amd64 main.go

# Windows
GOOS=windows GOARCH=amd64 go build -o mock-server-windows-amd64.exe main.go

# Raspberry Pi
GOOS=linux GOARCH=arm64 go build -o mock-server-linux-arm64 main.go
```

## Troubleshooting

### Connection Refused on Physical Device

1. Verify your computer's firewall allows incoming connections on port 5001
2. Confirm both devices are on the same Wi-Fi network
3. Try using your computer's IP address instead of hostname

### App Can't Connect from Emulator

Make sure you're using `10.0.2.2` (not `localhost` or `127.0.0.1`)

### Dashboard Not Updating

- Ensure JavaScript is enabled in your browser
- Check browser console for errors
- The page auto-refreshes every 5 seconds

### Port Already in Use

```bash
# Kill existing process
pkill -f mock-server

# Or use a different port
go run main.go -port=8080
```

## CORS Support

The server includes full CORS support with these headers:
- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS`
- `Access-Control-Allow-Headers: Content-Type, Authorization`

This allows the app to make API calls from any origin during development.

## License

Apache-2.0 (same as parent project)

