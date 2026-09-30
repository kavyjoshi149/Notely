/* My Library page: tab between uploads (mine) and bookmarks (mock split of NOTES). */
document.addEventListener("DOMContentLoaded", function () {
  const grid = document.getElementById("library-grid");
  const tabs = document.querySelectorAll(".tab");
  const mine = NOTES.slice(0, 3);
  const bookmarked = NOTES.slice(3, 5);

  const emptyUploads = `
    <div class="empty-state">
      ${uploadIconLg()}
      <h3>You haven't shared a note yet</h3>
      <p>Upload your first PDF and it will show up here.</p>
      <a class="btn btn-primary btn-sm" href="upload.html">Upload a note</a>
    </div>`;
  const emptyBookmarks = `
    <div class="empty-state">
      ${uploadIconLg()}
      <h3>No bookmarks yet</h3>
      <p>Save notes you find on Explore to read them later.</p>
      <a class="btn btn-outline btn-sm" href="explore.html">Browse notes</a>
    </div>`;

  function show(tabName) {
    tabs.forEach(t => t.classList.toggle("active", t.dataset.tab === tabName));
    if (tabName === "uploads") renderGrid(grid, mine, emptyUploads);
    else renderGrid(grid, bookmarked, emptyBookmarks);
  }

  tabs.forEach(t => t.addEventListener("click", () => show(t.dataset.tab)));
  show("uploads");
});

function uploadIconLg() {
  return `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><path d="M4 4h16v16H4z" opacity="0"/><path d="M12 3v12"/><polyline points="7 8 12 3 17 8"/><path d="M5 21h14"/></svg>`;
}
