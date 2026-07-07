# ===========================
# Stage 1: Build with Node
# ===========================
FROM node:22-alpine AS builder

WORKDIR /build
COPY smart-edu-frontend/package.json smart-edu-frontend/package-lock.json* ./
RUN npm ci --legacy-peer-deps

COPY smart-edu-frontend .
RUN npm run build

# ===========================
# Stage 2: Serve with Nginx
# ===========================
FROM nginx:1.27-alpine

RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=3s --retries=3 \
    CMD curl -f http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
