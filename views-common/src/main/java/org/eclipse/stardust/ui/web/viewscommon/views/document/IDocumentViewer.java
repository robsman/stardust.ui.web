/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public interface IDocumentViewer
{

   /**
    * Initialization
    */
   void initialize(IDocumentContentInfo documentContentInfo, View view);

   /**
    * return the supported Mime Type Map <Mime Type, User Friendly Name>
    * 
    * @return
    */
   MIMEType[] getMimeTypes();

   /**
    * 
    * returns the specific xhtml for the current Reader/Editor
    * 
    * @return
    */
   String getContentUrl();

   /**
    * sets the content to be displayed through the respected Reader/Editor
    * 
    * @param String
    */
   void setContent(String content);

   /**
    * returns the modified or existing content of the document
    * 
    * @return
    */
   String getContent();

   /**
    * returns the viewer/editor specific toolbar url
    * 
    * @return
    */
   String getToolbarUrl();
   
   
   /**
    * clear/free used system resources 
    */
   void closeDocument();
   
}
