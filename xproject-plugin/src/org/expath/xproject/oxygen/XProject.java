/****************************************************************************/
/*  File:       XProject.java                                               */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-02-16                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.xproject.oxygen;

import java.io.File;
import java.net.URI;
import org.apache.log4j.Logger;
import ro.sync.exml.workspace.api.process.ProcessListener;

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
     * The file parameter must point to a project directory.  The user message
     * object encapsulates the mean to dialog with the user (like showing
     * dialog boxes).  The Java process factory creates Java processes.  And
     * the plugin dir is the XProject subdir in the oXygen's "plugins/" dir.
     */
    public XProject(File project, UserMessages messages, JavaProcessFactory factory, File plugin_dir)
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
        File priv = new File(project, XProjectConstants.PRIVATE_DIR);
        if ( priv == null ) {
            throw new IllegalArgumentException("Project dir does not have an " + XProjectConstants.PRIVATE_DIR + "/ subdir (in '" + project + "').");
        }
        if ( ! priv.isDirectory() ) {
            throw new IllegalArgumentException("XProject subdir is not a directory: '" + priv + "'.");
        }
        myPrivate = priv;

        // the project descriptor
        File desc = new File(priv, XProjectConstants.DESCRIPTOR);
        if ( desc == null ) {
            throw new IllegalArgumentException("Project descriptor (aka " + XProjectConstants.DESCRIPTOR + ") does not exist (in '" + priv + "').");
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

        // the java process factory
        if ( factory == null ) {
            throw new NullPointerException("Java process factory is null.");
        }
        myFactory = factory;

        // the plugin directory
        if ( plugin_dir == null ) {
            throw new NullPointerException("Plugin dir is null.");
        }
        if ( ! plugin_dir.isDirectory() ) {
            throw new IllegalArgumentException("Plugin dir is not a directory: '" + plugin_dir + "'.");
        }
        myPluginDir = plugin_dir;
    }

    /**
     * Return the project descriptor, aka 'xproject/project.xml'.
     */
    public File getDescriptor()
    {
        return myDesc;
    }

    /**
     * Setup a new project (by calling the XProject "setup" pipeline with the dir as option).
     * 
     * calabash [pipeline] path=[dir]
     */
    public static XProject setup(File dir, UserMessages messages, JavaProcessFactory factory, File plugin_dir)
            throws XProjectException
    {
        if ( dir.exists() ) {
            throw new XProjectException("Directory exists: " + dir);
        }
        String pipe = XProjectConstants.SETUPER_STD;
        String path = MiscUtils.getUri(dir);
        JavaProcess proc = initJavaProcess(factory, messages, plugin_dir);
        proc.setMainClass("com.xmlcalabash.drivers.Main");
        proc.addArgument(pipe);
        proc.addArgument("path=" + path);
        proc.createJavaProcess().start();
        return new XProject(dir, messages, factory, plugin_dir);
    }

    /**
     * Build the project (by applying the XProject "build" pipeline on project.xml).
     * 
     * calabash -i source=xproject/project.xml [pipeline]
     */
    public void build()
            throws XProjectException
    {
        applyPipeline(XProjectConstants.BUILDER_STD, XProjectConstants.BUILDER_OVERRIDE, "builder");
    }

    /**
     * Test the project (by applying the XProject "test" pipeline on project.xml).
     * 
     * calabash -i source=xproject/project.xml [pipeline]
     */
   public void test()
            throws XProjectException
    {
        applyPipeline(XProjectConstants.TESTER_STD, XProjectConstants.TESTER_OVERRIDE, "tester");
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
        String pipe = getHref(XProjectConstants.DOCER_STD, XProjectConstants.DOCER_OVERRIDE, "doc maker");
        File src    = new File(myProject, "src/");
        File dist   = new File(myProject, "dist/xqdoc/");
        JavaProcess proc = initJavaProcess();
        proc.setMainClass("com.xmlcalabash.drivers.Main");
        proc.addArgument("-i");
        proc.addArgument("source=" + MiscUtils.getUri(myDesc));
        proc.addArgument(pipe);
        proc.createJavaProcess().start();
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
        applyStylesheet(XProjectConstants.RELEASER_STD, XProjectConstants.RELEASER_OVERRIDE, "releaser");
    }

    /**
     * Ensure [project]/dist/ subdir, where to place most of the result of the builds.
     */
    private void ensureDistDir()
            throws XProjectException
    {
        File dist = new File(myProject, "dist/");
        if ( ! dist.exists() ) {
            if ( ! dist.mkdir() ) {
                throw new XProjectException("Impossible to create the project dist/ subdir (in " + dist + ")");
            }
        }
    }

    /**
     * calabash -i source=xproject/project.xml [pipe]
     */
    private void applyPipeline(String std, String override, String name)
            throws XProjectException
    {
        String pipe = getHref(std, override, name);
        JavaProcess proc = initJavaProcess();
        proc.setMainClass("com.xmlcalabash.drivers.Main");
        proc.addArgument("-i");
        proc.addArgument("source=" + MiscUtils.getUri(myDesc));
        proc.addArgument(pipe);
        proc.createJavaProcess().start();
    }

    /**
     * saxon -xsl:[style] -s:xproject/project.xml proj:revision=...
     */
    private void applyStylesheet(String std, String override, String name)
            throws XProjectException
    {
        String style = getHref(std, override, name);
        JavaProcess proc = initJavaProcess();
        proc.setMainClass("net.sf.saxon.Transform");
        proc.addArgument("-init:org.expath.pkg.saxon.PkgInitializer");
        proc.addArgument("-xsl:" + style);
        proc.addArgument("-s:" + MiscUtils.getUri(myDesc));
        // TODO: Retrieve the revision number!
        proc.addArgument("{" + XProjectConstants.NS_URI + "}revision=yo");
        proc.createJavaProcess().start();
    }

    /**
     * Initialize a command line (as a list of strings) to call Java.
     * 
     * The class path is setup to include Saxon, Calabash and the EXPath
     * Packaging.  Technically, the class path is initialized by scanning the
     * content of the subdir lib/ in the plugin dir.  The local repository is
     * also setup correctly for Saxon and Calabash.  And our custom process
     * listener is set on the new Java process, ready to be started (as soon
     * as the caller set the main class and its parameters).
     */
    private JavaProcess initJavaProcess()
            throws XProjectException
    {
        return initJavaProcess(myFactory, myMsg, myPluginDir);
    }

    private static JavaProcess initJavaProcess(JavaProcessFactory factory, UserMessages msg, File plugin_dir)
            throws XProjectException
    {
        JavaProcess proc = factory.initNewProcess();
        // the classpath
        File lib = getPluginSubdir("lib/", plugin_dir);
        for ( File jar : lib.listFiles() ) {
            String path = jar.getAbsolutePath();
            proc.addClasspathItem(path);
        }
        // the repo dir
        File repo = getPluginSubdir("repo/", plugin_dir);
        // $EXPATH_REPO
        proc.addEnvVar("EXPATH_REPO", repo.getAbsolutePath());
        // the java args
        proc.addSystemProperty("org.expath.pkg.saxon.repo", repo.getAbsolutePath());
        proc.addSystemProperty("org.expath.pkg.calabash.repo", repo.getAbsolutePath());
        proc.addSystemProperty("com.xmlcalabash.xproc-configurer", "org.expath.pkg.calabash.PkgConfigurer");
        // the listener
        ProcessListener listener = new XProjectProcListener(msg);
        proc.setProcessListener(listener);
        return proc;
    }

    /**
     * The process listener for our Java processes.
     * 
     * It does the following things:
     * - log the command when the process is started
     * - show an error dialog if the process could not start
     * - show a dialog with either "success" or "failure" when process ends
     * - redirect stdout and stderr to an output panel at bottom of the screen
     * 
     * TODO: Redirecting stdout and stderr is not implemented yet (they are
     * logged, but not redirected to an output panel).
     */
    private static class XProjectProcListener
            extends ProcessListener
    {
        public XProjectProcListener(UserMessages messages) {
            myMsg = messages;
            myStdOut = new StringBuilder();
            myStdErr = new StringBuilder();
        }

        public String getStdOut() {
            return myStdOut.toString();
        }

        public String getStdErr() {
            return myStdErr.toString();
        }

        @Override
        public void newErrorLine(String line) {
            LOG.debug("STDERR: " + line);
            myStdErr.append(line);
            myStdErr.append("\n");
        }

        @Override
        public void newOutputLine(String line) {
            LOG.debug("STDOUT: " + line);
            myStdOut.append(line);
            myStdOut.append("\n");
        }

        @Override
        public void processCouldNotStart(String msg) {
            myMsg.error("Process could not start: " + msg);
        }

        @Override
        public void processEnded(int code) {
            myMsg.debug("STDOUT -----------------------------------");
            myMsg.debug(myStdOut.toString());
            myMsg.debug("STDERR -----------------------------------");
            myMsg.debug(myStdErr.toString());
            myMsg.debug("end-of-outputs ---------------------------");
            // check the result code
            // TODO: It seems Calabash returns always 0?!?  Even with p:error...?!?
            if ( code == 0 ) {
                myMsg.info("Build succesful");
            }
            else {
                myMsg.error("Build failure: " + code + "\n(please see oXygen logs)");
            }
        }

        @Override
        public void processStarted(String name, String command) {
            LOG.debug("Process started: " + name + "\n" + command);
        }

        private final UserMessages  myMsg;
        private final StringBuilder myStdOut;
        private final StringBuilder myStdErr;
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

    private static File getPluginSubdir(String name, File plugin_dir)
            throws XProjectException
    {
        File subdir = new File(plugin_dir, name);
        if ( subdir == null ) {
            throw new XProjectException("The plugin subdir is not found: '" + name + "'");
        }
        if ( ! subdir.isDirectory() ) {
            throw new XProjectException("The plugin subdir is not a dir: '" + subdir + "'");
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
    /** The java process factory. */
    private JavaProcessFactory myFactory;
    /** The java process factory. */
    private File myPluginDir;

    /** The logger for this class. */
    private static final Logger LOG = Logger.getLogger(XProjectExtension.class);

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
