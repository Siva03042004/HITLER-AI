const { complete } = require("./providers");

const agents = {
  Edith: { role: "Research and context specialist", style: "Evidence-led, curious, precise. Separate verified facts from assumptions." },
  Vision: { role: "Foresight and scenario specialist", style: "Thoughtful and measured. Explore future possibilities, risks and second-order effects." },
  Ultron: { role: "Optimization and challenge specialist", style: "Direct and rigorous. Stress-test assumptions, identify trade-offs and improve efficiency. Be constructive, never hostile." },
  Friday: { role: "Execution specialist", style: "Practical, concise and energetic. Convert decisions into steps, plans and deliverables." },
  Jarvis: { role: "Safety and quality guardian", style: "Professional and calm. Check accuracy, privacy, permissions, security and quality." }
};

const providerFor = (agent, selected) => selected?.[agent] || "openai";

async function runCouncil({ prompt, history = [], providers = {}, mode = "council" }) {
  const names = mode === "council" ? Object.keys(agents) : [mode];
  if (!names.every(n => agents[n])) throw new Error("Unknown specialist.");
  const transcript = [...history.slice(-12)];
  const turns = [];
  for (const name of names) {
    const a = agents[name];
    const context = transcript.map(m => `${m.role === "assistant" ? "Assistant" : "User"}: ${m.content}`).join("\n");
    const system = `You are ${name}, the ${a.role} in HITLER AI V4. ${a.style} You are one specialist in a collaborative assistant, not a real person. Respond in 2-5 useful paragraphs. Do not claim tools or actions you did not perform.`;
    const content = await complete({
      provider: providerFor(name, providers),
      system,
      messages: [{ role: "user", content: `User request: ${prompt}\n\nCouncil discussion so far:\n${context || "(No prior discussion)"}` }]
    });
    const turn = { agent: name, content };
    turns.push(turn);
    transcript.push({ role: "assistant", content: `${name}: ${content}` });
  }
  let synthesis = "";
  if (mode === "council") {
    synthesis = await complete({
      provider: providers.Head || "openai",
      system: "You are Head, the chairperson of a five-specialist AI council. Synthesize the specialists' contributions into one clear answer. Resolve disagreements, identify uncertainty, and give actionable next steps. Do not invent consensus.",
      messages: [{ role: "user", content: `Original request: ${prompt}\n\n${turns.map(t => `${t.agent}: ${t.content}`).join("\n\n")}` }]
    });
  }
  return { turns, synthesis };
}

module.exports = { agents, runCouncil };
