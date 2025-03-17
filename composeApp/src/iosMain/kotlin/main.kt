import androidx.compose.ui.window.ComposeUIViewController
import com.skanderjabouzi.alarm.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
