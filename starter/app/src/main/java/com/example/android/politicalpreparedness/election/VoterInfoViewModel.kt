package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.AdministrationBody
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

class VoterInfoViewModel(
    electionId: Int,
    division: Division,
    private val electionDao: ElectionDao
) : ViewModel() {

    private val _electionAdministrationBody = MutableLiveData<AdministrationBody>()
    val electionAdministrationBody: LiveData<AdministrationBody> get() = _electionAdministrationBody

    private val _voterInfoAddress = MutableLiveData<String>()
    val voterInfoAddress: LiveData<String> get() = _voterInfoAddress

    private val _selectedElection = MutableLiveData<Election>()
    val selectedElection: LiveData<Election> get() = _selectedElection

    private val _voterInfoIsSaved = MutableLiveData<Boolean>()
    val voterInfoIsSaved: LiveData<Boolean> get() = _voterInfoIsSaved

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */
    init {
        viewModelScope.launch {
            val electionFromDatabase = electionDao.getElectionById(electionId)
            _voterInfoIsSaved.value = electionFromDatabase != null
            showListOfVoterInfo(electionId, division)
        }
    }

    private fun showListOfVoterInfo(electionId: Int, division: Division) {
        viewModelScope.launch {
            val voterInfoAddress = "${division.state}, ${division.country}"

            try {
                val voterInfoResponse =
                    CivicsApi.retrofitService.getVoterInfo(voterInfoAddress, electionId)

                _selectedElection.postValue(voterInfoResponse.election)

                voterInfoResponse.state?.let { state ->
                    if (state.isNotEmpty()) {
                        val electionAdministrationBody = state[0].electionAdministrationBody

                        electionAdministrationBody.let {
                            _electionAdministrationBody.postValue(it)
                            _voterInfoAddress.postValue(it.correspondenceAddress?.toFormattedString())
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveElectionToDatabase() {
        viewModelScope.launch {
            _voterInfoIsSaved.postValue(true)
            _selectedElection.value?.let { electionDao.insertElection(it) }
        }
    }

    fun deleteElectionByIdFromDatabase() {
        viewModelScope.launch {
            _voterInfoIsSaved.postValue(false)
            _selectedElection.value?.let { electionDao.deleteElectionById(it.id) }
        }
    }
}

class VoterInfoViewModelFactory(
    private val electionId: Int,
    private val division: Division,
    private val electionDao: ElectionDao
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoterInfoViewModel::class.java)) {
            return VoterInfoViewModel(electionId, division, electionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}