/****************************************************************************/
/*  File:       XProjectException.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-02-17                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.xproject.oxygen;

/**
 * Generic exception for the XProject plugin for oXygen.
 *
 * @author Florent Georges
 * @date   2012-02-17
 */
public class XProjectException
        extends Exception
{
    public XProjectException(String msg)
    {
        super(msg);
    }

    public XProjectException(String msg, Throwable cause)
    {
        super(msg, cause);
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
