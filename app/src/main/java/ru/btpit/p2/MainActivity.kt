package ru.btpit.p2

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dto.Post
import ru.btpit.p2.databinding.ActivityPostsBinding
import viewmodel.PostViewModel


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)


//kotlin.run {
//    val preferences = getPreferences(Context.MODE_PRIVATE)
//    preferences.edit().apply {
//        putString("key", "value")
//        commit()
//    }
//}
//        run {
//            getPreferences(Context.MODE_PRIVATE)
//                .getString("key", "no value")?.let {
//                    Snackbar.make(binding.root, it, BaseTransientBottomBar.LENGTH_INDEFINITE)
//                        .show()
//                }
//        }


        val viewModel: PostViewModel by viewModels()
        val adapter = PostsActivity (object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                viewModel.like(post.id)
            }
            override fun onShare(post: Post)
            {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
                viewModel.share(post.id)
            }
            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

        })



        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts)
        }

        val newPostLauncher = registerForActivityResult(NewPostResultContract()) { result ->
            result ?: return@registerForActivityResult
            viewModel.changeContent(result)
            viewModel.save()
        }

        binding.fab.setOnClickListener {
            newPostLauncher.launch()
        }
    }
}

