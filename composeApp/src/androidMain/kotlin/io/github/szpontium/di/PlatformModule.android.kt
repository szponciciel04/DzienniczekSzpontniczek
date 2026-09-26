package io.github.szpontium.di

import io.github.szpontium.update.ApkInstaller
import io.github.szpontium.update.ApkStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { ApkStorage(get()) }
    single { ApkInstaller(get()) }
}
