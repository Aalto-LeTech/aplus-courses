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
      <li>
        <img src="images/%239_enable_plugins_notification.png" alt="Ensure 'Scala' plugin is installed and disabled">
      </li>
      <li>Restart an IDE</li>
      <li>
        <img src="images/%239_enable_plugins_notification.png" alt="Observe a notification">
      </li>
      <li>Click on the highlighted part of the notification</li>
      <li>
        <img src="images/%239_notification_inactive.png" alt="Check the notification became inactive">
      </li>
      <li>After the restart is done, ensure there is no notification anymore</li>
    </ol>  
  </p>
</details>
