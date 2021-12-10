package me.bytebeats.tools.recorder

import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Created by bytebeats on 2021/12/10 : 17:48
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */
class ReportFragment : Fragment() {

    interface Callback {
        fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        )
    }
}