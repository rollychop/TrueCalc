package com.brohit.truecalc.data.data_source.remote.dto

import com.google.gson.annotations.SerializedName

data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("html_url") val releaseUrl: String,
    @SerializedName("body") val releaseNotes: String,
    @SerializedName("assets") val assets: List<Asset>
) {
    data class Asset(
        @SerializedName("browser_download_url") val downloadUrl: String,
        @SerializedName("contentType", alternate = ["content_type"]) val contentType: String,
        @SerializedName("name") val fileName: String
    ) {
        val isApk
            get() = contentType.equals("application/vnd.android.package-archive", true)
                    || fileName.endsWith(".apk", true)
    }
}
