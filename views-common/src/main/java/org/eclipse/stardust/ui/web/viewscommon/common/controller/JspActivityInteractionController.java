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
package org.eclipse.stardust.ui.web.viewscommon.common.controller;

import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class JspActivityInteractionController
      implements IActivityInteractionController
{
   
   private static final Logger trace = LogManager.getLogger(JspActivityInteractionController.class);

   public String getContextId(ActivityInstance ai)
   {
      return PredefinedConstants.JSP_CONTEXT;
   }

   public PanelIntegrationStrategy getPanelIntegrationStrategy(ActivityInstance ai)
   {
      return PanelIntegrationStrategy.REDIRECT;
   }

   public void initializePanel(ActivityInstance ai, Map inData)
   {
      HttpSession session = (HttpSession)FacesContext.getCurrentInstance().
         getExternalContext().getSession(false);
      
      ApplicationContext applicationContext = ai.getActivity().getApplicationContext(PredefinedConstants.JSP_CONTEXT);

      for (Iterator i = applicationContext.getAllInDataMappings().iterator(); 
            i.hasNext() && session != null;)
      {
         DataMapping mapping = (DataMapping) i.next();
         String mappingID = mapping.getId();
         Object value = inData.get(mappingID);

         if (trace.isDebugEnabled())
         {
            trace.debug("Value " + value + " retrieved for mapping " + mappingID);
         }

         session.setAttribute(mappingID, value);
      }
   }

   public String providePanelUri(ActivityInstance ai)
   {
      ApplicationContext applicationContext = ai.getActivity().getApplicationContext(
            PredefinedConstants.JSP_CONTEXT);

      return (String) applicationContext.getAttribute(PredefinedConstants.HTML_PATH_ATT);
   }

   public boolean closePanel(ActivityInstance ai, ClosePanelScenario scenario)
   {
      // TODO any preconditions to check?
      return true;
   }

   public Map getOutDataValues(ActivityInstance ai)
   {
      trace.info("JSP Application");
      HttpSession webSession = (HttpSession)FacesContext.getCurrentInstance().
         getExternalContext().getSession(false);

      ApplicationContext applicationContext = ai.getActivity().getApplicationContext(
            PredefinedConstants.JSP_CONTEXT);

      Map outData = CollectionUtils.newMap();
      for (Iterator iterator = applicationContext.getAllOutDataMappings()
            .iterator(); iterator.hasNext() && webSession != null;)
      {
         DataMapping mapping = (DataMapping) iterator.next();
         String mappingID = mapping.getId();
         outData.put(mappingID, webSession.getAttribute(mappingID));
         webSession.removeAttribute(mappingID);
      }         

      return outData;
   }

   @Override
   public boolean isTypedDocumentOpen(ActivityInstance activityInstance)
   {
      return false;
   }

}
