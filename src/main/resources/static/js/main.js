/* Notely — shared behaviour: nav, active link, hero entrance, ledger count-up. */
document.addEventListener("DOMContentLoaded", function () {

  // mobile nav
  const toggle = document.querySelector(".nav-toggle");
  const drawer = document.querySelector(".nav-drawer");
  if (toggle && drawer) {
    toggle.addEventListener("click", function () {
      const open = drawer.classList.toggle("open");
      toggle.setAttribute("aria-expanded", open ? "true" : "false");
    });
  }

  // active nav link
  const here = document.body.getAttribute("data-page");
  document.querySelectorAll("[data-nav]").forEach(a => {
    if (a.getAttribute("data-nav") === here) a.setAttribute("aria-current", "page");
  });

  // footer year
  document.querySelectorAll(".js-year").forEach(el => el.textContent = new Date().getFullYear());

  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  // hero card entrance — one orchestrated moment
  const stack = document.querySelector(".card-stack");
  if (stack) {
    if (reduceMotion) stack.classList.add("is-ready");
    else requestAnimationFrame(() => setTimeout(() => stack.classList.add("is-ready"), 80));
  }

  // ledger stat count-up — fires once
  const ledger = document.querySelector(".stats-ledger");
  if (ledger && typeof STATS !== "undefined") {
    const targets = { notes: STATS.notes, students: STATS.students, downloads: STATS.downloads };
    const nums = ledger.querySelectorAll(".num");
    if (reduceMotion) {
      nums.forEach(n => n.textContent = Number(n.dataset.target).toLocaleString());
    } else {
      const io = new IntersectionObserver(entries => {
        entries.forEach(entry => {
          if (!entry.isIntersecting) return;
          nums.forEach(n => {
            const target = Number(n.dataset.target);
            const start = performance.now();
            const dur = 900;
            function tick(now) {
              const p = Math.min(1, (now - start) / dur);
              const eased = 1 - Math.pow(1 - p, 3);
              n.textContent = Math.round(target * eased).toLocaleString();
              if (p < 1) requestAnimationFrame(tick);
            }
            requestAnimationFrame(tick);
          });
          io.disconnect();
        });
      }, { threshold: .4 });
      io.observe(ledger);
    }
  }
});
