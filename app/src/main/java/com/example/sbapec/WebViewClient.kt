import android.app.Activity
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast

class MyWebViewClient(private val activity: Activity, private val loader: ProgressBar): WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?,
    ): Boolean {
        val url: String = request?.url.toString();
        view?.loadUrl(url)
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
        webView.loadUrl(url)
        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        loader.visibility = View.GONE
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError,
    ) {
        loader.visibility = View.GONE
        Toast.makeText(activity, "Got Error! $error", Toast.LENGTH_SHORT).show()
    }

}