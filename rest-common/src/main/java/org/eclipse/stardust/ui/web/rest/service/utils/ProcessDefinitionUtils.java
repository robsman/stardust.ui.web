/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.utils;

import static java.util.Collections.emptyList;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
public class ProcessDefinitionUtils
{
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;
   
   public static final Logger trace = LogManager.getLogger(ProcessDefinitionUtils.class);

   /**
    * @return
    */
   public List<ProcessDefinition> getStartableProcesses()
   {
      try
      {
         return serviceFactoryUtils.getWorkflowService().getStartableProcessDefinitions();
      }
      catch (ObjectNotFoundException onfe)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("No startable processes: " + onfe.getMessage());
         }

         return emptyList();
      }
   }

   /**
    * return true if the provided Process Definition supports Process Attachments
    * 
    * @param processInstance
    * @return
    */
   public boolean supportsProcessAttachments(ProcessDefinition pd)
   {
      boolean supportsProcessAttachments = false;

      @SuppressWarnings("unchecked")
      List<DataPath> dataPaths = pd.getAllDataPaths();

      for (DataPath dataPath : dataPaths)
      {
         if (DmsConstants.PATH_ID_ATTACHMENTS.equals(dataPath.getId())
               && dataPath.getDirection().equals(Direction.IN))
         {
            supportsProcessAttachments = true;
         }
      }

      return supportsProcessAttachments;
   }

}
