package fi.aalto.cs.apluscourses.e2e.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.SearchContext
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ActionButtonFixture
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.fixtures.JListFixture
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import fi.aalto.cs.apluscourses.e2e.utils.LocatorBuilder
import java.time.Duration
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JMenuBar
import javax.swing.JMenuItem

fun RemoteRobot.welcomeFrame() = find(WelcomeFrameFixture::class.java, Duration.ofSeconds(60))

fun RemoteRobot.ideFrame() = find(IdeFrameFixture::class.java, Duration.ofSeconds(20))

fun SearchContext.customComboBox(name: String) = find(
    CustomComboBoxFixture::class.java,
    LocatorBuilder()
        .withAttr("accessiblename", name)
        .withClass(JComboBox::class.java)
        .build()
)

fun SearchContext.dialog(title: String, timeout: Duration = Duration.ofSeconds(5)) =
    find(
        DialogFixture::class.java,
        LocatorBuilder()
            .withAttr("title", title)
            .withClass(JDialog::class.java)
            .build(),
        timeout
    )

fun SearchContext.heavyWeightWindow() = find(HeavyWeightWindowFixture::class.java, Duration.ofSeconds(5))

@FixtureName("Welcome Frame")
@DefaultXpath(by = "FlatWelcomeFrame type", xpath = "//div[@class='FlatWelcomeFrame']")
class WelcomeFrameFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {
    fun newProjectButton() = button(
        byXpath(
            "//div[(@class='MainButton' and @text='New Project') " +
                "or (@accessiblename='New Project' and @class='JButton')]"
        )
    )
}

@FixtureName("IDE Frame")
@DefaultXpath("IdeFrameImpl type", "//div[@class='IdeFrameImpl']")
class IdeFrameFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {
    fun menu() = find(
        MenuItemFixture::class.java,
        LocatorBuilder().withClass(JMenuBar::class.java).build()
    )

    fun projectViewTree() = find(CommonContainerFixture::class.java, byXpath("//div[@class='ProjectViewTree']"))
    fun aPlusSideBarButton() = find(
        CommonContainerFixture::class.java,
        byXpath("//div[@accessiblename='A+ Courses' and @class='StripeButton' and @text='A+ Courses']")
    )
    fun modules() = find(
        JListFixture::class.java,
        byXpath("//div[@class='ModuleListView']"),
        Duration.ofSeconds(20)
    )
    fun assignments() = find(
        CommonContainerFixture::class.java,
        byXpath("//div[@class='TreeView']"),
        Duration.ofSeconds(20)
    )
    fun filterButton() = find(
        ActionButtonFixture::class.java,
        byXpath("//div[@accessiblename='Filter Assignments']"),
        Duration.ofSeconds(20)
    )
    fun userButton() = find(
        ActionButtonFixture::class.java,
        byXpath("//div[@class='ActionButton' and @myaction='Not logged in (null)']"),
        Duration.ofSeconds(20)
    )
    fun searchEverywhereButton() = find(
        ActionButtonFixture::class.java,
        byXpath("//div[@accessiblename='Search Everywhere' and @class='ActionButton']"),
        Duration.ofSeconds(20)
    )
    fun dropDownMenu() = with(heavyWeightWindow()) {
        find(
            JListFixture::class.java,
            byXpath("//div[@class='MyList']"),
            Duration.ofSeconds(20)
        )
    }
    fun codeWithMeButton() = findAll(
        JButtonFixture::class.java,
        byXpath("//div[@class='JButton' and @text='Got It']")
    ).firstOrNull()
    fun ideErrorButton() = find(
        ComponentFixture::class.java,
        byXpath("//div[@class='IdeErrorsIcon']"),
        Duration.ofSeconds(20)
    )
    fun isDumbMode(): Boolean =
        callJs(
            "com.intellij.openapi.project.DumbService.isDumb(component.project);",
            runInEdt = true
        )
    fun waitForSmartMode() {
        waitFor(
            duration = Duration.ofMinutes(2),
            interval = Duration.ofSeconds(5),
            errorMessage = "Indexing takes too long"
        ) {
            isDumbMode().not()
        }
    }
}

@FixtureName("Menu Item")
class MenuItemFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    ContainerFixture(remoteRobot, remoteComponent) {
    fun item(text: String) = find(
        MenuItemFixture::class.java,
        LocatorBuilder()
            .withAttr("text", text)
            .withClass(JMenuItem::class.java)
            .build()
    )
    fun select(text: String): MenuItemFixture = with(item(text)) { click(); return@select this }
}

@FixtureName("Dialog")
class DialogFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {
    fun ContainerFixture.sidePanel() = find(
        ContainerFixture::class.java,
        byXpath("//div[@class='SidePanel']")
    )
    fun passwordField() = find(
        JTextFieldFixture::class.java,
        byXpath("//div[@class='JPasswordField']")
    )
}

@FixtureName("Custom Combo Box")
class CustomComboBoxFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    ComponentFixture(remoteRobot, remoteComponent) {
    fun dropdown() = click()
}

@FixtureName("Heavy Weight Window")
@DefaultXpath("HeavyWeightWindow type", "//div[@class='HeavyWeightWindow']")
class HeavyWeightWindowFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent)
