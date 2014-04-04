package org.eclipse.stardust.ui.web.reporting.beans.spring;

import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;

/**
 * Common interface for accessing model related information
 * Main purposed is to eliminate any web dependencies for plain unit testing
 * Also enables plugging in different implementations if needed
 *
 * @author Holger.Prause
 *
 */
public interface IModelService
{
   /**
    * @return
    */
   public List<DeployedModel> getActiveModels();
   /**
    * @return
    */
   public Collection<Participant> getAllParticipants();
   /**
    * @param filterPredefinedModel
    * @return
    */
   public List<QualifiedModelParticipantInfo> getAllModelParticipants(boolean filterPredefinedModel);
   /**
    * @param id
    * @param type
    * @return
    */
   @SuppressWarnings("rawtypes")
   public Participant getParticipant(String id, Class type);
   /**
    * @param oid
    * @return
    */
   public DeployedModel getModel(long oid);
   
   //Process Definitions
   /**
    * @param filterDuplicateProcesses
    * @param deployedModel
    * @return
    */
   public List<ProcessDefinition> getAllProcessDefinitions(boolean filterDuplicateProcesses, Model deployedModel);
   
}
