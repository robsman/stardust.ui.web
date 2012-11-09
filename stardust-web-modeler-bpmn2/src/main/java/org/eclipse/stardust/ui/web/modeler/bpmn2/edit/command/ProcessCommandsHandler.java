package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import javax.annotation.Resource;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BPMNDiagram;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2CoreElementsBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2DiBuilder;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ProcessDefinitionJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ProcessDiagramJto;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;

@CommandHandler
public class ProcessCommandsHandler
{
   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelService modelService;

   @OnCommand(commandId = "process.create")
   public void createProcess(Definitions model, Definitions context, JsonObject details)
   {
      // create process definition
      ProcessDefinitionJto jto = jsonIo.gson().fromJson(details, ProcessDefinitionJto.class);

      ModelBinding<Definitions> modelBinding = modelService.currentSession().modelRepository().getModelBinding(model);

      Bpmn2CoreElementsBuilder coreElementsBuilder = new Bpmn2CoreElementsBuilder();
      Process processDefinition = coreElementsBuilder.createProcess(model, jto);
      modelBinding.updateModelElement(processDefinition, details);
      coreElementsBuilder.attachProcess(model, processDefinition);

      // create default diagram
      ProcessDiagramJto diagramJto = new ProcessDiagramJto();
      diagramJto.name = "Default";

      Bpmn2DiBuilder diBuilder = new Bpmn2DiBuilder();
      BPMNDiagram diagram = diBuilder.createDiagram(processDefinition, diagramJto);

      diBuilder.attachDiagram(model, diagram);
   }

}
