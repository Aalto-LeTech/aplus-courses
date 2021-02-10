package fi.aalto.cs.apluscourses.e2e.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.SearchContext
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import fi.aalto.cs.apluscourses.e2e.utils.LocatorBuilder
import java.time.Duration
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JMenuItem

fun RemoteRobot.welcomeFrame() = find(WelcomeFrameFixture::class.java, Duration.ofSeconds(60))

fun RemoteRobot.ideFrame() = find(IdeFrameFixture::class.java, Duration.ofSeconds(20))

fun SearchContext.button(name: String) = find(ButtonFixture::class.java,
  LocatorBuilder()
    .withAttr("accessiblename", name)
    .withClass(JButton::class.java)
    .build())

fun SearchContext.comboBox(name: String) = find(ComboBoxFixture::class.java,
  LocatorBuilder()
    .withAttr("accessiblename", name)
    .withClass(JComboBox::class.java)
    .build())

fun SearchContext.dialog(title: String, timeout : Duration = Duration.ofSeconds(5)) =
    find(DialogFixture::class.java,
      LocatorBuilder()
        .withAttr("title", title)
        .withClass(JDialog::class.java)
        .build(),
      timeout)

fun SearchContext.allDialogs() = findAll(DialogFixture::class.java,
  LocatorBuilder()
    .withClass(JDialog::class.java)
    .build())

fun SearchContext.heavyWeightWindow() = find(HeavyWeightWindowFixture::class.java)

@FixtureName("Welcome Frame")
@DefaultXpath(by = "FlatWelcomeFrame type", xpath = "//div[@class='FlatWelcomeFrame']")
class WelcomeFrameFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ContainerFixture(remoteRobot, remoteComponent)

@FixtureName("IDE Frame")
@DefaultXpath("IdeFrameImpl type", "//div[@class='IdeFrameImpl']")
class IdeFrameFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ContainerFixture(remoteRobot, remoteComponent) {
  fun menu() = find(MenuItemFixture::class.java)
}

@FixtureName("Button")
class ButtonFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ComponentFixture(remoteRobot, remoteComponent)

@FixtureName("Menu Item")
@DefaultXpath("MenuFrameHeader type", "//div[@class='MenuFrameHeader']")
class MenuItemFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ContainerFixture(remoteRobot, remoteComponent) {
  fun item(text: String) = find(MenuItemFixture::class.java,
    LocatorBuilder()
      .withAttr("text", text)
      .withClass(JMenuItem::class.java)
      .build())
  fun select(text: String) : MenuItemFixture = with(item(text)) { click(); return@select this }
}

@FixtureName("Dialog")
class DialogFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ContainerFixture(remoteRobot, remoteComponent) {
  fun header() = find(ContainerFixture::class.java, byXpath("//div[@class='DialogHeader']"))
  fun close() = header().button("Close").click()
  fun ContainerFixture.sidePanel() = find(ContainerFixture::class.java,
      byXpath("//div[@class='SidePanel']"))
}

@FixtureName("Combo Box")
class ComboBoxFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
    : ContainerFixture(remoteRobot, remoteComponent) {
  fun dropdown() = click()
}

@FixtureName("Heavy Weight Window")
@DefaultXpath("HeavyWeightWindow type", "//div[@class='HeavyWeightWindow']")
class HeavyWeightWindowFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent)
  : ContainerFixture(remoteRobot, remoteComponent);
