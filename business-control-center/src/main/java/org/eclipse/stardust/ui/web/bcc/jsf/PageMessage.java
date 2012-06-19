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
package org.eclipse.stardust.ui.web.bcc.jsf;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

public final class PageMessage
{

   public static void setMessage(FacesException facesException)
   {
      FacesMessage msg = null;
      if (facesException instanceof InvalidServiceException)
      {
         InvalidServiceException invalidService = (InvalidServiceException) facesException;
         msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
               invalidService.getSummary(), invalidService.getDetail());
      }
      else if(facesException instanceof ValidatorException)
      {
         ValidatorException validatorException = (ValidatorException)facesException;
         msg = validatorException.getFacesMessage();
      }
      else
      {
         msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
               null, facesException.getLocalizedMessage());
      }
      if(msg != null)
      {
         setMessage(msg);
      }
   }
   
   public static void setMessage(FacesMessage.Severity serverity, String summary, String detail)
   {
      setMessage(new FacesMessage(serverity, summary, detail));
   }

   public static void setMessage(FacesMessage msg)
   {
      FacesContext.getCurrentInstance().addMessage(null, msg);
   }
}
