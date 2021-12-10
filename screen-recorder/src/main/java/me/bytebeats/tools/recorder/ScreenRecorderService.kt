package me.bytebeats.tools.recorder

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.os.IBinder
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by bytebeats on 2021/12/10 : 15:04
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */
class ScreenRecorderService : Service() {

    private var mResultCode by Delegates.notNull<Int>()
    private var mResultData by Delegates.notNull<Intent>()
    private var mScreenWidth by Delegates.notNull<Int>()
    private var mScreenHeight by Delegates.notNull<Int>()
    private var mScreenDensity by Delegates.notNull<Int>()

    private var mMediaProjection: MediaProjection? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mVirtualDisplay: VirtualDisplay? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.apply {
            mResultCode = getIntExtra(EXTRA_RESULT_CODE, -1)
            mResultData = getParcelableExtra<Intent>(EXTRA_RESULT_DATA) as Intent
            mScreenWidth = getIntExtra(EXTRA_RESULT_WIDTH, 0)
            mScreenHeight = getIntExtra(EXTRA_RESULT_HEIGHT, 0)
            mScreenDensity = getIntExtra(EXTRA_RESULT_DENSITY, 0)
        }
        try {
            mMediaProjection = createMediaProjection()
            mMediaRecorder = createMediaRecorder()
            mVirtualDisplay = createVirtualDisplay()

            mMediaRecorder?.start()
        } catch (ignore: Exception) {
            Log.d(TAG, ignore.message, ignore)
        }
        return START_NOT_STICKY
    }

    private fun createMediaProjection(): MediaProjection =
        (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
            .getMediaProjection(mResultCode, mResultData)

    private fun createMediaRecorder(): MediaRecorder? {
        val dir =
            File("${Environment.getExternalStorageState()}${File.separator}ScreenVideo${File.separator}")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss")
        val file = "$dir${sdf.format(Date())}.mp4"
        return try {
            MediaRecorder().apply {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setVideoEncodingBitRate(5 * mScreenWidth * mScreenHeight)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setVideoSize(mScreenWidth, mScreenHeight)
                setVideoFrameRate(60)
                setOutputFile(file)
                prepare()
            }
        } catch (ignore: Exception) {
            null
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay? = mMediaProjection?.createVirtualDisplay(
        "media_projection",
        mScreenWidth,
        mScreenHeight,
        mScreenDensity,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        mMediaRecorder?.surface,
        object : VirtualDisplay.Callback() {
            override fun onResumed() {
                Log.d(TAG, "VirtualDisplay resumed")
            }

            override fun onPaused() {
                Log.d(TAG, "VirtualDisplay paused")
            }

            override fun onStopped() {
                Log.d(TAG, "VirtualDisplay stopped")
            }
        },
        null
    )

    override fun onDestroy() {
        super.onDestroy()
        mVirtualDisplay?.release()
        mMediaRecorder?.stop()
        mMediaProjection?.stop()
    }
}

internal const val TAG = "ScreenRecorderService"
const val EXTRA_RESULT_CODE = "result_code"
const val EXTRA_RESULT_DATA = "result_data"
const val EXTRA_RESULT_WIDTH = "result_width"
const val EXTRA_RESULT_HEIGHT = "result_height"
const val EXTRA_RESULT_DENSITY = "result_density"