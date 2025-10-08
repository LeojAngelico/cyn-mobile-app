package cyn.mobile.app.data.repositories.profile

import cyn.mobile.app.data.repositories.auth.response.UserData
import cyn.mobile.app.data.repositories.profile.request.UpdateProfileRequest
import cyn.mobile.app.data.repositories.profile.response.ProfileDetailResponse
import cyn.mobile.app.data.repositories.base.response.GeneralResponse
import cyn.mobile.app.security.AuthStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val remoteDataSource: ProfileRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val authStorage: AuthStorage
) {

    fun getProfileInfo(): Flow<ProfileDetailResponse> {
        return flow {
            val response = remoteDataSource.getProfileInfo()
            val userInfo = response.data?: UserData()
            authStorage.setUserBasicInfo(userInfo)
            emit(response)
        }.flowOn(ioDispatcher)
    }

    fun doUpdateProfile(request: UpdateProfileRequest): Flow<GeneralResponse> {
        return flow {
            emit(remoteDataSource.doUpdateProfile(request))
        }.flowOn(ioDispatcher)
    }
}
