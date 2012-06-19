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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.eclipse.stardust.common.Pair;


public class GlobalPageMessage implements PhaseListener
{
   private static final String sessionToken = "carnot/globalMessages";
   
   
   public void afterPhase(PhaseEvent event)
   {
      // nothing todo
   }

   public void beforePhase(PhaseEvent event)
   {
      if(event.getPhaseId() == PhaseId.RENDER_RESPONSE) 
      {
         FacesContext facesContext = event.getFacesContext();
         restoreMessages(facesContext);
         Object param = facesContext.getExternalContext().getRequestParameterMap().get("loginError");
         if (param != null)
         {
            int errorCode = Integer.parseInt(param.toString());
            if (errorCode == 1)
            {
               facesContext.addMessage(null, new FacesMessage(
                     FacesMessage.SEVERITY_ERROR, Localizer
                           .getString(LocalizerKey.INVALID_LOGIN_CREDENTIALS), null));
            }
         }
         Object errorParam = facesContext.getExternalContext().getRequestParameterMap()
               .get(ExceptionFilter.ERROR_PARAM);
         if (errorParam != null)
         {
            int errorCode = Integer.parseInt(errorParam.toString());
            if (errorCode == 1)
            {
               facesContext.addMessage(null, new FacesMessage(
                     FacesMessage.SEVERITY_ERROR, Localizer
                           .getString(LocalizerKey.ERROR_OCCURED), null));
            }
         }
      }
   }
   
   public static void storeMessage(FacesContext facesContext, FacesMessage message, int roundTrips)
   {
      Map sessionMap = facesContext.getExternalContext().getSessionMap();
      List messages = (List) sessionMap.get(sessionToken);
      if(messages == null)
      {
         messages = new ArrayList();
         sessionMap.put(sessionToken, messages);
      }
      messages.add(new Pair(message, new Integer(roundTrips)));      
   }

   public PhaseId getPhaseId()
   {
      return PhaseId.RENDER_RESPONSE;
   }
   
   private void restoreMessages(FacesContext facesContext) 
   {
      Map sessionMap = facesContext.getExternalContext().getSessionMap();
      List messages = (List)sessionMap.remove(sessionToken);
      List msgForNextTrip = new ArrayList();
      if(messages != null)
      {
         for(Iterator i = messages.iterator(); i.hasNext(); ) 
         {
            Pair pair = (Pair)i.next();
            facesContext.addMessage(null, (FacesMessage)pair.getFirst());
            int trips = ((Integer)pair.getSecond()).intValue() - 1;
            if(trips > 0)
            {
               msgForNextTrip.add(new Pair(pair.getFirst(), new Integer(trips)));
            }
         }
         if(!msgForNextTrip.isEmpty())
         {
            sessionMap.put(sessionToken, msgForNextTrip);
         }
      }
   }

}
