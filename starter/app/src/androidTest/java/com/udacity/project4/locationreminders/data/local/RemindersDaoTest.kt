package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    
    //    COMPLETED: Add testing implementation to the RemindersDao.kt
// Executes each task synchronously(same time / parallel) using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: RemindersDatabase
    
    
    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database =
            Room.inMemoryDatabaseBuilder(getApplicationContext(), RemindersDatabase::class.java)
                .build()
    }
    
    @After
    fun closeDB() = database.close()
    
    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDTO(title = "Title test dao",
                description = "description dao",
                location = "Location dao",
                latitude = 37.8199,
                longitude = -122.4783)
        
        //insert reminder
        database.reminderDao()
            .saveReminder(reminder)
        
        // WHEN
        val loaded = database.reminderDao()
            .getReminderById(reminder.id)
        
        // THEN
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }
    
    @Test
    fun deleteAllReminder() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDTO(title = "Title test dao",
                description = "description dao",
                location = "Location dao",
                latitude = 37.8199,
                longitude = -122.4783)
        
        database.reminderDao()
            .saveReminder(reminder)
        
        // WHEN
        database.reminderDao()
            .deleteAllReminders()
        
        //THEN
        val reminders = database.reminderDao()
            .getReminders()
        assertThat(reminders.size, `is`(0))
    }
}