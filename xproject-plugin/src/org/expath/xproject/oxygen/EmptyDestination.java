/****************************************************************************/
/*  File:       EmptyDestination.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2012-02-17                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2012 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.xproject.oxygen;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;

/**
 * Saxon destination that ignores the output.
 *
 * @author Florent Georges
 * @date   2012-02-17
 */
public class EmptyDestination
        implements Destination
{
    public Receiver getReceiver(Configuration c)
            throws SaxonApiException
    {
        return new EmptyReceiver();
    }

    public void close()
            throws SaxonApiException
    {
        // empty
    }

    private static class EmptyReceiver
            implements Receiver
    {
        public void setPipelineConfiguration(PipelineConfiguration conf) {
            myConf = conf;
        }

        public PipelineConfiguration getPipelineConfiguration() {
            return myConf;
        }

        public void open() throws XPathException {
            // empty
        }

        public void startDocument(int i) throws XPathException {
            // empty
        }

        public void endDocument() throws XPathException {
            // empty
        }

        public void setUnparsedEntity(String string, String string1, String string2) throws XPathException {
            // empty
        }

        public void startElement(NodeName nn, SchemaType st, int i, int i1) throws XPathException {
            // empty
        }

        public void namespace(NamespaceBinding nb, int i) throws XPathException {
            // empty
        }

        public void attribute(NodeName nn, SimpleType st, CharSequence cs, int i, int i1) throws XPathException {
            // empty
        }

        public void startContent() throws XPathException {
            // empty
        }

        public void endElement() throws XPathException {
            // empty
        }

        public void characters(CharSequence cs, int i, int i1) throws XPathException {
            // empty
        }

        public void processingInstruction(String string, CharSequence cs, int i, int i1) throws XPathException {
            // empty
        }

        public void comment(CharSequence cs, int i, int i1) throws XPathException {
            // empty
        }

        public void close() throws XPathException {
            // empty
        }

        public boolean usesTypeAnnotations() {
            return false;
        }

        public String getSystemId() {
            return mySysid;
        }

        public void setSystemId(String sysid) {
            mySysid = sysid;
        }

        private String mySysid;
        private PipelineConfiguration myConf;
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
