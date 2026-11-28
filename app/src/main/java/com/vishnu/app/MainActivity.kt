package com.vishnu.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private val homeUrl = "https://vishnu-two-bice.vercel.app/"
    private val appHost = "vishnu-two-bice.vercel.app"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.rgb(7, 5, 12)
        window.navigationBarColor = android.graphics.Color.rgb(7, 5, 12)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false

        webView = WebView(this).apply {
            setBackgroundColor(android.graphics.Color.rgb(7, 5, 12))
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                loadsImagesAutomatically = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
                useWideViewPort = true
                loadWithOverviewMode = true
                builtInZoomControls = false
                displayZoomControls = false
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(true)
                userAgentString = userAgentString + " VISHNUAndroid/2.0"
            }
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    return routeUrl(request.url)
                }

                @Deprecated("Deprecated in Android API")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    return routeUrl(Uri.parse(url))
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, progress: Int) {
                    super.onProgressChanged(view, progress)
                }
            }

            setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                try {
                    val request = DownloadManager.Request(Uri.parse(url))
                    val cookies = CookieManager.getInstance().getCookie(url)
                    if (!cookies.isNullOrBlank()) request.addRequestHeader("Cookie", cookies)
                    request.addRequestHeader("User-Agent", userAgent)
                    request.setMimeType(mimeType)
                    request.setTitle(URLUtilCompat.fileName(contentDisposition, url))
                    request.setDescription("Downloading with VISHNU")
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        URLUtilCompat.fileName(contentDisposition, url)
                    )
                    val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    manager.enqueue(request)
                    Toast.makeText(this@MainActivity, "Download started", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    Toast.makeText(this@MainActivity, "Unable to start download", Toast.LENGTH_LONG).show()
                }
            })
        }

        setContentView(webView)
        if (savedInstanceState == null) webView.loadUrl(homeUrl) else webView.restoreState(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    private fun routeUrl(uri: Uri): Boolean {
        val scheme = uri.scheme ?: return true
        if (scheme == "http" || scheme == "https") {
            if (uri.host == appHost || uri.host?.endsWith("vishnu-two-bice.vercel.app") == true) return false
            // Keep media frames and Firebase/TMDB resources inside the website; only top-level
            // navigation reaches this method. Open unrelated external pages in the browser.
            return try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
                true
            } catch (_: Exception) {
                true
            }
        }
        return try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        } catch (_: Exception) {
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.removeAllViews()
            webView.destroy()
        }
        super.onDestroy()
    }
}

private object URLUtilCompat {
    fun fileName(contentDisposition: String?, url: String): String {
        val fromHeader = contentDisposition
            ?.substringAfter("filename=", "")
            ?.trim()
            ?.trim('"')
            ?.takeIf { it.isNotBlank() }
        val fromUrl = Uri.parse(url).lastPathSegment?.takeIf { it.isNotBlank() }
        return (fromHeader ?: fromUrl ?: "vishnu-download").replace(Regex("[^A-Za-z0-9._ -]"), "_")
    }
}
