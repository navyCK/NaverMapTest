package com.navyck.navermap.retrofit

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/6c14ab02-b757-4931-b3ba-2dd9e5765073")
    fun getHouseList(): Call<HouseDto>
}