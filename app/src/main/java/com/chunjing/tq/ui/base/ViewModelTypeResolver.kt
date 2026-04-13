package com.chunjing.tq.ui.base

import androidx.lifecycle.ViewModel
import java.lang.reflect.ParameterizedType
import kotlin.reflect.jvm.jvmErasure

/**
 * 解析「直接继承」某带两个泛型参数的基类时，第二个类型实参（ViewModel 的 Class）。
 *
 * Kotlin 子类在 JVM 上 [Class.getGenericSuperclass] 有时为原始 Class 而非 [ParameterizedType]，
 * 此时通过 [kotlin.reflect.KClass.supertypes] 读取。
 */
object ViewModelTypeResolver {

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <V : ViewModel> resolve(owner: Any, baseRaw: Class<*>): Class<V> {
        val gs = owner.javaClass.genericSuperclass
        if (gs is ParameterizedType && gs.rawType == baseRaw) {
            val vmArg = gs.actualTypeArguments[1]
            require(vmArg is Class<*>) { "ViewModel type argument must be a non-generic class" }
            return vmArg as Class<V>
        }
        val baseK = baseRaw.kotlin
        for (supertype in owner.javaClass.kotlin.supertypes) {
            if (supertype.classifier != baseK) continue
            val vmType = supertype.arguments.getOrNull(1)?.type
                ?: error("${owner.javaClass.name}：${baseRaw.simpleName}<> 缺少第二个类型参数")
            return vmType.jvmErasure.java as Class<V>
        }
        error("无法解析 ${owner.javaClass.name} 的 ViewModel 类型（基类应为 ${baseRaw.name}<Binding, ViewModel>）")
    }
}
