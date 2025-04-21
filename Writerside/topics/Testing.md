# Testing

## Setting up the testing environment

You can use Sandbox or create a zip file of the plugin for testing.

### Sandbox

Sandbox is a good tool for testing on the go during development.

* Start a sandbox instance from Gradle using `runIde`.

### Testing with a zip

Testing with zip creates a more authentic testing environment. Creating a Zip:

1. From Gradle run `buildPlugin`. This builds and creates a Zip file for testing and deployment.
2. Fetch the created file from `build/distributions`.
3. Optional: Install a new IntelliJ IDEA.
4. Install the zip in IntelliJ IDEA from `Settings > Plugins >  > Install Plugin from Disk...`.

## Testing main features

### Creating a new A+ courses project

1. Create a new project `File > New > Project...`.
2. Select **A+ courses** from the project type list on the left of the window.
3. Enter file configuration URL:
    ```
    xxx
    ```
4. Click `Next`.
5. Enable the `Leave IntelliJ settings unchanged` checkbox.
6. Under `Additional Configurations`, from the JDK dropdown box, choose `temurin-21` (if it doesn't appear in the list,
   click `Download JDK...` and choose version **21** from **Eclipse Temurin**).
7. Click `Next`.
8. Usually there is no need to change the default project name and location, just click `Create`.
9. If the IntelliJ Scala plugin was not installed already, it will be installed now and may require a restart of
   IntelliJ. In this case you will see a dialog box asking for a restart. Click `Restart` to finish project creation.

### Downloading a module

1. From A+ courses click the `Modules` tab.
2. From the list of modules, select module `GoodStuff`.
3. Click `Install`.
4. After installation, check that the module `GoodStuff` shows up in the project tree of IntelliJ IDEA.
5. In the A+ courses `Modules` tab, the installed module `GoodStuff` should now be under the `Installed Modules` list.

### Submit an exercise and reading the feedback

1.

## Test course

Contains three different modules:

* Normal?
* SBT
* Python



