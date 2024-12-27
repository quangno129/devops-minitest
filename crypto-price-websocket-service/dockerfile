# Stage 1: Build Stage - Install dependencies
FROM node:18-alpine AS builder

# Set the working directory
WORKDIR /usr/src/app

# Copy only the package files first for efficient layer caching
COPY package*.json ./

# Install all dependencies (production and development)
RUN npm install

# Copy the rest of the application code
COPY src/ ./src/
COPY config.json ./

# Stage 2: Production Stage - Create minimal runtime image
FROM node:18-alpine

# Set the working directory
WORKDIR /usr/src/app

# Copy the necessary files from the builder stage
COPY --from=builder /usr/src/app /usr/src/app

# Expose the application port
EXPOSE 8080

# Define environment variables
ENV NODE_ENV=production
ENV PORT=8080
ENV CONFIG_PATH=/usr/src/app/config.json

# Create a non-root user for better security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Run the application
CMD ["node", "src/server.js"]
