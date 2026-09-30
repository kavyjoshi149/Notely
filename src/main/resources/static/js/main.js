/* Notely — shared behaviour: nav, logged-in header, hero entrance, ledger count-up. */
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

  // header: swap Sign in / Join free for the user's name + Log out
  loadMe().then(renderAuthNav);

  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  // hero card entrance — one orchestrated moment
  const stack = document.querySelector(".card-stack");
  if (stack) {
    if (reduceMotion) stack.classList.add("is-ready");
    else requestAnimationFrame(() => setTimeout(() => stack.classList.add("is-ready"), 80));
  }
});

function logoutForm() {
  const form = document.createElement("form");
  form.method = "post";
  form.action = "/logout";
  form.style.display = "inline";
  const token = document.createElement("input");
  token.type = "hidden";
  token.name = csrfMeta("_csrf_parameter") || "_csrf";
  token.value = csrfMeta("_csrf");
  const btn = document.createElement("button");
  btn.type = "submit";
  btn.className = "btn btn-outline btn-sm";
  btn.textContent = "Log out";
  form.append(token, btn);
  return form;
}

function renderAuthNav(me) {
  if (!me.authenticated) return;
  const actions = document.querySelector(".nav-actions");
  if (actions) {
    actions.querySelectorAll('a[href="/login"], a[href="/register"]').forEach(a => a.remove());
    const chip = document.createElement("a");
    chip.href = "/library";
    chip.className = "btn btn-primary btn-sm";
    chip.textContent = me.name.split(" ")[0];
    chip.title = "My Library";
    actions.prepend(chip, logoutForm());
  }
  const drawer = document.querySelector(".nav-drawer");
  if (drawer) {
    drawer.querySelectorAll('a[href="/login"], a[href="/register"]').forEach(a => a.remove());
    drawer.appendChild(logoutForm());
  }
}

/** Ledger stat count-up on the home page; call after the numbers' data-target values are set. */
function animateLedger() {
  const ledger = document.querySelector(".stats-ledger");
  if (!ledger) return;
  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const nums = ledger.querySelectorAll(".num");
  if (reduceMotion) {
    nums.forEach(n => n.textContent = Number(n.dataset.target).toLocaleString());
    return;
  }
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
