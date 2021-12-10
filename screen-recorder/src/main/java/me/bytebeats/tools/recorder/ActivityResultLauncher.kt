package me.bytebeats.tools.recorder

import android.content.Intent
import androidx.fragment.app.FragmentActivity

/**
 * Created by bytebeats on 2021/12/10 : 18:08
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */
class ActivityResultLauncher(private val activity: FragmentActivity) {
    companion object {
        private const val TAG = "RequestLauncher"
        fun with(activity: FragmentActivity): ActivityResultLauncher {
            return ActivityResultLauncher(activity)
        }
    }

    private val mReportFragment by lazy { findReportFragment() }

    private fun findReportFragment(): ReportFragment {
        val fragmentManager = activity.supportFragmentManager
        var reportFragment = fragmentManager.findFragmentByTag(TAG) as ReportFragment?
        if (reportFragment == null) {
            reportFragment = ReportFragment.newInstance()
            fragmentManager.beginTransaction().add(reportFragment, TAG).commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
        }
        return reportFragment
    }

    fun startActivityForResult(intent: Intent, callback: ReportFragment.ActivityResultCallback?) {
        mReportFragment.startActivityForResult(intent, callback)
    }

    fun requestPermissions(
        permissions: Array<out String>,
        callback: ReportFragment.RequestPermissionCallback?
    ) {
        mReportFragment.requestPermissions(permissions, callback)
    }
}