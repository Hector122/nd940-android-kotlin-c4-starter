package com.udacity.project4.locationreminders.savereminder


import android.content.Context
import android.provider.Settings.Global.getString
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.base.Joiner.on
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mock
import org.mockito.Mockito.mock


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    //COMPLETED: provide testing to the SaveReminderView and its live data objects
    
    // Use a fake repository to be injected into the viewmodel
    private lateinit var fakeDataSource: FakeDataSource
    
    // Subject under tes
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Mock
    private lateinit var mockContext: Context
    
    @Before
    fun setUpViewModel() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }
    
    @After
    fun clearDataSource() {
        runBlocking {
            fakeDataSource.deleteAllReminders()
        }
    }
    
    @After
    fun tearDown() {
        // to remove error A Koin Application has already been started
        // after the first text run.
        stopKoin()
    }
    
    @Test
    fun saveReminder_noEnterTitle_showSnack() {
        //GIVEN
        val reminder = ReminderDataItem(title = "",
                description = "description 1",
                location = "Location Name 1",
                latitude = 37.8199,
                longitude = -122.4783)
        //WHEN
        saveReminderViewModel.validateAndSaveReminder(reminder)
        
        //THEN
        val value = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(value, `is`(R.string.err_enter_title))
    }
    
    @Test
    fun saveReminder_noEnterLocation_showSnack() {
        //GIVEN
        val reminder = ReminderDataItem(title = "Title 1",
                description = "description 1",
                location = "",
                latitude = 37.8199,
                longitude = -122.4783)
        //WHEN
        saveReminderViewModel.validateAndSaveReminder(reminder)
    
        //THEN
        val value = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(value, `is`(R.string.err_select_location))
    }
    
    @Test
    fun saveReminder_successful_showToast() {
        //GIVEN
        val reminder = ReminderDataItem(title = "Title 1",
                description = "description 1",
                location = "Location Name",
                latitude = 37.8199,
                longitude = -122.4783)
        //WHEN
        saveReminderViewModel.validateAndSaveReminder(reminder)
    
        //THEN
        val value = saveReminderViewModel.showToast.getOrAwaitValue()
        assertThat(value, `is`("Reminder Saved !"))
    }
}
