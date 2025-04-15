# Architecture

This page provides an overview of the code architecture for the A+ Courses Plugin, describing the purpose and contents
of the main source code directories.

## Actions

Defines classes that handle behavior triggered by different user or system events.
Actions are organized into three groups:

* `EXERCISE_ACTIONS`
* `MODULE_ACTIONS`
* `TOOL_WINDOW_ACTIONS`

Each group handles a specific context in which the actions are used.

## Activities

Contains the `InitializationActivity` class, which is responsible for setting up a project when it's created using the
A+ Courses plugin.

## API

Class `APlusApi` for fetching information using the A+ api. Also contains the `CourseConfig` object that serves as the
serializer and documentation for the course configuration file. More information on the course configuration
file can be found <a href="Configuration.md">here</a>.

## Config

Contains the class `APlusConfigurable` that provides a UI component to display plugin settings and methods to change
and save settings. In addition, it also shows a short description of the plugin.

## Generator

Implements the logic for project creation and structure configuration.
Important components:

* `CourseSelectStep`
* `CourseSettingsStep`

These two classes extend the IntelliJ project creation wizard to support custom steps for A+ course setup.

## Icons

Contains UI icons used throughout the plugin, bundled within the `CourseIcons` object.

## Model

Defines the data models used by the plugin to represent for example exercises, people and other related entities.

## Notifications

Contains classes for user notifications that may appear during use, such as status messages or alerts during plugin use.

## Services

## Toolwindows

Includes components related to IntelliJ's tool window system.

Key class:

* `APlusToolWindowFactory`: defines how the plugin is presented in IntelliJ's tool window panel, when it is expanded by
  the user.

## UI

Contains the user interface components of the plugin, including dialogs, views and other visual elements.

## Utils

Provides utility classes used across the plugin, such as:

* Parsers
* Callbacks

---
