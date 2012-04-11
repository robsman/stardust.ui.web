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
package org.eclipse.stardust.ui.web.processportal.view;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.TypedDocument;



public class DocumentInfo
{
   private String documentIcon;
   private String id = "";
   private String name = "";
   private TypedDocument typedDocument;

   /**
    * @param icon
    * @param document
    */
   public DocumentInfo(String icon, Document document)
   {
      this.documentIcon = icon;
      this.id = document.getId();
      this.name = document.getName();
   }

   /**
    * @param icon
    * @param typedDocument
    */
   public DocumentInfo(String icon, TypedDocument typedDocument)
   {
      this.documentIcon = icon;
      this.typedDocument = typedDocument;
      Document document = typedDocument.getDocument();
      if (null != document)
      {
         this.id = document.getId();
      }
      this.name = typedDocument.getName();
   }

   public String getDocumentIcon()
   {
      return documentIcon;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public TypedDocument getTypedDocument()
   {
      return typedDocument;
   }
}