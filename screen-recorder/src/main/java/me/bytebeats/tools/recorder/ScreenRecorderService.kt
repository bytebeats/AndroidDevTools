package me.bytebeats.tools.recorder

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
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
            createNotificationChannel(this)
            mResultCode = getIntExtra(EXTRA_RESULT_CODE, -1)
            mResultData = getParcelableExtra<Intent>(EXTRA_RESULT_DATA) as Intent
            mScreenWidth = getIntExtra(EXTRA_SCREEN_WIDTH, 0)
            mScreenHeight = getIntExtra(EXTRA_SCREEN_HEIGHT, 0)
            mScreenDensity = getIntExtra(EXTRA_SCREEN_DENSITY, 0)
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
        val path = "$dir/${sdf.format(Date())}.mp4"
        val file = File(path)
        if (!file.exists()) {
            file.createNewFile()
        }
        return try {
            MediaRecorder().apply {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setVideoEncodingBitRate(5 * mScreenWidth * mScreenHeight)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setVideoSize(mScreenWidth, mScreenHeight)
                setVideoFrameRate(60)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setOutputFile(file)
                } else {
                    setOutputFile(path)
                }
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

    private fun createNotificationChannel(intent: Intent) {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, SCREEN_RECORDER_CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        builder.setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(intent.component?.className),
                0
            )
        ).setLargeIcon(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.ic_notification_recording
            )
        )
            .setSmallIcon(R.drawable.ic_notification_recording)
            .setContentText("Recording...")
            .setWhen(System.currentTimeMillis())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                SCREEN_RECORDER_CHANNEL_ID,
                "screen_recorder_channel",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
        val notification = builder.build()
        notification.defaults = Notification.DEFAULT_SOUND
        startForeground(SCREEN_RECORDER_CHANNEL_ID.hashCode(), notification)
    }
}

internal const val TAG = "ScreenRecorderService"
const val EXTRA_RESULT_CODE = "result_code"
const val EXTRA_RESULT_DATA = "result_data"
const val EXTRA_SCREEN_WIDTH = "screen_width"
const val EXTRA_SCREEN_HEIGHT = "screen_height"
const val EXTRA_SCREEN_DENSITY = "screen_density"
internal val SCREEN_RECORDER_CHANNEL_ID = "screen_recorder_channel_id"