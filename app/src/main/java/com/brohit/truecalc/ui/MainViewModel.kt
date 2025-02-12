package com.brohit.truecalc.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.truecalc.BuildConfig
import com.brohit.truecalc.data.data_source.remote.GitHubApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val githubApi: GitHubApi
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }


    private val _isUpdateAvailable: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var downloadUrl: String? = null
    var notes: String? = null

    val isUpdateAvailable: StateFlow<Boolean> = _isUpdateAvailable
        .onStart { checkForUpdate() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val currentVersion = BuildConfig.VERSION_NAME

    init {
        checkForUpdate()
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            try {
                val release = githubApi.getLatestRelease("rollychop", "TrueCalc")
                if (release.tagName != currentVersion) {
                    downloadUrl = release.assets.firstOrNull { it.isApk }?.downloadUrl
                    notes = release.releaseNotes
                    _isUpdateAvailable.emit(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching update", e)
            }
        }
    }
}
