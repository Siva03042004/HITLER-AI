const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const root = path.join(__dirname, "..");
test("workspace includes all primary product areas", () => {
  const html = fs.readFileSync(path.join(root, "public/index.html"), "utf8");
  for (const id of ["chatView", "councilView", "agentsView", "feedView", "messagesView", "discoverView", "workView", "libraryView"]) {
    assert.match(html, new RegExp(`id="${id}"`));
  }
});
test("voice assistant is opt-in and includes the five wake names", () => {
  const html = fs.readFileSync(path.join(root, "public/index.html"), "utf8");
  const js = fs.readFileSync(path.join(root, "public/app.js"), "utf8");
  assert.match(html, /Wake word off/);
  for (const name of ["Jarvis", "Vision", "Ultron", "Friday", "Edith"]) assert.ok(js.includes(name));
});
test("PWA manifest references real icon assets", () => {
  const manifest = JSON.parse(fs.readFileSync(path.join(root, "public/manifest.webmanifest"), "utf8"));
  for (const icon of manifest.icons) assert.ok(fs.existsSync(path.join(root, "public", icon.src.replace(/^\//, ""))));
});
test("desktop and Android build guidance are included", () => {
  const pkg = JSON.parse(fs.readFileSync(path.join(root, "package.json"), "utf8"));
  assert.ok(pkg.scripts["dist:win"] && pkg.scripts["dist:mac"] && pkg.scripts["dist:linux"]);
  assert.ok(fs.existsSync(path.join(root, "mobile/ANDROID_BUILD.md")));
});
