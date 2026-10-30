/**
 * Production Environment Configuration for Vercel Deployment.
 *
 * Safe dual-mode configuration:
 * 1. Runtime injection via window.__STOCKSMART_API_BASE_URL__ if specified.
 * 2. Defaults to '/api', which works with Vercel rewrites (proxying to Render)
 *    or can be set directly to your Render backend URL:
 *    e.g. 'https://stocksmart-backend.onrender.com/api'
 */

export const environment = {
  production: true,
  apiBaseUrl: 'https://stocksmart-it29.onrender.com/api'
};
