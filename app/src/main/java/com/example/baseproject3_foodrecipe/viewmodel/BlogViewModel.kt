package com.example.baseproject3_foodrecipe.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject3_foodrecipe.model.BlogPost
import com.example.baseproject3_foodrecipe.model.BlogRepository
import com.example.baseproject3_foodrecipe.utils.LocalImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "BlogViewModel"

class BlogViewModel : ViewModel() {
    private val blogRepository = BlogRepository()

    private val _featuredBlogs = MutableStateFlow<List<BlogPost>>(emptyList())
    val featuredBlogs: StateFlow<List<BlogPost>> = _featuredBlogs.asStateFlow()

    private val _allBlogs = MutableStateFlow<List<BlogPost>>(emptyList())
    val allBlogs: StateFlow<List<BlogPost>> = _allBlogs.asStateFlow()

    // Combined blogs for main blog list
    private val _blogs = MutableStateFlow<List<BlogPost>>(emptyList())
    val blogs: StateFlow<List<BlogPost>> = _blogs.asStateFlow()

    private val _userBlogs = MutableStateFlow<List<BlogPost>>(emptyList())
    val userBlogs: StateFlow<List<BlogPost>> = _userBlogs.asStateFlow()

    private val _currentBlog = MutableStateFlow<BlogPost?>(null)
    val currentBlog: StateFlow<BlogPost?> = _currentBlog.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

