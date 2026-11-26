# V4 staged roadmap

## Delivered in this source foundation
1. Responsive chat/workspace shell and local-first social feed.
2. Server-side adapters for OpenAI-compatible chat, Anthropic Messages and Gemini generateContent.
3. Sequential specialist council with Head synthesis.
4. Electron desktop packaging configuration for Windows/macOS/Linux.
5. PWA installable shell for Android browsers.

## Next engineering stages
### Stage 2 — Identity and persistence
PostgreSQL, migrations, passwordless/passkey or secure auth, session/device management, per-user authorization, encrypted backups, audit trail, retention controls.

### Stage 3 — Social features
Profiles, follow graph, posts/comments/reactions, private messages, group rooms, media upload with scanning, abuse reporting, moderation and admin tooling. Use privacy defaults and rate limits.

### Stage 4 — Master AI orchestration
Per-agent provider/model selection, streaming, tool registry, retrieval, citations, file ingestion, durable memory with user controls, context budgets, retries/fallbacks, observability and evaluation.

### Stage 5 — Native clients
Android app (Kotlin or Capacitor), iOS app if desired, Electron desktop builds, OS-specific permissions and secure storage. Explicit approval for every sensitive action.

### Stage 6 — Production readiness
Threat model, penetration test, privacy/legal review, billing and quotas, backups/restore drills, metrics/alerts, signed installers, macOS notarization, Windows signing, Play Store release process.

## Quality gates
- Tests pass on supported Node LTS.
- No secrets in browser bundles or source control.
- Authorization checks on every user-owned resource.
- Rate limiting and abuse controls tested.
- Accessibility, keyboard navigation and mobile responsive checks.
- Per-platform packaging tested on native runners.
