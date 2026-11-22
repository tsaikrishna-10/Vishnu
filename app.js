import { initializeApp } from "https://www.gstatic.com/firebasejs/13.4.0/firebase-app.js";
import {
  getAuth, GoogleAuthProvider, onAuthStateChanged, signInWithPopup,
  signOut, setPersistence, browserLocalPersistence, updateProfile
} from "https://www.gstatic.com/firebasejs/13.4.0/firebase-auth.js";
import {
  getFirestore, collection, doc, getDocs, setDoc
} from "https://www.gstatic.com/firebasejs/13.4.0/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "AIzaSyCjI14eA4XYINwDGFRi0Mo7PnN52q7QoH0",
  authDomain: "vishnu-b6fa9.firebaseapp.com",
  projectId: "vishnu-b6fa9",
  storageBucket: "vishnu-b6fa9.firebasestorage.app",
  messagingSenderId: "582519068175",
  appId: "1:582519068175:web:18ceeb3eeb0bc8ec27f57a",
  measurementId: "G-8MZ442LLS2"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);
const provider = new GoogleAuthProvider();
provider.setCustomParameters({ prompt: "select_account" });

const $ = (selector) => document.querySelector(selector);
const image = (path, size = "w500") => path ? `https://image.tmdb.org/t/p/${size}${path}` : "";
let saved = [];
try { saved = JSON.parse(localStorage.getItem("vishnu_list") || "[]"); } catch { saved = []; }
let heroItem = null;
const pages = { movies: 1, kdramas: 1, anime: 1, shows: 1 };
const pageLocks = new Set();
const pageConfig = {
  movies: { endpoint: "/movie/popular", target: "#movie-grid", type: "movie" },
  kdramas: { endpoint: "/discover/tv?with_original_language=ko&sort_by=popularity.desc", target: "#kdrama-grid", type: "tv" },
  anime: { endpoint: "/discover/tv?with_genres=16&with_original_language=ja&sort_by=popularity.desc", target: "#anime-grid", type: "tv" },
  shows: { endpoint: "/tv/popular", target: "#show-grid", type: "tv" }
};

function toast(message) {
  const el = $("#toast");
  if (!el) return;
  el.textContent = message;
  el.style.display = "block";
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => { el.style.display = "none"; }, 2600);
}

function showPage(id) {
  const target = document.getElementById(id);
  if (!target || !target.classList.contains("page")) return;
  $("#search-overlay")?.classList.remove("open");
  document.querySelectorAll(".page").forEach(page => {
    const active = page === target;
    page.classList.toggle("active", active);
    page.setAttribute("aria-hidden", String(!active));
  });
  document.querySelectorAll("[data-page]").forEach(button => {
    const active = button.dataset.page === id;
    button.classList.toggle("active", active);
    if (active) button.setAttribute("aria-current", "page");
    else button.removeAttribute("aria-current");
  });
  window.scrollTo(0, 0);
  if (id === "mylist") renderList();
  if (pageConfig[id] && pages[id] === 1 && !$(pageConfig[id].target)?.children.length) loadPage(id, false);
}
window.showPage = showPage;

document.addEventListener("click", event => {
  const button = event.target.closest("[data-page]");
  if (button) { event.preventDefault(); showPage(button.dataset.page); }
}, true);

async function tmdb(path) {
  // All devices use the same Vercel server-side proxy; no client-side TMDB key is needed.
  const response = await fetch("/api/tmdb?path=" + encodeURIComponent(path), {
    headers: { accept: "application/json" }
  });
  if (!response.ok) {
    let detail = "";
    try { detail = (await response.json()).error || ""; } catch {}
    throw new Error(detail || `TMDB request failed (${response.status}).`);
  }
  return response.json();
}

function normalise(item, type) {
  return { ...item, media_type: item.media_type || type || (item.first_air_date ? "tv" : "movie") };
}

