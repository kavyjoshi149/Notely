/* Home page: real stats for the ledger, most-downloaded notes for "Trending", and the hero cards. */
document.addEventListener("DOMContentLoaded", async function () {
  const nums = document.querySelectorAll(".stats-ledger .num");

  api("/api/public/stats").then(s => {
    const values = [s.notes, s.students, s.downloads];
    nums.forEach((n, i) => { n.dataset.target = String(values[i] || 0); });
    animateLedger();
  }).catch(() => nums.forEach(n => { n.dataset.target = "0"; }));

  const grid = document.getElementById("trending-grid");
  if (!grid) return;
  const empty = `
    <div class="empty-state">
      <h3>No notes yet</h3>
      <p>Be the first to share one with your campus.</p>
      <a class="btn btn-primary btn-sm" href="/upload">Upload a note</a>
    </div>`;
  try {
    const page = await api("/api/notes?sort=downloads&size=3");
    renderGrid(grid, page.content, empty, { trending: true });

    // hero index cards show the same top notes when there are any
    const cards = document.querySelectorAll(".card-stack .index-card");
    page.content.slice(0, cards.length).forEach((n, i) => {
      const card = cards[i];
      const tag = card.querySelector(".note-tag");
      tag.textContent = n.branch;
      tag.className = "note-tag " + tagClass(n.branch);
      card.querySelector("h3").textContent = n.title;
      card.querySelector(".card-meta").textContent = n.code + " · " + n.downloads + " downloads";
    });
  } catch (e) {
    renderError(grid, e.message);
  }
});
