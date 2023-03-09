package com.example.sbapec

import MyWebViewClient
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sbapec.databinding.ActivityScannerBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException

val REQUEST_CAMERA_PERMISSION = 201
class ScannerActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityScannerBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    private var barcodeData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupView()
    }

    private fun setupView() {

        mBinding.toolbarScanner.imgBack.visibility = View.VISIBLE
        mBinding.toolbarScanner.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val bundle: Bundle? = intent.extras
        val screen = bundle!!.getString(SCREEN_KEY)
        if (screen == SCANNER_KEY) {
            mBinding.toolbarScanner.tvTitle.text = getString(R.string.scan_qr_code)
            initialiseDetectorsAndSources()
            mBinding.surfaceView.visibility = View.VISIBLE
            mBinding.webView.visibility = View.GONE
            mBinding.pbLoader.visibility = View.GONE
        } else if (screen == WEBVIEW_KEY) {
            mBinding.toolbarScanner.tvTitle.text = getString(R.string.verification_status)

            mBinding.surfaceView.visibility = View.GONE
            mBinding.webView.visibility = View.VISIBLE
            mBinding.pbLoader.visibility = View.VISIBLE

            val webUrl = bundle.getString(WEB_KEY)
            mBinding.webView.webViewClient = MyWebViewClient(this, mBinding.pbLoader)
            mBinding.webView.loadUrl(webUrl ?: getString(R.string.demo_url))
        }
    }

    private fun initialiseDetectorsAndSources() {

        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()
        mBinding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int,
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() > 0) {
                    barcodeData = barcodes.valueAt(0).displayValue
                    val intent = Intent()
                    intent.putExtra(WEB_KEY, barcodeData)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        cameraSource.release()
    }

    override fun onResume() {
        super.onResume()
        initialiseDetectorsAndSources()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.stop()
    }

}