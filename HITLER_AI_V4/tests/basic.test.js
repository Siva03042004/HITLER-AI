const test = require("node:test");
const assert = require("node:assert/strict");
const { agents } = require("../server/council");
test("five specialists are present with the required roles", () => {
  assert.deepEqual(Object.keys(agents), ["Edith","Vision","Ultron","Friday","Jarvis"]);
});
test("each specialist has role and style guidance", () => {
  for (const a of Object.values(agents)) {
    assert.ok(a.role.length > 5);
    assert.ok(a.style.length > 10);
  }
});
const { providerStatus, complete } = require("../server/providers");
test("providers are not marked ready without both a key and model ID", () => {
  const names = ["OPENAI_API_KEY", "OPENAI_MODEL", "ANTHROPIC_API_KEY", "ANTHROPIC_MODEL", "GEMINI_API_KEY", "GEMINI_MODEL", "DEEPSEEK_API_KEY", "DEEPSEEK_MODEL"];
  const previous = Object.fromEntries(names.map(name => [name, process.env[name]]));
  try {
    names.forEach(name => delete process.env[name]);
    assert.ok(providerStatus().every(provider => provider.configured === false));
  } finally {
    names.forEach(name => previous[name] === undefined ? delete process.env[name] : process.env[name] = previous[name]);
  }
});
test("unknown provider IDs are rejected before any network request", async () => {
  await assert.rejects(() => complete({ provider: "not-a-provider", messages: [] }), /Unknown provider/);
});
