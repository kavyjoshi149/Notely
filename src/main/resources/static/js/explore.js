/* Explore page: search + branch-chip filtering over NOTES. */
document.addEventListener("DOMContentLoaded", function () {
  const grid = document.getElementById("notes-grid");
  const input = document.getElementById("search-input");
  const chipRow = document.getElementById("chip-row");
  let activeBranch = "all";

  const emptyHTML = `
    <div class="empty-state">
      ${emptyIcon()}
      <h3>No notes match</h3>
      <p>Try a different keyword or branch — or be the first to upload one.</p>
      <a class="btn btn-primary btn-sm" href="upload.html">Upload a note</a>
    </div>`;

  function apply() {
    const q = (input.value || "").trim().toLowerCase();
    const filtered = NOTES.filter(n => {
      const matchesBranch = activeBranch === "all" || n.branch === activeBranch;
      const matchesQuery = !q ||
        n.title.toLowerCase().includes(q) ||
        n.code.toLowerCase().includes(q) ||
        n.uploader.toLowerCase().includes(q);
      return matchesBranch && matchesQuery;
    });
    renderGrid(grid, filtered, emptyHTML);
  }

  BRANCHES.forEach(b => {
    const chip = document.createElement("button");
    chip.type = "button";
    chip.className = "chip" + (b.code === "all" ? " active" : "");
    chip.textContent = b.label;
    chip.addEventListener("click", () => {
      chipRow.querySelectorAll(".chip").forEach(c => c.classList.remove("active"));
      chip.classList.add("active");
      activeBranch = b.code;
      apply();
    });
    chipRow.appendChild(chip);
  });

  let debounce;
  input.addEventListener("input", () => {
    clearTimeout(debounce);
    debounce = setTimeout(apply, 150);
  });

  apply();
});

function emptyIcon() {
  return `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.6" y2="16.6"/></svg>`;
}
