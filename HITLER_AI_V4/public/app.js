const $ = s => document.querySelector(s);
const agentData = [
 {name:"Ultron",role:"Strategy & optimization",desc:"Challenges assumptions, finds bottlenecks and improves plans.",letter:"U",color:"#ff827c",prompt:"Act as Ultron, my strategy and optimization specialist. Challenge assumptions constructively and help me improve this task: "},
 {name:"Vision",role:"Analysis & foresight",desc:"Explores scenarios, trade-offs, systems and long-term outcomes.",letter:"V",color:"#e8bd61",prompt:"Act as Vision, my analysis and foresight specialist. Explore scenarios, trade-offs and long-term implications for: "},
 {name:"Jarvis",role:"Personal assistant & quality",desc:"Organizes tasks, checks quality and helps you make informed decisions.",letter:"J",color:"#61d7b8",prompt:"Act as Jarvis, my personal assistant. Organize this request into a clear, high-quality response: "},
 {name:"Friday",role:"Execution & delivery",desc:"Turns decisions into practical steps, drafts and deliverables.",letter:"F",color:"#ad9bff",prompt:"Act as Friday, my execution specialist. Turn this into a practical plan and deliverable: "},
 {name:"Edith",role:"Research & context",desc:"Researches carefully and separates evidence from assumptions.",letter:"E",color:"#65b9ff",prompt:"Act as Edith, my research specialist. Explain the evidence, sources to verify and uncertainties about: "}
];
const views=["chat","council","agents","feed","messages","discover","work","library"];
const titles={chat:"AI Workspace",council:"AI Council",agents:"My AI Team",feed:"Social Feed",messages:"Messages",discover:"Discover",work:"Work Studio",library:"Library"};
const state={view:"chat",messages:read("tc_hitler_ai_current_chat",[]),provider:"openai",providers:[],posts:read("tc_hitler_ai_posts",[]),wake:false,recognition:null,recognitionMode:"wake",activeAgent:null};
function read(k,f){try{return JSON.parse(localStorage.getItem(k))??f}catch{return f}}
function save(k,v){try{localStorage.setItem(k,JSON.stringify(v))}catch{toast("Local storage is unavailable in this browser.")}}
function esc(s){return String(s).replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]))}
function toast(s){const t=$("#toast");t.textContent=s;t.classList.add("show");clearTimeout(toast.timer);toast.timer=setTimeout(()=>t.classList.remove("show"),3200)}
function switchView(v){state.view=v;document.querySelectorAll(".nav-item").forEach(b=>b.classList.toggle("active",b.dataset.view===v));views.forEach(x=>{const el=$("#"+x+"View");if(el)el.hidden=x!==v});$("#welcome").hidden=!(v==="chat"&&!state.messages.length);$("#chatView").hidden=!(v==="chat"&&state.messages.length>0);$("#inputDock").hidden=!(v==="chat"||v==="council");$("#viewTitle").textContent=titles[v];$("#sidebar").classList.remove("open")}
document.querySelectorAll(".nav-item").forEach(b=>b.addEventListener("click",()=>switchView(b.dataset.view)));
document.querySelectorAll("[data-go]").forEach(b=>b.addEventListener("click",()=>switchView(b.dataset.go)));
$("#menuBtn").onclick=()=>$("#sidebar").classList.toggle("open");
function resetChat(){state.messages=[];save("tc_hitler_ai_current_chat",state.messages);renderMessages();switchView("chat");$("#prompt").focus()}
$("#newChat").onclick=resetChat;$("#clearChat").onclick=resetChat;$("#startChat").onclick=()=>{$("#prompt").focus();$("#prompt").scrollIntoView({behavior:"smooth",block:"center"})};
$("#themeBtn").onclick=()=>{document.body.classList.toggle("light");save("tc_hitler_ai_light",document.body.classList.contains("light"))};if(read("tc_hitler_ai_light",false))document.body.classList.add("light");
$("#settingsBtn").onclick=()=>$("#settingsDialog").showModal();$("#provider").onchange=e=>state.provider=e.target.value;
async function loadStatus(){try{const r=await fetch("/api/providers");const p=await r.json();state.providers=p;const n=p.filter(x=>x.configured).length;$("#providerState").textContent=`${n} provider${n===1?"":"s"} connected`;$("#providerList").innerHTML=p.map(x=>`<div class="provider-row">${esc(x.label)} · ${esc(x.model)} <span>${x.configured?"Configured":"Needs server key"}</span></div>`).join("")}catch{$("#providerState").textContent="Server unavailable"}}
function renderMessages(){$("#welcome").hidden=state.messages.length>0;$("#chatView").hidden=state.messages.length===0;$("#messages").innerHTML=state.messages.map(m=>`<div class="message ${m.role}"><div class="avatar">${m.role==="user"?"YOU":"H"}</div><div class="message-body"><small>${m.role==="user"?"YOU":"HITLER AI"}</small><div class="bubble">${esc(m.content)}</div></div></div>`).join("");$("#messages").scrollTop=$("#messages").scrollHeight}
async function send(textOverride){const input=$("#prompt"),text=(textOverride??input.value).trim();if(!text)return;if(state.view==="council"){await council(text);return}state.messages.push({role:"user",content:text});save("tc_hitler_ai_current_chat",state.messages);input.value="";renderMessages();state.messages.push({role:"assistant",content:"Thinking…"});renderMessages();try{const r=await fetch("/api/chat",{method:"POST",headers:{"content-type":"application/json"},body:JSON.stringify({provider:state.provider,messages:state.messages.slice(0,-1)})});const d=await r.json();if(!r.ok)throw Error(d.error||"Request failed");state.messages[state.messages.length-1].content=d.reply||"No text response was returned."}catch(e){state.messages[state.messages.length-1].content=`I couldn't complete that request: ${e.message}\n\nCheck the server status and selected provider configuration.`}save("tc_hitler_ai_current_chat",state.messages);renderMessages();addHistory(text);}
$("#sendBtn").onclick=()=>send();$("#prompt").addEventListener("keydown",e=>{if(e.key==="Enter"&&!e.shiftKey){e.preventDefault();send()}});
document.querySelectorAll("[data-prompt]").forEach(b=>b.addEventListener("click",()=>{switchView("chat");$("#prompt").value=b.dataset.prompt;$("#prompt").focus()}));
function addHistory(t){const b=document.createElement("button");b.textContent=t.slice(0,46);b.title=t;b.onclick=()=>{switchView("chat");toast("Recent chat titles are local shortcuts; full chat history is not synced yet.")};$("#history").prepend(b)}
function renderAgents(){const cards=agentData.map(a=>`<article class="agent" style="--agent:${a.color}"><div class="orb">${a.letter}</div><b>${a.name}</b><small>${a.role}</small></article>`).join("");$("#agentGrid").innerHTML=cards;$("#assistantCards").innerHTML=agentData.map(a=>`<article class="assistant-card" style="--agent:${a.color}"><div class="assistant-top"><div class="orb">${a.letter}</div><span class="assistant-status">SPECIALIST AI</span></div><h3>${a.name}</h3><b>${a.role}</b><p>${a.desc}</p><button class="secondary-btn" data-agent="${a.name}">Work with ${a.name} ↗</button></article>`).join("");document.querySelectorAll("[data-agent]").forEach(b=>b.onclick=()=>{const a=agentData.find(x=>x.name===b.dataset.agent);state.activeAgent=a.name;switchView("chat");$("#prompt").value=a.prompt;$("#prompt").focus()})}
async function council(prompt){$("#prompt").value="";$("#councilOutput").innerHTML='<div class="empty-state">Your five specialists are preparing their perspectives…</div>';try{const map={Head:state.provider,Edith:state.provider,Vision:state.provider,Ultron:state.provider,Friday:state.provider,Jarvis:state.provider};const r=await fetch("/api/council",{method:"POST",headers:{"content-type":"application/json"},body:JSON.stringify({prompt,providers:map})});const d=await r.json();if(!r.ok)throw Error(d.error||"Council failed");const colors=Object.fromEntries(agentData.map(a=>[a.name,a.color]));$("#councilOutput").innerHTML=d.turns.map(t=>`<article class="council-turn" style="--agent:${colors[t.agent]||"#aaa"}"><b>${esc(t.agent)}</b><p>${esc(t.content)}</p></article>`).join("")+`<article class="synthesis"><b>HEAD · COUNCIL SYNTHESIS</b><p>${esc(d.synthesis)}</p></article>`}catch(e){$("#councilOutput").innerHTML=`<div class="empty-state">${esc(e.message)}. Check that the required provider is configured.</div>`}}
renderAgents();
$("#publishPost").onclick=()=>{const text=$("#postText").value.trim();if(!text)return;state.posts.unshift({id:crypto.randomUUID?.()||String(Date.now()),text,at:new Date().toLocaleString(),likes:0,liked:false,comments:[]});save("tc_hitler_ai_posts",state.posts);$("#postText").value="";renderFeed();toast("Post saved to this browser.")};
function renderFeed(){$("#feedPosts").innerHTML=state.posts.map((p,i)=>`<article class="post"><div class="post-head"><div class="user-avatar">TC</div><div><b>TC Workspace</b><small>${esc(p.at||"")} · Local preview</small></div><span class="post-menu">•••</span></div><p>${esc(p.text)}</p><div class="post-stats"><span>${p.likes||0} likes</span><span>${(p.comments||[]).length} comments</span></div><div class="post-actions"><button data-like="${i}">${p.liked?"♥":"♡"} Like</button><button data-comment="${i}">▢ Comment</button><button data-share="${i}">↗ Share</button></div><div class="comment-row"><input data-comment-input="${i}" placeholder="Write a comment…"><button data-comment-send="${i}">Send</button></div>${(p.comments||[]).map(c=>`<div class="comment"><b>TC Workspace</b> ${esc(c)}</div>`).join("")}</article>`).join("")||'<div class="empty-state">No posts yet. Share the first update.</div>';document.querySelectorAll("[data-like]").forEach(b=>b.onclick=()=>{const p=state.posts[+b.dataset.like];p.liked=!p.liked;p.likes=Math.max(0,(p.likes||0)+(p.liked?1:-1));save("tc_hitler_ai_posts",state.posts);renderFeed()});document.querySelectorAll("[data-comment-send]").forEach(b=>b.onclick=()=>{const i=+b.dataset.commentSend,input=document.querySelector(`[data-comment-input="${i}"]`),t=input.value.trim();if(!t)return;state.posts[i].comments.push(t);save("tc_hitler_ai_posts",state.posts);renderFeed()});document.querySelectorAll("[data-comment-input]").forEach(x=>x.addEventListener("keydown",e=>{if(e.key==="Enter")document.querySelector(`[data-comment-send="${x.dataset.commentInput}"]`).click()}));document.querySelectorAll("[data-share]").forEach(b=>b.onclick=()=>{const p=state.posts[+b.dataset.share];if(navigator.share)navigator.share({title:"HITLER AI",text:p.text}).catch(()=>{});else navigator.clipboard?.writeText(p.text).then(()=>toast("Post copied."))})}
renderFeed();
// Voice dictation is one-shot. Wake-word listening is explicit, visible and off by default.
const SpeechRecognition=window.SpeechRecognition||window.webkitSpeechRecognition;
function speak(text){if(!("speechSynthesis" in window))return;window.speechSynthesis.cancel();const u=new SpeechSynthesisUtterance(text);u.rate=1;u.pitch=1;window.speechSynthesis.speak(u)}
function setWake(on){if(!SpeechRecognition){toast("Wake-word voice is not supported by this browser. Try a supported Chromium browser.");return}if(on===state.wake)return;state.wake=on;$("#wakeLabel").textContent=on?"Wake word on":"Wake word off";$("#wakeBtn").classList.toggle("enabled",on);if(on){startWakeRecognition();toast("Wake-word listening enabled. Say Jarvis, Vision, Ultron, Friday or Edith.")}else{try{state.recognition?.stop()}catch{}state.recognition=null;toast("Wake-word listening stopped.")}}
function startWakeRecognition(){if(!state.wake||!SpeechRecognition)return;const rec=new SpeechRecognition();rec.continuous=true;rec.interimResults=false;rec.lang="en-IN";state.recognition=rec;rec.onresult=e=>{const phrase=e.results[e.results.length-1][0].transcript.trim();const match=agentData.find(a=>new RegExp(`\\b${a.name}\\b`,"i").test(phrase));if(!match)return;state.activeAgent=match.name;const command=phrase.replace(new RegExp(`\\b${match.name}\\b`,"i"),"").replace(/^[,\s]+/,"").trim();speak("Yes Boss. How can I help you?");if(command){setTimeout(()=>{switchView("chat");send(command)},700)}else{setTimeout(()=>startCommandCapture(match.name),1000)}};rec.onerror=e=>{if(e.error==="not-allowed"){setWake(false);toast("Microphone permission was denied.")}};rec.onend=()=>{if(state.wake&&state.recognition===rec)setTimeout(startWakeRecognition,650)};try{rec.start()}catch{}}
function startCommandCapture(agent){if(!SpeechRecognition||!state.wake)return;const rec=new SpeechRecognition();rec.lang="en-IN";rec.continuous=false;rec.interimResults=false;rec.onresult=e=>{const text=e.results[0][0].transcript.trim();if(text){switchView("chat");const prefix=agentData.find(a=>a.name===agent)?.prompt||"";send(prefix+text)}};rec.onerror=()=>toast("I couldn't hear that. Say the assistant name again.");rec.onend=()=>{if(state.wake)startWakeRecognition()};try{state.recognition?.stop();state.recognition=rec;rec.start()}catch{}}
$("#wakeBtn").onclick=()=>setWake(!state.wake);
$("#voiceBtn").onclick=()=>{if(!SpeechRecognition){toast("Voice dictation is not supported in this browser.");return}const rec=new SpeechRecognition();rec.lang="en-IN";rec.continuous=false;rec.interimResults=false;rec.onresult=e=>{$("#prompt").value+=( $("#prompt").value?" ":"")+e.results[0][0].transcript;$("#prompt").focus()};rec.onerror=()=>toast("Voice input could not start. Check microphone permission.");try{rec.start()}catch{toast("Voice input is already active.")}};
if("serviceWorker" in navigator)window.addEventListener("load",()=>navigator.serviceWorker.register("/sw.js").catch(()=>{}));
loadStatus();
switchView("chat");

async function loadAccount() {
  try {
    const response = await fetch("/api/auth/me", { credentials: "same-origin" });
    if (response.status === 401) return window.location.replace("/login");
    if (!response.ok) throw new Error("Account unavailable");
    const data = await response.json();
    const name = document.querySelector("#signedInName");
    const email = document.querySelector("#signedInEmail");
    if (name) name.textContent = data.user.name;
    if (email) email.textContent = data.user.email;
  } catch {
    toast("Could not verify your account session.");
  }
}
const logoutButton = document.querySelector("#logoutBtn");
if (logoutButton) logoutButton.addEventListener("click", async () => {
  logoutButton.disabled = true;
  try {
    await fetch("/api/auth/logout", { method: "POST", credentials: "same-origin" });
  } finally {
    window.location.replace("/login");
  }
});
loadAccount();
