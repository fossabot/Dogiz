package com.rubylichtenstein.data.breeds

import com.rubylichtenstein.domain.common.AsyncResult
import com.rubylichtenstein.domain.breeds.data.BreedsRepository
import com.rubylichtenstein.domain.common.asAsyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreedsRepositoryImpl @Inject constructor(
    private val breedsApi: BreedsRemoteApi,
    private val breedsDataStore: BreedsDataStore
) : BreedsRepository {

    override val breedsFlow: Flow<AsyncResult<List<BreedInfoImpl>>> = flow {
        // Try fetching from local first
        val localBreeds = getBreedsFromLocal()
        if (localBreeds != null) {
            emit(localBreeds)
        }

        // Then always fetch from remote
        try {
            val remoteBreeds = breedsApi.getAllBreeds().getOrThrow()
            val breeds = BreedInfoImpl.fromMap(remoteBreeds)
            breedsDataStore.save(breeds)
            emit(breeds)
        } catch (exception: Exception) {
            if (localBreeds == null) {
                throw exception
            }
        }
    }.asAsyncResult()

    private suspend fun getBreedsFromLocal(): List<BreedInfoImpl>? {
        return breedsDataStore.get.first()
    }
}


