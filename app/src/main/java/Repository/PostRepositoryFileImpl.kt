package ru.netology.nmedia.repository

import Repository.PostRepository
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dto.Post


class PostRepositoryFileImpl(
    private val context: Context,
) : PostRepository{
    private val gson = Gson()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private val filename = "posts.json"
    private var nextId = 1L
    private var post = emptyList<Post>()
    private val data = MutableLiveData(post)

    init {
        val file = context.filesDir.resolve(filename)
        if (file.exists()) {

            context.openFileInput(filename).bufferedReader().use {
                post = gson.fromJson(it, type)
                data.value = post
            }
        } else {

            sync()
        }
    }


    override fun getAll(): LiveData<List<Post>> = data
    override fun like(id:Long) {

        post = post.map {
            if (it.id == id && it.likedByMe) {
                it.copy(likeCount = it.likeCount - 1, likedByMe = false)
            } else if (it.id == id && !it.likedByMe) {
                it.copy(likeCount = it.likeCount + 1, likedByMe = true)
            } else {
                it
            }
        }

        data.value = post
        sync()



    }


    override fun save(posts: Post) {
        if (posts.id == 0L) {
            var sum = 1L
            for (p in this.post)
            {
                sum += p.id
            }
            post = listOf(
                posts.copy(
                    id = sum,
                    author = "Me",
                    likedByMe = false,
                    published = "now"
                )
            ) + post
            data.value = post
            sync()
            return
        }
        post = post.map {
            if (it.id == posts.id) it else it.copy(content = posts.content)
        }
        data.value = post
        sync()
    }

    override fun share(id: Long) {
        post = post.map {
            if (it.id != id) {
                it
            } else {
                it.copy(shareCount = it.shareCount + 1)
            }
        }
        data.value = post
        sync()
    }
    override fun removeById(id: Long)
    {
        post = post.filter { it.id != id }
        data.value =post
        sync()
    }
    private fun sync() {
        context.openFileOutput(filename, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(post))
        }
    }
}