function renderCards(items, target, type) {
  const el = typeof target === "string" ? $(target) : target;
  if (!el) return;
  if (!items?.length) {
    if (!el.children.length) el.innerHTML = '<div class="empty">No titles found.</div>';
    return;
  }
  const html = items.map(raw => {
    const item = normalise(raw, type);
    const title = item.title || item.name || "Untitled";
    const year = (item.release_date || item.first_air_date || "").slice(0, 4);
    const poster = item.poster_path
      ? `<img loading="lazy" src="${image(item.poster_path)}" alt="${escapeHtml(title)}">`
      : "";
    return `<article class="movie" data-id="${item.id}" data-type="${item.media_type}">
      <div class="poster">${poster}<span class="tag">${item.vote_average ? Number(item.vote_average).toFixed(1) + " ★" : "VISHNU"}</span></div>
      <h3>${escapeHtml(title)}</h3><small>${year}</small></article>`;
  }).join("");
  if (el.dataset.append === "true") el.insertAdjacentHTML("beforeend", html);
  else el.innerHTML = html;
  el.dataset.append = "false";
  el.querySelectorAll(".movie").forEach(card => {
    card.onclick = () => detail(card.dataset.id, card.dataset.type);
  });
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, ch => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[ch]);
}

function setHero(item) {
  if (!item) return;
  heroItem = normalise(item);
  const backdrop = image(item.backdrop_path || item.poster_path, "original");
  const hero = $("#hero-banner");
  if (hero && backdrop) hero.style.backgroundImage =
    `linear-gradient(180deg,rgba(7,5,12,.15),rgba(7,5,12,.62) 58%,var(--bg)),url("${backdrop}")`;
  if ($("#hero-eyebrow")) $("#hero-eyebrow").textContent = "● TRENDING ON VISHNU";
  if ($("#hero-title")) $("#hero-title").textContent = item.title || item.name || "Featured";
  if ($("#hero-meta")) $("#hero-meta").innerHTML =
    `<span class="rate">★ ${Number(item.vote_average || 0).toFixed(1)}</span><span>${(item.release_date || item.first_air_date || "").slice(0,4)}</span><span class="badge-4k">4K HDR</span>`;
  if ($("#hero-desc")) $("#hero-desc").textContent = item.overview || "";
}


function renderTop10AndPosterFeed(items) {
  const top = $("#top10Scroll");
  const feed = $("#posterFeedGrid");
  const count = $("#posterFeedCount");
  if (!top || !feed) return;
  const list = (items || []).filter(item => item && item.poster_path).slice(0, 10);
  if (!list.length) {
    top.innerHTML = '<div class="empty">No trending posters available right now.</div>';
    feed.innerHTML = '<div class="empty">No posters available right now.</div>';
    if (count) count.textContent = "0 Titles";
    return;
  }
  const titleOf = item => item.title || item.name || "Untitled";
  const kindOf = item => item.media_type === "tv" ? "TV" : "MOVIE";
  top.innerHTML = list.map((item, index) => {
    const title = escapeHtml(titleOf(item));
    const rank = index + 1;
    const rankStyle = rank === 1 ? "rank-1" : rank === 2 ? "rank-2" : rank === 3 ? "rank-3" : "rank-default";
    return `<div class="top10-item ${rank === 10 ? "rank-ten" : ""}" data-id="${item.id}" data-type="${item.media_type || "movie"}">
      <span class="top10-rank ${rankStyle}">${rank}</span>
      <div class="top10-card"><span class="top10-kind">${kindOf(item)}</span>
      <img src="${image(item.poster_path)}" alt="${title}" loading="lazy">
      <div class="top10-poster-footer"><div class="top10-poster-title">${title}</div></div></div></div>`;
  }).join("");
  feed.innerHTML = list.map((item, index) => {
    const title = escapeHtml(titleOf(item));
    const year = (item.release_date || item.first_air_date || "").slice(0, 4);
    return `<article class="feed-poster-card" data-id="${item.id}" data-type="${item.media_type || "movie"}">
      <div class="feed-poster-media"><img src="${image(item.poster_path)}" alt="${title}" loading="lazy">
      <span class="feed-rating-pill">★ ${Number(item.vote_average || 0).toFixed(1)}</span>
      <span class="feed-quality-pill">${kindOf(item)}</span></div>
      <div class="feed-poster-body"><div class="feed-poster-name">${title}</div>
      <div class="feed-poster-meta"><span>${year}</span><span>#${index + 1}</span></div></div></article>`;
  }).join("");
  if (count) count.textContent = `${list.length} Titles`;
  [top, feed].forEach(container => container.querySelectorAll("[data-id]").forEach(card => {
    card.addEventListener("click", () => detail(card.dataset.id, card.dataset.type));
  }));
}

