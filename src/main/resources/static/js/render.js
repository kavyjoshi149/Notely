/* Shared card renderer used by home, explore and library pages. Notes come from /api/notes. */

function noteCardHTML(note, opts) {
  opts = opts || {};
  return `
    <article class="note-card">
      <div class="note-top">
        <span class="note-tag ${tagClass(note.branch)}">${esc(note.branch)}</span>
        ${opts.trending ? `<span class="badge-trending">${trendingIcon()} Trending</span>` : ""}
      </div>
      <h3><a href="/notes/${encodeURIComponent(note.id)}">${esc(note.title)}</a></h3>
      <div class="note-code">${esc(note.code)} &middot; ${esc(note.university)}</div>
      <p class="note-desc">${esc(note.description || note.subjectName || "")}</p>
      <div class="note-foot">
        <div class="uploader">
          <span class="avatar">${esc(note.initials)}</span>
          ${esc(note.uploader)}
        </div>
        <div class="note-stats">
          <span>${downloadIcon()} ${esc(note.downloads)}</span>
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

function renderGrid(el, notes, emptyHTML, opts) {
  el.parentElement.querySelector(".empty-state")?.remove();
  if (!notes.length) {
    el.innerHTML = "";
    if (emptyHTML) el.insertAdjacentHTML("afterend", emptyHTML);
    return;
  }
  el.innerHTML = notes.map(n => noteCardHTML(n, opts)).join("");
}

function renderError(el, message) {
  el.parentElement.querySelector(".empty-state")?.remove();
  el.innerHTML = "";
  el.insertAdjacentHTML("afterend",
    `<div class="empty-state"><h3>Couldn't load notes</h3><p>${esc(message)}</p></div>`);
}
