package com.example.android.politicalpreparedness.election.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.databinding.ItemElectionBinding
import com.example.android.politicalpreparedness.network.models.Election

class ElectionListAdapter(
    private val listener: ElectionListener
) : ListAdapter<Election, ElectionListAdapter.ElectionViewHolder>(ElectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectionViewHolder {
        return ElectionViewHolder.from(parent)
    }

    //TODO: Bind ViewHolder
    override fun onBindViewHolder(holder: ElectionViewHolder, position: Int) {
        val election = getItem(position)

        holder.itemView.setOnClickListener {
            listener.onClick(election)
        }

        holder.bind(election)
    }

    //TODO: Create ElectionViewHolder
    class ElectionViewHolder(private var binding: ItemElectionBinding) : RecyclerView.ViewHolder(binding.root) {

        //TODO: Add companion object to inflate ViewHolder (from)
        companion object {
            fun from(parent: ViewGroup): ElectionViewHolder =
                ElectionViewHolder(
                    ItemElectionBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }

        fun bind(election: Election) {
            binding.election = election
            binding.executePendingBindings()
        }
    }

    //TODO: Create ElectionDiffCallback
    class ElectionDiffCallback : DiffUtil.ItemCallback<Election>() {
        override fun areItemsTheSame(oldItem: Election, newItem: Election): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Election, newItem: Election): Boolean =
            oldItem.id == newItem.id
    }
}

//TODO: Create ElectionListener
class ElectionListener(val clickListener: (election: Election) -> Unit) {
    fun onClick(election: Election) = clickListener(election)
}