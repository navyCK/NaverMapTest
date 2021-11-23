package com.navyck.navermap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import com.navyck.navermap.retrofit.HouseDto
import com.navyck.navermap.retrofit.HouseService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    private val mapView: MapView by lazy { findViewById(R.id.mapView)}

    private val currentLocationButton: LocationButtonView by lazy { findViewById(R.id.currentLocationButton) }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun getHouseListFromAPI() {
        val retrofit = Retrofit.Builder()
                .baseUrl("http://openapi.kepco.co.kr/service/EvInfoServiceV2/getEvSearchList")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()

        retrofit.create(HouseService::class.java).also {
            it.getHouseList()
                    .enqueue(object : Callback<HouseDto> {
                        override fun onResponse(call: Call<HouseDto>, response: Response<HouseDto>) {
                            if (response.isSuccessful.not()) {
                                Log.d("Retrofit", "실패1")
                                return
                            }


                        }

                        override fun onFailure(call: Call<HouseDto>, t: Throwable) {
                            TODO("Not yet implemented")
                        }
                    })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE)
            return

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        // 줌 범위 설정
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

        // 지도 위치 이동
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.497801, 127.027591))
        naverMap.moveCamera(cameraUpdate)

        // 현위치 버튼 기능
        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false // 뷰 페이져에 가려져 이후 레이아웃에 정의 하였음.

        currentLocationButton.map = naverMap // 이후 정의한 현위치 버튼에 네이버맵 연결

        // -> onRequestPermissionsResult // 위치 권한 요청
        locationSource =
                FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

//        // 지도 다 로드 이후에 가져오기
//        getHouseListFromAPI()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

//    private fun getHouseListFromAPI() {
//        TODO("Not yet implemented")
//    }

}