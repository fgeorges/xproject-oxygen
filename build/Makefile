# The version in the file name for the ZIP is also in ../xproject-plugin/rsrc/plugin.xml

all:
	mkdir xproject/
	cp ../xproject-plugin/dist/xproject-oxygen-plugin.jar xproject/
	cp ../xproject-plugin/rsrc/plugin.xml xproject/
	cp -R ../xproject-plugin/rsrc/repo/ xproject/repo/
	cp -R ../xproject-plugin/lib/ xproject/lib/
	cp -R ../xproject-plugin/src/ xproject/src/
	mkdir xproject/test/
	svn export ../../../../expath/pkg/project/test/hello-world/ xproject/test/hello-world/
	zip -r -9 xproject-oxygen-plugin-0.3.0.zip xproject/
