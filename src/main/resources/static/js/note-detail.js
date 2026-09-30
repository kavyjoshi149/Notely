/* Note detail page: reads ?id= from the URL, falls back to the first note. */
document.addEventListener("DOMContentLoaded", function () {
  const params = new URLSearchParams(location.search);
  const id = Number(params.get("id")) || NOTES[0].id;
  const note = NOTES.find(n => n.id === id) || NOTES[0];
  const b = branchInfo(note.branch);

  document.title = note.title + " | Notely";
  document.getElementById("noteTag").textContent = b.label;
  document.getElementById("noteTag").className = "note-tag " + b.tag;
  document.getElementById("noteTitle").textContent = note.title;
  document.getElementById("noteCode").textContent = `${note.code} · ${note.university} · ${note.pages} pages`;
  document.getElementById("noteDesc").textContent = note.desc;
  document.getElementById("noteDownloads").textContent = note.downloads.toLocaleString();
  document.getElementById("noteLikes").textContent = note.likes.toLocaleString();
  document.getElementById("noteWhen").textContent = daysAgoLabel(note.daysAgo);
  document.getElementById("uploaderName").textContent = note.uploader;
  document.getElementById("uploaderInitials").textContent = note.initials;

  const form = document.getElementById("commentForm");
  const list = document.getElementById("commentList");
  form.addEventListener("submit", function (e) {
    e.preventDefault();
    const input = document.getElementById("commentInput");
    if (!input.value.trim()) return;
    const row = document.createElement("div");
    row.className = "comment";
    row.innerHTML = `<span class="avatar">You</span>
      <div class="body"><span class="who">You<span class="when">just now</span></span><p>${input.value}</p></div>`;
    list.prepend(row);
    input.value = "";
  });
});
