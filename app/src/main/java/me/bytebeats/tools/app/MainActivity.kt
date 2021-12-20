package me.bytebeats.tools.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import me.bytebeats.tools.recorder.simulateHomeClick
import me.bytebeats.tools.recorder.requestRecordingPermission
import me.bytebeats.tools.recorder.startRecording
import me.bytebeats.tools.recorder.stopRecording

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.click_home).setOnClickListener {
            simulateHomeClick()
        }
        findViewById<Button>(R.id.add_float_window).setOnClickListener {
        }

        findViewById<Button>(R.id.start_recording).setOnClickListener {
            requestRecordingPermission(onGranted = { code, data ->
                startRecording(code, data)
            }, onDenied = {
            })
        }

        findViewById<Button>(R.id.stop_recording).setOnClickListener {
            stopRecording()
        }
    }
}