## Manual testing ##

[The complete list](https://github.com/Aalto-LeTech/intellij-plugin/labels/manual%20testing) of features that require writing
a manual testing manual. Marked with a github issue label "manual testing".


<details>
  <summary>
    <a href="https://github.com/Aalto-LeTech/intellij-plugin/issues/9">making hints on missing plugins #9</a>
  </summary>
  <div>
    <h5>Part 1. Checking missing plugins</h5>
    <ol>
      <li>Ensure "Scala" plugin is not installed <b>(File | Settings | Plugins | Marketplace)</b></li>
      <li>Restart an IDE</li>
      <li>Observe a notification saying
        <br/>
        <i>
        "A+
        The additional plugin(s) must be installed and enabled for the A+ plugin to work properly (Scala).
        <br/>
        <a href="">Install missing (Scala) plugin(s).</a>"
       </i>
      </li>
      <li>Click on the highlighted part of the notification, approve restart of the IDE</li>
      <li>After the restart is done, ensure there is no notification anymore</li>
    </ol>
  </div>
  <div>
    <h5>Part 2. Checking disabled plugins</h5>
    <ol>
      <li>Ensure 'Scala' plugin is installed and disabled
        <img src="images/%239_disable_plugin.png" alt="Ensure 'Scala' plugin is installed and disabled">
      </li>
      <li>Restart an IDE</li>
      <li>Observe a notification
        <img src="images/%239_enable_plugins_notification.png" alt="Observe a notification">
      </li>
      <li>Click on the highlighted part of the notification</li>
      <li>Check the notification became inactive
        <img src="images/%239_notification_inactive.png" alt="Check the notification became inactive">
      </li>
      <li>After the restart is done, ensure there is no notification anymore</li>
    </ol>  
  </div>
</details>
<details>
  <summary>
    <a href="https://github.com/Aalto-LeTech/intellij-plugin/issues/44">add new startup notification saying the plugin is in beta/dev #44</a>
  </summary>
  <div>
    <h5>Checking the notification regard the current A+ Course plugin version</h5>
    <ol>
      <li>Ensure "A+ Course" plugin is installed <b>(File | Settings | Plugins | Installed)</b> and check the plugin version from the plugin window or <a href="https://plugins.jetbrains.com/plugin/13634-a-plugin-for-intellij/versions">online.</a></li>
      <li>Restart an IDE</li>
      <li>Observe a notification saying and ensure the version matches the one shown for the plugin.
        <br/>
        <i>
          "A+ Courses plugin is under development: You are using version <b>0.1.0</b> of A+ Courses plugin, which is a pre-release version of the plugin and still under development. Some features of this plugin are still probably missing, and the plugin is not yet tested thoroughly. Use this plugin with caution and on your own risk!
       </i>
      </li>
      <li>The notification should remain after the restart is done.</li>
    </ol>
  </div>
</details>
<details>
  <summary>
    <a href="https://github.com/Aalto-LeTech/intellij-plugin/issues/30">install required
    dependencies for the project from LOCAL #30</a>
  </summary>
  <div>
    <h5>Part 1. Importing a module by double-clicking it</h5>
    <ol>
      <li>Create a new project.</li>
      <li>
        Open the <em>Modules</em> tool window (if it is not open). <sub>You may have to wait a
        few seconds for the list of modules to be initialized. If the initialization takes more
        than 10&nbsp;seconds, consider it an error.</sub>
      </li>
      <li>Select <em>GoodStuff</em> from the list and double click it.</li>
      <li>
        Ensure that <em>GoodStuff</em> and <em>O1Library</em> appear as loaded modules in the
        project tree, and their contents match the image below:<br/>
        <img src="images/30_module_loaded.png" alt="GoodStuff and O1Library contents" />
      </li>
      <li>
        Ensure that <em>GoodStuff</em> and <em>O1Library</em> are marked <em>Installed</em> in the
        <em>Modules</em> tool window.
      </li>
      <li>
        From <i>File</i> menu, open <i>Project Structure...</i> and navigate to <i>Modules</i> page
        (under <i>Project Settings</i>).  Ensure that <em>GoodStuff</em> and <em>O1Library</em> are
        listed there and neither of them is marked red (signaling missing dependencies).
      </li>
    </ol>
  </div>
  <div>
    <h5>Part 2. Importing a module using context menu.</h5>
    <ol>
      <li>
        Continuing from <strong>Part 1</strong>, right-click a non-installed module of your choice
        in the <em>Modules</em> tool window. <sub>On Mac with only one mouse button, you may need
        to use some other gesture to open a context menu, like holding <em>Ctrl</em> key while
        clicking. Use the way that is standard to the system.</sub>
      </li>
      <li>Ensure that a pop-up menu appears next to the mouse pointer.</li>
      <li>Click <em>Import A+ Module</em> menu item.</li>
      <li>
        Ensure that the module appears in the project tree. <sub>If module has dependencies, those
        are imported too. If other modules appear in the project tree in this step, you can assume
        they are dependencies of the module you chose and ignore them.</sub>
      </li>
      <li>
        Ensure that the module is marked <em>Installed</em> in the <em>Modules</em> tool window.
      </li>
    </ol>
  </div>
  <div>
    <h5>Part 3. Importing multiple modules using toolbar button.</h5>
    <ol>
      <li>
        Continuing from <strong>Part 2</strong>, select multiple non-installed modules in the
        <em>Modules</em> tool window by clicking them while holding <em>Ctrl</em> key.
        <sub>Again, Mac may do things differently, so use the way to select multiple items that is
        standard to the system.</sub>
      </li>
      <li>
        Click <em>Import A+ Module</em> toolbar button on the top of the <em>Modules</em> tool
        window. <sub>The button is denoted with a "download" icon.</sub>
      </li>
      <li>
        Ensure that the selected modules appear in the project tree. <sub>Again, in case other
        modules appear there as well, assume they are appropriate dependencies and ignore them.
        </sub>
      </li>
      <li>
        Ensure that the modules you selected are marked <em>Installed</em> in the <em>Modules</em>
        tool window.
      </li>
    </ol>
  </div>
</details>
<details>
  <summary>
    <a href="https://github.com/Aalto-LeTech/intellij-plugin/issues/92">implement REPL thingy #92</a>
  </summary>
  <div>
    <h5>Part 1. Importing a module</h5>
    <ol>
      <li>Create a new project.</li>
      <li>
        Open the <em>Modules</em> tool window (if it is not open). <sub>You may have to wait a
        few seconds for the list of modules to be initialized. If the initialization takes more
        than 10&nbsp;seconds, consider it an error.</sub>
      </li>
      <li>Select <em>GoodStuff</em> from the list and double click it to install the module.</li>
    </ol>
  </div>
  <div>
      <h5>Part 2. Verifying the REPL</h5>
      <ol>
        <li>Ensure Scala Plugin is installed and enabled</li>
        <li>Ensure Scala SDK is set properly <b>(File | Project Structure | Global Libraries | Add | Scala SDK)</b></li>
        <li>Open REPL by choosing a folder or a file within <em>GoodStuff</em> module <a href="https://confluence.jetbrains.com/pages/viewpage.action?pageId=53326891">(how-to)</a></li>
        <div>
          <h6>Part 2.1. REPL configuration dialog is shown, checkbox unchecked</h6>
            <ol>
            <li>When the REPL configuration dialog, that looks like the following image is shown, uncheck the "Don't show this window again" checkbox, click <em>OK</em>.<img src="images/%2392_REPL_configuration_dialog_initial.png" alt="REPL dialog" /></li>
            <li>Ensure the REPL that looks like the next one is shown. <img src="images/%2366_scala_REPL_workDir_and_classPath.png" alt="REPL" style="max-width: 50;max-width: 56% !important;"/></li>
            <li>Close the REPL and start it again.</li>
            <li>Ensure the REPL configuration dialog is shown, click "Cancel".</li>
            </ol>
        </div>
        <div>
          <h6>Part 2.2. REPL configuration dialog is shown, checkbox unchecked, valid changes</h6>
            <ol>
            <li>When the REPL configuration dialog, that looks like previous image is shown, uncheck the "Don't show this window again" checkbox.</li>
            <li>Under "User classpath and SDK of module" change the module to be <em>"O1Library"</em>, also select <em>"O1Library"</em>'s files source as a "Working directory", click <em>OK</em>.</li>
            <li>Ensure the REPL that looks like like the next one is shown, then close REPL. <img src="images/%2392_changed_module.png" alt="REPL" /></li>             
            </ol>
        </div> 
        <div>
          <h6>Part 2.3. REPL configuration dialog is shown, checkbox unchecked, invalid changes</h6>
            <ol>
            <li>When the REPL configuration dialog, that looks like previous image is shown, uncheck the "Don't show this window again" checkbox.</li>
            <li>Under "User classpath and SDK of module" change the module to be <em>"O1Library"</em>, also select <em>"GoodStuff"</em>'s files source as a "Working directory", click <em>OK</em>.</li>
            <li>Ensure the REPL that looks like the next image is shown, close the REPL.<img src="images/%2392_mixed_case_changed.png" alt="REPL" /></li>
            </ol>
        </div>
        <div>              
          <h6>Part 2.4. REPL configuration dialog is cancelled</h6>
          <ol>
          <li>Click <em>Cancel</em> when the REPL configuration dialog is shown and observe nothing happens.</li>
          </ol>
         </div>               
        <div>
          <h6>Part 2.5. REPL configuration dialog is shown, checkbox checked</h6>
          <ol>
          <li>Click <em>OK</em> when the REPL configuration dialog, that looks like the one displayed is shown.</li>
          <li>Ensure proper REPL is started.</li>
          <li>Close the REPL and start it again.</li>
          <li>Ensure the no REPL configuration dialog is shown.</li>
          </ol>
        </div>
        <h6>After each sub-part (2.x)</h6>
        <li>When the console opens, check, that the name of the REPL contains the name of the <em>GoodStuff</em> (selected module or "&lt;?&gt;")</li>
        <li>Next, type into the REPL prompt: <i>sys.props("user.dir")</i>
        </li>
        <li>Make sure, that the output directory is where the <em>GoodStuff</em> (selected) module resides</li>
        <li>Next, type into the REPL prompt: <i>sys.props("java.class.path")</i></li>
        <li>Make sure, that the output classpath contains the <em>GoodStuff</em> (selected) module, the complete result should look approximately like this:
        <img src="images/%2366_scala_REPL_workDir_and_classPath.png" alt="REPL" /><br/>
        </li>
      </ol>
    </div>
</details>
<details>
  <summary>
    <a href="https://github.com/Aalto-LeTech/intellij-plugin/issues/86">turn project into "A+ O1" project" feature #86</a>
  </summary>
  <div>
    <h5>Creating a Course Project</h5>
    <ol>
      <li>Start the IDE and observe that the "Modules" tool window is empty (no course loaded).</li>
      <li>From the A+ menu at the top, select "Turn Project into A+ Course Project</li>
      <li>
        An information dialog should appear notifying that the plugin will adjust IntelliJ
        IDEA settings. Check the opt out check box.
      </li>
      <li>Observe that the project gets reloaded (may appear as a quick "flash").</li>
      <li>The "Modules" tool window should now have a list of O1 modules.</li>
      <li>
        The .idea directory should contain O1 project settings (for an example scala_settings.xml).
      </li>
      <li>
        Run the "Turn Project into A+ Course Project" action again, this time not opting out of the
        IDE settings adjustments.
      </li>
      <li>The plugin should prompt you to restart the plugin, do so.</li>
      <li>The IDE should now have the dark theme from the O1 IDE settings.</li>
      <li>
        When the project gets opened, the plugin should recognize that the project is a O1 project
        and update the "Modules" tool window with O1 modules. 
      </li>
      <li>
        Using the "Turn Project Into A+ Course Project" action without a working internet connection
        should result in an error message.
      </li>
      <li>
        Using the "Turn Project Into A+ Course Project" action again should not import the already
        imported IDE settings again.
      </li>
    </ol>
  </div>
</details>
<details>
  <summary>
    <a href="https://github.com/Aalto-LeTech/intellij-plugin/issues/110">Renaming/removing module files leads to incorrect states in module list #110</a>
  </summary>
  <div>
    <h5>Part 1. Removing a module</h5>
    <ol>
      <li>Create a new project and import <i>GoodStuff</i> module.</li>
      <li>Right click the <i>O1Library</i> from the project tree and choose <i>Remove Module</i> from the popup menu.</li>
      <li>Choose <i>Remove</i> from the popup window.</li>
      <li>Ensure that <i>O1Library</i> is marked not installed in the modules list.</li>
      <li>Ensure that <i>GoodStuff</i> is marked "error in dependencies" in the modules list.</li>
    </ol>
  </div>
  <div>
    <h5>Part 2. Reinstalling a module</h5>
    <ol>
      <li>Continuing from Part 1, double-click <i>O1Library</i> in the modules list to re-install it.</li>
      <li>Ensure that <i>O1Library</i> shows loaded as a module in the project tree (it has the blue square symbol).</li>
      <li>Ensure that <i>O1Library</i> and <i>GoodStuff</i> are marked installed in the modules list.</li>
    </ol>
  </div>
  <div>
    <h5>Part 3. Removing and reinstalling Scala SDK</h5>
    <ol>
      <li>Continuing from Part 3, delete the directory of Scala SDK inside the <code>lib</code> directory.</li>
      <li>Ensure that <i>O1Library</i> and <i>GoodStuff</i> are marked "error in dependencies" in the modules list.</li>
      <li>Double-click <i>O1Library</i> in the modules list to re-install it.</li>
      <li>Ensure that <i>O1Library</i> and <i>GoodStuff</i> are marked installed in the modules list.</li>
    </ol>
  </div>
</details>
<details>
  <summary>
    <a href="https://github.com/Aalto-LeTech/intellij-plugin/issues/93">reset to "A+" settings #93</a>
  </summary>
  <div>
    <h5>Resetting A+ Courses Settings</h5>
    <ol>
      <li>Start the REPL for a module and check the "Don't show this window again" checkbox.</li>
      <li>Start the REPL for a module again and ensure that the window isn't shown again.</li>
      <li>From the A+ menu at the top, select "Reset A+ Courses Plugin Settings".</li>
      <li>Start the REPL for a module again and observe that the window is shown again.</li>
    </ol>
  </div>
</details>
<details>
  <summary>
    <a href="https://github.com/Aalto-LeTech/intellij-plugin/issues/130">
      Add AUTO_SHOW_ERRORS_IN_EDITOR="false" to workspace.xml when project settings are imported #130
    </a>
  </summary>
  <div>
    <h5>Importing project settings</h5>
    <ol>
      <li>Use the "Turn Project Into A+ Course Project" menu item to import project settings.</li>
      <li>Open the file <code>.idea/workspace.xml</code>.</li>
      <li>
        Ensure that it contains the following:
        <pre>
&lt;component name="CompilerWorkspaceConfiguration">
  &lt;option name="AUTO_SHOW_ERRORS_IN_EDITOR" value="false" />
&lt;/component></pre>
      </li>
      <li>Use the "Turn Project Into A+ Course Project" menu item again.</li>
      <li>
        If <code>workspace.xml</code> now contains option entry multiple times, save the
        <code>workspace.xml</code> file and ensure that the duplicate gets removed.
      </li>
    </ol>
  </div>
</details>
<details>
  <summary>
    <a href="https://github.com/Aalto-LeTech/intellij-plugin/issues/182">
      Cancelling IDE settings import does not work #182
    </a>
  </summary>
  <div>
    <h5>Course Project Action Dialog</h5>
    <ol>
      <li>From the A+ menu select "Turn Project Into A+ Course Project".</li>
      <li>Observe that the dialog mentions the course name "O1".</li>
      <li>Observe that by default the restart checkbox is checked and the opt out checkbox isn't.</li>
      <li>Observe that checking the opt out checkbox disables the restart checkbox.</li>
      <li>Select "Cancel" and observe that nothing has changed in the project.</li>
      <li>Select "Turn Project Into A+ Course Project" again.</li>
      <li>Select "OK" with the default selections and observe that the IDE restarts.</li>
      <li>Select "Turn Project Into A+ Course Project" again.</li>
      <li>Observe that the dialog mentions that IDE settings are already imported for O1.</li>
      <li>Observe that the restart checkbox is unchecked and not enabled.</li>
      <li>Observe that the opt out checkbox is checked and not enabled.</li>
    </ol>
  </div>
</details>
