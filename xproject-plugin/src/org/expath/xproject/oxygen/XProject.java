/****************************************************************************/
/*  File:       XProject.java                                               */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-02-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.xproject.oxygen;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Support for XProject build tools.
 * 
 * @author Florent Georges
 * @date   2012-02-16
 */
public class XProject
{
    /**
     * XProject constructor.
     * 
     * The file parameter must point to a project directory.  The workspace
     * parameter is the oXygen object to access the GUI services (it is used
     * to open dialog boxes).
     * 
     * TODO: Should we really throw exception in this context?  I think we
     * should give the user an error dialog box, but not throwing an error.
     * Maybe we should throw an error here and let XProjectExtension deal with
     * the user interaction?
     */
    public XProject(File project, UserMessages messages)
    {
        // the project dir
        if ( project == null ) {
            throw new NullPointerException("Project dir is null.");
        }
        if ( ! project.isDirectory() ) {
            throw new IllegalArgumentException("Project is not a directory: '" + project + "'.");
        }
        myProject = project;

        // the project private dir
        File priv = new File(project, XPROJECT_PRIVATE);
        if ( priv == null ) {
            throw new IllegalArgumentException("Project dir does not have an " + XPROJECT_PRIVATE + "/ subdir (in '" + project + "').");
        }
        if ( ! priv.isDirectory() ) {
            throw new IllegalArgumentException("XProject subdir is not a directory: '" + priv + "'.");
        }
        myPrivate = priv;

        // the project descriptor
        File desc = new File(priv, XPROJECT_DESC);
        if ( desc == null ) {
            throw new IllegalArgumentException("Project descriptor (aka " + XPROJECT_DESC + ") does not exist (in '" + priv + "').");
        }
        if ( ! desc.isFile() ) {
            throw new IllegalArgumentException("Project descriptor is not a regular file: '" + desc + "'.");
        }
        myDesc = desc;

        // the user reporting helper
        if ( messages == null ) {
            throw new NullPointerException("User messages is null.");
        }
        myMsg = messages;
    }

    /**
     * Build the package (by applying the XProject "package" stylesheet on project.xml).
     */
    public void build()
            throws XProjectException
    {
        ensureDistDir();
        applyStylesheet(PACKAGER_STD, PACKAGER_OVERRIDE, "packager");
    }

    /**
     * Test the project (by applying the XProject "test" pipeline on project.xml).
     * 
     * calabash -i source=xproject/project.xml [pipeline]
     */
   public void test()
            throws XProjectException
    {
        String pipe = getHref(TESTER_STD, TESTER_OVERRIDE, "tester");
        List<String> cmd = initJavaCmd();
        cmd.add("-Dcom.xmlcalabash.xproc-configurer=org.expath.pkg.calabash.PkgConfigurer");
        cmd.add("com.xmlcalabash.drivers.Main");
        cmd.add("-i");
        cmd.add("source=" + myDesc.getAbsolutePath());
        cmd.add(pipe);
        executeJavaCmd(cmd);
    }

    /**
     * Generate documentation for the project (by applying the XProject "doc" pipeline on project.xml).
     * 
     * calabash [pipeline] xquery=src/ output=dist/xqdoc/ currentdir=src/ format=html > dist/xqdoc/index.html
     */
    public void doc()
            throws XProjectException
    {
        ensureDistDir();
        String pipe = getHref(DOCER_STD, DOCER_OVERRIDE, "doc maker");
        File src   = new File(myProject, "src/");
        File dist  = new File(myProject, "dist/xqdoc/");
        // TODO: Redirect the output to the index.html file.
        // TODO: Actually, write instead a std doc.xproc pipeline in XProject.
        File index = new File(myProject, "dist/xqdoc/index.html");
        List<String> cmd = initJavaCmd();
        cmd.add("-Dcom.xmlcalabash.xproc-configurer=org.expath.pkg.calabash.PkgConfigurer");
        cmd.add("com.xmlcalabash.drivers.Main");
        cmd.add(pipe);
        cmd.add("xquery=" + src.getAbsolutePath());
        cmd.add("output=" + dist.getAbsolutePath());
        cmd.add("currentdir=" + src.getAbsolutePath());
        cmd.add("format=html");
        executeJavaCmd(cmd);
    }

