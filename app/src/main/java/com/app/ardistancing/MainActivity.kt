package com.app.ardistancing

import android.graphics.Point
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.ArFragment


class MainActivity : AppCompatActivity() {
    private var fragment: ArFragment? = null

    private val pointer = PointerDrawable()
    private var isTracking = false
    private var isHitting = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment
        fragment!!.arSceneView.scene
            .addOnUpdateListener { frameTime: FrameTime? ->
                fragment!!.onUpdate(frameTime)
                onUpdate()
            }
    }

    private fun onUpdate() {
        val trackingChanged: Boolean = updateTracking()
        val contentView: View = findViewById(android.R.id.content)
        if (trackingChanged) {
            if (isTracking) {
                contentView.getOverlay().add(pointer)
            } else {
                contentView.getOverlay().remove(pointer)
            }
            contentView.invalidate()
        }
        if (isTracking) {
            val hitTestChanged: Boolean = updateHitTest()
            if (hitTestChanged) {
                pointer.isEnabled = isHitting
                contentView.invalidate()
            }
        }
    }

    private fun updateTracking(): Boolean {
        val frame: Frame? = fragment!!.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame != null &&
                frame.getCamera().getTrackingState() === TrackingState.TRACKING
        return isTracking != wasTracking
    }

    private fun updateHitTest(): Boolean {
        val frame: Frame? = fragment!!.arSceneView.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        val wasHitting = isHitting
        isHitting = false
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane &&
                    (trackable as Plane).isPoseInPolygon(hit.hitPose)
                ) {
                    isHitting = true
                    break
                }
            }
        }
        return wasHitting != isHitting
    }

    private fun getScreenCenter(): Point {
        val vw: View = findViewById(android.R.id.content)
        return Point(vw.getWidth() / 2, vw.getHeight() / 2)
    }
}