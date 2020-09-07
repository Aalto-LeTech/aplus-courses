Troubleshooting the plugin
==============

### Verifying the setup

**0.** ensure the latest compatible version of IntelliJ IDEA Community Edition is installed (2020.2. or later).

**1.** update plugin to the latest version (you can check the latest version on [Plugin's official page](https://plugins.jetbrains.com/plugin/13634-a-courses/versions)).

**2.** create a new project: **File -> New -> Project...** and then **Empty Project**.

**3.** Turn project into an A+ Project: **A+ -> Turn Project Into A+ Project** (and choose the rest of the settings asked for in the pop-up dialog).

**3.** check the IDE for errors (you **should not** see the blinking red icon as shown)

**4.** review the project structure for correctness: **File -> Project Structure ->** ...

**Project**

**Modules**

**Libraries**

**5.** download a **GoodStuff** module and run it by right-clicking and choosing **Run**

**6.** start REPL on a **GoodStuff** module by right-clicking **Scala REPL...**

### [Known issues](https://github.com/Aalto-LeTech/intellij-plugin/issues?q=is%3Aissue+label%3Auser-bug)

| #   | status    | description                                                                                        |
|-----|-----------|----------------------------------------------------------------------------------------------------|
| 1.  | **problem**   | [IDE complaining about not able to see the SDK (whitespaces and special characters in naming (path))](https://github.com/Aalto-LeTech/intellij-plugin/issues/360)|
|     | **status**    | solved                                                                                             |
|     | **solution**  | update to the latest plugin version                                                                |
| 2.  | **problem**   | continuously being asked to re-enter the A+ token                                                  |
|     | **status**    | not possible to solve due to the configuration of Aalto Linux on VDI                               |
|     | **solution**  | none                                                                                               |
| 3.  | **problem**   | keymap imported by the plugin interferes with macOS's normal keyboard shortcuts                    |
|     | **status**    | actively worked on                                                                                 |
|     | **solution**  | (temporary) use [this link](https://www.jetbrains.com/help/idea/configuring-keyboard-and-mouse-shortcuts.html) to the standard macOS keymap for IntelliJ IDEA    |
| 4.  | **problem**   | [network error about A+ token containing whitespaces](https://github.com/Aalto-LeTech/intellij-plugin/issues/377)                                                |
|     | **status**    | under investigation                                                                                |
|     | **solution**  | none                                                                                               |
| 5.  | **problem**   | [assignments are not visible on Aalto Linux (VDI)](https://github.com/Aalto-LeTech/intellij-plugin/issues/371)                                                   |
|     | **status**    | soon to be solved by Aalto IT                                                                      |
|     | **solution**  | it's an environment issue and will resolve by itself                                               |
| 6.  | **problem**   | can't run program because of the missing **Run** in right-click menu                               |
|     | **status**    | under investigation                                                                                |
|     | **solution**  | none                                                                                               |
| 7.  | **problem**   | [local course file missing language](https://github.com/Aalto-LeTech/intellij-plugin/issues/315)   |
|     | **status**    | under investigation                                                                                |
|     | **solution**  | none                                                                                               |