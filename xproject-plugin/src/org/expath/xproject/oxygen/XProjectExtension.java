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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;

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
        UserMessages msg = new UserMessages(this);
        JavaProcessFactory factory = new JavaProcessFactory(ws);
        ToolbarComponentsCustomizer customizer = new MyToolbarCustomizer(msg, factory);
        ws.addToolbarComponentsCustomizer(customizer);
        myWorkspace = ws;
    }

    @Override
    public boolean applicationClosing()
    {
        // nothing
        return true;
    }

    /**
     * TODO: Pass the workspace directly to UserMessage, and don't provide these package-level methods...
     */
    void error(String msg)
    {
        LOG.error(msg);
        myWorkspace.showErrorMessage(msg);
    }

    void error(Throwable ex)
    {
        LOG.error(ex, ex);
        myWorkspace.showErrorMessage(ex.getMessage());
    }

    void info(String msg)
    {
        LOG.info(msg);
        myWorkspace.showInformationMessage(msg);
    }

    void debug(String msg)
    {
        LOG.debug(msg);
    }

    private static final Logger LOG = Logger.getLogger(XProjectExtension.class);
    private StandalonePluginWorkspace myWorkspace;

    // TODO: Should I use any kind of threading, in order to not block the GUI?
    private class MyAction
            extends AbstractAction
    {
        public MyAction(int action, UserMessages message, JavaProcessFactory factory)
        {
            myAction = action;
            myMsg = message;
            myFactory = factory;
        }

        @Override
        @SuppressWarnings("CallToThreadDumpStack")
        public void actionPerformed(ActionEvent event)
        {
            try {
                doAction(event);
            }
            catch ( Throwable ex ) {
                error("Unexpected runtime error: " + ex);
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }

        protected void doAction(ActionEvent event)
        {
            WSEditor editor = myWorkspace.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
            if ( editor == null ) {
                error("There is no opened editor.");
                return;
            }
            URL location = editor.getEditorLocation();
            if ( ! "file".equals(location.getProtocol()) ) {
                error("The open document has not a file: URI.");
                return;
            }
            URI uri;
            try {
                uri = location.toURI();
            }
            catch ( URISyntaxException ex ) {
                error("Not well-formed URI: " + location);
                return;
            }
            File file = new File(uri);
            File project = getProjectDir(file);
            if ( project == null ) {
                error("The edited file is not part of an EXPath project.");
                return;
            }
            XProject prj = new XProject(project, myMsg, myFactory);
            try {
                switch ( myAction ) {
                    case BUILD:   prj.build();   break;
                    case TEST:    prj.test();    break;
                    case DOC:     prj.doc();     break;
                    case DEPLOY:  prj.deploy();  break;
                    case RELEASE: prj.release(); break;
                    default:
                        throw new RuntimeException("Invalid state, cannot happen: " + myAction);
                }
            }
            catch ( XProjectException ex ) {
                error(ex);
            }
        }

        private File getProjectDir(File file)
        {
            if ( ! file.exists() ) {
                error("File does not exist: " + file);
                return null;
            }
            File parent = file.getParentFile();
            if ( parent == null ) {
                debug("PARENT NULL");
                return null;
            }
            else if ( isProjectDir(parent) ) {
                debug("PARENT is project! " + parent);
                return parent;
            }
            else {
                debug("PARENT recurse on " + parent);
                return getProjectDir(parent);
            }
        }

        private boolean isProjectDir(File dir)
        {
            File[] children = dir.listFiles(new XProjectFilter());
            if ( children == null ) {
                // TODO: Should probably throw an exception instead.
                error("File is not a dir: " + dir);
                return false;
            }
            return children.length > 0;
        }

        /** The possible kinds of action. */
        public static final int BUILD   = 1;
        public static final int TEST    = 2;
        public static final int DOC     = 3;
        public static final int DEPLOY  = 4;
        public static final int RELEASE = 5;
        /** What action is it? */
        private int myAction;
        /** The messages object, used for dialog boxes creation and logging. */
        private UserMessages myMsg;
        /** The java process factory. */
        private JavaProcessFactory myFactory;
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
        public MyToolbarCustomizer(UserMessages messages, JavaProcessFactory factory)
        {
            myMsg = messages;
            myFactory = factory;
        }

        @Override
        public void customizeToolbar(ToolbarInfo bar)
        {
            // TODO: Do we really need the test?
            if ( ToolbarComponentsCustomizer.CUSTOM.equals(bar.getToolbarID()) ) {
                // the build button
                MyAction build_a = new MyAction(MyAction.BUILD, myMsg, myFactory);
                build_a.setEnabled(true);
                JButton build_b = new JButton(build_a);
                build_b.setText("Build");
                // the test button
                MyAction test_a = new MyAction(MyAction.TEST, myMsg, myFactory);
                test_a.setEnabled(true);
                JButton test_b = new JButton(test_a);
                test_b.setText("Test");
                // the doc button
                MyAction doc_a = new MyAction(MyAction.DOC, myMsg, myFactory);
                build_a.setEnabled(true);
                JButton doc_b = new JButton(doc_a);
                doc_b.setText("Doc");
                // the deploy button
                MyAction deploy_a = new MyAction(MyAction.DEPLOY, myMsg, myFactory);
                build_a.setEnabled(true);
                JButton deploy_b = new JButton(deploy_a);
                deploy_b.setText("Deploy");
                // the release button
                MyAction release_a = new MyAction(MyAction.RELEASE, myMsg, myFactory);
                release_a.setEnabled(true);
                JButton release_b = new JButton(release_a);
                release_b.setText("Release");
                // add in toolbar
                JComponent[] comps = bar.getComponents();
                if ( comps == null ) {
                    // TODO: Deploy is not supported yet.
                    // comps = new JComponent[5];
                    comps = new JComponent[4];
                }
                else {
                    // TODO: Deploy is not supported yet.
                    // JComponent[] tmp = new JComponent[comps.length + 5];
                    JComponent[] tmp = new JComponent[comps.length + 4];
                    for ( int i = 0; i < comps.length; ++i ) {
                        tmp[i] = comps[i];
                    }
                    comps = tmp;
                }
                comps[comps.length - 4] = build_b;
                comps[comps.length - 3] = test_b;
                comps[comps.length - 2] = doc_b;
                // TODO: Deploy is not supported yet.
                // comps[comps.length - 5] = build_b;
                // comps[comps.length - 4] = test_b;
                // comps[comps.length - 3] = doc_b;
                // comps[comps.length - 2] = deploy_b;
                comps[comps.length - 1] = release_b;
                bar.setComponents(comps);
                // set title
                bar.setTitle("XProject");
            }
        }

        /** The messages object, used for dialog boxes creation and logging. */
        private UserMessages myMsg;
        /** The java process factory. */
        private JavaProcessFactory myFactory;
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
