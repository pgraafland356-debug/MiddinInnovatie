package com.middin.innovatie.app

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.middin.innovatie.app.data.local.AppDatabase
import com.middin.innovatie.app.data.local.DatabaseMigrations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomMigrationTest {
    private val dbName = "middin-migration-test.db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2_addsProducts() {
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                """
                CREATE TABLE IF NOT EXISTS `collective_memory` (
                    `id` TEXT NOT NULL,
                    `content` TEXT NOT NULL,
                    `authorName` TEXT NOT NULL,
                    `createdAtEpochMs` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            close()
        }
        val db = helper.runMigrationsAndValidate(
            dbName,
            2,
            true,
            DatabaseMigrations.MIGRATION_1_2,
        )
        db.query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='products'").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(1, c.getInt(0))
        }
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate2To3_addsLocalChatMessages() {
        helper.createDatabase(dbName, 2).apply {
            execSQL(
                """
                CREATE TABLE IF NOT EXISTS `collective_memory` (
                    `id` TEXT NOT NULL,
                    `content` TEXT NOT NULL,
                    `authorName` TEXT NOT NULL,
                    `createdAtEpochMs` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            execSQL(
                """
                CREATE TABLE IF NOT EXISTS `products` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `imageUri` TEXT,
                    `createdAtEpochMs` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            close()
        }
        val db = helper.runMigrationsAndValidate(
            dbName,
            3,
            true,
            DatabaseMigrations.MIGRATION_2_3,
        )
        db.query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='local_chat_messages'").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(1, c.getInt(0))
        }
        db.close()
    }
}
