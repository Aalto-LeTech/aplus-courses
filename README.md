[![Build Status](https://travis-ci.com/Aalto-LeTech/intellij-plugin.svg?branch=test)](https://travis-ci.com/Aalto-LeTech/intellij-plugin)
[![Known Vulnerabilities](https://snyk.io/test/github/Aalto-LeTech/intellij-plugin/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/Aalto-LeTech/intellij-plugin)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FAalto-LeTech%2Fintellij-plugin.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FAalto-LeTech%2Fintellij-plugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Aalto-LeTech_intellij-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=Aalto-LeTech_intellij-plugin)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Aalto-LeTech_intellij-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=Aalto-LeTech_intellij-plugin)
[![JetBrains IntelliJ plugins](https://img.shields.io/jetbrains/plugin/d/13634-a-plugin-for-intellij?label=plugin%20downloads)](https://plugins.jetbrains.com/plugin/13634-a-plugin-for-intellij)

Intellij IDEA plugin for A+

## Styling

This project uses google checkstyle from [Checkstyle GitHub](https://github.com/checkstyle/checkstyle/blob/checkstyle-8.12/src/main/resources/google_checks.xml) for code formatting. The particular version applied to this project is stored at: `checkstyle/`. Please note, that the checkstyle file itself is licensed under **GNU LGPL** (stored at that same folder).

## Packaging

Due to a certain delay, required for JetBrains to verify the plugin and expose it via official plugin repository: [**Stable** channel](https://plugins.jetbrains.com/plugin/13634-a-courses/versions), 
the built artifact of the latest production-ready "A+ Courses" plugin is now additionally being stored at [GitHub Packages service](https://github.com/Aalto-LeTech/intellij-plugin/packages).