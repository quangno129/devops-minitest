const WebSocket = require('ws');
const { logPriceUpdate, logError } = require('../log_utils');
const metricsService = require('../metrics_middleware');

class BybitWebSocketAdapter {
  constructor(pair) {
    this.pair = pair.toUpperCase().replace('/', '');
    this.websocketUrl = 'wss://stream.bybit.com/v5/public/spot';
    this.ws = null;
    this.exchange = 'Bybit';
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

      const subscribeMessage = {
        op: 'subscribe',
        args: [`tickers.${this.pair}`]
      };
      this.ws.send(JSON.stringify(subscribeMessage));
      console.log(`Connected to Bybit WebSocket for ${this.pair}`);
    });

    this.ws.on('message', (data) => {
      // Measure data processing latency
      const startTime = process.hrtime();

      try {
        const parsedData = JSON.parse(data);
        if (parsedData.topic === `tickers.${this.pair}`) {
          const price = parseFloat(parsedData.data.lastPrice);
          logPriceUpdate('Bybit', this.pair, price);

          // Record processing latency
          metricsService.measureDataProcessingLatency(
            this.exchange, 
            this.pair, 
            startTime
          );
        }
      } catch (error) {
        console.error('Error processing Bybit message:', error);
      }
    });

    this.ws.on('error', (error) => {
      // Track connection errors
      metricsService.recordConnectionError(this.exchange, this.pair);

      logError('Bybit', this.pair, error.message);
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
      console.log(`Attempting to reconnect to Bybit WebSocket for ${this.pair}`);
      this.connect();
    }, 5000);
  }

  close() {
    if (this.ws) {
      this.ws.close();
    }
  }
}

module.exports = BybitWebSocketAdapter;