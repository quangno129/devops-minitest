const WebSocket = require('ws');
const http = require('http');
const url = require('url');
const fs = require('fs');
const path = require('path');
const express = require('express');
const metricsService = require('./metrics_middleware');

const BinanceWebSocketAdapter = require('./ws/binance_ws');
const BybitWebSocketAdapter = require('./ws/bybit_ws');

require('dotenv').config();

// Use environment variable with a default fallback
const configPath = process.env.CONFIG_PATH || path.join(__dirname, '..', 'config.json');
const config = JSON.parse(fs.readFileSync(configPath, 'utf8'));

console.log(`configPath ${configPath}`);

class WebSocketServer {
  constructor() {
    this.exchanges = {};
    this.activeConnections = new Map();

    // Create Express app for metrics
    this.app = express();
    this.setupMetricsRoutes();
  }

  setupMetricsRoutes() {
    // Prometheus metrics endpoint
    this.app.get('/metrics', metricsService.getMetrics.bind(metricsService));

    // Health check endpoint
    this.app.get('/health', (req, res) => {
      res.json({
        status: 'OK',
        timestamp: new Date().toISOString(),
        activeConnections: this.activeConnections.size
      });
    });
  }

  initializeExchanges() {
    config.exchanges.forEach(exchange => {
      this.exchanges[exchange.name] = exchange.pairs;
    });
  }

  startServer(port = 8080) {
    this.initializeExchanges();
    // Create HTTP server
    const httpServer = http.createServer(this.app);
    const wss = new WebSocket.Server({ server: httpServer });

    wss.on('connection', (ws, req) => {
      const { query } = url.parse(req.url, true);
      const { exchange, pair } = query;

      if (exchange && pair) {
        this.handleSpecificConnection(ws, exchange, pair);
      } else {
        this.handleAllExchangesConnection(ws);
      }
    });

    // Start the server
    httpServer.listen(port, () => {
      console.log(`WebSocket server running on port ${port}`);
      console.log(`Metrics available at http://localhost:${port}/metrics`);
    });
  }

  handleSpecificConnection(ws, exchange, pair) {
    if (!this.exchanges[exchange] || !this.exchanges[exchange].includes(pair)) {
      ws.close();
      return;
    }

    const adapter = this.createWebSocketAdapter(exchange, pair);
    if (adapter) {
      adapter.connect();
      this.activeConnections.set(ws, adapter);

      ws.on('close', () => {
        adapter.close();
        this.activeConnections.delete(ws);
      });
    }
  }

  handleAllExchangesConnection(ws) {
    const adapters = [];

    Object.entries(this.exchanges).forEach(([exchangeName, pairs]) => {
      pairs.forEach(pair => {
        const adapter = this.createWebSocketAdapter(exchangeName, pair);
        if (adapter) {
          adapter.connect();
          adapters.push(adapter);
        }
      });
    });

    this.activeConnections.set(ws, adapters);

    ws.on('close', () => {
      adapters.forEach(adapter => adapter.close());
      this.activeConnections.delete(ws);
    });
  }

  createWebSocketAdapter(exchange, pair) {
    switch (exchange) {
      case 'Binance':
        return new BinanceWebSocketAdapter(pair);
      case 'Bybit':
        return new BybitWebSocketAdapter(pair);
      default:
        console.error(`Unsupported exchange: ${exchange}`);
        return null;
    }
  }
}

const server = new WebSocketServer();
server.startServer();