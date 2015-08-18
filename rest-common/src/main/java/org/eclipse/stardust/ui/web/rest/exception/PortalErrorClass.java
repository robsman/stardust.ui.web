/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.exception;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ws.rs.core.Response.Status;

/**
 * @author Yogesh.Manware
 * 
 */
public class PortalErrorClass implements Serializable
{
   private static final long serialVersionUID = -3549166538354661142L;

   public final static String BUNDLE_NAME = "rest-common-client-messages";

   // throw this error when document does exist in the repository
   public final static PortalErrorClass DOCUMENT_NOT_FOUND = new PortalErrorClass("DOC00001", Status.NOT_FOUND);
      
   private Status httpStatus;
   private String id;

   /**
    * @param id
    * @param httpStatus
    */
   protected PortalErrorClass(String id, Status httpStatus)
   {
      this.id = id;
      this.httpStatus = httpStatus;
   }

   /**
    * @param locale
    * @return
    */
   public String getLocalizedMessage(Locale locale)
   {
      ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
      return bundle.getString(id);
   }

   @Override
   public String toString()
   {
      return "PortalErrorClass [httpStatus=" + httpStatus + ", id=" + id + "]";
   }

   /**
    * @return
    */
   public Status getHttpStatus()
   {
      return httpStatus;
   }
}