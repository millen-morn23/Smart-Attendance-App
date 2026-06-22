package com.example.database

import androidx.room.*
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserByUid(uid: String): User?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersAsFlow(role: String): Flow<List<User>>

    @Query("SELECT * FROM users")
    fun getAllUsersAsFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCountFlow(): Flow<Int>
}
