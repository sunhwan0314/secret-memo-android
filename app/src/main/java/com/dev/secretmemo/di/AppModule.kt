package com.dev.secretmemo.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dev.secretmemo.data.datasource.CounterLocalDataSource
import com.dev.secretmemo.data.datasource.CounterLocalDataSourceImpl
import com.dev.secretmemo.data.datasource.MemoLocalDataSource
import com.dev.secretmemo.data.datasource.MemoLocalDataSourceImpl
import com.dev.secretmemo.data.repository.CounterRepository
import com.dev.secretmemo.data.repository.CounterRepositoryImpl
import com.dev.secretmemo.data.repository.MemoRepository
import com.dev.secretmemo.data.repository.MemoRepositoryImpl
import com.dev.secretmemo.ui.counter.CounterViewModel
import com.dev.secretmemo.ui.edit.MemoEditViewModel
import com.dev.secretmemo.ui.main.MainViewModel
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@Suppress("DEPRECATION")
fun provideEncryptedSharedPreferences(context: Context): SharedPreferences {
    return try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "secret_memo_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        context.getSharedPreferences("secret_memo_prefs_fallback", Context.MODE_PRIVATE)
    }
}

val appModule = module {
    single<SharedPreferences> { provideEncryptedSharedPreferences(androidContext()) }
    single { Gson() }
    
    // Memo
    single<MemoLocalDataSource> { MemoLocalDataSourceImpl(get(), get()) }
    single<MemoRepository> { MemoRepositoryImpl(get()) }
    
    // Counter
    single<CounterLocalDataSource> { CounterLocalDataSourceImpl(get(), get()) }
    single<CounterRepository> { CounterRepositoryImpl(get()) }
    
    // ViewModels
    viewModel { MainViewModel(get()) }
    viewModel { MemoEditViewModel(get()) }
    viewModel { CounterViewModel(get()) }
}
