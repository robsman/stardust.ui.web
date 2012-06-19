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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.viewscommon.utils.FacesUtils;


/**
 * Assist in displaying form level validation messages
 * 
 * @author Yogesh.Manware
 * 
 */
public class ValidationMessageBean implements Serializable
{
   private static final long serialVersionUID = -7515310494697409765L;
   private List<String> errorMessages;
   private FacesContext facesContext;

   public ValidationMessageBean()
   {
      super();
      errorMessages = new ArrayList<String>();
      facesContext = FacesContext.getCurrentInstance();
   }

   /**
    * @param reset
    * @return
    */
   public void reset()
   {
      this.errorMessages = new ArrayList<String>();
   }

   /**
    * @param errorMsg
    * @param clientIds
    */
   public void addError(String errorMsg, String... clientIds)
   {
      errorMessages.add(errorMsg);

      for (String clientId : clientIds)
      {
         facesContext.addMessage(FacesUtils.getClientId(clientId), new FacesMessage(FacesMessage.SEVERITY_ERROR, null,
               null));
      }
   }

   public List<String> getMessages()
   {
      return errorMessages;
   }

   public boolean isContainMessages()
   {
      return errorMessages.size() > 0;
   }
}
