package com.example.mappsample

import android.annotation.SuppressLint
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

// MAPP SAMPLE APP: INTENTIONALLY INSECURE demo material for the MAPP Scan scanner.
// Never ship this code. Each "MAPP demo:" comment names the rule it is meant to trigger.
class DeepLinkActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        val settings = webView.settings

        // MAPP demo: DEX-WEBVIEW-JS-ENABLED
        settings.javaScriptEnabled = true
        // MAPP demo: DEX-WEBVIEW-FILE-ACCESS
        settings.allowFileAccess = true
        settings.allowUniversalAccessFromFileURLs = true

        // MAPP demo: DEX-WEBVIEW-JS-INTERFACE
        webView.addJavascriptInterface(DemoBridge(), "Android")

        webView.webViewClient = object : WebViewClient() {
            // MAPP demo: DEX-TRUST-ALL-TLS (proceeds on every certificate error)
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()
            }
        }
        setContentView(webView)

        // MAPP demo: DEX-INTENT-EXTRA-TO-WEBVIEW (URL comes straight from the intent)
        val url = intent?.data?.toString()
            ?: intent?.getStringExtra("url")
            ?: "http://demo.example.com/"
        webView.loadUrl(url)

        // MAPP demo: DEX-INTENT-REDIRECT (starts an Intent supplied by the caller)
        val next = intent?.getParcelableExtra<Intent>("next")
        if (next != null) {
            startActivity(next)
        }
    }
}

class DemoBridge {
    @JavascriptInterface
    fun getToken(): String {
        return "demo-token-not-real"
    }
}
