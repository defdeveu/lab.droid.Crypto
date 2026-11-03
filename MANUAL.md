# Crypto lab — run steps

This lab has a single branch; there is no challenge/solution pair to switch
between.

## Prerequisites

- Android Studio Rabbit (2026.2) or newer, with Android SDK platform 37 and
  build-tools 37.0.0
- JDK 17 or newer; Android Studio's bundled JBR works
- A device or emulator running Android 7.0 (API 24) or newer

## Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Run

Launch **Crypto Lab**. Select the exercise using the numbered rail on the left.
Enter a message, press **Encrypt**, then press **Decrypt** to display the result.
Follow the instructor's directions for the exercise.
