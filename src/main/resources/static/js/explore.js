/* Explore page: server-side search (/api/notes?q=) plus branch-chip filtering. */
document.addEventListener("DOMContentLoaded", function () {
  const grid = document.getElementById("notes-grid");
  const input = document.getElementById("search-input");
  const chipRow = document.getElementById("chip-row");
  let activeBranch = "all";      // branch NAME ("all" or e.g. "CSE"); names repeat across courses, so filter by name
  let notes = [];

  const emptyHTML = `
    <div class="empty-state">
      ${emptyIcon()}
      <h3>No notes match</h3>
      <p>Try a different keyword or branch — or be the first to upload one.</p>
      <a class="btn btn-primary btn-sm" href="/upload">Upload a note</a>
    </div>`;

  function apply() {
    const shown = notes.filter(n => activeBranch === "all" || n.branch === activeBranch);
    renderGrid(grid, shown, emptyHTML);
  }

  async function fetchNotes() {
    const q = (input.value || "").trim();
    grid.setAttribute("aria-busy", "true");
    try {
      const page = await api("/api/notes?size=100&q=" + encodeURIComponent(q));
      notes = page.content;
      apply();
    } catch (e) {
      renderError(grid, e.message);
    }
  }

  function addChip(label, value) {
    const chip = document.createElement("button");
    chip.type = "button";
    chip.className = "chip" + (value === "all" ? " active" : "");
    chip.textContent = label;
    chip.addEventListener("click", () => {
      chipRow.querySelectorAll(".chip").forEach(c => c.classList.remove("active"));
      chip.classList.add("active");
      activeBranch = value;
      apply();
    });
    chipRow.appendChild(chip);
  }

  addChip("All branches", "all");
  api("/api/public/branches/all").then(branches => {
    [...new Set(branches.map(b => b.name))].sort().forEach(name => addChip(name, name));
  }).catch(() => { /* chips are optional */ });

  let debounce;
  input.addEventListener("input", () => {
    clearTimeout(debounce);
    debounce = setTimeout(fetchNotes, 250);
  });

  fetchNotes();
});

function emptyIcon() {
  return `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.6" y2="16.6"/></svg>`;
}
