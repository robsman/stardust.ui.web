package org.eclipse.stardust.ui.web.reporting.beans.spring;

import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.engine.api.model.Participant;
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
   public List<DeployedModel> getActiveModels();
   public Collection<Participant> getAllParticipants();
   public List<QualifiedModelParticipantInfo> getAllModelParticipants(boolean filterPredefinedModel);
   @SuppressWarnings("rawtypes")
   public Participant getParticipant(String id, Class type);
}
