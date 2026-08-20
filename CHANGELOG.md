<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# A+ Courses Changelog

## [Unreleased]

## [4.5.0] - 2026-08-20

### Added

- Setup checklist in the Course tab that shows the progress of course setup and helps resolve missing steps
- Plugins required by the course are registered as required plugins of the project
- Downloads show their actual progress and can be canceled

### Changed

- Redesigned the Course tab with loading placeholders and a clearer points table
- The Course tab remembers the previous session, so it shows content while loading and offline
- More detailed progress messages when loading the course and submitting assignments
- Tool window tabs show icons instead of emoji
- The About section in the plugin settings is no longer a tab

### Fixed

- Module dependencies are no longer lost when a module is removed and later re-installed
- A failed or canceled submission no longer prevents submitting until the IDE is restarted
- A failed or canceled module install no longer leaves the module stuck at "Installing…"
- Network errors no longer clear the assignments list and the tool window
- Errors during submission are now reported instead of the submission appearing to succeed
- Fixed Scala SDK installation failing due to a broken sources download URL
- Removed unnecessary JARs from the Scala SDK compiler and REPL classpaths
- Fixed downloaded files failing to be moved into place on some systems
- The new project wizard no longer freezes while validating a configuration URL, shows why the course list is empty, and reliably downloads the selected JDK
- Grade display no longer breaks on courses with incomplete grading configuration

## [4.4.2] - 2026-06-25

### Fixed

- Fixed Scala 3.8 incompatibility

## [4.4.1] - 2025-12-04

### Fixed

- Fixed write violation error when updating a module
- Don't show manually graded submissions as background tasks
- Improved keyboard navigation in assignments tab

## [4.4.0] - 2025-08-19

### Added

- Support for IntelliJ 2025.2

### Fixed

- Issues with Scala SDK installation
- Issues with assignment submissions in SBT projects
- Problems when automatically installing plugins

## [4.3.0] - 2025-02-05

### Added

- Support for the Programming 2 course and SBT modules

### Fixed

- Do not submit backups of updated modules

## [4.2.0] - 2025-01-08

### Fixed

- Show the correct course name in downloaded modules
- Loading assignments for certain courses

## [4.1.2] - 2024-11-27

### Fixed

- File separators of exported modules

## [4.1.1] - 2024-11-13

### Fixed

- Updated to version 2024.3 of IntelliJ.
- Small bug fixes.

## [4.1.0] - 2024-10-30

### Added

- Module exporting: Students can now export modules for submission.
- Module importing: Users can import multiple module zip files simultaneously. This feature helps assistants in grading
  and allows students to test their zips.

### Changed

- Hid point counter from feedback assignments.
- Removed submission IDs from the assignment tree.

### Fixed

- Improved enrollment detection.
- Added token invalidation detection: The plugin now prompts users to re-enter their token if it is invalid (i.e., a new
  one was generated, or it got revoked).

## [4.0.3] - 2024-09-04

### Fixed

- Increased network timeout to prevent module downloads from stopping on slower connections

## [4.0.2] - 2024-08-28

### Fixed

- Resolved issues for external users and importing IDE settings.

## [4.0.1] - 2024-08-26

### Changed

- Minor UI improvements.

## [4.0.0] - 2024-08-23

### Changed

- Complete UI overhaul
- Rewrite of the plugin from Java to Kotlin

[Unreleased]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.5.0...HEAD
[4.5.0]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.4.2...v4.5.0
[4.4.2]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.4.1...v4.4.2
[4.4.1]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.4.0...v4.4.1
[4.4.0]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.3.0...v4.4.0
[4.3.0]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.2.0...v4.3.0
[4.2.0]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.1.2...v4.2.0
[4.1.2]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.1.1...v4.1.2
[4.1.1]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.1.0...v4.1.1
[4.1.0]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.0.3...v4.1.0
[4.0.3]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.0.2...v4.0.3
[4.0.2]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.0.1...v4.0.2
[4.0.1]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.0.0...v4.0.1
[4.0.0]: https://github.com/Aalto-LeTech/aplus-courses/commits/v4.0.0
