package com.chaibet.app

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.CountDownTimer
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private lateinit var prefs: AppPreferences
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        prefs = AppPreferences(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val triggerPkg = intent?.getStringExtra("EXTRA_PACKAGE_NAME") ?: "App"
        showOverlay(triggerPkg)
        return START_NOT_STICKY
    }

    private fun showOverlay(triggerPkg: String) {
        if (overlayView != null) return

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.overlay_chai_bet, null)

        val txtBuddyMsg = overlayView?.findViewById<TextView>(R.id.txtBuddyMessage)
        val txtCountdown = overlayView?.findViewById<TextView>(R.id.txtCountdown)
        val btnTurnBack = overlayView?.findViewById<Button>(R.id.btnTurnBack)
        val btnTreatChai = overlayView?.findViewById<Button>(R.id.btnTreatChai)

        val buddy = prefs.buddyName
        val price = prefs.snackPrice
        val friendCut = (price * 0.8).toInt()

        txtBuddyMsg?.text = "Are these reels worth buying $buddy a hot cutting chai (₹$friendCut)?"
        btnTreatChai?.text = "Treat $buddy ₹$friendCut & Unlock 15m"
        btnTreatChai?.isEnabled = false

        // 20-Second Cooling Pause Timer
        countDownTimer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                txtCountdown?.text = "Cooling Pause: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                txtCountdown?.text = "Cooling Pause Finished"
                btnTreatChai?.isEnabled = true
            }
        }.start()

        // Turn Back -> Go to Android Home Screen
        btnTurnBack?.setOnClickListener {
            removeOverlay()
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        }

        // Treat Friend -> Launch UPI & Grant 15 min session
        btnTreatChai?.setOnClickListener {
            removeOverlay()
            prefs.grantUnlockGrace(15)
            prefs.recordChaiGiven(triggerPkg, price)
            UpiPaymentHelper.launchUpiPayment(
                this,
                prefs.buddyUpi,
                prefs.buddyName,
                friendCut,
                "Chai Bet - Lost focus on $triggerPkg"
            )
        }

        windowManager?.addView(overlayView, layoutParams)
    }

    private fun removeOverlay() {
        countDownTimer?.cancel()
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
