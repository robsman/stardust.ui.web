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

import org.apache.commons.collections.CollectionUtils;
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
   private List<String> infoMessages;
   private FacesContext facesContext;
   // Style attr added to have filter for show/Hide icons(Info,error msg icons)
   private String styleClass;

   public ValidationMessageBean()
   {
      super();
      errorMessages = new ArrayList<String>();
      infoMessages = new ArrayList<String>();
      facesContext = FacesContext.getCurrentInstance();
   }

   /**
    * @param reset
    * @return
    */
   public void reset()
   {
      this.errorMessages = new ArrayList<String>();
      this.infoMessages = new ArrayList<String>();
   }

   /**
    * @param errorMsg
    * @param clientIds
    */
   public void addError(String errorMsg, String... clientIds)
   {
      errorMessages.add(errorMsg);

      if (null != clientIds)
      {
         for (String clientId : clientIds)
         {
            facesContext.addMessage(FacesUtils.getClientId(clientId), new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg,
                  errorMsg));
         }
      }
   }

   /**
    * @param errorMsg
    * @param clientIds
    */
   public void addInfoMessage(String infoMsg, String... clientIds)
   {
      infoMessages.add(infoMsg);

      for (String clientId : clientIds)
      {
         facesContext.addMessage(FacesUtils.getClientId(clientId), new FacesMessage(FacesMessage.SEVERITY_INFO, infoMsg,
               infoMsg));
      }
   }

   public List<String> getErrorMessages()
   {
      return errorMessages;
   }

   public boolean isContainErrorMessages()
   {
      return errorMessages.size() > 0;
   }
   
   public List<String> getInfoMessages()
   {
      return infoMessages;
   }
   
   public boolean isContainInfoMessages()
   {
      return infoMessages.size() > 0;
   }
   
   public boolean isContainsMessage()
   {
      if(CollectionUtils.isNotEmpty(errorMessages) || CollectionUtils.isNotEmpty(infoMessages))
      {
         return true;
      }
      return false;
   }

   public String getStyleClass()
   {
      return styleClass;
   }

   public void setStyleClass(String styleClass)
   {
      this.styleClass = styleClass;
   }

   
}
