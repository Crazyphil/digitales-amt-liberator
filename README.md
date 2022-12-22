# Digitales Amt Liberator

This repository contains an [Xposed](https://github.com/rovo89/XposedBridge) module that removes root and bootloader checks from the
[Digitales Amt](https://play.google.com/store/apps/details?id=at.gv.oe.app) app by the Bundesministerium für Digitalisierung und Wirtschaftsort.

## Mission statement

### The problem

When your Android phone is rooted or its bootloader unlocked, the Digitales Amt app starts, but it doesn't allow users to connect their ID Austria to it (for
"security reasons"). This makes the e-govnerment app basically useless.

To check for rooted devices, it utilizes the [RootBeer](https://github.com/scottyab/rootbeer) library. This check can easily be circumvented, for example with
the [Universal SafetyNet Fix](https://github.com/kdrag0n/safetynet-fix) module for [Magisk](https://github.com/topjohnwu/Magisk). However, to test for an
unlocked bootloader, it checks whether the certificate chain for [Key Attestation](https://developer.android.com/training/articles/security-key-attestation) is
trusted. The result of this check cannot be faked in software because of the cryptography used.

### The solution

*Digitales Amt Liberator* removes any calls in the Digitales Amt app to root and bootloader checks and therefore provides a tailored solution to running the
app on rooted devices.

## Installation

Install and activate Xposed or one of its derivatives. One of the more modern variants is [LSPosed](https://github.com/LSPosed/LSPosed#install). Ensure that
loading Xposed modules for the Digitales Amt app is activated in the Xposed settings.

Download the `app-release.apk` file from the latest [Digitales Amt Liberator release](https://github.com/Crazyphil/digitales-amt-liberator/releases/latest) and
install it on your device. After completion, you should be automatically prompted to activate the new Xposed module.

Restart your device and ensure that the module is up and running in your Xposed settings.

You're done. Beginning with the next start of the Digitales Amt app, you are able to connect your ID Austria to the app and use it for e-government stuff.

## Development

The module is written in Kotlin. It only consists of one class, `it.kapfer.digitalesamt.liberator.ModuleMain`, which hooks loading Digitales Amt's package
loading process and replaces the calls to the root detection methods in the class responsible for this
(`at.asitplus.utils.deviceintegrity.DeviceIntegrityCheck`) to always indicate that the device is unmodified.

Build it using Gradle.