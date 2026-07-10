package com.bakery.customer.app

import androidx.compose.ui.window.ComposeUIViewController
import com.bakery.customer.app.di.initKoin
import com.bakery.customer.core.database.DatabaseBuilderFactory
import com.bakery.customer.core.database.SessionStoreFactory
import org.koin.dsl.module
import platform.UIKit.UIViewController

private val iosModule = module {
    single { DatabaseBuilderFactory() }
    single { SessionStoreFactory() }
}

fun initKoinIos() = initKoin(platformModule = iosModule)

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
