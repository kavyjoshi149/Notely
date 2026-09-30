/* Shared card renderer used by home, explore and library pages. */

function noteCardHTML(note, opts) {
  opts = opts || {};
  const b = branchInfo(note.branch);
  const trending = note.downloads > 500;
  return `
    <article class="note-card">
      <div class="note-top">
        <span class="note-tag ${b.tag}">${b.label}</span>
        ${trending ? `<span class="badge-trending">${trendingIcon()} Trending</span>` : ""}
      </div>
      <h3><a href="note-detail.html?id=${note.id}">${note.title}</a></h3>
      <div class="note-code">${note.code} &middot; ${note.university}</div>
      <p class="note-desc">${note.desc}</p>
      <div class="note-foot">
        <div class="uploader">
          <span class="avatar">${note.initials}</span>
          ${note.uploader}
        </div>
        <div class="note-stats">
          <span>${downloadIcon()} ${note.downloads}</span>
        </div>
      </div>
    </article>`;
}

function trendingIcon() {
  return `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><polyline points="3 17 9 11 13 15 21 6"/><polyline points="14 6 21 6 21 13"/></svg>`;
}
function downloadIcon() {
  return `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M12 3v12"/><polyline points="7 11 12 16 17 11"/><path d="M5 21h14"/></svg>`;
}

function renderGrid(el, notes, emptyHTML) {
  if (!notes.length) {
    el.innerHTML = "";
    el.parentElement.querySelector(".empty-state")?.remove();
    el.insertAdjacentHTML("afterend", emptyHTML);
    return;
  }
  el.parentElement.querySelector(".empty-state")?.remove();
  el.innerHTML = notes.map(n => noteCardHTML(n)).join("");
}
