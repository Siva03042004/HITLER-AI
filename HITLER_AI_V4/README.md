# HITLER AI V4 — TC Private Limited

A cross-platform AI workspace foundation with a ChatGPT-style interface, multi-provider chat, five specialist AI personas, a local-first social feed, work studio, browser voice wake-word prototype, PWA support and Electron desktop packaging.

> Product branding: “HITLER AI” is used here solely as the supplied software product name. This project contains no extremist symbols, propaganda or glorification.

## AI team
- **Ultron** — strategy and optimization
- **Vision** — analysis and foresight
- **Jarvis** — personal assistant and quality
- **Friday** — execution and delivery
- **Edith** — research and context

The Five-AI Council runs the five specialist prompts in sequence and asks a chair model to synthesize them. These are role-based AI personas, not independent people or autonomous employees.

## Current capabilities
- Responsive web/PWA UI for desktop, tablet and mobile
- Server-side adapters for OpenAI, Anthropic Claude, Google Gemini and DeepSeek
- Five specialist personas and a council synthesis endpoint
- Local browser feed with posts, likes and comments
- Work Studio templates for email, planning, proposals, analysis and software requirements
- Opt-in browser voice wake phrase detection for “Jarvis”, “Vision”, “Ultron”, “Friday” and “Edith”; spoken acknowledgement: “Yes Boss. How can I help you?”
- Electron packaging targets for Windows, macOS and Linux
- Capacitor Android wrapper configuration and Android build guide

## Quick start
Requirements: Node.js 20+ and npm.

```bash
npm install
cp .env.example .env
# Add provider API keys to .env on the server only
npm run check
npm test
npm start
```

Open `http://localhost:8787`. Provider keys must never be placed in `public/`, a mobile bundle, source control, or chat.

## Provider configuration
Set one or more of `OPENAI_API_KEY`, `ANTHROPIC_API_KEY`, `GEMINI_API_KEY`, and `DEEPSEEK_API_KEY` in the server environment, and set the matching `*_MODEL` variable to an exact model ID currently enabled for that API account. The code intentionally has no stale hard-coded model defaults. For OpenAI, configure the GPT model your API account supports (including GPT-6 only if enabled for API use). Consumer product labels do not guarantee API availability; Astra needs an authorized supported endpoint before integration.

## Voice assistant privacy
Wake-word listening is OFF by default. The user must turn it on and grant microphone permission. Browser speech recognition is not a native always-on OS hotword service and may process speech through the browser vendor's service. The visible control turns listening off. Browser/OS support varies.

## Platform builds
- **Android:** install the PWA from a supported Android browser. For APK/AAB, follow [`mobile/ANDROID_BUILD.md`](mobile/ANDROID_BUILD.md) using Android Studio and Capacitor.
- **Windows:** `npm run dist:win` on a Windows build machine.
- **macOS:** `npm run dist:mac` on macOS; signing and notarization require Apple credentials.
- **Linux:** `npm run dist:linux` on a Linux build machine.
- **Web:** deploy the Node app or use the included Vercel function/config.

## Important production gaps
This is a feature-rich prototype/foundation, not a finished production social network or a certified “master AI”. Feed data is browser-local. Messages, accounts, cloud sync, multi-user social graph, notifications, media uploads, encrypted storage, billing, admin/moderation, real-time messaging, file ingestion, durable memory and secure tool execution are not yet implemented. The in-memory rate limiter is not sufficient for serverless production. Add a database, identity provider, distributed rate limits, monitoring, abuse controls, privacy policy, security review and backups before public launch.

## Documentation
- [`docs/ROADMAP.md`](docs/ROADMAP.md)
- [`docs/PLATFORM_RELEASE.md`](docs/PLATFORM_RELEASE.md)
