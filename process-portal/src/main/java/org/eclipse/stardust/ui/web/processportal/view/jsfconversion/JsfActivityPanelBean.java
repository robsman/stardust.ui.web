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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.ui.common.form.InputController;
import org.eclipse.stardust.ui.common.form.jsf.DocumentInputController;
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
      if (null != activityForm)
      {
         return activityForm.getFormId();
      }
      else
      {
         return "";
      }
   }

   /**
    * @return
    */
   public boolean isFormValidationsPresent()
   {
      if (null != activityForm)
      {
         return activityForm.isFormValidationsPresent();
      }
      else
      {
         return false;
      }
   }

   /**
    * @return
    */
   public Map<String, InputController> getFullPathInputControllerMap()
   {
      if (null != activityForm)
      {
         return activityForm.getFullPathInputControllerMap();
      }
      else
      {
         return null;
      }
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
      if (null != dataMap && null != mapWithListData)
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
   }

   /**
    * This methods saves document if it's not already saved (i.e. if it's freshly uploaded document) 
    * Calling this method in complete() is responsibility of Backing Bean.
    * @param id Data Mapping id
    */
   protected boolean saveDocumentIfRequired(String id)
   {
      Document doc = (Document)getData(id);
      if (null != doc)
      {
         DocumentInputController docInputCtrl = (DocumentInputController)activityForm.getTopLevelInputController(id);
         return docInputCtrl.saveDocument();
      }

      return false;
   }

   /**
    * Converts java.util.Calendar to java.util.Date
    * Calling this method from JSF getter method is responsibility of Backing Bean.
    * @param id Data Mapping id
    * @return
    */
   protected Date getDataAsDate(String id)
   {
      Object data = getData(id);
      if (null != data)
      {
         if (data instanceof Date)
         {
            return (Date)data;
         }
         else if (data instanceof Calendar)
         {
            return ((Calendar)data).getTime();
         }
      }
      return null;
   }

   /**
    * Converts java.util.Date to java.util.Calendar
    * Calling this method from JSF getter method is responsibility of Backing Bean.
    * @param id
    * @return
    */
   protected Calendar getDataAsCalendar(String id)
   {
      Object data = getData(id);
      if (null != data)
      {
         if (data instanceof Date)
         {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date)data);
            return calendar;
         }
         else if (data instanceof Calendar)
         {
            return (Calendar)data;
         }
      }
      return null;
   }

   /**
    * Converts Priority String to Integer
    * Calling this method from JSF getter method is responsibility of Backing Bean.
    * @param id Data Mapping id
    * @return
    */
   protected int getDataAsPriorityIndex(String id)
   {
      try
      {
         Object priorityObj = getData(id);
         if (priorityObj instanceof Integer)
         {
            return (Integer)priorityObj;
         }
         else if (priorityObj instanceof String)
         {
            Integer priority = Integer.parseInt((String)priorityObj);
            return priority;
         }
      }
      catch (Exception e)
      {
      }
      
      return ProcessInstancePriority.NORMAL;
   }

   /**
    * @param id
    * @return
    */
   protected Object getData(String id)
   {
      if (null != activityForm)
      {
         return activityForm.getValue(id);
      }
      else
      {
         return null;
      }
   }

   /**
    * @param id
    * @param value
    */
   protected void setData(String id, Object value)
   {
      if (null != activityForm)
      {
         activityForm.setValue(id, value);
      }
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
