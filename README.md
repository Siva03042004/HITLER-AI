# Hitler AI — TC Private Limited (Android)

A native Android (Kotlin + Jetpack Compose) rewrite of the Hitler AI V4 workspace by TC Private Limited, powered by Firebase AI Logic (Gemini), Cloud Firestore, and Google Sign-In.

> Product branding: "Hitler AI" is preserved solely as the supplied software product name from the imported repository.

## Features
- **AI Workspace**: Multi-turn streaming conversations powered by Firebase AI Logic (`gemini-3.8-flash`) with quick-start prompts, recent chats synced to Cloud Firestore, and save-to-Library support.
- **Five-AI Council**: Sequential perspectives from **Edith** (Research), **Vision** (Foresight), **Ultron** (Optimization), **Friday** (Execution), and **Jarvis** (Quality), followed by **Head** synthesis and cloud-saved council history.
- **My AI Team**: Dedicated cards to launch conversations directly with each specialist persona.
- **Social Feed**: Create posts, toggle likes, add comments, and share via Android intents, synced privately to Firestore.
- **Work Studio & Discover**: Curated professional templates and topic starters that jump straight into AI Workspace.
- **Library**: Cloud-synced collection of saved AI replies and council syntheses.
- **Voice Assistant**: Opt-in native Android `SpeechRecognizer` dictation and wake-word detection for specialist names with `TextToSpeech` acknowledgement.
- **Google Sign-In & Adaptive UI**: Jetpack Credential Manager authentication, dark/light Material 3 theme toggle, and adaptive navigation drawer for phones and tablets.
