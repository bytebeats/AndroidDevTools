package me.bytebeats.tools.recorder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Created by bytebeats on 2021/12/10 : 15:51
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

internal const val RECORDING_REQUEST_CODE = 0x10001

internal val PERMISSIONS = arrayOf(
    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.CAMERA,
    Manifest.permission.SYSTEM_ALERT_WINDOW,
)

fun FragmentActivity.inflateRecorderFloatWindow() {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val floatView = LayoutInflater.from(this).inflate(R.layout.recorder_floating_window, null)
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_CREATE) {
                val layoutParams = WindowManager.LayoutParams()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
                }
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                layoutParams.gravity = Gravity.END and Gravity.BOTTOM
                layoutParams.format = PixelFormat.RGBA_8888
                layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                windowManager.addView(floatView, layoutParams)
                floatView.post {
                    layoutParams.x = windowManager.defaultDisplay.width - floatView.width
                    layoutParams.x = windowManager.defaultDisplay.height - floatView.height

                }
                floatView.findViewById<ImageView>(R.id.recorder_action).apply {
                    tag = false
                    setOnClickListener {
                        if (tag == true) {
                            setImageResource(R.drawable.recorder_stop)
                            stopRecording()
                        } else {
                            requestPermissions(PERMISSIONS, onAllGranted = {
                                startRecording()
                                tag = true
                                setImageResource(R.drawable.recorder_start)

                            }, onDenied = { permissions ->
                                tag = false
                                setImageResource(R.drawable.recorder_stop)
                            })
                        }
                    }
                    setOnLongClickListener {
                        windowManager.removeView(floatView)
                        false
                    }
                }
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                windowManager.removeView(floatView)
            }
        }
    })
}

internal fun FragmentActivity.startRecording() {
    val mediaProjectionManager =
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    val intent = mediaProjectionManager.createScreenCaptureIntent()
    startActivityForResult(intent, RECORDING_REQUEST_CODE)
}

internal fun FragmentActivity.stopRecording() {
    val intent = Intent(this, ScreenRecorderService::class.java)
    stopService(intent)
}

internal fun FragmentActivity.requestPermissions(
    permissions: Array<out String>,
    onAllGranted: () -> Unit,
    onDenied: ((List<String>) -> Unit)? = null
) {
    PermissionRequestLauncher.with(this)
        .requestPermissions(permissions, object : ReportFragment.Callback {
            override fun onRequestPermissionsResult(
                requestCode: Int,
                permissions: Array<out String>,
                grantResults: IntArray
            ) {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    onAllGranted()
                } else {
                    onDenied?.invoke(permissions.filterIndexed { index, _ -> grantResults[index] != PackageManager.PERMISSION_DENIED })
                }
            }
        })
}