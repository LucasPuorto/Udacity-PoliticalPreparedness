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

class ElectionsViewModel(electionDao: ElectionDao) : ViewModel() {

    val upcomingElections = MutableLiveData<List<Election>>()

    val savedElections = electionDao.getAllElections()

    private val _navigateToSelectedElection = MutableLiveData<Election>()
    val navigateToSelectedElection: LiveData<Election> get() = _navigateToSelectedElection

    init {
        getUpcomingElections()
    }

    fun getUpcomingElections() {
        viewModelScope.launch {
            try {
                val result = CivicsApi.retrofitService.getElections().elections

                if (result.isNotEmpty()) {
                    upcomingElections.postValue(result)
                } else {
                    upcomingElections.postValue(ArrayList())
                }
            } catch (e: Exception) {
                upcomingElections.postValue(ArrayList())
            }
        }
    }

    fun navigateToElectionDetails(election: Election) {
        _navigateToSelectedElection.postValue(election)
    }

    fun navigationIsCompleted() {
        _navigateToSelectedElection.value = null
    }
}

class ElectionsViewModelFactory(private val electionDao: ElectionDao): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ElectionsViewModel::class.java)) {
            return ElectionsViewModel(electionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}