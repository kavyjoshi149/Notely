/* Note detail page: loads /api/notes/{id} (id is the last path segment of /notes/{id}). */
document.addEventListener("DOMContentLoaded", async function () {
  const id = location.pathname.split("/").filter(Boolean).pop();
  const $ = i => document.getElementById(i);

  try {
    const note = await api("/api/notes/" + encodeURIComponent(id));
    document.title = note.title + " | Notely";
    $("noteTag").textContent = note.branch;
    $("noteTag").className = "note-tag " + tagClass(note.branch);
    $("noteTitle").textContent = note.title;
    $("noteCode").textContent = [note.code, note.university, note.semester ? "Semester " + note.semester : null,
      formatSize(note.fileSize)].filter(Boolean).join(" · ");
    $("noteDesc").textContent = note.description || note.subjectName || "";
    $("noteDownloads").textContent = note.downloads.toLocaleString();
    $("noteLikes").textContent = note.likes.toLocaleString();
    $("noteWhen").textContent = daysAgoLabel(note.daysAgo);
    $("uploaderName").textContent = note.uploader;
    $("uploaderInitials").textContent = note.initials;

    const me = await loadMe();
    const dl = $("downloadBtn");
    if (me.authenticated) {
      dl.href = "/api/notes/" + encodeURIComponent(note.id) + "/download";
    } else {
      dl.href = "/login";
      dl.textContent = "Log in to download";
    }
  } catch (e) {
    $("noteTitle").textContent = e.status === 404 ? "Note not found" : "Couldn't load this note";
    $("noteDesc").textContent = e.status === 404
      ? "It may have been removed, or it is only visible to students of another university."
      : e.message;
    $("noteTag").style.display = "none";
    $("downloadBtn").style.display = "none";
  }
});
