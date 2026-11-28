# HITLER AI — Unified Cross-Platform Architecture

This repository is the single source of truth for the HITLER AI product.

## Existing foundations
- `app/`: Native Android application built with Kotlin and Jetpack Compose.
- Android already uses Firebase Authentication, Firestore, Firebase AI Logic (Gemini), App Check, Google Sign-In and specialist personas.
- The V4 ZIP supplies the web/PWA client, Node/Express provider layer, five-specialist council and Electron desktop wrapper.

## Target repository layout
```text
/
  app/                         # Android Kotlin app (keep native)
  web/                         # V4 web/PWA client and Vercel entry point
    public/
    api/
    package.json
  desktop/                     # Electron wrapper for Windows/macOS/Linux
  shared/
    assistant-personas.json    # shared assistant names and role metadata
  firebase/
    firestore.rules
    firestore.indexes.json
  docs/
```

## Shared services
- Firebase Authentication: one account system across Android and web.
- Cloud Firestore: user-scoped chat history, council sessions, saved library items and social feed.
- Firebase Storage: user-owned attachments and media, guarded by Storage Rules.
- Gemini: Firebase AI Logic on supported clients; privileged operations go through authenticated Cloud Functions.
- App Check, Firestore/Storage Security Rules and server-side authorization are required before public release.

## Five specialists
- Edith — research
- Vision — foresight
- Ultron — optimization
- Friday — execution
- Jarvis — quality and safety
- Head — final synthesis

## Platform delivery
- Android: native Kotlin/Compose app in `app/`.
- Web/PWA: V4 web workspace in `web/`, deployed from the `web` root directory on Vercel.
- Windows/macOS/Linux: Electron shell in `desktop/`, packaged on each target operating system.
- All clients use the same Firebase project and Firestore data model.

## Migration rules
1. Preserve the Android application as the native mobile client.
2. Port the V4 browser UI and Electron shell into the repository without replacing Android Gradle files.
3. Replace V4's direct provider-key backend with Firebase AI Logic or authenticated Cloud Functions.
4. Remove MongoDB code, connection strings, environment variables and dependencies from application source.
5. Never put a Gemini secret key in browser or Android source. Use Firebase AI Logic or Secret Manager-backed Cloud Functions.
6. Keep Firebase client configuration public by design, but enforce access through Security Rules and App Check.
7. Do not commit `.env`, service-account JSON, signing keys, or production secrets.

## Deployment
- Vercel project root: `web/` after the Vercel-compatible API adapter and static output are validated.
- Android release: build and sign the native app through the Android Gradle toolchain.
- Desktop release: package with Electron Builder separately on Windows, macOS and Linux.
- A deployment is complete only after CI checks pass and the production URL is verified.
