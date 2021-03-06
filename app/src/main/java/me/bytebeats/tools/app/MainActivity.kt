package me.bytebeats.tools.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import me.bytebeats.tools.recorder.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inflateRecorderFloatWindow()
        findViewById<Button>(R.id.click_home).setOnClickListener {
            simulateHomeClick()
        }
        findViewById<Button>(R.id.add_float_window).setOnClickListener {
        }

        findViewById<Button>(R.id.start_recording).setOnClickListener {
            requestPermissions(PERMISSIONS, onAllGranted = {
                requestRecordingPermission(onGranted = { code, data ->
                    startRecording(code, data)
                }, onDenied = {
                })
            }, onDenied = { permissions ->
                requestRecordingPermission(onGranted = { code, data ->
                    startRecording(code, data)
                }, onDenied = {
                })
            })
        }

        findViewById<Button>(R.id.stop_recording).setOnClickListener {
            stopRecording()
        }

        Log.i("ScreenRecorderService", "window added? ${findViewById<ImageView>(R.id.add_float_window) != null}")
    }
}