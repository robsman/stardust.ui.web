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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * @author Yogesh.Manware;
 * 
 */
public class SelectProcessDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "selectProcessDialog";
   private static final String SELECT = "SELECT";
   private static final String SPECIFY = "SPECIFY";
   private static final String VIEW_ACTIVITY_PANEL = "activityPanel";
   private Long selectedProcess;
   private ICallbackHandler iCallbackHandler;
   private Long specifiedProcess;
   private String selectedOption = SELECT;
   private boolean isSelect = false;
   private List<SelectItem> allProcessDefns;

   /**
    * @return fileUploadAdminDialog object
    */
   public static SelectProcessDialog getInstance()
   {
      return (SelectProcessDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   @Override
   public void initialize()
   {
      //FacesUtils.refreshPage();
      selectedProcess = null;
      specifiedProcess = null;
      List<View> openViews = PortalApplication.getInstance().getOpenViews();
      View view;
      Object activityOid;
      long activityOidLong;
      ActivityInstance actInstance;
      ProcessInstance pi;
      allProcessDefns = new ArrayList<SelectItem>();
      StringBuffer processLabel;
      for (Iterator<View> iterator = openViews.iterator(); iterator.hasNext();)
      {
         view = (View) iterator.next();
         if (VIEW_ACTIVITY_PANEL.equals(view.getName()))
         {
            activityOid = (String) view.getViewParams().get("oid");
            if (null != activityOid && activityOid instanceof String)
            {
               activityOidLong = Long.parseLong(((String) activityOid).trim());
               actInstance = ActivityInstanceUtils.getActivityInstance(activityOidLong);
               pi = actInstance.getProcessInstance();
               processLabel = new StringBuffer(I18nUtils.getProcessName(ProcessDefinitionUtils.getProcessDefinition(pi.getProcessID())));
               processLabel.append(" (#").append(pi.getOID()).append(")");
               allProcessDefns.add(new SelectItem(pi.getOID(), processLabel.toString()));
            }
         }
      }
      if (allProcessDefns.size() > 0)
      {
         isSelect = true;
         selectedOption = SELECT;
      }
      else
      {
         isSelect = false;
         selectedOption = SPECIFY;
      }
   }

   /**
    * @param event
    */
   public void processSelected()
   {
      if (!isSelect)
      {
         selectedProcess = specifiedProcess;
      }
      iCallbackHandler.handleEvent(EventType.APPLY);
      closePopup();
   }

   /**
    * @param event
    */
   public void optionChanged(ValueChangeEvent event)
   {

      if (SELECT.equalsIgnoreCase((String) event.getNewValue()) && (getAvailableProcessDefs().size() > 0))
      {
         isSelect = true;
      }
      else
      {
         isSelect = false;
      }
   }

   public List<SelectItem> getAvailableProcessDefs()
   {
      return allProcessDefns;
   }

   public void setAvailableProcessDefs(List<SelectItem> availableProcessDefs)
   {}

   public Long getSelectedProcess()
   {
      return selectedProcess;
   }

   public void setSelectedProcess(Long selectedProcess)
   {
      this.selectedProcess = selectedProcess;
   }

   public ICallbackHandler getICallbackHandler()
   {
      return iCallbackHandler;
   }

   public void setICallbackHandler(ICallbackHandler callbackHandler)
   {
      iCallbackHandler = callbackHandler;
   }

   public Long getSpecifiedProcess()
   {
      return specifiedProcess;
   }

   public void setSpecifiedProcess(Long specifiedProcess)
   {
      this.specifiedProcess = specifiedProcess;
   }

   public String getSelectedOption()
   {
      return selectedOption;
   }

   public void setSelectedOption(String selectedOption)
   {
      this.selectedOption = selectedOption;
   }

   public boolean isSelect()
   {
      return isSelect;
   }

   public boolean isOpenActivityAvailable()
   {
      return (getAvailableProcessDefs().size() > 0);
   }

   public void setSelect(boolean select)
   {
      this.isSelect = select;
   }
}