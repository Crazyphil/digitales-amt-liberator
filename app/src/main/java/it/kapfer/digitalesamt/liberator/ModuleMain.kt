package it.kapfer.digitalesamt.liberator

import android.content.Context
import android.content.res.XModuleResources
import android.os.Build
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

// Classes used by multiple apps
const val ROOTBEER_CLASS: String = "com.scottyab.rootbeer.RootBeer"
const val KEYGENPARAMETERSPEC_CLASS: String = "android.security.keystore.KeyGenParameterSpec.Builder"
// Classes to hook in Digitales Amt app
const val DEVICE_INTEGRITY_CHECK_CLASS: String = "at.asitplus.utils.deviceintegrity.DeviceIntegrityCheck"
// Classes to hook in FON [+] app
const val ROOTBEER_CLASS_FIO: String = "com.scottyab.rootbeer.b"
const val ATTESTATION_HELPER_CLASS: String = "at.gv.bmf.bmf2go.tools.utils.AttestationHelper"
// Classes to hook in edu.digicard
const val HOMEFRAGMENT_CLASS: String = "at.asitplus.digitalid.wallet.homescreen.HomeFragment\$Companion"
// Classes to hook in mObywatel
const val ROOTBEERNATIVE_CLASS_MOBYWATEL: String = "com.scottyab.rootbeer.RootBeerNative"
const val ROOTCHECKS_MOBYWATEL: String = "fe.b"
const val MORE_ROOTCHECKS_MOBYWATEL: String = "c5.a"

