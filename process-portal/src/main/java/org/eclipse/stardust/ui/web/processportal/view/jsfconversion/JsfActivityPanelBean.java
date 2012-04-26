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

import java.util.HashMap;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.common.form.preferences.FormGenerationPreferences;
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
public class JsfActivityPanelBean implements ViewEventHandler
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
               // This field does not have any effect. Preferences set at the time of
               // conversion are already used in generating the sources
               FormGenerationPreferences formPref = new FormGenerationPreferences(1, 1);
      
               activityForm = new ManualActivityForm(formPref, "activityDetailsBean.activityForm", activityInstance,
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
   
   public void setData(HashMap<String, ? > data)
   {
      activityForm.setData();
   }

   /**
    * This is default 'complete' method called by framework
    * If implementation changes the name of 'complete' method then the same method must be implemented
    * And this method can be called to retrieve Out Data in there
    * @return
    */
   public Object complete()
   {
      return activityForm.retrieveData();
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
