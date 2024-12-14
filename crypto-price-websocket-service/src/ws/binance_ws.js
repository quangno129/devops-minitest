const WebSocket = require('ws');
const { logPriceUpdate, logError } = require('../log_utils');
const metricsService = require('../metrics_middleware');

class BinanceWebSocketAdapter {
  constructor(pair) {
    this.pair = pair.toLowerCase().replace('/', '');
    this.websocketUrl = `wss://stream.binance.com:9443/ws/${this.pair}@ticker`;
    this.ws = null;
    this.exchange = 'Binance';
  }

  connect() {
    // Track connection attempt
    metricsService.recordConnectionAttempt(this.exchange, this.pair, 'initiated');

    // Track active connections
    const decrementConnections = metricsService.incrementActiveConnections(
      this.exchange, 
      this.pair
    );

    this.ws = new WebSocket(this.websocketUrl);

    this.ws.on('open', () => {
      metricsService.recordConnectionAttempt(this.exchange, this.pair, 'success');
      console.log(`Connected to Binance WebSocket for ${this.pair}`);
    });

    this.ws.on('message', (data) => {
      // Measure data processing latency
      const startTime = process.hrtime();

      try {
        const parsedData = JSON.parse(data);
        const price = parseFloat(parsedData.c);
        logPriceUpdate('Binance', this.pair, price);

        // Record processing latency
        metricsService.measureDataProcessingLatency(
          this.exchange, 
          this.pair, 
          startTime
        );
      } catch (error) {
        console.error('Error processing Binance message:', error);
      }
    });

    this.ws.on('error', (error) => {
      // Track connection errors
      metricsService.recordConnectionError(this.exchange, this.pair);

      logError('Binance', this.pair, error.message);
      this.reconnect();
    });

    this.ws.on('close', () => {
      // Decrement active connections
      decrementConnections();
      this.reconnect();
    });
  }

  reconnect() {
    setTimeout(() => {
      console.log(`Attempting to reconnect to Binance WebSocket for ${this.pair}`);
      this.connect();
    }, 5000);
  }

  close() {
    if (this.ws) {
      this.ws.close();
    }
  }
}

module.exports = BinanceWebSocketAdapter;