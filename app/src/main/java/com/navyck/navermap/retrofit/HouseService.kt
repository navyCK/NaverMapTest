package com.navyck.navermap.retrofit

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/8fb6e2aa-624d-4c5e-9a37-5fe66c24e666")
    fun getHouseList(): Call<HouseDto>
}