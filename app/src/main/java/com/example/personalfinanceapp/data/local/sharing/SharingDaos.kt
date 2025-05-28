package com.example.personalfinanceapp.data.local.sharing

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad SharedUserEntity.
 */
@Dao
interface SharedUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedUser(sharedUser: SharedUserEntity)

    @Update
    suspend fun updateSharedUser(sharedUser: SharedUserEntity)

    @Query("SELECT * FROM shared_users WHERE userId = :userId")
    fun getSharedUserById(userId: String): Flow<SharedUserEntity?>

    @Query("SELECT * FROM shared_users")
    fun getAllSharedUsers(): Flow<List<SharedUserEntity>>

    @Query("DELETE FROM shared_users WHERE userId = :userId")
    suspend fun deleteSharedUser(userId: String)
}

/**
 * Data Access Object (DAO) para la entidad SharingInvitationEntity.
 */
@Dao
interface SharingInvitationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitation(invitation: SharingInvitationEntity): Long

    @Update
    suspend fun updateInvitation(invitation: SharingInvitationEntity)

    @Query("SELECT * FROM sharing_invitations WHERE invitationId = :invitationId")
    fun getInvitationById(invitationId: String): Flow<SharingInvitationEntity?>

    @Query("SELECT * FROM sharing_invitations WHERE invitedUserEmail = :email AND direction = 'RECEIVED'")
    fun getReceivedInvitationsForUser(email: String): Flow<List<SharingInvitationEntity>>

    @Query("SELECT * FROM sharing_invitations WHERE inviterUserEmail = :email AND direction = 'SENT'")
    fun getSentInvitationsByUser(email: String): Flow<List<SharingInvitationEntity>>

    @Query("SELECT * FROM sharing_invitations")
    fun getAllInvitations(): Flow<List<SharingInvitationEntity>>

    @Query("DELETE FROM sharing_invitations WHERE invitationId = :invitationId")
    suspend fun deleteInvitation(invitationId: String)
}

