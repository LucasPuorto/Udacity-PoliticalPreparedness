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
        private const val REQUEST_LOCATION_PERMISSION = 1001
        private const val LOCATION_PERMISSION_INDEX = 0
    }

    private lateinit var binding: FragmentRepresentativeBinding
    private val viewModel: RepresentativeViewModel by viewModels()

    private val representativeAdapter = RepresentativeListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_representative, container, false)
        binding.apply {
            lifecycleOwner = this@DetailFragment
            viewModel = viewModel
        }

        setupSpinnerAdapter()

        binding.myRepresentativeRecycler.adapter = representativeAdapter

        observe()

        setupClickListeners()
        return binding.root
    }

    private fun setupSpinnerAdapter() {
        ArrayAdapter.createFromResource(
            binding.root.context,
            R.array.states,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.state.adapter = adapter
        }
    }

    private fun observe() {
        viewModel.representatives.observe(viewLifecycleOwner, { reps ->
            reps.let {
                representativeAdapter.submitList(it)
            }
        })
    }

    private fun setupClickListeners() {
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
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            false
        }

    private fun isPermissionGranted(): Boolean =
        PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val locationClient: FusedLocationProviderClient = getFusedLocationProviderClient(requireActivity())

        locationClient.lastLocation
            .addOnSuccessListener { location ->
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