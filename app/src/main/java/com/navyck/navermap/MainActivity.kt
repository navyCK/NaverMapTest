package com.navyck.navermap

import HouseListAdapter
import HouseViewPagerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import com.navyck.navermap.retrofit.HouseDto
import com.navyck.navermap.retrofit.HouseModel
import com.navyck.navermap.retrofit.HouseService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    private val mapView: MapView by lazy { findViewById(R.id.mapView)}

    private val viewPager: ViewPager2 by lazy { findViewById(R.id.houseViewPager) }
    private val viewPagerAdapter = HouseViewPagerAdapter(itemClicked = {
        onHouseModelClicked(houseModel = it)
    })

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }
    private val recyclerViewAdapter = HouseListAdapter()

    private fun initHouseRecyclerView() {
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun onHouseModelClicked(houseModel: HouseModel) {
//        val intent = Intent()
//                .apply {
//                    action = Intent.ACTION_SEND
//                    putExtra(
//                            Intent.EXTRA_TEXT,
//                            "[?????? ??? ????????? ???????????????!!]\n" +
//                                    "?????? ?????? : ${houseModel.title}\n" +
//                                    "?????? ?????? : ${houseModel.price}\n" +
//                                    "?????? ?????? : ${houseModel.imgUrl}",
//                    )
//                    type = "text/plain"
//                }
//        startActivity(Intent.createChooser(intent, null))

        val intent = Intent(this, DetailActivity::class.java)
        startActivity(intent)

    }


    private val currentLocationButton: LocationButtonView by lazy { findViewById(R.id.currentLocationButton) }
    private val bottomSheetTitleTextView: TextView by lazy { findViewById(R.id.bottomSheetTitleTextView) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView.getMapAsync(this)
        initHouseViewPager()
        initHouseRecyclerView()
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

    private fun initHouseViewPager() {
        viewPager.adapter = viewPagerAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val selectedHouseModel = viewPagerAdapter.currentList[position]
                val cameraUpdate =
                        CameraUpdate.scrollTo(LatLng(selectedHouseModel.lat, selectedHouseModel.lng))
                                .animate(CameraAnimation.Easing)
                naverMap.moveCamera(cameraUpdate)
            }
        })
    }

    private fun getHouseListFromAPI() {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://run.mocky.io/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()

        retrofit.create(HouseService::class.java).also {
            it.getHouseList()
                    .enqueue(object : Callback<HouseDto> {
                        @SuppressLint("SetTextI18n")
                        override fun onResponse(call: Call<HouseDto>, response: Response<HouseDto>) {
                            if (response.isSuccessful.not()) {
                                Log.d("Retrofit", "??????1")
                                return
                            }
                            response.body()?.let { dto ->
                                updateMarker(dto.items)
                                viewPagerAdapter.submitList(dto.items)
                                recyclerViewAdapter.submitList(dto.items)
                                bottomSheetTitleTextView.text = "${dto.items.size}?????? ??????"
                            }
                        }

                        override fun onFailure(call: Call<HouseDto>, t: Throwable) {
                            Log.d("Retrofit", "??????2")
                            Log.d("Retrofit", t.stackTraceToString())
                        }
                    })
        }
    }

    private fun updateMarker(houses: List<HouseModel>) {
        houses.forEach { house ->
            val marker = Marker()
            marker.position = LatLng(house.lat, house.lng)
            marker.onClickListener = this
            marker.map = naverMap
            marker.tag = house.id
//            marker.icon = MarkerIcons.BLACK
//            marker.iconTintColor = Color.RED
//            marker.width = 50
//            marker.height = 60
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

        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.497801, 127.027591))
        naverMap.moveCamera(cameraUpdate)

        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false

        currentLocationButton.map = naverMap

        locationSource =
                FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        getHouseListFromAPI()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onClick(overlay: Overlay): Boolean {
        val selectedModel = viewPagerAdapter.currentList.firstOrNull {
            it.id == overlay.tag
        }
        selectedModel?.let {
            val position = viewPagerAdapter.currentList.indexOf(it)
            viewPager.currentItem = position
        }
        return true
    }

}