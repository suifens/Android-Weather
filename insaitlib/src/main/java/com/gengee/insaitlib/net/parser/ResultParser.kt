package com.gengee.insaitlib.net.parser

import java.lang.reflect.Type

interface ResultParser<T> {
    fun parse(json: String): T
}