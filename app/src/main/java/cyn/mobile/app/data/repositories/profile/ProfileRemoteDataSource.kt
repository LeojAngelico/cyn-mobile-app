package cyn.mobile.app.data.repositories.profile

import cyn.mobile.app.data.repositories.base.response.GeneralResponse
import cyn.mobile.app.data.repositories.profile.request.UpdateProfileRequest
import cyn.mobile.app.data.repositories.profile.response.ProfileDetailResponse
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class ProfileRemoteDataSource @Inject constructor(
    private val profileService: ProfileService
) {

    suspend fun getProfileInfo(): ProfileDetailResponse {
        val response = profileService.getProfileInfo()
        if (response.code() != HttpURLConnection.HTTP_OK) throw HttpException(response)
        return response.body() ?: throw NullPointerException("Response data is empty")
    }

    suspend fun doUpdateProfile(request: UpdateProfileRequest): GeneralResponse {
        val response = profileService.doUpdateProfile(request)
        if (response.code() != HttpURLConnection.HTTP_CREATED) throw HttpException(response)
        return response.body() ?: throw NullPointerException("Response data is empty")
    }
}