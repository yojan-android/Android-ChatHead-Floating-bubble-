package com.example.floatingbubbleapp

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import kotlin.math.roundToInt

class MyFloatingWindowService : Service() {

    var LAYOUT_FLAG: Int = 0
    lateinit var floatingView: View
    lateinit var manager: WindowManager
    lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        this.params = params
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = 0
        params.y = 100

        manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.chat_head_layout, null)

        // Set initial transparency level for idle state
        floatingView.alpha = 0.7f
        manager.addView(floatingView, params)

        floatingView.findViewById<View>(R.id.chat_head_image)?.setOnTouchListener(object :
            View.OnTouchListener {

            var initialX: Int? = null
            var initialY: Int? = null
            var initialTouchX: Float? = null
            var initialTouchY: Float? = null

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                when (motionEvent!!.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Make the button fully opaque when touched
                        floatingView.alpha = 1.0f
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = motionEvent.rawX
                        initialTouchY = motionEvent.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // Return to idle transparency when touch is released
                        floatingView.alpha = 0.7f
                        val Xdiff = (motionEvent.rawX - initialTouchX!!)
                        val Ydiff = (motionEvent.rawY - initialTouchY!!)
                        if (Xdiff < 10 && Ydiff < 10) {
                            val intent = Intent(this@MyFloatingWindowService, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX!!.plus((motionEvent.rawX - initialTouchX!!)).roundToInt()
                        params.y = initialY!!.plus((motionEvent.rawY - initialTouchY!!).roundToInt())
                        manager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.removeView(floatingView)
    }
}
