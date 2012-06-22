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
package org.eclipse.stardust.ui.web.processportal.view.jsfconversion;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.common.form.InputController;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.util.SessionRendererHelper;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.processportal.view.manual.ManualActivityForm;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

/**
 * Class supporting JSF Converted Applications
 * @author Subodh.Godbole
 * 
 */
public class JsfActivityPanelBean implements IJsfActivityPanelBean, ViewEventHandler
{
   protected ManualActivityForm activityForm;
   private String sessionRendererId;

   /**
    * 
    */
   public JsfActivityPanelBean()
   {
      try
      {
         View view = PortalApplication.getInstance().getFocusView();
         if (null != view)
         {
            ActivityInstance activityInstance = (ActivityInstance) view.getViewParams().get(ActivityInstance.class.getName());
            
            if (null != activityInstance)
            {
               activityForm = new ManualActivityForm(null, "activityDetailsBean.activityForm", activityInstance,
                     ServiceFactoryUtils.getWorkflowService(), activityInstance.getActivity().getApplicationContext("jsf"));

               setSessionRendererId(activityInstance);
               if (StringUtils.isNotEmpty(sessionRendererId))
               {
                  SessionRendererHelper.addCurrentSession(sessionRendererId);
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new PublicException(e);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      switch (event.getType())
      {
      case CLOSED:
         if (StringUtils.isNotEmpty(sessionRendererId))
         {
            SessionRendererHelper.removeCurrentSession(sessionRendererId);
         }
         break;
      }
   }

   /**
    * @param activityInstance
    */
   private void setSessionRendererId(ActivityInstance activityInstance)
   {
      sessionRendererId = SessionRendererHelper.getPortalSessionRendererId(PortalApplication.getInstance().getLoggedInUser());
      sessionRendererId += ":jsf-" + activityInstance.getOID();
   }

   /**
    * @return
    */
   public String getFormId()
   {
      return activityForm.getFormId();
   }

   /**
    * @return
    */
   public boolean isFormValidationsPresent()
   {
      return activityForm.isFormValidationsPresent();
   }

   /**
    * @return
    */
   public Map<String, InputController> getFullPathInputControllerMap()
   {
      return activityForm.getFullPathInputControllerMap();
   }

   /**
    * For JSF Apps - The List data is maintained with activityForm, where as rest Structure data is maintained with Backing Bean
    * This method merges/copies List data to map maintained with Backing Bean.
    * Calling this method in complete() is responsibility of Backing Bean.
    * @param dataMap
    * @param mapWithListData
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   protected void mergeDataForList(Map<String, Object> dataMap, Map<String, Object> mapWithListData)
   {
      for (Entry<String, Object> entry : dataMap.entrySet())
      {
         if (entry.getValue() instanceof List)
         {
            // For now only one level of list data is supported on UI. Hence this does not need recursion
            entry.setValue(mapWithListData.get(entry.getKey()));
         }
         else if (entry.getValue() instanceof Map)
         {
            mergeDataForList((Map)entry.getValue(), (Map)mapWithListData.get(entry.getKey()));
         }
      }
   }

   /**
    * @param id
    * @return
    */
   protected Object getData(String id)
   {
      return activityForm.getValue(id);
   }

   /**
    * @param id
    * @param value
    */
   protected void setData(String id, Object value)
   {
      activityForm.setValue(id, value);
   }

   public ManualActivityForm getActivityForm()
   {
      return activityForm;
   }

   /**
    * This method is called by the framework to render the JSF Activity Panel Session
    * @return
    */
   public String getSessionRendererId()
   {
      return sessionRendererId;
   }
}
