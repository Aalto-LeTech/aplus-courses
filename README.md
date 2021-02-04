# ![A+ Courses logo](images/logo_courses_border.svg)

[![Build](https://github.com/Aalto-LeTech/intellij-plugin/workflows/build/badge.svg)](https://github.com/Aalto-LeTech/intellij-plugin/actions?query=workflow%3Abuild)
[![Known Vulnerabilities](https://snyk.io/test/github/Aalto-LeTech/intellij-plugin/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/Aalto-LeTech/intellij-plugin)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FAalto-LeTech%2Fintellij-plugin.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FAalto-LeTech%2Fintellij-plugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Aalto-LeTech_intellij-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=Aalto-LeTech_intellij-plugin)
[![JetBrains IntelliJ plugins](https://img.shields.io/jetbrains/plugin/d/13634-a-plugin-for-intellij?label=plugin%20downloads)](https://plugins.jetbrains.com/plugin/13634-a-plugin-for-intellij)

This repository hosts the code for the A+ Courses [IntelliJ IDEA](https://www.jetbrains.com/idea/) plugin communicating with the [A+ learning management system](https://apluslms.github.io/). It allows users to download code modules, submit assignments, and use the Scala REPL more conveniently among other learning experience improvements. The plugin is currently used in introductory programming courses taught at Aalto University.

The project is developed under the [Aalto Le-Tech research group](https://research.cs.aalto.fi/LeTech/) and is steered by **[Juha Sorva](https://github.com/jsorva)** and **[Otto Seppälä](https://github.com/oseppala)**.

## Features and Further Development

Most of the available features are described in the [testing manual](https://github.com/Aalto-LeTech/intellij-plugin/blob/master/TESTING.md), which is also used internally for testing. The roadmap and plans for the future development of the plugin are in the [project wiki](https://github.com/Aalto-LeTech/intellij-plugin/wiki/Requirements). The list of requirements is in no way final and we [welcome all input and ideas](https://github.com/Aalto-LeTech/intellij-plugin/issues/new/choose) on how to make this plugin better.

## Maintenance

If you are a student in a course and you have discovered an issue you'd like to report, please turn to the teaching assistants. You can also create an issue directly here: [Aalto-LeTech/intellij-plugin/issues](https://github.com/Aalto-LeTech/intellij-plugin/issues).

Once the bug report is made, the development team (**[@OlliKiljunen](https://github.com/OlliKiljunen)**, **[@nikke234](https://github.com/nikke234)**, **[@superseacat](https://github.com/superseacat)**) will handle it on a **best-effort basis:**

1. The issue will be confirmed and prioritized within **two working days**.
2. An estimation of when the issue could be fixed is made within **three working days**.
3. Once the issue is fixed, it will take at most two working days until the fix is publicly available in the [JetBrains plugin repository](https://plugins.jetbrains.com/plugin/13634-a-courses), 
and, eventually, your IDE.

Medium- to high-priority issues are usually solved within **one working-week**.

## Code Style

This project uses slightly modified google checkstyle rules from [Checkstyle GitHub](https://github.com/checkstyle/checkstyle/blob/checkstyle-8.12/src/main/resources/google_checks.xml) for code formatting. The particular version applied to this project is in the  `checkstyle` directory. Please note that the checkstyle file itself is licensed under the **GNU LGPL** license (also in the directory). Scala code is checked using the [default rules from the scalastyle repository](https://github.com/scalastyle/scalastyle/blob/master/src/main/resources/default_config.xml). The configuration file is located in the `scalastyle` directory and it is licensed under the **Apache-2.0** license.  

## Code of Conduct

The team follows Aalto University's general [code of conduct](https://www.aalto.fi/en/aalto-university/code-of-conduct) principles.

## Credits

We would like to acknowledge **[@valtonv2](https://github.com/valtonv2)**, **[@xiaoxiaobt](https://github.com/xiaoxiaobt)**, **Ida Iskala**, and **[@Atlanex](https://github.com/Atlanex)** for their help in testing and improving this project.
