
# ================================
# Stage 1: Build the frontend
# ================================
FROM node:24-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Pass production URLs into Vite at build time.
ARG VITE_API_BASE_URL 
ARG VITE_WS_URL 
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL} 
ENV VITE_WS_URL=${VITE_WS_URL}

RUN npm run build


# ================================
# Stage 2: Run the Nitro server
# ================================
FROM node:24-alpine AS runtime

WORKDIR /app

ENV NODE_ENV=production
ENV NITRO_HOST=0.0.0.0
ENV NITRO_PORT=3000

COPY --from=build /app/.output ./.output

EXPOSE 3000

CMD ["node", ".output/server/index.mjs"]