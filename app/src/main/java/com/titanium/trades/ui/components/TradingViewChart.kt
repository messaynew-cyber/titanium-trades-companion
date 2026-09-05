package com.titanium.trades.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AColor
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.titanium.trades.ui.theme.IsDark
import java.net.URLEncoder

/**
 * Live candlestick chart via TradingView's free embed endpoint, hosted in a
 * transparent WebView. Keyed by symbol+interval+theme so Compose only reloads
 * the widget when one of those actually changes (not on every recomposition).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TradingViewChart(
    symbol: String,
    interval: String = "60",   // 1,5,15,60,240,D,W
    modifier: Modifier = Modifier
) {
    val theme = if (IsDark) "dark" else "light"
    val safe = when {
        symbol.isBlank() -> "BTCUSD"
        symbol.contains(".") || symbol.contains(":") -> symbol
        else -> symbol
    }
    val mode = remember(safe, interval, theme) { "$safe|$interval|$theme" }
    val url = remember(mode) { buildEmbedUrl(safe, interval, theme) }

    AndroidView(
        modifier = modifier.fillMaxWidth().height(340.dp),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(AColor.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                tag = "tv_$mode"
            }
        },
        update = { wv ->
            if (wv.tag != "tv_$mode") {
                wv.tag = "tv_$mode"
                wv.loadUrl(url)
            }
        }
    )
}

/** Build a deterministic TradingView widgetembed URL for a symbol+interval+theme. */
internal fun buildEmbedUrl(symbol: String, interval: String, theme: String): String {
    val border = if (theme == "dark") "1a1610" else "dcd4c2"
    val bg = if (theme == "dark") "0a0a0a" else "ffffff"
    val sym = URLEncoder.encode(symbol, "UTF-8")
    return "https://s.tradingview.com/widgetembed/?frameElementId=tt_${Math.abs(symbol.hashCode())}" +
            "&symbol=$sym&interval=$interval&hidesidetoolbar=1&hidelegend=0" +
            "&symboledit=1&saveimage=0&toolbarbg=rgba(0,0,0,0)" +
            "&theme=$theme&style=1&timezone=Africa%2FAddis_Ababa" +
            "&withdateranges=1&hidevolume=0&hide_side_toolbar=0" +
            "&bordercolor=%23$border&background_color=$bg" +
            "&underMobile=0&locale=en"
}
