/****************************************************************************/
/*  File:       UserMessages.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-02-18                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.xproject.oxygen;

import org.apache.log4j.Logger;
import ro.sync.exml.workspace.api.Workspace;

/**
 * Provides a mean to show message to the user.
 * 
 * Depending on the messages, they can be displayed in the graphical interface
 * (e.g. in dialog boxes) or in the log file.
 *
 * @author Florent Georges
 * @date   2012-02-18
 */
public class UserMessages
{
    public UserMessages(Workspace ws)
    {
        myWorkspace = ws;
    }

    /**
     * Display an error message in a dialog box, and in the logs.
     */
    public void error(String msg)
    {
        LOG.error(msg);
        myWorkspace.showErrorMessage(msg);
    }

    /**
     * Display an info message in a dialog box, and in the logs.
     */
    public void info(String msg)
    {
        LOG.info(msg);
        myWorkspace.showInformationMessage(msg);
    }

    /** The workspace object, to create dialog boxes. */
    private Workspace myWorkspace;
    /**
     * TODO: This is not correct, let the caller pass its own logger...
     */
    private static final Logger LOG = Logger.getLogger(UserMessages.class);
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
