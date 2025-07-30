package com.gengee.insaitlib.net

import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gengee.insaitlib.net.exception.ExceptionUtils
import com.gengee.insaitlib.net.exception.RequestException
import com.gengee.insaitlib.net.parser.FengYunParser
import com.gengee.insaitlib.net.parser.HeFengParser
import com.gengee.insaitlib.net.parser.ResultParser
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object HttpUtils {

    public var BASE_URL: String? = null

    private var cer: String? = null

    init {
//        BASE_URL = Constant.URL_OFFICIAL_SSL
//        cer = "gitstar.cer"
    }

    @JvmStatic
    fun getBaseUrl(): String? {
        return BASE_URL
    }

    suspend inline fun <reified T> get(
        url: String,
        param: HashMap<String, Any>? = null,
        headers: HashMap<String, String>? = null,
        useCache: Boolean = false,
        noinline error: ((String) -> Unit)? = null
    ): T? {
        val type = object : TypeToken<T>() {}.type
        return get(url, param, type, headers, useCache, error)
    }

    suspend fun <T> get(
        url: String,
        param: HashMap<String, Any>? = null,
        returnType: Type,
        headers: HashMap<String, String>? = null,
        useCache: Boolean = false,
        error: ((String) -> Unit)? = null
    ): T? {
        val newUrl = if (url.startsWith("http")) url else BASE_URL + url
        val urlBuilder = newUrl.toHttpUrlOrNull()!!.newBuilder()
        param?.let {
            it.keys.forEach { key ->
                urlBuilder.addQueryParameter(key, it[key].toString())
            }
        }
        return execRequest(
            "GET",
            urlBuilder.build(),
            headers,
            null,
            HeFengParser<T>(returnType),
            useCache,
            error
        )
    }

    suspend inline fun <reified T> post(
        url: String,
        param: HashMap<String, Any>? = null,
        headers: HashMap<String, String>? = null, noinline error: ((String) -> Unit)? = null
    ): T? {
        val type = object : TypeToken<T>() {}.type

        return post(url, param, type, headers, error)
    }

    suspend inline fun <reified T> post(
        url: String,
        param: Any? = null,
        headers: HashMap<String, String>? = null, noinline error: ((String) -> Unit)? = null
    ): T? {
        val type = object : TypeToken<T>() {}.type

        return post(url, param, type, headers, error)
    }

    suspend fun <T> post(
        url: String,
        param: Any? = null,
        returnType: Type,
        headers: HashMap<String, String>? = null, error: ((String) -> Unit)? = null
    ): T? {
        val newUrl = if (url.startsWith("http")) url else BASE_URL + url
        val urlBuilder = newUrl.toHttpUrlOrNull()!!.newBuilder()

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            Gson().toJson(param)
        )

        return execRequest(
            "POST",
            urlBuilder.build(),
            headers,
            requestBody,
            FengYunParser<T>(returnType),
            error = error
        )
    }

    suspend fun <T> execRequest(
        method: String,
        httpUrl: HttpUrl,
        headers: HashMap<String, String>? = null,
        requestBody: RequestBody?,
        parser: ResultParser<T>,
        useCache: Boolean = false,
        error: ((String) -> Unit)? = null
    ): T? {
        val request = Request.Builder().url(httpUrl).method(method, requestBody)
        headers?.keys?.forEach {
            headers[it]?.let { it1 -> request.addHeader(it, it1) }
        }
        if (useCache) {
            request.cacheControl(
                CacheControl.Builder()
                    .maxAge(60, TimeUnit.SECONDS)
                    .maxStale(60, TimeUnit.SECONDS)
                    .build()
            )
        }
        try {
            OkHttpUtils.getClient().newCall(request.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RequestException("请求异常：${response.code}")
                }
                val body = response.body?.string() ?: throw RequestException("数据为空")

                val jsonObject = JSONObject(body)
                val code: String = if (jsonObject.has("error_code")) {
                    jsonObject.get("error_code").toString()
                } else if (jsonObject.has("code")) {
                    jsonObject.get("code").toString()
                } else {
                    "0"
                }

                when (code) {
                    "200", "0" -> {
                        return parser.parse(body)
                        /*return if (flag == 0) {
                            Gson().fromJson(body, returnType)
                        } else {
                            val data = jsonObject.get("result").toString()
                            Gson().fromJson(data, returnType)
                        }*/
                    }
                    "201" -> {
                        return null
                    }
                    "401" -> {
                        throw RequestException("请求异常异常：401 鉴权错误，请确认key是否正确", "401")
                    }
                    else -> {
                        throw RequestException("请求异常异常, code: $code", "$code")
                    }
                }
            }
        } catch (e: Throwable) {
            if (error != null && e is RequestException) {
                error(ExceptionUtils.parseException(e))
                return null
            } else {
                throw e
            }
        }
    }
}