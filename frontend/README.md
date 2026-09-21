# SharveshMart Frontend

Vanilla SPA (HTML5 + CSS3 Glassmorphic + Vanilla JS Fetch API) extracted from monolith `src/main/resources/static`.

## Structure
```
frontend/
├── index.html          # SPA shell (was src/main/resources/static/index.html)
├── css/styles.css      # 24KB design system
├── js/app.js           # 72KB client engine (uses window.apiUrl)
├── config.js           # API base: window.API_BASE = BACKEND_URL || ''
├── package.json        # serve -s . -l $PORT
└── railway.toml        # Nixpacks nodejs, serve static
```

## API Wiring
`config.js` defines `window.API_BASE` from `window.BACKEND_URL`. `js/app.js` wraps all `fetch('/api/...')` via `apiUrl(path)`:
- **Monolith** (backend serves static): `BACKEND_URL=""` → `fetch('/api/...')` same origin
- **Split** (Railway two services): `BACKEND_URL=https://<backend>.railway.app` → `fetch('https://<backend>.../api/...')`

Set in Railway: **frontend service → Variables → `BACKEND_URL=https://sharveshmart-backend.up.railway.app`**

Local split dev:
```bat
:: backend on 8080
cd backend && mvn spring-boot:run
:: frontend on 3000
cd frontend
set BACKEND_URL=http://localhost:8080
npx serve -s . -l 3000
:: open http://localhost:3000
```

## Deploy to Railway (two services, same repo)
1. Create Railway project → **New Service → GitHub Repo → `Sharvesh-Mart-2` → Root Directory = `backend`** → Deploy (uses `backend/railway.toml` + `backend/nixpacks.toml`)
2. **New Service → Same Repo → Root Directory = `frontend`** → Deploy (uses `frontend/railway.toml`)
3. In **frontend service → Variables** add `BACKEND_URL=https://<backend-service>.up.railway.app`
4. In **backend service → Variables** add `CORS_ALLOWED_ORIGINS=https://<frontend-service>.up.railway.app`
5. Frontend health: `/` , Backend health: `/api/v1/health`

## Local (monolith fallback)
Backend still works standalone: `cd backend && mvn spring-boot:run` → http://localhost:8080 serves API only (no static). Use frontend service for UI.
