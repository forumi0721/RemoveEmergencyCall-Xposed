package kr.stonecold.removeemergencycall.hooks

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import kr.stonecold.removeemergencycall.common.XposedUtil

object HookSystemUi {
    private const val TAG = "HookSystemUi"

    fun handlePackageReady(param: PackageReadyParam, xposed: XposedInterface) {
        if (param.packageName == "com.android.systemui") {
            hookEmergencyButton(param, xposed)
        }
    }

    @SuppressLint("PrivateApi")
    private fun hookEmergencyButton(param: PackageReadyParam, xposed: XposedInterface) {
        val className = "com.android.keyguard.EmergencyButton"
        val emergencyButtonClass = runCatching {
            Class.forName(className, false, param.classLoader)
        }.getOrElse {
            XposedUtil.xposedInfo(TAG, "Skipping $className: class not found")
            return
        }

        hookConstructorIfExists(
            xposed,
            emergencyButtonClass,
            Context::class.java
        )
        hookConstructorIfExists(
            xposed,
            emergencyButtonClass,
            Context::class.java,
            AttributeSet::class.java
        )
        hookOnFinishInflateIfExists(xposed, emergencyButtonClass)
    }

    private fun hookConstructorIfExists(
        xposed: XposedInterface,
        clazz: Class<*>,
        vararg parameterTypes: Class<*>
    ) {
        val constructor = runCatching {
            clazz.getDeclaredConstructor(*parameterTypes).apply {
                isAccessible = true
            }
        }.getOrElse {
            XposedUtil.xposedInfo(TAG, "Skipping ${clazz.name} constructor: method not found")
            return
        }

        xposed.hook(constructor).intercept { chain ->
            val result = chain.proceed()
            (chain.thisObject as? View)?.deactivate()
            result
        }

        XposedUtil.xposedInfo(TAG, "Hooked ${clazz.name} constructor.")
    }

    private fun hookOnFinishInflateIfExists(xposed: XposedInterface, clazz: Class<*>) {
        val methodName = "onFinishInflate"
        val method = runCatching {
            clazz.getDeclaredMethod(methodName).apply {
                isAccessible = true
            }
        }.getOrElse {
            XposedUtil.xposedInfo(TAG, "Skipping ${clazz.name}.$methodName: method not found")
            return
        }

        xposed.hook(method).intercept { chain ->
            val result = chain.proceed()
            val view = chain.thisObject as? View
            view?.deactivate()
            view?.post {
                view.layoutParams?.let { layoutParams ->
                    layoutParams.width = 0
                    layoutParams.height = 0
                    view.layoutParams = layoutParams
                }
            }
            result
        }

        XposedUtil.xposedInfo(TAG, "Hooked ${clazz.name}.$methodName.")
    }

    private fun View.deactivate() {
        visibility = View.GONE
        alpha = 0f
        isClickable = false
        isFocusable = false
        isEnabled = false
        scaleX = 0f
        scaleY = 0f
    }
}
