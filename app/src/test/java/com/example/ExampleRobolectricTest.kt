package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.StarKingDatabase
import com.example.data.StarKingRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: StarKingDatabase
    private lateinit var repository: StarKingRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StarKingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = StarKingRepository(database.starKingDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Star King", appName)
    }

    @Test
    fun `registerUser creates permanent 6-digit User ID and 1000 welcome coins`() = runBlocking {
        val newUser = repository.registerUser(
            nickname = "RoyalPrince",
            gender = "Male",
            country = "United States",
            dob = "1999-09-09",
            language = "English",
            bio = "Ready to party on Star King! 👑",
            authProvider = "GOOGLE",
            authIdentifier = "royal.prince@gmail.com",
            deviceInfo = "Google Pixel 8",
            sessionId = "SK-SES-TEST1234"
        )

        // Verify permanent 6-digit User ID (between 500000 and 999999)
        assertTrue(newUser.userId in 500000L..999999L)
        assertEquals("RoyalPrince", newUser.nickname)
        assertEquals("GOOGLE", newUser.authProvider)
        assertEquals("royal.prince@gmail.com", newUser.authIdentifier)

        // Verify welcome wallet created with 1,000 coins
        val wallet = database.starKingDao().getWalletSync(newUser.userId)
        assertNotNull(wallet)
        assertEquals(1000L, wallet!!.coinBalance)
    }

    @Test
    fun `deleteAccount completely wipes user and associated data`() = runBlocking {
        val user = repository.registerUser(
            nickname = "TemporaryStar",
            gender = "Star",
            country = "Global",
            dob = "2000-01-01",
            language = "English",
            bio = "Temporary user",
            authProvider = "GUEST"
        )

        assertNotNull(database.starKingDao().getUserByIdSync(user.userId))
        assertNotNull(database.starKingDao().getWalletSync(user.userId))

        repository.deleteAccount(user.userId)

        assertNull(database.starKingDao().getUserByIdSync(user.userId))
        assertNull(database.starKingDao().getWalletSync(user.userId))
    }

    @Test
    fun `purchase and equip medal updates user badge and customization`() = runBlocking {
        val user = repository.registerUser(
            nickname = "MedalMaster",
            gender = "Star",
            country = "United States",
            dob = "2000-01-01",
            language = "English",
            bio = "Medal collector",
            authProvider = "GUEST"
        )

        val medalKing = com.example.model.StoreCustomizationEntity(
            id = "medal_king",
            name = "King",
            type = "MEDAL",
            iconOrEmoji = "👑",
            priceCoins = 500L,
            durationDays = 365
        )
        database.starKingDao().insertStoreCustomizations(listOf(medalKing))

        val purchaseResult = repository.purchaseCustomization(user.userId, "medal_king")
        assertTrue(purchaseResult.isSuccess)

        val updatedUser = database.starKingDao().getUserByIdSync(user.userId)
        assertNotNull(updatedUser)
        assertEquals("King", updatedUser!!.equippedBadge)

        val wallet = database.starKingDao().getWalletSync(user.userId)
        assertNotNull(wallet)
        assertEquals(500L, wallet!!.coinBalance)
    }

    @Test
    fun `firestore user profile holds accumulated medals display name and picture`() {
        val medals = listOf("medal_king", "medal_official", "medal_dream_wedding")
        val firestoreProfile = com.example.data.FirestoreUserProfile(
            userId = 504094L,
            displayName = "CloudEmperor 🌟",
            profilePicture = "avatar_user",
            bio = "Live voice broadcaster",
            level = 10,
            equippedMedal = "King",
            accumulatedMedals = medals,
            medalsCount = medals.size
        )

        assertEquals("CloudEmperor 🌟", firestoreProfile.displayName)
        assertEquals("avatar_user", firestoreProfile.profilePicture)
        assertEquals("King", firestoreProfile.equippedMedal)
        assertEquals(3, firestoreProfile.accumulatedMedals.size)
        assertTrue(firestoreProfile.accumulatedMedals.contains("medal_king"))
        assertTrue(firestoreProfile.accumulatedMedals.contains("medal_official"))
    }

    @Test
    fun `sendGift to speaker deducts coins and creates transaction for Lottie overlay`() = runBlocking {
        val sender = repository.registerUser(
            nickname = "GiftGiver",
            gender = "Star",
            country = "Global",
            dob = "1998-05-05",
            language = "English",
            bio = "Love sending gifts",
            authProvider = "GUEST"
        )

        val receiver = repository.registerUser(
            nickname = "TopSpeaker",
            gender = "Star",
            country = "Global",
            dob = "1997-07-07",
            language = "English",
            bio = "Voice host",
            authProvider = "GUEST"
        )

        val rocketGift = com.example.model.GiftEntity(
            id = "gift_rocket",
            name = "Cosmic Rocket",
            iconEmoji = "🚀",
            category = "Luxury",
            coinPrice = 500L,
            animationType = "BURST"
        )
        database.starKingDao().insertGifts(listOf(rocketGift))

        val room = repository.createRoom(
            hostUser = receiver,
            name = "Voice Party Live",
            description = "Welcome all",
            category = "Music",
            language = "English",
            seatCount = 8,
            welcomeMsg = "Welcome!",
            isPrivate = false,
            password = ""
        )

        val sendResult = repository.sendGift(
            roomId = room.roomId,
            senderUser = sender,
            receiverUserId = receiver.userId,
            receiverName = receiver.nickname,
            giftId = "gift_rocket",
            count = 1
        )

        assertTrue(sendResult.isSuccess)
        val giftTx = sendResult.getOrNull()
        assertNotNull(giftTx)
        assertEquals("Cosmic Rocket", giftTx!!.giftName)
        assertEquals("🚀", giftTx.giftIcon)
        assertEquals(1, giftTx.giftCount)
        assertEquals(receiver.userId, giftTx.receiverUserId)

        val senderWallet = database.starKingDao().getWalletSync(sender.userId)
        assertNotNull(senderWallet)
        assertEquals(500L, senderWallet!!.coinBalance)
    }
}
