package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.GalleryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class GalleryTab {
    HOME, EXPLORE, CREATE, MARKETPLACE, NOTIFICATIONS, PROFILE
}

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = GalleryRepository(database.galleryDao())

    // App Navigation & UI State
    private val _currentTab = MutableStateFlow(GalleryTab.HOME)
    val currentTab: StateFlow<GalleryTab> = _currentTab.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true) // Default to obsidian gallery theme
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Active inspections & dialogs
    private val _inspectedPost = MutableStateFlow<ArtPostEntity?>(null)
    val inspectedPost: StateFlow<ArtPostEntity?> = _inspectedPost.asStateFlow()

    private val _commentingPostId = MutableStateFlow<Long?>(null)
    val commentingPostId: StateFlow<Long?> = _commentingPostId.asStateFlow()

    private val _purchasingItem = MutableStateFlow<Pair<String, Any?>?>(null) // Pair("ARTWORK"|"MATERIAL", Entity)
    val purchasingItem: StateFlow<Pair<String, Any?>?> = _purchasingItem.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _showAdminPanel = MutableStateFlow(false)
    val showAdminPanel: StateFlow<Boolean> = _showAdminPanel.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Filters & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _marketplaceTab = MutableStateFlow("ARTWORKS") // "ARTWORKS" or "MATERIALS"
    val marketplaceTab: StateFlow<String> = _marketplaceTab.asStateFlow()

    // Data streams from repository
    val currentUser: StateFlow<UserEntity?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allPosts: StateFlow<List<ArtPostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reels: StateFlow<List<ArtPostEntity>> = repository.reels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val originalsForSale: StateFlow<List<ArtPostEntity>> = repository.originalsForSale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAnnouncements: StateFlow<List<AnnouncementEntity>> = repository.activeAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnnouncements: StateFlow<List<AnnouncementEntity>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketplaceProducts: StateFlow<List<MarketplaceProductEntity>> = repository.marketplaceProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User-specific streams
    val likedPostIds: StateFlow<List<Long>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getLikedPostIds(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPostIds: StateFlow<List<Long>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getSavedPostIds(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getNotificationsForUser(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userOrders: StateFlow<List<OrderEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getOrdersForUser(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Comments for active commenting post
    val activeComments: StateFlow<List<CommentEntity>> = commentingPostId.flatMapLatest { postId ->
        if (postId != null) repository.getCommentsForPost(postId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: GalleryTab) {
        _currentTab.value = tab
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setMarketplaceTab(tab: String) {
        _marketplaceTab.value = tab
    }

    fun inspectPost(post: ArtPostEntity?) {
        _inspectedPost.value = post
    }

    fun openComments(postId: Long?) {
        _commentingPostId.value = postId
    }

    fun openPurchase(itemType: String, item: Any?) {
        _purchasingItem.value = Pair(itemType, item)
    }

    fun closePurchase() {
        _purchasingItem.value = null
    }

    fun toggleAuthDialog(show: Boolean) {
        _showAuthDialog.value = show
    }

    fun toggleAdminPanel(show: Boolean) {
        _showAdminPanel.value = show
    }

    // Actions
    fun toggleLike(postId: Long) {
        viewModelScope.launch {
            repository.toggleLike(postId)
        }
    }

    fun toggleSave(postId: Long) {
        viewModelScope.launch {
            repository.toggleSave(postId)
            val isSaved = savedPostIds.value.contains(postId)
            _toastMessage.emit(if (!isSaved) "Artwork saved to your private collection" else "Removed from saved collection")
        }
    }

    fun toggleFollow(artistId: String) {
        viewModelScope.launch {
            repository.toggleFollow(artistId)
        }
    }

    fun addComment(postId: Long, content: String) {
        viewModelScope.launch {
            repository.addComment(postId, content)
        }
    }

    fun submitPost(
        title: String,
        description: String,
        mediaUri: String,
        mediaType: String,
        category: String,
        medium: String,
        technique: String,
        dimensions: String,
        materialsUsed: String,
        isReel: Boolean,
        videoDurationSec: Int,
        isForSale: Boolean,
        price: Double,
        hasCertificateOfAuthenticity: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.createPost(
                    title = title,
                    description = description,
                    mediaUri = mediaUri,
                    mediaType = mediaType,
                    category = category,
                    medium = medium,
                    technique = technique,
                    dimensions = dimensions,
                    materialsUsed = materialsUsed,
                    isReel = isReel,
                    videoDurationSec = videoDurationSec,
                    isForSale = isForSale,
                    price = price,
                    hasCertificateOfAuthenticity = hasCertificateOfAuthenticity
                )
                _toastMessage.emit("Artwork published to Manukx Art Gallery!")
                _currentTab.value = GalleryTab.HOME
                onSuccess()
            } catch (e: Exception) {
                _toastMessage.emit("Failed to publish: ${e.message}")
            }
        }
    }

    fun submitProduct(
        name: String,
        description: String,
        imageUri: String,
        category: String,
        price: Double,
        condition: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.createProduct(name, description, imageUri, category, price, condition)
                _toastMessage.emit("Art material listed on Marketplace!")
                _currentTab.value = GalleryTab.MARKETPLACE
                onSuccess()
            } catch (e: Exception) {
                _toastMessage.emit("Failed to list product: ${e.message}")
            }
        }
    }

    fun completePurchase(
        itemType: String,
        itemId: Long,
        itemTitle: String,
        itemImage: String,
        price: Double,
        sellerId: String,
        sellerName: String,
        shippingAddress: String
    ) {
        viewModelScope.launch {
            try {
                repository.placeOrder(
                    itemType = itemType,
                    itemId = itemId,
                    itemTitle = itemTitle,
                    itemImage = itemImage,
                    price = price,
                    sellerId = sellerId,
                    sellerName = sellerName,
                    shippingAddress = shippingAddress
                )
                _purchasingItem.value = null
                _toastMessage.emit("Order Confirmed! Your studio acquisition is being processed.")
            } catch (e: Exception) {
                _toastMessage.emit("Error placing order: ${e.message}")
            }
        }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            repository.switchUser(userId)
            _showAuthDialog.value = false
            _toastMessage.emit("Switched profile successfully")
        }
    }

    fun createAccount(username: String, displayName: String, bio: String, specialty: String, role: String) {
        viewModelScope.launch {
            repository.createUser(username, displayName, bio, specialty, role)
            _showAuthDialog.value = false
            _toastMessage.emit("Welcome to Manukx, $displayName!")
        }
    }

    fun updateProfile(displayName: String, bio: String, specialty: String, badge: String) {
        viewModelScope.launch {
            repository.updateUserProfile(displayName, bio, specialty, badge)
            _toastMessage.emit("Studio profile updated")
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(user.userId)
        }
    }

    // Admin commands
    fun adminDeletePost(postId: Long) {
        viewModelScope.launch {
            repository.adminDeletePost(postId)
            _toastMessage.emit("Post removed by Admin")
        }
    }

    fun adminToggleFeatured(postId: Long, isFeatured: Boolean) {
        viewModelScope.launch {
            repository.adminToggleFeatured(postId, isFeatured)
            _toastMessage.emit(if (isFeatured) "Artwork marked as Curators' Pick" else "Curators' Pick removed")
        }
    }

    fun adminAddCategory(name: String, desc: String, icon: String) {
        viewModelScope.launch {
            repository.adminAddCategory(name, desc, icon)
            _toastMessage.emit("Category added")
        }
    }

    fun adminDeleteCategory(categoryId: Long) {
        viewModelScope.launch {
            repository.adminDeleteCategory(categoryId)
            _toastMessage.emit("Category removed")
        }
    }

    fun adminCreateAnnouncement(title: String, message: String) {
        viewModelScope.launch {
            repository.adminCreateAnnouncement(title, message)
            _toastMessage.emit("Announcement broadcasted")
        }
    }

    fun adminDeleteAnnouncement(id: Long) {
        viewModelScope.launch {
            repository.adminDeleteAnnouncement(id)
            _toastMessage.emit("Announcement deleted")
        }
    }

    fun adminDeleteProduct(productId: Long) {
        viewModelScope.launch {
            repository.adminDeleteProduct(productId)
            _toastMessage.emit("Product deleted from marketplace")
        }
    }
}
