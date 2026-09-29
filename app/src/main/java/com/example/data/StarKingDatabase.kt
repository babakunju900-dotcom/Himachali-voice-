package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        WalletEntity::class,
        WalletTransactionEntity::class,
        RoomEntity::class,
        RoomSeatEntity::class,
        RoomMemberEntity::class,
        RoomHandRaiseEntity::class,
        ChatMessageEntity::class,
        GiftEntity::class,
        GiftTransactionEntity::class,
        FollowEntity::class,
        CoinPackageEntity::class,
        VipPlanEntity::class,
        StoreCustomizationEntity::class,
        UserCustomizationEntity::class,
        EventEntity::class,
        AgencyEntity::class,
        SupportTicketEntity::class,
        ReportEntity::class,
        ModerationActionEntity::class,
        NotificationEntity::class,
        AppSettingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class StarKingDatabase : RoomDatabase() {

    abstract fun starKingDao(): StarKingDao

    companion object {
        @Volatile
        private var INSTANCE: StarKingDatabase? = null

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE rooms ADD COLUMN coverPhotoUrl TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE users ADD COLUMN authProvider TEXT NOT NULL DEFAULT 'GUEST'")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE users ADD COLUMN authIdentifier TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE users ADD COLUMN sessionId TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE users ADD COLUMN deviceInfo TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE users ADD COLUMN lastLoginAt INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS room_hand_raises (
                        roomId INTEGER NOT NULL,
                        userId INTEGER NOT NULL,
                        userName TEXT NOT NULL,
                        userAvatar TEXT NOT NULL,
                        userLevel INTEGER NOT NULL,
                        userVip INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        PRIMARY KEY(roomId, userId)
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): StarKingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StarKingDatabase::class.java,
                    "star_king_voice_chat_v2.db"
                )
                    .addCallback(StarKingDatabaseCallback(scope))
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class StarKingDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.starKingDao())
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    try {
                        if (database.starKingDao().getUserCount() == 0) {
                            populateInitialData(database.starKingDao())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        private suspend fun populateInitialData(dao: StarKingDao) {
            // Seed Users
            val officialOwner = UserEntity(
                userId = 100001L,
                nickname = "Star King Official",
                avatarUrl = "avatar_crown",
                gender = "Star",
                country = "Global",
                dob = "2020-01-01",
                language = "English",
                bio = "Official Star King Team 🌟 Connect, Talk, Party!",
                level = 99,
                exp = 999999,
                vipTier = 4,
                vipExpiry = System.currentTimeMillis() + 31536000000L,
                isOfficialVerified = true,
                role = UserRole.OWNER.name,
                equippedFrame = "Royal Crown Frame",
                equippedBadge = "✓ OFFICIAL",
                equippedTitle = "Platform Founder"
            )

            val supportDesk = UserEntity(
                userId = 100002L,
                nickname = "Customer Support 24/7",
                avatarUrl = "avatar_support",
                gender = "Star",
                country = "Global",
                dob = "2020-01-01",
                language = "English",
                bio = "Official Help Desk. We are here to assist with payments, accounts, & rooms.",
                level = 50,
                exp = 50000,
                vipTier = 4,
                vipExpiry = System.currentTimeMillis() + 31536000000L,
                isOfficialVerified = true,
                role = UserRole.CUSTOMER_SUPPORT.name,
                equippedFrame = "Gold Star Frame",
                equippedBadge = "SUPPORT STAFF",
                equippedTitle = "Help Specialist"
            )

            val hostAria = UserEntity(
                userId = 502110L,
                nickname = "Aria Luna 🎙️",
                avatarUrl = "avatar_aria",
                gender = "Female",
                country = "United States",
                dob = "1998-05-12",
                language = "English",
                bio = "Acoustic singer, night owls welcome! Live every evening 🎸",
                level = 18,
                exp = 18500,
                vipTier = 3,
                vipExpiry = System.currentTimeMillis() + 15000000000L,
                isOfficialVerified = true,
                role = UserRole.HOST.name,
                equippedFrame = "Galaxy Aura Frame",
                equippedBadge = "TOP HOST",
                equippedTitle = "Golden Vocalist",
                followersCount = 1420,
                giftsReceivedCount = 540
            )

            val hostViktor = UserEntity(
                userId = 503442L,
                nickname = "DJ Viktor 🔥",
                avatarUrl = "avatar_viktor",
                gender = "Male",
                country = "Germany",
                dob = "1995-11-20",
                language = "English",
                bio = "Electronic Beats & Party Anthems. Turn your volume up!",
                level = 15,
                exp = 14200,
                vipTier = 2,
                vipExpiry = System.currentTimeMillis() + 10000000000L,
                isOfficialVerified = false,
                role = UserRole.HOST.name,
                equippedFrame = "Cyber Neon Frame",
                equippedBadge = "PARTY KING",
                equippedTitle = "Beat Master",
                followersCount = 890,
                giftsReceivedCount = 310
            )

            val hostLayla = UserEntity(
                userId = 504890L,
                nickname = "Princess Layla 👑",
                avatarUrl = "avatar_layla",
                gender = "Female",
                country = "United Arab Emirates",
                dob = "1999-08-15",
                language = "Arabic",
                bio = "Poetry, meaningful chats, and good vibes only. 🌙",
                level = 22,
                exp = 24000,
                vipTier = 4,
                vipExpiry = System.currentTimeMillis() + 20000000000L,
                isOfficialVerified = true,
                role = UserRole.HOST.name,
                equippedFrame = "Royal Crown Frame",
                equippedBadge = "TOP GIVER",
                equippedTitle = "Crown Ambassador",
                followersCount = 2850,
                giftsReceivedCount = 920
            )

            // Current Primary User Profile
            val myUser = UserEntity(
                userId = 504094L,
                nickname = "StarRuler",
                avatarUrl = "avatar_user",
                gender = "Star",
                country = "United States",
                dob = "2001-04-10",
                language = "English",
                bio = "Voice party enthusiast and music lover 🌟",
                level = 3,
                exp = 450,
                vipTier = 1,
                vipExpiry = System.currentTimeMillis() + 604800000L,
                isOfficialVerified = false,
                role = UserRole.OWNER.name, // Gives full owner/admin testing privileges to test admin panels
                equippedFrame = "Gold Star Frame",
                equippedBadge = "STARTER",
                equippedTitle = "Rising Star",
                followersCount = 12,
                followingCount = 4
            )

            dao.insertUser(officialOwner)
            dao.insertUser(supportDesk)
            dao.insertUser(hostAria)
            dao.insertUser(hostViktor)
            dao.insertUser(hostLayla)
            dao.insertUser(myUser)

            // Wallets
            dao.insertWallet(WalletEntity(userId = 504094L, coinBalance = 25000L, diamondBalance = 1500L, totalRecharged = 50000L, totalSpent = 25000L))
            dao.insertWallet(WalletEntity(userId = 100001L, coinBalance = 9999999L, diamondBalance = 500000L))
            dao.insertWallet(WalletEntity(userId = 502110L, coinBalance = 45000L, diamondBalance = 8200L))
            dao.insertWallet(WalletEntity(userId = 503442L, coinBalance = 32000L, diamondBalance = 4100L))
            dao.insertWallet(WalletEntity(userId = 504890L, coinBalance = 120000L, diamondBalance = 19500L))

            // Seed Permanent Official Customer Support Room & Authentic Community Rooms
            val room1 = RoomEntity(
                roomId = 708101L,
                name = "⭐ Star Voice Customer Support",
                description = "Permanent Official 24/7 Customer Support Room for Star Voice. Talk to authorized Star Voice staff for instant account, coin recharge, gift, or safety assistance.",
                coverEmoji = "⭐",
                coverPhotoUrl = "official_support_logo",
                category = "Official",
                language = "English",
                country = "Global",
                seatCount = 8,
                welcomeMessage = "⭐ Welcome to Star Voice Customer Support. Authorized staff are active to assist you. Respect staff and community guidelines.",
                hostUserId = 100001L,
                hostName = "Star Voice Customer Support",
                hostAvatar = "avatar_crown",
                isLive = true,
                onlineCount = 85,
                totalGiftsValue = 120000L
            )

            val room2 = RoomEntity(
                roomId = 708210L,
                name = "👑 Royal Acoustic Night & Chill Party",
                description = "Relaxing guitar, song requests, singing, and late night friendly chats.",
                coverEmoji = "🎸",
                category = "Music",
                language = "English",
                country = "United States",
                seatCount = 8,
                welcomeMessage = "Grab a mic seat and sing your favorite ballad! 🎵",
                hostUserId = 502110L,
                hostName = "Aria Luna 🎙️",
                hostAvatar = "avatar_aria",
                isLive = true,
                onlineCount = 89,
                totalGiftsValue = 284500L
            )

            val room3 = RoomEntity(
                roomId = 708315L,
                name = "🔥 Global EDM & DJ Beat Drop",
                description = "Non-stop dance party and high-energy music stream.",
                coverEmoji = "🎧",
                category = "Party",
                language = "English",
                country = "Germany",
                seatCount = 10,
                welcomeMessage = "Drop fire in the chat! Let's get the party rocking!",
                hostUserId = 503442L,
                hostName = "DJ Viktor 🔥",
                hostAvatar = "avatar_viktor",
                isLive = true,
                onlineCount = 124,
                totalGiftsValue = 415000L
            )

            val room4 = RoomEntity(
                roomId = 708420L,
                name = "💎 VIP Star Lounge • Poetry & Talk",
                description = "Deep night discussions, poetry readings, and heart-to-heart connections.",
                coverEmoji = "🌙",
                category = "Dating",
                language = "Arabic",
                country = "United Arab Emirates",
                seatCount = 8,
                welcomeMessage = "Ahlan wa Sahlan! Welcome to the VIP Star Lounge 🌙",
                hostUserId = 504890L,
                hostName = "Princess Layla 👑",
                hostAvatar = "avatar_layla",
                isLive = true,
                onlineCount = 65,
                totalGiftsValue = 189000L
            )

            val room5 = RoomEntity(
                roomId = 708550L,
                name = "🎮 Esports & Voice Gaming Hub",
                description = "Looking for party members, ranked teammates, and voice gaming fun.",
                coverEmoji = "🎮",
                category = "Gaming",
                language = "English",
                country = "Global",
                seatCount = 8,
                welcomeMessage = "Join a mic seat and call your team plays!",
                hostUserId = 504094L,
                hostName = "StarRuler",
                hostAvatar = "avatar_user",
                isLive = true,
                onlineCount = 18,
                totalGiftsValue = 15000L
            )

            dao.insertRoom(room1)
            dao.insertRoom(room2)
            dao.insertRoom(room3)
            dao.insertRoom(room4)
            dao.insertRoom(room5)

            // Seed Seats for Room 2 (Aria's Room)
            val room2Seats = listOf(
                RoomSeatEntity(roomId = 708210L, seatIndex = 0, userId = 502110L, userName = "Aria Luna 🎙️", userAvatar = "avatar_aria", userLevel = 18, userVip = 3, isSpeaking = true),
                RoomSeatEntity(roomId = 708210L, seatIndex = 1, userId = 504890L, userName = "Princess Layla 👑", userAvatar = "avatar_layla", userLevel = 22, userVip = 4),
                RoomSeatEntity(roomId = 708210L, seatIndex = 2, userId = 503442L, userName = "DJ Viktor 🔥", userAvatar = "avatar_viktor", userLevel = 15, userVip = 2),
                RoomSeatEntity(roomId = 708210L, seatIndex = 3, userId = null, isLocked = false),
                RoomSeatEntity(roomId = 708210L, seatIndex = 4, userId = null, isLocked = false),
                RoomSeatEntity(roomId = 708210L, seatIndex = 5, userId = null, isLocked = false),
                RoomSeatEntity(roomId = 708210L, seatIndex = 6, userId = null, isLocked = false),
                RoomSeatEntity(roomId = 708210L, seatIndex = 7, userId = null, isLocked = true) // VIP Reserved seat
            )
            dao.insertSeats(room2Seats)

            // Seed Seats for Room 1 (Official Support)
            val room1Seats = (0 until 8).map { idx ->
                if (idx == 0) {
                    RoomSeatEntity(roomId = 708101L, seatIndex = 0, userId = 100001L, userName = "Star King Official", userAvatar = "avatar_crown", userLevel = 99, userVip = 4)
                } else if (idx == 1) {
                    RoomSeatEntity(roomId = 708101L, seatIndex = 1, userId = 100002L, userName = "Customer Support 24/7", userAvatar = "avatar_support", userLevel = 50, userVip = 4)
                } else {
                    RoomSeatEntity(roomId = 708101L, seatIndex = idx, userId = null, isLocked = false)
                }
            }
            dao.insertSeats(room1Seats)

            // Seed Gifts
            val gifts = listOf(
                GiftEntity("gift_rose", "Rose", "🌹", "Popular", 10L, "FLOAT"),
                GiftEntity("gift_heart", "Love Heart", "💖", "Love", 50L, "BURST"),
                GiftEntity("gift_confetti", "Party Popper", "🎉", "Popular", 100L, "BURST"),
                GiftEntity("gift_star", "Star Charm", "⭐", "Star", 200L, "FLOAT"),
                GiftEntity("gift_balloon", "Love Balloon", "🎈", "Love", 500L, "FLOAT"),
                GiftEntity("gift_ring", "Diamond Ring", "💍", "Love", 1000L, "BURST"),
                GiftEntity("gift_bouquet", "Flower Bouquet", "💐", "Love", 1500L, "BURST"),
                GiftEntity("gift_teddy", "Golden Teddy", "🧸", "Popular", 2000L, "BURST"),
                GiftEntity("gift_gold_star", "Royal Star", "🌟", "Star", 5000L, "BURST"),
                GiftEntity("gift_crown", "King Crown", "👑", "Luxury", 10000L, "CROWN"),
                GiftEntity("gift_rocket", "Cosmic Rocket", "🚀", "Luxury", 15000L, "BURST"),
                GiftEntity("gift_scepter", "Star Scepter", "🪄", "Luxury", 25000L, "CROWN"),
                GiftEntity("gift_car", "Sports Car", "🏎️", "Luxury", 50000L, "CAR"),
                GiftEntity("gift_yacht", "Mega Yacht", "🛥️", "Luxury", 100000L, "GALAXY"),
                GiftEntity("gift_castle", "Royal Castle", "🏰", "Special", 500000L, "GALAXY")
            )
            dao.insertGifts(gifts)

            // Seed Coin Packages
            val coinPackages = listOf(
                CoinPackageEntity("pkg_10k", 10000L, 0L, 0.99, ""),
                CoinPackageEntity("pkg_50k", 50000L, 2500L, 4.99, "+5% BONUS"),
                CoinPackageEntity("pkg_100k", 100000L, 10000L, 9.99, "POPULAR +10%"),
                CoinPackageEntity("pkg_500k", 50000L, 75000L, 49.99, "+15% BONUS"),
                CoinPackageEntity("pkg_1m", 1000000L, 200000L, 99.99, "BEST VALUE +20%")
            )
            dao.insertCoinPackages(coinPackages)

            // Seed VIP Plans
            val vipPlans = listOf(
                VipPlanEntity("vip_bronze", "VIP Bronze", 1, 7, 10000L, "BRONZE", "VIP Badge + Bronze Chat Bubble + 5% Exp Boost"),
                VipPlanEntity("vip_silver", "VIP Silver", 2, 30, 35000L, "SILVER", "Silver Frame + Entry Sound + 10% Exp Boost + Exclusive Gifts"),
                VipPlanEntity("vip_gold", "VIP Gold", 3, 90, 90000L, "GOLD", "Gold Star Frame + Animated Crown + 20% Exp Boost + VIP Seat Access"),
                VipPlanEntity("vip_diamond", "Diamond King VIP", 4, 365, 300000L, "DIAMOND", "Diamond Crown Frame + Luxury Sports Car Entrance + 50% Exp Boost + Dedicated Concierge")
            )
            dao.insertVipPlans(vipPlans)

            // Seed Customization Store Items & Medals
            val storeItems = listOf(
                // 22 Medals matching user screenshots
                StoreCustomizationEntity("medal_king", "King", "MEDAL", "👑", 15000L, 365),
                StoreCustomizationEntity("medal_official", "Official", "MEDAL", "⭐", 10000L, 365),
                StoreCustomizationEntity("medal_dream_wedding", "Dream wedding", "MEDAL", "💛", 12000L, 365),
                StoreCustomizationEntity("medal_assistant", "Assistant", "MEDAL", "👤", 8000L, 365),
                StoreCustomizationEntity("medal_service_team", "Service team", "MEDAL", "🎧", 6000L, 365),
                StoreCustomizationEntity("medal_love_day", "Love day", "MEDAL", "💖", 7000L, 365),
                StoreCustomizationEntity("medal_best_friend", "Best friend", "MEDAL", "🤝", 5000L, 365),
                StoreCustomizationEntity("medal_snack", "Snack", "MEDAL", "🐉", 9000L, 365),
                StoreCustomizationEntity("medal_millionaire", "Millionaire", "MEDAL", "💎", 25000L, 365),
                StoreCustomizationEntity("medal_friendship_maker", "Friendship Maker", "MEDAL", "🤝", 6500L, 365),
                StoreCustomizationEntity("medal_rocket1", "Rocket 1", "MEDAL", "🚀", 14000L, 365),
                StoreCustomizationEntity("medal_bd", "BD", "MEDAL", "🛡️", 8500L, 365),
                StoreCustomizationEntity("medal_coin_seller", "Coin seller", "MEDAL", "💲", 11000L, 365),
                StoreCustomizationEntity("medal_tiger_king", "Tiger king", "MEDAL", "🐯", 16000L, 365),
                StoreCustomizationEntity("medal_30day_top", "30 day monthly top", "MEDAL", "🏆", 20000L, 365),
                StoreCustomizationEntity("medal_100m", "100M", "MEDAL", "🦁", 30000L, 365),
                StoreCustomizationEntity("medal_cs_admin", "CS Admin", "MEDAL", "👩‍💼", 12000L, 365),
                StoreCustomizationEntity("medal_first_recharge", "First recharge", "MEDAL", "💵", 3000L, 365),
                StoreCustomizationEntity("medal_official_team", "Official team", "MEDAL", "👔", 18000L, 365),
                StoreCustomizationEntity("medal_winner", "Winner", "MEDAL", "🏆", 13000L, 365),
                StoreCustomizationEntity("medal_lucky_777", "Lucky 777 Pro", "MEDAL", "🎰", 15000L, 365),
                StoreCustomizationEntity("medal_expert", "Medal Expert", "MEDAL", "🎖️", 10000L, 365),

                // Frames & Badges
                StoreCustomizationEntity("frame_gold_star", "Gold Star Frame", "FRAME", "🌟", 5000L, 30),
                StoreCustomizationEntity("frame_crown", "Royal Crown Frame", "FRAME", "👑", 12000L, 30),
                StoreCustomizationEntity("frame_galaxy", "Galaxy Aura Frame", "FRAME", "🌌", 15000L, 30),
                StoreCustomizationEntity("frame_cyber", "Cyber Neon Frame", "FRAME", "⚡", 8000L, 30),
                StoreCustomizationEntity("badge_singer", "Golden Vocalist", "BADGE", "🎙️", 3000L, 30),
                StoreCustomizationEntity("badge_giver", "Top Giver", "BADGE", "💎", 5000L, 30),
                StoreCustomizationEntity("badge_party", "Party King", "BADGE", "🔥", 4000L, 30),
                StoreCustomizationEntity("vehicle_car", "Neon Supercar", "VEHICLE", "🏎️", 25000L, 30),
                StoreCustomizationEntity("vehicle_dragon", "Cosmic Phoenix", "VEHICLE", "🦅", 50000L, 30)
            )
            dao.insertStoreCustomizations(storeItems)

            // Pre-seed User 504094L with owned medals
            val defaultOwnedCustomizations = listOf(
                UserCustomizationEntity(504094L, "medal_king", isEquipped = true, expiresAt = System.currentTimeMillis() + 86400000L * 365),
                UserCustomizationEntity(504094L, "medal_official", isEquipped = false, expiresAt = System.currentTimeMillis() + 86400000L * 365),
                UserCustomizationEntity(504094L, "medal_best_friend", isEquipped = false, expiresAt = System.currentTimeMillis() + 86400000L * 365),
                UserCustomizationEntity(504094L, "frame_gold_star", isEquipped = true, expiresAt = System.currentTimeMillis() + 86400000L * 30)
            )
            for (custom in defaultOwnedCustomizations) {
                dao.insertUserCustomization(custom)
            }

            // Seed Events
            val events = listOf(
                EventEntity(
                    id = "event_top_host_2026",
                    title = "STAR KING TOP HOST GALA",
                    bannerEmoji = "🏆",
                    description = "Collect gift points while hosting live voice rooms. Top 3 hosts win exclusive Diamond Crown badges and 1,000,000 coin prize pool!",
                    startDate = System.currentTimeMillis() - 86400000L * 2,
                    endDate = System.currentTimeMillis() + 86400000L * 5,
                    rules = "1 coin received = 1 event point. Only public voice rooms count.",
                    prizeDescription = "Rank 1: 500,000 Coins + Diamond Crown Frame. Rank 2: 300,000 Coins. Rank 3: 200,000 Coins.",
                    targetPoints = 500000L,
                    category = "WEEKLY"
                ),
                EventEntity(
                    id = "event_summer_vocal",
                    title = "GLOBAL VOICE SUPERSTAR",
                    bannerEmoji = "🎤",
                    description = "Sing live on any public mic seat. Listeners vote with stars and roses!",
                    startDate = System.currentTimeMillis() - 86400000L,
                    endDate = System.currentTimeMillis() + 86400000L * 10,
                    rules = "Minimum 15 minutes mic time required.",
                    prizeDescription = "Exclusive 'Golden Vocalist' permanent title and verified badge.",
                    targetPoints = 250000L,
                    category = "SPECIAL"
                )
            )
            dao.insertEvents(events)

            // Seed Agencies
            val agencies = listOf(
                AgencyEntity("ag_starlight", "Starlight Media Agency", 100001L, "Premier talent agency nurturing top creators & voice room hosts.", 14, 0.12),
                AgencyEntity("ag_royal", "Royal Beats Guild", 503442L, "Electronic and acoustic music producers & party streamers.", 8, 0.10)
            )
            dao.insertAgencies(agencies)

            // Seed Initial In-Room Messages for Room 2
            dao.insertMessage(ChatMessageEntity(roomId = 708210L, senderUserId = 502110L, senderName = "Aria Luna 🎙️", senderAvatar = "avatar_aria", senderLevel = 18, senderVip = 3, messageText = "Welcome in everyone! Playing guitar requests tonight 🎸"))
            dao.insertMessage(ChatMessageEntity(roomId = 708210L, senderUserId = 504890L, senderName = "Princess Layla 👑", senderAvatar = "avatar_layla", senderLevel = 22, senderVip = 4, messageText = "Can you play some soulful acoustic tunes? 💖"))

            // Seed Initial Private Support Chat
            dao.insertMessage(ChatMessageEntity(recipientUserId = 504094L, senderUserId = 100002L, senderName = "Customer Support 24/7", senderAvatar = "avatar_support", senderLevel = 50, senderVip = 4, messageText = "Welcome to Star King Voice Chat! If you have any inquiries regarding recharge, audio seats, or room verification, reply here anytime."))

            // Seed Sample Support Ticket
            dao.insertTicket(SupportTicketEntity(
                ticketId = "TCK-8821",
                userId = 504094L,
                userNickname = "StarRuler",
                category = "Room Issue",
                subject = "Question about hosting mic seats",
                message = "How do I configure 10 seats instead of 8 in my room?",
                status = "RESOLVED",
                staffReply = "Hi! You can select 8, 10, or 12 seats during room creation or adjust it in Room Settings under Host Controls.",
                updatedAt = System.currentTimeMillis()
            ))

            // Seed Initial Notifications
            dao.insertNotification(NotificationEntity(
                userId = 504094L,
                title = "Welcome to Star King!",
                message = "Congratulations on registering your permanent Star King ID: 504094. Enjoy 1,000 bonus coins!",
                type = "SYSTEM"
            ))
            dao.insertNotification(NotificationEntity(
                userId = 504094L,
                title = "Aria Luna is live now!",
                message = "Aria Luna started hosting in 'Royal Acoustic Night & Chill Party'.",
                type = "ROOM_INVITE"
            ))
        }
    }
}
