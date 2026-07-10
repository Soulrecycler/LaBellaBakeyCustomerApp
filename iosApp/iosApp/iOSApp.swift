import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        // Kotlin `initKoinIos()` is exported as `doInitKoinIos()` (ObjC renames init* symbols).
        MainViewControllerKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
        }
    }
}
