package com.crosssafe.app.util

import androidx.appcompat.app.AppCompatActivity
import com.crosssafe.app.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class UpdateManager(private val activity: AppCompatActivity) {

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    private val UPDATE_REQUEST_CODE = 500

    private lateinit var installStateListener: InstallStateUpdatedListener

    init {
        installStateListener = InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> showUpdateReadySnackbar()
                InstallStatus.INSTALLED -> appUpdateManager.unregisterListener(installStateListener)
                InstallStatus.FAILED -> { /* retry later */ }
                else -> {}
            }
        }
    }

    fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.updatePriority() >= 4
                && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) ->
                    startImmediateUpdate(info)

                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) ->
                    startFlexibleUpdate(info)

                info.installStatus() == InstallStatus.DOWNLOADED ->
                    showUpdateReadySnackbar()
            }
        }
    }

    private fun startImmediateUpdate(info: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            info, AppUpdateType.IMMEDIATE, activity, UPDATE_REQUEST_CODE
        )
    }

    private fun startFlexibleUpdate(info: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            info, AppUpdateType.FLEXIBLE, activity, UPDATE_REQUEST_CODE
        )
        appUpdateManager.registerListener(installStateListener)
    }

    private fun showUpdateReadySnackbar() {
        Snackbar.make(
            activity.findViewById(android.R.id.content),
            "Update ready! Restart CrossSafe to install.",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Restart now") {
            appUpdateManager.completeUpdate()
        }.setActionTextColor(activity.getColor(R.color.colorPrimary)).show()
    }

    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startImmediateUpdate(info)
            }
        }
    }

    fun onDestroy() {
        appUpdateManager.unregisterListener(installStateListener)
    }
}
