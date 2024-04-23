package com.example.myapplication.ui.home

import GridGalleryFragment
import SampleFragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3 // Assuming you have 3 tabs

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> GridGalleryFragment()
                1 -> SampleFragment()
                2 -> GridGalleryFragment()
                else -> throw IllegalArgumentException("Invalid position $position")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup ViewPager2
        val viewPager: ViewPager2 = binding.viewPager
        viewPager.adapter = ViewPagerAdapter(this)

        // Setup TabLayout with ViewPager2
        val tabs: TabLayout = binding.tabs
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Popular"
                1 -> tab.text = "Valuable"
                2 -> tab.text = "Trash"
                // Add more cases as needed
            }
        }.attach()
        tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.blue))


        // Initialize gallery items list
        val myGalleryItems = listOf(
            GalleryItem(R.drawable.art_6, "Landscapes"),
            GalleryItem(R.drawable.art_7, "Design"),
            GalleryItem(R.drawable.art_8, "People"),
            GalleryItem(R.drawable.art_9, "Urban Landscapes"),
            GalleryItem(R.drawable.art_10, "Trash Art"),
            GalleryItem(R.drawable.art_11, "Abstract")
            // Add your items here
        )

        binding.recyclerViewGallery.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = HorizontalGalleryAdapter(myGalleryItems) { galleryItem ->
                // Intent to start ArtworkDetailActivity
                val intent = Intent(requireContext(), ArtworkDetailActivity::class.java).apply {
                    putExtra("imageResId", galleryItem.imageResId)
                    putExtra("title", galleryItem.title)
                }
                startActivity(intent)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    // Adapter for the horizontal RecyclerView
    class HorizontalGalleryAdapter(private val items: List<GalleryItem>, private val clickListener: (GalleryItem) -> Unit) :
        RecyclerView.Adapter<HorizontalGalleryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.imageView_gallery_item)
            val titleView: TextView = view.findViewById(R.id.textView_gallery_title)

            init {
                view.setOnClickListener {
                    clickListener(items[adapterPosition])
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            Glide.with(holder.imageView.context).load(item.imageResId).into(holder.imageView)
            holder.titleView.text = item.title
        }

        override fun getItemCount(): Int = items.size
    }

    interface OnItemClickListener {
        fun onItemClick(item: GalleryItem)
    }

    // Adapter for the grid RecyclerView
    class GridGalleryAdapter(private val items: List<GalleryItem>, private val clickListener: (GalleryItem) -> Unit) :
        RecyclerView.Adapter<GridGalleryAdapter.ViewHolder>() {

        private val aspectRatios = listOf(1.0f, 1.5f, 1.0f, 0.75f) // Example ratios

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.imageView_grid_item)

            init {
                view.setOnClickListener {
                    clickListener(items[adapterPosition])
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_grid_gallery, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            Glide.with(holder.imageView.context)
                .load(item.imageResId)
                .into(holder.imageView)
            val aspectRatio = aspectRatios[position % aspectRatios.size]
            val fixedWidth =
                holder.imageView.resources.getDimensionPixelSize(R.dimen.image_width) // Define this dimension in your resources
            val height =
                (fixedWidth * aspectRatio).toInt() // Calculate the height based on the aspect ratio
            val layoutParams = holder.imageView.layoutParams

            layoutParams.height = height
            holder.imageView.layoutParams = layoutParams
            holder.imageView.setImageResource(item.imageResId)
        }

        override fun getItemCount(): Int = items.size
    }

    data class FilterItem(val name: String)
    class FilterAdapter(private val items: List<FilterItem>) :
        RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val filterName: TextView = view.findViewById(R.id.textView_filter)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.filterName.text = item.name
        }

        override fun getItemCount(): Int = items.size
    }

    // Data class for gallery items
    data class GalleryItem(
        val imageResId: Int,
        val title: String
    )
}