class ModuleMain : IXposedHookZygoteInit, IXposedHookLoadPackage {
    private lateinit var digitalesAmtPackageName: String
    private lateinit var bmf2GoPackageName: String
    private lateinit var eduDigicardPackageName: String
    private lateinit var serviceportalBundPackageName: String
    private lateinit var mobywatelPackageName: String

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        val moduleResources = XModuleResources.createInstance(startupParam.modulePath, null)
        digitalesAmtPackageName = moduleResources.getString(R.string.digitales_amt_package_name)
        bmf2GoPackageName = moduleResources.getString(R.string.bmf2go_package_name)
        eduDigicardPackageName = moduleResources.getString(R.string.edudigicard_package_name)
        serviceportalBundPackageName = moduleResources.getString(R.string.serviceportal_bund_package_name)
        mobywatelPackageName = moduleResources.getString(R.string.mobywatel_package_name)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            digitalesAmtPackageName -> handleASitPlusIntegrityCheck(lpparam)
            bmf2GoPackageName -> handleBmf2Go(lpparam)
            eduDigicardPackageName -> handleEduDigicard(lpparam)
            serviceportalBundPackageName -> handleASitPlusIntegrityCheck(lpparam)
            mobywatelPackageName -> handleMobywatel(lpparam)
        }
    }

    private fun getPackageVersion(lpparam: XC_LoadPackage.LoadPackageParam): Int {
        val apkPath = File(lpparam.appInfo.sourceDir)
        val packageParser = XposedHelpers.findClass("android.content.pm.PackageParser", lpparam.classLoader)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val packageLite = XposedHelpers.callStaticMethod(packageParser, "parsePackageLite", apkPath, 0)
            XposedHelpers.getIntField(packageLite, "versionCode")
        } else {
            val packageObject = XposedHelpers.callMethod(packageParser.newInstance(), "parsePackage", apkPath, 0)
            XposedHelpers.getIntField(packageObject, "mVersionCode")
        }
    }

    private fun handleASitPlusIntegrityCheck(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("Hooking DeviceIntegrityCheck")
        XposedHelpers.findAndHookMethod(DEVICE_INTEGRITY_CHECK_CLASS, lpparam.classLoader, "checkIntegrity", XC_MethodReplacement.DO_NOTHING)
        XposedHelpers.findAndHookMethod(DEVICE_INTEGRITY_CHECK_CLASS, lpparam.classLoader, "checkIntegrityForceCheck", XC_MethodReplacement.DO_NOTHING)
    }

    private fun handleBmf2Go(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (getPackageVersion(lpparam) < 161) {
            XposedBridge.log("Detected FON [+] version < 3.0.0")
            XposedBridge.log("Hooking RootBeer")
            // Hook RootBeer's obfuscated isRooted() method
            XposedHelpers.findAndHookMethod(ROOTBEER_CLASS_FIO, lpparam.classLoader, "n", XC_MethodReplacement.returnConstant(false))

            XposedBridge.log("Hooking AttestationHelper")
            // Hook method that checks whether hardware key attestation is supported
            XposedHelpers.findAndHookMethod(ATTESTATION_HELPER_CLASS, lpparam.classLoader, "b", XC_MethodReplacement.returnConstant(false))
            // Hook method that checks whether hardware key attestation returns officially signed results
            XposedHelpers.findAndHookMethod(ATTESTATION_HELPER_CLASS, lpparam.classLoader, "i", XC_MethodReplacement.returnConstant(true))
        }
        else {
            XposedBridge.log("Detected FON [+] version >= 3.0.0")
            XposedBridge.log("Hooking RootBeer")
            // Hook RootBeer's isRooted() method
            XposedHelpers.findAndHookMethod(ROOTBEER_CLASS, lpparam.classLoader, "isRootedWithoutBusyBoxCheck", XC_MethodReplacement.returnConstant(false))

            XposedBridge.log("Hooking KeyGenParameterSpec")
            XposedHelpers.findAndHookMethod(KEYGENPARAMETERSPEC_CLASS, lpparam.classLoader, "setAttestationChallenge", ByteArray::class.java, object :
                XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    param?.args?.set(0, null) // set the attestation bytes to null -> silently fails attestation
                }
            })
        }
    }

    private fun handleEduDigicard(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("Hooking RootBeer")
        // Hook RootBeer's isRooted() method
        XposedHelpers.findAndHookMethod(ROOTBEER_CLASS, lpparam.classLoader, "isRooted", XC_MethodReplacement.returnConstant(false))

        XposedBridge.log("Hooking HomeFragment")
        // Hook HomeFragment's getHasAttestationCapabilities() method
        XposedHelpers.findAndHookMethod(HOMEFRAGMENT_CLASS, lpparam.classLoader, "getHasAttestationCapabilities", XC_MethodReplacement.returnConstant(true))

        /*
        // TODO: find out how attestation in edu.digicard works
        XposedBridge.log("Hooking KeyGenParameterSpec")
        XposedHelpers.findAndHookMethod(KEYGENPARAMETERSPEC_CLASS, lpparam.classLoader, "setAttestationChallenge", ByteArray::class.java, object :
            XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                param?.args?.set(0, null) // set the attestation bytes to null -> silently fails attestation
            }
        })
        */
    }

    private fun handleMobywatel(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("Hooking RootBeer")
        //Hook RootBeerNative
        XposedHelpers.findAndHookMethod(ROOTBEERNATIVE_CLASS_MOBYWATEL, lpparam.classLoader, "a", XC_MethodReplacement.returnConstant(false))

        XposedBridge.log("Hooking all the obfuscated root checks")
        //Hook PackageManager check
        XposedHelpers.findAndHookMethod(ROOTCHECKS_MOBYWATEL, lpparam.classLoader, "r", List::class.java, XC_MethodReplacement.returnConstant(false))
        //Hook path existence check
        XposedHelpers.findAndHookMethod(ROOTCHECKS_MOBYWATEL, lpparam.classLoader, "b", String::class.java, XC_MethodReplacement.returnConstant(false))
        //Hook ro.debuggable and ro.secure check
        XposedHelpers.findAndHookMethod(ROOTCHECKS_MOBYWATEL, lpparam.classLoader, "d", XC_MethodReplacement.returnConstant(false))
        //Hook mount rw check
        XposedHelpers.findAndHookMethod(ROOTCHECKS_MOBYWATEL, lpparam.classLoader, "g", XC_MethodReplacement.returnConstant(false))
        //Hook RootBeer check
        XposedHelpers.findAndHookMethod(ROOTCHECKS_MOBYWATEL, lpparam.classLoader, "h", XC_MethodReplacement.returnConstant(false))
        //Hook `which su` check
        XposedHelpers.findAndHookMethod(ROOTCHECKS_MOBYWATEL, lpparam.classLoader, "j", XC_MethodReplacement.returnConstant(false))
        //Hook test-key build tag check
        XposedHelpers.findAndHookMethod(ROOTCHECKS_MOBYWATEL, lpparam.classLoader, "q", XC_MethodReplacement.returnConstant(false))
        //Hook PackageManager check
        XposedHelpers.findAndHookMethod(MORE_ROOTCHECKS_MOBYWATEL, lpparam.classLoader, "h", Context::class.java, String::class.java, XC_MethodReplacement.returnConstant(false))
    }
}