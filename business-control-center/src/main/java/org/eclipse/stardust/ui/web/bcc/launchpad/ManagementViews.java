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
package org.eclipse.stardust.ui.web.bcc.launchpad;

import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;


/**
 * @author Subodh.Godbole
 * 
 */
public class ManagementViews extends AbstractLaunchPanel implements ResourcePaths
{
   /**
    * 
    */
   public ManagementViews()
   {
      super(LP_managementViews);
      setExpanded(true);
   }

   /**
    * 
    */
   public void openAllProcessManagement()
   {
      PortalApplication portalApp = PortalApplication.getInstance();

      openViewIfAvailable(portalApp, V_processOverview);
      openViewIfAvailable(portalApp, V_processSearch);
      openViewIfAvailable(portalApp, V_trafficLight);
   }

   /**
    * 
    */
   public void openAllActivityManagement()
   {
      PortalApplication portalApp = PortalApplication.getInstance();
      
      openViewIfAvailable(portalApp, "activityCriticalityManagerView");
      openViewIfAvailable(portalApp, V_pendingActivitiesView);
      openViewIfAvailable(portalApp, V_completedActivitiesView);
      openViewIfAvailable(portalApp, V_postponedActivitiesView);
      openViewIfAvailable(portalApp, V_strandedActivitiesView);
   }

   /**
    * 
    */
   public void openAllResourceManagement()
   {
      PortalApplication portalApp = PortalApplication.getInstance();

      openViewIfAvailable(portalApp, V_resourceAvailability);
      openViewIfAvailable(portalApp, V_roleAssignment);
      openViewIfAvailable(portalApp, V_deputyTeamMemberView);
      openViewIfAvailable(portalApp, V_resourceLogin);
      openViewIfAvailable(portalApp, V_resourcePerformanceView);
      openViewIfAvailable(portalApp, V_performanceTeamleaderView);
   }

   /**
    * @param portalApp
    * @param viewId
    */
   private void openViewIfAvailable(PortalApplication portalApp, String viewId)
   {
      if (portalApp.isViewAvailable(viewId))
      {
         portalApp.openViewById(viewId, "", null, null, false);
      }
   }

   @Override
   public void update()
   {}
}
