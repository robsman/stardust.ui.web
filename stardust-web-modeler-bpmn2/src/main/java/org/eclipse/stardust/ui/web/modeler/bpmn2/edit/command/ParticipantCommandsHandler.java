package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.ElementRefUtils.encodeReference;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.ElementRefUtils.resolveElementIdFromReference;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.ElementRefUtils.resolveModelIdFromReference;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Binding;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2CoreElementsBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.edit.TouchedElementsCollector;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ModelParticipantJto;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

import com.google.gson.JsonObject;

@CommandHandler
public class ParticipantCommandsHandler
{
   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelService modelService;

   @Resource
   private TouchedElementsCollector touchedElementsCollector;

   @OnCommand(commandId="role.create")
   public void onCreateRole(Definitions model, JsonObject details)
   {
      ModelParticipantJto jto = jsonIo.gson().fromJson(details, ModelParticipantJto.class);

      jto.type = ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY;
      jto.participantType = ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY;

      Bpmn2CoreElementsBuilder coreElementsBuilder = new Bpmn2CoreElementsBuilder();

      org.eclipse.bpmn2.Resource participant = coreElementsBuilder.createModelParticipant(model, jto);
      coreElementsBuilder.attachToModel(model, participant);
   }

   @OnCommand(commandId="role.create")
   public void onCreateRole(Definitions model, org.eclipse.bpmn2.Resource parentOrg, JsonObject details)
   {
      ModelParticipantJto jto = jsonIo.gson().fromJson(details, ModelParticipantJto.class);

      jto.type = ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY;
      jto.participantType = ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY;

      jto.parentUUID = ((Bpmn2Binding) modelService.findModelBinding(model)).findUuid(model, parentOrg);

      Bpmn2CoreElementsBuilder coreElementsBuilder = new Bpmn2CoreElementsBuilder();

      org.eclipse.bpmn2.Resource participant = coreElementsBuilder.createModelParticipant(model, jto);
      coreElementsBuilder.attachToModel(model, participant);
   }

   @OnCommand(commandId="organization.create")
   public void onCreateOrganization(Definitions model, JsonObject details)
   {
      ModelParticipantJto jto = jsonIo.gson().fromJson(details, ModelParticipantJto.class);

      jto.type = ModelerConstants.ORGANIZATION_PARTICIPANT_TYPE_KEY;
      jto.participantType = ModelerConstants.ORGANIZATION_PARTICIPANT_TYPE_KEY;

      Bpmn2CoreElementsBuilder coreElementsBuilder = new Bpmn2CoreElementsBuilder();

      org.eclipse.bpmn2.Resource participant = coreElementsBuilder.createModelParticipant(model, jto);
      coreElementsBuilder.attachToModel(model, participant);
   }

   @OnCommand(commandId="organization.create")
   public void onCreateOrganization(Definitions model, org.eclipse.bpmn2.Resource parentOrg, JsonObject details)
   {
      ModelParticipantJto jto = jsonIo.gson().fromJson(details, ModelParticipantJto.class);

      jto.type = ModelerConstants.ORGANIZATION_PARTICIPANT_TYPE_KEY;
      jto.participantType = ModelerConstants.ORGANIZATION_PARTICIPANT_TYPE_KEY;

      jto.parentUUID = ((Bpmn2Binding) modelService.findModelBinding(model)).findUuid(model, parentOrg);

      Bpmn2CoreElementsBuilder coreElementsBuilder = new Bpmn2CoreElementsBuilder();

      org.eclipse.bpmn2.Resource participant = coreElementsBuilder.createModelParticipant(model, jto);
      coreElementsBuilder.attachToModel(model, participant);
   }

   @OnCommand(commandId = "organization.updateTeamLeader")
   public void onUpdateTeamLead(Definitions model, org.eclipse.bpmn2.Resource organization, JsonObject details)
   {
      String teamLeaderUUID = extractString(details, ModelerConstants.UUID_PROPERTY);

      JsonObject extJson = Bpmn2ExtensionUtils.getExtensionAsJson(organization, "core");

      String previousTeamLead = extJson.has(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY)
            ? extractString(extJson, ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY)
            : null;

      if ( !isEmpty(teamLeaderUUID))
      {
         EObject newTeamLead = modelService.currentSession().modelRepository()
               .getModelBinding(model).getNavigator()
               .findElementByUuid(model, teamLeaderUUID);
         if (newTeamLead instanceof org.eclipse.bpmn2.Resource)
         {
            extJson.addProperty(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY,
                  encodeReference((org.eclipse.bpmn2.Resource) newTeamLead));
         }
      }
      else
      {
         extJson.remove(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY);
      }

      String newTeamLead = extJson.has(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY)
            ? extractString(extJson, ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY)
            : null;

      // immediately touch new/previous team leads as dirty to get their type flags updated
      // TODO improve to a nicer API
      if ( !isEmpty(previousTeamLead))
      {
         markTeamLeadModified(previousTeamLead);
      }
      if ( !isEmpty(newTeamLead))
      {
         markTeamLeadModified(newTeamLead);
      }

      Bpmn2ExtensionUtils.setExtensionFromJson(organization, "core", extJson);
   }

   private void markTeamLeadModified(String teamLeadFullId)
   {
      String modelId = resolveModelIdFromReference(teamLeadFullId);
      EObject teamLeadModel = modelService.currentSession().modelRepository().findModel(modelId);
      if (null != teamLeadModel)
      {
         EObject teamLead = modelService.currentSession()
               .modelRepository()
               .getModelBinding(teamLeadModel)
               .getNavigator()
               .findElementByUuid(teamLeadModel, resolveElementIdFromReference(teamLeadFullId));

         if (teamLead instanceof org.eclipse.bpmn2.Resource)
         {
            touchedElementsCollector.touchElement(teamLead);
         }
      }
   }

   @OnCommand(commandId="conditionalPerformer.create")
   public void onCreateConditionalPerformer(Definitions model, JsonObject details)
   {
      ModelParticipantJto jto = jsonIo.gson().fromJson(details, ModelParticipantJto.class);

      jto.type = ModelerConstants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE_KEY;
      jto.participantType = ModelerConstants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE_KEY;

      Bpmn2CoreElementsBuilder coreElementsBuilder = new Bpmn2CoreElementsBuilder();

      org.eclipse.bpmn2.Resource participant = coreElementsBuilder.createModelParticipant(model, jto);
      coreElementsBuilder.attachToModel(model, participant);
   }
}
