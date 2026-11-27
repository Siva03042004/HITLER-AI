const { app, BrowserWindow, shell } = require("electron");
const expressApp = require("../server/app");
let server;
function createWindow() {
  const win = new BrowserWindow({ width: 1440, height: 940, minWidth: 760, minHeight: 620, title: "HITLER AI · TC Private Limited", backgroundColor: "#080c14", webPreferences: { contextIsolation: true, nodeIntegration: false, sandbox: true } });
  win.loadURL("http://127.0.0.1:8787");
  win.webContents.setWindowOpenHandler(({ url }) => { shell.openExternal(url); return { action: "deny" }; });
}
app.whenReady().then(() => {
  server = expressApp.listen(8787, "127.0.0.1", createWindow);
  app.on("activate", () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); });
});
app.on("before-quit", () => { if (server) server.close(); });
app.on("window-all-closed", () => { if (process.platform !== "darwin") app.quit(); });
