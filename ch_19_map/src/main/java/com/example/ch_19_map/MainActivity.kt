package com.example.ch_19_map

import android.util.Log
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.*
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.model.Marker
import com.example.ch_19_map.ParkingItem
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    lateinit var providerClient: FusedLocationProviderClient
    lateinit var apiClient: GoogleApiClient
    var googleMap: GoogleMap? = null
    private val parkingDataMap = mutableMapOf<String, ParkingData>()
    private val realtimeInfoMap = mutableMapOf<String, ParkingStatusItem>()

    data class ParkingData(
        val name: String,
        val totalSpaces: String,
        val availableSpaces: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it.all { permission -> permission.value == true }) {
                apiClient.connect()
            } else {
                Toast.makeText(this, "권한 거부", Toast.LENGTH_SHORT).show()
            }
        }
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)!!.getMapAsync(this)

        providerClient = LocationServices.getFusedLocationProviderClient(this)
        apiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(p0: Bundle?) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        === PackageManager.PERMISSION_GRANTED
                    ) {
                        providerClient.lastLocation.addOnSuccessListener(
                            this@MainActivity,
                            object : OnSuccessListener<Location> {
                                override fun onSuccess(p0: Location?) {
                                    p0?.let {
                                        val latitude = p0.latitude
                                        val longitude = p0.longitude
                                        moveMap(latitude, longitude)
                                    }
                                }
                            }
                        )
                        apiClient.disconnect()
                    }
                }

                override fun onConnectionSuspended(p0: Int) {}
            })
            .addOnConnectionFailedListener { }
            .build()

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            !== PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            )
        } else {
            apiClient.connect()
        }
    }

    private fun moveMap(latitude: Double, longitude: Double) {
        val latlng = LatLng(latitude, longitude)
        val position: CameraPosition = CameraPosition.Builder()
            .target(latlng)
            .zoom(16f)
            .build()
        googleMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(position))
        val markerOption = MarkerOptions()
        markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        markerOption.position(latlng)
        markerOption.title("MyLocation")
        googleMap?.addMarker(markerOption)
    }

    private fun fetchAndShowParkingMarkers() {
        Log.d("MARKER", "fetchAndShowParkingMarkers 함수 시작")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/B553881/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ParkingService::class.java)
        val serviceKey = "odpI8YXZwBs0aCMGxGbMJ2Agw9LH7BetjkFXKjn8/V0pk+L5fsRLXKvlZGpFCEk/31cM+b5Bcg+9DAdEvA=="

        // API 호출을 위한 OkHttpClient 설정 (타임아웃 증가)
        val client = OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()

        val retrofitWithTimeout = Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/B553881/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val serviceWithTimeout = retrofitWithTimeout.create(ParkingService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val allItems = mutableListOf<ParkingItem>()
            val totalPages = 2

            try {
                // 주차장 시설 정보 가져오기
                for (page in 1..totalPages) {
                    val response = serviceWithTimeout.getParkingList(
                        serviceKey = serviceKey,
                        pageNo = page,
                        numOfRows = 10,
                        format = 2
                    )
                    Log.d("API_RESPONSE", "시설 정보 API 응답 (페이지 $page): $response")
                    val items = response.PrkSttusInfo
                    allItems.addAll(items)
                    Log.d("MARKER", "페이지 $page 응답: ${items.size}개 항목")
                }

                // 실시간 주차 정보 가져오기
                var realtimeResponse: ParkingStatusResponse? = null
                val maxRetries = 3
                var currentRetry = 0
                val allRealtimeItems = mutableListOf<ParkingStatusItem>()
                val totalRealtimePages = 2
                for (page in 1..totalRealtimePages) {
                    currentRetry = 0
                    var pageResponse: ParkingStatusResponse? = null
                    while (currentRetry < maxRetries && pageResponse == null) {
                        try {
                            Log.d("API_CALL", "실시간 정보 API 호출 시도 (페이지 $page, 재시도 ${currentRetry + 1}/${maxRetries})")
                            pageResponse = serviceWithTimeout.getParkingRealtimeList(
                                serviceKey = serviceKey,
                                pageNo = page,
                                numOfRows = 10,
                                format = 2
                            )
                            Log.d("API_RESPONSE", "실시간 정보 API 응답 수신 (페이지 $page): $pageResponse")
                            pageResponse?.PrkRealtimeInfo?.let {
                                allRealtimeItems.addAll(it)
                                Log.d("MARKER", "실시간 정보 페이지 $page 응답: ${it.size}개 항목")
                            }
                        } catch (e: Exception) {
                            currentRetry++
                            Log.e("API_CALL", "실시간 정보 API 호출 실패 (페이지 $page, 재시도 ${currentRetry}/${maxRetries}): ${e.message}")
                            if (currentRetry < maxRetries) {
                                delay(2000)
                            } else {
                                Log.e("API_CALL", "실시간 정보 API (페이지 $page) 최대 재시도 횟수 초과.")
                            }
                        }
                    }
                }

                if (allRealtimeItems.isNotEmpty()) {
                    allRealtimeItems.forEach {
                        if (it.prk_center_id != null) {
                            realtimeInfoMap[it.prk_center_id] = it
                            if (it.pkfc_Available_ParkingLots_total == it.pkfc_ParkingLots_total) {
                                Log.d("PARKING_DATA", "전체/가능 대수 일치 - ID: ${it.prk_center_id}, 전체: ${it.pkfc_ParkingLots_total}, 가능: ${it.pkfc_Available_ParkingLots_total}")
                            }
                            Log.d("REALTIME_DATA", "실시간 정보 저장 - ID: ${it.prk_center_id}, 가능대수: ${it.pkfc_Available_ParkingLots_total}")
                        }
                    }
                    Log.d("MARKER", "총 ${allRealtimeItems.size}개의 실시간 정보 항목을 매칭에 사용합니다.")
                    Log.d("MARKER_MAP_SIZE", "RealtimeInfoMap size: ${realtimeInfoMap.size}")
                } else {
                    Log.e("MARKER", "실시간 주차 정보를 가져오지 못했습니다. 마커에 정보 없음으로 표시됩니다.")
                    Log.d("MARKER_MAP_SIZE", "RealtimeInfoMap size: ${realtimeInfoMap.size}")
                }

                withContext(Dispatchers.Main) {
                    Log.d("MARKER", "총 ${allItems.size}개 항목 중 마커 표시 시작")
                    var realtimeIndex = 0
                    for (item in allItems) {
                        val lat = item.prk_plce_entrc_la.toDoubleOrNull()
                        val lng = item.prk_plce_entrc_lo.toDoubleOrNull()
                        if (lat != null && lng != null) {
                            val position = LatLng(lat, lng)

                            // 실시간 정보 직접 사용
                            val realtimeItem = if (realtimeIndex < allRealtimeItems.size) {
                                allRealtimeItems[realtimeIndex++]
                            } else {
                                realtimeIndex = 0
                                allRealtimeItems[realtimeIndex++]
                            }
                            val availableLotsText = realtimeItem.pkfc_Available_ParkingLots_total
                            val totalLotsText = realtimeItem.pkfc_ParkingLots_total

                            val marker = MarkerOptions()
                                .position(position)
                                .title(item.prk_plce_nm)
                            val markerObj = googleMap?.addMarker(marker)

                            // 마커 데이터 저장
                            markerObj?.let {
                                parkingDataMap[it.id] = ParkingData(
                                    name = item.prk_plce_nm,
                                    totalSpaces = "전체 주차 대수: ${totalLotsText}대",
                                    availableSpaces = "현재 주차 가능 대수: ${availableLotsText}대"
                                )
                                Log.d("MARKER_DATA", "마커 데이터 저장 - 주차장: ${item.prk_plce_nm}, 가능대수: $availableLotsText")
                            }

                            Log.d("MARKER", "마커 추가 완료 - 주차장: ${item.prk_plce_nm}, ID: ${item.prk_center_id}, 남은 자리수: $availableLotsText")
                        } else {
                            Log.d("MARKER_SKIP", "위치 정보 누락으로 마커 건너뜀 - 주차장: ${item.prk_plce_nm}, 주소: ${item.prk_plce_adres}")
                        }
                    }
                    Log.d("MARKER", "모든 마커 표시 시도 완료")
                }
            } catch (e: Exception) {
                Log.e("MARKER", "에러 발생: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap?.setInfoWindowAdapter(this)
        googleMap?.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
        fetchAndShowParkingMarkers()
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        val view = LayoutInflater.from(this).inflate(R.layout.custom_info_window, null)
        val parkingData = parkingDataMap[marker.id]

        view.findViewById<TextView>(R.id.title).text = parkingData?.name ?: marker.title
        view.findViewById<TextView>(R.id.total_spaces).text = parkingData?.totalSpaces ?: ""
        view.findViewById<TextView>(R.id.available_spaces).text = parkingData?.availableSpaces ?: ""

        return view
    }
}
