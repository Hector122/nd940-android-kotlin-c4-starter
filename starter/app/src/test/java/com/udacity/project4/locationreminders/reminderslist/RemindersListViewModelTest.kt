package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.ArgumentMatchers.matches

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    
    //COMPLETED: provide testing to the RemindersListViewModel and its live data objects
// Use a fake repository to be injected into the viewmodel
    private lateinit var fakeDataSource: FakeDataSource
    
    // Subject under tes
    private lateinit var remindersListViewModel: RemindersListViewModel
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()
    
    @Before
    fun setUpViewModel() {
        fakeDataSource = FakeDataSource()
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
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
    fun shouldReturnError() = mainCoroutineRule.runBlockingTest {
        //GIVE
        fakeDataSource.setShouldReturnError(true)
    
        //WHEN
        remindersListViewModel.loadReminders()
    
        //THEN
        val snackbarText = remindersListViewModel.showSnackBar.getOrAwaitValue()
        assertThat(snackbarText, IsEqual("Test exception/getReminders"))
    }
    
    
    @Test
    fun loadReminders_emptyList_showNoDataTrue() = runBlocking {
        //GIVE
        fakeDataSource.deleteAllReminders()
        
        //WHEN
        remindersListViewModel.loadReminders()
        
        //THEN
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }
    
    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {
        // GIVEN
        mainCoroutineRule.pauseDispatcher()//begin
        remindersListViewModel.loadReminders()
        
        // WHEN
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()//end
        
        //THEN progress indicator is hidden
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
}