async function loadHome() {
  try {
    const requests = [
      tmdb("/trending/all/week?language=en-US"),
      tmdb("/movie/popular?language=en-US&page=1"),
      tmdb("/movie/top_rated?language=en-US&page=1"),
      tmdb("/discover/movie?with_original_language=te|hi&sort_by=popularity.desc&page=1"),
      tmdb("/discover/tv?with_original_language=ko&sort_by=popularity.desc&page=1"),
      tmdb("/discover/tv?with_genres=16&with_original_language=ja&sort_by=popularity.desc&page=1"),
      tmdb("/tv/popular?language=en-US&page=1")
    ];
    const [trending, popular, topRated, indian, kdramas, anime, shows] = await Promise.all(requests);
    const featured = trending.results?.find(x => x.backdrop_path) || trending.results?.[0];
    setHero(featured);
    renderTop10AndPosterFeed(trending.results);
    renderCards(trending.results, "#trending");
    renderCards(popular.results, "#popular", "movie");
    renderCards(topRated.results, "#toprated", "movie");
    renderCards(indian.results, "#indian", "movie");
    renderCards(kdramas.results, "#kdrama-row", "tv");
    renderCards(anime.results, "#anime-row", "tv");
    renderCards(popular.results, "#movie-grid", "movie");
    renderCards(kdramas.results, "#kdrama-grid", "tv");
    renderCards(anime.results, "#anime-grid", "tv");
    renderCards(shows.results, "#show-grid", "tv");
  } catch (error) { console.error("VISHNU catalogue:", error); toast(error.message); }
}

async function loadPage(id, append = true) {
  const config = pageConfig[id];
  if (!config) return;
  if (pageLocks.has(id)) return;
  pageLocks.add(id);
  const page = append ? pages[id] + 1 : 1;
  try {
    const data = await tmdb(config.endpoint + (config.endpoint.includes("?") ? "&" : "?") + `language=en-US&page=${page}`);
    const target = $(config.target);
    if (target) {
      target.dataset.append = append && page > 1 ? "true" : "false";
      renderCards(data.results, target, config.type);
    }
    pages[id] = page;
  } catch (error) {
    console.error("VISHNU page load:", id, error);
    toast(error.message);
  } finally {
    pageLocks.delete(id);
  }
}
window.loadMore = id => loadPage(id, true);

