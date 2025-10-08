package cyn.mobile.app.data.repositories.profile

import cyn.mobile.app.data.repositories.base.response.GeneralResponse
import cyn.mobile.app.data.repositories.profile.request.UpdateProfileRequest
import cyn.mobile.app.data.repositories.profile.response.ProfileDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ProfileService {

    @POST("/api/auth/profile/show")
    suspend fun getProfileInfo(): Response<ProfileDetailResponse>

    @POST("api/auth/profile/update")
    suspend fun doUpdateProfile(@Body request: UpdateProfileRequest): Response<GeneralResponse>
}