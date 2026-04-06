package io.github.szpontium.di

import io.github.szpontium.platform.createHttpClient
import io.github.szpontium.session.ApiSession
import io.github.szpontium.session.SessionStorage
import io.github.szpontium.session.createSessionDataStore
import io.github.szpontium.viewmodel.AnnouncementsViewModel
import io.github.szpontium.viewmodel.DashboardViewModel
import io.github.szpontium.viewmodel.ExamsViewModel
import io.github.szpontium.viewmodel.GradesViewModel
import io.github.szpontium.viewmodel.HomeworkViewModel
import io.github.szpontium.viewmodel.LoginViewModel
import io.github.szpontium.viewmodel.NotesViewModel
import io.github.szpontium.viewmodel.TimetableViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ApiSession() }
    single { createHttpClient() }
    single { createSessionDataStore() }
    single { SessionStorage(get(), get()) }

    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { GradesViewModel(get()) }
    viewModel { TimetableViewModel(get()) }
    viewModel { ExamsViewModel(get()) }
    viewModel { HomeworkViewModel(get()) }
    viewModel { NotesViewModel(get()) }
    viewModel { AnnouncementsViewModel(get()) }
}
