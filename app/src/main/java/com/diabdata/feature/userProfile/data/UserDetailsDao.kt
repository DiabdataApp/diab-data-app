package com.diabdata.feature.userProfile.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.diabdata.core.model.UserDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDetailsDao {
    @Query("SELECT * FROM user_details WHERE id = 0")
    fun getUserDetails(): Flow<UserDetails?>

    @Upsert
    suspend fun upsertUserDetails(userDetails: UserDetails)

    @Query("DELETE FROM user_details")
    suspend fun deleteUserDetails()

    @Query("UPDATE user_details SET profilePhotoPath = :path, updatedAt = :updatedAt WHERE id = 0")
    suspend fun updateProfilePhotoPath(path: String?, updatedAt: Long)
}