async function detail(id, type = "movie") {
  try {
    const kind = type === "tv" ? "tv" : "movie";
    const item = await tmdb(`/${kind}/${id}?language=en-US&append_to_response=videos,credits`);
    item.media_type = kind;
    showPage("detail");
    const bg = image(item.backdrop_path || item.poster_path, "original");
    const poster = item.poster_path ? `<img src="${image(item.poster_path)}" alt="">` : "<div></div>";
    const title = escapeHtml(item.title || item.name || "Untitled");
    const overview = escapeHtml(item.overview || "No synopsis available.");
    const detailView = $("#detail");
    if (!detailView) { toast("Detail view is missing from index.html."); return; }
    detailView.innerHTML = `
      <div class="detail-hero" style="background-image:linear-gradient(180deg,rgba(7,5,12,.25),rgba(7,5,12,.82) 65%,var(--bg)),url('${bg}')">
        <div class="detail-grid">${poster}<div><div class="eyebrow">${kind === "tv" ? "TV SERIES" : "FEATURED FILM"}</div>
        <h1>${title}</h1><div class="hero-meta"><span class="rate">★ ${Number(item.vote_average || 0).toFixed(1)}</span>
        <span>${(item.release_date || item.first_air_date || "").slice(0,4)}</span></div></div></div>
      </div><p class="muted" style="margin-bottom:18px">${overview}</p>
      <div class="actions" style="margin-bottom:18px"><button class="btn gold" id="save-title">+ My List</button><button class="btn" id="detail-back">← Back</button></div>
      <div class="panel"><h3>Download with Flud</h3><p class="muted">Paste a magnet link for content you own or are authorised to download. Flud must be installed on your Android device.</p>
      <label class="field">Magnet link<input id="flud-magnet" type="text" inputmode="url" placeholder="magnet:?xt=urn:btih:…"></label>
      <div class="actions"><button class="btn gold" id="open-flud">↓ Open in Flud</button></div>
      <p class="muted" style="margin-top:10px;font-size:12px">VISHNU does not provide torrent files or search for copyrighted copies. The magnet link is supplied by you.</p></div>
      <div class="panel"><h3>Play from your source</h3><p class="muted">Use a direct media URL you own or are authorised to access.</p>
      <label class="field">Media URL<input id="media-url" type="url" placeholder="https://…"></label>
      <div class="actions"><button class="btn gold" id="play">▶ Play</button><button class="btn" id="download">Open download</button></div>
      <video id="player" controls playsinline style="display:none;width:100%;margin-top:16px;border-radius:12px;background:#000"></video></div>`;
    $("#save-title").onclick = () => saveTitle(item);
    $("#detail-back").onclick = () => showPage("home");
    $("#play").onclick = () => playUrl($("#media-url").value);
    $("#download").onclick = () => downloadUrl($("#media-url").value);
    $("#open-flud").onclick = () => {
      const value = $("#flud-magnet")?.value.trim();
      if (!value || !/^magnet:\?xt=urn:btih:[a-z0-9]{32,40}/i.test(value)) {
        toast("Paste a valid BitTorrent magnet link first.");
        return;
      }
      // Android will offer Flud (if installed and registered for magnet links).
      // Other devices may show an app chooser or report that no handler exists.
      window.location.href = value;
    };
  } catch (error) { toast(error.message); }
}

async function saveTitle(item) {
  const entry = { id: item.id, title: item.title || item.name, poster_path: item.poster_path, media_type: item.media_type || "movie" };
  const key = entry.media_type + "_" + entry.id;
  if (!saved.some(x => x.id === entry.id && x.media_type === entry.media_type)) saved.push(entry);
  localStorage.setItem("vishnu_list", JSON.stringify(saved));
  renderList();
  if (auth.currentUser) {
    try { await setDoc(doc(db, "users", auth.currentUser.uid, "watchlist", key), entry); toast("Saved and synced to your account."); }
    catch (error) { console.error(error); toast("Saved on this device; cloud sync failed."); }
  } else toast("Added to My List.");
}
function renderList() { renderCards(saved, "#list-grid"); }

function updateAccount(user) {
  const status = $("#auth-status"), login = $("#google-login"), logout = $("#google-logout");
  if (!status || !login || !logout) return;
  const displayName = user?.displayName || user?.email?.split("@")[0] || "VISHNU User";
  const email = user?.email || "Sign in to see your account details";
  const initials = displayName.split(/\s+/).slice(0, 2).map(part => part[0]?.toUpperCase() || "").join("") || "V";
  if ($("#settings-name")) $("#settings-name").textContent = displayName;
  if ($("#settings-email")) $("#settings-email").textContent = email;
  if ($("#settings-avatar")) $("#settings-avatar").textContent = initials;
  if ($("#settings-account-state")) $("#settings-account-state").textContent = user ? "Connected" : "Guest";
  if ($("#settings-display-name") && user && document.activeElement !== $("#settings-display-name")) $("#settings-display-name").value = user.displayName || "";
  if (user) {
    status.textContent = "Signed in as " + (user.displayName || user.email) + " · Cloud sync enabled";
    login.style.display = "none"; logout.style.display = "inline-flex";
  } else {
    status.textContent = "Not signed in. Sign in to sync your watchlist across devices.";
    login.style.display = "inline-flex"; logout.style.display = "none";
  }
}

