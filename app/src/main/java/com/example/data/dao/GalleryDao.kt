package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GalleryDao {

    // --- ART POSTS ---
    @Query("SELECT * FROM art_posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<ArtPostEntity>>

    @Query("SELECT * FROM art_posts WHERE isReel = 1 ORDER BY createdAt DESC")
    fun getReels(): Flow<List<ArtPostEntity>>

    @Query("SELECT * FROM art_posts WHERE isForSale = 1 AND isSold = 0 ORDER BY createdAt DESC")
    fun getOriginalsForSale(): Flow<List<ArtPostEntity>>

    @Query("SELECT * FROM art_posts WHERE category = :category ORDER BY createdAt DESC")
    fun getPostsByCategory(category: String): Flow<List<ArtPostEntity>>

    @Query("SELECT * FROM art_posts WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun getPostsByAuthor(authorId: String): Flow<List<ArtPostEntity>>

    @Query("SELECT * FROM art_posts WHERE id = :id")
    fun getPostById(id: Long): Flow<ArtPostEntity?>

    @Query("""
        SELECT * FROM art_posts 
        WHERE title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
           OR authorName LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR medium LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchPosts(query: String): Flow<List<ArtPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: ArtPostEntity): Long

    @Update
    suspend fun updatePost(post: ArtPostEntity)

    @Query("DELETE FROM art_posts WHERE id = :id")
    suspend fun deletePostById(id: Long)

    @Query("UPDATE art_posts SET isFeatured = :isFeatured WHERE id = :id")
    suspend fun setPostFeatured(id: Long, isFeatured: Boolean)

    @Query("UPDATE art_posts SET isSold = :isSold WHERE id = :id")
    suspend fun setPostSold(id: Long, isSold: Boolean)

    @Query("UPDATE art_posts SET likesCount = likesCount + 1 WHERE id = :postId")
    suspend fun incrementLikesCount(postId: Long)

    @Query("UPDATE art_posts SET likesCount = CASE WHEN likesCount > 0 THEN likesCount - 1 ELSE 0 END WHERE id = :postId")
    suspend fun decrementLikesCount(postId: Long)

    @Query("UPDATE art_posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementCommentsCount(postId: Long)

    @Query("UPDATE art_posts SET savesCount = savesCount + 1 WHERE id = :postId")
    suspend fun incrementSavesCount(postId: Long)

    @Query("UPDATE art_posts SET savesCount = CASE WHEN savesCount > 0 THEN savesCount - 1 ELSE 0 END WHERE id = :postId")
    suspend fun decrementSavesCount(postId: Long)

    // --- LIKES ---
    @Query("SELECT COUNT(*) > 0 FROM likes WHERE postId = :postId AND userId = :userId")
    fun isPostLiked(postId: Long, userId: String): Flow<Boolean>

    @Query("SELECT postId FROM likes WHERE userId = :userId")
    fun getLikedPostIds(userId: String): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity): Long

    @Query("DELETE FROM likes WHERE postId = :postId AND userId = :userId")
    suspend fun deleteLike(postId: Long, userId: String)

    // --- SAVES ---
    @Query("SELECT COUNT(*) > 0 FROM saves WHERE postId = :postId AND userId = :userId")
    fun isPostSaved(postId: Long, userId: String): Flow<Boolean>

    @Query("SELECT postId FROM saves WHERE userId = :userId")
    fun getSavedPostIds(userId: String): Flow<List<Long>>

    @Query("""
        SELECT a.* FROM art_posts a
        INNER JOIN saves s ON a.id = s.postId
        WHERE s.userId = :userId
        ORDER BY s.createdAt DESC
    """)
    fun getSavedPostsForUser(userId: String): Flow<List<ArtPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSave(save: SaveEntity): Long

    @Query("DELETE FROM saves WHERE postId = :postId AND userId = :userId")
    suspend fun deleteSave(postId: Long, userId: String)

    // --- COMMENTS ---
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Query("DELETE FROM comments WHERE commentId = :commentId")
    suspend fun deleteComment(commentId: Long)

    // --- FOLLOWS ---
    @Query("SELECT COUNT(*) > 0 FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    fun isFollowing(followerId: String, followingId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: FollowEntity): Long

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun deleteFollow(followerId: String, followingId: String)

    @Query("UPDATE users SET followersCount = followersCount + 1 WHERE userId = :userId")
    suspend fun incrementFollowers(userId: String)

    @Query("UPDATE users SET followersCount = CASE WHEN followersCount > 0 THEN followersCount - 1 ELSE 0 END WHERE userId = :userId")
    suspend fun decrementFollowers(userId: String)

    @Query("UPDATE users SET followingCount = followingCount + 1 WHERE userId = :userId")
    suspend fun incrementFollowing(userId: String)

    @Query("UPDATE users SET followingCount = CASE WHEN followingCount > 0 THEN followingCount - 1 ELSE 0 END WHERE userId = :userId")
    suspend fun decrementFollowing(userId: String)

    // --- USERS ---
    @Query("SELECT * FROM users ORDER BY displayName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserDirect(): UserEntity?

    @Query("UPDATE users SET isCurrentUser = 0")
    suspend fun clearCurrentUser()

    @Query("UPDATE users SET isCurrentUser = 1 WHERE userId = :userId")
    suspend fun markUserAsCurrent(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)

    @Query("""
        SELECT * FROM users 
        WHERE displayName LIKE '%' || :query || '%' 
           OR username LIKE '%' || :query || '%'
           OR bio LIKE '%' || :query || '%'
        ORDER BY displayName ASC
    """)
    fun searchUsers(query: String): Flow<List<UserEntity>>

    // --- MARKETPLACE PRODUCTS ---
    @Query("SELECT * FROM marketplace_products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<MarketplaceProductEntity>>

    @Query("SELECT * FROM marketplace_products WHERE category = :category ORDER BY createdAt DESC")
    fun getProductsByCategory(category: String): Flow<List<MarketplaceProductEntity>>

    @Query("""
        SELECT * FROM marketplace_products 
        WHERE name LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchProducts(query: String): Flow<List<MarketplaceProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: MarketplaceProductEntity): Long

    @Update
    suspend fun updateProduct(product: MarketplaceProductEntity)

    @Query("DELETE FROM marketplace_products WHERE productId = :productId")
    suspend fun deleteProduct(productId: Long)

    // --- ORDERS ---
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE buyerId = :userId OR sellerId = :userId ORDER BY createdAt DESC")
    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    // --- CATEGORIES ---
    @Query("SELECT * FROM categories ORDER BY displayOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE categoryId = :categoryId")
    suspend fun deleteCategory(categoryId: Long)

    // --- ANNOUNCEMENTS ---
    @Query("SELECT * FROM announcements WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements ORDER BY createdAt DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Update
    suspend fun updateAnnouncement(announcement: AnnouncementEntity)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: Long)

    // --- NOTIFICATIONS ---
    @Query("SELECT * FROM notifications WHERE recipientId = :recipientId ORDER BY createdAt DESC")
    fun getNotificationsForUser(recipientId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientId = :userId")
    suspend fun markAllNotificationsAsRead(userId: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)
}
