package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class DetailFragment : Fragment() {

    companion object {
        //TODO: Add Constant for Location request
        private const val REQUEST_LOCATION_PERMISSION = 1001
        private const val LOCATION_PERMISSION_INDEX = 0
    }

    //TODO: Declare ViewModel
    private lateinit var binding: FragmentRepresentativeBinding
    private val viewModel: RepresentativeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        //TODO: Establish bindings
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_representative, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // spinner :O
        ArrayAdapter.createFromResource(binding.root.context, R.array.states, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.state.adapter = adapter
        }

        //TODO: Define and assign Representative adapter
        val representativeAdapter = RepresentativeListAdapter()
        binding.myRepresentativeRecycler.adapter = representativeAdapter

        //TODO: Populate Representative adapter
        viewModel.representatives.observe(viewLifecycleOwner, { reps ->
            reps.let {
                representativeAdapter.submitList(it)
            }
        })

        //TODO: Establish button listeners for field and location search
        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            val address = Address(
                binding.addressLine1.text.toString(),
                binding.addressLine2.text.toString(),
                binding.city.text.toString(),
                binding.state.selectedItem.toString(),
                binding.zip.text.toString()
            )

            viewModel.onSearchRepresentativesByAddress(address)
        }

        binding.buttonLocation.setOnClickListener {
            if (checkLocationPermissions()) {
                getLocation()
            }
        }

        return binding.root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //TODO: Handle location permission result to get location on permission granted
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
        ) {
            Snackbar.make(
                requireView(),
                R.string.permission_required,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
        } else {
            getLocation()
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun checkLocationPermissions(): Boolean =
        if (isPermissionGranted()) {
            true
        } else {
            //TODO: Request Location permissions
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            false
        }

    private fun isPermissionGranted(): Boolean =
        //TODO: Check if permission is already granted and return (true = granted, false = denied/other)
        PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        //TODO: Get location from LocationServices
        val locationClient: FusedLocationProviderClient = getFusedLocationProviderClient(requireActivity())

        locationClient.lastLocation
            .addOnSuccessListener { location -> // GPS location can be null if GPS is switched off
                //TODO: The geoCodeLocation method is a helper function to change the lat/long location to a human readable street address
                location?.let {
                    val address = geoCodeLocation(it)
                    viewModel.onSearchRepresentativesByAddress(address)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun geoCodeLocation(location: Location): Address =
        Geocoder(context, Locale.getDefault())
            .getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
            }
            .first()
}