package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {
    
    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob
    
    companion object {
        private const val JOB_ID = 573
        private val TAG = GeofenceTransitionsJobIntentService::class.java.name
        
        //        COMPLETED: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, GeofenceTransitionsJobIntentService::class.java, JOB_ID, intent)
        }
    }
    
    override fun onHandleWork(intent: Intent) {
        //COMPLETED: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = errorMessage(baseContext, geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }
        
        //COMPLETED call @sendNotification
        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            sendNotification(geofencingEvent.triggeringGeofences)
        }
    }
    
    //COMPLETED: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        // Since the user might want to add multiple reminders at the same location, please make
        // sure to use a for loop to loop through all triggering Geofences instead of just getting the first one.
        for (geofence in triggeringGeofences) {
            val requestId = geofence.requestId
            
            // Get the local repository instance
            // fix bug reference: https://github.com/udacity/nd940-android-kotlin-c4-starter/pull/2
            val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(this@GeofenceTransitionsJobIntentService,
                            ReminderDataItem(reminderDTO.title,
                                    reminderDTO.description,
                                    reminderDTO.location,
                                    reminderDTO.latitude,
                                    reminderDTO.longitude,
                                    reminderDTO.id))
                }
            }
        }
    }
    
    
    /**
     * Returns the error string for a geofencing error code.
     */
    private fun errorMessage(context: Context, errorCode: Int): String {
        val resources = context.resources
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(R.string.geofence_not_available)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(R.string.geofence_too_many_geofences)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(R.string.geofence_too_many_pending_intents)
            else -> resources.getString(R.string.geofence_unknown_error)
        }
    }
}