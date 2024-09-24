package com.example.sbapec

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sbapec.databinding.ActivityHomeBinding


val SCREEN_KEY = "screen"
val SCANNER_KEY = "scanner"
val WEB_KEY = "webUrl"
val WEBVIEW_KEY = "webview"

class HomeActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityHomeBinding

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

        mBinding.tvHelpline.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + getString(R.string.helpline))
            startActivity(intent)
        }
        mBinding.tvWhatsapp.setOnClickListener {
            val installed = appInstalledOrNot("com.whatsapp")
            if (installed) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data =
                    Uri.parse("http://api.whatsapp.com/send?phone=" + getString(R.string.whatsapp))
                startActivity(intent)
            } else {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:" + getString(R.string.whatsapp))
                startActivity(intent)
            }
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
                showAlertDialog(getString(R.string.base_url) + webUrl)
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
}