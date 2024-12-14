const winston = require('winston');

const logger = winston.createLogger({
  level: 'info',
  format: winston.format.printf(({ message }) => message),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ filename: 'app.log' })
  ]
});

module.exports = {
  logPriceUpdate: (exchange, pair, price) => {
    const timestamp = new Date().toISOString();
    logger.info(`[${timestamp}] Exchange: ${exchange}, Market Pair: ${pair}, Price: ${price}`);
  },
  logError: (exchange, pair, errorMessage) => {
    const timestamp = new Date().toISOString();
    logger.error(`[${timestamp}] Error reconnecting to ${exchange}, Market Pair: ${pair}: ${errorMessage}`);
  }
};