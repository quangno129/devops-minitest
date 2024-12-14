const client = require('prom-client');

// Create a registry for metrics
const register = new client.Registry();

// Define custom metrics
const metrics = {
  // WebSocket connection metrics
  activeConnections: new client.Gauge({
    name: 'websocket_active_connections',
    help: 'Number of active WebSocket connections'
  }),

  connectionErrors: new client.Counter({
    name: 'websocket_connection_errors_total',
    help: 'Total number of WebSocket connection errors',
    labelNames: ['exchange', 'pair']
  }),

  dataProcessingLatency: new client.Histogram({
    name: 'websocket_data_processing_latency_seconds',
    help: 'Histogram of data processing latency',
    labelNames: ['exchange', 'pair'],
    buckets: [0.1, 0.5, 1, 2, 5, 10]
  }),

  // Connection attempts metrics
  connectionAttempts: new client.Counter({
    name: 'websocket_connection_attempts_total',
    help: 'Total number of WebSocket connection attempts',
    labelNames: ['exchange', 'pair', 'status']
  })
};

// Register metrics
Object.values(metrics).forEach(metric => register.registerMetric(metric));

// Metrics middleware
class MetricsService {
  constructor() {
    this.metrics = metrics;
    this.register = register;
  }

  // Track active WebSocket connections
  incrementActiveConnections(exchange, pair) {
    this.metrics.activeConnections.inc(1);
    return () => {
      this.metrics.activeConnections.dec(1);
    };
  }

  // Track connection errors
  recordConnectionError(exchange, pair) {
    this.metrics.connectionErrors.inc({
      exchange,
      pair
    });
  }

  // Track connection attempts
  recordConnectionAttempt(exchange, pair, status) {
    this.metrics.connectionAttempts.inc({
      exchange,
      pair,
      status
    });
  }

  // Track data processing latency
  measureDataProcessingLatency(exchange, pair, startTime) {
    const endTime = process.hrtime(startTime);
    const latencySeconds = endTime[0] + endTime[1] / 1e9;

    this.metrics.dataProcessingLatency.observe(
      { exchange, pair },
      latencySeconds
    );
  }

  // Get metrics endpoint handler
  async getMetrics(req, res) {
    try {
      res.set('Content-Type', register.contentType);
      res.end(await register.metrics());
    } catch (error) {
      res.status(500).send(error.message);
    }
  }
}

module.exports = new MetricsService();