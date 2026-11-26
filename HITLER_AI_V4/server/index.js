require("dotenv").config();
const app = require("./app");
const port = Number(process.env.PORT || 8787);
app.listen(port, process.env.HOST || "0.0.0.0", () => console.log(`HITLER AI V4 running on port ${port}`));
