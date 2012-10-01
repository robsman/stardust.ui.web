package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import javax.annotation.Resource;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BPMNDiagram;

import com.google.gson.JsonObject;

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

      Process processDefinition = (Process) modelBinding.createModelElement(model, jto);
      modelBinding.updateModelElement(processDefinition, details);
      modelBinding.attachModelElement(model, processDefinition);

      // create default diagram
      ProcessDiagramJto diagramJto = new ProcessDiagramJto();
      diagramJto.name = "Default";

      BPMNDiagram diagram = (BPMNDiagram) modelBinding.createProcessDiagram(processDefinition, diagramJto);

      modelBinding.attachModelElement(model, diagram);
   }
}
