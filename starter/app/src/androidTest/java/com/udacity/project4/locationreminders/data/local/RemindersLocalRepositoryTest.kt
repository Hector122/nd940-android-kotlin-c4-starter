package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import javax.sql.DataSource

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
//    COMPLETED: Add testing implementation to the RemindersLocalRepository.kt
    
    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var remindersDatabase: RemindersDatabase
    
    // Executes task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    
    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        remindersDatabase =
            Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                    RemindersDatabase::class.java)
                .allowMainThreadQueries()//Don't do this in production code!
                .build()
        
        localDataSource =
            RemindersLocalRepository(remindersDatabase.reminderDao(), Dispatchers.Main)
    }
    
    @After
    fun cleanUp() {
        remindersDatabase.close()
    }
    
    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN
        val reminder = ReminderDTO(title = "Title 1",
                description = "description 1",
                location = "Location Name 1",
                latitude = 37.8199,
                longitude = -122.4783)
        
        localDataSource.saveReminder(reminder)
        
        // WHEN  - Task retrieved by ID
        val result = localDataSource.getReminder(reminder.id)
        
        // THEN - Same task is returned
        Assert.assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        Assert.assertThat(result.data.title, `is`(reminder.title))
        Assert.assertThat(result.data.description, `is`(reminder.description))
        Assert.assertThat(result.data.location, `is`(reminder.location))
        Assert.assertThat(result.data.latitude, `is`(reminder.latitude))
        Assert.assertThat(result.data.longitude, `is`(reminder.longitude))
    }
}