# VISHNU

Premium, mobile-first cinema discovery and playback companion.

## Features
- Dark, understated premium interface with warm gold accents
- TMDB-powered trending, movie, TV and multi-search catalogue
- Server-side TMDB proxy so users do not enter or expose API credentials
- Local watchlist and Google sign-in / Firestore sync
- Title detail pages
- Direct playback for compatible media URLs supplied by the user
- Responsive mobile navigation

## Configure TMDB on Vercel (required)
1. In TMDB, create an API credential and copy the **API Read Access Token** (the long Bearer token).
2. Open the VISHNU project in Vercel → **Settings → Environment Variables**.
3. Add `TMDB_API_READ_ACCESS_TOKEN` and paste the token as its value. Enable it for Production (and Preview if you use preview deployments).
4. Save, then go to **Deployments** and redeploy the latest deployment so the function receives the variable.
5. Open the deployed VISHNU site. The catalogue should load on every device without asking visitors for a TMDB key.

You may alternatively set `TMDB_API_KEY` to the TMDB v3 API key instead of the Read Access Token. Do not put either credential in `app.js`, `index.html`, or browser localStorage.

## Local development
The frontend expects the `/api/tmdb` Vercel serverless function. Use the Vercel CLI (`vercel dev`) to run the project locally; opening `index.html` directly will not provide the proxy.

## Deploy
Import this repository into Vercel. The `api/tmdb.js` serverless function is deployed automatically. No build command is required; the output directory is the repository root.

## Playback and downloads
VISHNU does not bundle unauthorized streaming scrapers or content. Playback accepts direct media URLs from sources the user owns or is authorized to access. Browser playback depends on codec, CORS and server support. Download behavior depends on the source's permissions, headers and browser restrictions.
