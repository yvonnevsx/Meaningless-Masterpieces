package com.example.myapplication.ui.notifications

import GridGalleryFragment
import SampleFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentNotificationsBinding
//import com.example.myapplication.ui.home.SampleFragment
import com.google.android.material.tabs.TabLayoutMediator
import profileFragment

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3 // Number of tabs

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> profileFragment()
                1 -> SampleFragment()
                2 -> profileFragment()
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set up the ViewPager2 and TabLayout
        val viewPager = binding.viewPager
        viewPager.adapter = ViewPagerAdapter(this)

        val tabs = binding.tabs // Make sure you have a TabLayout with id 'tabs' in your XML
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "My Art"
                1 -> "Collection"
                2 -> "Favorites"
                else -> null
            }
        }.attach()
        tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.blue))

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
