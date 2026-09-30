/* Upload page: dropzone + validation, then multipart POST /api/notes. */
document.addEventListener("DOMContentLoaded", function () {
  const dropzone = document.getElementById("dropzone");
  const fileInput = document.getElementById("fileInput");
  const fileChip = document.getElementById("fileChip");
  const fileNameEl = document.getElementById("fileName");
  const removeBtn = document.getElementById("removeFile");
  const form = document.getElementById("uploadForm");
  const formError = document.getElementById("uploadError");
  const courseSelect = document.getElementById("courseSelect");
  const branchSelect = document.getElementById("branchSelect");
  let selectedFile = null;

  loadCourses(courseSelect);
  wireCourseBranch(courseSelect, branchSelect);

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

  form.addEventListener("submit", async function (e) {
    e.preventDefault();
    formError.style.display = "none";
    let ok = true;
    form.querySelectorAll("[required]").forEach(field => {
      const wrap = field.closest(".field");
      if (!field.value.trim()) { wrap.classList.add("has-error"); ok = false; }
      else wrap.classList.remove("has-error");
    });
    document.getElementById("fileError").style.display = selectedFile ? "none" : "block";
    if (!selectedFile) ok = false;
    if (!ok) return;

    const data = new FormData();
    data.append("file", selectedFile);
    data.append("title", document.getElementById("title").value.trim());
    data.append("description", document.getElementById("description").value.trim());
    data.append("branchId", branchSelect.value);
    data.append("subjectName", document.getElementById("subjectName").value.trim());
    data.append("subjectCode", document.getElementById("subjectCode").value.trim());
    data.append("semester", document.getElementById("semester").value.trim());
    data.append("universityOnly", document.getElementById("visibilityToggle").checked ? "true" : "false");

    const submit = form.querySelector('button[type="submit"]');
    submit.disabled = true;
    submit.textContent = "Publishing…";
    try {
      const note = await api("/api/notes", { method: "POST", body: data });
      location.href = "/notes/" + note.id;
    } catch (err) {
      if (err.status === 401) { location.href = "/login"; return; }
      formError.textContent = err.message;
      formError.style.display = "block";
      submit.disabled = false;
      submit.textContent = "Publish note";
    }
  });
});
