package com.ajsnarr.choosewhatyoumute

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ajsnarr.choosewhatyoumute.Data.App
import com.ajsnarr.choosewhatyoumute.Data.AppDAO
import com.ajsnarr.choosewhatyoumute.Data.AppDatabase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var appDao: AppDAO
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        appDao = db.appDAO
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetApp() {
        val id = "com.ajsnarr.testing123"
        val app = App(id=id, isMuted=false)
        appDao.insert(app)
        val recievedApp = appDao.get(id)
        Assert.assertEquals(app.id, recievedApp?.id)
        Assert.assertEquals(app.isMuted, recievedApp?.isMuted)
    }
}