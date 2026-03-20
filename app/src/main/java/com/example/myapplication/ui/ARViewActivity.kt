package com.example.myapplication.ui

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CartRepository
import com.example.myapplication.databinding.ActivityArViewBinding
import com.example.myapplication.viewmodel.ARViewModel
import com.google.ar.core.Config
import com.google.ar.core.Plane
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch
import com.example.myapplication.R


class ARViewActivity : ComponentActivity() {

    private lateinit var binding: ActivityArViewBinding
    private lateinit var arViewModel: ARViewModel
    private var modelNode: ModelNode? = null
    private val anchorNodes = mutableListOf<AnchorNode>()
    
    private var productId: Int = -1
    private var arModelPath: String = ""
    private var lastTrackingState: com.google.ar.core.TrackingState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init ViewModel with CartRepository via Factory
        val db = AppDatabase.getDatabase(this)
        val cartRepo = CartRepository(db.cartDao())
        arViewModel = ViewModelProvider(this, ARViewModel.Factory(cartRepo))[ARViewModel::class.java]

        // Get Intent extras
        arModelPath = intent.getStringExtra("AR_MODEL") ?: ""
        val productName = intent.getStringExtra("PRODUCT_NAME") ?: "Product"
        productId = intent.getIntExtra("PRODUCT_ID", -1)

        binding.tvARProductName.text = productName

        setupUI()
        setupARSceneView()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnCloseAR.setOnClickListener { finish() }

        binding.btnResetModel.setOnClickListener { resetARScene() }

        // Scale buttons: step scale by ±20% each tap
        binding.btnScaleUp.setOnClickListener {
            modelNode?.let { it.scale = it.scale * 1.2f }
        }
        binding.btnScaleDown.setOnClickListener {
            modelNode?.let { it.scale = it.scale * 0.8f }
        }

