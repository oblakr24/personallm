import SwiftUI
import ComposeApp
import UIKit

@main
struct iOSApp: App {
  @UIApplicationDelegateAdaptor(AppDelegate.self)
   var appDelegate: AppDelegate

	var body: some Scene {
		WindowGroup {
            ContentView(comp: appDelegate.diComponent, root: appDelegate.root)
		}
	}
}
