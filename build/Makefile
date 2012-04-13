# The version in the file name for the ZIP is also in ../xproject-plugin/rsrc/plugin.xml
# TODO: Subsitute it automatically in plugin.xml...

all:
	mkdir xproject/
	cp    ../xproject-plugin/dist/xproject-oxygen-plugin.jar xproject/
	cp    ../xproject-plugin/rsrc/plugin.xml xproject/
	cp -R ../xproject-plugin/lib/            xproject/lib/
	cp -R ../xproject-plugin/src/            xproject/src/
	cp -R ../xproject-plugin/rsrc/icons/     xproject/icons/
	cp -R ../xproject-plugin/rsrc/templates/ xproject/templates/
	mkdir xproject/test/
	svn export ../../../../expath/pkg/project/test/hello-world/ xproject/test/hello-world/
	mkdir xproject/repo/
	for pkg in ../xproject-plugin/rsrc/packages/*.xar; do \
	    xrepo --repo xproject/repo/ install $$pkg; \
	done
	zip -r -9 xproject-oxygen-plugin-0.3.0.zip xproject/
