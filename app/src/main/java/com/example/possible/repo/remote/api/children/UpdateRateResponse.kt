package com.example.possible.repo.remote.api.children

data class UpdateRateResponse(
    val id: Int,
    val name: String,
    val age: Int,
    val gender: Int,
    val image: String,
    val readingRate: Int?,
    val writingRate: Int?,
    val lastReadingTime: String?,
    val lastWritingTime: String?,
    val readingDays: Int?,
    val writingDays: Int?,
    val difficult: String,
    val parentId: String,
    val parent: Any?,
    val testChildrens: Any?

)

