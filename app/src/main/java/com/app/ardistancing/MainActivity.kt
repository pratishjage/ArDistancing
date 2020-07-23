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

    private val pointer = PointerDrawable()
    private var isTracking = false
    private var isHitting = false
    var count = 0;
    var andyRenderable: ModelRenderable?=null
    private val placedAnchorNodes = ArrayList<AnchorNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        fragment.arSceneView.scene
            .addOnUpdateListener { frameTime: FrameTime? ->
                fragment.onUpdate(frameTime)
                onUpdate()

                /*val frame: Frame? = fragment.arSceneView.getArFrame()
                if (frame == null) {
                    return@addOnUpdateListener
                }

                for (plane in frame.getUpdatedTrackables(
                    Plane::class.java
                )) {
                    if (plane.trackingState == TrackingState.TRACKING) {
                        if (count == 0) {
                            count++;
                            updateAnchorNode()
                        }
                    }
                }*/
            }
        /* fragment.setOnTapArPlaneListener(
             { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
                 if (andyRenderable == null) {
                     return@setOnTapArPlaneListener
                 }
                 val modelRenderable: ModelRenderable = andyRenderable.makeCopy()

                 // Create the Anchor.
                 val anchor = hitResult.createAnchor()
                 val anchorNode =
                     AnchorNode(anchor)
                 anchorNode.setParent(fragment.getArSceneView().getScene())
                 val node = Node()


                 // Create the transformable andy and add it to the anchor.
                 val andy =
                     TransformableNode(fragment.getTransformationSystem())
                 andy.setParent(anchorNode)
                 andy.renderable = andyRenderable
                 andy.select()
             })*/
    }

    private fun onUpdate() {
        val trackingChanged: Boolean = updateTracking()
        val contentView: View = findViewById(android.R.id.content)
        if (trackingChanged) {
            //clear()
            updateAnchorNode()
        }
        if (isTracking) {
            val hitTestChanged: Boolean = updateHitTest()
            if (hitTestChanged) {

              //  clear()
                /* pointer.isEnabled = isHitting
                 contentView.invalidate()*/
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
        /*  ModelRenderable.builder().setSource(
              this,
              Uri.parse("https://firebasestorage.googleapis.com/v0/b/pf-asset-holder.appspot.com/o/8nov19%2Fmodel_3.glb?alt=media&token=baef2962-5ebc-4d17-bdb3-ed99e16da428")
          ).setRegistryId(123)
              .build()
              .thenAccept { modelRenderable: ModelRenderable? ->
                  loadModel(modelRenderable, anchor)
              }.exceptionally { exception ->
                  Toast.makeText(this, "something wrong", Toast.LENGTH_SHORT).show()
                  return@exceptionally null
              }*/

        if (andyRenderable != null) {
           // loadModel(andyRenderable, anchor)
            return
        }
        val url =
            "https://firebasestorage.googleapis.com/v0/b/ilead-d2f48.appspot.com/o/ringwhite.glb?alt=media&token=79524ef4-8f01-4af7-b5ab-277a4de00d57";
        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
            //.setSource(this, R.raw.andy)
            .setSource(
                this, RenderableSource.builder().setSource(
                    this,
                    Uri.parse(url),
                    RenderableSource.SourceType.GLB
                )
                    //.setScale(0.0254f)
                    // Scale the original model to 50%.
                  // .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .build()
            )
            .setRegistryId(url)
            .build()
            .thenAccept { modelRenderable: ModelRenderable? ->
                Toast.makeText(this, "something Correct", Toast.LENGTH_SHORT).show()

                andyRenderable = modelRenderable!!
                loadModel(modelRenderable, anchor)
            }.exceptionally { exception ->
                Toast.makeText(this, "something wrong", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun updateAnchorNode() {
        Toast.makeText(this, "updateAnchorNode Correct", Toast.LENGTH_SHORT).show()
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
        /* val pose = fragment.getArSceneView().arFrame!!.camera.pose
         var cur: Vector3=Vector3()
         pose?.let {
             cur = Vector3(pose.tx(), pose.ty(), pose.tz())
         }
         anchorNode.worldPosition = cur*/
        // Create the transformable model and add it to the anchor.

        // Create the transformable model and add it to the anchor.
        val model = TransformableNode(fragment.getTransformationSystem())
        model.setParent(anchorNode)
        model.renderable = modelRenderable
        model.select()
    }

    private fun clear() {
        for (anchorNode in placedAnchorNodes) {
            fragment.arSceneView.scene.removeChild(anchorNode)
            anchorNode.isEnabled = false
            anchorNode.anchor!!.detach()
            anchorNode.setParent(null)

          /*  val pose = fragment.getArSceneView().arFrame!!.camera.pose
            var cur: Vector3=Vector3()
            pose?.let {
                cur = Vector3(pose.tx(), pose.ty(), pose.tz())
            }
            anchorNode.worldPosition = cur*/
        }
    }
}