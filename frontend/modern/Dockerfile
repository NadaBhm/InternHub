# ---- Build stage: Angular 15 requires Node 14/16/18 ----
FROM node:18-bullseye AS build
WORKDIR /app
ENV NODE_OPTIONS=--max-old-space-size=4096
COPY package.json package-lock.json .npmrc ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npx ng build --configuration production

# ---- Runtime stage: static files served by nginx ----
FROM nginx:1.27-alpine
# The nginx image renders /etc/nginx/templates/*.template with env vars (e.g. $PORT) at startup
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/velzon /usr/share/nginx/html
ENV PORT=8080
EXPOSE 8080
