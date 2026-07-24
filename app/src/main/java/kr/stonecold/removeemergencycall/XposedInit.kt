package kr.stonecold.removeemergencycall

import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import kr.stonecold.removeemergencycall.common.XposedUtil
import kr.stonecold.removeemergencycall.hooks.*

@Suppress("unused")
class XposedInit : XposedModule() {
    private companion object {
        const val TAG = "XposedInit"
    }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        XposedUtil.attach(this)

        XposedUtil.xposedInfo(TAG, "Module loaded in process: ${param.processName}")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        XposedUtil.attach(this)

        XposedUtil.xposedInfo(TAG, "Package ready: ${param.packageName}")

        if (param.packageName == "com.android.systemui") {
            HookSystemUi.handlePackageReady(param, this)
        }
    }
}
