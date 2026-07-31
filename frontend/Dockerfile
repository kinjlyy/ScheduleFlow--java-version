# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# These VITE_ vars are baked into the JS bundle at build time by Vite.
# Override them in Render's "Build Command" or Dockerfile if URLs change.
ARG VITE_TIMETABLE_API_URL=https://scheduleflow-backend.onrender.com
ARG VITE_EVENT_API_URL=https://scheduleflow-backend.onrender.com
ENV VITE_TIMETABLE_API_URL=$VITE_TIMETABLE_API_URL
ENV VITE_EVENT_API_URL=$VITE_EVENT_API_URL

RUN npm run build

# Run stage
FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html

# Nginx alpine entrypoint automatically processes /etc/nginx/templates/*.template
# with envsubst and outputs to /etc/nginx/conf.d/
COPY nginx.conf /etc/nginx/templates/default.conf.template

# This proxies /api/rooms/* → Resource Service. Timetable/Event go direct via JS.
ENV API_GATEWAY_URL=https://scheduleflow-java-version-1.onrender.com

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
