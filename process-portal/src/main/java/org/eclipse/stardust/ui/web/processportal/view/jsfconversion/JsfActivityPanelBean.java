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
import java.util.List;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.common.form.jsf.DocumentInputController;
import org.eclipse.stardust.ui.common.form.preferences.FormGenerationPreferences;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.processportal.view.manual.ManualActivityForm;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

/**
 * Class supporting JSF Converted Applications
 * @author Subodh.Godbole
 * 
 */
public class JsfActivityPanelBean implements ViewEventHandler
{
   private ManualActivityForm activityForm;

   /**
    * 
    */
   public JsfActivityPanelBean()
   {
      try
      {
         ActivityInstance activityInstance = null;
         View view = PortalApplication.getInstance().getFocusView();
         if (null != view)
         {
            activityInstance = (ActivityInstance) view.getViewParams().get(ActivityInstance.class.getName());
            
            if (null != activityInstance)
            {
               // This field does not have any effect. Preferences set at the time of
               // conversion are already used in generating the sources
               FormGenerationPreferences formPref = new FormGenerationPreferences(1, 1);
      
               activityForm = new ManualActivityForm(formPref, "activityDetailsBean.activityForm", activityInstance,
                     ServiceFactoryUtils.getWorkflowService(), activityInstance.getActivity().getApplicationContext("jsf"));
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
         // Close the document, because it belongs to activity data and needs to close with activity
         // This is irrespective of "close related views flag"
         if(null != activityForm)
         {
            List<DocumentInputController> mappedDocs = activityForm.getDisplayedMappedDocuments(true, true);
            for (DocumentInputController docInputCtrl : mappedDocs)
            {
               docInputCtrl.closeDocument();
            }
         }
         break;
      }
   }

   public void setData(HashMap<String, ? > data)
   {
      activityForm.setData();
   }

   public Object complete()
   {
      return activityForm.retrieveData();
   }

   public ManualActivityForm getActivityForm()
   {
      return activityForm;
   }
}
