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

class VoterInfoFragment : Fragment() {

    private lateinit var binding: FragmentVoterInfoBinding

    private val election by lazy { VoterInfoFragmentArgs.fromBundle(requireArguments()).argElectionId }
    private val division by lazy { VoterInfoFragmentArgs.fromBundle(requireArguments()).argDivision }

    private val viewModel: VoterInfoViewModel by viewModels {
        VoterInfoViewModelFactory(election, division, ElectionDatabase.getInstance(requireContext()).electionDao)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        //TODO: Add binding values
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_voter_info, container, false)
        //TODO: Add ViewModel values and create ViewModel
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO: Populate voter info -- hide views without provided data.
        /**
         * Hint: You will need to ensure proper data is provided from previous fragment.
         */
        viewModel.electionAdministrationBody.observe(viewLifecycleOwner, { administrationBody ->
            binding.addressGroup.visibility = if (administrationBody.correspondenceAddress != null) View.VISIBLE else View.GONE

            //TODO: Handle loading of URLs
            if (administrationBody.ballotInfoUrl != null) {
                binding.stateBallot.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        loadUrlIntent(administrationBody.ballotInfoUrl)
                    }
                }
            } else {
                binding.stateBallot.visibility = View.GONE
            }

            if (administrationBody.votingLocationFinderUrl != null) {
                binding.stateLocations.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        loadUrlIntent(administrationBody.votingLocationFinderUrl)
                    }
                }
            } else {
                binding.stateLocations.visibility = View.GONE
            }
        })

        //TODO: Handle save button UI state
        viewModel.voterInfoIsSaved.observe(viewLifecycleOwner, { isStateSaved ->
            //TODO: cont'd Handle save button clicks
            when (isStateSaved) {
                false -> {
                    binding.mbtFollow.apply {
                        text = getString(R.string.follow)

                        setOnClickListener {
                            viewModel.saveElectionToDatabase()
                            Toast.makeText(context, R.string.election_saved, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                true -> {
                    binding.mbtFollow.apply {
                        text = getString(R.string.unfollow)

                        setOnClickListener {
                            viewModel.deleteElectionByIdFromDatabase()
                            Toast.makeText(context, R.string.election_deleted, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    //TODO: Create method to load URL intents
    private fun loadUrlIntent(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}