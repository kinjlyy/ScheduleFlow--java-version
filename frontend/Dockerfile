# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Run stage
FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html

# Nginx alpine entrypoint automatically processes /etc/nginx/templates/*.template
# with envsubst and outputs to /etc/nginx/conf.d/
COPY nginx.conf /etc/nginx/templates/default.conf.template

# Default local fallback; override in Render Web Service environment variables:
# API_GATEWAY_URL = https://scheduleflow-api-gateway.onrender.com
ENV API_GATEWAY_URL=http://localhost:8082

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