async function syncWatchlist(user) {
  const snapshot = await getDocs(collection(db, "users", user.uid, "watchlist"));
  const cloudItems = snapshot.docs.map(d => d.data());
  if (cloudItems.length) {
    saved = cloudItems;
    localStorage.setItem("vishnu_list", JSON.stringify(saved));
    renderList();
  } else {
    for (const item of saved) {
      const id = (item.media_type || "movie") + "_" + item.id;
      await setDoc(doc(db, "users", user.uid, "watchlist", id), item);
    }
  }
}

$("#google-login")?.addEventListener("click", async () => {
  const status = $("#auth-status");
  try {
    if (status) status.textContent = "Opening Google sign-in…";
    await setPersistence(auth, browserLocalPersistence);
    const result = await signInWithPopup(auth, provider);
    updateAccount(result.user);
    await syncWatchlist(result.user);
    toast("Signed in successfully.");
  } catch (error) {
    console.error("VISHNU sign-in:", error);
    if (status) status.textContent = "Sign-in failed: " + (error.code || error.message);
    toast("Google sign-in failed: " + (error.code || error.message));
  }
});
$("#google-logout")?.addEventListener("click", async () => {
  try { await signOut(auth); toast("Signed out."); }
  catch (error) { toast(error.message); }
});
onAuthStateChanged(auth, async user => {
  updateAccount(user);
  if (user) {
    try { await syncWatchlist(user); }
    catch (error) { console.error("VISHNU Firestore sync:", error); toast("Signed in, but watchlist sync failed. Check Firestore rules."); }
  }
});

function saveKey() {
  toast("TMDB is configured centrally. No key is needed on this device.");
  loadHome();
}
window.saveKey = saveKey;

function playUrl(value) {
  try {
    const url = new URL(value);
    if (!["http:", "https:"].includes(url.protocol)) throw new Error();
    const player = $("#player");
    player.src = url.href; player.style.display = "block";
    // A deliberate tap on Play always starts playback; Autoplay Next is a queue preference.
    player.play().catch(() => toast("Playback could not start. Check the media URL or browser support."));
    player.addEventListener("loadedmetadata", () => {
      const enableSubtitles = localStorage.getItem("vishnu_subtitles") === "true";
      Array.from(player.textTracks || []).forEach(track => {
        if (track.kind === "subtitles" || track.kind === "captions") {
          track.mode = enableSubtitles ? "showing" : "disabled";
        }
      });
    }, { once: true });
  } catch { toast("Enter a valid direct media URL."); }
}
function downloadUrl(value) {
  try {
    const url = new URL(value);
    if (!["http:", "https:"].includes(url.protocol)) throw new Error();
    const link = document.createElement("a");
    link.href = url.href; link.target = "_blank"; link.rel = "noopener"; link.click();
  } catch { toast("Enter a valid direct media URL."); }
}

$("#open-search")?.addEventListener("click", () => {
  $("#search-overlay")?.classList.add("open");
  if ($("#search-results")) $("#search-results").innerHTML = '<div class="empty">Type a title and press Enter to search TMDB.</div>';
  setTimeout(() => $("#search")?.focus(), 40);
});
$("#close-search")?.addEventListener("click", () => $("#search-overlay")?.classList.remove("open"));
$("#search-overlay")?.addEventListener("click", event => {
  if (event.target.id === "search-overlay") event.currentTarget.classList.remove("open");
});
$("#search")?.addEventListener("keydown", async event => {
  if (event.key !== "Enter") return;
  const query = event.target.value.trim();
  if (!query) return;
  try {
    const result = await tmdb("/search/multi?query=" + encodeURIComponent(query) + "&include_adult=false&language=en-US&page=1");
    renderCards(result.results.filter(x => x.media_type !== "person"), "#search-results");
    $("#search-overlay")?.classList.add("open");
  } catch (error) { toast(error.message); }
});
$("#hero-play")?.addEventListener("click", () => heroItem ? detail(heroItem.id, heroItem.media_type) : showPage("movies"));
$("#hero-list")?.addEventListener("click", () => heroItem ? saveTitle(heroItem) : showPage("mylist"));
document.querySelector(".mark")?.addEventListener("click", () => showPage("home"));
window.addEventListener("scroll", () => $("#navbar")?.classList.toggle("scrolled", window.scrollY > 24), { passive: true });

renderList();
loadHome();


