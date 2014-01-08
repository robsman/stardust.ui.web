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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalErrorClass;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class CasabacActivityInteractionController implements IActivityInteractionController
{
   
   private static final Logger trace = LogManager.getLogger(CasabacActivityInteractionController.class);
   
   public static final String CASABAC_CONTEXT = "cad";

   public String getContextId(ActivityInstance ai)
   {
      return CASABAC_CONTEXT;
   }

   public PanelIntegrationStrategy getPanelIntegrationStrategy(ActivityInstance ai)
   {
      // TODO which strategy?
      return PanelIntegrationStrategy.REDIRECT;
   }

   public void initializePanel(ActivityInstance ai, Map inData)
   {
      ApplicationContext applicationContext = ai.getActivity().getApplicationContext(
            CASABAC_CONTEXT);
      
      for (Iterator iterator = applicationContext.getAllInDataMappings().iterator(); iterator.hasNext();)
      {
         DataMapping mapping = (DataMapping) iterator.next();
         String mappingID = mapping.getId();
         Object value = inData.get(mappingID);

         if (trace.isDebugEnabled())
         {
            trace.debug("Value " + value + " retrieved for mapping " + mappingID);
         }
      }
   }

   public String providePanelUri(ActivityInstance ai)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public boolean closePanel(ActivityInstance ai, ClosePanelScenario scenario)
   {
      // TODO any preconditions to check?
      return true;
   }

   public Map getOutDataValues(ActivityInstance ai)
   {
      trace.info("Application context cad");

      try
      {
         Object targetObject = null;
         trace.info("Target object " + targetObject);

         // TODO completion methods with parameters

         String methodName = "complete";

         Method completionMethod = targetObject.getClass().getMethod(methodName, (Class[]) null);

         completionMethod.invoke(targetObject, (Object[]) null);

         return null;
      }
      catch (Exception e)
      {
         throw new PublicException(
               ProcessPortalErrorClass.FAILED_INVOKING_COMPLETION_METHOD, e);
      }
   }

   @Override
   public boolean isTypedDocumentOpen(ActivityInstance activityInstance)
   {
      return false;
   }

}
