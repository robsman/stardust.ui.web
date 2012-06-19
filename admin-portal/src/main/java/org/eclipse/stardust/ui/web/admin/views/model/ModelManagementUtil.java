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
package org.eclipse.stardust.ui.web.admin.views.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;



public class ModelManagementUtil
{
   private static final String STOPPED_DAEMONS = "modelMgmt/stoppedDaemons";

   private ModelManagementUtil()
   {}

   /**
    * method to get editable ConfigurationVariables it removes ConfigurationVariable
    * containing default value or preferences store contain value for same name.
    * 
    * @param allConfigurationVariables
    * @return
    */
   public static List<ConfigurationVariables> getEditableConfigurationVariables(
         List<ConfigurationVariables> allConfigurationVariables) throws Exception
   {
      AdministrationService administrationService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      List<ConfigurationVariables> returnList = new ArrayList<ConfigurationVariables>();

      for (ConfigurationVariables variables : allConfigurationVariables)
      {
         List<ConfigurationVariable> removeList = new ArrayList<ConfigurationVariable>();

         ConfigurationVariables preferenceStoreVars = administrationService.getConfigurationVariables(variables
               .getModelId());

         if ((variables != null) && !variables.isEmpty())
         {
            ConfigurationVariables tempVariables = new ConfigurationVariables(variables.getModelId());
            tempVariables.setConfigurationVariables(new ArrayList<ConfigurationVariable>());

            for (ConfigurationVariable var : variables.getConfigurationVariables())
            {
               tempVariables.getConfigurationVariables().add(var);

               // if configuration variable contains default then don't show on table
               if (StringUtils.isNotEmpty(var.getDefaultValue()))
               {
                  removeList.add(var);
               }
               else if ((preferenceStoreVars != null) && !preferenceStoreVars.getConfigurationVariables().isEmpty())
               {
                  for (ConfigurationVariable prefVar : preferenceStoreVars.getConfigurationVariables())
                  {
                     // if Preference Store contain same name then don't show on table
                     if (var.getName().equals(prefVar.getName()) && StringUtils.isNotEmpty(prefVar.getValue()))
                     {
                        removeList.add(var);

                        break;
                     }
                  }
               }
            }

            // remove Configuration Variable's
            tempVariables.getConfigurationVariables().removeAll(removeList);

            if (!tempVariables.isEmpty())
            {
               returnList.add(tempVariables);
            }
         }
      }

      return returnList;
   }

   /**
    * Start current daemons
    * 
    * @param stoppedDaemons
    */
   public static synchronized void startDaemons(Daemon[] stoppedDaemons, SessionContext sessionContext)
   {
      // SessionContext sessionContext = SessionContext.findSessionContext();
      AdministrationService administrationService = sessionContext.getServiceFactory().getAdministrationService();

      for (Daemon daemon : stoppedDaemons)
      {
         administrationService.startDaemon(daemon.getType(), false);
      }

      sessionContext.bind(STOPPED_DAEMONS, null);
   }

   /**
    * Stops current daemons
    * 
    * @return
    */
   public static synchronized Daemon[] stopDaemons(SessionContext sessionContext)
   {
      List<Daemon> stoppedDaemons = new ArrayList<Daemon>();

      if (sessionContext.isSessionInitialized())
      {
         AdministrationService as = sessionContext.getServiceFactory().getAdministrationService();
         List<Daemon> allDaemons = as.getAllDaemons(false);

         for (Daemon daemon : allDaemons)
         {
            if (daemon.isRunning())
            {
               daemon = as.stopDaemon(daemon.getType(), false);
               stoppedDaemons.add(daemon);
            }
         }
      }

      if (!stoppedDaemons.isEmpty())
      {
         // MessageDialog.addMessage(MessageType.ERROR, "Message", "Daemons stopped");
         sessionContext.bind(STOPPED_DAEMONS, stoppedDaemons);
      }
      else
      {
         Object rawStoppedDaemons = sessionContext.lookup(STOPPED_DAEMONS);

         if (rawStoppedDaemons instanceof Daemon[])
         {
            return (Daemon[]) rawStoppedDaemons;
         }
      }

      Daemon[] daemons = new Daemon[stoppedDaemons.size()];

      return (Daemon[]) stoppedDaemons.toArray(daemons);
   }
}
