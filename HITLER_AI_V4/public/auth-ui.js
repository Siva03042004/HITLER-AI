const form = document.querySelector("#loginForm, #signupForm");
const message = document.querySelector("#message");

document.querySelectorAll("[data-toggle]").forEach((button) => {
  button.addEventListener("click", () => {
    const input = document.getElementById(button.dataset.toggle);
    const showing = input.type === "text";
    input.type = showing ? "password" : "text";
    button.textContent = showing ? "Show" : "Hide";
  });
});

async function checkExistingSession() {
  try {
    const response = await fetch("/api/auth/me", { credentials: "same-origin" });
    if (response.ok) window.location.replace("/app");
  } catch {}
}
checkExistingSession();

if (form) {
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    message.textContent = "";
    message.classList.remove("success");

    const data = Object.fromEntries(new FormData(form).entries());
    const isSignup = form.id === "signupForm";

    if (isSignup && data.password !== data.confirmPassword) {
      message.textContent = "Your passwords do not match.";
      return;
    }

    const button = form.querySelector(".submit");
    button.disabled = true;
    button.textContent = isSignup ? "Creating account…" : "Signing in…";

    try {
      const response = await fetch(isSignup ? "/api/auth/signup" : "/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin",
        body: JSON.stringify(data)
      });
      const result = await response.json();
      if (!response.ok) throw new Error(result.error || "Unable to complete your request.");

      message.textContent = "Success. Opening your workspace…";
      message.classList.add("success");
      window.location.replace("/app");
    } catch (error) {
      message.textContent = error.message;
      button.disabled = false;
      button.textContent = isSignup ? "Create account →" : "Sign in →";
    }
  });
}
