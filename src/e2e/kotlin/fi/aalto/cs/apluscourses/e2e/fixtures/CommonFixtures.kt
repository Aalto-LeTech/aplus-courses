package fi.aalto.cs.apluscourses.e2e.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.SearchContext
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JMenuItem

fun RemoteRobot.welcomeFrame() = find(WelcomeFrame::class.java, Duration.ofSeconds(60))

fun RemoteRobot.ideFrame() = find(IdeFrame::class.java, Duration.ofSeconds(20))

fun <T> Class<T>.toXPathExp(): String = "@javaclass='$name' or contains(@classhierarchy, '$name')"

fun <T> byAttributeAndSwingClass(attr : String, value : String, clazz : Class<T>) =
    byXpath("//div[@$attr='$value' and (${clazz.toXPathExp()})]")

fun SearchContext.button(name: String) = find(Button::class.java,
    byAttributeAndSwingClass("accessiblename", name, JButton::class.java))

fun SearchContext.comboBox(name: String) = find(ComboBox::class.java,
    byAttributeAndSwingClass("accessiblename", name, JComboBox::class.java))

fun SearchContext.dialog(title: String, timeout : Duration = Duration.ofSeconds(5)) =
    find(Dialog::class.java, byAttributeAndSwingClass("title", title, JDialog::class.java), timeout)

fun SearchContext.heavyWeightWindow() = find(HeavyWeightWindow::class.java)

@FixtureName("Welcome Frame")
@DefaultXpath(by = "FlatWelcomeFrame type", xpath = "//div[@class='FlatWelcomeFrame']")
class WelcomeFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ContainerFixture(remoteRobot, remoteComponent)

@FixtureName("IDE Frame")
@DefaultXpath("IdeFrameImpl type", "//div[@class='IdeFrameImpl']")
class IdeFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ContainerFixture(remoteRobot, remoteComponent) {
  fun menu() = find(MenuItem::class.java)
}

@FixtureName("Button")
class Button(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ComponentFixture(remoteRobot, remoteComponent)

@FixtureName("Menu Item")
@DefaultXpath("MenuFrameHeader type", "//div[@class='MenuFrameHeader']")
class MenuItem(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ContainerFixture(remoteRobot, remoteComponent) {
  fun item(text: String) = find(MenuItem::class.java,
      byAttributeAndSwingClass("text", text, JMenuItem::class.java))
  fun select(text: String) : MenuItem = with(item(text)) { click(); return@select this }
}

@FixtureName("Dialog")
class Dialog(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ContainerFixture(remoteRobot, remoteComponent) {
  fun header() = find(ContainerFixture::class.java, byXpath("//div[@class='DialogHeader']"))
  fun close() = header().button("Close").click()
  fun ContainerFixture.sidePanel() = find(ContainerFixture::class.java,
      byXpath("//div[@class='SidePanel']"))
}

@FixtureName("Combo Box")
class ComboBox(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ContainerFixture(remoteRobot, remoteComponent) {
  fun dropdown() = click()
}

@FixtureName("Heavy Weight Window")
@DefaultXpath("HeavyWeightWindow type", "//div[@class='HeavyWeightWindow']")
class HeavyWeightWindow(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
  : ContainerFixture(remoteRobot, remoteComponent);
