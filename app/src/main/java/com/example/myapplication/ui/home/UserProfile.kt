package com.example.myapplication.ui.home

import SampleFragment
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import profileFragment

class UserProfile : AppCompatActivity() {
    private lateinit var followButton: Button
    private var viewPager: ViewPager2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        supportActionBar?.hide()

        val localViewPager = findViewById<ViewPager2>(R.id.view_pager)
        viewPager = localViewPager  // Assign to your property if needed elsewhere

        if (localViewPager != null) {
            localViewPager.adapter = UserProfilePagerAdapter(this)
            val tabLayout = findViewById<TabLayout>(R.id.tabs)

            if (tabLayout != null) {
                TabLayoutMediator(tabLayout, localViewPager) { tab, position ->
                    when (position) {
                        0 -> tab.text = "My creations"
                        1 -> tab.text = "Collection"
                        2 -> tab.text = "Favorites"
                    }
                }.attach()
            }

            // Set up the follow button
            followButton = findViewById<Button>(R.id.follow_button)
            followButton.setOnClickListener {
                // Toggle the text based on the current state
                if (followButton.text.toString().contains("Follow")) {
                    followButton.text = "Unfollow"
                    followButton.setBackgroundResource(R.drawable.filter_item_default_background)
                } else {
                    followButton.text = "+ Follow" }
            }
        }

    }
}


private class UserProfilePagerAdapter(activity: AppCompatActivity?) :
    FragmentStateAdapter(activity!!) {
    override fun getItemCount(): Int {
        return 3 // Number of tabs
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> profileFragment()
            1 -> SampleFragment()
            2 -> profileFragment()
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}
