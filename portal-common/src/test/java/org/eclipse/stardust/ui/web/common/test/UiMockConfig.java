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
package org.eclipse.stardust.ui.web.common.test;

import org.eclipse.stardust.ui.web.common.*;

/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class UiMockConfig
{

   private PerspectiveDefinition perspective;
   
   public UiMockConfig()
   {
      this.perspective = new PerspectiveDefinition();
      
      perspective.addMenuSection(new MenuSection("common",
            "/process-portal/menu/common.xhtml", "P", false));
      perspective.addMenuSection(new MenuSection("administration",
            "/process-portal/menu/administration.xhtml", "P", false));

      perspective.addLaunchPanel(new LaunchPanel("overview",
            "/process-portal/outline/overview.xhtml", "P", false));
      perspective.addLaunchPanel(new LaunchPanel("mySharedWorklists",
            "/process-portal/outline/mySharedWorklists.xhtml", "P", false));
      
      perspective.addLaunchPanel(new LaunchPanel("myProcesses",
            "/process-portal/outline/myProcesses.xhtml", "P", false));

      perspective.addToolbarSection(new ToolbarSection("workflowActions",
            "/process-portal/toolbar/workflowActions.xhtml", "P", false));
      
      // Context Portal extensions
      
      MenuExtension cpMenuExt = new MenuExtension();
      cpMenuExt.setAfter("common");
      cpMenuExt.addElement(new MenuSection("documentsMenu", "/context-portal/extensions/process-portal/menu/documentsMenu.xhtml", "P", false));
      
      LaunchpadExtension cpOutlineExt = new LaunchpadExtension();
      cpOutlineExt.setAfter("myProcesses");
      cpOutlineExt.addElement(new LaunchPanel("myFavoriteDocuments", "/context-portal/extensions/process-portal/outline/myDocuments.xhtml", "P", false));
      
      PerspectiveExtension cpPerspExt = new PerspectiveExtension();
      cpPerspExt.addMenuExtension(cpMenuExt);
      cpPerspExt.addLaunchpadExtension(cpOutlineExt);
      
      perspective.addExtension(cpPerspExt);

      // BCC extensions
      
      MenuExtension bccMenuExt = new MenuExtension();
      bccMenuExt.setBefore("administration");
      bccMenuExt.addElement(new MenuSection("bccMenu", "/business-control-center/extensions/process-portal/menu/bccMenu.xhtml", "P", false));
      
      LaunchpadExtension bccOutlineExt = new LaunchpadExtension();
      bccOutlineExt.setAfter("myProcesses");
      bccOutlineExt.addElement(new LaunchPanel("myFavoriteManagementViews",
            "/business-control-center/extensions/process-portal/outline/favoriteViews.xhtml", "P", false));
      bccOutlineExt.addElement(new LaunchPanel("searchManagementViews",
            "/business-control-center/extensions/process-portal/outline/searchViews.xhtml", "P", false));
      
      PerspectiveExtension bccPerspExt = new PerspectiveExtension();
      bccPerspExt.addMenuExtension(bccMenuExt);
      bccPerspExt.addLaunchpadExtension(bccOutlineExt);
      
      perspective.addExtension(bccPerspExt);
   }

   public PerspectiveDefinition getPerspective()
   {
      return perspective;
   }
   
}
