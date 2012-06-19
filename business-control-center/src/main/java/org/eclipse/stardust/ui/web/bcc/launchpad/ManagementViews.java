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

      portalApp.openViewById(V_processOverview, "", null, null, false);
      portalApp.openViewById(V_processSearch, "", null, null, false);
      portalApp.openViewById(V_trafficLight, "", null, null, false);
   }

   /**
    * 
    */
   public void openAllActivityManagement()
   {
      PortalApplication portalApp = PortalApplication.getInstance();
      
      portalApp.openViewById("activityCriticalityManagerView", "", null, null, false);
      portalApp.openViewById(V_pendingActivitiesView, "", null, null, false);
      portalApp.openViewById(V_completedActivitiesView, "", null, null, false);
      portalApp.openViewById(V_postponedActivitiesView, "", null, null, false);
      portalApp.openViewById(V_strandedActivitiesView, "", null, null, false);
   }

   /**
    * 
    */
   public void openAllResourceManagement()
   {
      PortalApplication portalApp = PortalApplication.getInstance();

      portalApp.openViewById(V_resourceAvailability, "", null, null, false);
      portalApp.openViewById(V_roleAssignment, "", null, null, false);
      portalApp.openViewById(V_resourceLogin, "", null, null, false);
      portalApp.openViewById(V_resourcePerformanceView, "", null, null, false);
      portalApp.openViewById(V_performanceTeamleaderView, "", null, null, false);
   }

   @Override
   public void update()
   {}
}
