package org.eclipse.stardust.ui.web.modeler.bpmn2.builder;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.createInternalId;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils.setExtensionAttribute;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.Interface;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ProcessType;
import org.eclipse.bpmn2.Resource;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.stardust.ui.web.modeler.model.ApplicationJto;
import org.eclipse.stardust.ui.web.modeler.model.ModelJto;
import org.eclipse.stardust.ui.web.modeler.model.ModelParticipantJto;
import org.eclipse.stardust.ui.web.modeler.model.ProcessDefinitionJto;

public class Bpmn2CoreElementsBuilder
{
   public Definitions createModel(ModelJto jto)
   {
      Definitions model = Bpmn2Utils.bpmn2Factory().createDefinitions();
      model.setName(jto.name);

      model.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.deriveElementIdFromName(jto.name));

      setExtensionAttribute(model, ModelerConstants.UUID_PROPERTY, createInternalId());

      // TODO review, externalize values
      model.setExporter("Eclipse Stardust");
      model.setExporterVersion(CurrentVersion.getVersionName());

      // TODO expression language: JavaScript
      // TODO type language: XSD (this is the default, though)

      // TODO verify URL compatibility of ID
      model.setTargetNamespace("http://eclipse.org/stardust/model/" + model.getId());

      DocumentRoot docRoot = Bpmn2Utils.bpmn2Factory().createDocumentRoot();
      docRoot.setDefinitions(model);

      return model;
   }

   public Process createProcess(Definitions model, ProcessDefinitionJto jto)
   {
      Process process = Bpmn2Utils.bpmn2Factory().createProcess();

      process.setName(jto.name);
      process.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.deriveElementIdFromName(jto.name));

      // apply defaults
      process.setProcessType(ProcessType.PRIVATE);
      process.setIsExecutable(true);

      return process;
   }

   public void attachProcess(Definitions model, Process process)
   {
      assert (null == process.eContainer());

      model.getRootElements().add(process);
   }

   public Interface createApplicationDefinition(Definitions model, ApplicationJto jto)
   {
      Interface application = Bpmn2Utils.bpmn2Factory().createInterface();

      application.setName(jto.name);
      application.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.deriveElementIdFromName(jto.name));

      // apply defaults
      JsonObject appDetails = new JsonObject();
      appDetails.addProperty(ModelerConstants.APPLICATION_TYPE_PROPERTY, jto.applicationType);
      Bpmn2ExtensionUtils.setExtensionFromJson(application, jto.applicationType, appDetails);

      EObject extensionElement = Bpmn2ExtensionUtils.getExtensionElement(application, jto.applicationType);
      if (null != extensionElement)
      {
         application.setImplementationRef(extensionElement);
      }

      return application;
   }

   public Resource createModelParticipant(Definitions model, ModelParticipantJto jto)
   {
      Resource participant = Bpmn2Utils.bpmn2Factory().createResource();

      participant.setName(jto.name);
      participant.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.deriveElementIdFromName(jto.name));

      setExtensionAttribute(participant, "uuid", Bpmn2Utils.createInternalId());

      // apply defaults
      JsonObject participantDetails = new JsonObject();
      participantDetails.addProperty(ModelerConstants.PARTICIPANT_TYPE_PROPERTY, jto.participantType); // TODO participantType?
      if ( !isEmpty(jto.parentUUID))
      {
         participantDetails.addProperty(ModelerConstants.PARENT_UUID_PROPERTY, jto.parentUUID);
      }
      Bpmn2ExtensionUtils.setExtensionFromJson(participant, "core", participantDetails);

      return participant;
   }

   public void attachToModel(Definitions model, RootElement element)
   {
      assert (null == element.eContainer());

      model.getRootElements().add(element);
   }

}
