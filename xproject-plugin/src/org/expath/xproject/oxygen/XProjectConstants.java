/****************************************************************************/
/*  File:       XProjectConstants.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-04-10                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.xproject.oxygen;

/**
 * Miscellaneous constants related to XProject.
 *
 * @author Florent Georges
 * @date   2012-04-10
 */
public class XProjectConstants
{
    public static enum ProjectPhase
    {
        CREATE,
        BUILD,
        TEST,
        DOC,
        DEPLOY,
        RELEASE
    }

    public static final String NS_URI            = "http://expath.org/ns/project";
    public static final String PRIVATE_DIR       = "xproject";
    public static final String DESCRIPTOR        = "project.xml";
    public static final String SETUPER_STD       = "http://expath.org/ns/project/setup.xproc";
    public static final String BUILDER_STD       = "http://expath.org/ns/project/build.xproc";
    public static final String BUILDER_OVERRIDE  = "build-project.xproc";
    public static final String TESTER_STD        = "http://expath.org/ns/project/test.xproc";
    public static final String TESTER_OVERRIDE   = "test-project.xproc";
    public static final String DOCER_STD         = "http://expath.org/ns/project/doc.xproc";
    public static final String DOCER_OVERRIDE    = "doc-project.xproc";
    public static final String RELEASER_STD      = "http://expath.org/ns/project/release.xsl";
    public static final String RELEASER_OVERRIDE = "release-project.xsl";
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
