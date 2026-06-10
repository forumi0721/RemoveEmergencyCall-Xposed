package kr.stonecold.removeemergencycall.common

@Suppress("unused")
object Util {
    fun buildMethodKey(className: String, methodName: String, vararg parameterTypes: Class<*>): String {
        val parameters = parameterTypes.joinToString(",") { it.name }
        return "$className.$methodName($parameters)"
    }

    fun doesClassExist(className: String, classLoader: ClassLoader): Boolean {
        return try {
            Class.forName(className, true, classLoader)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    fun doesMethodExist(className: String, classLoader: ClassLoader, methodName: String, vararg parameterTypes: Class<*>): Boolean {
        return try {
            val clazz = Class.forName(className, true, classLoader)
            clazz.getDeclaredMethod(methodName, *parameterTypes) // getDeclaredMethod 사용
            true
        } catch (e: ClassNotFoundException) {
            false
        } catch (e: NoSuchMethodException) {
            false
        }
    }
}
