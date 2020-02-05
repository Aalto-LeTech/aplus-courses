## Manual testing ##

[The complete list](https://github.com/Aalto-LeTech/intellij-plugin/labels/manual%20testing) of features that require writing
a manual testing manual. Marked with a github issue label "manual testing".


<details>
  <summary>
    <a href="https://github.com/Aalto-LeTech/intellij-plugin/issues/9">making hints on missing plugins #9</a>
  </summary>
  <p>
    <p><b>Part 1. Checking missing plugins</b></p>
    <ol>
      <li>Ensure "Scala" plugin is not installer <b>(File | Settings | Plugins | Marketplace)</b></li>
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
  </p>
    <p>
    <p><b>Part 2. Checking disabled plugins</b></p>
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
  </p>
</details>
<details>
  <summary>
    <a href="https://github.com/Aalto-LeTech/intellij-plugin/issues/44">add new startup notification saying the plugin is in beta/dev #44</a>
  </summary>
  <p>
    <p><b>Checking the notification regard the current A+ Course plugin version</b></p>
    <ol>
      <li>Ensure "A+ Course" plugin is installed <b>(File | Settings | Plugins | Installed)</b> and check the plugin version from the plugin window or <a href="https://plugins.jetbrains.com/plugin/13634-a-plugin-for-intellij/versions">online.</a></li>
      <li>Restart an IDE</li>
      <li>Observe a notification saying and ensure the version matches the one shown for the plugin.
        <br/>
        <i>
          "A+ Courses plugin is under development: You are using version <b>0.1.0</b> of A+ Courses plugin, which is a pre-release version of the plugin and still under development. Some features of this plugin are still probably missing, and the plugin is not yet tested thoroughly. Use this plugin with caution and on your own risk!
       </i>
      </li>
      <li>The notification should reamin after the restart is done.</li>
    </ol>  
  </p>
</details>
