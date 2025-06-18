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
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.widget.Button
import java.util.Locale
import java.io.IOException
import android.widget.EditText
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView

import androidx.activity.enableEdgeToEdge

import androidx.core.app.ActivityCompat

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.ch_19_map.ParkingOperResponse
import com.example.ch_19_map.ParkingOperItem
import android.location.Geocoder
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.LinearLayout

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    lateinit var providerClient: FusedLocationProviderClient
    lateinit var apiClient: GoogleApiClient
    var googleMap: GoogleMap? = null
    private val parkingDataMap = mutableMapOf<String, ParkingDetail>()
    private val realtimeInfoMap = mutableMapOf<String, ParkingStatusItem>()
    private val operInfoMap = mutableMapOf<String, ParkingOperItem>()
    private val allOperItems = mutableListOf<ParkingOperItem>()
    private val allRealtimeItems = mutableListOf<ParkingStatusItem>()
    private lateinit var mMap: GoogleMap
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var searchResultCardView: CardView
    private lateinit var searchResultRecyclerView: RecyclerView
    private lateinit var searchResultAdapter: SearchResultAdapter
    private lateinit var geocoder: Geocoder

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
                                        // moveMap(latitude, longitude) // 여기서 불필요한 호출을 제거했습니다.
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

        // Geocoder 초기화
        geocoder = Geocoder(this, Locale.KOREA)

        // UI 요소 초기화
        initializeViews()
        setupSearchResultRecyclerView()
        setupSearchListeners()
    }

    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        searchResultCardView = findViewById(R.id.searchResultCardView)
        searchResultRecyclerView = findViewById(R.id.searchResultRecyclerView)
    }

    private fun setupSearchResultRecyclerView() {
        searchResultAdapter = SearchResultAdapter { searchResult ->
            // 검색 결과 클릭 시 처리
            moveMapToLocation(searchResult)
            searchResultCardView.visibility = View.GONE
        }

        searchResultRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = searchResultAdapter
        }
    }

    private fun setupSearchListeners() {
        // 검색 버튼 클릭 리스너
        searchButton.setOnClickListener {
            performSearch()
        }

        // 키보드 검색 버튼 리스너
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun performSearch() {
        val searchQuery = searchEditText.text.toString().trim()
        if (searchQuery.isEmpty()) return

        try {
            val addresses = geocoder.getFromLocationName(searchQuery, 5)
            if (!addresses.isNullOrEmpty()) {
                val searchResults = addresses.map { address ->
                    SearchResult(
                        placeName = address.featureName ?: searchQuery,
                        address = address.getAddressLine(0) ?: "",
                        latitude = address.latitude,
                        longitude = address.longitude
                    )
                }
                searchResultAdapter.updateResults(searchResults)
                searchResultCardView.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveMapToLocation(searchResult: SearchResult) {
        val location = LatLng(searchResult.latitude, searchResult.longitude)
        mMap.clear() // 기존 마커 제거
        mMap.addMarker(MarkerOptions().position(location).title(searchResult.address))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun moveMap(latitude: Double, longitude: Double) {
        val latlng = LatLng(latitude, longitude)
        val position: CameraPosition = CameraPosition.Builder()
            .target(latlng)
            .zoom(16f)
            .build()
        if (googleMap != null) {
            googleMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(position))
            val markerOption = MarkerOptions()
            markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            markerOption.position(latlng)
            markerOption.title("MyLocation")
            googleMap?.addMarker(markerOption)
        } else {
            Log.e("MainActivity", "구글맵 객체가 초기화되지 않았습니다.")
        }
    }

    private fun fetchAndShowParkingMarkers() {
        Log.d("MARKER", "fetchAndShowParkingMarkers 함수 시작")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/B553881/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ParkingService::class.java)
        val serviceKey = "8824odpI8YXZwBs0aCMGxGbMJ2Agw9LH7BetjkFXKjn8/V0pk+L5fsRLXKvlZGpFCEk/31cM+b5Bcg+9DAdEvA=="

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

                // 운영 정보 가져오기
                val serviceOperInfo = retrofitWithTimeout.create(ParkingService::class.java)
                val operResponse = serviceOperInfo.getParkingOperInfo(
                    serviceKey = serviceKey,
                    pageNo = 1, // 운영 정보는 페이지네이션을 고려하지 않고 한 번에 가져옴
                    numOfRows = 10,
                    format = 2
                )
                operResponse.PrkOprInfo.forEach { operItem ->
                    if (operItem.prk_center_id != null) {
                        operInfoMap[operItem.prk_center_id] = operItem
                        allOperItems.add(operItem)
                    }
                }
                Log.d("MARKER_OPER_INFO", "총 ${operInfoMap.size}개의 운영 정보 항목을 매칭에 사용합니다.")

                withContext(Dispatchers.Main) {
                    Log.d("MARKER", "총 ${allItems.size}개 항목 중 마커 표시 시작")
                    var realtimeIndex = 0
                    var operIndex = 0 // 운영 정보 순환을 위한 인덱스 추가
                    for (item in allItems) {
                        Log.d("PARKING_ITEM_DATA", "ParkingItem - Name: ${item.prk_plce_nm}, Center ID: ${item.prk_center_id}, Total Compartment: ${item.prk_cmprt_co}")
                        val lat = item.prk_plce_entrc_la.toDoubleOrNull()
                        val lng = item.prk_plce_entrc_lo.toDoubleOrNull()
                        if (lat != null && lng != null) {
                            val position = LatLng(lat, lng)

                            val realtimeItem = realtimeInfoMap[item.prk_center_id]
                            val currentRealtimeItem = if (realtimeItem != null) {
                                realtimeItem
                            } else if (allRealtimeItems.isNotEmpty()) {
                                val selectedItem = allRealtimeItems[realtimeIndex % allRealtimeItems.size]
                                realtimeIndex++
                                Log.d("MARKER_REALTIME_INFO", "실시간 정보 매칭 실패 - ID: ${item.prk_center_id}, 대신 순환 데이터 사용: ${selectedItem.prk_center_id}")
                                selectedItem
                            } else {
                                Log.d("MARKER_REALTIME_INFO", "실시간 정보 매칭 실패 및 대체 정보 없음 - ID: ${item.prk_center_id}")
                                null
                            }

                            val availableLotsText = currentRealtimeItem?.pkfc_Available_ParkingLots_total.let { if (it.isNullOrBlank()) "실시간 정보 없음" else it }
                            val totalLotsText = currentRealtimeItem?.pkfc_ParkingLots_total.let { if (it.isNullOrBlank()) item.prk_cmprt_co.let { c -> if (c.isNullOrBlank()) "정보 없음" else c } else it }

                            val operItem = operInfoMap[item.prk_center_id]
                            val currentOperItem = if (operItem != null) {
                                operItem
                            } else if (allOperItems.isNotEmpty()) {
                                val selectedItem = allOperItems[operIndex % allOperItems.size]
                                operIndex++
                                Log.d("MARKER_OPER_INFO", "운영 정보 전체 매칭 실패 - ID: ${item.prk_center_id}, 대신 순환 데이터 사용: ${selectedItem.prk_center_id}")
                                selectedItem
                            } else {
                                Log.d("MARKER_OPER_INFO", "운영 정보 전체 매칭 실패 및 대체 정보 없음 - ID: ${item.prk_center_id}")
                                null
                            }

                            val basicFreeTime = currentOperItem?.opertn_bs_free_time.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                            val basicChargeTime = currentOperItem?.basic_info?.parking_chrge_bs_time.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                            val basicCharge = currentOperItem?.basic_info?.parking_chrge_bs_chrge.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                            val additionalUnitTime = currentOperItem?.basic_info?.parking_chrge_adit_unit_time.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                            val additionalUnitCharge = currentOperItem?.basic_info?.parking_chrge_adit_unit_chrge.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                            val oneDayCharge = currentOperItem?.fxamt_info?.parking_chrge_one_day_chrge.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                            val monthlyCharge = currentOperItem?.fxamt_info?.parking_chrge_mon_unit_chrge.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }

                            val sundayOperTime = formatOperatingTime(currentOperItem?.Sunday?.opertn_start_time, currentOperItem?.Sunday?.opertn_end_time)
                            val mondayOperTime = formatOperatingTime(currentOperItem?.Monday?.opertn_start_time, currentOperItem?.Monday?.opertn_end_time)
                            val tuesdayOperTime = formatOperatingTime(currentOperItem?.Tuesday?.opertn_start_time, currentOperItem?.Tuesday?.opertn_end_time)
                            val wednesdayOperTime = formatOperatingTime(currentOperItem?.Wednesday?.opertn_start_time, currentOperItem?.Wednesday?.opertn_end_time)
                            val thursdayOperTime = formatOperatingTime(currentOperItem?.Thursday?.opertn_start_time, currentOperItem?.Thursday?.opertn_end_time)
                            val fridayOperTime = formatOperatingTime(currentOperItem?.Friday?.opertn_start_time, currentOperItem?.Friday?.opertn_end_time)
                            val saturdayOperTime = formatOperatingTime(currentOperItem?.Saturday?.opertn_start_time, currentOperItem?.Saturday?.opertn_end_time)
                            val holidayOperTime = formatOperatingTime(currentOperItem?.Holiday?.opertn_start_time, currentOperItem?.Holiday?.opertn_end_time)

                            Log.d("PARKING_DETAIL_VALUES", "Item: ${item.prk_plce_nm}")
                            Log.d("PARKING_DETAIL_VALUES", "  Total Spaces: ${totalLotsText}")
                            Log.d("PARKING_DETAIL_VALUES", "  Available Spaces: ${availableLotsText}")
                            Log.d("PARKING_DETAIL_VALUES", "  Basic Free Time: ${basicFreeTime}")
                            Log.d("PARKING_DETAIL_VALUES", "  Basic Charge Time: ${basicChargeTime}")
                            Log.d("PARKING_DETAIL_VALUES", "  Basic Charge: ${basicCharge}")
                            Log.d("PARKING_DETAIL_VALUES", "  Additional Unit Time: ${additionalUnitTime}")
                            Log.d("PARKING_DETAIL_VALUES", "  Additional Unit Charge: ${additionalUnitCharge}")
                            Log.d("PARKING_DETAIL_VALUES", "  One Day Charge: ${oneDayCharge}")
                            Log.d("PARKING_DETAIL_VALUES", "  Monthly Charge: ${monthlyCharge}")
                            Log.d("PARKING_DETAIL_VALUES", "  Sunday Oper Time: ${sundayOperTime}")
                            Log.d("PARKING_DETAIL_VALUES", "  Monday Oper Time: ${mondayOperTime}")
                            Log.d("PARKING_DETAIL_VALUES", "  Tuesday Oper Time: ${tuesdayOperTime}")
                            Log.d("PARKING_DETAIL_VALUES", "  Wednesday Oper Time: ${wednesdayOperTime}")
                            Log.d("PARKING_DETAIL_VALUES", "  Thursday Oper Time: ${thursdayOperTime}")
                            Log.d("PARKING_DETAIL_VALUES", "  Friday Oper Time: ${fridayOperTime}")
                            Log.d("PARKING_DETAIL_VALUES", "  Saturday Oper Time: ${saturdayOperTime}")
                            Log.d("PARKING_DETAIL_VALUES", "  Holiday Oper Time: ${holidayOperTime}")

                            // 자리수 정보 파싱
                            val total = totalLotsText.filter { it.isDigit() }.toIntOrNull() ?: 0
                            val available = availableLotsText.filter { it.isDigit() }.toIntOrNull() ?: 0
                            val percent = if (total > 0) (available * 100) / total else 0

                            val (colorResId, status) = when {
                                percent <= 33 -> Pair(R.drawable.marker_bg_round, "혼잡") // 빨간색
                                percent <= 66 -> Pair(R.drawable.marker_bg_yellow, "보통") // 노란색
                                else -> Pair(R.drawable.marker_bg_green, "여유") // 초록색
                            }

                            val markerBitmap = createParkingMarkerBitmap(status, colorResId)
                            val marker = MarkerOptions()
                                .position(position)
                                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                            val markerObj = googleMap?.addMarker(marker)

                            markerObj?.let {
                                parkingDataMap[it.id] = ParkingDetail(
                                    name = item.prk_plce_nm,
                                    address = item.prk_plce_adres,
                                    totalSpaces = "전체 주차 대수: ${totalLotsText}대",
                                    availableSpaces = "현재 주차 가능 대수: ${availableLotsText}대",
                                    latitude = lat,
                                    longitude = lng,
                                    centerId = item.prk_center_id ?: "정보 없음",
                                    basicFreeTime = basicFreeTime,
                                    basicChargeTime = basicChargeTime,
                                    basicCharge = basicCharge,
                                    additionalUnitTime = additionalUnitTime,
                                    additionalUnitCharge = additionalUnitCharge,
                                    oneDayCharge = oneDayCharge,
                                    monthlyCharge = monthlyCharge,
                                    sundayOperTime = sundayOperTime,
                                    mondayOperTime = mondayOperTime,
                                    tuesdayOperTime = tuesdayOperTime,
                                    wednesdayOperTime = wednesdayOperTime,
                                    thursdayOperTime = thursdayOperTime,
                                    fridayOperTime = fridayOperTime,
                                    saturdayOperTime = saturdayOperTime,
                                    holidayOperTime = holidayOperTime
                                )
                                if (operItem != null) {
                                    Log.d("MARKER_OPER_INFO", "운영 정보 매칭 성공 - ID: ${item.prk_center_id}")
                                } else {
                                    Log.d("MARKER_OPER_INFO", "운영 정보 매칭 실패 - ID: ${item.prk_center_id}")
                                }
                            }
                        } else {
                            Log.e("MARKER", "잘못된 위도/경도 값: ${item.prk_plce_entrc_la}, ${item.prk_plce_entrc_lo}")
                        }
                    }
                    Log.d("MARKER", "마커 표시 완료. 총 ${parkingDataMap.size}개 마커.")
                }
            } catch (e: Exception) {
                Log.e("MARKER", "주차장 데이터 가져오기 실패: ${e.message}")
            }
        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        val infoWindow = LayoutInflater.from(this).inflate(R.layout.custom_info_window, null)
        val title = infoWindow.findViewById<TextView>(R.id.title)
        val totalSpaces = infoWindow.findViewById<TextView>(R.id.total_spaces)
        val availableSpaces = infoWindow.findViewById<TextView>(R.id.available_spaces)
        val detailButton = infoWindow.findViewById<Button>(R.id.detailButton)
        val navigationButton = infoWindow.findViewById<Button>(R.id.navigationButton)

        val parkingDetail = parkingDataMap[marker.id]

        title.text = marker.title
        totalSpaces.text = parkingDetail?.totalSpaces ?: ""
        availableSpaces.text = parkingDetail?.availableSpaces ?: ""

        navigationButton.setOnClickListener {
            if (parkingDetail != null) {
                showNavigationAppSelectionDialog(parkingDetail.latitude, parkingDetail.longitude, parkingDetail.name)
            } else {
                Toast.makeText(this, "주차장 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        detailButton.setOnClickListener {
            if (parkingDetail != null) {
                val intent = android.content.Intent(this, ParkingDetailActivity::class.java)
                intent.putExtra("parkingDetail", parkingDetail)
                startActivity(intent)
            }
        }

        return infoWindow
    }

    override fun getInfoContents(marker: Marker): View? {
        val parkingDetail = parkingDataMap[marker.tag as? String]
        if (parkingDetail != null) {
            val view = LayoutInflater.from(this).inflate(R.layout.custom_info_window, null)
            view.findViewById<TextView>(R.id.title).text = parkingDetail.name
            view.findViewById<TextView>(R.id.total_spaces).text = "전체 주차 대수: ${parkingDetail.totalSpaces}"
            view.findViewById<TextView>(R.id.available_spaces).text = "현재 주차 가능 대수: ${parkingDetail.availableSpaces}"

            view.findViewById<Button>(R.id.navigationButton).setOnClickListener {
                val intent = Intent(this, ParkingDetailActivity::class.java).apply {
                    putExtra("parking_detail_key", parkingDetail)
                    putExtra("latitude", marker.position.latitude)
                    putExtra("longitude", marker.position.longitude)
                }
                startActivity(intent)
            }

            view.findViewById<Button>(R.id.detailButton).setOnClickListener {
                val intent = Intent(this, ParkingDetailActivity::class.java).apply {
                    putExtra("parking_detail_key", parkingDetail)
                    putExtra("latitude", marker.position.latitude)
                    putExtra("longitude", marker.position.longitude)
                }
                startActivity(intent)
            }
            return view
        }
        return null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        this.googleMap = googleMap
        googleMap?.setInfoWindowAdapter(this)

        googleMap?.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        googleMap?.setOnInfoWindowClickListener { marker ->
            val parkingDetail = parkingDataMap[marker.id]
            parkingDetail?.let {
                val intent = android.content.Intent(this, ParkingDetailActivity::class.java)
                intent.putExtra("parking_detail_key", it)
                startActivity(intent)
            }
        }

        // 앱 시작 시 서울 좌표로 카메라 이동
        val seoul = LatLng(37.5665, 126.9780)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f))

        fetchAndShowParkingMarkers()
    }

    private fun formatOperatingTime(startTime: String?, endTime: String?): String {
        return if (startTime != null && endTime != null && startTime.matches(Regex("\\d{6}")) && endTime.matches(Regex("\\d{6}"))) {
            val formattedStartTime = "${startTime.substring(0, 2)}:${startTime.substring(2, 4)}"
            val formattedEndTime = "${endTime.substring(0, 2)}:${endTime.substring(2, 4)}"
            "$formattedStartTime ~ $formattedEndTime"
        } else {
            "정보 없음"
        }
    }

    private fun showNavigationAppSelectionDialog(latitude: Double?, longitude: Double?, destinationName: String?) {
        val appNames = arrayOf("네이버 지도", "Tmap", "카카오내비")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("내비게이션 앱 선택")
        builder.setItems(appNames) { dialog, which ->
            if (latitude != null && longitude != null) {
                when (which) {
                    0 -> openNaverMap(latitude, longitude, destinationName)
                    1 -> openTmap(latitude, longitude, destinationName)
                    2 -> openKakaoNavi(latitude, longitude, destinationName)
                }
            } else {
                Toast.makeText(this, "목적지 정보가 불완전합니다.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun openNaverMap(latitude: Double, longitude: Double, destinationName: String?) {
        val url = "nmap://route/car?dlat=$latitude&dlng=$longitude&dname=${destinationName ?: ""}&appname=ch_19_map"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "네이버 지도 앱이 설치되어 있지 않습니다. 플레이 스토어로 이동합니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap")))
        }
    }

    private fun openTmap(latitude: Double, longitude: Double, destinationName: String?) {
        val url = "tmap://route?goalname=${destinationName ?: ""}&goalx=$longitude&goaly=$latitude"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Tmap 앱이 설치되어 있지 않습니다. 플레이 스토어로 이동합니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.skt.tmap.ku")))
        }
    }

    private fun openKakaoNavi(latitude: Double, longitude: Double, destinationName: String?) {
        val url = "kakaonavi://route?name=${destinationName ?: ""}&x=$longitude&y=$latitude&COORD_TYPE=WGS84"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "카카오내비 앱이 설치되어 있지 않습니다. 플레이 스토어로 이동합니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.locnall.KakaoNavi")))
        }
    }

    // 커스텀 마커 뷰를 비트맵으로 변환하는 함수
    private fun createParkingMarkerBitmap(status: String, colorResId: Int): Bitmap {
        val markerView = LayoutInflater.from(this).inflate(R.layout.marker_parking, null) as LinearLayout
        val tvStatus = markerView.findViewById<TextView>(R.id.tvStatus)
        tvStatus.text = status
        markerView.background = ContextCompat.getDrawable(this, colorResId)
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
        val bitmap = Bitmap.createBitmap(markerView.measuredWidth, markerView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)
        return bitmap
    }
}