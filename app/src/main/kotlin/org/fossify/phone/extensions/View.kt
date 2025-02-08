package org.fossify.phone.extensions

import android.graphics.BlurMaskFilter
import android.graphics.Rect
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.widget.TextView
import org.fossify.phone.helpers.Config

val View.boundingBox
    get() = Rect().also { getGlobalVisibleRect(it) }

fun getBlurNumber() = "**********"

fun TextView.applyBlurEffect(config: Config) {
    if (!config.hidePhoneNumber) {
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ (API 31+): Use RenderEffect
        val blurEffect = RenderEffect.createBlurEffect(25f, 25f, Shader.TileMode.CLAMP)
        this.setRenderEffect(blurEffect)
    } else {
        // Older Android Versions: Use BlurMaskFilter
        val paint = this.paint
        paint.maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
        this.setLayerType(TextView.LAYER_TYPE_SOFTWARE, paint)
    }
}

fun TextView.removeBlurEffect() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.setRenderEffect(null) // Remove RenderEffect
    } else {
        val paint = this.paint
        paint.maskFilter = null // Remove BlurMaskFilter
        this.setLayerType(TextView.LAYER_TYPE_NONE, paint)
    }
}
