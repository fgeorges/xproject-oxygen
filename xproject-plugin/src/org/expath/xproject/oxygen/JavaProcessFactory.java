/****************************************************************************/
/*  File:       JavaProcessFactory.java                                     */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-02-19                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.xproject.oxygen;

import ro.sync.exml.workspace.api.Workspace;

/**
 * Create Java process objects.
 *
 * @author Florent Georges
 * @date   2012-02-19
 */
public class JavaProcessFactory
{
    /**
     * Package-visibility, intended to be used only by XProjectExtension.
     */
    JavaProcessFactory(Workspace ws, UserMessages messages)
    {
        myWorkspace = ws;
        myMessages = messages;
    }

    public JavaProcess initNewProcess()
    {
        return new JavaProcess(myWorkspace, myMessages);
    }

    private Workspace myWorkspace;
    private UserMessages myMessages;
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
