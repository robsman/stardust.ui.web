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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.processportal.common.Resources;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;


/**
 * @author Subodh.Godbole
 * 
 */
public class WorklistsTreeAssemblyLineUserObject extends WorklistsTreeUserObject
{
   public static Logger trace = LogManager.getLogger(WorklistsTreeAssemblyLineUserObject.class);

   private WorklistsBean worklistsBean;
   private long activityCount;

   /**
    * @param wrapper
    * @param worklistsBean
    */
   public WorklistsTreeAssemblyLineUserObject(DefaultMutableTreeNode wrapper, WorklistsBean worklistsBean)
   {
      super(wrapper);

      this.worklistsBean = worklistsBean;
      calculateActivityCount();

      this.setText(MessagePropertiesBean.getInstance().getString("launchPanels.worklists.assemblyLine.title"));
      this.setLeafIcon(Resources.Icons.getAssemblyLine());
      this.setLeaf(true);
   }

   /**
    * @param event
    */
   public void select(ActionEvent event)
   {
      worklistsBean.openNextAssemblyLineActivity();
      calculateActivityCount();
   }

   /**
    * 
    */
   public void calculateActivityCount()
   {
      try
      {
         if (worklistsBean.isAssemblyLineMode() && null != worklistsBean.getAssemblyLineActivityProvider())
         {
            activityCount = worklistsBean.getAssemblyLineActivityProvider().getAssemblyLineActivityCount(
                  ServiceFactoryUtils.getProcessExecutionPortal(), worklistsBean.getAssemblyLineParticipants());
         }
      }
      catch (Exception e)
      {
         activityCount = 0;
         trace.error("Error occurred calculating Assembly Line Activity Count", e);
      }
   }

   public Set<String> getAssemblyLineParticipants()
   {
      return worklistsBean.getAssemblyLineParticipants();
   }

   public boolean isAssemblyLineMode()
   {
      return worklistsBean.isAssemblyLineMode();
   }
   
   public long getActivityCount()
   {
      return activityCount;
   }
}
