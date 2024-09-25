package com.example.sbapec

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sbapec.databinding.ActivityHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


val SCREEN_KEY = "screen"
val SCANNER_KEY = "scanner"
val WEB_KEY = "webUrl"
val WEBVIEW_KEY = "webview"

class HomeActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityHomeBinding
    private lateinit var latitude: String
    private lateinit var longitude: String

    val LOCATION_PERMISSIONS = mutableListOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ).toTypedArray()
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setupView()
    }

    private fun setupView() {
        mBinding.toolbarHome.tvTitle.text = getString(R.string.pec_sba_qr_scanner)
        mBinding.imgQr.setOnClickListener {
            checkCameraPermission()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()


        mBinding.tvHelpline.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:042–99260156")
            startActivity(intent)
        }
        mBinding.tvWhatsapp.setOnClickListener {
            val installed = appInstalledOrNot("com.whatsapp")
            if (installed) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data =
                    Uri.parse("http://api.whatsapp.com/send?phone=03334045115")
                startActivity(intent)
            } else {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:0333-4045115")
                startActivity(intent)
            }
        }
    }

    private fun checkLocationPermission() {
        if (!isLocationPermGranted()) askForLocationPermission()
        else getLastLocation { lat, lng ->
            latitude = lat
            longitude = lng
            Log.d("HomeActivity", "checkLocationPermission: $lat $lng")
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this@HomeActivity, android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            openScannerActivityForResult()
        }
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@HomeActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openScannerActivityForResult()
            } else {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openScannerActivityForResult() {
        val intent = Intent(this, ScannerActivity::class.java)
        intent.putExtra(SCREEN_KEY, SCANNER_KEY)
        resultLauncher.launch(intent)
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val webUrl = data?.getStringExtra(WEB_KEY)
                if (::latitude.isInitialized && ::longitude.isInitialized)
                    showAlertDialog("$webUrl/$latitude/$longitude")
                else {
                    Toast.makeText(this, "Unable to fetch location.", Toast.LENGTH_SHORT).show()
                    showAlertDialog(webUrl)
                }
            }
        }

    private fun showAlertDialog(webUrl: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.open_url_title))
        builder.setMessage(getString(R.string.open_url_msg, webUrl))
//        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(getString(R.string.yes)) { dialog, which ->
            openWebView(webUrl ?: getString(R.string.demo_url))
        }

        builder.setNegativeButton(getString(R.string.no)) { dialog, which ->

        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()

        // Set other dialog properties
        alertDialog.setCancelable(false)

        alertDialog.show()
    }

    private fun appInstalledOrNot(url: String): Boolean {
        val packageManager = packageManager
        val appInstalled: Boolean = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else
                packageManager.getPackageInfo(url, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return appInstalled
    }

    private fun openWebView(barcodeData: String) {
        val webview = Intent(this, ScannerActivity::class.java)
        webview.putExtra(SCREEN_KEY, WEBVIEW_KEY)
        webview.putExtra(WEB_KEY, barcodeData)
        startActivity(webview)
    }

    private fun isLocationPermGranted(): Boolean {
        var isGranted = true
        LOCATION_PERMISSIONS.forEach {
            if (ContextCompat.checkSelfPermission(
                    this, it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                isGranted = false
                return@forEach
            }

        }
        return isGranted
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: java.lang.Exception) {
            Log.d("AppUtils", "isLocationEnabled: ${ex.message}")
        }
        return if (!gpsEnabled) {
            val locationManager: LocationManager =
                getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } else gpsEnabled
    }

    private fun askForLocationPermission(
    ) {
        LOCATION_PERMISSIONS.forEach {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
                return@forEach
            }
        }
        locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isGranted = true
            LOCATION_PERMISSIONS.forEach {
                if (permissions[it] != true) {
                    isGranted = false
                    return@forEach
                }
            }
            if (!isGranted) {
                Log.d(
                    "ComplainantFormFragment", "init: Location permission not granted"
                )
            } else {
                getLastLocation { lat, lng ->
                    latitude = lat
                    longitude = lng
                    Log.d("HomeActivity", "$lat $lng")
                }
            }
        }

    @SuppressLint("MissingPermission")
    fun getLastLocation(
        location: (lat: String, lng: String) -> Unit
    ) {
        if (isLocationPermGranted()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData(mFusedLocationClient)
                    } else {
                        location(location.latitude.toString(), location.longitude.toString())
                    }
                }
            } else openEnableLocationPage()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(
        mFusedLocationClient: FusedLocationProviderClient
    ) {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback, Looper.myLooper()
        )
    }

    private fun openEnableLocationPage() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                val mLastLocation: Location = it
                var lat = mLastLocation.latitude.toString()
                var lng = mLastLocation.longitude.toString()
            }
        }
    }
}