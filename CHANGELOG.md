<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# A+ Courses Changelog

## [Unreleased]

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

[Unreleased]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.3.0...HEAD
[4.3.0]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.2.0...v4.3.0
[4.2.0]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.1.2...v4.2.0
[4.1.2]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.1.1...v4.1.2
[4.1.1]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.1.0...v4.1.1
[4.1.0]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.0.3...v4.1.0
[4.0.3]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.0.2...v4.0.3
[4.0.2]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.0.1...v4.0.2
[4.0.1]: https://github.com/Aalto-LeTech/aplus-courses/compare/v4.0.0...v4.0.1
[4.0.0]: https://github.com/Aalto-LeTech/aplus-courses/commits/v4.0.0
