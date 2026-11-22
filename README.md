# VISHNU

Premium, mobile-first cinema discovery and playback companion.

## Features
- Dark, understated premium interface with warm gold accents
- TMDB-powered trending, movie, TV and multi-search catalogue
- Local watchlist
- Title detail pages
- Direct playback for compatible media URLs supplied by the user
- Open-source download handoff for authorized sources
- Responsive mobile navigation

## Run locally
Open `index.html` in a browser or serve this directory with any static web server.

## Connect TMDB
1. Create a TMDB account and obtain an API Read Access Token.
2. Open VISHNU → Settings.
3. Paste the token and select **Save & connect**.

The token is stored in localStorage on that browser. For a public production deployment, use a server-side proxy or serverless function to protect API credentials rather than exposing a private key in client code.

## Deploy
Import this repository into Vercel as a static site. No build command is required; the output directory is the repository root.

## Playback and downloads
VISHNU does not bundle unauthorized streaming scrapers or content. Playback accepts direct media URLs from sources the user owns or is authorized to access. Browser playback depends on codec, CORS and server support. Download behavior depends on the source's permissions, headers and browser restrictions; this is not a universal downloader.

## Current scope
This is the first working UI foundation. Authentication, cross-device sync, a robust background download manager, and a protected API proxy require backend configuration and are not yet implemented.
