# Course configuration file

To utilize the A+ Courses plugin, each course requires its own course configuration file. This page explains the
structure and key properties of that file.

## Before you start

Ensure you have access to
the [Course Configuration repository](https://version.aalto.fi/gitlab/aplus-courses/course-config-urls). Access is
granted by ... team. Once the configuration file is complete and hosted, it should be added to the repository. See the
section on the [course configuration repository](#config_repo) for more information.

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

## Basic information

Description of the basic information properties of the course:

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
  ]
}
```

* `id: String` ID for the course from the A+ API.

```
"id": "197"
```

* `name: String` Name of the course that gets shown in the UI.

```
"name": "O1"
```

* `aPlusUrl: String` URL for A+.

```
  "aPlusUrl": "https://plus.cs.aalto.fi/",

```

* `languages: Array[String]` An array of the languages for the assignments. Different languages may use different
  modules.

```
  "languages": [
    "fi",
    "en"
  ]
```

* `version`: Minimum version of the plugin required to use the course in the format major.minor.

* `resources Map[String, String]` : URLs for some resources the plugin uses:

    * `ideSettings` A .zip file, containing settings for IntelliJ that the user may optionally install while turning
      their project into an A+ project.
    * `ideSettingsMac` Same as before, but for macOS.
    * `projectSettings` A .zip file, containing settings for the project the course is installed in, such as inspection
      profiles and code styles.
    * `customProperties` A .properties file, that overwrites the UI texts for the plugin. The default file may be
      found [here](https://github.com/Aalto-LeTech/aplus-courses/blob/main/src/main/resources/messages/resources.properties)

```
  "resources": {
    "ideSettings": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_ij_settings.zip",
    "ideSettingsMac": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_ij_mac_settings.zip",
    "projectSettings": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_project_settings.zip"
  }
```

* `vmOptions: Map[String, String]` A map of options for the JVM.

```
  "vmOptions": {
    "file.encoding": "UTF-8"
  }
```

* `autoInstall: Array[String]` An array of the modules that get installed automatically when the project gets turned
  into an A+ project.

```
  "autoInstall": ["O1Library"]
```

## Scala REPL

If the course is not taught using Scala, this part is left out from the configuration file and you can skip this
section.

```JSON
{
  "repl": {
    "initialCommands": {
      "Adventure": [
        "import o1.adventure._"
      ]
    },
    "arguments": "-new-syntax -feature -deprecation -explain-types"
  }
}
```

* `initialCommands: Map[String, Array[String]]` A map from a module name to an array of commands that get run when
  opening the REPL for the given module.

```
  "initialCommands": {
    "Adventure": [
      "import o1.adventure._"
    ]
    }
```

* `arguments: String` Arguments for the Scala compiler of the REPL.

```
    "arguments": "-new-syntax -feature -deprecation -explain-types"

```

## Modules

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

In the configuration file all modules are listed under `modules` as a list. For each module the
configuration file has the following information:

* `name: String` Name for the module shown in the UI.

```
"name": "Adventure"
```

* `language: String` Optional language of the module. If provided, only shown for that language in the UI.

```
"language": "en"
```

* `url: String` URL to a .zip file containing skeleton code, that the plugin downloads.

```
"url": "https://grader.cs.aalto.fi/static/O1_2021/projects/given/Adventure/Adventure.zip"
```

* `version: String` Version number. Increment the major number when making breaking changes, else the
  minor number. Format of the version number is `major.minor`.

```
"version": "2.3"
```

* `changelog: String` Optional changelog, that gets shown as a tooltip in the module list. Use empty string or leave
  out if you don't want a changelog.

```
"changelog": "Fixed game crashing on some inputs."
```

## Exercise modules

```JSON
 {
  "exerciseModules": {
    "31353": {
      "en": "Subprograms",
      "fi": "Aliohjelmia"
    }
  }
}

```

* `exerciseModules: Map[String, Map[String, String]]` Information, about which module each assignment uses. The
  assignment ID is used as a key to a map,
  where language codes are used as keys for the module names.

## Configuration repository {id="config_repo"}

When the configuration file is ready, add the course to
the [Course Configuration repository](https://version.aalto.fi/gitlab/aplus-courses/course-config-urls). This is done by
updating the file `courses.yaml`.

Example format:

``` yaml
- name: O1 2024
  semester: Autumn 2024
  language: scala
  url: https://gitmanager.cs.aalto.fi/static/O1_2024/modules/o1_course_config.json
```

* `name`: Name of the course. When creating a new project via the plugin, this is the name shown in the course
  selection list.
* `semester`: Semester in which the course is held.
* `language`: Programming language taught in the course.
* `url`: Link to the location of the course configuration file.

After the course is added to the configuration repository, it should show up on the course selection list when creating
a new project in IntelliJ using the A+ courses plugin.

<img src="new-project.png" alt="Course selection list in IntelliJ" width="600"/>

