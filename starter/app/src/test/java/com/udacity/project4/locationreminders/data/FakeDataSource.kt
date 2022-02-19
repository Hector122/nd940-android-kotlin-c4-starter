package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.lang.Exception

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {
    
    //    Completed: Create a fake data source to act as a double to the real data source
    private var shouldReturnError = true
    
    fun setShouldReturnError(value:Boolean){
        shouldReturnError = value
    }
    
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test exception/getReminders")
        }
        reminders?.let {
            return Result.Success(ArrayList(it))
        }
        return Result.Error("Reminders not found")
    }
    
    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }
    
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception/getReminder")
        }
        reminders?.filter { it.id == id }
            .let {
                if(it != null)
                return Result.Success(it.first())
            }
        return Result.Error("Could not found reminder/getReminder")
    }
    
    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}