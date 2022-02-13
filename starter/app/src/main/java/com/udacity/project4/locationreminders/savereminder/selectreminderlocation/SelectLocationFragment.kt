package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_save_reminder.*
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    companion object {
        private val TAG = SelectLocationFragment::class.java.simpleName
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val ZOOM_LEVEL = 15f
    }
    
    private lateinit var map: GoogleMap
    private var pointOfInterest: PointOfInterest? = null
    
    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        
        //COMPLETED: add the map setup implementation
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        Completed: call this function after the user confirms on the selected location
        binding.buttonSave.setOnClickListener {
            onLocationSelected()
        }
        return binding.root
    }
    
    // Callback for the result from requesting permissions.
    // This method is invoked for every call on requestPermissions(android.app.Activity, String[], int).
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }
    
    private fun onLocationSelected() {
        //        COMPLETED: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        if (pointOfInterest != null) {
            _viewModel.reminderSelectedLocationStr.value = pointOfInterest?.name
            _viewModel.longitude.value = pointOfInterest?.latLng?.longitude
            _viewModel.latitude.value = pointOfInterest?.latLng?.latitude
            _viewModel.selectedPOI.value = pointOfInterest
            
            //navigate back
            _viewModel.navigationCommand.value = NavigationCommand.Back
        } else {
            _viewModel.showSnackBar.value = getString(R.string.select_poi)
        }
    }
    
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // COMPLETED: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        
        //COMPLETED: zoom to the user location after taking his permission
        enableMyLocation()
        
        //Get user last know location.
        moveToDeviceLocation()
        
        // add style to the map
        setMapStyle(map)
        
        // COMPLETED: put a marker to location that the user selected
        setMapLongClick(map)
        setPoiClick(map)
    }
    
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions()
                return
            }
    
            // Enable location
            map.isMyLocationEnabled = true
        } else {
            requestPermissions()
        }
    }
    
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
    }
    
    
    /**
     * Get the current location of the device and set the position of the map.
     */
    @SuppressLint("MissingPermission")
    private fun moveToDeviceLocation() {
        // if location not enable get out.
        if (!map.isMyLocationEnabled) return
        
        val fusedLocationProviderClient = FusedLocationProviderClient(requireContext())
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful && task.result != null) {
                // Set the map's camera position to the last current location of the device.
                val lastKnownLocation = LatLng(task.result!!.latitude, task.result!!.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, ZOOM_LEVEL))
            } else {
                Log.d(TAG, "Current location is null. Using defaults.")
                Log.e(TAG, "Exception: %s", task.exception)
            }
        }
    }
    
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Add a Marker on long click in the map.
     */
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            //Remove previous market.
            map.clear()
            
            // A Snippet is Additional text that's displayed below the title
            val snippet = String.format(Locale.getDefault(),
                    getString(R.string.lat_long_snippet),
                    latLng.latitude,
                    latLng.longitude)
            
            //Add marker
            map.addMarker(MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker())
                .title(getString(R.string.dropped_pin))
                .snippet(snippet))
        }
    }
    
    /**
     *  Add points of interest (POIs)
     */
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { pointOfInterest ->
            //Remove previous market.
            map.clear()
            
            //set global POI
            this.pointOfInterest = pointOfInterest
            
            //set POI data to UI
            val poiMarker = map.addMarker(MarkerOptions().position(pointOfInterest.latLng)
                .title(pointOfInterest.name))
            
            //immediately show the info window.
            poiMarker.showInfoWindow()
        }
    }
    
    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),
                    R.raw.map_style))
            
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }
}
