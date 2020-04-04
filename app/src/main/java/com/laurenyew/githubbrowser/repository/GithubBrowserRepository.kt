package com.laurenyew.githubbrowser.repository

import android.util.MalformedJsonException
import androidx.annotation.VisibleForTesting
import com.laurenyew.githubbrowser.repository.models.ErrorState
import com.laurenyew.githubbrowser.repository.models.GithubRepoModel
import com.laurenyew.githubbrowser.repository.models.GithubRepoModelsResponse
import com.laurenyew.githubbrowser.repository.networking.api.GithubApi
import com.laurenyew.githubbrowser.repository.networking.api.responses.SearchGithubReposResponse
import io.reactivex.Observable
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubBrowserRepository @Inject constructor(private val githubApi: GithubApi) {
    fun searchTopGithubRepositoriesByOrganization(organizationName: String?): Observable<GithubRepoModelsResponse> =
        githubApi
            .searchRepos(createSearchRepositoriesQuery(organizationName))
            .map {
                parseGithubRepositoriesResponseSuccess(it)
            }
            .onErrorReturn { error ->
                parseGithubRepositoriesResponseError(error)
            }
            .toObservable()

    private fun createSearchRepositoriesQuery(organizationName: String?): String =
        if (organizationName != null) {
            ORG_QUERY + organizationName
        } else {
            ""
        }


    private fun parseGithubRepositoriesResponseSuccess(response: SearchGithubReposResponse): GithubRepoModelsResponse {
        val repos = arrayListOf<GithubRepoModel>()
        response.items.forEach {
            repos.add(
                GithubRepoModel(
                    id = it.id,
                    name = it.name,
                    language = it.language,
                    description = it.description,
                    numStars = it.starCount,
                    websiteUrl = it.websiteUrl
                )
            )
        }
        return GithubRepoModelsResponse.Success(repos)
    }

    @VisibleForTesting
    fun parseGithubRepositoriesResponseError(exception: Throwable): GithubRepoModelsResponse {
        val errorState = when (exception) {
            is HttpException ->
                when (exception.code()) {
                    INVALID_QUERY_ERROR_CODE -> ErrorState.InvalidQueryError
                    RATE_LIMIT_ERROR_CODE -> ErrorState.HitRateLimitError
                    else -> ErrorState.NetworkError
                }
            is MalformedJsonException -> ErrorState.MalformedResultError
            else -> ErrorState.UnknownError(exception.message)
        }
        return GithubRepoModelsResponse.Failure(errorState)
    }

    companion object {
        @VisibleForTesting
        const val INVALID_QUERY_ERROR_CODE = 422
        @VisibleForTesting
        const val RATE_LIMIT_ERROR_CODE = 403
        private const val ORG_QUERY = "org:"
    }
}