package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(appContext, get() as ReminderDataSource)
            }
            single {
                SaveReminderViewModel(appContext, get() as ReminderDataSource)
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = GlobalContext.get().koin.get()
        
        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
        
    }
    
    //    COMPLETED: test the navigation of the fragments.
    @Test
    fun clickOnFab_navigateToSaveReminderFragment() {
        val reminderListFragment =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(reminderListFragment)
        
        val navController = Mockito.mock(NavController::class.java)
        
        reminderListFragment.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        
        // Click Fab button
        onView(withId(R.id.addReminderFAB)).perform(click())
        
        // Navigate back to SaveReminderFragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
    
    //   COMPLETED: test the displayed data on the UI.
    @Test
    fun addReminders_displayDataOnUI() {
        val reminders = listOf(ReminderDTO(title = "Title 1",
                description = "Description 1",
                location = "Location Name 1",
                latitude = 37.8199,
                longitude = -122.4783),
                ReminderDTO(title = "Title 2",
                        description = "Description 2",
                        location = "Location Name 12",
                        latitude = 37.8199,
                        longitude = -122.4783),
                ReminderDTO(title = "Title 3",
                        description = "Description 3",
                        location = "Location Name 3",
                        latitude = 37.8199,
                        longitude = -122.4783))
        
        runBlocking {
            for (reminder in reminders) {
                repository.saveReminder(reminder)
            }
        }
        
        val reminderListFragment =
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(reminderListFragment)
        
        val navController = mock(NavController::class.java)
        reminderListFragment.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        
        onView(withText(reminders[0].title)).check(ViewAssertions.matches(isDisplayed()))
        onView(withText(reminders[0].description)).check(ViewAssertions.matches(isDisplayed()))
        onView(withText(reminders[0].location)).check(ViewAssertions.matches(isDisplayed()))
        
        onView(withText(reminders[1].title)).check(ViewAssertions.matches(isDisplayed()))
        onView(withText(reminders[1].description)).check(ViewAssertions.matches(isDisplayed()))
        onView(withText(reminders[1].location)).check(ViewAssertions.matches(isDisplayed()))
    }
    
    //    COMPLETED: add testing for the error messages.
    @Test
    fun remindersEmptyList_showNoData() {
        val reminderListFragment =
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(reminderListFragment)
        
        val navController = mock(NavController::class.java)
        
        reminderListFragment.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(isDisplayed()))
    }
}