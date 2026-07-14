package com.chunjing.tq.db

import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.nio.charset.StandardCharsets

/**
 * 天气/业务缓存编解码：新写 JSON，读时兼容旧 Java 序列化。
 * 无 Android 依赖，便于 JVM 单测。
 */
object CacheCodec {
    private const val JSON_PREFIX = "JSON1|"
    private val gson = Gson()

    private data class JsonCacheEnvelope(
        val type: String,
        val payload: String,
    )

    fun encode(body: Any): ByteArray {
        val typeName = body::class.java.name
        val payload = gson.toJson(body)
        val envelope = JsonCacheEnvelope(typeName, payload)
        return (JSON_PREFIX + gson.toJson(envelope)).toByteArray(StandardCharsets.UTF_8)
    }

    fun decode(data: ByteArray): Any? {
        return try {
            val text = String(data, StandardCharsets.UTF_8)
            if (text.startsWith(JSON_PREFIX)) {
                val envelope = gson.fromJson(
                    text.removePrefix(JSON_PREFIX),
                    JsonCacheEnvelope::class.java
                )
                val clazz = Class.forName(envelope.type)
                gson.fromJson(envelope.payload, clazz)
            } else {
                decodeJavaSerialized(data)
            }
        } catch (_: Exception) {
            try {
                decodeJavaSerialized(data)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun isJsonEncoded(data: ByteArray): Boolean {
        return try {
            String(data, StandardCharsets.UTF_8).startsWith(JSON_PREFIX)
        } catch (_: Exception) {
            false
        }
    }

    private fun decodeJavaSerialized(data: ByteArray): Any? {
        val bais = ByteArrayInputStream(data)
        val ois = ObjectInputStream(bais)
        val readObject = ois.readObject()
        ois.close()
        return readObject
    }
}
