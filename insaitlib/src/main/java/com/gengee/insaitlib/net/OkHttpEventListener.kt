package com.gengee.insaitlib.net

import com.blankj.utilcode.util.LogUtils
import okhttp3.*
import okhttp3.EventListener.Factory
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy


class OkHttpEventListener : EventListener() {

    override fun dnsStart(call: Call, domainName: String) {
        super.dnsStart(call, domainName)
        LogUtils.e("dnsStart: $domainName")
    }

    override fun dnsEnd(
        call: Call,
        domainName: String,
        inetAddressList: List<@JvmSuppressWildcards InetAddress>
    ) {
        super.dnsEnd(call, domainName, inetAddressList)
        LogUtils.e("dnsEnd: $domainName")
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        super.connectStart(call, inetSocketAddress, proxy)
        LogUtils.e("connectStart: ")
    }

    override fun connectEnd(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?
    ) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol)
        LogUtils.e("connectEnd: ")
    }

    override fun secureConnectStart(call: Call) {
        super.secureConnectStart(call)
        LogUtils.e("secureConnectStart: ")
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        super.secureConnectEnd(call, handshake)
        LogUtils.e("secureConnectEnd: ")
    }

    override fun requestHeadersStart(call: Call) {
        super.requestHeadersStart(call)
        LogUtils.e("requestHeadersStart: ")
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        super.requestHeadersEnd(call, request)
        LogUtils.e("requestHeadersEnd: ")
    }

    override fun requestBodyStart(call: Call) {
        super.requestBodyStart(call)
        LogUtils.e("requestBodyStart: ")
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        super.requestBodyEnd(call, byteCount)
        LogUtils.e("requestBodyEnd: byteCount -> $byteCount")
    }

    override fun responseBodyStart(call: Call) {
        super.responseBodyStart(call)
        LogUtils.e("responseBodyStart: ")
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        super.responseBodyEnd(call, byteCount)
        LogUtils.e("responseBodyEnd: byteCount -> $byteCount")
    }

    override fun callEnd(call: Call) {
        super.callEnd(call)
        LogUtils.e("callEnd: success")
    }

    override fun callFailed(call: Call, ioe: IOException) {
        super.callFailed(call, ioe)
        LogUtils.e("callFailed: fail")
    }

    companion object {
        val FACTORY = object : Factory {
            override fun create(call: Call): EventListener {
                return OkHttpEventListener()
            }
        }
    }
}