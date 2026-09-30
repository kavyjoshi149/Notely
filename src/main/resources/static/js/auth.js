/* Auth page: sign in (native form POST /login handled by Spring Security) and
   join free (JSON POST /api/public/register). Dropdowns come from the database. */
document.addEventListener("DOMContentLoaded", function () {
  const tabs = document.querySelectorAll(".tab[data-auth-tab]");
  const panels = document.querySelectorAll(".auth-panel");

  function showTab(id) {
    tabs.forEach(x => x.classList.toggle("active", x.dataset.authTab === id));
    panels.forEach(p => p.style.display = p.id === id ? "block" : "none");
  }
  tabs.forEach(t => t.addEventListener("click", () => showTab(t.dataset.authTab)));

  // /register opens the Join tab; the ?error / ?logout / ?registered flags come from Spring Security & the app
  const params = new URLSearchParams(location.search);
  if (location.pathname === "/register") showTab("joinPanel");
  const notice = document.getElementById("signinNotice");
  if (params.has("error")) showNotice(notice, "Wrong username/email or password.", true);
  else if (params.has("registered")) showNotice(notice, "Account created — log in to continue.", false);
  else if (params.has("logout")) showNotice(notice, "You have been logged out.", false);

  // registration dropdowns
  const uniSelect = document.getElementById("regUniversity");
  const courseSelect = document.getElementById("regCourse");
  const branchSelect = document.getElementById("regBranch");
  api("/api/public/universities").then(list => fillSelect(uniSelect, list, "Select university")).catch(() => {});
  loadCourses(courseSelect);
  wireCourseBranch(courseSelect, branchSelect);

  // registration form -> backend
  const FIELD_IDS = {
    name: "regName", username: "regUsername", email: "regEmail", password: "regPassword",
    confirmPassword: "regConfirm", batch: "regBatch", rollNumber: "regRoll",
    universityId: "regUniversity", courseId: "regCourse", branchId: "regBranch",
  };
  const joinForm = document.getElementById("joinForm");
  const formError = document.getElementById("joinError");

  function setFieldError(fieldName, message) {
    const input = document.getElementById(FIELD_IDS[fieldName]);
    if (!input) return false;
    const wrap = input.closest(".field");
    wrap.classList.add("has-error");
    if (message) wrap.querySelector(".error").textContent = message;
    return true;
  }

  joinForm.addEventListener("submit", async e => {
    e.preventDefault();
    formError.style.display = "none";
    joinForm.querySelectorAll(".field").forEach(f => f.classList.remove("has-error"));

    const v = id => document.getElementById(id).value.trim();
    const pw = document.getElementById("regPassword").value;
    const cpw = document.getElementById("regConfirm").value;
    let ok = true;
    joinForm.querySelectorAll("[required]").forEach(f => {
      if (!f.value.trim()) { f.closest(".field").classList.add("has-error"); ok = false; }
    });
    if (pw && pw.length < 8) { setFieldError("password", "Password must be at least 8 characters."); ok = false; }
    if (pw && cpw && pw !== cpw) { setFieldError("confirmPassword", "Passwords do not match"); ok = false; }
    if (!ok) return;

    const submit = joinForm.querySelector('button[type="submit"]');
    submit.disabled = true;
    try {
      await api("/api/public/register", {
        method: "POST",
        json: {
          name: v("regName"), username: v("regUsername"), email: v("regEmail"),
          password: pw, confirmPassword: cpw, batch: v("regBatch"), rollNumber: v("regRoll"),
          universityId: Number(v("regUniversity")) || null,
          courseId: Number(v("regCourse")) || null,
          branchId: Number(v("regBranch")) || null,
        },
      });
      location.href = "/login?registered";
    } catch (err) {
      const errors = (err.data && err.data.errors) || {};
      let shown = false;
      Object.keys(errors).forEach(k => { shown = setFieldError(k, errors[k]) || shown; });
      if (!shown) showNotice(formError, err.message, true);
    } finally {
      submit.disabled = false;
    }
  });
});

function showNotice(el, message, isError) {
  if (!el) return;
  el.textContent = message;
  el.style.display = "block";
  el.style.background = isError ? "#FBEAE6" : "var(--moss-tint)";
  el.style.color = isError ? "#9B2C1B" : "var(--moss)";
}