    /**
     * TODO: Mimic "xproj deploy".  Which does not exist yet...
     */
    public void deploy()
            throws XProjectException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Release the project (by applying the XProject "release" stylesheet on project.xml).
     */
    public void release()
            throws XProjectException
    {
        ensureDistDir();
        applyStylesheet(RELEASER_STD, RELEASER_OVERRIDE, "releaser");
    }

    /**
     * Ensure [project]/dist/ subdir, where to place most of the result of the builds.
     */
    private void ensureDistDir()
    {
        File dist = new File(myProject, "dist/");
        if ( ! dist.exists() ) {
            if ( ! dist.mkdir() ) {
                myMsg.error("Impossible to create the project dist/ subdir (in " + dist + ")");
            }
        }
    }

    /**
     * saxon -xsl:[style] -s:xproject/project.xml proj:revision=...
     */
    private void applyStylesheet(String std, String override, String name)
            throws XProjectException
    {
        String style = getHref(std, override, name);
        List<String> cmd = initJavaCmd();
        cmd.add("net.sf.saxon.Transform");
        cmd.add("-init:org.expath.pkg.saxon.PkgInitializer");
        cmd.add("-xsl:" + style);
        cmd.add("-s:" + myDesc.getAbsolutePath());
        cmd.add("{" + PROJECT_NS + "}revision=yo");
        executeJavaCmd(cmd);
    }

    /**
     * Initialize a command line (as a list of strings) to call Java.
     * 
     * The class path is setup to include Saxon, Calabash and the EXPath
     * Packaging.  The caller can add the main class name to invoke and its
     * parameters to the list before passing it to Runtime.exec() (as an
     * array of strings), or any other JVM param before.  Technically, the
     * class path is initialized by scanning the content of the subdir lib/
     * in the plugin dir.
     */
    private List<String> initJavaCmd()
    {
        // TODO: Access the plugin dir through some oXygen API...
        // the classpath
        File lib = getPluginSubdir("lib/");
        String cp = null;
        for ( File jar : lib.listFiles() ) {
            String path = jar.getAbsolutePath();
            cp = cp == null ? path : cp + ":" + path;
        }
        // the repo dir
        File repo = getPluginSubdir("repo/");
        // the command
        List<String> cmd = new ArrayList<String>();
        cmd.add("java");
        cmd.add("-Dorg.expath.pkg.saxon.repo=" + repo.getAbsolutePath());
        cmd.add("-cp");
        cmd.add(cp);
        return cmd;
    }

    private void executeJavaCmd(List<String> cmd)
            throws XProjectException
    {
        try {
            String[] array = cmd.toArray(new String[]{});
            Process proc = Runtime.getRuntime().exec(array);
            int result = proc.waitFor();
            if ( result == 0 ) {
                myMsg.info("Build succesful");
            }
            else {
                myMsg.error("Build failure: " + result + "\n(please see oXygen logs)");
            }
            BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line;
            while ( (line = stdout.readLine()) != null ) {
                LOG.debug("STDOUT: " + line);
            }
            while ( (line = stderr.readLine()) != null ) {
                LOG.debug("STDERR: " + line);
            }
        }
        catch ( IOException ex ) {
            throw new XProjectException("Error executing Saxon", ex);
        }
        catch ( InterruptedException ex ) {
            throw new XProjectException("Error executing Saxon", ex);
        }
    }

    /**
     * TODO: Adapt javadoc from getSource()...
     */
    private String getHref(String std, String override, String name)
            throws XProjectException
    {
        // last bit of the msg, for logging
        String msg = name + "' (aka '" + override + "'), in '" + myPrivate + "'";
        // check if the override file exists (either return it or the std one)
        File override_f = new File(myPrivate, override);
        if ( override_f.exists() ) {
            LOG.debug("Project dir overrides '" + msg);
            return override_f.toURI().toString();
        }
        else {
            LOG.debug("Project dir does not override '" + msg);
            return std;
        }
    }

    /**
     * Get a specific subdir of the plugin directory.
     * 
     * Throw an error if it does not exist or if it is not a directory.
     */
    private File getPluginSubdir(String name)
    {
        String path = PLUGIN_DIR + name;
        File subdir = new File(path);
        if ( subdir == null ) {
            myMsg.error("The plugin subdir is not found: '" + path + "'");
        }
        if ( ! subdir.isDirectory() ) {
            myMsg.error("The plugin subdir is not a dir: '" + path + "'");
        }
        return subdir;
    }

