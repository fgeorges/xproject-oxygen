/****************************************************************************/
/*  File:       UserMessages.java                                           */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-02-18                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.xproject.oxygen;

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
    public UserMessages(XProjectExtension ext)
    {
        myExt = ext;
    }

    /**
     * Display an error message in a dialog box, and in the logs.
     */
    public void error(String msg)
    {
        myExt.error(msg);
    }

    /**
     * Display an info message in a dialog box, and in the logs.
     */
    public void info(String msg)
    {
        myExt.info(msg);
    }

    private XProjectExtension myExt;
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
