const env = process.env;

function providerStatus() {
  return [
    { id: "openai", label: "OpenAI", configured: !!env.OPENAI_API_KEY && !!env.OPENAI_MODEL, model: env.OPENAI_MODEL || "Set OPENAI_MODEL" },
    { id: "anthropic", label: "Claude", configured: !!env.ANTHROPIC_API_KEY && !!env.ANTHROPIC_MODEL, model: env.ANTHROPIC_MODEL || "Set ANTHROPIC_MODEL" },
    { id: "gemini", label: "Google Gemini", configured: !!env.GEMINI_API_KEY && !!env.GEMINI_MODEL, model: env.GEMINI_MODEL || "Set GEMINI_MODEL" },
    { id: "deepseek", label: "DeepSeek", configured: !!env.DEEPSEEK_API_KEY && !!env.DEEPSEEK_MODEL, model: env.DEEPSEEK_MODEL || "Set DEEPSEEK_MODEL" }
  ];
}

function requiredModel(name) {
  if (!env[name]) throw new Error(`${name} is not configured. Set it to a current model ID enabled for your API account.`);
  return env[name];
}

async function jsonFetch(url, headers, body) {
  const response = await fetch(url, { method: "POST", headers: { "content-type": "application/json", ...headers }, body: JSON.stringify(body) });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.error?.message || data.message || `Provider request failed (${response.status})`);
  return data;
}

async function complete({ provider = "openai", messages, system = "", temperature = 0.5 }) {
  let data;
  if (provider === "openai" || provider === "deepseek") {
    const deep = provider === "deepseek";
    const key = deep ? env.DEEPSEEK_API_KEY : env.OPENAI_API_KEY;
    if (!key) throw new Error(`${deep ? "DeepSeek" : "OpenAI"} API key is not configured on the server.`);
    const base = deep ? (env.DEEPSEEK_BASE_URL || "https://api.deepseek.com") : (env.OPENAI_BASE_URL || "https://api.openai.com/v1");
    data = await jsonFetch(`${base.replace(/\/$/, "")}/chat/completions`, { authorization: `Bearer ${key}` }, {
      model: deep ? requiredModel("DEEPSEEK_MODEL") : requiredModel("OPENAI_MODEL"),
      messages: [...(system ? [{ role: "system", content: system }] : []), ...messages],
      temperature
    });
    return data.choices?.[0]?.message?.content || "";
  }
  if (provider === "anthropic") {
    if (!env.ANTHROPIC_API_KEY) throw new Error("Anthropic API key is not configured on the server.");
    data = await jsonFetch("https://api.anthropic.com/v1/messages", {
      "x-api-key": env.ANTHROPIC_API_KEY, "anthropic-version": "2023-06-01"
    }, {
      model: requiredModel("ANTHROPIC_MODEL"), max_tokens: 1800, temperature,
      system, messages: messages.map(m => ({ role: m.role, content: m.content }))
    });
    return (data.content || []).filter(x => x.type === "text").map(x => x.text).join("\n");
  }
  if (provider === "gemini") {
    if (!env.GEMINI_API_KEY) throw new Error("Google Gemini API key is not configured on the server.");
    const model = requiredModel("GEMINI_MODEL");
    const contents = messages.map(m => ({ role: m.role === "assistant" ? "model" : "user", parts: [{ text: m.content }] }));
    if (system) contents.unshift({ role: "user", parts: [{ text: `System instructions: ${system}` }] });
    data = await jsonFetch(`https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${env.GEMINI_API_KEY}`, {}, {
      contents, generationConfig: { temperature, maxOutputTokens: 1800 }
    });
    return data.candidates?.[0]?.content?.parts?.map(p => p.text || "").join("") || "";
  }
  throw new Error(`Unknown provider: ${provider}`);
}

module.exports = { complete, providerStatus };
