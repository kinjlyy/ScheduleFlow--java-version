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

# Default Render backend URL fallback; override in Render Web Service environment variables if needed
ENV API_GATEWAY_URL=https://scheduleflow-java-version-1.onrender.com

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
