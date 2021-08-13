package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.network.models.AdministrationBody

class VoterInfoFragment : Fragment() {

    private lateinit var binding: FragmentVoterInfoBinding

    private val election by lazy { VoterInfoFragmentArgs.fromBundle(requireArguments()).argElectionId }
    private val division by lazy { VoterInfoFragmentArgs.fromBundle(requireArguments()).argDivision }

    private val viewModel: VoterInfoViewModel by viewModels {
        VoterInfoViewModelFactory(election, division, ElectionDatabase.getInstance(requireContext()).electionDao)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_voter_info, container, false)
        binding.apply {
            lifecycleOwner = this@VoterInfoFragment
            binding.viewModel = viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observes()
    }

    private fun observes() {
        viewModel.run {
            electionAdministrationBody.observe(viewLifecycleOwner, { administrationBody ->
                administrationBody?.let { handlerElectionAdministrationBody(it) }
            })

            voterInfoIsSaved.observe(viewLifecycleOwner, { isStateSaved ->
                isStateSaved?.let { handlerVoterInfoIsSaved(it) }
            })
        }
    }

    private fun handlerElectionAdministrationBody(administrationBody: AdministrationBody) {
        setupCorrespondenceAddressVisibility(administrationBody)
        verifyBallotInfoUrl(administrationBody)
        verifyVotingLocationFinderUrl(administrationBody)
    }

    private fun setupCorrespondenceAddressVisibility(administrationBody: AdministrationBody) {
        binding.addressGroup.visibility = if (administrationBody.correspondenceAddress != null)
            View.VISIBLE
        else
            View.GONE
    }

    private fun verifyBallotInfoUrl(administrationBody: AdministrationBody) {
        binding.stateBallot.apply {
            if (administrationBody.ballotInfoUrl != null) {
                visibility = View.VISIBLE
                setOnClickListener {
                    loadUrlIntent(administrationBody.ballotInfoUrl)
                }
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun verifyVotingLocationFinderUrl(administrationBody: AdministrationBody) {
        binding.stateLocations.apply {
            if (administrationBody.votingLocationFinderUrl != null) {
                visibility = View.VISIBLE
                setOnClickListener {
                    loadUrlIntent(administrationBody.votingLocationFinderUrl)
                }
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun handlerVoterInfoIsSaved(isStateSaved: Boolean) {
        binding.mbtFollow.apply {
            if (isStateSaved) {
                text = getString(R.string.follow)
                setOnClickListener {
                    viewModel.saveElectionToDatabase()
                    Toast.makeText(context, R.string.election_saved, Toast.LENGTH_SHORT).show()
                }
            } else {
                text = getString(R.string.unfollow)
                setOnClickListener {
                    viewModel.deleteElectionByIdFromDatabase()
                    Toast.makeText(context, R.string.election_deleted, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUrlIntent(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}