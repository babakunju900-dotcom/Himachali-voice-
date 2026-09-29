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
}
