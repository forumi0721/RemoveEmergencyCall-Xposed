package kr.stonecold.removeemergencycall

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.removeemergencycall.hooks.*

@Suppress("unused")
class XposedInit : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam == null) {
            return
        }

        XposedBridge.log("Loaded App: " + lpparam.packageName)

        if (lpparam.packageName == "com.android.systemui") {
            HookSystemUi.handleLoadPackage(lpparam)
        }
    }
}
