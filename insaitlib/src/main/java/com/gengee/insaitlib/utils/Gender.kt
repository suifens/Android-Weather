package com.gengee.insaitlib.utils

import com.gengee.insaitlib.R

enum class Gender(val tagId: String, val labelId: Int) {
    UNKNOWN("Not specified", R.string.gender_unknown),
    MALE("Male", R.string.gender_male),
    FEMALE("Female", R.string.gender_female);
}