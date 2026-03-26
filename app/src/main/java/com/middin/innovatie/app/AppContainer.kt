package com.middin.innovatie.app

import android.content.Context
import androidx.room.Room
import com.middin.innovatie.app.data.local.AppDatabase
import com.middin.innovatie.app.data.local.DatabaseMigrations
import com.middin.innovatie.app.data.InnovationNewsRepository
import com.middin.innovatie.app.data.ChatRepository
import com.middin.innovatie.app.data.local.LocalProductChatRepository
import com.middin.innovatie.app.data.remote.AuthRepository
import com.middin.innovatie.app.data.remote.GeminiRepository
import com.middin.innovatie.app.data.remote.createHttpClient

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "middin.db",
    )
        .addMigrations(DatabaseMigrations.MIGRATION_1_2, DatabaseMigrations.MIGRATION_2_3)
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()

    val userPreferences = UserPreferencesRepository(
        context = appContext,
        defaultApiBaseUrl = BuildConfig.API_BASE_URL,
    )
    val changelogRepository = ChangelogRepository()
    val innovationNewsRepository = InnovationNewsRepository()
    val updatesRepository = UpdatesRepository()
    val geminiRepository = GeminiRepository()

    private val httpClient = createHttpClient(enableLogging = BuildConfig.DEBUG)

    val authRepository = AuthRepository(
        client = httpClient,
        baseUrlProvider = { userPreferences.resolvedApiBaseUrl() },
        pathPrefix = BuildConfig.API_PATH_PREFIX,
        loginIdentifierField = BuildConfig.API_LOGIN_FIELD,
        userPreferences = userPreferences,
    )

    val chatRepository: ChatRepository = LocalProductChatRepository(
        chatDao = database.localChatMessageDao(),
        productDao = database.productDao(),
    )
}
