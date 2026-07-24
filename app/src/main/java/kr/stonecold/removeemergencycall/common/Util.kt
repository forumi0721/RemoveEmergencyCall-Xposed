package kr.stonecold.removeemergencycall.common

@Suppress("unused")
object Util {
    fun findDeclaredMethodIfExists(
        className: String,
        classLoader: ClassLoader,
        methodName: String,
        vararg parameterTypes: Class<*>
    ) = runCatching {
        Class.forName(className, false, classLoader)
            .getDeclaredMethod(methodName, *parameterTypes)
            .apply { isAccessible = true }
    }.getOrNull()
}
