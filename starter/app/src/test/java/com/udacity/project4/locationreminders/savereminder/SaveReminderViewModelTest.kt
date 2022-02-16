package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.util.getOrAwaitValue

import com.udacity.project4.utils.SingleLiveEvent


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.bouncycastle.asn1.crmf.EncryptedValue
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

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
    
    @Test
    fun saveReminder_noEnterTitle_showToast() {
        //GIVEN
        val reminder = ReminderDataItem(title = "",
                description = "description 1",
                location = "Location Name",
                latitude = 37.8199,
                longitude = -122.4783)
        //WHEN
        saveReminderViewModel.validateAndSaveReminder(reminder)
        
        //THEN
        val value = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(value, `is`(R.string.err_enter_title))
    }
    
    @Test
    fun saveReminder_noEnterLocation_showToast() {
        //WHEN
        
        //THEN
    }
    
    @Test
    fun saveReminder_successful() {
        //WHEN
        
        //THEN
    }
    
    
}
