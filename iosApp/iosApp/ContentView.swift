import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {

    let comp: AppComponent
    let root: RootComponent

    func makeUIViewController(context: Context) -> UIViewController {

        MainViewControllerKt.MainViewController(component: comp, root: root)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    
    let comp: AppComponent
    let root: RootComponent
    
    var body: some View {
        ComposeView(comp: comp, root: root)
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}



