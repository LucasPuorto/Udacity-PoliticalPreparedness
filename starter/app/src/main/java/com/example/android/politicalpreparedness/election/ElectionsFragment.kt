package com.example.android.politicalpreparedness.election

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.network.models.Election

class ElectionsFragment : Fragment() {

    private val viewModel: ElectionsViewModel by viewModels {
        ElectionsViewModelFactory(ElectionDatabase.getInstance(requireContext()).electionDao)
    }
    private lateinit var binding: FragmentElectionBinding

    private val upcomingAdapter = ElectionListAdapter(ElectionListener {
        viewModel.navigateToSelectedElection(it)
    })

    private val savedAdapter = ElectionListAdapter(ElectionListener {
        viewModel.navigateToSelectedElection(it)
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)
        binding.apply {
            lifecycleOwner = this@ElectionsFragment
            viewModel = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observes()
        setupRecyclerViews()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        savedAdapter.notifyDataSetChanged()
        viewModel.getElections()
    }

    private fun observes() {
        viewModel.run {
            upcomingElections.observe(viewLifecycleOwner, { elections ->
                handlerUpcomingElections(elections)
            })

            savedElections.observe(viewLifecycleOwner, { elections ->
                handlerSavedElections(elections)
            })

            navigateToSelectedElection.observe(viewLifecycleOwner, { election ->
                handlerNavigateToSelectedElection(election)
            })
        }
    }

    private fun handlerUpcomingElections(elections: List<Election>) {
        elections.let {
            upcomingAdapter.submitList(it)
        }
    }

    private fun handlerSavedElections(elections: List<Election>) {
        elections.let {
            savedAdapter.submitList(it)
        }
    }

    private fun handlerNavigateToSelectedElection(election: Election) {
        startVoteInfoFragment(election)
        viewModel.navigationIsCompleted()
    }

    private fun startVoteInfoFragment(election: Election) {
        findNavController().navigate(
            ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(election.id, election.division)
        )
    }

    private fun setupRecyclerViews() {
        binding.rvUpcomingElections.adapter = upcomingAdapter
        binding.rvSavedElections.adapter = savedAdapter
    }
}