// VISHNU Settings Stage 2 — device-local preferences.
const SETTINGS_KEY = "vishnu_settings_v2";
const defaultSettings = {
  compactUi: false,
  largeText: false,
  autoplayNext: true,
  subtitles: false,
  quality: "auto"
};
function readSettings() {
  try {
    const stored = JSON.parse(localStorage.getItem(SETTINGS_KEY) || "{}");
    return {
      ...defaultSettings,
      ...stored,
      autoplayNext: stored.autoplayNext ?? (localStorage.getItem("vishnu_autoplay") !== "false"),
      quality: stored.quality || localStorage.getItem("vishnu_quality") || "auto"
    };
  } catch { return { ...defaultSettings }; }
}
const settings = readSettings();
function persistSettings(message = "Settings saved on this device.") {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify(settings));
  localStorage.setItem("vishnu_autoplay", String(settings.autoplayNext));
  localStorage.setItem("vishnu_subtitles", String(settings.subtitles));
  localStorage.setItem("vishnu_quality", settings.quality);
  const status = $("#playback-status");
  if (status) status.textContent = message;
}
function applyAppearanceSettings() {
  document.body.classList.toggle("compact-ui", settings.compactUi);
  document.body.classList.toggle("large-text", settings.largeText);
}
applyAppearanceSettings();

document.querySelectorAll("[data-settings-tab]").forEach(tab => {
  tab.addEventListener("click", () => {
    const target = tab.dataset.settingsTab;
    document.querySelectorAll("[data-settings-tab]").forEach(button => {
      const active = button === tab;
      button.classList.toggle("active", active);
      button.setAttribute("aria-selected", String(active));
    });
    document.querySelectorAll(".settings-panel").forEach(panel => {
      panel.classList.toggle("active", panel.id === "settings-" + target);
    });
  });
});

const compactToggle = $("#compact-toggle");
const largeTextToggle = $("#large-text-toggle");
const autoplayPreference = $("#pref-autoplay");
const subtitlesPreference = $("#pref-subtitles");
const qualityPreference = $("#pref-quality");
if (compactToggle) {
  compactToggle.checked = settings.compactUi;
  compactToggle.addEventListener("change", () => {
    settings.compactUi = compactToggle.checked;
    applyAppearanceSettings();
    persistSettings("Compact card preference saved.");
  });
}
if (largeTextToggle) {
  largeTextToggle.checked = settings.largeText;
  largeTextToggle.addEventListener("change", () => {
    settings.largeText = largeTextToggle.checked;
    applyAppearanceSettings();
    persistSettings("Text size preference saved.");
  });
}
if (autoplayPreference) {
  autoplayPreference.checked = settings.autoplayNext;
  autoplayPreference.addEventListener("change", () => {
    settings.autoplayNext = autoplayPreference.checked;
    persistSettings("Autoplay Next preference saved.");
  });
}
if (subtitlesPreference) {
  subtitlesPreference.checked = settings.subtitles;
  subtitlesPreference.addEventListener("change", () => {
    settings.subtitles = subtitlesPreference.checked;
    persistSettings("Subtitle preference saved.");
  });
}
if (qualityPreference) {
  qualityPreference.value = settings.quality;
  qualityPreference.addEventListener("change", () => {
    settings.quality = qualityPreference.value;
    persistSettings("Quality preference saved. Actual resolution depends on the media source.");
  });
}

$("#settings-save-name")?.addEventListener("click", async () => {
  const input = $("#settings-display-name");
  const status = $("#settings-name-status");
  const name = input?.value.trim();
  if (!name) {
    if (status) status.textContent = "Display name cannot be empty.";
    return;
  }
  if (!auth.currentUser) {
    if (status) status.textContent = "Sign in with Google before changing your profile name.";
    return;
  }
  try {
    await updateProfile(auth.currentUser, { displayName: name });
    updateAccount(auth.currentUser);
    if (status) status.textContent = "Display name updated.";
    toast("Profile name updated.");
  } catch (error) {
    console.error("VISHNU profile update:", error);
    if (status) status.textContent = "Could not update name: " + (error.code || error.message);
  }
});
