package com.gengee.insaitlib.callback

interface DataCallback<T>{
    fun onComplete(data: T?, errorMsg: String?)
}