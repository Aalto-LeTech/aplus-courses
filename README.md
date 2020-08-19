[![Build Status](https://travis-ci.com/Aalto-LeTech/intellij-plugin.svg?branch=test)](https://travis-ci.com/Aalto-LeTech/intellij-plugin)
[![Known Vulnerabilities](https://snyk.io/test/github/Aalto-LeTech/intellij-plugin/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/Aalto-LeTech/intellij-plugin)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FAalto-LeTech%2Fintellij-plugin.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FAalto-LeTech%2Fintellij-plugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Aalto-LeTech_intellij-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=Aalto-LeTech_intellij-plugin)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Aalto-LeTech_intellij-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=Aalto-LeTech_intellij-plugin)
[![JetBrains IntelliJ plugins](https://img.shields.io/jetbrains/plugin/d/13634-a-plugin-for-intellij?label=plugin%20downloads)](https://plugins.jetbrains.com/plugin/13634-a-plugin-for-intellij)

# ![Small_Logo](images/small_logo.png) IntelliJ IDEA plugin for A+ Courses

## Welcome

This repository hosts the code for [IntelliJ IDEA](https://www.jetbrains.com/idea/) plugin operating with [A+ LMS](https://apluslms.github.io/). It allows users to retrieve, submit and manipulate assignments for introductory programming courses taught at Aalto Univerity Computer Science Faculty (like "Programming 1").

The project is developed under [Aalto Le-Tech research group](https://research.cs.aalto.fi/LeTech/) and is steered by **[@jsorva](https://github.com/jsorva)** and **[@oseppala](https://github.com/oseppala)**.

## Maintenance

If you have discovered an issue you'd like to report, please turn to [Teaching Assistants (TA)](https://plus.cs.aalto.fi/o1/2020/w01/ch01/#course-staff), so they would handle. For (head) TAs, please create issues directly here: [Aalto-LeTech/intellij-plugin/issues](https://github.com/Aalto-LeTech/intellij-plugin/issues).

Once the issue (bug report) is made, the development team (**[@OlliKiljunen](https://github.com/OlliKiljunen)**, **[@nikke234](https://github.com/nikke234)**, **[@superseacat](https://github.com/superseacat)**) will handle it on a **best-effort basis:**

1. in **48h** the issue will be labeled and triaged;
2. in **72h** the estimate when the issue could be fixed is made;
3. in **72h + XXh** the issue is fixed;
4. in **120h + XXh** the fix is available at [A+ Courses plugin repo](https://plugins.jetbrains.com/plugin/13634-a-courses), 
and, eventually, your IDE :wink:;

Ideally, the issues are **solved within one working-week time** for ones marked as "high" and "medium".

## Styling

This project uses slightly modified google checkstyle rules from [Checkstyle GitHub](https://github.com/checkstyle/checkstyle/blob/checkstyle-8.12/src/main/resources/google_checks.xml) for code formatting. The particular version applied to this project is stored at: `checkstyle/`. Please note, that the checkstyle file itself is licensed under **GNU LGPL** (stored at that same folder). Scala code is checked using the [default rules from the scalastyle repository](https://github.com/scalastyle/scalastyle/blob/master/src/main/resources/default_config.xml). The configuration file is located in `scalastyle` and it is licensed under the **Apache-2.0** license.  

## Credits

We would like to acknowledge **[@valtonv2](https://github.com/valtonv2)**, **[@xiaoxiaobt](https://github.com/xiaoxiaobt)** and **Ida Iskala** for their help in making this project better, more durable.