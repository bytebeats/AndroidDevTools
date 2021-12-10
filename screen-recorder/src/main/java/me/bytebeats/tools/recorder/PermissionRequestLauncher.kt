package me.bytebeats.tools.recorder

import androidx.fragment.app.FragmentActivity

/**
 * Created by bytebeats on 2021/12/10 : 18:08
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */
class PermissionRequestLauncher(private val activity: FragmentActivity) {
    companion object {
        private const val TAG = "RequestLauncher"
        fun with(activity: FragmentActivity): PermissionRequestLauncher {
            return PermissionRequestLauncher(activity)
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

    fun requestPermissions(permissions: Array<out String>, callback: ReportFragment.Callback?) {
        mReportFragment.requestPermissions(permissions, callback)
    }
}