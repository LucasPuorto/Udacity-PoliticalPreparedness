package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

//TODO: Construct ViewModel and provide election datasource
class ElectionsViewModel(electionDao: ElectionDao) : ViewModel() {

    //TODO: Create live data val for upcoming elections
    val upcomingElections = MutableLiveData<List<Election>>()

    //TODO: Create live data val for saved elections
    val savedElections = electionDao.getAllElections()

    //TODO: Create functions to navigate to saved or upcoming election voter info
    private val _navigateToSelectedElection = MutableLiveData<Election>()
    val navigateToSelectedElection: LiveData<Election> get() = _navigateToSelectedElection

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database
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