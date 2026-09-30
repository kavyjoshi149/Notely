/* Home page: sets data-target on ledger numbers, then renders trending notes. */
document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll(".stats-ledger .num").forEach(n => {
    n.dataset.target = n.dataset.target || "0";
  });

  const grid = document.getElementById("trending-grid");
  if (grid) {
    const trending = [...NOTES].sort((a, b) => b.downloads - a.downloads).slice(0, 3);
    renderGrid(grid, trending, "");
  }
});
