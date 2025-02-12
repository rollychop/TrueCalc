package com.brohit.truecalc.data.data_source.remote

import com.brohit.truecalc.data.data_source.remote.dto.GitHubRelease
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRelease
}
