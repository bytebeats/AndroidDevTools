package me.bytebeats.tools.recorder

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
    private val mCallbacks = SparseArrayCompat<Callback?>()
    private val mRequestCodeGenerator = Random.Default

    companion object {
        fun newInstance(): ReportFragment {
            return ReportFragment()
        }
    }

    fun requestPermissions(permissions: Array<out String>, callback: Callback?) {
        val requestCode = makeRequestCode()
        mCallbacks.put(requestCode, callback)
        ActivityCompat.requestPermissions(requireActivity(), permissions, requestCode)
    }

    private fun makeRequestCode(): Int {
        var requestCode = 0
        var retry = 0
        do {
            requestCode = mRequestCodeGenerator.nextInt(0xffff)
            retry += 1
        } while (mCallbacks.indexOfKey(requestCode) >= 0 && retry < 10)
        return requestCode
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        mCallbacks[requestCode]?.let {
            it.onRequestPermissionsResult(requestCode, permissions, grantResults)
            mCallbacks.remove(requestCode)
        }
    }

    interface Callback {
        fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        )
    }
}