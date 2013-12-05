package org.eclipse.stardust.ui.web.modeler.model;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class ModelParticipantJto extends ModelElementJto
{
   // TODO more details
   public String participantType;

   public String parentUUID;

   public String teamLeadFullId = ModelerConstants.TO_BE_DEFINED;
   
   public List<ModelParticipantJto> childParticipants = newArrayList();
}
