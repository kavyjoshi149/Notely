/* Notely — shared helpers used by every page: CSRF-aware fetch wrapper, current user, small formatters.
   (Replaces the old mock data file; all data now comes from the Spring controllers.) */

function csrfMeta(name) {
  const el = document.querySelector('meta[name="' + name + '"]');
  return el ? el.content : "";
}

/** fetch() wrapper: same-origin cookies, CSRF header on writes, JSON in/out, throws Error with .status/.data. */
async function api(url, options) {
  options = options || {};
  const method = (options.method || "GET").toUpperCase();
  const headers = Object.assign({ Accept: "application/json" }, options.headers || {});
  if (method !== "GET" && method !== "HEAD") {
    headers[csrfMeta("_csrf_header") || "X-CSRF-TOKEN"] = csrfMeta("_csrf");
  }
  let body = options.body;
  if (options.json !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(options.json);
  }
  const res = await fetch(url, { method, headers, body, credentials: "same-origin" });
  let data = null;
  const text = await res.text();
  if (text) { try { data = JSON.parse(text); } catch (e) { data = { message: text }; } }
  if (!res.ok) {
    const err = new Error((data && data.message) || (res.status === 401 ? "Please log in" : "Request failed (" + res.status + ")"));
    err.status = res.status;
    err.data = data;
    throw err;
  }
  return data;
}

let mePromise = null;
/** Cached "who is logged in?" — resolves to {authenticated:false} for visitors. */
function loadMe() {
  if (!mePromise) mePromise = api("/api/me").catch(() => ({ authenticated: false }));
  return mePromise;
}

function esc(value) {
  return String(value == null ? "" : value)
    .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

const TAG_CLASSES = ["tag-navy", "tag-moss", "tag-brick", "tag-mustard"];
/** Stable colour per branch name, so "CSE" is always the same tag colour. */
function tagClass(name) {
  let h = 0;
  const s = String(name || "");
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) >>> 0;
  return TAG_CLASSES[h % TAG_CLASSES.length];
}

function daysAgoLabel(n) {
  if (n === 0) return "today";
  if (n === 1) return "1 day ago";
  return n + " days ago";
}

function formatSize(bytes) {
  if (bytes == null) return "";
  return bytes >= 1024 * 1024 ? (bytes / 1024 / 1024).toFixed(1) + " MB" : Math.max(1, Math.round(bytes / 1024)) + " KB";
}

/** Fill a <select> with {id,name} options. */
function fillSelect(select, items, placeholder) {
  select.innerHTML = '<option value="">' + esc(placeholder) + "</option>" +
    items.map(i => '<option value="' + esc(i.id) + '">' + esc(i.name) + "</option>").join("");
}

/** Course -> branch dropdown backed by /api/public/branches?courseId= */
function wireCourseBranch(courseSelect, branchSelect) {
  courseSelect.addEventListener("change", async () => {
    fillSelect(branchSelect, [], "Select branch");
    if (!courseSelect.value) return;
    try {
      fillSelect(branchSelect, await api("/api/public/branches?courseId=" + encodeURIComponent(courseSelect.value)), "Select branch");
    } catch (e) { /* leave empty; the required check will stop the submit */ }
  });
}

async function loadCourses(courseSelect) {
  try { fillSelect(courseSelect, await api("/api/public/courses"), "Select course"); } catch (e) { /* offline */ }
}
