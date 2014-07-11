package org.eclipse.stardust.ui.web.reporting.beans.spring;

import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * 
 */
@Component(value = ModelServiceBean.BEAN_NAME)
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class ModelServiceBean implements IModelService
{
   public static final String BEAN_NAME = "reportingModelService";

   private ModelCache modelCache;

   public ModelServiceBean()
   {
      modelCache = ModelCache.findModelCache();
   }

   @Override
   public List<DeployedModel> getActiveModels()
   {
      return modelCache.getActiveModels();
   }

   @Override
   public Collection<Participant> getAllParticipants()
   {
      return modelCache.getAllParticipants();
   }

   @Override
   public List<QualifiedModelParticipantInfo> getAllModelParticipants(boolean filterPredefinedModel)
   {
      return ParticipantUtils.getAllModelParticipants(filterPredefinedModel);
   }

   @Override
   public Participant getParticipant(String id, Class type)
   {
      return modelCache.getParticipant(id, type);
   }

   @Override
   public DeployedModel getModel(long oid)
   {
      return modelCache.getModel(oid);
   }

   @Override
   public List<ProcessDefinition> getAllProcessDefinitions(boolean filterDuplicateProcesses, Model deployedModel)
   {
      return ProcessDefinitionUtils.getAllProcessDefinitions();
   }
}
