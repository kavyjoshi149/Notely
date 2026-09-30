/* Auth page: sign in / join free tabs, and the course -> branch dropdown for registration. */
document.addEventListener("DOMContentLoaded", function () {
  const tabs = document.querySelectorAll(".tab[data-auth-tab]");
  const panels = document.querySelectorAll(".auth-panel");

  tabs.forEach(t => t.addEventListener("click", () => {
    tabs.forEach(x => x.classList.remove("active"));
    t.classList.add("active");
    panels.forEach(p => p.style.display = p.id === t.dataset.authTab ? "block" : "none");
  }));

  if (location.search.includes("mode=signup")) {
    document.querySelector('[data-auth-tab="joinPanel"]').click();
  }

  const courseSelect = document.getElementById("regCourse");
  const branchSelect = document.getElementById("regBranch");
  const COURSE_BRANCHES = {
    btech: ["cse", "it", "ece", "eee", "mech", "civil"],
    bca: ["business"],
    math: ["math"],
  };
  if (courseSelect) {
    courseSelect.addEventListener("change", () => {
      const allowed = COURSE_BRANCHES[courseSelect.value] || [];
      branchSelect.innerHTML = '<option value="">Select branch</option>' +
        BRANCHES.filter(b => allowed.includes(b.code))
          .map(b => `<option value="${b.code}">${b.label}</option>`).join("");
    });
  }

  const joinForm = document.getElementById("joinForm");
  if (joinForm) {
    joinForm.addEventListener("submit", e => {
      e.preventDefault();
      const pw = document.getElementById("regPassword");
      const cpw = document.getElementById("regConfirm");
      let ok = true;
      joinForm.querySelectorAll("[required]").forEach(f => {
        const wrap = f.closest(".field");
        if (!f.value) { wrap.classList.add("has-error"); ok = false; }
        else wrap.classList.remove("has-error");
      });
      if (pw.value && cpw.value && pw.value !== cpw.value) {
        cpw.closest(".field").classList.add("has-error");
        cpw.closest(".field").querySelector(".error").textContent = "Passwords do not match";
        ok = false;
      }
      if (!ok) return;
      document.getElementById("joinSuccess").style.display = "flex";
      joinForm.style.display = "none";
    });
  }
});
