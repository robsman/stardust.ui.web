package org.eclipse.stardust.ui.web.reporting.beans.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.ui.web.reporting.common.portal.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 *
 */
@Component(value=ModelServiceBean.BEAN_NAME)
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class ModelServiceBean implements IModelService
{
   public static final String BEAN_NAME = "reportingModelService";

   @Resource
   private SessionContext sessionContext;

   @Resource
   private ServletContext servletContext;

   private ModelCache getModelCache()
   {
      return ModelCache.getModelCache(sessionContext, servletContext);
   }

   public ModelServiceBean()
   {
   }

   @Override
   public DeployedModel getModel(long oid)
   {
      return getModelCache().getModel(oid);
   }
   
   @Override
   public List<DeployedModel> getActiveModels()
   {
      return getModelCache().getActiveModels();
   }

   @Override
   public Collection<Participant> getAllParticipants()
   {
      return getModelCache().getAllParticipants();
   }

   @Override
   public List<QualifiedModelParticipantInfo> getAllModelParticipants(
         boolean filterPredefinedModel)
   {
      Collection<DeployedModel> allModels = getActiveModels();
      List<QualifiedModelParticipantInfo> allParticipants = new ArrayList<QualifiedModelParticipantInfo>();
      Set<String> allParticipantQIDs = new HashSet<String>();
      boolean isAdminAdded = false;

      for (DeployedModelDescription model : allModels)
      {
         if (filterPredefinedModel
               && PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
         {
            continue;
         }
         Collection<Participant> participants = getAllParticipants();

         for (Participant participant : participants)
         {
            if (participant instanceof QualifiedModelParticipantInfo)
            {
               boolean isAdminRole = ParticipantUtils.isAdministratorRole(participant);

               // Administrator should be added only once
               if (!isAdminAdded && isAdminRole)
               {
                  allParticipants.add((QualifiedModelParticipantInfo) participant);
                  isAdminAdded = true;
               }
               else if (!isAdminRole)
               {
                  if (!allParticipantQIDs.contains(participant.getQualifiedId()))
                  {
                     allParticipants.add((QualifiedModelParticipantInfo) participant);
                     allParticipantQIDs.add(participant.getQualifiedId());
                  }
               }
            }
         }
      }
      return allParticipants;
   }

   @SuppressWarnings("rawtypes")
   @Override
   public Participant getParticipant(String id, Class type)
   {
      return getModelCache().getParticipant(id, type);
   }

   //Process Definition
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
   @Override
   public List<ProcessDefinition> getAllProcessDefinitions(boolean filterDuplicateProcesses, Model deployedModel)
   {
      List<ProcessDefinition> allProcesses = CollectionUtils.newArrayList();
      List<ProcessDefinition> processes; // filteredProcesses;
      Set<String> processDefinitionQIds = new HashSet<String>();

      List<DeployedModel> models = getActiveModels();

      for (Model model : models)
      {
         if (null == deployedModel || deployedModel.getQualifiedId().equals(model.getQualifiedId()))
         {
            processes = model.getAllProcessDefinitions();
            /*
             * filteredProcesses = doFilterAccess == true ? filterAccessibleProcesses(
             * ServiceFactoryUtils.getWorkflowService(), processes) : processes;
             */
            for (ProcessDefinition processDefinition : processes)
            {
               // check for duplicate process from different versions (active version's
               // processes will override)
               if (!(filterDuplicateProcesses && processDefinitionQIds.contains(processDefinition.getQualifiedId())))
               {
                  allProcesses.add(processDefinition);
               }
               processDefinitionQIds.add(processDefinition.getQualifiedId());
            }
         }
      }
      return allProcesses;
   }   
}