        binding.btnAddToCart.setOnClickListener {
            if (productId != -1) arViewModel.addToCart(productId, 1)
        }
    }

    private fun setupARSceneView() {
        val sceneView = binding.arSceneView

        // Enable the light estimator – it is OFF by default in SceneView 2.3.3.
        // Without this, ENVIRONMENTAL_HDR session config has no effect on the
        // Filament renderer and models appear completely dark.
        sceneView.lightEstimator?.isEnabled = true
        sceneView.lightEstimator?.apply {
            environmentalHdrMainLightDirection = true  // correct sun direction
            environmentalHdrMainLightIntensity = true  // correct sun intensity
            environmentalHdrSphericalHarmonics = true  // ambient SH lighting
            environmentalHdrReflections        = true  // reflection cubemap
            // environmentalHdrSpecularFilter disabled: runs IBL prefilter every
            // frame which is the single biggest GPU cost — visually negligible on mobile
            environmentalHdrSpecularFilter     = false
        }

        // configureSession fires at the right moment for ENVIRONMENTAL_HDR in 2.3.3
        sceneView.configureSession { session, config ->
            config.planeFindingMode    = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            config.depthMode = when {
                session.isDepthModeSupported(Config.DepthMode.AUTOMATIC) -> Config.DepthMode.AUTOMATIC
                else -> Config.DepthMode.DISABLED
            }
        }

        sceneView.onSessionFailed = { exception ->
            arViewModel.onModelError("AR Session failed: ${exception.message}")
        }

        sceneView.onSessionCreated = {
            // Session ready, start loading model
            loadModel()
        }

        sceneView.onSessionUpdated = { _, frame ->
            // Only update LiveData when tracking state actually changes,
            // not on every frame (which fires 30-60x per second).
            val state = frame.camera.trackingState
            if (state != lastTrackingState) {
                lastTrackingState = state
                if (arViewModel.modelState.value == ARViewModel.ModelState.Loaded && arViewModel.isModelPlaced.value != true) {
                    val msg = if (state == com.google.ar.core.TrackingState.TRACKING)
                        "Tracking active. Point at a surface."
                    else
                        "Searching for surfaces..."
                    arViewModel.updateTrackingStatus(msg)
                }
            }
        }

    }

    // dispatchTouchEvent is used instead of setOnTouchListener so we can peek at
    // single-finger taps for placement WITHOUT ever consuming the event.
    // This keeps SceneView's multi-touch gesture chain (pinch/rotate/drag) fully intact.
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { event ->
            if (event.action == MotionEvent.ACTION_UP && event.pointerCount == 1) {
                if (arViewModel.modelState.value == ARViewModel.ModelState.Loaded &&
                    arViewModel.isModelPlaced.value != true) {
                    val hitResult = binding.arSceneView.frame
                        ?.hitTest(event)
                        ?.firstOrNull {
                            val trackable = it.trackable
                            trackable is Plane && trackable.isPoseInPolygon(it.hitPose)
                        }
                    if (hitResult != null) {
                        placeModel(hitResult.createAnchor())
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev) // always pass events through — never consume
    }

    private fun loadModel() {
        if (arModelPath.isEmpty()) {
            arViewModel.onModelError("No AR model available for this product.")
            return
        }

        arViewModel.onModelLoading()

        lifecycleScope.launch {
            try {
                // Ensure model instance is loaded correctly
                val modelInstance = binding.arSceneView.modelLoader.loadModelInstance(arModelPath)
                if (modelInstance != null) {
                    modelNode = ModelNode(
                        modelInstance = modelInstance,
                        autoAnimate = true,
                        scaleToUnits = 0.5f,
                        centerOrigin = Position(y = -0.5f)
                    ).apply {
                        isEditable = true
                        isScaleEditable    = true   // pinch-to-scale + Scale +/- buttons
                        isRotationEditable = true   // two-finger twist
                        isPositionEditable = false  // drag handled by AnchorNode along detected planes
                    }
                    arViewModel.onModelLoaded()
                } else {
                    arViewModel.onModelError("Failed to load model file.")
                }
            } catch (e: Exception) {
                arViewModel.onModelError("Error loading model: ${e.message}")
            }
        }
    }

    private fun placeModel(anchor: com.google.ar.core.Anchor) {
        val anchorNode = AnchorNode(binding.arSceneView.engine, anchor).apply {
            isEditable = true  // required so gestures (pinch/rotate/drag) pass to child ModelNode
        }

        // Attach model as child of the anchor node
        modelNode?.let { anchorNode.addChildNode(it) }

        binding.arSceneView.addChildNode(anchorNode)
        anchorNodes.add(anchorNode)

        arViewModel.onModelPlaced()
    }

    private fun resetARScene() {
        anchorNodes.forEach {
            it.destroy()
        }
        anchorNodes.clear()
        
        modelNode?.parent = null
        
        arViewModel.onModelReset()
        arViewModel.updateTrackingStatus("Move device to detect surfaces...")
    }

    private fun observeViewModel() {
        arViewModel.modelState.observe(this) { state ->
            when (state) {
                is ARViewModel.ModelState.Loading -> {
                    binding.layoutErrorMessage.visibility = View.VISIBLE  // Status banner
                    binding.tvErrorMessage.text = "Loading 3D model\u2026"
                    binding.ivErrorIcon.setImageResource(R.drawable.ic_camera)
                }
                is ARViewModel.ModelState.Loaded -> {
                    binding.layoutErrorMessage.visibility = View.GONE
                    binding.ivModelStatus.visibility = View.VISIBLE
                    binding.ivModelStatus.setImageResource(R.drawable.ic_check)
                }
                is ARViewModel.ModelState.Error -> {
                    binding.layoutErrorMessage.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = state.message
                    binding.ivErrorIcon.setImageResource(R.drawable.ic_delete)
                }
            }
        }

        arViewModel.isModelPlaced.observe(this) { isPlaced ->
            if (isPlaced) {
                binding.layoutPlacementGuide.visibility = View.GONE
                binding.layoutTrackingStatus.visibility = View.GONE
                binding.layoutControlPanel.visibility = View.VISIBLE
            } else {
                binding.layoutPlacementGuide.visibility = View.VISIBLE
                binding.layoutTrackingStatus.visibility = View.VISIBLE
                binding.layoutControlPanel.visibility = View.GONE
            }
        }

        arViewModel.trackingStatus.observe(this) { status ->
            binding.tvTrackingStatus.text = status
        }

        arViewModel.cartEvent.observe(this) { event ->
            when (event) {
                is ARViewModel.CartEvent.Success -> {
                    Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is ARViewModel.CartEvent.Error -> {
                    Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        anchorNodes.forEach { it.destroy() }
        anchorNodes.clear()
        modelNode = null
    }
}

