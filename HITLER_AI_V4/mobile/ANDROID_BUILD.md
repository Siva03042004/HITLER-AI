# Android app build (Capacitor wrapper)

The PWA is installable from a supported Android browser today. For a signed Android APK/AAB, use a machine with Android Studio, Android SDK and Java installed.

1. Run `npm install`.
2. Run `npm install @capacitor/core @capacitor/cli @capacitor/android`.
3. Run `npx cap add android` once.
4. Run `npx cap sync android`.
5. Run `npx cap open android`, then build a signed APK/AAB in Android Studio.

The `capacitor.config.json` is preconfigured. Do not ship API keys inside the Android app. The app should call the secured HTTPS backend. Microphone permissions and a privacy disclosure must be configured and tested before publishing.
