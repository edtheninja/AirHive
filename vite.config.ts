import { defineConfig } from "@lovable.dev/vite-tanstack-config";

export default defineConfig({
  // Use a Node.js-compatible Nitro server for Docker.
  nitro: {
    preset: "node-server",
  },

  tanstackStart: {
    // Preserve the existing SSR error wrapper.
    server: { entry: "server" },
  },
});
