package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import javax.annotation.Resource;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.dd.di.DiagramElement;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@CommandHandler
public class PatchModelCommandHandler
{
   private static final Logger trace = LogManager.getLogger(PatchModelCommandHandler.class);

   @Resource
   private ModelService modelService;

   @OnCommand(commandId = "modelElement.update")
   public void patchBpmn2ModelElement(Definitions model, BaseElement targetElement,
         JsonObject patch)
   {
      trace.info("About to patch model element " + targetElement);

      modelService.findModelBinding(model).updateModelElement(targetElement, patch);
   }

   @OnCommand(commandId = "modelElement.update")
   public void patchBpmn2ModelElement(Definitions model, DiagramElement targetElement, JsonObject patch)
   {
      trace.info("About to patch diagram element " + targetElement);

      modelService.findModelBinding(model).updateModelElement(targetElement, patch);
   }
}
