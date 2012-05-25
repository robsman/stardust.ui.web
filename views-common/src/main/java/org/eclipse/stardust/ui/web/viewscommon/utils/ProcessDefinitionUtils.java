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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ConditionalPerformer;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.DataQuery;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery;
import org.eclipse.stardust.engine.api.runtime.DataQueryResult;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.ProcessDefinitions;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.compatibility.extensions.dms.DmsConstants;
import org.eclipse.stardust.engine.core.runtime.utils.PermissionHelper;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUser;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUserProvider;



public class ProcessDefinitionUtils
{
   public static final Logger trace = LogManager.getLogger(ProcessDefinitionUtils.class);
   public static final String PROCESS_IS_AUXILIARY_ATT = "isAuxiliaryProcess";  

   public static final Comparator<ProcessDefinition> PROCESS_ORDER = new Comparator<ProcessDefinition>()
   {
      public int compare(ProcessDefinition process1, ProcessDefinition process2)
      {
         return I18nUtils.getProcessName(process1).compareTo(I18nUtils.getProcessName(process2));
      }
   };
   
   private ProcessDefinitionUtils()
   {
      // Utility class
   }

   public static boolean isWithAttachments(ProcessDefinition process)
   {
      boolean result = false;

      DataPath path = getDataPath(process, DmsConstants.PATH_ID_ATTACHMENTS, Direction.IN);

      if (null != path)
      {
         Class pathType = path.getMappedType();
         result = (null != pathType) && List.class.isAssignableFrom(pathType);
      }

      return result;
   }



   public static DataPath getDataPath(ProcessDefinition process, String dataPathId, Direction direction)
   {
      DataPath result = null;

      if (null != process)
      {
         for (Iterator<DataPath> i = process.getAllDataPaths().iterator(); i.hasNext();)
         {
            DataPath path = (DataPath) i.next();
            if (dataPathId.equals(path.getId())
                  && (Direction.IN_OUT.equals(direction) || direction.equals(path.getDirection())))
            {
               result = path;
               break;
            }
         }
      }

      return result;
   }

   public static List<ProcessDefinition> filterAccessibleProcesses(WorkflowService ws, List<ProcessDefinition> processes)
   {
      PermissionHelper permissionHelper = ((IppUser) IppUserProvider.getInstance().getUser()).getPermissionHelper();
      return permissionHelper.filterProcessAccess(ws, processes);
   }

   /**
    * @return
    */
   public static List<ProcessDefinition> getAllAccessibleProcessDefinitionsfromAllVersions()
   {
      return getAllProcessDefinitions(true, false, false);
   }

