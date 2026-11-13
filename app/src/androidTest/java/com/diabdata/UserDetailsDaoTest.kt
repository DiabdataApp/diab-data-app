package com.diabdata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.UserDetails
import com.diabdata.core.model.nowMillisInLong
import com.diabdata.feature.userProfile.data.UserDetailsDao
import com.diabdata.shared.utils.dataTypes.BloodType
import com.diabdata.shared.utils.dataTypes.DiabetesType
import com.diabdata.shared.utils.dataTypes.Gender
import com.diabdata.shared.utils.dataTypes.GlucoseUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate

/**
 * Tests for [UserDetailsDao] found at [com.diabdata.feature.userProfile.data.UserDetailsDao].
 */
@RunWith(AndroidJUnit4::class)
class UserDetailsDaoTest {
    private lateinit var userDetailsDao: UserDetailsDao
    private lateinit var db: DiabDataDatabase

    val today: LocalDate = LocalDate.now()

    val testUser = UserDetails(
        id = 0,
        firstName = "John",
        lastName = "Doe",
        profilePhotoPath = "/path/to/photo.jpg",
        birthdate = today.minusYears(18).minusMonths(5).minusDays(4),
        gender = Gender.MALE,
        bloodType = BloodType.O_NEGATIVE,
        diabetesType = DiabetesType.TYPE_1,
        diagnosisDate = today.minusYears(1).minusMonths(2),
        endocrinologist = "Dr. Smith",
        generalPractitioner = "Dr. Johnson",
        ophthalmologist = "Dr. Brown",
        cardiologist = "Dr. Davis",
        nephrologist = "Dr. Wilson",
        insulinPumpModel = "Model X",
        cgmModel = "Model Y",
        insulinType = "Fasting",
        basalInsulinType = "Basal",
        targetGlucoseMin = 100f,
        targetGlucoseMax = 120f,
        glucoseUnit = GlucoseUnit.MG_DL,
        emergencyContactName = "Jane Doe",
        emergencyContactPhone = "123-456-7890"
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            DiabDataDatabase::class.java
        ).build()
        userDetailsDao = db.userDetailsDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetUser() = runBlocking {
        userDetailsDao.upsertUserDetails(testUser)
        val retrievedUser = userDetailsDao.getUserDetails().first()
        assertEquals(testUser, retrievedUser)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndDeleteUser() = runBlocking {
        userDetailsDao.upsertUserDetails(testUser)
        val retrievedUserBefore = userDetailsDao.getUserDetails().first()
        assertEquals(testUser, retrievedUserBefore)

        userDetailsDao.deleteUserDetails()
        val retrievedUserAfter = userDetailsDao.getUserDetails().first()
        assertEquals(null, retrievedUserAfter)
    }

    @Test
    @Throws(Exception::class)
    fun insertUserAndUpdateUser() = runBlocking {
        userDetailsDao.upsertUserDetails(testUser)
        val retrievedUserBefore = userDetailsDao.getUserDetails().first()
        assertEquals(testUser, retrievedUserBefore)

        val updatedUser = testUser.copy(
            firstName = "Updated",
            lastName = "User",
        )

        userDetailsDao.upsertUserDetails(updatedUser)
        val retrievedUserAfter = userDetailsDao.getUserDetails().first()
        assertEquals(updatedUser, retrievedUserAfter)
    }

    @Test
    @Throws(Exception::class)
    fun insertUserAndUpdatePhoto() = runBlocking {
        userDetailsDao.upsertUserDetails(testUser)
        val retrievedUserBefore = userDetailsDao.getUserDetails().first()
        assertEquals(testUser, retrievedUserBefore)

        val photoPath = "updated/photo/path"
        userDetailsDao.updateProfilePhotoPath(photoPath, nowMillisInLong())

        val retrievedUserAfter = userDetailsDao.getUserDetails().first()
        assertEquals(photoPath, retrievedUserAfter!!.profilePhotoPath)
    }
}