# Android App Configuration for HTTPS Mock Server

This guide explains how to configure your Android app to work with the HTTPS-enabled mock server.

## Quick Start

The mock server now uses **HTTPS by default** with a self-signed certificate. You have two options:

### Option 1: Use HTTP Mode (Easiest for Quick Testing)

Start the server without HTTPS:
```bash
cd mock-server
./mock-server -https=false
```

Configure your Android app with:
- **Emulator**: `http://10.0.2.2:5001`
- **Physical Device**: `http://<your-computer-ip>:5001`

### Option 2: Configure Android to Trust Self-Signed Certificates (Recommended)

This is more realistic for production testing but requires Android configuration.

## Configuring Android for Self-Signed Certificates

### Step 1: Add Network Security Configuration

Create `app/src/main/res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- For debug builds, trust user-installed certificates -->
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
            <certificates src="system" />
        </trust-anchors>
    </debug-overrides>

    <!-- For specific domains in debug (alternative approach) -->
    <!--
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">192.168.1.100</domain>
        <trust-anchors>
            <certificates src="user" />
            <certificates src="system" />
        </trust-anchors>
    </domain-config>
    -->
</network-security-config>
```

### Step 2: Reference in AndroidManifest.xml

Add to your `<application>` tag in `AndroidManifest.xml`:

```xml
<application
    android:name=".WearableMonitorApplication"
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

### Step 3: Configure App URLs

Use HTTPS URLs in your app:
- **Emulator**: `https://10.0.2.2:5001`
- **Physical Device**: `https://<your-computer-ip>:5001`

### Step 4: Accept Security Warning (First Time)

When you first connect, you may need to accept the security warning:
- The certificate is self-signed (not from a trusted CA)
- This is expected for local development
- The connection is still encrypted

## Testing HTTPS Connection

### Start the Server

```bash
cd mock-server
./mock-server
```

You should see:
```
🩺  Mock NightScout server (Go) running on port 5001
   Protocol: HTTPS (TLS 1.2+)
   Mode    : STATIC (S-curve)
   Scenario: normal
   TLS     : Self-signed certificate (auto-generated)

   Configure the app's Base URL to:
   • Android Emulator  → https://10.0.2.2:5001
   • Physical device   → https://<your-LAN-IP>:5001
```

### Test from Command Line

```bash
# From your computer (with -k to accept self-signed cert)
curl -k https://localhost:5001/health

# From Android emulator (if adb is available)
adb shell curl -k https://10.0.2.2:5001/health
```

## Troubleshooting

### "SSL handshake failed" or "Certificate not trusted"

**Solution**: Make sure you've added the network security configuration (Steps 1-2 above).

### "Cleartext HTTP traffic not permitted"

**Solution**: Either:
1. Add network security config to allow your server's IP
2. Or use HTTP mode: `./mock-server -https=false`

### Can't connect from Android app

1. **Check the URL**: Make sure it starts with `https://` (or `http://` if using `-https=false`)
2. **Verify network security config**: Ensure `network_security_config.xml` exists and is referenced
3. **Check firewall**: Allow port 5001 on your computer
4. **Same network**: Ensure phone and computer are on same Wi-Fi
5. **Try HTTP mode**: Use `./mock-server -https=false` to rule out TLS issues

### Certificate hostname mismatch

The self-signed certificate includes:
- `localhost`
- `127.0.0.1`
- `10.0.2.2` (Android emulator IP)

If you're using a different IP address, the hostname won't match. This is expected for self-signed certificates in development.

## Production Certificates (Optional)

For more realistic testing, you can use a real certificate:

### Option 1: Use mkcert (Easy)

```bash
# Install mkcert
brew install mkcert  # macOS
# or follow instructions at https://github.com/FiloSottile/mkcert

# Create local CA
mkcert -install

# Generate certificate
mkcert localhost 127.0.0.1 10.0.2.2 ::1

# Start server with certificate
./mock-server -cert=localhost+3.pem -key=localhost+3-key.pem
```

### Option 2: Use OpenSSL

```bash
# Generate certificate
openssl req -x509 -newkey rsa:4096 \
  -keyout key.pem -out cert.pem \
  -days 365 -nodes \
  -subj "/CN=localhost"

# Start server
./mock-server -cert=cert.pem -key=key.pem
```

## Security Best Practices

### Development (Current Setup)
✅ Use self-signed certificates
✅ Network security config in debug builds only
✅ Accept user certificates in debug
❌ Don't ship debug config to production

### Production
✅ Use certificates from trusted CA
✅ Enable certificate pinning
✅ Remove debug overrides
✅ Validate hostnames

## Example: Complete Android Setup

### 1. Create Network Security Config

`app/src/debug/res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
            <certificates src="system" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

### 2. Update AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:networkSecurityConfig="@xml/network_security_config"
        ...>
        ...
    </application>
</manifest>
```

### 3. Configure Base URL in App

```kotlin
// In your settings or configuration
const val BASE_URL = "https://10.0.2.2:5001"  // For emulator
// or
const val BASE_URL = "https://192.168.1.100:5001"  // For physical device
```

### 4. Test Connection

```kotlin
// Your existing network call should work
// The network security config will handle the self-signed cert
```

## Summary

**For Quick Testing:**
- Use HTTP mode: `./mock-server -https=false`
- Configure app with `http://10.0.2.2:5001`

**For Realistic Testing:**
- Use HTTPS mode (default): `./mock-server`
- Add network security configuration to your Android app
- Configure app with `https://10.0.2.2:5001`

**Both modes provide identical API responses** - the only difference is the transport layer security.

## Need Help?

- Check server logs for connection attempts
- Use `make run-http` for HTTP mode
- See [README.md](README.md) for full server documentation
- See [QUICKSTART.md](QUICKSTART.md) for quick setup guide

