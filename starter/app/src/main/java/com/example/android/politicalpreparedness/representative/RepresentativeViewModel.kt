package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel : ViewModel() {

    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>> get() = _representatives

    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address> get() = _address

    /**
     *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official :

    val (offices, officials) = getRepresentativesDeferred.await()
    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data housing representatives

     */
    private fun findMatchingRepresentatives() {
        if (_address.value != null) {
            viewModelScope.launch {
                try {
                    val addressString = _address.value?.toFormattedString()
                    val representativeResponse = addressString?.let { CivicsApi.retrofitService.getRepresentatives(it) }

                    _representatives.value = representativeResponse?.offices?.flatMap { office ->
                        office.getRepresentatives(representativeResponse.officials)
                    }
                } catch (ex: Exception) {
                    _representatives.value = ArrayList()
                }
            }
        } else {
            _representatives.value = ArrayList()
        }
    }

    fun onSearchRepresentativesByAddress(address: Address) {
        _address.value = address
        findMatchingRepresentatives()
    }
}