# Makefile for Mock NightScout Server (Go version)

.PHONY: build run run-https dev test-hypo test-hyper test-critical-low test-critical-high test-slow test-errors test-chaos clean help

# Build the server
build:
	go build -o mock-server main.go

# Run with default settings (HTTP, static normal glucose profile)
run: build
	./mock-server

# Run with HTTPS
run-https: build
	./mock-server -https

# Run with live simulation (values change over time)
dev: build
	./mock-server -live

# Test low glucose alerts
test-hypo: build
	./mock-server -live -scenario=hypo

# Test high glucose alerts
test-hyper: build
	./mock-server -live -scenario=hyper

# Test critical low scenario
test-critical-low: build
	./mock-server -live -scenario=critical-low

# Test critical high scenario
test-critical-high: build
	./mock-server -live -scenario=critical-high

# Test slow network
test-slow: build
	./mock-server -delay=2000

# Test error handling (20% error rate)
test-errors: build
	./mock-server -error-rate=0.2

# Test chaos mode (live + delay + errors)
test-chaos: build
	./mock-server -live -delay=1000 -error-rate=0.3

# Clean build artifacts
clean:
	rm -f mock-server

# Show help
help:
	@echo "Mock NightScout Server - Available Commands:"
	@echo ""
	@echo "  make build              - Build the server binary"
	@echo "  make run                - Run server with HTTP (default)"
	@echo "  make run-https          - Run server with HTTPS (self-signed cert)"
	@echo "  make dev                - Run with live simulation (values change)"
	@echo "  make test-hypo          - Test low glucose alerts"
	@echo "  make test-hyper         - Test high glucose alerts"
	@echo "  make test-critical-low  - Test critical low scenario"
	@echo "  make test-critical-high - Test critical high scenario"
	@echo "  make test-slow          - Test slow network (2s delay)"
	@echo "  make test-errors        - Test error handling (20% error rate)"
	@echo "  make test-chaos         - Test chaos mode (live + delay + errors)"
	@echo "  make clean              - Remove build artifacts"
	@echo "  make help               - Show this help message"
	@echo ""
	@echo "Command line options:"
	@echo "  -port=<n>              - Server port (default: 5001)"
	@echo "  -https                 - Enable HTTPS with self-signed cert (default: false)"
	@echo "  -cert=<file>           - Path to TLS certificate file"
	@echo "  -key=<file>            - Path to TLS key file"
	@echo "  -live                  - Enable live random walk simulation"
	@echo "  -scenario=<name>       - Glucose scenario (normal, hypo, hyper, critical-low, critical-high)"
	@echo "  -delay=<ms>            - Network delay in milliseconds"
	@echo "  -error-rate=<n>        - Error rate (0.0-1.0), e.g., 0.2 = 20%"
	@echo ""
	@echo "Examples:"
	@echo "  go run main.go                                    # HTTP (default)"
	@echo "  go run main.go -https                             # HTTPS with self-signed cert"
	@echo "  go run main.go -live -scenario=hypo               # Live hypoglycemia with HTTP"
	@echo "  go run main.go -https -cert=cert.pem -key=key.pem # Custom TLS certificate"

