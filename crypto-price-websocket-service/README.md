### Install and run
1. Install dependencies: `npm install`
2. Start the server: `npm start`

### WebSocket Connection Examples:
- Specific pair: `ws://localhost:8080?exchange=Binance&pair=BTC/USDT`
- All pairs: `ws://localhost:8080`
- Metrics: `http://localhost:8080/metrics`

### Docker build
- Build image: `docker build -t crypto-websocket-service .`
- Run: `docker run -dp 8080:8080 crypto-websocket-service`