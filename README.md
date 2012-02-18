This project provides a plugin for the [oXygen XML](http://oxygenxml.com/) IDE,
to support [XProject](http://expath.org/modules/xproject/), the XML project
manager from [EXPath](http://expath.org/).

Basically, the plugin provides for now 4 buttons in the toolbar, to
respectively **build** the project (that is, generate the XAR file), **test**
it (run the test suites and generate reports), **document** it (generate
documentation out of source files, like Javadoc does), and **release** it
(build a ZIP file containing the XAR, a README, the sources, etc., ready for
shipping).  Those button can be used while editing any file within the project
directory (that is, any file which is a descendent of the project dir).

To install it, unzip the release file in your oXygen plugins directory (the
sub-directory named `plugins/` in your main oXygen installation dir) in a new
subdir called `plugins/xproject/`.

FYI this plugin dir will contain:

- `plugin.xml` (from `xproject-plugin/rsrc/plugin.xml`)
- `xproject-oxygen-plugin.jar` (from `xproject-plugin/dist/xproject-oxygen-plugin.jar`)
- `repo/` (from `xproject-plugin/rsrc/repo/`)
- `lib/` (from `xproject-plugin/lib/`)

Technically, `xproject-plugin/` is a NetBeans project.  You need to have both
Java libraries `oxygen` and `apache-log4j` defined in you NetBeans (in _Tools_
> _Libraries_).  The lib `oxygen` must contain the JAR file from the oXygen
SDK.
