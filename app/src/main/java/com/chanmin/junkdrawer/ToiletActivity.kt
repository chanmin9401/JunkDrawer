package com.chanmin.junkdrawer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.chanmin.junkdrawer.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_find_toilet.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class ToiletActivity : AppCompatActivity() {

    //런타임에서 권한이 필요한 퍼미션 목록
    val PERMISSION = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    // 서울시 발급 인증키
    // 7451726e6f72656439334557575457
    //퍼미션 승인 요청시 사용되는 코드
    val REQUEST_PERMISSION_CODE = 1

    //기본 앱붐 레벨
    val DEFAULT_ZOOM_LEVEL = 17f

    //현재위치를 가져올수 없는 경우 서울 시청의 위로지 지도를 보여주기
    //latLng 클래스는 위도와 경도를 가지는 클래스
    val CITY_HALL = LatLng(37.5662952, 126.97794509999994)

    //구글 맵 객체 참조
    var googleMap: GoogleMap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_toilet)

        //앱뷰에 onCreate 함수호출
        mapView.onCreate(savedInstanceState)
        val myLocationBtn = findViewById<FloatingActionButton>(R.id.myLocationButton)

        //앱이 실행될때 런타임에 위치 권한 체크
        if (hasPermissions()){
            initMap()
        }else{
            ActivityCompat.requestPermissions(this,PERMISSION,REQUEST_PERMISSION_CODE)
        }

        myLocationBtn.setOnClickListener { onMyLocationButtonClick() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //앱 초기화
        initMap()
    }

    //앱에서 사용하는 권이 있는 지체크
    fun hasPermissions(): Boolean{

        //퍼미션목록중에 하나라도 권한이 없으면 false
        for (permission in PERMISSION){
            if (ActivityCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

    //앱초기화
    @SuppressLint("MissingPermission")
    fun initMap(){
        mapView.getMapAsync{

            // 구글맵 멤버 변수에 구글맵 객체 저장
            googleMap = it
            // 현재위치로 이동 버튼 비활성화
            it.uiSettings.isMyLocationButtonEnabled = false
            // 위치 사용 권한이 있는 경우
            when {
                hasPermissions() -> {
                    // 현재위치 표시 활성화
                    it.isMyLocationEnabled = true
                    // 현재위치로 카메라 이동
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(), DEFAULT_ZOOM_LEVEL))
                }
                else -> {
                    // 권한이 없으면 서울시청의 위치로 이동
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(CITY_HALL, DEFAULT_ZOOM_LEVEL))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getMyLocation(): LatLng {

        val locationProvider: String =  LocationManager.GPS_PROVIDER

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val lastKnownLocation: Location = locationManager.getLastKnownLocation(locationProvider)
        return LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
    }

    //현재위치 버튼 클릭
    fun onMyLocationButtonClick(){

        when{
            hasPermissions() -> googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(),DEFAULT_ZOOM_LEVEL))
            else -> Toast.makeText(applicationContext, "위치사용권한 설정에 동의 햐.",Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    val API_KEY = "7451726e6f72656439334557575457"

    var task: ToiletReadTask? = null

    var toilets = JSONArray()

    val bitmap by lazy {
        val drawable = resources.getDrawable(R.drawable.restroom_sign) as BitmapDrawable
        Bitmap.createScaledBitmap(drawable.bitmap, 64,64,false)
    }

    //jsonArray 를 병합히기
    fun JSONArray.merge(anotherArray: JSONArray){
        for (i in 0 until anotherArray.length()){
            this.put(anotherArray.get(i))
        }
    }

    fun readData(startIndex: Int, lastIndex: Int): JSONObject {
        val url =
            URL("http://openAPI.seoul.go.kr:8088"+"/${API_KEY}/json/SearchPublicToiletPOIService/${startIndex}/${lastIndex}")

        val connection = url.openConnection()

        val data = connection.getInputStream().readBytes().toString(charset("UTF-8"))

        return JSONObject(data)
    }

    inner class ToiletReadTask : AsyncTask<Void, JSONArray, String>(){
        override fun onPreExecute() {
            googleMap?.clear()

            toilets = JSONArray()
        }

        override fun doInBackground(vararg p0: Void?): String {
            val  step = 1000
            var startIndex = 1
            var lastIndex = step
            var totalCount = 0

            do {
                if (isCancelled) break

                if (totalCount != 0){
                    startIndex += step
                    lastIndex += step
                }

                val jsonObject = readData(startIndex, lastIndex)

                totalCount = jsonObject.getJSONObject("SearchPublicToiletPOIService").getInt("list_total_count")

                val rows = jsonObject.getJSONObject("SearchPublicToiletPOIService").getJSONArray("row")

                toilets.merge(rows)

                publishProgress(rows)

            }while (lastIndex < totalCount)

            return "complete"
        }

        override fun onProgressUpdate(vararg values: JSONArray?) {
            //super.onProgressUpdate(*values)
            val array = values[0]
            array?.let {
                for (i in 0 until array.length()){
                    addMarkers(array.getJSONObject(i))
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        task?.cancel(true)
        task = ToiletReadTask()
        task?.execute()
    }

    override fun onStop() {
        super.onStop()
        task?.cancel(true)
        task = null
    }
    fun addMarkers(toilet: JSONObject){
        googleMap?.addMarker(
            MarkerOptions()
                .position(LatLng(toilet.getDouble("Y_WGS84"),toilet.getDouble("X_WGS84")))
                .title(toilet.getString("FNAME"))
                .snippet(toilet.getString("ANAME"))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()

        startActivity(Intent(this,MainActivity::class.java))
    }

}
