package it.kapfer.digitalesamt.liberator

import android.content.res.XModuleResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

const val DEVICE_INTEGRITY_CHECK_CLASS: String = "at.asitplus.utils.deviceintegrity.DeviceIntegrityCheck"

class ModuleMain : IXposedHookZygoteInit, IXposedHookLoadPackage {
    private lateinit var digitalesAmtPackageName: String

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        val moduleResources = XModuleResources.createInstance(startupParam.modulePath, null)
        digitalesAmtPackageName = moduleResources.getString(R.string.digitales_amt_package_name)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (digitalesAmtPackageName != lpparam.packageName) {
            return
        }

        XposedBridge.log("Hooking DeviceIntegrityCheck")
        XposedHelpers.findAndHookMethod(DEVICE_INTEGRITY_CHECK_CLASS, lpparam.classLoader, "checkIntegrity", XC_MethodReplacement.DO_NOTHING)
        XposedHelpers.findAndHookMethod(DEVICE_INTEGRITY_CHECK_CLASS, lpparam.classLoader, "checkIntegrityForceCheck", XC_MethodReplacement.DO_NOTHING)
    }
}