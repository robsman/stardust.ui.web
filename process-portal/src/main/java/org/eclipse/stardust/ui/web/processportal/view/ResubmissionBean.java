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
package org.eclipse.stardust.ui.web.processportal.view;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.model.EventHandler;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.EventHandlerBinding;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.extensions.conditions.timer.TimeStampCondition;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.common.PopupUIViewComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.services.ContextPortalServices;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ResubmissionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class ResubmissionBean extends PopupUIViewComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(ResubmissionBean.class);

   private ActivityInstance activityInstance;
   private ICallbackHandler callbackHandler;

   private Date resubmissionDate;
   private String note;
   private boolean addNote;
   
   private String validationMessage;

   /**
    * 
    */
   public ResubmissionBean()
   {
   }

   /**
    * @return
    */
   public static ResubmissionBean getCurrent()
   {
      return (ResubmissionBean)FacesUtils.getBeanFromContext("resubmissionBean");
   }

   /**
    * @param activityInstance
    * @return
    */
   public static boolean isResubmissionActivity(ActivityInstance activityInstance)
   {
      EventHandler handler = ActivityUtils.getEventHandler(activityInstance,
            TimeStampCondition.class.getName(), "Resubmission");
      return null != handler;
   }

   @Override
   public void initialize()
   {
      validationMessage = "";
      addNote = false;
      try
      {
         resubmissionDate = ResubmissionUtils.getResubmissionDate(activityInstance, ContextPortalServices
               .getWorkflowService());
         note = "";
      }
      catch(Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   @Override
   public void openPopup()
   {
      initialize();
      super.openPopup();
   }
   
   @Override
   public void closePopup()
   {
      if(callbackHandler != null)
      {
         callbackHandler.handleEvent(EventType.CANCEL);
      }      
      super.closePopup();
   }
   
   @Override
   public void apply()
   {
      if(resubmissionDate.before(Calendar.getInstance().getTime()))
      {
         validationMessage = MessagePropertiesBean.getInstance().getString(
               "popups.resubmission.message.dateGreaterThanNow");
      }
      else
      {
         try
         {
            performResubmission();
            super.closePopup();
            
            if(callbackHandler != null)
            {
               callbackHandler.handleEvent(EventType.APPLY);
            }
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
   }

   /**
    * 
    */
   private void performResubmission()
   {
      //TODO: Add validation from org.eclipse.stardust.ui.web.jsf.processportal.beans.ActivityInstanceDialogBean
      //validateActivityInstance()

      WorkflowService ws = ContextPortalServices.getWorkflowService();
      if ((null != activityInstance) && (null != resubmissionDate) && ws != null)
      {
         EventHandlerBinding binding = ws.getActivityInstanceEventHandler(
               activityInstance.getOID(), "Resubmission");

         if (trace.isDebugEnabled())
         {
            trace.debug("Setting Resubmission date/time: " + resubmissionDate.getTime());
         }
         binding.setAttribute(PredefinedConstants.TARGET_TIMESTAMP_ATT, new Long(
               resubmissionDate.getTime()));
         
         if (binding.isBound())
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Setting UnbindActivityEventHandler for OID " + activityInstance.getOID());
            }
            ws.unbindActivityEventHandler(activityInstance.getOID(), "Resubmission");
         }
         
         if (trace.isDebugEnabled())
         {
            trace.debug("Setting BindActivityEventHandler for OID " + activityInstance.getOID() );
         }
         ws.bindActivityEventHandler(activityInstance.getOID(), binding);
         
         if (addNote && !StringUtils.isEmpty(note))
         {
            try
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Adding Resubmission Note for OID " + activityInstance.getOID());
               }
               ProcessInstance processInstance = ProcessInstanceUtils.getProcessInstance(activityInstance);
               ProcessInstanceAttributes attributes = processInstance.getAttributes();
               attributes.addNote(note, ContextKind.ProcessInstance, processInstance.getOID());
               ServiceFactoryUtils.getWorkflowService()
                     .setProcessInstanceAttributes(attributes);
            }
            catch (Exception pe)
            {
               trace.error("Unable to set Notes of Activity Instance OID: "
                     + activityInstance != null ? activityInstance.getOID() : "",
                     pe);
            }
         }
      }
   }

   /**
    * @return
    */
   public Date getResubmissionDate()
   {
      if (resubmissionDate == null)
      {
         try
         {
            ActivityInstance ai = getCurrentActivityInstance();
            if (null != ai)
            {
               EventHandler handler = ActivityUtils.getEventHandler(ai,
                     TimeStampCondition.class.getName(), "Resubmission");

               if (null != handler)
               {
                  Object delay = handler
                        .getAttribute(PredefinedConstants.TIMER_PERIOD_ATT);
                  if (delay instanceof Period)
                  {
                     Calendar now = Calendar.getInstance();
                     Calendar then = ((Period) delay).add(now);
                     return then.getTime();
                  }
               }
            }
         }
         catch (Exception e)
         {
         }
      }
      
      if(resubmissionDate == null)
         resubmissionDate = Calendar.getInstance().getTime();

      return resubmissionDate;   
   }

   public ActivityInstance getActivityInstance()
   {
      return activityInstance;
   }

   public void setActivityInstance(ActivityInstance activityInstance)
   {
      this.activityInstance = activityInstance;
   }

   public void setCallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }
   
   public void setResubmissionDate(Date resubmissionDate)
   {
      this.resubmissionDate = resubmissionDate;
   }

   public String getNote()
   {
      return note;
   }

   public void setNote(String note)
   {
      this.note = note;
   }
   
   public boolean isAddNote()
   {
      return addNote;
   }

   public void setAddNote(boolean addNote)
   {
      this.addNote = addNote;
   }

   public String getValidationMessage()
   {
      return validationMessage;
   }
}
