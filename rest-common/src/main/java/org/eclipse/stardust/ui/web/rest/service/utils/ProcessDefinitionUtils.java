/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.utils;

import static java.util.Collections.emptyList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.springframework.stereotype.Component;

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
   
   /**
    * @param processId
    * @return
    */
   public ProcessDefinition getProcessDefinition(String processId)
   {
      ProcessDefinition processDefinition = null;

      List<DeployedModel> models = CollectionUtils.newList(ModelUtils.getAllModelsActiveFirst());
      for (Model model : models)
      {
         processDefinition = model.getProcessDefinition(processId);
         if (processDefinition != null)
         {
            break;
         }
      }

      return processDefinition;
   }

   /**
    * returns process definition
    * 
    * @param modelOid
    * @param processId
    * @return
    */
   public ProcessDefinition getProcessDefinition(long modelOid, String processId)
   {
      ModelCache modelCache = ModelCache.findModelCache();
      Model model = modelCache.getModel(modelOid);
      return model != null ? model.getProcessDefinition(processId) : null;
   }

   /**
    * @param onlyFilterable
    * @return
    */
   public static Map<String, DataPath> getAllDescriptors(Boolean onlyFilterable)
   {
      return CommonDescriptorUtils.getAllDescriptors(onlyFilterable);
   }
   
   /**
    * 
    * @param processId
    * @return
    */
   public boolean isCaseProcess(String processId)
   {      
      return PredefinedConstants.CASE_PROCESS_ID.equals(processId) ? true : false;
   }
   
   /**
    * Sorts the specified list of ProcessDefinition by their i18n name
    * 
    * @param processDefinitions
    */
   public void sort(List<ProcessDefinition> processDefinitions)
   {
      Collections.sort(processDefinitions, new ProcessDefinitionComparator());
   }

   /**
    * Comparator to sort ProcessDefinitions by name
    * 
    */
   private static class ProcessDefinitionComparator implements Comparator<ProcessDefinition>
   {
      public int compare(ProcessDefinition pd1, ProcessDefinition pd2)
      {
         String pd1Name = I18nUtils.getProcessName(pd1);
         String pd2Name = I18nUtils.getProcessName(pd2);
         return pd1Name.compareTo(pd2Name);
      }
   }

}
