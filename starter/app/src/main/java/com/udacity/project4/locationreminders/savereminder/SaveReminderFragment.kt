package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.ACTION_GEOFENCE_EVENT
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
private const val LOCATION_PERMISSION_INDEX = 0

class SaveReminderFragment : BaseFragment() {
    
    companion object {
        private val TAG = SaveReminderFragment::class.java.simpleName
        const val GEOFENCE_RADIUS_IN_METERS = 100f
    }
    
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    
    private lateinit var geofencingClient: GeofencingClient
    
    //This will check what API the device is running
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    
    //Pending intent to handle the geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        
        //init geofence client
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        
        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//            COMPLETED: use the user entered reminder details to:
            val reminderDataItem = ReminderDataItem(title = title,
                    description = description,
                    location = location,
                    latitude = latitude,
                    longitude = longitude)
    
            //  1) add a geofencing request
            //  2) save the reminder to the local db
            if (_viewModel.validateEnteredData(reminderDataItem)) {
                addGeofenceForReminder(reminderDataItem)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
    
    /***
     * Return true if the permissions are granted and false if not
     */
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION))
        
        val backgroundPermissionApproved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            //Return true if the device is running lower than Q where you don't need a
            // permission to access location in the background.
            true
        }
        
        return foregroundLocationApproved && backgroundPermissionApproved
    }
    
    //Result of checking location setting on or off
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            // We don't rely on the result code, but just check the location setting again
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }
    
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //permission request was cancelled or
        // it means that the user denied foreground permissions. or
        //is denied it means that the device is running API 29 or above and that background permissions were denied.
        if (grantResults.isEmpty()
            || grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
            || (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                    && grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)) {
            
            //show explanation
            showSnackBarExplanation()
        } else {
            //permissions have been granted
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }
    
    private fun showSnackBarExplanation() {
        Snackbar.make(requireView(),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
    }
    
    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        //If the permissions have already been approved, you don’t need to ask again
        if (foregroundAndBackgroundLocationPermissionApproved()) return
        
        //contains the permissions that are going to be requested
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        
        //Request permissions
        requestPermissions(permissionsArray, resultCode)
    }
    
    
    /**
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create()
            .apply {
                priority = LocationRequest.PRIORITY_LOW_POWER
            }
        
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        
        // get the Settings Client
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                //prompt the user to turn on device location.
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    startIntentSenderForResult(exception.resolution.intentSender,
                            REQUEST_TURN_DEVICE_LOCATION_ON,
                            null,
                            0,
                            0,
                            0,
                            null)
                    
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(requireView(),
                        R.string.location_required_error,
                        Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                        checkDeviceLocationSettingsAndStartGeofence()
                    }
                    .show()
            }
        }
        
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "Successful location setting response")
                // device location enabled
            }
        }
    }
    
    
    @SuppressLint("MissingPermission")
    private fun addGeofenceForReminder(reminderDataItem: ReminderDataItem) {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            //Build the geofence using the geofence builder
            val geofence = Geofence.Builder()
                .setRequestId(reminderDataItem.id)
                .setCircularRegion(reminderDataItem.latitude!!,
                        reminderDataItem.longitude!!,
                        GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
            
            //Build the geofence request
            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
            
            //Add geofence
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                ?.run {
                    addOnSuccessListener {
                        // Geofences added
                        Log.i(TAG, geofence.requestId)
                    }
                    addOnFailureListener {
                        // Failed to add geofences
                        if ((it.message != null)) {
                            Log.w(TAG, it.message.toString())
                        }
                    }
    
                    // save reminder.
                    _viewModel.saveReminder(reminderDataItem)
                }
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }
}
