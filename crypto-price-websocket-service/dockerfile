# Use an official Node.js runtime as the base image
FROM node:18-alpine

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application code
COPY src/ ./src/
COPY config.json ./

# Expose the port the app runs on
EXPOSE 8080

# Define environment variables
ENV NODE_ENV=production
ENV PORT=8080
ENV CONFIG_PATH=/usr/src/app/config.json

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Command to run the application
CMD ["npm", "start"]