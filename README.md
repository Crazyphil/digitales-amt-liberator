# E-Government Liberator (formerly Digitales Amt Liberator)

This repository contains an [Xposed](https://github.com/rovo89/XposedBridge) module that removes root and bootloader checks from Austrian e-government apps.
These are

* [Digitales Amt](https://play.google.com/store/apps/details?id=at.gv.oe.app) by Bundesministerium für Digitalisierung und Wirtschaftsort
* [edu.digicard](https://play.google.com/store/apps/details?id=at.asitplus.digitalid.wallet.pupilid) by BMBWF
* [FinanzOnline [+]](https://play.google.com/store/apps/details?id=at.gv.bmf.bmf2go) by Bundesministerium für Finanzen
* [SPB Serviceportal Bund](https://play.google.com/store/apps/details?id=at.gv.bka.serviceportal) by Bundeskanzleramt Oesterreich

As an additional bonus, this module also supports some other apps with those kinds of checks, but doesn't offer first-class support for them (i.e. if something
breaks, it won't be fixed until someone [creates an issue](https://github.com/Crazyphil/digitales-amt-liberator/issues/new/choose)). This is not because we are
mean,
but none of the authors use these apps and therefore will not notice for themselves when something goes wrong.

* [mObywatel](https://play.google.com/store/apps/details?id=pl.nask.mobywatel) by Ministerstwo Cyfryzacji 🇵🇱

### The app I use is not listed above

Adding support for apps typically isn't easy. The app has to be reverse-engineered to find all checks and override them so they don't do anything. Typically,
these apps are also security-critical (which is why they bother with rooted devices in the first place) and therefore heavily obfuscated, so reverse-engineering
is even more difficult. Getting an app to work with E-Government Liberator needs hours to weeks of work. Sometimes, if server-side checks are involved, it is
even impossible to get them to run on rooted devices.

This means that a developer has to actively benefit from including a new app to this module. This benefit usually comes directly from personally being able to
use the app afterwards, or indirectly from the fun of reverse-engineering it an learning something through the process. And then there's always the risk of the
app breaking again after each update, which potentially means going back to start.

We collect all apps requested by users under the
[`app-support` label](https://github.com/Crazyphil/digitales-amt-liberator/issues?q=is%3Aissue+is%3Aopen+label%3Aapp-support)
in the issues.\
If you are a developer and see yourself up to the challenge, feel free to try implementing support for an app. For information about development,
see the [Development](#development) section. Don't hesitate to write a comment in the issue if you need help.\
If you are a user of an unsupported app,
[create an issue](https://github.com/Crazyphil/digitales-amt-liberator/issues/new?assignees=&labels=app-support&projects=&template=app-support.md&title=Support+for+%5BName+of+the+app%5D).

## Mission statement

### The problem

When your Android phone is rooted or its bootloader unlocked, the app starts, but it doesn't allow users to connect their ID Austria to it (for
"security reasons"), or it already denies starting for the same reason. This makes the e-government app basically useless.

To check for rooted devices, it utilizes the [RootBeer](https://github.com/scottyab/rootbeer) library. This check can easily be circumvented, for example with
the [Play Integrity Fix](https://github.com/chiteroman/PlayIntegrityFix) module for [Magisk](https://github.com/topjohnwu/Magisk). However, to test for an
unlocked bootloader, it checks whether the certificate chain for [Key Attestation](https://developer.android.com/training/articles/security-key-attestation) is
trusted. The result of this check cannot be faked in software because of the cryptography used.

### The solution

*E-Government Liberator* removes any calls in supported Austrian e-government apps to root and bootloader checks and therefore provides a tailored solution to
running the app on rooted devices.

## Installation

Install and activate Xposed or one of its derivatives. One of the more modern variants is [LSPosed](https://github.com/JingMatrix/LSPosed#install). Ensure that
loading Xposed modules for the supported apps is activated in the Xposed settings, and the app is not blocklisted in Magisk, if you use that.

Download the `app-release.apk` file from the latest [E-Government Liberator release](https://github.com/Crazyphil/digitales-amt-liberator/releases/latest) and
install it on your device. After completion, you should be automatically prompted to activate the new Xposed module.

Restart your device and ensure that the module is up and running in your Xposed settings. Ensure that all Austrian e-government apps you want to liberate are
checked in the module's settings (tap on *E-Government Liberator* on the *Modules* tab)

You're done. Beginning with the next start of the app, you are able to connect your ID Austria to the app and use it for e-government stuff.

## Support

If this module doesn't work for you, first check whether you've followed the installation instructions to the point. Refer to
[this issue comment](https://github.com/Crazyphil/digitales-amt-liberator/issues/2#issuecomment-1447865040) if you'd like to have some screenshots to compare
with your configuration.

If it still doesn't work, check whether you recently installed an update for the app that isn't working now. If you've got an update and the app now is broken,
please [create an issue](https://github.com/Crazyphil/digitales-amt-liberator/issues/new/choose) and don't forget to mention the version of the app that now
breaks. In any other case, just create an issue giving as much detail about what problem you have and what you've already done to try fixing it, and we'll see
what we can do for you.

## Development

The module is written in Kotlin. It only consists of one class, `it.kapfer.digitalesamt.liberator.ModuleMain`, which hooks loading supported apps' package
loading process and replaces the calls to the root detection methods in the class responsible for this
(e.g. `at.asitplus.utils.deviceintegrity.DeviceIntegrityCheck`) to always indicate that the device is unmodified. It also contains some app version checks for
certain method hooks to ensure that both older and newer versions of the supported apps can be used.

Build it using Gradle.
