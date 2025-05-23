<p align="center">
   <a href="https://plugins.jetbrains.com/plugin/13634-a-courses" style="box-shadow: 5px 5px 15px 1px black;">
      <img src="images/courses-banner.webp" alt="A+ Courses is a plugin for IntelliJ IDEA, used in programming courses at Aalto University" >
   </a>
</p>

<div align="center">

  <a href="https://github.com/Aalto-LeTech/aplus-courses/actions?query=workflow%3Abuild">![Build](https://github.com/Aalto-LeTech/aplus-courses/actions/workflows/push.yml/badge.svg?branch=main)</a>
  <a href="https://plugins.jetbrains.com/plugin/13634-a-plugin-for-intellij">![Plugin Downloads](https://img.shields.io/jetbrains/plugin/d/13634-a-plugin-for-intellij?label=plugin%20downloads)</a>
  <a href="https://plugins.jetbrains.com/plugin/13634-a-plugin-for-intellij">![Plugin Version](https://img.shields.io/jetbrains/plugin/v/13634?label=plugin%20version)</a>

</div>

<div align="center">
   <a href="https://plugins.jetbrains.com/plugin/13634-a-courses">
      <img width="245px" src="images/marketplace.webp" alt="Get from Marketplace">
   </a>
</div>

---

<img width="300px" align="right" src="images/image_4_readme.webp" alt="Screenshot">

<!-- Plugin description -->
This plugin supports the educational use of IntelliJ IDEA in programming courses that rely on the A+ LMS developed at
Aalto University.
The plugin accesses programming assignments and automated grading services provided by A+ and otherwise enhances the
student experience.

Keywords: aplus courses, a plus
<!-- Plugin description end -->

This repository hosts the source code for the A+ Courses [IntelliJ IDEA](https://www.jetbrains.com/idea/) plugin
communicating with the [A+ LMS](https://apluslms.github.io/). It allows users to download code modules, submit
assignments, and use the Scala REPL more conveniently among other learning experience improvements. The plugin is
currently used in introductory programming courses taught at Aalto
University ([Programming 1](https://plus.cs.aalto.fi/o1)
and [Programming Studio 2/A](https://oodi.aalto.fi/a/opintjakstied.jsp?OpinKohd=1125591784&haettuOpas=-1&Kieli=6)).

The project is developed under the [Aalto LeTech research group](https://research.cs.aalto.fi/LeTech/) and is steered
by **[Juha Sorva](https://github.com/jsorva)** and **[Otto Seppälä.](https://github.com/oseppala)**

## Features and Further Development

Most of the available features are described in
the [testing manual,](https://github.com/Aalto-LeTech/aplus-courses/blob/master/TESTING.md) which is also used
internally for testing. There's also a basic video presentation of the A+ Courses plugins functionality
stored [here.](https://aalto.cloud.panopto.eu/Panopto/Pages/Viewer.aspx?id=42740f68-8dd8-4ba2-8f1c-acb1007bf8ef)

The roadmap and plans for the future development of the plugin are in
the [project wiki.](https://github.com/Aalto-LeTech/aplus-courses/wiki/Requirements) The list of requirements is in no
way final and we [welcome all input and ideas](https://github.com/Aalto-LeTech/aplus-courses/issues/new/choose) on how
to make this plugin better.

## Maintenance

If you are a student in a course and you have discovered an issue you'd like to report, please turn to the teaching
assistants. You can also create an issue directly
here: [Aalto-LeTech/aplus-courses/issues.](https://github.com/Aalto-LeTech/aplus-courses/issues)

Once the bug report is made, the development team (**[@jaakkonakaza,](https://github.com/jaakkonakaza)**
**[@OlliKiljunen,](https://github.com/OlliKiljunen)**
**[@Taikelenn](https://github.com/Taikelenn)**) will handle it on a **best-effort basis:**

1. The issue will be confirmed and prioritized within **two working days**.
2. An estimation of when the issue could be fixed is made within **three working days**.
3. Once the issue is fixed, it will take at most two working days until the fix is publicly available in
   the [JetBrains plugin repository,](https://plugins.jetbrains.com/plugin/13634-a-courses) and, eventually, your IDE.

Medium- to high-priority issues are usually solved within **one working-week**.

List of courses is available at https://version.aalto.fi/gitlab/aplus-courses/course-config-urls.

## Research

Academic research related to or created based on the A+ Courses plugin.


<div align="center">

  <a href="https://aaltodoc.aalto.fi/handle/123456789/102499"><img 
    alt="'Creating an educational plugin to support online programming learning A case of IntelliJ IDEA plugin for A+ Learning Management System' Master's thesis by Nikolai Denissov" 
    width="375"
    src="images/thesis-nikolai.svg" ></a>
  <a href="https://aaltodoc.aalto.fi/handle/123456789/107623"><img 
    alt="'Plug-in Interoperation in Extendable Platforms' Master's thesis by Olli Kiljunen"
    width="375"
    src="images/thesis-olli.svg" ></a>
   
</div>



## Code Style

This project uses slightly modified google checkstyle rules
from [Checkstyle GitHub](https://github.com/checkstyle/checkstyle/blob/checkstyle-8.12/src/main/resources/google_checks.xml)
for code formatting. The particular version applied to this project is in the `checkstyle` directory. Please note that
the checkstyle file itself is licensed under the **GNU LGPL** license (also in the directory). Scala code is checked
using
the [default rules from the scalastyle repository.](https://github.com/scalastyle/scalastyle/blob/master/src/main/resources/default_config.xml)
The configuration file is located in the `scalastyle` directory and it is licensed under the **Apache-2.0** license.

## Code of Conduct

The team follows general principles of Aalto
University's [code of conduct.](https://www.aalto.fi/sites/g/files/flghsv161/files/2018-09/aalto_university_code_of_conduct_en-003.pdf)

## Credits

We would like to
acknowledge **[Nikolas Drosdek,](https://github.com/nikke234)** **[@superseacat,](https://github.com/superseacat)** *
*[Styliani Tsovou,](https://github.com/stellatsv)** **[@valtonv2,](https://github.com/valtonv2)** *
*[@xiaoxiaobt,](https://github.com/xiaoxiaobt)**
**Ida Iskala,** and **[@StanislavFranko](https://github.com/StanislavFranko)** for their help in testing and improving
this project.

<p align="center">
   <a href="https://github.com/Aalto-LeTech/aplus-courses">
      <img width="1500px" src="images/footer.png" alt="A+ Courses">
   </a>
</p>
