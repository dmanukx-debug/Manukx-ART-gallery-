package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.ArtPostEntity
import com.example.data.model.UserEntity
import com.example.data.repository.GalleryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: GalleryRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = GalleryRepository(database.galleryDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAppNameResource() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Manukx Art Gallery", appName)
    }

    @Test
    fun testCreateAndLikeArtworkPost() = runBlocking {
        // Insert active artist
        val artist = UserEntity(
            userId = "test_artist_1",
            username = "elena",
            displayName = "Elena Rostova",
            bio = "Master Draftsman",
            avatarUrl = "",
            isCurrentUser = true
        )
        database.galleryDao().insertUser(artist)

        val postId = repository.createPost(
            title = "Solitude at Dawn",
            description = "Architectural stone arch study bathed in golden morning light.",
            mediaUri = "res:art_solitude_dawn",
            mediaType = "IMAGE",
            category = "Acrylic Art",
            medium = "Oil & Tempera on Linen",
            technique = "Chiaroscuro Glazing",
            dimensions = "24 x 36 in",
            materialsUsed = "Raw Linen, Earth Pigments",
            isReel = false,
            videoDurationSec = 0,
            isForSale = true,
            price = 450.0,
            hasCertificateOfAuthenticity = true
        )

        assertTrue(postId > 0)

        val posts = repository.allPosts.first()
        assertEquals(1, posts.size)
        assertEquals("Solitude at Dawn", posts[0].title)
        assertEquals(450.0, posts[0].price, 0.01)

        // Like post
        repository.toggleLike(postId)
        val isLiked = repository.isPostLiked(postId, artist.userId).first()
        assertTrue(isLiked)

        // Save post
        repository.toggleSave(postId)
        val isSaved = repository.isPostSaved(postId, artist.userId).first()
        assertTrue(isSaved)
    }

    @Test
    fun testPlaceMarketplaceOrder() = runBlocking {
        val seller = UserEntity(
            userId = "seller_1",
            username = "marcus",
            displayName = "Marcus Aurel",
            bio = "Plein Air Painter",
            avatarUrl = ""
        )
        val buyer = UserEntity(
            userId = "buyer_1",
            username = "collector_jane",
            displayName = "Jane Doe",
            bio = "Art Collector",
            avatarUrl = "",
            isCurrentUser = true
        )
        database.galleryDao().insertUser(seller)
        database.galleryDao().insertUser(buyer)

        val orderId = repository.placeOrder(
            itemType = "ARTWORK",
            itemId = 101L,
            itemTitle = "Misty Mountain Ridge",
            itemImage = "res:art_watercolor_mist",
            price = 320.0,
            sellerId = seller.userId,
            sellerName = seller.displayName,
            shippingAddress = "100 Fine Arts Boulevard, Gallery Suite 4"
        )

        assertTrue(orderId > 0)

        val orders = repository.allOrders.first()
        assertEquals(1, orders.size)
        assertEquals("Misty Mountain Ridge", orders[0].itemTitle)
        assertEquals("CONFIRMED", orders[0].status)

        // Verify seller received notification
        val sellerNotifs = repository.getNotificationsForUser(seller.userId).first()
        assertEquals(1, sellerNotifs.size)
        assertTrue(sellerNotifs[0].message.contains("New Studio Acquisition"))
    }
}
