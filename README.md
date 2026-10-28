# Crypto

Android teaching app for the "Development security in Android" course. It
encrypts and decrypts a message, and shows two encryption implementations
side by side.

## Requirements

- Android Studio Rabbit (2026.2) or newer
- JDK 17 or newer (Android Studio's bundled JBR works)
- Android SDK platform 37 and build-tools 37.0.0
- A device or emulator running Android 7.0 (API 24) or newer

## Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio and run the `app` configuration.

## What the app does

- The first screen encrypts a message, shows the ciphertext, and decrypts a
  ciphertext back into plaintext. Ciphertext is lowercase hexadecimal.
- The second screen encrypts the same message with two implementations and
  shows both results.

## Tests

```sh
./gradlew testDebugUnitTest
```

The unit tests cover the encryption services, the hexadecimal codec, and both
view models. Instrumented Compose tests live under `app/src/androidTest` and
need a running device or emulator.
