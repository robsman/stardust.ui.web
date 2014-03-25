package org.eclipse.stardust.ui.web.reporting.beans.spring;

import java.util.*;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.ui.web.reporting.common.ModelCache;
import org.eclipse.stardust.ui.web.reporting.core.IModelService;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;

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

}
