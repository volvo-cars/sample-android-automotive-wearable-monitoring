# Quick Start Guide

## 1. Build the Server

```bash
cd mock-server
make build
```

Or manually:
```bash
go build -o mock-server main.go
```

The server will compile into a single binary with no dependencies.

## 2. Start the Server

```bash
# Using make (recommended)
make run

# Or directly
./mock-server

# Or with go run
go run main.go
```

The server will start on port 5001 with default settings (HTTP, static normal glucose profile).

## 3. View the Dashboard

Open your browser:
```
http://localhost:5001/dashboard
```

**Optional**: To enable HTTPS, start with `./mock-server -https` (requires accepting security warning for self-signed certificate).

You'll see a real-time dashboard showing:
- Current glucose value
- Server status and configuration
- Request metrics
- API endpoints

## 4. Configure Your Android App

### For Android Emulator:
1. Open the WearableMonitor app
2. Go to Settings
3. Set Base URL to: `http://10.0.2.2:5001`
4. Save settings

**Optional**: If using `-https` mode, use `https://10.0.2.2:5001` and configure network security (see `ANDROID-HTTPS-SETUP.md`)

### For Physical Device:
1. Find your computer's IP address:
   ```bash
   # macOS
   ipconfig getifaddr en0

   # Linux
   hostname -I | awk '{print $1}'

   # Or use ifconfig
   ifconfig | grep "inet " | grep -v 127.0.0.1
   ```

2. In the app, set Base URL to: `http://YOUR_IP:5001`
   - Example: `http://192.168.1.100:5001`

3. Make sure your phone and computer are on the same Wi-Fi network

**Optional**: If using `-https` mode, use `https://YOUR_IP:5001` and see `ANDROID-HTTPS-SETUP.md` for configuration

## 5. Test Different Scenarios

Try these make commands for common scenarios:

```bash
# Live simulation (values change over time)
make dev

# Test low glucose alerts
make test-hypo

# Test high glucose alerts
make test-hyper

# Test critical low scenario
make test-critical-low

# Test critical high scenario
make test-critical-high

# Test slow network
make test-slow

# Test error handling (20% error rate)
make test-errors

# Test chaos mode (live + delay + errors)
make test-chaos
```

Or use command-line flags directly:

```bash
go run main.go -live -scenario=hypo
go run main.go -delay=2000
go run main.go -error-rate=0.2
go run main.go -live -delay=1000 -error-rate=0.3
```

## 6. Verify It's Working

Test the endpoints manually:

```bash
# Health check
curl http://localhost:5001/health

# Get glucose entries
curl "http://localhost:5001/api/v1/entries.json?count=5"

# Get server status
curl http://localhost:5001/api/v1/status.json
```

## 7. Stop the Server

Press `Ctrl+C` in the terminal where the server is running.

Or kill the process:
```bash
pkill -f mock-server
```

## Command Line Options

All options use single dash (`-`) prefix:

```bash
# Basic options
go run main.go -port=8080                    # Custom port
go run main.go -live                         # Live simulation mode
go run main.go -scenario=hypo                # Specific scenario
go run main.go -delay=1000                   # Network delay (ms)
go run main.go -error-rate=0.2               # 20% error rate

# Combined options
go run main.go -live -scenario=hyper -delay=500 -error-rate=0.1
```

## Available Scenarios

- `normal` - Normal glucose range (80-170 mg/dL)
- `hypo` - Hypoglycemia (55-75 mg/dL)
- `hyper` - Hyperglycemia (200-280 mg/dL)
- `critical-low` - Critical low (40-60 mg/dL)
- `critical-high` - Critical high (300-400 mg/dL)

## Common Issues

### Port Already in Use
If you see "bind: address already in use" error:
```bash
# Kill existing server
pkill -f mock-server

# Or use a different port
go run main.go -port=8080
```

### Can't Connect from Android Device
1. Check firewall settings (allow port 5001)
2. Verify both devices are on same Wi-Fi
3. Try pinging your computer from your phone
4. Make sure you're using the correct IP address

### No Data Showing in App
1. Check the server logs for requests
2. Verify the Base URL in app settings
3. Check for error messages in the app
4. Try the `/health` endpoint to confirm connectivity

### Go Not Installed
If you don't have Go installed:

**macOS:**
```bash
brew install go
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install golang-go
```

**Other platforms:**
Download from https://go.dev/dl/

## Performance Benefits

- ✅ **Fast startup**: <100ms
- ✅ **Low memory**: ~10MB
- ✅ **Single binary**: No external dependencies
- ✅ **Easy distribution**: Just copy the binary

## Next Steps

- Read [README.md](README.md) for detailed documentation
- Explore different command-line options
- Check the web dashboard for real-time monitoring
- Try cross-compiling for different platforms

## Make Commands Reference

```bash
make build              # Build the server binary
make run                # Run with static normal glucose
make dev                # Run with live simulation
make test-hypo          # Test hypoglycemia scenario
make test-hyper         # Test hyperglycemia scenario
make test-critical-low  # Test critical low scenario
make test-critical-high # Test critical high scenario
make test-slow          # Test slow network (2s delay)
make test-errors        # Test 20% error rate
make test-chaos         # Test chaos mode
make clean              # Remove build artifacts
make help               # Show all available commands
```

