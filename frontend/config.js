// SharveshMart Frontend - API base config for split deployment
// Railway: set BACKEND_URL env var in frontend service, or replace at build time
// Local dev: defaults to same origin (relative /api) when backend runs on same host
(function() {
  // Allow override via window.BACKEND_URL injected by Railway or local config
  // e.g. https://sharveshmart-backend.up.railway.app
  const envBackend = (typeof window !== 'undefined' && window.BACKEND_URL) || '';
  // If frontend and backend are separate Railway services, set this:
  // In Railway dashboard: frontend service -> Variables -> BACKEND_URL=https://<backend>.railway.app
  // For local split dev: export BACKEND_URL=http://localhost:8080
  window.API_BASE = (envBackend || '').replace(/\/$/, '') || '';
  // Helper used by app.js
  window.apiUrl = function(path) {
    // path is like "/api/products" - prefix with API_BASE if set
    if (!path.startsWith('/')) path = '/' + path;
    return window.API_BASE + path;
  };
})();
