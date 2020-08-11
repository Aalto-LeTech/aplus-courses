Manual testing
==============

[The complete list](https://github.com/Aalto-LeTech/intellij-plugin/labels/manual%20testing)
of features that require writing a manual testing manual.
Marked with a github issue label "manual testing."

### Notes

+ The menus of IntelliJ are bit different on Mac than on Linux/Windows.
  For example, to open settings on Linux, one can navigate to **File > Settings...**
  but on Mac, the same thing is achieved with **IntelliJ IDEA > Preferences...**.

+ Similarly, on Mac one does not use right-click
  but, instead, clicks holding **Ctrl** key down.

### 0 Setup

**0.1** Make sure you are using a clean installment of
        **IntelliJ IDEA Community** version **2020.1**.
- You can download IntelliJ IDEA Community from https://www.jetbrains.com/idea/download.
- In case you use pre-existing installment of IntelliJ IDEA,
  you can use one of the following ways to restore to the default settings:

**0.1.A.** In the main window, choose **File > Manage IDE Settings > Restore Default Settings...**
- In the popup window, choose **Restore and restart**.

**0.1.B** In the startup window, choose **Configure > Restore Default Settings...**
- In the popup window, choose **Restore and restart**.

**0.2** When the IntelliJ IDEA opens first time, choose **Light** UI theme and
        click **Skip Remaining and Set Defaults**.

**0.3** Remove installed external plugins, if there are any.
- In the startup window, choose **Configure > Plugins** and open **Installed** tab.
- If there are **Downloaded** plugins on the top of the list,
  uninstall all of them, one by one.
- Restart IntelliJ IDEA.

### 1 Install **A+ Courses** plugin.

**1.1** In the startup window, choose **Configure > Plugins**.

**1.2** Do one of the following options, depending on whether you are testing
        a pre-publish version of the plugin or a published version.

**1.2.A** Install a published version.
- Open **Marketplace** tab and search **A+ Courses** plugin.
- Ensure that the version is the one you want to test.
- Click **Install**.

**1.2.B** Install a pre-publish version.
- If you don't have a ZIP file yet, you can generate it.
  To get the source code, execute

      git clone https://github.com/Aalto-LeTech/intellij-plugin.git

  and navigate to the cloned directory.
  Checkout the branch/tag/commit you want to test, and execute
 
      ./gradlew buildPlugin
  
  Once the build is ready,
  the generated ZIP file can be found in `build/distributions`.
- In **Plugins** window, click the cog icon next to the tabs.
- From a popup menu, choose **Install Plugin from Disk...**
- Select the ZIP file on the popped-up file selector.

**1.3** ASSERTION: IntelliJ IDEA requires a restart.

**1.4** Restart IntelliJ IDEA.

### 2 Initialize a new project

**2.1** In the startup window, click **Create New Project**.

**2.2** In **New Project** window, choose **Empty Project** on a left-hand side list.

**2.3** Click **Next**.

**2.4** On the next view, click **Finish**.

**2.5** If **Tip of the Day** window is shown,
        check **Don't show tips** and click **Close**. 

**2.6** ASSERTION: **Project Structure** window opens with **Modules** tab visible.

**2.7** Switch to **Project** tab.

**2.8** ASSERTION: **Project SDK** reads **\<No SDK\>**.

**2.9** Choose JDK 11 as **Project SDK**.
- If JDK 11 is not installed on the computer,
  choose **Add SDK > Download JDK...**
- On the window that opens,
  choose **AdoptOpenJDK (HotSpot)** as **Vendor**
  and **11.x.x** as **Version**,
  and click **Download**.

**2.10** Click **OK** to close **Project Structure** window.

### 3 Observe startup notification

**3.1** ASSERTION: The screen shows a notification telling
        you are using a pre-release version of the plugin.

**3.2** ASSERTION: The version mentioned in the notification is correct.

![Version notification](images/version-notification.png)

- You can check the version of the plugin in **File > Settings... > Plugins > Installed**

### 4 About window

**4.1** From the main menu, choose **A+ > About A+ Plugin**.

**4.2** The about window is shown.

**4.3** Close the window by clicking **OK**.

### 5 Turn project into A+ project

**5.1** From the main menu, choose **A+ > Turn project into A+ course project**.

**5.2** On the dialog that opens, click **Cancel**.

**5.3** ASSERTION: The dialog closes but nothing else seems to happen.

**5.4** Open **A+ > Turn project into A+ course project** again.

**5.5** ASSERTION: **Leave IntelliJ settings unchanged** checkbutton is not checked.

**5.6** Check "leave unchanged" checkbox and click OK.

**5.7** ASSERTION: **O1Library** appears as a module in the project tree.

**5.8** ASSERTION: **A+ Courses** tool window shows a list of O1 modules in **Modules** list.

**5.9** ASSERTION: **O1Library** is marked **Installed** in **Modules** list.

**5.10** Once again, navigate to **A+ > Turn project into A+ course project**.

**5.11** Leave the checkbox unchecked and click **OK**.

**5.12** The screen shows a dialog that tells the IDE will be restarted.

**5.13** Click **OK**.

**5.14** The IDE restarts.

**5.15** ASSERTION: The theme has changed to dark.

### 6 Importing modules

**6.1** On **Modules** list  tool window, double-click **RobotTribes**.

**6.2** ASSERTION: **RobotTribes** and **Robots** appear as modules in the project tree.

**6.3** ASSERTION: **RobotTribes** and **Robots** are marked **Installed** in **Modules** list.

**6.4** On **Modules** list, right-click **SwingExamples**.

**6.5** On the context menu that appears, choose **Import A+ module**.

**6.6** ASSERTION: **SwingExamples** appears as a module in the project tree.

![Project tree](images/project-tree.png)

**6.7** ASSERTION: **SwingExamples** is marked **Installed** in the modules list.

**6.8** On **Modules** list, select multiple uninstalled modules.
- Hold **Ctrl**/**Cmd** key down while clicking to select many items.

**6.9** Click the button with download icon
        on the top of the **Modules** list
        to install all selected modules.

**6.10** ASSERTION: The chosen modules appear in the project tree.

**6.11** ASSERTION: The chosen modules are marked **Installed** in **Modules** list.

**6.12** ASSERTION: All the installed modules have their dependencies correctly configured.
- To check this, navigate to **File > Project Structure... > Modules**
  and make sure none of the modules is underlined in red.
- Exit **Project Structure** window by clicking **Cancel**.

### 7 Using Scala REPL

#### Part I: Basic case

**7.1** Open REPL by choosing a folder or a file within **SwingExamples** module.
- Read [this](https://confluence.jetbrains.com/pages/viewpage.action?pageId=53326891)
  if you don't know how to open and use REPL.

**7.2** In **REPL configuration** window that opens,
        uncheck **Don't show this window again** checkbox.

![REPL dialog](images/repl-dialog.png)

**7.3** Click **OK**.

**7.4** ASSERTION: A REPL opens with **SwingExamples** in its title. 

**7.5** ASSERTION: Welcome message contains the **SwingExamples** as module name and a list of 
imported packages: **o1, o1.llama, o1.randomtext**.

**7.6** ASSERTION: Welcome message contains `[Ctrl+Enter]` as one of the available commands.

![REPL welcome](images/repl-welcome.png)

**7.7** Execute the following command in REPL:

    val RTA = RandomTextApp

**7.8** ASSERTION: Results of the command has no errors and looks alike the following image.

![REPL RTA](images/repl-rta.png)

**7.9** Execute the following two commands in REPL:

    sys.props("user.dir")
    sys.props("java.class.path")

**7.10** ASSERTION: The output of the first statement is the directory of **SwingExamples** module.

![REPL verify](images/repl-verify.png)

**7.11** ASSERTION: The output of the second statement (classpath) contains **SwingExamples**.

**7.12** Close the REPL by clicking X next to its tab title.

**7.13** The window opens asking whether REPL should be terminated.
        Check **Remember, don't ask again** checkbox and click **Terminate**.

#### Part II: Changing parameters

**7.14** Reopen REPL by choosing a folder or a file within **SwingExamples** module.

**7.15** Again, uncheck **Don't show this window again** checkbox.

**7.16** Change **Working directory** to be the directory of **O1Library** module.
- Just replacing **SwingExamples** with **O1Library** in the path does the trick.

**7.17** Change value for **Use classpath and SDK of module** dropdown list to be **O1Library**.

**7.18** Click **OK**.

**7.19** ASSERTION: A REPL opens with **O1Library** in its title.

**7.20** In REPL, execute the same two commands as in **7.9**.

**7.21** ASSERTION: The output of the first statement is the directory of **O1Library** module.

**7.22** ASSERTION: The output of the second statement (classpath) contains **O1Library**
         (but not other modules, such as **SwingExamples**).

**7.23** Close the REPL.

#### Part III: Inconsistent parameters

**7.24** Reopen REPL by choosing a folder or a file within **O1Library** module.

**7.25** This time, leave **Don't show this window again** checkbox checked.

**7.26** Change **Working directory** to be the directory of **SwingExamples** module.

**7.27** Click **OK**.

**7.28** ASSERTION: A REPL opens with **O1Library** in its title.

**7.29** In REPL, execute the same two commands as in **7.9**.

**7.30** ASSERTION: The output of the first statement is the directory of **SwingExamples** module.

**7.31** ASSERTION: The output of the second statement (classpath) contains **O1Library**
         (but not other modules, such as **SwingExamples**).

**7.32** Close the REPL.

#### Part IV: Persistence of choice

**7.33** Reopen REPL by choosing a folder or a file within **O1Library** module.

**7.34** ASSERTION: **REPL Configuration** window does not show up.

**7.35** ASSERTION: A REPL opens with **O1Library** in its title.

**7.36** Execute the same two commands as in **7.9**.

**7.37** ASSERTION: The output of the first statement is the directory of **O1Library** module.

**7.38** ASSERTION: The output of the second statement (classpath) contains **O1Library**
         (but not other modules, such as **SwingExamples**).

**7.39** Close the REPL.

**7.40** Restart te IDE.

**7.41** Once the IDE has restarted, open the REPL by choosing any file.

**7.42** ASSERTION: **REPL Configuration** window does not show up.

**7.43** ASSERTION: A REPL opens.

**7.44** Close the REPL.

#### Part V: Settings reset.

**7.45** From the main menu, choose **A+ > Reset A+ Courses Plugin Settings**.

**7.46** Open REPL by choosing any file.

**7.47** ASSERTION: **REPL Configuration** window shows up.

**7.48** Close the window by clicking **Cancel**.

**7.49** ASSERTION: No REPL opens.

### 8 Removing a module

**8.1** Right-click **SwingExamples** in the project tree
        and choose **Remove Module** from the context menu.

**8.2** Confirm the removal by clicking **Remove** on a popup window.

**8.3** ASSERTION: **SwingExamples** no longer shows **Installed** in **Modules** list.

**8.4** Double click **SwingExamples** in **Modules** list.

**8.5** ASSERTION: **SwingExamples** shows up as a module in the project tree.
