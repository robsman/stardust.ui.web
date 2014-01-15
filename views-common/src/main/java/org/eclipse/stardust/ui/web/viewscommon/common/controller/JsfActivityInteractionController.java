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

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelKind;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalErrorClass;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.utils.JsfBackingBeanUtils;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class JsfActivityInteractionController implements IActivityInteractionController
{
   
   private static final Logger trace = LogManager.getLogger(JsfActivityInteractionController.class);

   public String getContextId(ActivityInstance ai)
   {
      return PredefinedConstants.JSF_CONTEXT;
   }

   public PanelIntegrationStrategy getPanelIntegrationStrategy(ActivityInstance ai)
   {
      Activity activity = ai.getActivity();
      ApplicationContext applicationContext = activity.getApplicationContext(PredefinedConstants.JSF_CONTEXT);

      String kind = (String) applicationContext.getAttribute("jsf:componentKind");

      if (PanelKind.JSP_STANDALONE.getId().equals(kind))
      {
         return PanelIntegrationStrategy.REDIRECT;
      }
      else if (PanelKind.JSP_EMBEDDED.getId().equals(kind))
      {
         return PanelIntegrationStrategy.EMBEDDED_IFRAME;
      }
      else
      {
         return PanelIntegrationStrategy.EMBEDDED_FACELET;
      }
   }

   public void initializePanel(ActivityInstance ai, Map inData)
   {
      boolean panelWasInitialized = false;
      
      Activity activity = ai.getActivity();
      ApplicationContext applicationContext = activity.getApplicationContext(PredefinedConstants.JSF_CONTEXT);
            
      if ( !panelWasInitialized)
      {
         JsfBackingBeanUtils.performBackingBeanInDataMappings(applicationContext, inData);
      }
   }

   public String providePanelUri(ActivityInstance ai)
   {
      ApplicationContext applicationContext = ai.getActivity().getApplicationContext(PredefinedConstants.JSF_CONTEXT);

      return (String) applicationContext.getAttribute("jsf:url");
   }

   public boolean closePanel(ActivityInstance ai, ClosePanelScenario scenario)
   {
      // TODO any preconditions to check?
      return true;
   }

   public Map getOutDataValues(ActivityInstance ai)
   {
      trace.info("Application context jsf");

      Map outData = CollectionUtils.newMap();
      
      ApplicationContext applicationContext = ai.getActivity().getApplicationContext(PredefinedConstants.JSF_CONTEXT);
      try
      {
         outData.putAll(JsfBackingBeanUtils.performBackingBeanOutDataMappings(applicationContext));
      }
      catch (PortalException pe)
      {
         throw new PublicException((null != pe.getErrorClass())
               ? pe.getErrorClass()
               : ProcessPortalErrorClass.FAILED_EVALUATING_OUT_DATA_MAPPING, pe);
      }

      return outData;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController#unregisterInteraction(org.eclipse.stardust.engine.api.runtime.ActivityInstance)
    */
   public boolean unregisterInteraction(ActivityInstance ai)
   {
      return false;
   }

   @Override
   public boolean isTypedDocumentOpen(ActivityInstance activityInstance)
   {
		return false;
   }
}