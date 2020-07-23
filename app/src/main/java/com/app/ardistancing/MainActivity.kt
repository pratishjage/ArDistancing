package com.app.ardistancing

import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class MainActivity : AppCompatActivity() {
    private lateinit var fragment: ArFragment
    private var isTracking = false
    private var isHitting = false
    private var andyRenderable: ModelRenderable? = null
    private val placedAnchorNodes = ArrayList<AnchorNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        fragment.arSceneView.scene
            .addOnUpdateListener { frameTime: FrameTime? ->
                fragment.onUpdate(frameTime)
                onUpdate()
            }

    }

    private fun onUpdate() {
        val trackingChanged: Boolean = updateTracking()
        if (trackingChanged) {
            clear()
            updateAnchorNode()
        }
        if (isTracking) {
            val hitTestChanged: Boolean = updateHitTest()
            if (hitTestChanged) {
                clear()
                updateAnchorNode()
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
        return Point(vw.getWidth() / 2, vw.width)
    }

    private fun createRing(anchor: Anchor) {
        if (andyRenderable != null) {
            loadModel(andyRenderable, anchor)
            return
        }
        val url =
            "https://firebasestorage.googleapis.com/v0/b/ilead-d2f48.appspot.com/o/yellowring.glb?alt=media&token=4e600211-74ab-4a73-992e-ad88838ad986";

        ModelRenderable.builder()
            //.setSource(this, R.raw.andy)
            .setSource(
                this, RenderableSource.builder().setSource(
                    this,
                    Uri.parse(url),
                    RenderableSource.SourceType.GLB
                ).build()
            )
            .setRegistryId(url)
            .build()
            .thenAccept { modelRenderable: ModelRenderable? ->
                andyRenderable = modelRenderable!!
                loadModel(modelRenderable, anchor)
            }.exceptionally { exception ->
                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun updateAnchorNode() {
        val frame = fragment.arSceneView.arFrame
        val point = getScreenCenter()
        //val point = Point(0, 0)
        if (frame != null) {
            val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    createRing(hit.createAnchor())
                    break
                }
            }
        }
    }

    private fun loadModel(
        modelRenderable: ModelRenderable?,
        anchor: Anchor
    ) {

        // Create the Anchor.
        val anchorNode =
            AnchorNode(anchor)
        anchorNode.setParent(fragment.getArSceneView().getScene())
        placedAnchorNodes.add(anchorNode)
        val pose = fragment.getArSceneView().arFrame!!.camera.pose
        var cur: Vector3 = Vector3()
        val worldPosition = anchorNode.worldPosition
        pose?.let {
            cur = Vector3(pose.tx(), worldPosition.y, pose.tz())
        }
        val model = Node()
        model.setParent(anchorNode)
        model.renderable = modelRenderable
        model.worldPosition = cur
        // model.select()
    }

    private fun clear() {
        for (anchorNode in placedAnchorNodes) {
            fragment.arSceneView.scene.removeChild(anchorNode)
            anchorNode.isEnabled = false
            anchorNode.anchor!!.detach()
            anchorNode.setParent(null)
        }
    }
}