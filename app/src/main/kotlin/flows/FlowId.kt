package defdev.labs.droid.crypto.flows

enum class FlowId(val value: String) {
    HARDCODED("hardcoded"),
    RESOURCE("resource"),
    HTTPS("https"),
    KEYSTORE("keystore"),
    BIOMETRIC("biometric"),
}