    /** The project directory. */
    private File myProject;
    /** The project private sub-directory. */
    private File myPrivate;
    /** The project descriptor, aka xproject/project.xml. */
    private File myDesc;
    /** The messages object, used for dialog boxes creation and logging. */
    private UserMessages myMsg;
    /** The logger for this class. */
    private static final Logger LOG = Logger.getLogger(XProjectExtension.class);

    /**
     * FIXME: Hard-coded value...!!!
     * 
     * TODO: Retrieve the plugin dir through some oXygen API...
     */
    private static final String PLUGIN_DIR        = "/Applications/oxygen-13.2/plugins/xproject/";

    private static final String PROJECT_NS        = "http://expath.org/ns/project";
    private static final String XPROJECT_PRIVATE  = "xproject";
    private static final String XPROJECT_DESC     = "project.xml";
    private static final String PACKAGER_STD      = "http://expath.org/ns/project/package.xsl";
    private static final String PACKAGER_OVERRIDE = "package-project.xsl";
    private static final String TESTER_STD        = "http://expath.org/ns/project/test.xproc";
    private static final String TESTER_OVERRIDE   = "test-project.xproc";
    // TODO: Define a standard one.
    private static final String DOCER_STD         = "http://xqdoc.org/xquerydoc.xpl";
    private static final String DOCER_OVERRIDE    = "doc-project.xproc";
    private static final String RELEASER_STD      = "http://expath.org/ns/project/release.xsl";
    private static final String RELEASER_OVERRIDE = "release-project.xsl";

// Notes on: Old code...
// 
// The initial approach was to instantiate Saxon and Calabash directly from 
// here and use their API to run the transforms/pipelines.  But because oXygen
// does not use Saxon 9.4 yet, it was easier to just launch them as external
// processors.  The code below will help would we decide that we should follow
// this approach again...
// 
//    /**
//     * Get the cached repository.
//     */
//    private synchronized SaxonRepository getRepo()
//            throws XProjectException
//    {
//        if ( ourRepo == null ) {
//            // TODO: FIXME: Retrieve the exact location of the repo from the oXygen options!
//            // (or by default from $EXPATH_REPO)
//            File repo_dir = getPluginSubdir("repo/");
//            try {
//                FileSystemStorage storage = new FileSystemStorage(repo_dir);
//                ourRepo = new SaxonRepository(storage);
//            }
//            catch ( PackageException ex ) {
//                throw new XProjectException("Error initializing the package repository", ex);
//            }
//        }
//        return ourRepo;
//    }
//
//    /**
//     * Get a source object for an XProject stylesheet or pipeline.
//     * 
//     * The standard XProject stylesheets and pipelines can be overriden in the
//     * project's private directory.
//     * 
//     * @param std The absolute import URI for the standard XProject component.
//     * 
//     * @param override The name of the corresponding overriding component,
//     *     relative to the project's private directory.
//     * 
//     * @param repo The repository to use to resolve the standard component.
//     * 
//     * @param name The logical name of the component (to display in messages
//     *     and logs dedicated to humans).
//     * 
//     * @return The source representing the override if it exists, or the
//     *     standard component.
//     */
//    private Source getSource(String std, String override, SaxonRepository repo, String name)
//            throws XProjectException
//    {
//        File override_f = new File(myPrivate, override);
//        if ( override_f.exists() ) {
//            debug("Project dir overrides " + name + " (aka " + override + "), in " + myPrivate);
//            return new StreamSource(override_f);
//        }
//        else {
//            debug("Project dir does not override " + name + " (aka " + override + "), in " + myPrivate);
//            try {
//                return repo.resolve(std, URISpace.XSLT);
//            }
//            catch ( PackageException ex ) {
//                throw new XProjectException("Error resolving the " + name + " within the package repository: '" + std + "'", ex);
//            }
//        }
//    }
//
//    /**
//     * Apply a stylesheet (either the standard one or its override) to the project descriptor.
//     * 
//     * Must correspond to the pseudo-command:
//     * <pre>
//     * saxon -xsl:[style] -s:xproject/project.xml proj:revision=...
//     * </pre>
//     */
//    private void applyStylesheet(String std, String override, String name)
//            throws XProjectException
//    {
//        // TODO: Remove all dependencies on packaging (to be able to use XProject without any repo).
//        SaxonRepository repo = getRepo();
//        Source style = getSource(std, override, repo, name);
//        Processor saxon = getSaxon(repo);
//        XsltCompiler compiler = saxon.newXsltCompiler();
//        try {
//            compiler.setErrorListener(new MyLogErrorListener(this));
//            XsltExecutable exec = compiler.compile(style);
//            XsltTransformer transform = exec.load();
//            transform.setSource(new StreamSource(myDesc));
//            transform.setDestination(new EmptyDestination());
//            // TODO: Retrieve the $proj:revision param value from SVN.
//            XdmValue revision = new XdmAtomicValue("rXXX");
//            transform.setParameter(new QName(PROJECT_NS, "proj:revision"), revision);
//            transform.transform();
//            // TODO: The stylesheet does NOT give any useful info.  For now it
//            // uses xsl:message.  Make it return some kind of message in the
//            // main output tree (so it is usable from here as well as from the
//            // shell script.)
//            info("Successfuly built");
//        }
//        catch ( SaxonApiException ex ) {
//            throw new XProjectException("Error building the project", ex);
//        }
//    }
// 
//    /**
//     * Apply a stylesheet (either the standard one or its override) to the project descriptor.
//     * 
//     * Must correspond to the pseudo-command:
//     * <pre>
//     * calabash -i source=xproject/project.xml [pipeline]
//     * </pre>
//     */
//    private void applyPipeline(String std, String override, String name)
//            throws XProjectException
//    {
//        SaxonRepository repo = getRepo();
//        XProcRuntime calabash = getCalabash(repo);
//        String href = getHref(std, override, repo, URISpace.XPROC, name);
//        try {
//            // TODO: Add something similar to MyLogErrorListener...?
//            // calabash.setMessageListener(...);
//            XPipeline pipe = calabash.load(href);
//            InputSource desc = new InputSource(myDesc.toURI().toString());
//            pipe.writeTo("source", calabash.parse(desc));
//            // TODO: Retrieve the $proj:revision param value from SVN.  (Really?)
//            pipe.run();
//            // TODO: The pipeline does NOT give any useful info.  Make it
//            // return some kind of message in the main output tree (so it is
//            // usable from here as well as from the shell script.)
//            info("Successfuly built");
//        }
//        catch ( SaxonApiException ex ) {
//            throw new XProjectException("Error building the project", ex);
//        }
//    }
//
//    /**
//     * Return Saxon processor object.
//     * 
//     * TODO: Try to use oXygen's own instance?  Is it accessible from plugins?
//     */
//    private Processor getSaxon(SaxonRepository repo)
//            throws XProjectException
//    {
//        try {
//            Processor saxon = new Processor(false);
//            ConfigHelper configurer = new ConfigHelper(repo);
//            configurer.config(saxon.getUnderlyingConfiguration());
//            return saxon;
//        }
//        catch ( PackageException ex ) {
//            throw new XProjectException("Error configuring Saxon with the package repository", ex);
//        }
//    }
//
//    /**
//     * Return Calabash processor object.
//     * 
//     * TODO: Try to use oXygen's own instance?  Is it accessible from plugins?
//     */
//    private XProcRuntime getCalabash(SaxonRepository repo)
//            throws XProjectException
//    {
//        Processor saxon = new Processor(false);
//        XProcConfiguration xconf = new XProcConfiguration(saxon);
//        // xconf.debug = true;
//        XProcRuntime calabash = new XProcRuntime(xconf);
//        PkgConfigurer configurer = new PkgConfigurer(calabash, repo.getUnderlyingRepo());
//        calabash.setConfigurer(configurer);
//        return calabash;
//    }
// 
//    /**
//     * Redirect Saxon messages in oXygen's log file.
//     * 
//     * TODO: myXPrj.error() is used only here...  Find a way to report those
//     * messages to XProjectExtension, so we can get rid of myWorkspace at all!
//     */
//    private static class MyLogErrorListener
//            implements ErrorListener
//    {
//        public MyLogErrorListener(XProject xprj)
//        {
//            myXPrj = xprj;
//        }
//
//        public void warning(TransformerException ex)
//                throws TransformerException
//        {
//            myXPrj.debug("XSLT warning", ex);
//        }
//
//        public void error(TransformerException ex)
//                throws TransformerException
//        {
//            myXPrj.error("XSLT error", ex);
//        }
//
//        public void fatalError(TransformerException ex)
//                throws TransformerException
//        {
//            myXPrj.error("XSLT fatal error", ex);
//        }
//
//        private XProject myXPrj;
//    }
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
