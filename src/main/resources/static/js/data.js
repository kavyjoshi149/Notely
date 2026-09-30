/* Notely — mock data used by every page until the real API is wired up.
   Replace NOTES/SUBJECTS with data from the Spring controllers in later steps. */

const STATS = { notes: 1240, students: 3860, downloads: 18400 };

// branch/tag -> visual tag class + label, kept in sync with the DataSeeder branches
const BRANCHES = [
  { code: "all",      label: "All branches" },
  { code: "cse",       label: "CSE",  tag: "tag-navy" },
  { code: "it",        label: "IT",   tag: "tag-navy" },
  { code: "ece",       label: "ECE",  tag: "tag-moss" },
  { code: "eee",       label: "EEE",  tag: "tag-moss" },
  { code: "mech",      label: "Mechanical", tag: "tag-brick" },
  { code: "civil",     label: "Civil", tag: "tag-brick" },
  { code: "math",      label: "Mathematics", tag: "tag-moss" },
  { code: "business",  label: "Business", tag: "tag-mustard" },
];

function branchInfo(code) {
  return BRANCHES.find(b => b.code === code) || BRANCHES[0];
}

const NOTES = [
  { id: 1, title: "Trees & Graphs — Unit 3", code: "CS301", branch: "cse", university: "AKTU",
    uploader: "Priya Sharma", initials: "PS", pages: 24, downloads: 842, likes: 96, daysAgo: 2,
    desc: "Handwritten notes covering BSTs, AVL trees, and graph traversal with solved problems." },
  { id: 2, title: "Thermodynamics — Laws & Cycles", code: "ME201", branch: "mech", university: "AKTU",
    uploader: "Rohit Verma", initials: "RV", pages: 31, downloads: 611, likes: 74, daysAgo: 4,
    desc: "Second-law problems, Carnot and Rankine cycles, worked numericals from past papers." },
  { id: 3, title: "Digital Electronics — Combinational Circuits", code: "EC204", branch: "ece", university: "University of Delhi",
    uploader: "Aisha Khan", initials: "AK", pages: 18, downloads: 530, likes: 61, daysAgo: 6,
    desc: "K-maps, multiplexers and adders explained with circuit diagrams." },
  { id: 4, title: "Microeconomics — Market Structures", code: "EC101", branch: "business", university: "University of Mumbai",
    uploader: "Karan Mehta", initials: "KM", pages: 22, downloads: 398, likes: 45, daysAgo: 1,
    desc: "Perfect competition, monopoly and oligopoly with diagrams and short-answer prep." },
  { id: 5, title: "Linear Algebra — Eigenvalues", code: "MA202", branch: "math", university: "AKTU",
    uploader: "Neha Gupta", initials: "NG", pages: 16, downloads: 705, likes: 88, daysAgo: 3,
    desc: "Diagonalization, eigenvectors and a bank of solved end-sem questions." },
  { id: 6, title: "Power Systems — Transmission Basics", code: "EE305", branch: "eee", university: "University of Delhi",
    uploader: "Arjun Nair", initials: "AN", pages: 27, downloads: 289, likes: 33, daysAgo: 9,
    desc: "Line parameters, per-unit system and load flow, summarised from lectures." },
  { id: 7, title: "Structural Analysis — Beams", code: "CE210", branch: "civil", university: "AKTU",
    uploader: "Simran Kaur", initials: "SK", pages: 20, downloads: 214, likes: 27, daysAgo: 11,
    desc: "Shear force and bending moment diagrams for statically determinate beams." },
  { id: 8, title: "Operating Systems — Scheduling", code: "CS304", branch: "cse", university: "University of Mumbai",
    uploader: "Dev Patel", initials: "DP", pages: 19, downloads: 466, likes: 58, daysAgo: 5,
    desc: "CPU scheduling algorithms compared with Gantt charts and solved examples." },
];

function daysAgoLabel(n) {
  if (n === 0) return "today";
  if (n === 1) return "1 day ago";
  return n + " days ago";
}
