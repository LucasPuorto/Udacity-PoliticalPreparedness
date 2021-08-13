package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

class ElectionsViewModel(
    electionDao: ElectionDao
) : ViewModel() {

    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>> get() = _upcomingElections

    val savedElections = electionDao.getAllElections()

    private val _navigateToSelectedElection = MutableLiveData<Election>()
    val navigateToSelectedElection: LiveData<Election> get() = _navigateToSelectedElection

    init {
        getElections()
    }

    fun getElections() {
        viewModelScope.launch {
            try {
                val elections = CivicsApi.retrofitService.getElections().elections

                if (elections.isNotEmpty()) {
                    _upcomingElections.postValue(elections)
                } else {
                    _upcomingElections.postValue(arrayListOf())
                }
            } catch (e: Exception) {
                _upcomingElections.postValue(arrayListOf())
            }
        }
    }

    fun navigateToSelectedElection(election: Election) {
        _navigateToSelectedElection.postValue(election)
    }

    fun navigationIsCompleted() {
        _navigateToSelectedElection.value = null
    }
}

class ElectionsViewModelFactory(private val electionDao: ElectionDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ElectionsViewModel::class.java)) {
            return ElectionsViewModel(electionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}