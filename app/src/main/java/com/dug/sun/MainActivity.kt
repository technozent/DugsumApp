package com.dug.sun

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.dug.sun.api.RetrofitClient
import com.dug.sun.api.getErrorMessage
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var ivImage: ImageView
    private lateinit var overlayContainer: FrameLayout
    private lateinit var etText: EditText
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var draggableTextBox: DraggableTextBox? = null

    private lateinit var tvUsername: TextView
    private lateinit var tvPlanStart: TextView
    private lateinit var tvPlanEnd: TextView

    enum class FilterPreset { NONE, WARM, COOL, VINTAGE }
    private var currentPreset = FilterPreset.NONE
    private var contrastValue = 1f
    private var scaleFactor = 1f
    private var initialScale = 1f
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private val imageMatrix = Matrix()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawableResource(android.R.color.white)
        setContentView(R.layout.activity_main)

        RetrofitClient.init(SessionManager(this))

        setupStatusBar()
        setupToolbarAndDrawer()
      //  fetchPlanStatus()

        val statusBarSpacer = findViewById<View>(R.id.statusBarSpacer)
        val rootLayout = findViewById<View>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            statusBarSpacer.layoutParams.height = statusBars.top
            statusBarSpacer.requestLayout()
            
            rootLayout.setPadding(0, 0, 0, navigationBars.bottom)
            insets
        }

        ivImage = findViewById(R.id.ivImage)
        overlayContainer = findViewById(R.id.overlayContainer)
        etText = findViewById(R.id.etText)

        setupPinchToZoom()

        ivImage.post { centerImage() }

        etText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handleTextSync(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<SeekBar>(R.id.seekContrast).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                contrastValue = progress / 100f
                applyColorFilter()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<SeekBar>(R.id.seekTextBlackness).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                draggableTextBox?.setTextBlackness(progress / 255f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<Button>(R.id.btnWarm).setOnClickListener { currentPreset = FilterPreset.WARM; applyColorFilter() }
        findViewById<Button>(R.id.btnCool).setOnClickListener { currentPreset = FilterPreset.COOL; applyColorFilter() }
        findViewById<Button>(R.id.btnHighContrast).setOnClickListener { contrastValue = 1.8f; applyColorFilter() }
        findViewById<Button>(R.id.btnVintage).setOnClickListener { currentPreset = FilterPreset.VINTAGE; applyColorFilter() }

        findViewById<Button>(R.id.btnZoomIn).setOnClickListener { zoom(1.2f) }
        findViewById<Button>(R.id.btnZoomOut).setOnClickListener { zoom(0.8f) }
    }

    private fun fetchPlanStatus() {
        val sessionManager = SessionManager(this)
        
        // Initial load from session
        tvUsername.text = sessionManager.getUsername() ?: "User"
        tvPlanStart.text = "Plan Start: ${sessionManager.getPlanStart() ?: ""}"
        tvPlanEnd.text = "Plan End: ${sessionManager.getPlanEnd() ?: ""}"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPlanStatus()
                if (response.isSuccessful) {
                    response.body()?.let { plan ->
                        // Update UI
                        tvUsername.text = plan.username ?: "User"
                        tvPlanStart.text = "Plan Start: ${plan.planStartDate ?: ""}"
                        tvPlanEnd.text = "Plan End: ${plan.planEndDate ?: ""}"

                        // Sync with session
                        sessionManager.saveUserDetails(plan.username, plan.planStartDate, plan.planEndDate)

                        if (plan.isExpired) {
                            showExpirationDialog()
                        }
                    }
                } else {
                    val message = response.getErrorMessage()
                    showErrorDialog(message)
                }
            } catch (e: Exception) {
                showErrorDialog("Network error: ${e.message}")
            }
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Application Error")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Exit") { _, _ ->
                finishAffinity()
            }
            .show()
    }

    private fun showExpirationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Plan Expired")
            .setMessage("Your plan has expired. Please renew to continue using the app.")
            .setCancelable(false)
            .setPositiveButton("Exit") { _, _ ->
                finishAffinity()
            }
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.logout()
                if (response.isSuccessful) {
                    SessionManager(this@MainActivity).clearSession()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                } else {
                    val message = response.getErrorMessage()
                    Toast.makeText(this@MainActivity, "Logout failed: $message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupStatusBar() {
        window.statusBarColor = Color.parseColor("#471A72")
        // Ensure status bar icons are white
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }

    private fun setupToolbarAndDrawer() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        toggle.drawerArrowDrawable.color = Color.WHITE
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        tvUsername = headerView.findViewById(R.id.tvUsername)
        tvPlanStart = headerView.findViewById(R.id.tvPlanStart)
        tvPlanEnd = headerView.findViewById(R.id.tvPlanEnd)

        navigationView.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.nav_logout) {
                showLogoutConfirmation()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupPinchToZoom() {
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val factor = detector.scaleFactor
                zoomAt(factor, detector.focusX, detector.focusY)
                return true
            }
        })

        overlayContainer.setOnTouchListener { v, event ->
            scaleGestureDetector.onTouchEvent(event)

            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    // Only pan if we are not scaling and have exactly one finger
                    if (!scaleGestureDetector.isInProgress && event.pointerCount == 1) {
                        val dx = event.x - lastTouchX
                        val dy = event.y - lastTouchY
                        imageMatrix.postTranslate(dx, dy)
                        ivImage.imageMatrix = imageMatrix
                    }

                    // Always update last touch position when moving with one finger
                    // to keep the next delta accurate.
                    if (event.pointerCount == 1) {
                        lastTouchX = event.x
                        lastTouchY = event.y
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    // When one finger is lifted, update lastTouchX/Y to the remaining finger's position
                    // to prevent jumping if panning resumes with the remaining finger.
                    val index = if (event.actionIndex == 0) 1 else 0
                    lastTouchX = event.getX(index)
                    lastTouchY = event.getY(index)
                }
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                }
            }
            true
        }
    }

    private fun centerImage() {
        val drawable = ivImage.drawable ?: return
        val viewWidth = ivImage.width.toFloat()
        val viewHeight = ivImage.height.toFloat()
        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()

        val scale: Float = if (drawableWidth / drawableHeight > viewWidth / viewHeight) {
            viewWidth / drawableWidth
        } else {
            viewHeight / drawableHeight
        }

        imageMatrix.setScale(scale, scale)
        val postTranslateX = (viewWidth - drawableWidth * scale) / 2f
        val postTranslateY = (viewHeight - drawableHeight * scale) / 2f
        imageMatrix.postTranslate(postTranslateX, postTranslateY)
        ivImage.imageMatrix = imageMatrix
        initialScale = scale
        scaleFactor = scale
    }

    private fun zoom(factor: Float) {
        zoomAt(factor, ivImage.width / 2f, ivImage.height / 2f)
    }

    private fun zoomAt(factor: Float, focusX: Float, focusY: Float) {
        val nextScale = scaleFactor * factor
        val relativeScale = nextScale / initialScale
        if (relativeScale < 0.5f || relativeScale > 4.0f) return

        scaleFactor = nextScale
        imageMatrix.postScale(factor, factor, focusX, focusY)
        ivImage.imageMatrix = imageMatrix
    }

    private fun handleTextSync(text: String) {
        if (text.isEmpty()) {
            draggableTextBox?.visibility = View.GONE
        } else {
            if (draggableTextBox == null) {
                draggableTextBox = DraggableTextBox(this).apply {
                    val widthPx = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._150sdp)
                    val heightPx = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._45sdp)
                    val lp = FrameLayout.LayoutParams(widthPx, heightPx)
                    lp.leftMargin = (overlayContainer.width - widthPx) / 2
                    lp.topMargin = (overlayContainer.height - heightPx) / 2
                    layoutParams = lp
                }
                overlayContainer.addView(draggableTextBox)
            }
            draggableTextBox?.visibility = View.VISIBLE
            draggableTextBox?.setText(text)
            
            // Sync current blackness
            val blacknessProgress = findViewById<SeekBar>(R.id.seekTextBlackness).progress
            draggableTextBox?.setTextBlackness(blacknessProgress / 255f)
        }
    }

    private fun applyColorFilter() {
        val matrix = ColorMatrix()
        when (currentPreset) {
            FilterPreset.WARM -> matrix.postConcat(ColorMatrix(floatArrayOf(
                1.15f, 0f, 0f, 0f, 15f,
                0f, 1.05f, 0f, 0f, 5f,
                0f, 0f, 0.85f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )))
            FilterPreset.COOL -> matrix.postConcat(ColorMatrix(floatArrayOf(
                0.85f, 0f, 0f, 0f, -10f,
                0f, 0.95f, 0f, 0f, 0f,
                0f, 0f, 1.2f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            )))
            FilterPreset.VINTAGE -> {
                matrix.postConcat(ColorMatrix(floatArrayOf(
                    0.9f, 0.1f, 0f, 0f, 10f,
                    0.1f, 0.8f, 0.1f, 0f, 10f,
                    0.1f, 0.1f, 0.6f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                )))
                matrix.postConcat(ColorMatrix().apply { setSaturation(0.6f) })
            }
            else -> {}
        }
        matrix.postConcat(contrastMatrix(contrastValue))
        ivImage.colorFilter = ColorMatrixColorFilter(matrix)
    }

    private fun contrastMatrix(contrast: Float): ColorMatrix {
        val translate = (1 - contrast) / 2f * 255f
        return ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
    }
}