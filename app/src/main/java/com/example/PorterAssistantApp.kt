package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.preferences.SettingsRepository
import com.example.geocoding.CompositeGeocodingEngine

class PorterAssistantApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var geocodingEngine: CompositeGeocodingEngine
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "porter_assistant.db"
        ).fallbackToDestructiveMigration().build()

        settingsRepository = SettingsRepository(applicationContext)
        geocodingEngine = CompositeGeocodingEngine(applicationContext)
    }

    companion object {
        lateinit var instance: PorterAssistantApp
            private set
    }
}
