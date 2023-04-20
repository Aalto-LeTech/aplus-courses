# Local mock server for easy tutorial testing

## Requirements

- **Python 3.4+** with **pip**

## Make your configuration

Make two directories in the same directory where **mock_api.py** is located: one
named **tutorials** and one named **modules**. Add whatever tutorials and
modules you want in them. Modules should be ZIP files (e.g. `GoodStuff.zip`) and
tutorials should be XML files with integer names (e.g. `111.xml`).
The directory structure could now look like this:

```
aplus-courses
`--tools
   `--mockserver
      |--modules
      |  |--GoodStuff.zip
      |  `--O1Library.zip
      |--tutorials
      |  |--111.xml
      |  `--112.xml
      |--README.md
      `--mock_api.py
```

## Start a mock server

If you haven't installed Flask, do it with

```sh
pip3 install flask
```

Open terminal in the directory containing **mock_api.py** and execute

```sh
python3 mock_api.py
```

This starts a simple HTTP server on port 5000. If you navigate to
http://localhost:5000/, you shoud see a message confirming that the server is
running. This server mocks the functionalities of A+ server as well as static
file servers (e.g. gitmanager) used by A+ Courses plugin.

## Initialize the project

Start IntelliJ IDEA with A+ Courses plugin.

Create a new **A+ Project** and set the project SDK (e.g. JDK 11).
Turn the project into A+ project using A+ menu. In the dialog that opens, choose
none of the courses on the list but, instead, type the following in the
**Configuration URL** field:

```
http://localhost:5000/config/
```

Choose either of the languages and click OK. Next, set A+ token to whatever. You
should now see the module list. Double click one of the modules to install it.
Wait some seconds for the indexing to be ready.

## Run the tutorial

Select an assignment from the assignment tree and click the play button above
the assignment tree to start the tutorial. Go through the steps to finish the
tutorial.
