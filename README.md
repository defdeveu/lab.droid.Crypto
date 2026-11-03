# Crypto

Android teaching app for the "Development security in Android" course. It
encrypts and decrypts a message through numbered exercise cases.

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

- Select a case from the numbered rail, enter a message, then use Encrypt
  and Decrypt. Ciphertext is editable lowercase hexadecimal.
- The biometric case requests authentication separately for each operation.
  Device setup instructions are in [MANUAL.md](MANUAL.md).

## Tests

```sh
./gradlew testDebugUnitTest
```

The unit tests cover the encryption services, hexadecimal codec, operation
state and authentication request handling. Instrumented Compose tests live under `app/src/androidTest` and
need a running device or emulator.
