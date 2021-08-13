package com.example.android.politicalpreparedness.representative.adapter

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.ItemRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Channel
import com.example.android.politicalpreparedness.representative.model.Representative

class RepresentativeListAdapter : ListAdapter<Representative, RepresentativeListAdapter.RepresentativeViewHolder>(RepresentativeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepresentativeViewHolder = RepresentativeViewHolder.from(parent)

    override fun onBindViewHolder(holder: RepresentativeViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class RepresentativeViewHolder(val binding: ItemRepresentativeBinding) : RecyclerView.ViewHolder(binding.root) {

        //TODO: Add companion object to inflate ViewHolder (from)
        companion object {

            private const val FACEBOOK_LABEL = "Facebook"
            private const val FACEBOOK_BASE_URL = "https://www.facebook.com/"
            private const val TWITTER_LABEL = "Twitter"
            private const val TWITTER_BASE_URL = "https://www.twitter.com/"

            fun from(parent: ViewGroup): RepresentativeViewHolder =
                RepresentativeViewHolder(
                    ItemRepresentativeBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }

        fun bind(item: Representative) {
            binding.representative = item
            binding.representativePhoto.setImageResource(R.drawable.ic_profile)

            //TODO: Show social links ** Hint: Use provided helper methods
            item.official.channels?.let { showSocialLinks(it) }

            //TODO: Show www link ** Hint: Use provided helper methods
            item.official.urls?.let { showWWWLinks(it) }

            binding.executePendingBindings()
        }

        private fun showSocialLinks(channels: List<Channel>) {
            val facebookUrl = getFacebookUrl(channels)
            if (!facebookUrl.isNullOrBlank()) {
                enableLink(binding.facebookIcon, facebookUrl)
            }

            val twitterUrl = getTwitterUrl(channels)
            if (!twitterUrl.isNullOrBlank()) {
                enableLink(binding.twitterIcon, twitterUrl)
            }
        }

        private fun showWWWLinks(urls: List<String>) {
            enableLink(binding.wwwIcon, urls.first())
        }

        private fun getFacebookUrl(channels: List<Channel>): String? {
            return channels.filter { channel -> channel.type == FACEBOOK_LABEL }
                .map { channel -> FACEBOOK_BASE_URL + channel.id }
                .firstOrNull()
        }

        private fun getTwitterUrl(channels: List<Channel>): String? {
            return channels.filter { channel -> channel.type == TWITTER_LABEL }
                .map { channel -> TWITTER_BASE_URL + channel.id }
                .firstOrNull()
        }

        private fun enableLink(view: ImageView, url: String) {
            view.visibility = View.VISIBLE
            view.setOnClickListener { setIntent(url) }
        }

        private fun setIntent(url: String) {
            val uri = Uri.parse(url)
            val intent = Intent(ACTION_VIEW, uri)
            itemView.context.startActivity(intent)
        }
    }

    //TODO: Create RepresentativeDiffCallback
    class RepresentativeDiffCallback : DiffUtil.ItemCallback<Representative>() {
        override fun areItemsTheSame(oldItem: Representative, newItem: Representative): Boolean =
            oldItem.office == newItem.office && oldItem.official == newItem.official

        override fun areContentsTheSame(oldItem: Representative, newItem: Representative): Boolean =
            oldItem == newItem
    }
}
