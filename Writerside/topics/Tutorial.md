# Course configuration

To utilize the A+ Courses plugin, each course needs its own course configuration file.
This page will explain the structure and properties of the course configuration file.

## Before you start

For teachers:

## Example

Example configuration file:

```JSON

{
  "id": "197",
  "name": "O1",
  "aPlusUrl": "https://plus.cs.aalto.fi/",
  "languages": [
    "fi",
    "en"
  ],
  "resources": {
    "ideSettings": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_ij_settings.zip",
    "ideSettingsMac": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_ij_mac_settings.zip",
    "projectSettings": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_project_settings.zip"
  },
  "vmOptions": {
    "file.encoding": "UTF-8"
  },
  "autoInstall": [
    "O1Library"
  ],
  "repl": {
    "initialCommands": {
      "Adventure": [
        "import o1.adventure._"
      ],
      "AdventureDraft": [
        "import o1.adventure.draft._"
      ],
      "Aliohjelmia": [
        "import o1._",
        "import o1.aliohjelmia._"
      ]
    }
  },
  "modules": [
    {
      "name": "Adventure",
      "url": "https://grader.cs.aalto.fi/static/O1_2021/projects/given/Adventure/Adventure.zip",
      "version": "1.0",
      "changelog": ""
    },
    {
      "name": "AdventureDraft",
      "url": "https://grader.cs.aalto.fi/static/O1_2021/projects/given/AdventureDraft/AdventureDraft.zip",
      "version": "1.0",
      "changelog": ""
    },
    {
      "name": "Aliohjelmia",
      "url": "https://grader.cs.aalto.fi/static/O1_2021/projects/given/Aliohjelmia/Aliohjelmia.zip",
      "version": "1.0",
      "changelog": ""
    }
  ],
  "exerciseModules": {
    "31353": {
      "en": "Subprograms",
      "fi": "Aliohjelmia"
    },
    "31383": {
      "en": "IntroOOP",
      "fi": "Oliointro"
    },
    "31427": {
      "en": "Ave",
      "fi": "Ave"
    }
  }
}
```

Describe what the user will learn and accomplish in the first part,
then write a step-by-step procedure but on a real-world example.

1. Execute the following command in the terminal:

   ```bash
    run this --that
   ```

2. Step with a [link](https://www.jetbrains.com)

3. Final step in part 1.

## Part 1: Basic information

Description of the basic information properties of the course:

* `@property id`: ID for the course from the A+ API.
* `@property name`: Name of the course that gets shown in the UI.
* `@property aPlusUrl`: URL for A+.
* `@property languages`: An array of the languages for the assignments. Different languages may use different modules.
* `@property version`: Minimum version of the plugin required to use the course in the format major.minor.

## Part 2: Resources

## Part 3: REPL

* `@property initialCommands`: A map from a module name to an array of commands that get run when opening the REPL for
  the given module.
* `@property arguments`: Arguments for the Scala compiler of the REPL.

## Part 4: Modules

```JSON
{
  "modules": [
    {
      "name": "Adventure",
      "language": "en",
      "url": "https://grader.cs.aalto.fi/static/O1_2021/projects/given/Adventure/Adventure.zip",
      "version": "2.3",
      "changelog": "Fixed game crashing on some inputs."
    }
  ]
}
```

In the configuration file all modules are listed under `@property model` as a list. For each module the
configuration file has the following information:

* `@property name`: Name for the module shown in the UI.
* `@property url`: URL to a .zip file containing skeleton code, that the plugin downloads.
* `@property language`: Optional language of the module. If provided, only shown for that language in the UI.
* `@property version`: Version number. Increment the major number when making breaking changes, else the
  minor number. Format of the version number is `major.minor`.
* `@property changelog`: Optional changelog, that gets shown as a tooltip in the module list. Use empty string or leave
  out if you don't want a changelog.

## Part 5: Exercise modules

## Update configuration list

When the configuration file is ready, add the course to
the [Course Configuration repository](https://version.aalto.fi/gitlab/aplus-courses/course-config-urls). This is done by
updating the file `courses.yaml`. This file should contain the course's name, semester, language and url to the
configuration file.

## Summary {id="tutorial-summary"}

Summarize what the reader achieved by completing this tutorial.

<seealso>
<!--Give some related links to how-to articles-->
</seealso>
