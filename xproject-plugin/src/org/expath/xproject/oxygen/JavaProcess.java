/****************************************************************************/
/*  File:       JavaProcess.java                                            */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-02-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.xproject.oxygen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ro.sync.exml.workspace.api.Workspace;
import ro.sync.exml.workspace.api.process.ProcessController;
import ro.sync.exml.workspace.api.process.ProcessListener;

/**
 * Represent a Java process to be executed.
 *
 * @author Florent Georges
 * @date   2012-02-19
 */
public class JavaProcess
{
    /**
     * Package-visibility, intended to be used only by JavaProcessFactory.
     */
    JavaProcess(Workspace ws)
    {
        myWorkspace = ws;
    }

    /**
     * Set the additional Java arguments like "-Xmx256m".
     * 
     * TODO: Add a new method to add a system property (by giving a key/value
     * pair instead of a JVM "-D" argument).
     */
    public void addJavaArgument(String arg)
    {
        myJavaArgs.add(arg);
    }

    /**
     * Add a system property definition (shortcut for addJavaArgument() properly formated).
     */
    public void addSystemProperty(String name, String value)
    {
        addJavaArgument("-D" + name + "=" + value);
    }

    /**
     * Add a component of the classpath (i.e. the path to a dir or a JAR).
     */
    public void addClasspathItem(String path)
    {
        myClasspath.add(path);
    }

    /**
     * Set the name of the main class to invoke.
     */
    public void setMainClass(String clazz)
    {
        myMainClass = clazz;
    }

    /**
     * Set the additional arguments to the program.
     */
    public void addArgument(String arg)
    {
        myArgs.add(arg);
    }

    /**
     * Add a new variable to the process environment.
     */
    public void addEnvVar(String name, String value)
    {
        myEnvVars.put(name, value);
    }

    /**
     * Set the process's working directory.
     */
    public void setWorkingDir(File cwd)
    {
        myCwd = cwd;
    }

    /**
     * Set the process listener.
     */
    public void setProcessListener(ProcessListener listener)
    {
        myListener = listener;
    }

    /**
     * Create the process.
     */
    public ProcessController createJavaProcess()
    {
        String[] cp = myClasspath.toArray(new String[]{});
        String args = formatArgs(myArgs);
        String java_args = formatArgs(myJavaArgs);
        return myWorkspace.createJavaProcess(java_args, cp, myMainClass, args, myEnvVars, myCwd, myListener);
    }

    private String formatArgs(List<String> args)
    {
        // TODO: Escape the individual args with double quotes?
        String s = null;
        for ( String a : args ) {
            s = s == null ? a : s + " " + a;
        }
        return s;
    }

    private Workspace myWorkspace;
    private List<String> myJavaArgs = new ArrayList<String>();
    private List<String> myClasspath = new ArrayList<String>();
    private String myMainClass;
    private List<String> myArgs = new ArrayList<String>();
    private Map<String, String> myEnvVars = new HashMap<String, String>();
    private File myCwd;
    private ProcessListener myListener;
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