    fun loadFeaturedBlogs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val blogs = blogRepository.getFeaturedBlogs()
                _featuredBlogs.value = blogs
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load featured blogs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllBlogs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val blogs = blogRepository.getAllBlogs()
                _allBlogs.value = blogs
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load blogs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Get all blogs for main blog list
    fun getAllBlogs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val blogs = blogRepository.getAllBlogs()
                _blogs.value = blogs
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load blogs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load blogs created by a specific user
     */
    fun loadUserBlogs(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val blogs = blogRepository.getBlogsByAuthor(userId)
                _userBlogs.value = blogs
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error loading blogs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getBlogById(blogId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val blog = blogRepository.getBlogById(blogId)
                _currentBlog.value = blog
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get blog: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getBlogsByCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val blogs = blogRepository.getBlogsByCategory(category)
                _allBlogs.value = blogs
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get blogs by category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveImageAndCreateBlogPost(
        context: Context,
        imageUri: Uri,
        title: String,
        content: String,
        summary: String,
        authorId: String,
        authorName: String,
        category: String,
        readTime: Int,
        featured: Boolean = false
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false

            try {
                // Save image to local storage
                val imagePath = LocalImageStorage.saveImage(context, imageUri, LocalImageStorage.BLOG)

                // Create blog post with image path
                createBlogPost(
                    title = title,
                    content = content,
                    summary = summary,
                    imageUrl = imagePath,
                    authorId = authorId,
                    authorName = authorName,
                    category = category,
                    readTime = readTime,
                    featured = featured
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error saving image and creating blog post: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun createBlogPost(
        title: String,
        content: String,
        summary: String,
        imageUrl: String = "",
        authorId: String,
        authorName: String,
        category: String,
        readTime: Int,
        featured: Boolean = false
    ) {
        Log.d(TAG, "createBlogPost called")
        Log.d(TAG, "Title: $title, Author: $authorName ($authorId)")
        Log.d(TAG, "Category: $category, Featured: $featured")

        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false

            try {
                Log.d(TAG, "Creating blog post...")
                val blogId = UUID.randomUUID().toString()
                val publishDate = System.currentTimeMillis()

                val blog = BlogPost(
                    id = blogId,
                    title = title,
                    content = content,
                    summary = summary,
                    imageUrl = imageUrl,
                    authorId = authorId,
                    authorName = authorName,
                    category = category,
                    readTime = readTime,
                    publishDate = publishDate,
                    featured = featured,
                    likes = 0,
                    commentCount = 0
                )

                Log.d(TAG, "Blog object created, calling repository")
                val success = blogRepository.createBlogPost(blog)
                Log.d(TAG, "Repository returned: $success")

                if (success) {
                    Log.d(TAG, "Blog post created successfully")
                    _operationSuccess.value = true
                    _errorMessage.value = null

                    // Refresh blogs
                    getAllBlogs()
                    loadUserBlogs(authorId)
                    if (featured) {
                        loadFeaturedBlogs()
                    }
                } else {
                    Log.e(TAG, "Failed to create blog post")
                    _errorMessage.value = "Failed to create blog post"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating blog post: ${e.message}", e)
                _errorMessage.value = "Error creating blog post: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBlogPost(
        blogId: String,
        title: String,
        content: String,
        summary: String,
        imageUrl: String,
        category: String,
        readTime: Int,
        featured: Boolean,
        userId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false

            try {
                val currentBlog = blogRepository.getBlogById(blogId)

                if (currentBlog == null) {
                    _errorMessage.value = "Blog post not found"
                    _isLoading.value = false
                    return@launch
                }

                if (currentBlog.authorId != userId) {
                    _errorMessage.value = "You can only update your own blog posts"
                    _isLoading.value = false
                    return@launch
                }

                val updatedBlog = currentBlog.copy(
                    title = title,
                    content = content,
                    summary = summary,
                    imageUrl = imageUrl,
                    category = category,
                    readTime = readTime,
                    featured = featured
                )

                val success = blogRepository.updateBlogPost(updatedBlog)

                if (success) {
                    _operationSuccess.value = true
                    _errorMessage.value = null
                    _currentBlog.value = updatedBlog

                    // Refresh blogs
                    getAllBlogs()
                    loadUserBlogs(userId)
                    loadFeaturedBlogs()
                } else {
                    _errorMessage.value = "Failed to update blog post"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating blog post: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a blog post (without author verification)
     */
    fun deleteBlogPost(blogId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = blogRepository.deleteBlogPost(blogId)
                if (success) {
                    // Remove the deleted blog from the lists
                    _allBlogs.value = _allBlogs.value.filter { it.id != blogId }
                    _userBlogs.value = _userBlogs.value.filter { it.id != blogId }
                    _featuredBlogs.value = _featuredBlogs.value.filter { it.id != blogId }
                    _blogs.value = _blogs.value.filter { it.id != blogId }

                    if (_currentBlog.value?.id == blogId) {
                        _currentBlog.value = null
                    }

                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to delete blog post"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting blog post: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBlogPost(blogId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false

            try {
                val success = blogRepository.deleteBlogPost(blogId, userId)

                if (success) {
                    _operationSuccess.value = true
                    _errorMessage.value = null

                    // Refresh blogs
                    getAllBlogs()
                    loadUserBlogs(userId)
                    loadFeaturedBlogs()
                } else {
                    _errorMessage.value = "Failed to delete blog post"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting blog post: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adminDeleteBlogPost(blogId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false

            try {
                // Get the blog to find the author
                val blog = blogRepository.getBlogById(blogId)
                val authorId = blog?.authorId

                val success = blogRepository.adminDeleteBlogPost(blogId)

                if (success) {
                    _operationSuccess.value = true
                    _errorMessage.value = null

                    // Refresh blogs
                    getAllBlogs()
                    if (authorId != null) {
                        loadUserBlogs(authorId)
                    }
                    loadFeaturedBlogs()
                } else {
                    _errorMessage.value = "Failed to delete blog post"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting blog post: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likeBlogPost(blogId: String, userId: String) {
        viewModelScope.launch {
            try {
                val success = blogRepository.likeBlogPost(blogId, userId)

                if (success) {
                    // Update current blog if it's the one being liked
                    if (_currentBlog.value?.id == blogId) {
                        val updatedBlog = blogRepository.getBlogById(blogId)
                        _currentBlog.value = updatedBlog
                    }

                    // Refresh blogs
                    getAllBlogs()
                    loadFeaturedBlogs()
                } else {
                    _errorMessage.value = "Failed to like blog post"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error liking blog post: ${e.message}"
            }
        }
    }

    fun unlikeBlogPost(blogId: String, userId: String) {
        viewModelScope.launch {
            try {
                val success = blogRepository.unlikeBlogPost(blogId, userId)

                if (success) {
                    // Update current blog if it's the one being unliked
                    if (_currentBlog.value?.id == blogId) {
                        val updatedBlog = blogRepository.getBlogById(blogId)
                        _currentBlog.value = updatedBlog
                    }

                    // Refresh blogs
                    getAllBlogs()
                    loadFeaturedBlogs()
                } else {
                    _errorMessage.value = "Failed to unlike blog post"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error unliking blog post: ${e.message}"
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    fun resetOperationSuccess() {
        _operationSuccess.value = false
    }
}
