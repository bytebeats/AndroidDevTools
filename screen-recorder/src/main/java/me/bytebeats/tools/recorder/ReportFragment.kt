package me.bytebeats.tools.recorder

import android.content.Intent
import androidx.collection.SparseArrayCompat
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlin.random.Random

/**
 * Created by bytebeats on 2021/12/10 : 17:48
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */
class ReportFragment : Fragment() {
    private val mRequestPermissionCallbacks = SparseArrayCompat<RequestPermissionCallback?>()
    private val mActivityResultCallbacks = SparseArrayCompat<ActivityResultCallback?>()
    private val mRequestCodeGenerator = Random.Default

    companion object {
        fun newInstance(): ReportFragment {
            return ReportFragment()
        }
    }

    fun startActivityForResult(
        intent: Intent,
        callback: ActivityResultCallback?
    ) {
        val requestCode = makeRequestCode()
        mActivityResultCallbacks.put(requestCode, callback)
        startActivityForResult(intent, requestCode, null)
    }

    fun requestPermissions(
        permissions: Array<out String>,
        callback: RequestPermissionCallback?
    ) {
        val requestCode = makeRequestCode()
        mRequestPermissionCallbacks.put(requestCode, callback)
        requestPermissions(permissions, requestCode)
    }

    private fun makeRequestCode(): Int {
        var requestCode = 0
        var retry = 0
        do {
            requestCode = mRequestCodeGenerator.nextInt(0xffff)
            retry += 1
        } while (mRequestPermissionCallbacks.indexOfKey(requestCode) >= 0 && retry < 10)
        return requestCode
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        mRequestPermissionCallbacks[requestCode]?.let {
            it.onRequestPermissionsResult(requestCode, permissions, grantResults)
            mRequestPermissionCallbacks.remove(requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mActivityResultCallbacks[requestCode]?.let {
            it.onActivityResult(requestCode, resultCode, data)
            mActivityResultCallbacks.remove(requestCode)
        }
    }

    interface ActivityResultCallback {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }

    interface RequestPermissionCallback {
        fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        )
    }
}