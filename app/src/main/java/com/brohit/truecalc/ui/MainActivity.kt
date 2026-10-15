package com.brohit.truecalc.ui

import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalView
import com.brohit.truecalc.BuildConfig
import com.brohit.truecalc.ui.utils.showAllowNotificationSnackbar
import com.brohit.truecalc.ui.utils.showSnackBar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val UPDATE_REQUEST_CODE = 101
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var view: View
    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.data == null) return@registerForActivityResult
        if (result.resultCode == UPDATE_REQUEST_CODE) {
            Toast.makeText(this, "Downloading started", Toast.LENGTH_SHORT).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            view.showAllowNotificationSnackbar()
        }
    }


    private val updateResultStarter =
        IntentSenderForResultStarter { intent, _, fillInIntent, flagsMask, flagsValues, _, _ ->
            val request = IntentSenderRequest.Builder(intent)
                .setFillInIntent(fillInIntent)
                .setFlags(flagsValues, flagsMask)
                .build()

            updateLauncher.launch(request)
        }

    private val appUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            notifyUpdateDownloaded()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(appUpdateListener)
        setContent {
            view = LocalView.current
            MainAppScreen()
        }
        checkForAppUpdate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!BuildConfig.IS_PLAY_BUILD) return
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                notifyUpdateDownloaded()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(appUpdateListener)
    }

    private fun checkForAppUpdate() {
        // Play in-app updates only work for installs from Google Play.
        if (!BuildConfig.IS_PLAY_BUILD) return
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateResultStarter,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                        UPDATE_REQUEST_CODE
                    )
                } catch (exception: IntentSender.SendIntentException) {
                    Log.d(TAG, "checkForAppUpdate: ", exception)
                }
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                notifyUpdateDownloaded()
            }
        }
    }

    private fun notifyUpdateDownloaded() {
        view.showSnackBar(
            message = "Update Downloaded",
            actionLabel = "Restart",
            onAction = {
                appUpdateManager.completeUpdate()
            }
        )
    }


}


