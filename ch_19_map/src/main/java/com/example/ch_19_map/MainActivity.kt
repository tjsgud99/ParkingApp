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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.ch_19_map.SearchHistoryAdapter
import com.example.ch_19_map.SearchHistoryItem
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import android.widget.FrameLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.view.inputmethod.InputMethodManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.ImageView
import java.net.URLEncoder
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    companion object {
        // 정적 데이터 저장
        private var savedParkingLotList: List<ParkingLotResponse> = emptyList()
        private var savedParkingDataMap = mutableMapOf<String, ParkingDetail>()
        private var savedFavoriteParkingLotIds = mutableSetOf<Long>()
    }

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
    private lateinit var parkingLotResultAdapter: ParkingLotResultAdapter
    private lateinit var geocoder: Geocoder
    private val favoriteParkingLotIds = mutableSetOf<Long>()

    // SharedPreferences 키
    private val PREFS_NAME = "search_history_prefs"
    private val KEY_RECENT = "recent_history"
    private val KEY_FAVORITE = "favorite_history"

    private lateinit var fullAdapter: SearchHistoryAdapter
    private var isFavoriteTab = false

    // UI 관련 변수들
    private lateinit var searchFullLayout: FrameLayout
    private lateinit var btnCloseSearch: ImageButton
    private lateinit var searchCardView: View
    private lateinit var btnPQ: ImageButton
    private lateinit var pqText: View
    private lateinit var fullSearchEditText: EditText
    private lateinit var fullSearchButton: ImageButton
    private lateinit var fullTabRecent: TextView
    private lateinit var fullTabFavorite: TextView
    private lateinit var fullHistoryRecyclerView: RecyclerView
    private lateinit var sideMenuLayout: LinearLayout
    private lateinit var sideMenuOverlay: View

    private var parkingLotList: List<ParkingLotResponse> = emptyList()

    // 지도 검색 결과 어댑터 및 뷰 변수 추가
    private lateinit var searchPlaceResultCardView: androidx.cardview.widget.CardView
    private lateinit var searchPlaceResultRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var searchPlaceResultAdapter: SearchResultAdapter

    private var isDataLoaded = false

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. UI 요소부터 초기화
        initializeViews()
        
        // 2. 리스너 설정
        setupListeners()
        
        // 3. RecyclerView 설정
        setupRecyclerViews()

        // 4. 나머지 초기화 작업
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

        // 로그인 성공 후, 또는 onResume에서 즐겨찾기 목록 불러오기
        if (isLoggedIn()) {
            fetchFavorites()
        }
        loadBackendData()

        // 알림 메뉴 클릭 시 NotificationActivity로 이동
        findViewById<LinearLayout>(R.id.layoutNotification).setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        // 현재 언어 표시
        val lang = getSharedPreferences("settings", MODE_PRIVATE).getString("lang", "ko")
        val langName = when(lang) {
            "en" -> getString(R.string.english)
            else -> getString(R.string.korean)
        }
        findViewById<TextView>(R.id.tvCurrentLanguage)?.text = getString(R.string.current_language, langName)

        // 설정(언어 변경) 클릭 리스너
        findViewById<TextView>(R.id.tvSettings).setOnClickListener {
            val languages = arrayOf(getString(R.string.korean), getString(R.string.english))
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.language_select))
                .setItems(languages) { _, which ->
                    when (which) {
                        0 -> setLocale("ko")
                        1 -> setLocale("en")
                    }
                }
                .show()
        }

        // 고객센터 메뉴 클릭 리스너 추가
        findViewById<TextView>(R.id.tvCs).setOnClickListener {
            startActivity(Intent(this, CustomerServiceActivity::class.java))
        }

        // 지도 검색 결과 뷰 초기화 (초기화 코드 복구)
        searchPlaceResultCardView = findViewById(R.id.searchPlaceResultCardView)
        searchPlaceResultRecyclerView = findViewById(R.id.searchPlaceResultRecyclerView)
        searchPlaceResultAdapter = SearchResultAdapter(emptyList()) { result ->
            moveMap(result.latitude, result.longitude)
            searchPlaceResultCardView.visibility = View.GONE
        }
        searchPlaceResultRecyclerView.adapter = searchPlaceResultAdapter
        searchPlaceResultRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        // 기존 서울로 이동 코드 대신 위치 권한 체크 및 내 위치 이동
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            moveToMyLocation()
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                moveToMyLocation()
            } else {
                // 권한 거부 시 서울로 이동
                val seoul = LatLng(37.5665, 126.9780)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f))
            }
        }
    }

    private fun moveToMyLocation() {
        if (!::mMap.isInitialized) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val myLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15f))
                } else {
                    // 위치 못 받으면 서울로 이동
                    val seoul = LatLng(37.5665, 126.9780)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f))
                }
            }
        }
    }

    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        searchResultCardView = findViewById(R.id.searchResultCardView)
        searchResultRecyclerView = findViewById(R.id.searchResultRecyclerView)
        btnPQ = findViewById(R.id.btnPQ)
        pqText = findViewById(R.id.pqText)
        searchFullLayout = findViewById(R.id.searchFullLayout)
        btnCloseSearch = findViewById(R.id.btnCloseSearch)
        searchCardView = findViewById(R.id.searchCardView)
        fullSearchEditText = findViewById(R.id.fullSearchEditText)
        fullSearchButton = findViewById(R.id.fullSearchButton)
        fullTabRecent = findViewById(R.id.fullTabRecent)
        fullTabFavorite = findViewById(R.id.fullTabFavorite)
        fullHistoryRecyclerView = findViewById(R.id.fullHistoryRecyclerView)
        sideMenuLayout = findViewById(R.id.sideMenuLayout)
        sideMenuOverlay = findViewById(R.id.sideMenuOverlay)
    }

    private fun setupListeners() {
        // 검색 관련 리스너
        searchButton.setOnClickListener { executeSearch(searchEditText.text.toString()) }
        searchEditText.setOnEditorActionListener { _, id, _ -> if (id == EditorInfo.IME_ACTION_SEARCH) { executeSearch(searchEditText.text.toString()); true } else false }
        fullSearchButton.setOnClickListener { executeSearch(fullSearchEditText.text.toString()) }
        fullSearchEditText.setOnEditorActionListener { _, id, _ -> if (id == EditorInfo.IME_ACTION_SEARCH) { executeSearch(fullSearchEditText.text.toString()); true } else false }
        
        // UI 상호작용 리스너
        searchEditText.setOnClickListener { openSearchFullScreen() }
        btnCloseSearch.setOnClickListener { closeSearchFullScreen() }

        // 사이드 메뉴 리스너
        btnPQ.setOnClickListener { openSideMenu() }
        sideMenuOverlay.setOnClickListener { closeSideMenu() }
        findViewById<Button>(R.id.btnLogin).setOnClickListener { handleLoginButtonClick() }
        findViewById<TextView>(R.id.tvMyInfo).setOnClickListener { openMyInfo() }

        // 탭 리스너
        fullTabRecent.setOnClickListener { selectRecentTab() }
        fullTabFavorite.setOnClickListener { selectFavoriteTab() }
    }
    
    private fun setupRecyclerViews() {
        // 검색 결과 RecyclerView
        parkingLotResultAdapter = ParkingLotResultAdapter(
            favoriteParkingLotIds = favoriteParkingLotIds,
            onItemClick = { parkingLot ->
                val location = LatLng(parkingLot.latitude, parkingLot.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                searchResultCardView.visibility = View.GONE
            },
            onFavoriteClick = { parkingLot, isCurrentlyFavorite ->
                handleFavoriteClick(parkingLot, isCurrentlyFavorite)
            }
        )
        searchResultRecyclerView.layoutManager = LinearLayoutManager(this)
        searchResultRecyclerView.adapter = parkingLotResultAdapter
        
        // 검색 히스토리 RecyclerView
        val favoriteKeywords = loadHistory(true).map { it.keyword }.toSet()
        fullAdapter = SearchHistoryAdapter(
            historyList = loadHistory(isFavoriteTab),
            favoriteKeywords = favoriteKeywords,
            isLoggedIn = isLoggedIn(),
            onItemClick = { item ->
                Log.d("SearchHistory", "Item clicked: ${item.keyword}")
                fullSearchEditText.setText(item.keyword)
                executeSearch(item.keyword)
            },
            onDeleteClick = { item ->
                Log.d("SearchHistory", "Deleting item: ${item.keyword}")
                val currentList = loadHistory(isFavoriteTab).toMutableList()
                currentList.removeAll { it.keyword == item.keyword }
                saveHistory(currentList, isFavoriteTab)
                if (!isFavoriteTab) {
                    val favoriteList = loadHistory(true).toMutableList()
                    favoriteList.removeAll { it.keyword == item.keyword }
                    saveHistory(favoriteList, true)
                }
                updateFullSearchUI()
            },
            onFavoriteClick = { item ->
                val favoriteList = loadHistory(true).toMutableList()
                val isCurrentlyFavorite = favoriteList.any { it.keyword == item.keyword }
                if (isCurrentlyFavorite) {
                    favoriteList.removeAll { it.keyword == item.keyword }
                } else {
                    favoriteList.add(0, item)
                }
                saveHistory(favoriteList, true)
                updateFullSearchUI()
            },
            onLoginRequired = {
                showLoginRequiredToast()
            }
        )
        fullHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        fullHistoryRecyclerView.adapter = fullAdapter
    }
    
    // UI 이벤트 핸들러 함수들
    private fun openSearchFullScreen() {
        searchFullLayout.visibility = View.VISIBLE
        searchCardView.visibility = View.GONE
        btnPQ.visibility = View.GONE
        pqText.visibility = View.GONE
        searchEditText.clearFocus()
        updateFullSearchUI()
    }
    
    private fun closeSearchFullScreen() {
        searchFullLayout.visibility = View.GONE
        searchCardView.visibility = View.VISIBLE
        btnPQ.visibility = View.VISIBLE
        pqText.visibility = View.VISIBLE
    }
    
    private fun openSideMenu() {
        updateSideMenuUserInfo()
        sideMenuLayout.visibility = View.VISIBLE
        sideMenuOverlay.visibility = View.VISIBLE
    }
    
    private fun closeSideMenu() {
        sideMenuLayout.visibility = View.GONE
        sideMenuOverlay.visibility = View.GONE
    }
    
    private fun handleLoginButtonClick() {
        if (isLoggedIn()) {
            getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply()
            favoriteParkingLotIds.clear()
            updateSideMenuUserInfo()
            updateLoginButtonUI()
            updateFullSearchUI()
            // 즐겨찾기만 초기화
            if (::parkingLotResultAdapter.isInitialized) {
                parkingLotResultAdapter.updateFavorites(emptySet<Long>())
            }
            // 마커 데이터는 건드리지 않고, 지도에 캐시된 마커를 다시 그림
            if (::mMap.isInitialized) {
                val cachedMarkers = loadMarkersFromPrefs()
                if (cachedMarkers.isNotEmpty()) {
                    addMarkersToMap(cachedMarkers)
                }
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
    
    private fun openMyInfo() {
        if (!isLoggedIn()) {
            showLoginRequiredToast()
            closeSideMenu()
            return
        }
        startActivity(Intent(this, MyInfoActivity::class.java))
        closeSideMenu()
    }
    
    private fun selectRecentTab() {
        isFavoriteTab = false
        updateFullSearchUI()
    }
    
    private fun selectFavoriteTab() {
        if (isLoggedIn()) {
            isFavoriteTab = true
            updateFullSearchUI()
        } else {
            showLoginRequiredToast()
        }
    }

    private fun executeSearch(searchQuery: String) {
        if (searchQuery.isBlank()) {
            Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        saveSearchQuery(searchQuery)
        closeSearchFullScreen()
        searchResultCardView.visibility = View.GONE

        // ================== [지도 장소 검색: 여러 결과 리스트로 표시] ==================
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocationName(searchQuery, 10)
                withContext(Dispatchers.Main) {
                    if (addresses != null && addresses.isNotEmpty()) {
                        val results = addresses.map {
                            SearchResult(
                                placeName = searchQuery,
                                address = it.getAddressLine(0) ?: "",
                                latitude = it.latitude,
                                longitude = it.longitude
                            )
                        }
                        searchPlaceResultAdapter.updateResults(results)
                        searchPlaceResultCardView.visibility = View.VISIBLE
                    } else {
                        searchPlaceResultAdapter.updateResults(emptyList())
                        searchPlaceResultCardView.visibility = View.GONE
                        Toast.makeText(this@MainActivity, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    searchPlaceResultAdapter.updateResults(emptyList())
                    searchPlaceResultCardView.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "위치 검색 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // =============================================================
    }

    private fun saveSearchQuery(query: String) {
        val history = loadHistory(isFavorite = false).toMutableList()
        
        // 중복 검색어 제거
        history.removeAll { it.keyword == query }

        // 새 검색어 추가 (최신 날짜로)
        val currentDate = SimpleDateFormat("MM.dd", Locale.KOREA).format(Date())
        history.add(0, SearchHistoryItem(query, currentDate, false))

        // 목록 크기 제한 (예: 20개)
        val trimmedHistory = if (history.size > 20) history.subList(0, 20) else history
        
        saveHistory(trimmedHistory, isFavorite = false)
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
        if (isDataLoaded) {
            Log.d("MARKER", "중복 호출 방지: 이미 데이터가 로드됨");
            return
        }

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
            val totalPages = 1
            val numOfRaw = 200
            try {
                // 주차장 시설 정보 가져오기
                for (page in 1..totalPages) {
                    val response = serviceWithTimeout.getParkingList(
                        serviceKey = serviceKey,
                        pageNo = page,
                        numOfRows = numOfRaw,
                        format = 2
                    )
                    Log.d("API_RESPONSE", "시설 정보 API 응답 (페이지 $page): $response")
                    val items = response.PrkSttusInfo
                    allItems.addAll(items)
                    Log.d("MARKER", "페이지 $page 응답: ${items.size}개 항목")
                }

                // 실시간 주차 정보 가져오기
                allRealtimeItems.clear()
                for (page in 1..totalPages) {
                    var currentRetry = 0
                    var pageResponse: ParkingStatusResponse? = null
                    val maxRetries = 3
                    while (currentRetry < maxRetries && pageResponse == null) {
                        try {
                            Log.d("API_CALL", "실시간 정보 API 호출 시도 (페이지 $page, 재시도 "+
                                "${currentRetry + 1}/${maxRetries})")
                            pageResponse = serviceWithTimeout.getParkingRealtimeList(
                                serviceKey = serviceKey,
                                pageNo = page,
                                numOfRows = numOfRaw,
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

                // 운영 정보 가져오기
                allOperItems.clear()
                val serviceOperInfo = retrofitWithTimeout.create(ParkingService::class.java)
                for (page in 1..totalPages) {
                    val operResponse = serviceOperInfo.getParkingOperInfo(
                        serviceKey = serviceKey,
                        pageNo = page,
                        numOfRows = numOfRaw, // 필요시 100 등으로 늘릴 수 있음
                        format = 2
                    )
                    operResponse.PrkOprInfo.forEach { operItem ->
                        if (operItem.prk_center_id != null) {
                            allOperItems.add(operItem)
                        }
                    }
                }

                // === 순서대로 매칭해서 마커 표시 ===
                withContext(Dispatchers.Main) {
                    Log.d("MARKER", "총 ${allItems.size}개 항목 중 순서대로 마커 표시 시작")
                    parkingDataMap.clear()
                    googleMap?.clear()
                    val markerCount = minOf(allItems.size, allOperItems.size, allRealtimeItems.size)
                    for (i in 0 until markerCount) {
                        val item = allItems[i]
                        val operItem = allOperItems[i]
                        val realtimeItem = allRealtimeItems[i]
                        val lat = item.prk_plce_entrc_la.toDoubleOrNull()
                        val lng = item.prk_plce_entrc_lo.toDoubleOrNull()
                        if (lat == null || lng == null) continue
                        val position = LatLng(lat, lng)

                        val availableLotsText = realtimeItem.pkfc_Available_ParkingLots_total.let { if (it.isNullOrBlank()) "실시간 정보 없음" else it }
                        val totalLotsText = realtimeItem.pkfc_ParkingLots_total.let { if (it.isNullOrBlank()) item.prk_cmprt_co.let { c -> if (c.isNullOrBlank()) "정보 없음" else c } else it }

                        val basicFreeTime = operItem.opertn_bs_free_time.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                        val basicChargeTime = operItem.basic_info?.parking_chrge_bs_time.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                        val basicCharge = operItem.basic_info?.parking_chrge_bs_chrge.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                        val additionalUnitTime = operItem.basic_info?.parking_chrge_adit_unit_time.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                        val additionalUnitCharge = operItem.basic_info?.parking_chrge_adit_unit_chrge.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                        val oneDayCharge = operItem.fxamt_info?.parking_chrge_one_day_chrge.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }
                        val monthlyCharge = operItem.fxamt_info?.parking_chrge_mon_unit_chrge.let { if (it.isNullOrBlank()) "운영 정보 없음" else it }

                        val sundayOperTime = formatOperatingTime(operItem.Sunday?.opertn_start_time, operItem.Sunday?.opertn_end_time)
                        val mondayOperTime = formatOperatingTime(operItem.Monday?.opertn_start_time, operItem.Monday?.opertn_end_time)
                        val tuesdayOperTime = formatOperatingTime(operItem.Tuesday?.opertn_start_time, operItem.Tuesday?.opertn_end_time)
                        val wednesdayOperTime = formatOperatingTime(operItem.Wednesday?.opertn_start_time, operItem.Wednesday?.opertn_end_time)
                        val thursdayOperTime = formatOperatingTime(operItem.Thursday?.opertn_start_time, operItem.Thursday?.opertn_end_time)
                        val fridayOperTime = formatOperatingTime(operItem.Friday?.opertn_start_time, operItem.Friday?.opertn_end_time)
                        val saturdayOperTime = formatOperatingTime(operItem.Saturday?.opertn_start_time, operItem.Saturday?.opertn_end_time)
                        val holidayOperTime = formatOperatingTime(operItem.Holiday?.opertn_start_time, operItem.Holiday?.opertn_end_time)

                        // 자리수 정보 파싱
                        val total = totalLotsText.filter { it.isDigit() }.toIntOrNull() ?: 0
                        val available = availableLotsText.filter { it.isDigit() }.toIntOrNull() ?: 0
                        val percent = if (total > 0) (available * 100) / total else 0

                        val (bgResId, statusText, pColor) = when {
                            percent >= 60 -> Triple(R.drawable.marker_bg_green, "여유", android.graphics.Color.parseColor("#43A047"))
                            percent >= 30 -> Triple(R.drawable.marker_bg_yellow, "보통", android.graphics.Color.parseColor("#FBC02D"))
                            else -> Triple(R.drawable.marker_bg_red, "혼잡", android.graphics.Color.parseColor("#E53935"))
                        }

                        val markerView = LayoutInflater.from(this@MainActivity).inflate(R.layout.marker_layout, null)
                        val tvStatus = markerView.findViewById<TextView>(R.id.marker_text)
                        val root = markerView.findViewById<View>(R.id.marker_root)
                        val tvP = markerView.findViewById<TextView>(R.id.marker_p)
                        tvStatus.text = statusText
                        root.setBackgroundResource(bgResId)
                        tvP.setTextColor(pColor)

                        val markerBitmap = createBitmapFromView(markerView)
                        val marker = MarkerOptions()
                            .position(position)
                            .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                        val markerObj = googleMap?.addMarker(marker)

                        markerObj?.let {
                            parkingDataMap[it.id] = ParkingDetail(
                                name = item.prk_plce_nm,
                                address = item.prk_plce_adres,
                                totalSpaces = totalLotsText,
                                availableSpaces = availableLotsText,
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
                        }
                    }
                    Log.d("MARKER", "마커 표시 완료. 총 ${parkingDataMap.size}개 마커.")

                    // 데이터를 companion object에 저장
                    savedParkingLotList = this@MainActivity.parkingLotList
                    savedParkingDataMap.clear()
                    savedParkingDataMap.putAll(parkingDataMap)
                    savedFavoriteParkingLotIds.clear()
                    savedFavoriteParkingLotIds.addAll(favoriteParkingLotIds)
                    isDataLoaded = true

                    // API로 받은 데이터 저장
                    saveMarkersToPrefs(this@MainActivity.parkingLotList)
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
        val actionButton = infoWindow.findViewById<LinearLayout>(R.id.actionButton)

        val parkingDetail = parkingDataMap[marker.id]
        title.text = marker.title ?: parkingDetail?.name ?: ""
        totalSpaces.text = "전체 자리수: ${parkingDetail?.totalSpaces ?: "-"}"
        availableSpaces.text = "현재 남은 자리수: ${parkingDetail?.availableSpaces ?: "-"}"

        actionButton.setOnClickListener {
            if (parkingDetail != null) {
                val intent = Intent(this, ParkingDetailActivity::class.java)
                intent.putExtra("parking_detail_key", parkingDetail)
                intent.putExtra("latitude", marker.position.latitude)
                intent.putExtra("longitude", marker.position.longitude)
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
            view.findViewById<TextView>(R.id.total_spaces).text = "전체 자리수: ${parkingDetail.totalSpaces}"
            view.findViewById<TextView>(R.id.available_spaces).text = "현재 남은 자리수: ${parkingDetail.availableSpaces}"

            view.findViewById<LinearLayout>(R.id.actionButton).setOnClickListener {
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
                intent.putExtra("latitude", marker.position.latitude)
                intent.putExtra("longitude", marker.position.longitude)
                startActivity(intent)
            }
        }
        val seoul = LatLng(37.5665, 126.9780)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f))
        // parkingLotList가 비어있지 않으면 항상 마커 추가
        if (parkingLotList.isNotEmpty()) {
            addMarkersToMap(parkingLotList)
        } else {
            // 캐시된 마커 먼저 표시
            val cachedMarkers = loadMarkersFromPrefs()
            if (cachedMarkers.isNotEmpty()) {
                addMarkersToMap(cachedMarkers)
            }
            // 최신 데이터로 갱신
            fetchAndShowParkingMarkers()
        }
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
        val encodedName = URLEncoder.encode(destinationName ?: "", "UTF-8")
        val url = "tmap://route?goalx=$longitude&goaly=$latitude&goalname=$encodedName&coordType=WGS84"
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
        val markerView = LayoutInflater.from(this).inflate(R.layout.marker_layout, null)
        val tvStatus = markerView.findViewById<TextView>(R.id.marker_text)
        tvStatus.text = status
        markerView.setBackgroundResource(colorResId)
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
        val bitmap = Bitmap.createBitmap(markerView.measuredWidth, markerView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)
        return bitmap
    }

    // 최근검색/즐겨찾기 불러오기
    private fun loadHistory(isFavorite: Boolean): List<SearchHistoryItem> {
        val prefs = getSharedPreferences("search_history", MODE_PRIVATE)
        val gson = Gson()
        
        val key = if (isFavorite) {
            val userId = getUserId()
            if (userId != -1L) {
                "favorite_history_$userId" // User-specific key
            } else {
                Log.w("History", "Cannot load favorites without a logged-in user.")
                return emptyList() // Not logged in, return empty list for favorites
            }
        } else {
            "recent_history"
        }
        
        val json = prefs.getString(key, null)
        val type = object : TypeToken<List<SearchHistoryItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // 최근검색/즐겨찾기 저장
    private fun saveHistory(list: List<SearchHistoryItem>, isFavorite: Boolean) {
        val prefs = getSharedPreferences("search_history", MODE_PRIVATE).edit()
        val gson = Gson()
        
        val key = if (isFavorite) {
            val userId = getUserId()
            if (userId != -1L) {
                "favorite_history_$userId" // User-specific key
            } else {
                Log.w("History", "Cannot save favorites without a logged-in user.")
                return // Do not save if user is not logged in
            }
        } else {
            "recent_history"
        }
        
        val json = gson.toJson(list)
        prefs.putString(key, json)
        prefs.apply()
    }

    // 최근검색 추가
    private fun addRecentHistory(keyword: String) {
        Log.d("SearchHistory", "Adding recent history: $keyword")
        val date = SimpleDateFormat("MM.dd", Locale.getDefault()).format(Date())
        val list = loadHistory(false).toMutableList()
        list.removeAll { it.keyword == keyword }
        list.add(0, SearchHistoryItem(keyword, date, false))
        if (list.size > 20) list.removeAt(list.lastIndex) // 최대 20개
        saveHistory(list, false)
        Log.d("SearchHistory", "Current recent history size: ${list.size}")
    }

    // 즐겨찾기 추가/삭제
    private fun toggleFavorite(item: SearchHistoryItem) {
        val favList = loadHistory(true).toMutableList()
        if (favList.any { it.keyword == item.keyword }) {
            favList.removeAll { it.keyword == item.keyword }
        } else {
            favList.add(0, item.copy(isFavorite = true))
        }
        saveHistory(favList, true)
        updateFullSearchUI() // 즐겨찾기 변경 후 UI 갱신 추가
    }

    // UI 갱신 함수
    private fun updateFullSearchUI() {
        // 탭 상태 업데이트
        fullTabRecent.isSelected = !isFavoriteTab
        fullTabFavorite.isSelected = isFavoriteTab

        val favoriteKeywords = loadHistory(true).map { it.keyword }.toSet()
        val currentHistory = loadHistory(isFavoriteTab)
        val isLoggedInNow = isLoggedIn()
        if (::fullAdapter.isInitialized) {
            (fullHistoryRecyclerView.adapter as SearchHistoryAdapter).updateList(currentHistory, favoriteKeywords, isLoggedInNow)
        } else {
            fullAdapter = SearchHistoryAdapter(
                historyList = currentHistory,
                favoriteKeywords = favoriteKeywords,
                isLoggedIn = isLoggedInNow,
                onItemClick = { item ->
                    Log.d("SearchHistory", "Item clicked: ${item.keyword}")
                    fullSearchEditText.setText(item.keyword)
                    executeSearch(item.keyword)
                },
                onDeleteClick = { item ->
                    Log.d("SearchHistory", "Deleting item: ${item.keyword}")
                    val currentList = loadHistory(isFavoriteTab).toMutableList()
                    currentList.removeAll { it.keyword == item.keyword }
                    saveHistory(currentList, isFavoriteTab)
                    if (!isFavoriteTab) {
                        val favoriteList = loadHistory(true).toMutableList()
                        favoriteList.removeAll { it.keyword == item.keyword }
                        saveHistory(favoriteList, true)
                    }
                    updateFullSearchUI()
                },
                onFavoriteClick = { item ->
                    val favoriteList = loadHistory(true).toMutableList()
                    val isCurrentlyFavorite = favoriteList.any { it.keyword == item.keyword }
                    if (isCurrentlyFavorite) {
                        favoriteList.removeAll { it.keyword == item.keyword }
                    } else {
                        favoriteList.add(0, item)
                    }
                    saveHistory(favoriteList, true)
                    updateFullSearchUI()
                },
                onLoginRequired = {
                    showLoginRequiredToast()
                }
            )
            fullHistoryRecyclerView.adapter = fullAdapter
        }
    }

    // ==================== 백엔드 API 연동 기능 ====================
    
    // 백엔드에서 주차장 데이터 가져오기
    private fun fetchParkingLotsFromBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.getApiService(this@MainActivity).getAllParkingLots()
                if (response.isSuccessful) {
                    val lots = response.body()
                    if (!lots.isNullOrEmpty()) {
                        this@MainActivity.parkingLotList = lots
                        withContext(Dispatchers.Main) {
                            addMarkersToMap(lots)
                        }
                    }
                } else {
                    Log.e("MainActivity", "주차장 정보를 불러오는데 실패했습니다: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading parking lot data", e)
            }
        }
    }
    
    // 백엔드 주차장 데이터를 지도에 표시
    private fun displayBackendParkingLots(parkingLots: List<ParkingLotResponse>) {
        parkingLots.forEach { parkingLot ->
            // 주소를 좌표로 변환
            try {
                val addresses = geocoder.getFromLocationName(parkingLot.address, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val location = LatLng(address.latitude, address.longitude)
                    
                    // 마커 생성
                    val markerOptions = MarkerOptions()
                        .position(location)
                        .title(parkingLot.name)
                        .snippet("${parkingLot.address}\n운영시간: ${parkingLot.runtime}\n요금: ${parkingLot.fee}원")
                    
                    val marker = mMap.addMarker(markerOptions)
                    
                    // 마커 정보 저장
                    marker?.let { m ->
                        val parkingDetail = ParkingDetail(
                            name = parkingLot.name,
                            address = parkingLot.address,
                            totalSpaces = parkingLot.total_space.toString(),
                            availableSpaces = "0", // 백엔드에서 제공하지 않으므로 기본값
                            latitude = address.latitude,
                            longitude = address.longitude,
                            centerId = "", // 필요시 적절히 할당
                        )
                        parkingDataMap[m.id] = parkingDetail
                    }
                }
            } catch (e: IOException) {
                Log.e("Geocoding", "주소 변환 실패: ${parkingLot.address}", e)
            }
        }
    }
    
    // 주차장 등록 (관리자용)
    private fun registerParkingLotToBackend(parkingLot: ParkingLotRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.getApiService(this@MainActivity).registerParkingLot(parkingLot)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "주차장 등록 완료!", Toast.LENGTH_SHORT).show()
                        // 등록 후 목록 새로고침
                        fetchParkingLotsFromBackend()
                    } else {
                        Toast.makeText(this@MainActivity, "주차장 등록 실패: ${response.body()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "주차장 등록 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
                Log.e("API", "주차장 등록 오류", e)
            }
        }
    }
    
    // 사용자 로그인
    private fun loginUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UserLoginRequest(email, password)
                val response = RetrofitClient.getApiService(this@MainActivity).loginUser(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && responseBody.contains("로그인 성공")) {
                            Toast.makeText(this@MainActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            // 로그인 성공 후 처리 (예: 사용자 정보 저장)
                        } else {
                            Toast.makeText(this@MainActivity, "로그인 실패: $responseBody", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "로그인 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
                Log.e("API", "로그인 오류", e)
            }
        }
    }
    
    // 사용자 회원가입
    private fun registerUser(username: String, email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UserRegisterRequest(username, email, password)
                val response = RetrofitClient.getApiService(this@MainActivity).registerUser(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val userResponse = response.body()
                        if (userResponse?.success == true) {
                            Toast.makeText(this@MainActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "회원가입 실패: ${userResponse?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "회원가입 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
                Log.e("API", "회원가입 오류", e)
            }
        }
    }
    
    // 즐겨찾기 추가
    private fun addFavorite(userId: Long, parkingLotId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = FavoriteRequest(userId, parkingLotId)
                val response = RetrofitClient.getApiService(this@MainActivity).addFavorite(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "즐겨찾기 추가 완료!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("FavoriteTest", "handleFavoriteClick: 즐겨찾기 추가 실패, code=${response.code()}, error=${response.errorBody()?.string()}")
                        Toast.makeText(this@MainActivity, "즐겨찾기 추가 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "즐겨찾기 추가 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
                Log.e("API", "즐겨찾기 추가 오류", e)
            }
        }
    }
    
    // 앱 시작 시 백엔드 데이터 로드
    private fun loadBackendData() {
        fetchParkingLotsFromBackend()
    }

    private fun updateSideMenuUserInfo() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val username = prefs.getString("username", null)
        val email = prefs.getString("email", null)
        findViewById<TextView>(R.id.tvUserName).text = username ?: getString(R.string.login_please)
        findViewById<TextView>(R.id.tvUserEmail).text = email ?: getString(R.string.email)
        updateAdminUI() // 로그인 후에도 관리자 UI 즉시 반영
    }

    private fun isLoggedIn(): Boolean {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getString("username", null) != null
    }

    private fun updateLoginButtonUI() {
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        if (isLoggedIn()) {
            btnLogin.text = getString(R.string.logout)
        } else {
            btnLogin.text = getString(R.string.login)
        }
    }

    private fun handleFavoriteClick(parkingLot: ParkingLotResponse, isCurrentlyFavorite: Boolean) {
        Log.d("FavoriteTest", "handleFavoriteClick 진입: parkingLotId=${parkingLot.id}, isCurrentlyFavorite=$isCurrentlyFavorite")
        if (!isLoggedIn()) {
            Log.d("FavoriteTest", "handleFavoriteClick: 로그인 필요")
            showLoginRequiredToast()
            return
        }

        val userId = getUserId() // SharedPreferences에서 userId 가져오기
        Log.d("FavoriteTest", "handleFavoriteClick: userId=$userId")
        if (userId == -1L) return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val request = FavoriteRequest(userId, parkingLot.id)
                if (isCurrentlyFavorite) {
                    Log.d("FavoriteTest", "handleFavoriteClick: 즐겨찾기 삭제 시도")
                    val response = RetrofitClient.getApiService(this@MainActivity).removeFavorite(request)
                    if (response.isSuccessful) {
                        favoriteParkingLotIds.remove(parkingLot.id)
                        Log.d("FavoriteTest", "handleFavoriteClick: 즐겨찾기 삭제 성공")
                        Toast.makeText(this@MainActivity, "즐겨찾기에서 삭제했습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("FavoriteTest", "handleFavoriteClick: 즐겨찾기 삭제 실패")
                        Toast.makeText(this@MainActivity, "즐겨찾기 삭제 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("FavoriteTest", "handleFavoriteClick: 즐겨찾기 추가 시도")
                    val response = RetrofitClient.getApiService(this@MainActivity).addFavorite(request)
                    if (response.isSuccessful) {
                        favoriteParkingLotIds.add(parkingLot.id)
                        Log.d("FavoriteTest", "handleFavoriteClick: 즐겨찾기 추가 성공")
                        Toast.makeText(this@MainActivity, "즐겨찾기에 추가했습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("FavoriteTest", "handleFavoriteClick: 즐겨찾기 추가 실패, code=${response.code()}, error=${response.errorBody()?.string()}")
                        Toast.makeText(this@MainActivity, "즐겨찾기 추가 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                parkingLotResultAdapter.notifyDataSetChanged() // 아이콘 업데이트
            } catch (e: Exception) {
                Log.e("FavoriteTest", "handleFavoriteClick: 예외 발생", e)
                Toast.makeText(this@MainActivity, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchFavorites() {
        val userId = getUserId()
        if (userId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(this@MainActivity).getFavorites(userId)
                if (response.isSuccessful) {
                    val newFavoriteIds = response.body()?.map { it.parkingLot.id }?.toSet() ?: emptySet<Long>()
                    
                    withContext(Dispatchers.Main) {
                        if (newFavoriteIds != favoriteParkingLotIds) {
                            favoriteParkingLotIds.clear()
                            favoriteParkingLotIds.addAll(newFavoriteIds)
                            if (::mMap.isInitialized) addMarkersToMap(parkingLotList)
                            if (::parkingLotResultAdapter.isInitialized) parkingLotResultAdapter.updateFavorites(favoriteParkingLotIds)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FetchFavorites", "Failed to fetch favorites", e)
            }
        }
    }

    private fun getUserId(): Long {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getLong("user_id", -1L) // -1 or some other invalid value
    }

    // onResume: 화면이 다시 활성화될 때마다 호출
    override fun onResume() {
        super.onResume()
        updateSideMenuUserInfo()
        updateLoginButtonUI()
        val cachedMarkers = loadMarkersFromPrefs()
        if (cachedMarkers.isNotEmpty() && ::mMap.isInitialized) {
            addMarkersToMap(cachedMarkers)
        }
        if (::parkingLotResultAdapter.isInitialized) {
            parkingLotResultAdapter.updateFavorites(
                if (isLoggedIn()) favoriteParkingLotIds else emptySet<Long>()
            )
        }
        if(::fullAdapter.isInitialized) {
            updateFullSearchUI()
        }
        updateAdminUI()
        // 현재 언어 텍스트도 항상 갱신
        val lang = getSharedPreferences("settings", MODE_PRIVATE).getString("lang", "ko")
        val langName = when(lang) {
            "en" -> getString(R.string.english)
            else -> getString(R.string.korean)
        }
        findViewById<TextView>(R.id.tvCurrentLanguage)?.text = getString(R.string.current_language, langName)
    }

    private fun updateAdminUI() {
        val isAdmin = isAdmin()
        val adminLabel = findViewById<TextView?>(R.id.tvAdminLabel)
        val myInfo = findViewById<TextView>(R.id.tvMyInfo)
        val settings = findViewById<TextView>(R.id.tvSettings)
        val currentLang = findViewById<TextView>(R.id.tvCurrentLanguage)
        val cs = findViewById<TextView>(R.id.tvCs)
        
        if (isAdmin) {
            adminLabel?.visibility = View.VISIBLE
            myInfo.visibility = View.GONE
            settings.visibility = View.GONE
            currentLang.visibility = View.GONE
            cs.visibility = View.VISIBLE
        } else {
            adminLabel?.visibility = View.GONE
            myInfo.visibility = View.VISIBLE
            settings.visibility = View.VISIBLE
            currentLang.visibility = View.VISIBLE
            cs.visibility = View.VISIBLE
        }
    }

    private fun isAdmin(): Boolean {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val email = prefs.getString("email", "")
        // 관리자 이메일을 하드코딩하거나, 서버에서 권한 정보를 받아와도 됨
        return email == "admin@parq.com" // 예시 관리자 계정
    }

    // onResume, handleLoginButtonClick 등에서 이 함수를 호출
    private fun addMarkersToMap(parkingLots: List<ParkingLotResponse>) {
        if (!::mMap.isInitialized) return
        mMap.clear()

        parkingLots.forEach { parkingLot ->
            val markerLocation = LatLng(parkingLot.latitude, parkingLot.longitude)
            val isFavorite = favoriteParkingLotIds.contains(parkingLot.id)

            val markerView = LayoutInflater.from(this).inflate(R.layout.marker_layout, null)
            val tvParkingName = markerView.findViewById<TextView>(R.id.marker_text)

            tvParkingName.text = parkingLot.name

            val markerOptions = MarkerOptions()
                .position(markerLocation)
                .icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerView)))
                .anchor(0.5f, 1.0f)

            mMap.addMarker(markerOptions)?.tag = parkingLot
        }
    }

    // View를 Bitmap으로 변환하는 헬퍼 함수
    private fun createBitmapFromView(view: View): Bitmap {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        return bitmap
    }

    // 언어 변경 함수
    private fun setLocale(language: String) {
        val locale = java.util.Locale(language)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        // 선택 언어 저장
        getSharedPreferences("settings", MODE_PRIVATE).edit().putString("lang", language).apply()
        // 앱 재시작 (플래그 추가)
        val intent = intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        finish()
        startActivity(intent)
    }

    // 마커 캐시 저장
    private fun saveMarkersToPrefs(markerList: List<ParkingLotResponse>) {
        val prefs = getSharedPreferences("marker_cache", MODE_PRIVATE)
        val json = Gson().toJson(markerList)
        prefs.edit().putString("marker_list", json).apply()
    }

    // 마커 캐시 불러오기
    private fun loadMarkersFromPrefs(): List<ParkingLotResponse> {
        val prefs = getSharedPreferences("marker_cache", MODE_PRIVATE)
        val json = prefs.getString("marker_list", null)
        return if (json != null) {
            Gson().fromJson(json, object : TypeToken<List<ParkingLotResponse>>() {}.type)
        } else {
            emptyList()
        }
    }

    private fun showLoginRequiredToast() {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.toast_login_required, null)
        val toast = Toast(this)
        toast.view = layout
        toast.duration = Toast.LENGTH_SHORT
        toast.show()
    }

    override fun attachBaseContext(newBase: android.content.Context?) {
        if (newBase == null) {
            super.attachBaseContext(null)
            return
        }
        val prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("lang", null)
        if (lang != null) {
            val locale = java.util.Locale(lang)
            java.util.Locale.setDefault(locale)
            val config = android.content.res.Configuration(newBase.resources.configuration)
            config.setLocale(locale)
            val context = newBase.createConfigurationContext(config)
            super.attachBaseContext(context)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    // prk_center_id의 뒷부분(고유번호)만 추출하는 함수 추가
    private fun extractKey(prkCenterId: String?): String? {
        if (prkCenterId == null) return null
        val parts = prkCenterId.split("-")
        return if (parts.size >= 3) parts.takeLast(3).joinToString("-") else prkCenterId
    }

    override fun onBackPressed() {
        // 1. 전체검색(검색창) 열려있으면 닫기
        if (searchFullLayout.visibility == View.VISIBLE) {
            closeSearchFullScreen()
            return
        }
        // 2. 사이드 메뉴(PQ) 열려있으면 닫기
        if (sideMenuLayout.visibility == View.VISIBLE) {
            closeSideMenu()
            return
        }
        // 3. 그 외에는 종료 다이얼로그
        val builder = AlertDialog.Builder(this)
        builder.setTitle("앱 종료")
            .setMessage("종료하시겠습니까?")
            .setPositiveButton("네") { dialog, _ ->
                dialog.dismiss()
                finishAffinity() // 앱 완전 종료
            }
            .setNegativeButton("아니요") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 마커 데이터 저장
        val json = Gson().toJson(parkingLotList)
        outState.putString("parking_lot_list_json", json)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // 마커 데이터 복원
        val json = savedInstanceState.getString("parking_lot_list_json", null)
        if (json != null) {
            val type = object : com.google.gson.reflect.TypeToken<List<ParkingLotResponse>>() {}.type
            parkingLotList = Gson().fromJson(json, type)
        }
    }
}