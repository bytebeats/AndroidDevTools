package me.bytebeats.tools.recorder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import me.bytebeats.tools.window.FloatingWindow

/**
 * Created by bytebeats on 2021/12/10 : 15:51
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

val PERMISSIONS = arrayOf(
    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.CAMERA,
    Manifest.permission.SYSTEM_ALERT_WINDOW,
    Manifest.permission.FOREGROUND_SERVICE,
)

fun FragmentActivity.inflateRecorderFloatWindow() {
    Log.i(TAG, "inflateRecorderFloatWindow")
//    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//    val floatView =
//        LayoutInflater.from(this).inflate(R.layout.recorder_floating_window, null, false)
//    lifecycle.addObserver(object : LifecycleEventObserver {
//        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//            if (event == Lifecycle.Event.ON_RESUME) {
    Log.i(TAG, "ON_CREATE")
//    val layoutParams = WindowManager.LayoutParams()
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//    } else {
//        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
//    }
//    layoutParams.flags =
//        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//
//    layoutParams.gravity = Gravity.LEFT //Gravity.END and Gravity.BOTTOM
//    layoutParams.format = PixelFormat.RGBA_8888
//    layoutParams.width = 100
    //WindowManager.LayoutParams.MATCH_PARENT
//    layoutParams.height = 100
    //WindowManager.LayoutParams.MATCH_PARENT
//    layoutParams.x = 0
//    layoutParams.y = 0
//    windowManager.addView(floatView, layoutParams)
//    Log.i(TAG, "addView")
//    floatView.post {
//                    layoutParams.x = windowManager.defaultDisplay.width - floatView.width
//                    layoutParams.y = windowManager.defaultDisplay.height - floatView.height
//        Log.i(TAG, "${layoutParams.x}, ${layoutParams.y}")
//    }
    FloatingWindow(this, R.layout.recorder_floating_window).onView { floatView ->

        floatView?.findViewById<ImageView>(R.id.recorder_action)?.apply {
            tag = false
            setOnClickListener {
                if (tag == true) {
                    setImageResource(R.drawable.recorder_stop)
                    stopRecording()
                } else {
                    requestPermissions(PERMISSIONS, onAllGranted = {
                        requestRecordingPermission(onGranted = { code, data ->
                            startRecording(code, data)
                            tag = true
                            setImageResource(R.drawable.recorder_start)
                        }, onDenied = {
                            tag = false
                            setImageResource(R.drawable.recorder_stop)
                        })
                    }, onDenied = { permissions ->
                        tag = false
                        setImageResource(R.drawable.recorder_stop)
                    })
                }
            }
            setOnLongClickListener {
//                windowManager.removeView(floatView)
                false
            }
        }
    }
//            } else if (event == Lifecycle.Event.ON_DESTROY) {
//                windowManager.removeView(floatView)
//                Log.i(TAG, "ON_DESTROY")
//            }
//        }
//    })
}

fun FragmentActivity.requestRecordingPermission(
    onGranted: (Int, Intent?) -> Unit,
    onDenied: (() -> Unit)?
) {
    val mediaProjectionManager =
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    val intent = mediaProjectionManager.createScreenCaptureIntent()
    ActivityResultLauncher.with(this)
        .startActivityForResult(intent, object : ReportFragment.ActivityResultCallback {
            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                if (resultCode == Activity.RESULT_OK) {
                    onGranted(resultCode, data)
                } else {
                    onDenied?.invoke()
                }
            }
        })
}

fun FragmentActivity.startRecording(resultCode: Int, data: Intent?) {
    val intent = Intent(this, ScreenRecorderService::class.java).apply {
        putExtra(EXTRA_RESULT_CODE, resultCode)
        putExtra(EXTRA_RESULT_DATA, data)
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        putExtra(EXTRA_SCREEN_WIDTH, metrics.widthPixels)
        putExtra(EXTRA_SCREEN_HEIGHT, metrics.heightPixels)
        putExtra(EXTRA_SCREEN_DENSITY, metrics.densityDpi)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

fun FragmentActivity.stopRecording() {
    val intent = Intent(this, ScreenRecorderService::class.java)
    stopService(intent)
}

fun FragmentActivity.requestPermissions(
    permissions: Array<out String>,
    onAllGranted: () -> Unit,
    onDenied: ((List<String>) -> Unit)? = null
) {
    ActivityResultLauncher.with(this)
        .requestPermissions(permissions, object : ReportFragment.RequestPermissionCallback {
            override fun onRequestPermissionsResult(
                requestCode: Int,
                permissions: Array<out String>,
                grantResults: IntArray
            ) {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    onAllGranted()
                } else {
                    val deniedPermissions =
                        permissions.filterIndexed { index, _ -> grantResults[index] != PackageManager.PERMISSION_GRANTED }
                    onDenied?.invoke(deniedPermissions)
                }
            }
        })
}

fun FragmentActivity.simulateHomeClick() {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.addCategory(Intent.CATEGORY_HOME)
    startActivity(intent)
}