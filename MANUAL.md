# Crypto lab — run steps

This lab has a single branch; there is no challenge/solution pair to switch
between.

## Prerequisites

- Android Studio Rabbit (2026.2) or newer, with Android SDK platform 37 and
  build-tools 37.0.0
- JDK 17 or newer; Android Studio's bundled JBR works
- A device or emulator running Android 11 (API 30) or newer
- Flow 5 needs a device screen lock and an enrolled strong biometric. The
  other implemented cases can run without biometric setup. See below if setup
  is already present or is completed after first opening the app.

## Build and install

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Run

Launch **Crypto Lab**. Select the exercise using the numbered rail on the left.
Enter a message, press **Encrypt**, then press **Decrypt** to display the result.
Follow the instructor's directions for the exercise.

## Flow 5: biometric setup and use

Flow 5 uses Android's **strong biometric (Class 3)** capability. An existing
eligible fingerprint or other strong biometric can be used; the app does not
require a particular finger, emulator profile or numeric fingerprint ID.
A face-unlock option is sufficient only when Android classifies it as strong.
For an emulator, use an AVD with a supported biometric sensor; the fingerprint
procedure below is the reference setup.

| Setup at the app's first startup | What to do |
| --- | --- |
| Screen lock and a strong biometric are already configured | Keep that setup. Open flow 5 and authenticate using an already enrolled identity. On an emulator, select the same simulated profile that was used for its enrollment. |
| No biometric is enrolled yet | Complete the setup steps below. If Crypto Lab has already been opened, force-stop and reopen it afterward. |
| Only a weak biometric is configured, or no supported sensor is available | Configure an available strong biometric, or use a suitable AVD/device. A PIN alone does not satisfy flow 5. Force-stop and reopen the app after setup. |

### Set up a simulated fingerprint

1. Inside the virtual Android device, open **Settings** and search for
   **Fingerprint** or **Biometrics**. Start fingerprint enrollment. Set or
   confirm the device's PIN, pattern or password when Android requests it;
   there is no lab-wide PIN to enter.
2. In the emulator window, open **More / ⋮ → Extended controls → Fingerprint**.
   If the controls are unavailable in Studio's embedded emulator, open the
   emulator in its separate window.
3. Choose a simulated profile, for example **Finger 1**. Keep that same profile
   selected throughout enrollment. When Android asks for a sensor touch,
   click and release **Touch Sensor**. Repeat until Android reports that the
   fingerprint was added, then finish setup.
4. Remember the selected emulator profile: it is the one to use at the app's
   later authentication prompts. Android's enrolled-fingerprint display name
   is not a reliable way to infer an emulator profile or console ID. Do not
   mix GUI profiles with arbitrary console IDs; GUI **Finger 1** does not mean
   console fingerprint ID `1`.
5. If Crypto Lab was already running, restart its process as described below.

If another biometric simulation is available on the chosen AVD, use that
AVD's enrollment and simulation controls consistently. It must still provide
Android's strong biometric capability. The fingerprint walkthrough does not
imply that every AVD supports simulated face recognition.

### Restart after completing setup

Use Android **Settings → Apps → Crypto Lab → Force stop**, then launch the app
again. Returning Home or merely closing its screen is not a process restart.
This step does not clear app storage.

Alternatively, with the device connected through ADB:

```sh
adb shell am force-stop defdev.labdroid.crypto
adb shell am start -n defdev.labdroid.crypto/.MainActivity
```

With multiple devices, select the intended one using `adb -s SERIAL` for each
command; `adb devices -l` lists their serials.

### Encrypt and decrypt

Select case **5**, enter a message and press **Encrypt**. When Android displays
the authentication sheet, use an enrolled biometric. On the reference
emulator setup, select the profile used during enrollment and click/release
**Touch Sensor**. After ciphertext appears, press **Decrypt** and authenticate
again. Each operation requires its own biometric authorization.

The device PIN is used for Android setup/unlocking; it is not a substitute at
this biometric sheet. **Cancel** returns to the form, where the operation can
be tried again.

### If an operation cannot proceed

| Outcome | Action |
| --- | --- |
| Not recognized | Use a profile that was actually enrolled on this AVD. Check the profile selection before another attempt; do not add a new fingerprint merely to guess the matching profile. If the existing profile is unknown, ask the instructor about the supplied AVD setup. |
| Setup instruction remains after enrollment | Force-stop and reopen Crypto Lab using the steps above. |
| Strong biometric unavailable | Check that the chosen device/AVD has an enrolled strong biometric. A weak face-unlock option or a screen-lock PIN alone is insufficient. |
| Biometric lockout | Unlock Android with its screen-lock credential and retry biometric authentication when Android allows it. Repeatedly pressing Touch Sensor will not bypass a lockout. |
| The encryption key is unavailable | Restart once. If the message remains, ask the instructor before resetting the lab, particularly if biometrics or the screen lock changed after the app had already been used. |

Keep the biometric setup unchanged during the exercise. If the instructor
directs a fresh lab start, Android **Settings → Apps → Crypto Lab → Storage &
cache → Clear storage** removes this app's local data and device-generated keys. Previously
produced ciphertext may then be unusable; this affects other cases' local
state too. Complete biometric setup before reopening the app. Ordinary
`adb install -r` updates preserve app data and are not a storage reset.

Platform references: [emulator extended controls](https://developer.android.com/studio/run/emulator-extended-controls)
and [Android biometric capability](https://developer.android.com/identity/sign-in/biometric-auth).
