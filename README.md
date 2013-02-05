# XProject oXygen Plugin

This project provides a plugin for the [oXygen XML](http://oxygenxml.com/) IDE,
to support [XProject](http://expath.org/modules/xproject/), the XML project
manager from [EXPath](http://expath.org/).

Documentation can be found at http://expath.org/modules/xproject/oxygen.


## What’s it do?

The plugin provides 4 buttons in the toolbar: 

- **build** - generates the XAR file
- **test** - runs test suites and generate reports
- **document** - generates documentation from source code
- **release** - builds a ZIP archive containing the XAR, a README, the
  source, etc., ready to use

The buttons can be used while editing any file within the project
directory.


## Download

Download the release file from the [download
area](http://code.google.com/p/expath-pkg/downloads).  Look for the
latest file named `xproject-oxygen-plugin-x.y.z.zip`.


## Installation

- Create a new directory called `xproject` in `{your oXygen dir}/plugins` 
- Unzip the release file to `{your oXygen dir}/plugins/xproject`

The plugin directory will contain:

- `plugin.xml` (from `xproject-plugin/rsrc/plugin.xml`)
- `xproject-oxygen-plugin.jar` (from `xproject-plugin/dist/xproject-oxygen-plugin.jar`)
- `repo/` (from `xproject-plugin/rsrc/repo/`)
- `lib/` (from `xproject-plugin/lib/`)


## Developing this project

Technically, `xproject-plugin/` is a NetBeans project.  You’ll need to
add two libraries to NetBeans (in "Tools ► Libraries"): `oxygen` and
`apache-log4j`.  The `oxygen` lib must contain the JAR file from the
oXygen SDK.
