package it.kapfer.digitalesamt.liberator

import android.content.res.XModuleResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

// Classes to hook in Digitales Amt app
const val DEVICE_INTEGRITY_CHECK_CLASS: String = "at.asitplus.utils.deviceintegrity.DeviceIntegrityCheck"
// Classes to hook in FON [+] app
const val ROOTBEER_CLASS: String = "com.scottyab.rootbeer.b"
const val ATTESTATION_HELPER_CLASS: String = "at.gv.bmf.bmf2go.tools.utils.AttestationHelper"

class ModuleMain : IXposedHookZygoteInit, IXposedHookLoadPackage {
    private lateinit var digitalesAmtPackageName: String
    private lateinit var bmf2GoPackageName: String

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        val moduleResources = XModuleResources.createInstance(startupParam.modulePath, null)
        digitalesAmtPackageName = moduleResources.getString(R.string.digitales_amt_package_name)
        bmf2GoPackageName = moduleResources.getString(R.string.bmf2go_package_name)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            digitalesAmtPackageName -> handleDigitalesAmt(lpparam)
            bmf2GoPackageName -> handleBmf2Go(lpparam)
        }
    }

    private fun handleDigitalesAmt(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("Hooking DeviceIntegrityCheck")
        XposedHelpers.findAndHookMethod(DEVICE_INTEGRITY_CHECK_CLASS, lpparam.classLoader, "checkIntegrity", XC_MethodReplacement.DO_NOTHING)
        XposedHelpers.findAndHookMethod(DEVICE_INTEGRITY_CHECK_CLASS, lpparam.classLoader, "checkIntegrityForceCheck", XC_MethodReplacement.DO_NOTHING)
    }

    private fun handleBmf2Go(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("Hooking RootBeer")
        // Hook RootBeer's isRooted() method
        XposedHelpers.findAndHookMethod(ROOTBEER_CLASS, lpparam.classLoader, "n", XC_MethodReplacement.returnConstant(false))

        XposedBridge.log("Hooking AttestationHelper")
        // Hook method that checks whether hardware key attestation is supported
        XposedHelpers.findAndHookMethod(ATTESTATION_HELPER_CLASS, lpparam.classLoader, "b", XC_MethodReplacement.returnConstant(false))
        // Hook method that checks whether hardware key attestation returns officially signed results
        XposedHelpers.findAndHookMethod(ATTESTATION_HELPER_CLASS, lpparam.classLoader, "i", XC_MethodReplacement.returnConstant(true))
    }
}