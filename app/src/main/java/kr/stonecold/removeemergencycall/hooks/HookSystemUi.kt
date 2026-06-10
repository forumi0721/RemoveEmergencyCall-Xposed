package kr.stonecold.removeemergencycall.hooks

import android.view.View

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.removeemergencycall.common.Util
import kr.stonecold.removeemergencycall.common.XposedUtil

object HookSystemUi {
    private var tag: String = this::class.java.simpleName

    private val methodExistenceMap = mutableMapOf<String, Boolean?>()

    fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam == null) {
            return
        }

        if (lpparam.packageName == "com.android.systemui") {
            hookSetVisibility(lpparam)
        }
    }

    private fun hookSetVisibility(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam == null) {
            return
        }

		XposedHelpers.findAndHookMethod(
			View::class.java,
			"setVisibility",
			Int::class.javaPrimitiveType!!,
			object : XC_MethodHook() {
				override fun beforeHookedMethod(param: MethodHookParam) {
					val view = param.thisObject as? View ?: return

					if (view.javaClass.name == "com.android.keyguard.EmergencyButton") {
						param.args[0] = View.GONE
					}
				}
			}
		)

		var className = "com.android.keyguard.EmergencyButton"
		var methodName = "setVisibility"

        XposedUtil.xposedDebug(tag, "Successfully registered $className.$methodName.")
    }
}
