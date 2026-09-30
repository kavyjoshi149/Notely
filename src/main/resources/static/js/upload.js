/* Upload page: dropzone + client-side validation. Prototype only — no network call yet.
   Wire this to POST /notes/upload once NoteController exists (Step 7). */
document.addEventListener("DOMContentLoaded", function () {
  const dropzone = document.getElementById("dropzone");
  const fileInput = document.getElementById("fileInput");
  const fileChip = document.getElementById("fileChip");
  const fileNameEl = document.getElementById("fileName");
  const removeBtn = document.getElementById("removeFile");
  const form = document.getElementById("uploadForm");
  const successBanner = document.getElementById("successBanner");
  const courseSelect = document.getElementById("courseSelect");
  const branchSelect = document.getElementById("branchSelect");
  let selectedFile = null;

  // course -> branch, mirrors AcademicService.getBranchesByCourse on the backend
  const COURSE_BRANCHES = {
    btech: ["cse", "it", "ece", "eee", "mech", "civil"],
    bca: ["business"],
    math: ["math"],
  };

  courseSelect.addEventListener("change", () => {
    const allowed = COURSE_BRANCHES[courseSelect.value] || [];
    branchSelect.innerHTML = '<option value="">Select branch</option>' +
      BRANCHES.filter(b => allowed.includes(b.code))
        .map(b => `<option value="${b.code}">${b.label}</option>`).join("");
  });

  function pickFile(file) {
    if (!file) return;
    if (file.type !== "application/pdf") {
      alert("Please choose a PDF file.");
      return;
    }
    if (file.size > 15 * 1024 * 1024) {
      alert("File is larger than 15 MB.");
      return;
    }
    selectedFile = file;
    fileNameEl.textContent = `${file.name} · ${(file.size / 1024 / 1024).toFixed(1)} MB`;
    fileChip.style.display = "flex";
    dropzone.style.display = "none";
  }

  dropzone.addEventListener("click", () => fileInput.click());
  dropzone.addEventListener("keydown", e => { if (e.key === "Enter" || e.key === " ") fileInput.click(); });
  fileInput.addEventListener("change", () => pickFile(fileInput.files[0]));

  ["dragenter", "dragover"].forEach(evt =>
    dropzone.addEventListener(evt, e => { e.preventDefault(); dropzone.classList.add("dragover"); }));
  ["dragleave", "drop"].forEach(evt =>
    dropzone.addEventListener(evt, e => { e.preventDefault(); dropzone.classList.remove("dragover"); }));
  dropzone.addEventListener("drop", e => pickFile(e.dataTransfer.files[0]));

  removeBtn.addEventListener("click", () => {
    selectedFile = null;
    fileInput.value = "";
    fileChip.style.display = "none";
    dropzone.style.display = "block";
  });

  form.addEventListener("submit", function (e) {
    e.preventDefault();
    let ok = true;
    form.querySelectorAll("[required]").forEach(field => {
      const wrap = field.closest(".field");
      if (!field.value) { wrap.classList.add("has-error"); ok = false; }
      else wrap.classList.remove("has-error");
    });
    if (!selectedFile) {
      document.getElementById("fileError").style.display = "block";
      ok = false;
    } else {
      document.getElementById("fileError").style.display = "none";
    }
    if (!ok) return;

    successBanner.style.display = "flex";
    form.style.display = "none";
  });
});
