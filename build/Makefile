# The version in the file name for the ZIP is also in ../xproject-plugin/rsrc/plugin.xml
# TODO: Subsitute it automatically in plugin.xml...

# TODO: Must the the same in ../xproject-plugin/rsrc/plugin.xml as well !
VERSION=0.5.2

HELLO_DIR=../../../../expath/pkg/project/test/hello-world/

all:
	mkdir xproject/
	cp    ../xproject-plugin/dist/xproject-oxygen-plugin.jar xproject/
	cp    ../xproject-plugin/rsrc/plugin.xml xproject/
	cp -R ../xproject-plugin/lib/            xproject/lib/
	# Do NOT include oXygen's JAR file nor Log4J's...
	rm xproject/lib/oxygen.jar
	rm xproject/lib/log4j-1.2.17.jar
	cp -R ../xproject-plugin/src/            xproject/src/
	cp -R ../xproject-plugin/rsrc/icons/     xproject/icons/
	cp -R ../xproject-plugin/rsrc/templates/ xproject/templates/
	mkdir xproject/test/
	svn export $(HELLO_DIR) xproject/test/hello-world/
	mkdir xproject/repo/
	for pkg in ../xproject-plugin/rsrc/packages/*.xar; do \
	    xrepo --repo xproject/repo/ install $$pkg; \
	done
	zip -r -9 xproject-oxygen-plugin-$(VERSION).zip xproject/
