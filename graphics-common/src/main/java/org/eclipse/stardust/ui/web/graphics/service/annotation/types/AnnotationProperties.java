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
package org.eclipse.stardust.ui.web.graphics.service.annotation.types;

/**
 * @author Shrikant.Gangal
 *
 */
public class AnnotationProperties
{
   private String text;
   
   private String completetext;
   
   private AnnotationDimensions dimensions;
   
   private AnnotationAttributes attributes;
   
   private String url;
   
   private String documentid;
   
   private String orientation;

   public String getText()
   {
      return text;
   }

   public void setText(String text)
   {
      this.text = text;
   }

   public String getCompletetext()
   {
      return completetext;
   }

   public void setCompletetext(String completetext)
   {
      this.completetext = completetext;
   }

   public AnnotationDimensions getDimensions()
   {
      return dimensions;
   }

   public void setDimensions(AnnotationDimensions dimensions)
   {
      this.dimensions = dimensions;
   }

   public AnnotationAttributes getAttributes()
   {
      return attributes;
   }

   public void setAttributes(AnnotationAttributes attributes)
   {
      this.attributes = attributes;
   }

   public String getUrl()
   {
      return url;
   }

   public void setUrl(String url)
   {
      this.url = url;
   }

   public String getDocumentid()
   {
      return documentid;
   }

   public void setDocumentId(String documentid)
   {
      this.documentid = documentid;
   }

   public String getOrientation()
   {
      return orientation;
   }

   public void setOrientation(String orientation)
   {
      this.orientation = orientation;
   }
}
