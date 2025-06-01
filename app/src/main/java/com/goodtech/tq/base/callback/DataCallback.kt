package com.goodtech.tq.base.callback

interface DataCallback<T>{
    fun onComplete(data: T?, errorMsg: String?)
}