package com.example.data.repository

import com.example.data.dao.GalleryDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class GalleryRepository(private val dao: GalleryDao) {

    val allPosts: Flow<List<ArtPostEntity>> = dao.getAllPosts()
    val reels: Flow<List<ArtPostEntity>> = dao.getReels()
    val originalsForSale: Flow<List<ArtPostEntity>> = dao.getOriginalsForSale()
    val categories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val activeAnnouncements: Flow<List<AnnouncementEntity>> = dao.getActiveAnnouncements()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = dao.getAllAnnouncements()
    val currentUser: Flow<UserEntity?> = dao.getCurrentUser()
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val marketplaceProducts: Flow<List<MarketplaceProductEntity>> = dao.getAllProducts()
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()

    fun getPostsByCategory(category: String): Flow<List<ArtPostEntity>> = dao.getPostsByCategory(category)
    fun getPostsByAuthor(authorId: String): Flow<List<ArtPostEntity>> = dao.getPostsByAuthor(authorId)
    fun searchPosts(query: String): Flow<List<ArtPostEntity>> = dao.searchPosts(query)
    fun getPostById(id: Long): Flow<ArtPostEntity?> = dao.getPostById(id)
    fun getUserById(userId: String): Flow<UserEntity?> = dao.getUserById(userId)
    fun searchUsers(query: String): Flow<List<UserEntity>> = dao.searchUsers(query)
    fun searchProducts(query: String): Flow<List<MarketplaceProductEntity>> = dao.searchProducts(query)
    fun getProductsByCategory(category: String): Flow<List<MarketplaceProductEntity>> = dao.getProductsByCategory(category)
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>> = dao.getCommentsForPost(postId)
    fun isPostLiked(postId: Long, userId: String): Flow<Boolean> = dao.isPostLiked(postId, userId)
    fun isPostSaved(postId: Long, userId: String): Flow<Boolean> = dao.isPostSaved(postId, userId)
    fun isFollowing(followerId: String, followingId: String): Flow<Boolean> = dao.isFollowing(followerId, followingId)
    fun getLikedPostIds(userId: String): Flow<List<Long>> = dao.getLikedPostIds(userId)
    fun getSavedPostIds(userId: String): Flow<List<Long>> = dao.getSavedPostIds(userId)
    fun getSavedPostsForUser(userId: String): Flow<List<ArtPostEntity>> = dao.getSavedPostsForUser(userId)
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> = dao.getNotificationsForUser(userId)
    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>> = dao.getOrdersForUser(userId)

    suspend fun toggleLike(postId: Long) = withContext(Dispatchers.IO) {
        val user = dao.getCurrentUserDirect() ?: return@withContext
        val isLiked = dao.isPostLiked(postId, user.userId).firstOrNull() ?: false
        if (isLiked) {
            dao.deleteLike(postId, user.userId)
            dao.decrementLikesCount(postId)
        } else {
            dao.insertLike(LikeEntity(postId = postId, userId = user.userId))
            dao.incrementLikesCount(postId)
            val post = dao.getPostById(postId).firstOrNull()
            if (post != null && post.authorId != user.userId) {
                dao.insertNotification(
                    NotificationEntity(
                        recipientId = post.authorId,
                        senderName = user.displayName,
                        type = "LIKE",
                        message = "${user.displayName} appreciated your artwork \"${post.title}\"",
                        referenceId = postId
                    )
                )
            }
        }
    }

    suspend fun toggleSave(postId: Long) = withContext(Dispatchers.IO) {
        val user = dao.getCurrentUserDirect() ?: return@withContext
        val isSaved = dao.isPostSaved(postId, user.userId).firstOrNull() ?: false
        if (isSaved) {
            dao.deleteSave(postId, user.userId)
            dao.decrementSavesCount(postId)
        } else {
            dao.insertSave(SaveEntity(postId = postId, userId = user.userId))
            dao.incrementSavesCount(postId)
        }
    }

    suspend fun toggleFollow(targetUserId: String) = withContext(Dispatchers.IO) {
        val user = dao.getCurrentUserDirect() ?: return@withContext
        if (user.userId == targetUserId) return@withContext
        val isFollowing = dao.isFollowing(user.userId, targetUserId).firstOrNull() ?: false
        if (isFollowing) {
            dao.deleteFollow(user.userId, targetUserId)
            dao.decrementFollowers(targetUserId)
            dao.decrementFollowing(user.userId)
        } else {
            dao.insertFollow(FollowEntity(followerId = user.userId, followingId = targetUserId))
            dao.incrementFollowers(targetUserId)
            dao.incrementFollowing(user.userId)
            dao.insertNotification(
                NotificationEntity(
                    recipientId = targetUserId,
                    senderName = user.displayName,
                    type = "FOLLOW",
                    message = "${user.displayName} started following your studio"
                )
            )
        }
    }

    suspend fun addComment(postId: Long, content: String) = withContext(Dispatchers.IO) {
        val user = dao.getCurrentUserDirect() ?: return@withContext
        if (content.isBlank()) return@withContext
        dao.insertComment(
            CommentEntity(
                postId = postId,
                userId = user.userId,
                userName = user.displayName,
                userHandle = user.username,
                userAvatar = user.avatarUrl,
                content = content.trim()
            )
        )
        dao.incrementCommentsCount(postId)
        val post = dao.getPostById(postId).firstOrNull()
        if (post != null && post.authorId != user.userId) {
            dao.insertNotification(
                NotificationEntity(
                    recipientId = post.authorId,
                    senderName = user.displayName,
                    type = "COMMENT",
                    message = "${user.displayName} commented on \"${post.title}\": \"${content.take(40)}\"",
                    referenceId = postId
                )
            )
        }
    }

    suspend fun createPost(
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
        hasCertificateOfAuthenticity: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val user = dao.getCurrentUserDirect() ?: throw IllegalStateException("No active user")
        val post = ArtPostEntity(
            authorId = user.userId,
            authorName = user.displayName,
            authorHandle = user.username,
            authorAvatar = user.avatarUrl,
            authorBadge = user.badge,
            title = title.trim(),
            description = description.trim(),
            mediaUri = mediaUri,
            mediaType = mediaType,
            category = category,
            medium = medium.trim(),
            technique = technique.trim(),
            dimensions = dimensions.trim(),
            materialsUsed = materialsUsed.trim(),
            isReel = isReel,
            videoDurationSec = videoDurationSec,
            isForSale = isForSale,
            price = price,
            hasCertificateOfAuthenticity = hasCertificateOfAuthenticity
        )
        val id = dao.insertPost(post)
        // Update user cataloged count
        val updatedUser = user.copy(catalogedCount = user.catalogedCount + 1)
        dao.updateUser(updatedUser)
        id
    }

    suspend fun createProduct(
        name: String,
        description: String,
        imageUri: String,
        category: String,
        price: Double,
        condition: String
    ): Long = withContext(Dispatchers.IO) {
        val user = dao.getCurrentUserDirect() ?: throw IllegalStateException("No active user")
        val product = MarketplaceProductEntity(
            sellerId = user.userId,
            sellerName = user.displayName,
            name = name.trim(),
            description = description.trim(),
            imageUri = imageUri,
            category = category,
            price = price,
            condition = condition
        )
        dao.insertProduct(product)
    }

    suspend fun placeOrder(
        itemType: String,
        itemId: Long,
        itemTitle: String,
        itemImage: String,
        price: Double,
        sellerId: String,
        sellerName: String,
        shippingAddress: String
    ): Long = withContext(Dispatchers.IO) {
        val user = dao.getCurrentUserDirect() ?: throw IllegalStateException("No active user")
        val order = OrderEntity(
            buyerId = user.userId,
            buyerName = user.displayName,
            sellerId = sellerId,
            sellerName = sellerName,
            itemType = itemType,
            itemId = itemId,
            itemTitle = itemTitle,
            itemImage = itemImage,
            price = price,
            status = "CONFIRMED",
            shippingAddress = shippingAddress.trim()
        )
        val orderId = dao.insertOrder(order)
        if (itemType == "ARTWORK") {
            dao.setPostSold(itemId, isSold = true)
        }
        // Notify seller
        dao.insertNotification(
            NotificationEntity(
                recipientId = sellerId,
                senderName = user.displayName,
                type = "ORDER",
                message = "New Studio Acquisition! ${user.displayName} purchased \"$itemTitle\" for $$price.",
                referenceId = orderId
            )
        )
        // Notify buyer
        dao.insertNotification(
            NotificationEntity(
                recipientId = user.userId,
                senderName = "Manukx Art Gallery",
                type = "ORDER",
                message = "Order #$orderId Confirmed! Your acquisition of \"$itemTitle\" has been recorded.",
                referenceId = orderId
            )
        )
        orderId
    }

    suspend fun switchUser(userId: String) = withContext(Dispatchers.IO) {
        dao.clearCurrentUser()
        dao.markUserAsCurrent(userId)
    }

    suspend fun createUser(
        username: String,
        displayName: String,
        bio: String,
        specialty: String,
        role: String = "ARTIST"
    ) = withContext(Dispatchers.IO) {
        val newUser = UserEntity(
            userId = "user_${System.currentTimeMillis()}",
            username = username.trim().lowercase().replace(" ", "_"),
            displayName = displayName.trim(),
            bio = bio.trim(),
            avatarUrl = "",
            badge = if (role == "ADMIN") "Gallery Admin" else "Studio Member",
            specialty = specialty.trim(),
            role = role,
            isCurrentUser = true
        )
        dao.clearCurrentUser()
        dao.insertUser(newUser)
    }

    suspend fun updateUserProfile(
        displayName: String,
        bio: String,
        specialty: String,
        badge: String
    ) = withContext(Dispatchers.IO) {
        val user = dao.getCurrentUserDirect() ?: return@withContext
        val updated = user.copy(
            displayName = displayName.trim(),
            bio = bio.trim(),
            specialty = specialty.trim(),
            badge = badge.trim()
        )
        dao.updateUser(updated)
    }

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsAsRead(userId: String) = withContext(Dispatchers.IO) {
        dao.markAllNotificationsAsRead(userId)
    }

    // Admin Operations
    suspend fun adminDeletePost(postId: Long) = withContext(Dispatchers.IO) {
        dao.deletePostById(postId)
    }

    suspend fun adminToggleFeatured(postId: Long, isFeatured: Boolean) = withContext(Dispatchers.IO) {
        dao.setPostFeatured(postId, isFeatured)
    }

    suspend fun adminAddCategory(name: String, description: String, iconKey: String) = withContext(Dispatchers.IO) {
        dao.insertCategory(CategoryEntity(name = name.trim(), description = description.trim(), iconKey = iconKey))
    }

    suspend fun adminDeleteCategory(categoryId: Long) = withContext(Dispatchers.IO) {
        dao.deleteCategory(categoryId)
    }

    suspend fun adminCreateAnnouncement(title: String, message: String) = withContext(Dispatchers.IO) {
        dao.insertAnnouncement(AnnouncementEntity(title = title.trim(), message = message.trim(), isActive = true))
    }

    suspend fun adminDeleteAnnouncement(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteAnnouncement(id)
    }

    suspend fun adminDeleteProduct(productId: Long) = withContext(Dispatchers.IO) {
        dao.deleteProduct(productId)
    }

    suspend fun adminUpdateOrderStatus(orderId: Long, status: String) = withContext(Dispatchers.IO) {
        dao.updateOrderStatus(orderId, status)
    }
}
