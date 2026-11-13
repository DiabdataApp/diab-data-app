package com.diabdata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.Appointment
import com.diabdata.core.model.nowMillis
import com.diabdata.feature.appointments.data.AppointmentDao
import com.diabdata.shared.utils.dataTypes.AppointmentType
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
 * Tests for [AppointmentDao] found at [com.diabdata.feature.appointments.data.AppointmentDao]
 */
@RunWith(AndroidJUnit4::class)
class AppointmentDaoTest {
    private lateinit var db: DiabDataDatabase
    private lateinit var appointmentDao: AppointmentDao

    val today: LocalDate = LocalDate.now()
    val now = nowMillis()

    val appointment = Appointment(
        id = 1,
        date = today.atTime(12, 0),
        doctor = "Dr. Johnson",
        type = AppointmentType.APPOINTMENT,
        createdAt = now,
        isArchived = false,
        notes = "Initial appointment today",
        updatedAt = now,
        entryUuid = "appointment-1"
    )
    val appointment2 = appointment.copy(
        id = 2,
        date = today.minusMonths(8).plusDays(15).atTime(9, 0),
        notes = "past appointment",
        type = AppointmentType.ANNUAL_CHECKUP,
        entryUuid = "appointment-2"
    )
    val appointment3 = appointment.copy(
        id = 3,
        date = today.plusMonths(3).plusDays(8).atTime(16, 30),
        doctor = "Dr. Smith",
        entryUuid = "appointment-3"
    )
    val appointment4 = appointment.copy(
        id = 4,
        date = today.plusMonths(3).plusDays(20).atTime(16, 30),
        doctor = "Dr. Smith",
        type = AppointmentType.ANNUAL_CHECKUP,
        entryUuid = "appointment-4"
    )

    val allAppointments = listOf(appointment, appointment2, appointment3, appointment4)

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            DiabDataDatabase::class.java
        ).build()
        appointmentDao = db.appointmentDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAppointmentTest() = runBlocking {
        appointmentDao.insert(appointment)

        val appointments = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointments.size)
        assertEquals(appointment, appointments[0])
    }

    @Test
    @Throws(Exception::class)
    fun deleteAppointmentTest() = runBlocking {
        appointmentDao.insert(appointment)

        val appointmentsBefore = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointmentsBefore.size)
        assertEquals(appointment, appointmentsBefore[0])

        appointmentDao.deleteById(appointment.id)
        val appointmentsAfter = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(0, appointmentsAfter.size)
    }

    @Test
    @Throws(Exception::class)
    fun updateAppointmentTest() = runBlocking {
        appointmentDao.insert(appointment)

        val appointmentsBefore = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointmentsBefore.size)
        assertEquals(appointment, appointmentsBefore[0])

        appointmentDao.update(appointment.copy(doctor = "Dr. Smith"))
        val appointmentsAfter = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointmentsAfter.size)
        assertEquals("Dr. Smith", appointmentsAfter[0].doctor)
    }

    @Test
    @Throws(Exception::class)
    fun setAppointmentAsArchivedTest() = runBlocking {
        appointmentDao.insert(appointment)

        val appointmentsBefore = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointmentsBefore.size)
        assertEquals(appointment, appointmentsBefore[0])

        appointmentDao.update(appointment.copy(isArchived = true))
        val appointmentsAfterArchive = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointmentsAfterArchive.size)
        assertEquals(true, appointmentsAfterArchive[0].isArchived)

        appointmentDao.update(appointment.copy(isArchived = false))
        val appointmentsAfterUnarchive = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointmentsAfterUnarchive.size)
        assertEquals(false, appointmentsAfterUnarchive[0].isArchived)
    }

    @Test
    @Throws(Exception::class)
    fun getAllAppointmentsTest() = runBlocking {
        allAppointments.forEach { appointmentDao.insert(it) }

        val appointments = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(4, appointments.size)
    }

    @Test
    @Throws(Exception::class)
    fun getAllUpcomingAppointmentsTest() = runBlocking {
        allAppointments.forEach { appointmentDao.insert(it) }

        val appointments = appointmentDao.getUpcomingAppointmentsFlow(today.atTime(0,0)).first()
        assertEquals(3, appointments.size)
        assertEquals(appointment, appointments[0])
    }
}