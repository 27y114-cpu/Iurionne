package com.example.utils

import com.example.domain.models.Work
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiUtils {
    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val workAdapter = moshi.adapter(Work::class.java)

    fun workToJson(work: Work): String {
        return workAdapter.toJson(work)
    }

    fun jsonToWork(json: String): Work? {
        return try {
            workAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }
}
