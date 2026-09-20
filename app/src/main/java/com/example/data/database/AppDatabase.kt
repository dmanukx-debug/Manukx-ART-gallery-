package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.GalleryDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ArtPostEntity::class,
        CommentEntity::class,
        LikeEntity::class,
        FollowEntity::class,
        SaveEntity::class,
        MarketplaceProductEntity::class,
        OrderEntity::class,
        CategoryEntity::class,
        AnnouncementEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun galleryDao(): GalleryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "manukx_art_gallery.db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialTaxonomy(database.galleryDao())
                    }
                }
            }

            private suspend fun populateInitialTaxonomy(dao: GalleryDao) {
                // Official requested categories
                val categories = listOf(
                    CategoryEntity(name = "Portrait", description = "Facial studies, expressive heads, and character portraiture", iconKey = "face", displayOrder = 1),
                    CategoryEntity(name = "Pencil Drawing", description = "Graphite, charcoal, tonal value studies, and hatching", iconKey = "edit", displayOrder = 2),
                    CategoryEntity(name = "Watercolor", description = "Wet-on-wet washes, glazing, botanical, and transparent pigments", iconKey = "water_drop", displayOrder = 3),
                    CategoryEntity(name = "Acrylic Art", description = "Heavy body impasto, acrylic gouache, and vivid canvas works", iconKey = "brush", displayOrder = 4),
                    CategoryEntity(name = "Digital Art", description = "Digital illustrations, 2D concept art, speedpaints, and pixel art", iconKey = "computer", displayOrder = 5),
                    CategoryEntity(name = "Crafting", description = "Pottery, printmaking, woodcut, bookbinding, and fiber art", iconKey = "handyman", displayOrder = 6),
                    CategoryEntity(name = "Colored Pencil", description = "Polychromos, luminance layering, burnishing, and botanical realism", iconKey = "palette", displayOrder = 7),
                    CategoryEntity(name = "Anime", description = "Manga inkwork, anime illustrations, character sheets, and cel shading", iconKey = "auto_awesome", displayOrder = 8),
                    CategoryEntity(name = "Realistic Drawing", description = "Hyper-realism, anatomical studies, still life, and trompe-l'œil", iconKey = "visibility", displayOrder = 9)
                )
                categories.forEach { dao.insertCategory(it) }

                // Default active artist profile for the current device user
                val primaryUser = UserEntity(
                    userId = "user_master_elena",
                    username = "elena_rostova",
                    displayName = "Elena Rostova",
                    bio = "Classical realist & charcoal draftsman. Resident Master at Manukx Atelier. Working with Fabriano 300g and Faber-Castell 9000.",
                    avatarUrl = "",
                    badge = "Resident Master",
                    specialty = "Charcoal & Botanical Watercolor",
                    followersCount = 0,
                    followingCount = 0,
                    catalogedCount = 0,
                    acquiredCount = 0,
                    role = "ADMIN",
                    isCurrentUser = true
                )
                dao.insertUser(primaryUser)

                // Secondary profile option for user switching/testing
                val guestArtist = UserEntity(
                    userId = "user_marcus_aurel",
                    username = "marcus_aurel",
                    displayName = "Marcus Aurel",
                    bio = "Plein air watercolorist and architectural sketching enthusiast. Exploring natural pigments.",
                    avatarUrl = "",
                    badge = "Verified Artist",
                    specialty = "Plein Air Watercolor",
                    followersCount = 0,
                    followingCount = 0,
                    catalogedCount = 0,
                    acquiredCount = 0,
                    role = "ARTIST",
                    isCurrentUser = false
                )
                dao.insertUser(guestArtist)

                // Official Gallery welcome announcement
                dao.insertAnnouncement(
                    AnnouncementEntity(
                        title = "Welcome to Manukx Art Gallery",
                        message = "A professional community for drawing, digital painting, and art collectors. Share your real creations, time-lapse videos, or list your originals and materials for sale.",
                        author = "Manukx Art Curators",
                        isActive = true
                    )
                )
            }
        }
    }
}
