/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.rest.dto.LaunchPanelDTO;
import org.eclipse.stardust.ui.web.rest.dto.PerspectiveDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 *
 */
@Component
public class PerspectiveService
{
   /**
    * @return
    */  
   public List<PerspectiveDTO> getPerspectives()
   {
      List<IPerspectiveDefinition> perspectives = PortalApplication.getInstance().getPortalUiController()
            .getAllPerspectives();
      
      String activePerspective = PortalApplication.getInstance().getPortalUiController().getPerspective().getName();
      List<PerspectiveDTO> perspectivesInfo = DTOBuilder.buildList(perspectives, PerspectiveDTO.class);
      for (PerspectiveDTO perspectiveDTO : perspectivesInfo)
      {
         perspectiveDTO.active = perspectiveDTO.name.equals(activePerspective);
         Iterator<LaunchPanelDTO> it = perspectiveDTO.launchPanels.iterator();
         while(it.hasNext())
         {
            LaunchPanelDTO lpDTO = (LaunchPanelDTO)it.next();
            if (lpDTO.include.startsWith("/"))
            {
               lpDTO.include = lpDTO.include.substring(1);
            }
            if (!lpDTO.include.endsWith(".html"))
            {
               it.remove();
            }
         }

      }
      return perspectivesInfo;
   }
}
