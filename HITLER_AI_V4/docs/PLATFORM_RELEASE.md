# HITLER AI V4 — platform release guide

## Supported delivery targets
- Web: Express app; deploy to a Node host or Vercel.
- Android: installable PWA; optional Capacitor Android wrapper for APK/AAB.
- Windows: Electron + electron-builder (NSIS installer / portable).
- macOS: Electron + electron-builder (DMG / ZIP). Build and sign on macOS; notarization requires Apple developer credentials.
- Linux: Electron + electron-builder (AppImage / DEB).

## Desktop builds
Install dependencies with Node.js 20+, then run `npm run dist:win`, `npm run dist:mac`, or `npm run dist:linux` on the matching native build machine. Cross-building macOS artifacts from Windows/Linux is not a supported release assumption. Sign Windows and macOS artifacts before public distribution.

## Current capability boundary
This release is a working UI and integration foundation. The social feed, likes and comments are local browser data, not a multi-user network. Messages are a UI preview only. Accounts, cloud persistence, private messaging, media uploads, moderation, billing, file search, tool execution, durable memory, streaming and production voice services require backend services and security review.

## Voice privacy
Wake-word listening is opt-in, off by default, and uses browser speech recognition where available. It requires microphone permission and may send audio to the browser vendor's speech recognition service. It is not an always-on native OS hotword engine. Provide a clear privacy notice and a visible off control.

## AI model routing
OpenAI, Anthropic, Google Gemini and DeepSeek adapters are supported by server-side credentials. A brand name or consumer-app model selector does not imply API access. Models such as Astra must be connected through an authorized, documented API endpoint before they can be selected. Never embed provider keys in a browser or mobile bundle.