   /**
    * @return
    */
   public static List<ProcessDefinition> getStartableProcesses()
   {
      try
      {
         return ServiceFactoryUtils.getWorkflowService().getStartableProcessDefinitions();
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
    * @param processId
    * @return
    */
   public static ProcessDefinition getProcessDefinition(String processId)
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
   public static ProcessDefinition getProcessDefinition(long modelOid, String processId)
   {
      ModelCache modelCache = ModelCache.findModelCache();
      Model model = modelCache.getModel(modelOid);
      return model != null ? model.getProcessDefinition(processId) : null;
   }

   public static boolean isAuxiliaryProcess(ProcessDefinition processDefinition)
   {
      Boolean auxiliaryAttr = false;
      Object attr = processDefinition.getAttribute(PROCESS_IS_AUXILIARY_ATT);

      if (attr instanceof Boolean)
      {
         auxiliaryAttr = (Boolean) attr;
      }
      else if (attr instanceof String && !StringUtils.isEmpty((String) attr))
      {
         auxiliaryAttr = Boolean.valueOf((String) attr);
      }
      return auxiliaryAttr;
   }

   /**
    * @return
    */
   public static List<ProcessDefinition> getAllProcessDefinitions()
   {
      return getAllProcessDefinitions(false, false, true);
   }

   /**
    * @return
    */
   public static List<ProcessDefinition> getAllAccessibleProcessDefinitions()
   {
      return getAllProcessDefinitions(true, false, true);
   }

   /**
    * 
    * @return Business relevant processes
    */
   public static List<ProcessDefinition> getAllBusinessRelevantProcesses()
   {
      return getAllProcessDefinitions(true, true, true);
   }

   
   /**
    * @param activeModel
    * @param filterAuxiliaryProcesses
    * @return
    */
   public static List<ProcessDefinition> getAllProcessDefinitions(Model activeModel,
         boolean filterAuxiliaryProcesses)
   {
      return getAllProcessDefinitions(true, filterAuxiliaryProcesses, true, activeModel);
   }
   
   
   /**
    * This method returns ALL accessible process definitions across ALL model versions
    * 
    * 
    * @param doFilterAccess
    * @param filterAuxiliaryProcesses
    * @param filterDuplicateProcesses
    *           (across model versions)
    * @return
    */
   private static List<ProcessDefinition> getAllProcessDefinitions(boolean doFilterAccess,
         boolean filterAuxiliaryProcesses, boolean filterDuplicateProcesses)
   {
      return getAllProcessDefinitions(doFilterAccess, filterAuxiliaryProcesses, filterDuplicateProcesses, null);
   }

   /**
    * Returns all processes from all models or from provided model
    * 
    * @param doFilterAccess
    * @param filterAuxiliaryProcesses
    * @param filterDuplicateProcesses
    * @param deployedModel
    * @return
    */
   @SuppressWarnings("unchecked")
   private static List<ProcessDefinition> getAllProcessDefinitions(boolean doFilterAccess,
         boolean filterAuxiliaryProcesses, boolean filterDuplicateProcesses, Model deployedModel)
   {
      List<ProcessDefinition> allProcesses = CollectionUtils.newArrayList();
      List<ProcessDefinition> processes, filteredProcesses;
      Set<String> processDefinitionQIds = new HashSet<String>();

      List<DeployedModel> models = ModelUtils.getAllModelsActiveFirst();
      
      for (Model model : models)
      {
         if (null == deployedModel || deployedModel.getQualifiedId().equals(model.getQualifiedId()))
         {
            processes = model.getAllProcessDefinitions();
            filteredProcesses = doFilterAccess == true ? filterAccessibleProcesses(
                  ServiceFactoryUtils.getWorkflowService(), processes) : processes;
            for (ProcessDefinition processDefinition : filteredProcesses)
            {
               // check for duplicate process from different versions (active version's
               // processes will override)
               if (!(filterDuplicateProcesses && processDefinitionQIds.contains(processDefinition.getQualifiedId())))
               {
                  // check for Auxiliary Processes
                  if (!(filterAuxiliaryProcesses && ProcessDefinitionUtils.isAuxiliaryProcess(processDefinition)))
                  {
                     allProcesses.add(processDefinition);
                  }
               }
               processDefinitionQIds.add(processDefinition.getQualifiedId());
            }
         }
      }
      return allProcesses;
   }
   
   /**
    * @return method return SelectItem list with unique process names by qualified id
    */
   public static List<SelectItem> getAllUniqueProcessDefinitionItems()
   {

      List<SelectItem> pdItems = CollectionUtils.newArrayList();
      List<ProcessDefinition> pdList = getAllAccessibleProcessDefinitions();    
      
      Set<String> processFQIDSet = CollectionUtils.newHashSet();
      for (ProcessDefinition pd : pdList)
      {
         String qualifiedId = pd.getQualifiedId();
         if (!processFQIDSet.contains(qualifiedId))
         {
            pdItems.add(new SelectItem(pd.getQualifiedId(), I18nUtils.getProcessName(pd)));
            processFQIDSet.add(qualifiedId);
         }
      }
      // sort process in ascending order
      Collections.sort(pdItems, IceComponentUtil.SELECT_ITEM_ORDER);
      
      return pdItems;
   }

   /**
    * @return
    */
   public static List<ProcessDefinition> getProcessDefinitions_forUser()
   {
      // TODO modelcache interface getAllModels()
      // ProcessPortalContext.getCurrentInstance().getClientContext().getModelCache();

      Map<String, ProcessDefinition> map = new TreeMap<String, ProcessDefinition>();
      Iterator<DeployedModel> modelIter = ModelCache.findModelCache().getAllModels().iterator();
      User currentUser = ServiceFactoryUtils.getSessionContext().getUser();
      Set<String> workshopParticipants = Collections.emptySet();
      if (currentUser != null)
      {
         workshopParticipants = WorklistUtils.categorizeParticipants(currentUser).workshopParticipants;
      }
      List<ProcessDefinition> pdList = null;
      WorkflowService ws = ServiceFactoryUtils.getWorkflowService();
      while (modelIter.hasNext())
      {
         Model model = modelIter.next();
         pdList = ProcessDefinitionUtils.filterAccessibleProcesses(ws, model.getAllProcessDefinitions());
         for (ProcessDefinition pd : pdList)
         {
            if (!map.containsKey(pd.getQualifiedId()) && hasProcessPerformingActivity(pd, workshopParticipants))
            {
               map.put(pd.getQualifiedId(), pd);
            }
         }
      }
      return new ArrayList<ProcessDefinition>(map.values());
   }

   /**
    * @param pd
    * @param participants
    * @return
    */
   private static boolean hasProcessPerformingActivity(ProcessDefinition pd, Set participants)
   {
      Iterator<Activity> aIter = pd.getAllActivities().iterator();
      while (aIter.hasNext())
      {
         Activity activity = aIter.next();
         if (activity.isInteractive())
         {
            ModelParticipant performer = activity.getDefaultPerformer();
            if (performer instanceof ConditionalPerformer || participants.contains(performer.getId()))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * returns startable processes with support process attachment or or contains DMS
    * document type data in all active models
    * 
    * @return
    */
   public static List<ProcessDefinition> getStartableProcessSupportAttachmentInActiveModels()
   {
      List<ProcessDefinition> processDefinitions = CollectionUtils.newArrayList();
      List<DeployedModel> models = ModelCache.findModelCache().getActiveModels();
      for (DeployedModel model : models)
      {
         processDefinitions.addAll(getStartableProcessSupportAttachment(model.getModelOID()));
      }
      return processDefinitions;
   }

   /**
    * returns This should show the startable processes for the user (associated with a Manual Trigger) 
    * and which support Process Attachments or reference Document Data (via datapaths, datamappings, triggers).
    * 
    * @return
    */
   public static List<ProcessDefinition> getStartableProcessSupportAttachment(long modelOID)
   {
      DeployedModel model = ModelUtils.getModel(modelOID);
      List<ProcessDefinition> processDefinitions = CollectionUtils.newArrayList();

      ProcessDefinitions pds = ServiceFactoryUtils.getQueryService().getProcessDefinitions(
            ProcessDefinitionQuery.findStartable(modelOID));
      if (CollectionUtils.isNotEmpty(pds))
      {
         for (ProcessDefinition pd : pds)
         {
            if (ProcessDefinitionUtils.isWithAttachments(pd))
            {
               processDefinitions.add(pd);
            }
            else
            // check for dms data support
            {
               boolean isContainDmsDocument = isProcessContainsDmsData(model, pd.getId());
               if (isContainDmsDocument)
               {
                  processDefinitions.add(pd);
               }
            }
         }
      }
      return processDefinitions;

   }

   /**
    * 
    * @param model
    * @param processDefinationFQID
    * @return
    */
   public static boolean isProcessContainsDmsData(DeployedModel model, String processDefinationFQID)
   {
      QueryService QueryService= ServiceFactoryUtils.getQueryService();
      DataQuery dataQuery=DataQuery.findUsedInProcessHavingDataType(Long.valueOf(model.getModelOID()).longValue(), processDefinationFQID, org.eclipse.stardust.engine.extensions.dms.data.DmsConstants.DATA_TYPE_DMS_DOCUMENT); 
      DataQueryResult result = QueryService.getAllData(dataQuery);
      return !result.isEmpty();
   }

   /**
    * 
    * @param dataTypeId
    * @return
    */
   public static boolean isDmsDocumentData(String dataTypeId)
   {
      return org.eclipse.stardust.engine.extensions.dms.data.DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId);
   }

   /**
    * 
    * @return ProcessDefinition of all active models
    */
   public static List<ProcessDefinition> getAllProcessDefinitionsOfActiveModels()
   {
      List<ProcessDefinition> processDefinitions = CollectionUtils.newArrayList();
      for (DeployedModel model : ModelUtils.getActiveModels())
      {
         processDefinitions.addAll(model.getAllProcessDefinitions());
      }
      return processDefinitions;
   }  

   /**
    * 
    * @param processId
    * @return
    */
   public static boolean isCaseProcess(String processId)
   {      
      return PredefinedConstants.CASE_PROCESS_ID.equals(processId) ? true : false;
   }

   /**
    * Sorts the specified list of ProcessDefinition by their i18n name
    * 
    * @param processDefinitions
    */
   public static void sort(List<ProcessDefinition> processDefinitions)
   {
      Collections.sort(processDefinitions, new ProcessDefinitionComparator());
   }

   /**
    * Comparator to sort ProcessDefinitions by name
    * 
    * @author Vikas.Mishra
    * @version $Revision: $
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
