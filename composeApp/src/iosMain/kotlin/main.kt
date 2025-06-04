import androidx.compose.ui.window.ComposeUIViewController
import com.skanderjabouzi.alarm.App
import com.skanderjabouzi.alarm.alarmmanager.iOSAppLifeCycleObserver
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
    iOSAppLifeCycleObserver.startObserving()
}
