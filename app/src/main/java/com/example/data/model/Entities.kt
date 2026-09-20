package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val avatarUrl: String,
    val badge: String = "Resident Artist", // e.g. "Resident Master", "Verified Artist", "Studio Member"
    val specialty: String = "Traditional & Digital",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val catalogedCount: Int = 0,
    val acquiredCount: Int = 0,
    val role: String = "ARTIST", // "ADMIN", "ARTIST", "MEMBER"
    val isCurrentUser: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "art_posts")
data class ArtPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatar: String,
    val authorBadge: String = "Artist",
    val title: String,
    val description: String = "",
    val mediaUri: String, // local drawing data URL, gallery URI, or bundled preset key
    val mediaType: String = "IMAGE", // "IMAGE", "VIDEO_REEL", "SKETCH"
    val category: String, // Portrait, Pencil Drawing, Watercolor, Acrylic Art, Digital Art, Crafting, Colored Pencil, Anime, Realistic Drawing
    val medium: String = "Mixed Media", // e.g. "Charcoal & Graphite", "Watercolor on 300g Linen"
    val technique: String = "Cross-hatching, Glazing",
    val dimensions: String = "18 x 24 in",
    val materialsUsed: String = "Pencil, 100% Cotton Rag",
    val isReel: Boolean = false,
    val videoDurationSec: Int = 0,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val savesCount: Int = 0,
    val isForSale: Boolean = false,
    val price: Double = 0.0,
    val currency: String = "USD",
    val isSold: Boolean = false,
    val isFeatured: Boolean = false,
    val hasCertificateOfAuthenticity: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val commentId: Long = 0,
    val postId: Long,
    val userId: String,
    val userName: String,
    val userHandle: String,
    val userAvatar: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "likes")
data class LikeEntity(
    @PrimaryKey(autoGenerate = true) val likeId: Long = 0,
    val postId: Long,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "follows")
data class FollowEntity(
    @PrimaryKey(autoGenerate = true) val followId: Long = 0,
    val followerId: String,
    val followingId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saves")
data class SaveEntity(
    @PrimaryKey(autoGenerate = true) val saveId: Long = 0,
    val postId: Long,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "marketplace_products")
data class MarketplaceProductEntity(
    @PrimaryKey(autoGenerate = true) val productId: Long = 0,
    val sellerId: String,
    val sellerName: String,
    val name: String,
    val description: String,
    val imageUri: String,
    val category: String, // "Brushes", "Paints & Pigments", "Paper & Canvas", "Pencils & Charcoal", "Studio Kits"
    val price: Double,
    val condition: String = "Brand New", // "Brand New", "Pre-loved", "Surplus"
    val isAvailable: Boolean = true,
    val rating: Float = 4.9f,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val orderId: Long = 0,
    val buyerId: String,
    val buyerName: String,
    val sellerId: String,
    val sellerName: String,
    val itemType: String, // "ARTWORK", "MATERIAL"
    val itemId: Long,
    val itemTitle: String,
    val itemImage: String,
    val price: Double,
    val status: String = "CONFIRMED", // "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED"
    val shippingAddress: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val categoryId: Long = 0,
    val name: String,
    val description: String = "",
    val iconKey: String = "palette",
    val displayOrder: Int = 0
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val author: String = "Gallery Curators",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientId: String,
    val senderName: String,
    val type: String, // "LIKE", "COMMENT", "FOLLOW", "ORDER", "ANNOUNCEMENT"
    val message: String,
    val referenceId: Long = 0,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
