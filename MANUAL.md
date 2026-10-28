# Crypto lab — run steps

This lab has a single branch; there is no challenge/solution pair to switch
between.

## 1. Prerequisites

- Android Studio Rabbit (2026.2) or newer, with Android SDK platform 37 and
  build-tools 37.0.0
- JDK 17 or newer; Android Studio's bundled JBR works
- A device or emulator running Android 7.0 (API 24) or newer

## 2. Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 3. Run

1. Launch **Crypto**.
2. On the first screen, type a message, press **Encrypt**, and read the
   ciphertext below the field.
3. Press **Decrypt** to turn that ciphertext back into plaintext.
4. Press **Compare implementations** to open the second screen, type the same
   message, and press **Compare** to see both results.

## 4. Notes

- Ciphertext is lowercase hexadecimal.
- The second screen's device-key implementation creates its key in the Android
  Keystore on first use; a device or emulator without a usable Keystore reports
  that the key is unavailable.
