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
        viewModel.navigateToElectionDetails(it)
    })

    private val savedAdapter = ElectionListAdapter(ElectionListener {
        viewModel.navigateToElectionDetails(it)
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observes()
        setupRecyclerView()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        savedAdapter.notifyDataSetChanged()
        viewModel.getUpcomingElections()
    }

    private fun observes() {
        viewModel.run {
            upcomingElections.observe(viewLifecycleOwner, { elections ->
                elections?.let {
                    upcomingAdapter.submitList(it)
                }
            })

            savedElections.observe(viewLifecycleOwner, { elections ->
                elections?.let {
                    savedAdapter.submitList(it)
                }
            })

            navigateToSelectedElection.observe(viewLifecycleOwner, { election ->
                if (election != null) {
                    startVoteInfoFragment(election)
                    viewModel.navigationIsCompleted()
                }
            })
        }
    }

    private fun startVoteInfoFragment(election: Election) {
        findNavController().navigate(
            ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(election.id, election.division)
        )
    }

    private fun setupRecyclerView() {
        binding.rvUpcomingElections.adapter = upcomingAdapter
        binding.rvSavedElections.adapter = savedAdapter
    }
}