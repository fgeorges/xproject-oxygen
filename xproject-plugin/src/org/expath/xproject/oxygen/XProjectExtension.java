/****************************************************************************/
/*  File:       XProjectExtension.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2011-01-23                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2011 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.xproject.oxygen;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.border.EmptyBorder;
import org.apache.log4j.Logger;
import org.expath.xproject.oxygen.XProjectConstants.ProjectPhase;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
import ro.sync.util.editorvars.EditorVariables;

/**
 * Support for XProject build tools in oXygen.
 *
 * @author Florent Georges
 * @date   2011-01-23
 */
public class XProjectExtension
        implements WorkspaceAccessPluginExtension
{
    @Override
    public void applicationStarted(StandalonePluginWorkspace ws)
    {
        // the workspace object
        myWorkspace = ws;
        // the way to give user some feedback
        myMsg = new UserMessages(ws);
        // the install plugin dir and its icons/ subdir (TODO: check for errors?)
        String install_str = myWorkspace.getUtilAccess().expandEditorVariables(EditorVariables.OXYGEN_INSTALL_DIR, null);
        File install = new File(install_str);
        File plugins = new File(install, "plugins/");
        myPluginDir  = new File(plugins, "xproject/");
        myIconsDir   = new File(myPluginDir, "icons/");
        // the toolbar customizer, to inject the XProject toolbar
        ToolbarComponentsCustomizer cust = new MyToolbarCustomizer();
        ws.addToolbarComponentsCustomizer(cust);
    }

    @Override
    public boolean applicationClosing()
    {
        // nothing
        return true;
    }

    private File getCurrentEditedFile()
            throws XProjectException
    {
        WSEditor editor = myWorkspace.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
        if ( editor == null ) {
            throw new XProjectException("There is no opened editor.");
        }
        URL location = editor.getEditorLocation();
        String scheme = location.getProtocol();
        if ( ! "file".equals(scheme) ) {
            throw new XProjectException("The open document URI has not a 'file:' scheme: " + scheme);
        }
        URI uri;
        try {
            uri = location.toURI();
        }
        catch ( URISyntaxException ex ) {
            throw new XProjectException("Not well-formed URI: " + location, ex);
        }
        return new File(uri);
    }

    private XProject getCurrentXProject()
            throws XProjectException
    {
        File file = getCurrentEditedFile();
        File project = getProjectDir(file);
        if ( project == null ) {
            throw new XProjectException("The edited file is not part of an EXPath project.");
        }
        JavaProcessFactory factory = new JavaProcessFactory(myWorkspace);
        return new XProject(project, myMsg, factory, myPluginDir);
    }

    private File getProjectDir(File file)
    {
        if ( ! file.exists() ) {
            myMsg.error(LOG, "File does not exist: " + file);
            return null;
        }
        File parent = file.getParentFile();
        if ( parent == null ) {
            myMsg.debug(LOG, "PARENT NULL");
            return null;
        }
        else if ( isProjectDir(parent) ) {
            myMsg.debug(LOG, "PARENT is project! " + parent);
            return parent;
        }
        else {
            myMsg.debug(LOG, "PARENT recurse on " + parent);
            return getProjectDir(parent);
        }
    }

    private boolean isProjectDir(File dir)
    {
        File[] children = dir.listFiles(new XProjectFilter());
        if ( children == null ) {
            // TODO: Should probably throw an exception instead.
            myMsg.error(LOG, "File is not a dir: " + dir);
            return false;
        }
        return children.length > 0;
    }

    /** The oXygen workspace object. */
    private StandalonePluginWorkspace myWorkspace;
    /** The way to display dialogs and to log messages. */
    private UserMessages myMsg;
    /** The install dir for the XProject plugin (like [oxygen]/plugins/xproject/). */
    private File myPluginDir;
    /** The icons dir for the XProject plugin (like [myPluginDir]/icons/). */
    private File myIconsDir;

    /** The logger object. */
    private static final Logger LOG = Logger.getLogger(XProjectExtension.class);

    private class CreateProjectAction
            extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent event)
        {
            try {
                doAction(event);
            }
            catch ( Throwable ex ) {
                myMsg.error(LOG, "Unexpected runtime error: " + ex, ex);
                throw new RuntimeException(ex);
            }
        }

        protected void doAction(ActionEvent event)
        {
            JFileChooser chooser = new JFileChooser();
            try {
                File file = getCurrentEditedFile();
                File cwd = file.getParentFile();
                chooser.setCurrentDirectory(cwd);
            }
            catch ( XProjectException ex ) {
                // nothing: by default the file chooser display the home dir
                // (or the last visited dir if any)
            }
            JFrame frame = (JFrame) myWorkspace.getParentFrame();
            int res = chooser.showDialog(frame, "Create project");
            // if the cancel button has been hit
            if ( res != JFileChooser.APPROVE_OPTION ) {
                return;
            }
            // create the project dirs & descriptor
            File dir = chooser.getSelectedFile();
            if ( dir.exists() ) {
                myMsg.error(LOG, "Directory exists: " + dir);
                return;
            }
            boolean created = dir.mkdirs();
            if ( ! created ) {
                myMsg.error(LOG, "Error creating directory: " + dir);
                return;
            }
            File src = new File(dir, "src");
            created = src.mkdir();
            if ( ! created ) {
                myMsg.error(LOG, "Error creating directory: " + src);
                return;
            }
            File xproject = new File(dir, "xproject");
            created = xproject.mkdir();
            if ( ! created ) {
                myMsg.error(LOG, "Error creating directory: " + xproject);
                return;
            }
            File project = new File(xproject, "project.xml");
            created = createProjectDescriptor(project);
            if ( created ) {
                try {
                    myWorkspace.open(project.toURI().toURL());
                }
                catch ( MalformedURLException ex ) {
                    myMsg.error(LOG, "Error opening the project descriptor in the editor: " + project);
                }
            }
        }

        private boolean createProjectDescriptor(File desc)
        {
            try {
                PrintWriter out = new PrintWriter(desc);
                out.append("<project xmlns=\"" + XProjectConstants.NS_URI + "\"\n");
                out.append("         name=\"[[ http://example.org/your/project/name ]]\"\n");
                out.append("         abbrev=\"[[ your-project ]]\"\n");
                out.append("         version=\"[[ 0.1.0 ]]\">\n");
                out.append("\n");
                out.append("   <title>[[ Short description of your project ]]</title>\n");
                out.append("\n");
                out.append("</project>\n");
                out.close();
                return true;
            }
            catch ( IOException ex ) {
                myMsg.error(LOG, "Error writing the project descriptor " + desc + ": " + ex, ex);
                return false;
            }
        }
    }

    private class ActionOnExistingProject
            extends AbstractAction
    {
        public ActionOnExistingProject(ProjectPhase phase)
        {
            myPhase = phase;
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            try {
                doAction(event);
            }
            catch ( Throwable ex ) {
                myMsg.error(LOG, "Unexpected runtime error: " + ex, ex);
                throw new RuntimeException(ex);
            }
        }

        protected void doAction(ActionEvent event)
        {
            try {
                XProject prj = getCurrentXProject();
                switch ( myPhase ) {
                    case BUILD:   prj.build();   break;
                    case TEST:    prj.test();    break;
                    case DOC:     prj.doc();     break;
                    case DEPLOY:  prj.deploy();  break;
                    case RELEASE: prj.release(); break;
                    default:
                        throw new RuntimeException("Invalid state, cannot happen: " + myPhase);
                }
            }
            catch ( XProjectException ex ) {
                myMsg.error(LOG, ex.getMessage(), ex);
            }
        }

        /** What action is it? */
        private ProjectPhase myPhase;
    }

    private static class XProjectFilter
            implements FilenameFilter
    {
        @Override
        public boolean accept(File dir, String name)
        {
            if ( "xproject".equals(name) ) {
                File proj = new File(dir, name);
                // TODO: And probably see if it contains a file "project.xml".
                return proj.exists() && proj.isDirectory();
            }
            return false;
        }
    }

    private class MyToolbarCustomizer
            implements ToolbarComponentsCustomizer
    {
        @Override
        public void customizeToolbar(ToolbarInfo bar)
        {
            // TODO: Do we really need the test?
            if ( ToolbarComponentsCustomizer.CUSTOM.equals(bar.getToolbarID()) ) {
                // the actions
                Action create_action  = new CreateProjectAction();
                Action build_action   = new ActionOnExistingProject(ProjectPhase.BUILD);
                Action test_action    = new ActionOnExistingProject(ProjectPhase.TEST);
                Action doc_action     = new ActionOnExistingProject(ProjectPhase.DOC);
                Action release_action = new ActionOnExistingProject(ProjectPhase.RELEASE);
                try {
                    // the buttons
                    JButton create  = configAction(create_action, "Create new project", "create-project.png");
                    JButton build   = configAction(build_action, "Build project", "build-project.png");
                    JButton test    = configAction(test_action, "Test project", "test-project.png");
                    JButton doc     = configAction(doc_action, "Generate project doc", "doc-project.png");
                    JButton release = configAction(release_action, "Build release file for the project", "release-project.png");
                    // add in toolbar
                    JComponent[] c = bar.getComponents();
                    if ( c == null ) {
                        c = new JComponent[5];
                    }
                    else {
                        JComponent[] tmp = new JComponent[c.length + 5];
                        for ( int i = 0; i < c.length; ++i ) {
                            tmp[i] = c[i];
                        }
                        c = tmp;
                    }
                    c[c.length - 5] = create;
                    c[c.length - 4] = build;
                    c[c.length - 3] = test;
                    c[c.length - 2] = doc;
                    c[c.length - 1] = release;
                    bar.setComponents(c);
                    // set title
                    bar.setTitle("XProject");
                }
                catch ( XProjectException ex ) {
                    myMsg.error(LOG, "Error setting up the XProject toolbar: " + ex, ex);
                }
            }
        }

        /**
         * Create and configure a button to add to the toolbar.
         * 
         * @param action The action to attach to the button, when clicked.
         * @param tip The text to use as a tooltip.
         * @param icon_file The icon to use for the button, as a filename
         *      relative to myIconsDir.
         */
        private JButton configAction(Action action, String tip, String icon_file)
                throws XProjectException
        {
            if ( action != null ) {
                action.setEnabled(true);
            }
            JButton button = new JButton(action);
            button.setToolTipText(tip);
            button.setBorder(new EmptyBorder(0, 5, 0, 5));
            File img = new File(myIconsDir, icon_file);
            if ( ! img.exists() ) {
                throw new XProjectException("Icon file does not exist: " + img);
            }
            Icon icon = new ImageIcon(MiscUtils.getPath(img));
            button.setIcon(icon);
            return button;
        }
    }
}


/* ------------------------------------------------------------------------ */
/*  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT.               */
/*                                                                          */
/*  The contents of this file are subject to the Mozilla Public License     */
/*  Version 1.0 (the "License"); you may not use this file except in        */
/*  compliance with the License. You may obtain a copy of the License at    */
/*  http://www.mozilla.org/MPL/.                                            */
/*                                                                          */
/*  Software distributed under the License is distributed on an "AS IS"     */
/*  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See    */
/*  the License for the specific language governing rights and limitations  */
/*  under the License.                                                      */
/*                                                                          */
/*  The Original Code is: all this file.                                    */
/*                                                                          */
/*  The Initial Developer of the Original Code is Florent Georges.          */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
