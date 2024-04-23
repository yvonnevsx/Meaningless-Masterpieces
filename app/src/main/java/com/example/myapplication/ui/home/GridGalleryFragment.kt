import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGridGalleryBinding
import com.example.myapplication.ui.home.HomeFragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.myapplication.ui.home.ArtworkDetailActivity

class GridGalleryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentGridGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val myGridGalleryItems = listOf(
            HomeFragment.GalleryItem(R.drawable.art_1, "Grid Title 1"),
            HomeFragment.GalleryItem(R.drawable.art_2, "Grid Title 2"),
            HomeFragment.GalleryItem(R.drawable.art_3, "Grid Title 3"),
            HomeFragment.GalleryItem(R.drawable.art_4, "Grid Title 4"),
            HomeFragment.GalleryItem(R.drawable.art_5, "Grid Title 5"),
            HomeFragment.GalleryItem(R.drawable.art_1, "Grid Title 6"),
            HomeFragment.GalleryItem(R.drawable.art_2, "Grid Title 7"),
            HomeFragment.GalleryItem(R.drawable.art_3, "Grid Title 8"),
            HomeFragment.GalleryItem(R.drawable.art_4, "Grid Title 9"),
            HomeFragment.GalleryItem(R.drawable.art_5, "Grid Title 10")
        )

        val spanCount = 2 // Number of columns

        binding.recyclerViewGridGallery.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
            adapter = HomeFragment.GridGalleryAdapter(myGridGalleryItems) { item ->
                val intent = Intent(requireContext(), ArtworkDetailActivity::class.java).apply {
                    putExtra("imageResId", item.imageResId)
                    putExtra("title", item.title)
                }
                startActivity(intent)
            }
        }

        return root
    }}
