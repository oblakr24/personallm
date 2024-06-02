import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {

    let root: RootComponent
    let backDispatcher: BackDispatcher

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(root: root, backDispatcher: backDispatcher)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {

    let root: RootComponent
    let backDispatcher: BackDispatcher
    
    var body: some View {
        ComposeView(root: root, backDispatcher: backDispatcher)
            .ignoresSafeArea(.keyboard, edges: .all) // Compose has own keyboard handler
    }
}



