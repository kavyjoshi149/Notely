/* My Library page: profile header from /api/me, "My uploads" from /api/notes/mine. */
document.addEventListener("DOMContentLoaded", async function () {
  const grid = document.getElementById("library-grid");
  const tabs = document.querySelectorAll(".tab");
  let mine = [];

  const emptyUploads = `
    <div class="empty-state">
      ${uploadIconLg()}
      <h3>You haven't shared a note yet</h3>
      <p>Upload your first PDF and it will show up here.</p>
      <a class="btn btn-primary btn-sm" href="/upload">Upload a note</a>
    </div>`;
  const emptyBookmarks = `
    <div class="empty-state">
      ${uploadIconLg()}
      <h3>Bookmarks are coming soon</h3>
      <p>For now, find notes on Explore and download the ones you need.</p>
      <a class="btn btn-outline btn-sm" href="/explore">Browse notes</a>
    </div>`;

  function show(tabName) {
    tabs.forEach(t => t.classList.toggle("active", t.dataset.tab === tabName));
    if (tabName === "uploads") renderGrid(grid, mine, emptyUploads);
    else renderGrid(grid, [], emptyBookmarks);
  }
  tabs.forEach(t => t.addEventListener("click", () => show(t.dataset.tab)));

  const me = await loadMe();
  if (me.authenticated) {
    document.getElementById("profileAvatar").textContent = me.initials;
    document.getElementById("profileName").textContent = me.name;
    document.getElementById("profileHandle").textContent =
      "@" + me.username + (me.batch ? " · Batch " + me.batch : "");
    const tags = document.getElementById("profileTags");
    tags.innerHTML = [me.branch, me.university].filter(Boolean)
      .map(t => `<span class="note-tag ${tagClass(t)}">${esc(t)}</span>`).join("");
  }

  try {
    mine = (await api("/api/notes/mine")).content;
    show("uploads");
  } catch (e) {
    if (e.status === 401) { location.href = "/login"; return; }
    renderError(grid, e.message);
  }
});

function uploadIconLg() {
  return `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M4 4h16v16H4z" opacity="0"/><path d="M12 3v12"/><polyline points="7 8 12 3 17 8"/><path d="M5 21h14"/></svg>`;
}
