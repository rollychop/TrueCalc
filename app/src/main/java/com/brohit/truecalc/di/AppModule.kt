package com.brohit.truecalc.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.brohit.truecalc.common.Constant
import com.brohit.truecalc.common.Constant.DATA_STORE_NAME
import com.brohit.truecalc.data.AppAnalyticsImpl
import com.brohit.truecalc.data.SearchRepositoryImpl
import com.brohit.truecalc.data.data_source.local.room.AppDatabase
import com.brohit.truecalc.data.data_source.remote.GitHubApi
import com.brohit.truecalc.domain.AppAnalytics
import com.brohit.truecalc.domain.FixedChargesRepository
import com.brohit.truecalc.domain.SearchRepository
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    fun providesIoDispatcher() = Dispatchers.IO

    @Singleton
    @Provides
    fun providesApplicationScope(
        ioDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `favorite_tools_table` (" +
                        "`route` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`description` TEXT NOT NULL, " +
                        "`category` TEXT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`route`))"
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `tool_usage_table` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`route` TEXT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL)"
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `calculation_history_table` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`toolRoute` TEXT NOT NULL, " +
                        "`toolName` TEXT NOT NULL, " +
                        "`inputsJson` TEXT NOT NULL, " +
                        "`resultsJson` TEXT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL)"
            )
        }
    }

    @Singleton
    @Provides
    fun providesAppDB(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "true_calc.db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }

    @Singleton
    @Provides
    fun providesAppAnalytics(@ApplicationContext context: Context): AppAnalytics =
        AppAnalyticsImpl(context)


    @Singleton
    @Provides
    fun provideGsonBuilder(): Gson {
        return GsonBuilder().apply {
            addSerializationExclusionStrategy(
                object : ExclusionStrategy {
                    override fun shouldSkipField(f: FieldAttributes?): Boolean {
                        val exposes = f?.annotations?.filterIsInstance<Expose>()
                        val expose = exposes?.firstOrNull()
                        val serialize = expose?.serialize
                        return serialize?.not() ?: false
                    }

                    override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                        return false
                    }
                }
            )
        }.create()
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(
        @ApplicationContext appContext: Context
    ): OkHttpClient {
        val isDebug: Boolean =
            appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(
            if (isDebug) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        )

        //okHttpClient
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }


    @Singleton
    @Provides
    fun providesRetrofit(
        gson: Gson,
        okHttpClient: OkHttpClient,
        @ApplicationContext appContext: Context
    ): Retrofit {
        val isDebug: Boolean =
            appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(if (isDebug) Constant.BASE_URL else Constant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }


    @Singleton
    @Provides
    fun providePreferencesDataStore(
        @ApplicationContext appContext: Context,
        ioDispatcher: CoroutineDispatcher,
    ): DataStore<androidx.datastore.preferences.core.Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(SharedPreferencesMigration(appContext, DATA_STORE_NAME)),
            scope = CoroutineScope(ioDispatcher + SupervisorJob()),
            produceFile = { appContext.preferencesDataStoreFile(DATA_STORE_NAME) }
        )
    }


    @Provides
    fun providesFixedChargesRepository(
        appDatabase: AppDatabase
    ): FixedChargesRepository = FixedChargesRepository(appDatabase.fixedCharges())

    @Provides
    fun providesFavoriteToolDao(
        appDatabase: AppDatabase
    ) = appDatabase.favoriteTools()

    @Provides
    fun providesToolUsageDao(
        appDatabase: AppDatabase
    ) = appDatabase.toolUsage()

    @Provides
    fun providesCalculationHistoryDao(
        appDatabase: AppDatabase
    ) = appDatabase.calculationHistory()

    @Provides
    fun providesSearchRepository(): SearchRepository = SearchRepositoryImpl()

    @Provides
    fun providesGithubApi(okHttpClient: OkHttpClient): GitHubApi {
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(GitHubApi::class.java)
    }

}