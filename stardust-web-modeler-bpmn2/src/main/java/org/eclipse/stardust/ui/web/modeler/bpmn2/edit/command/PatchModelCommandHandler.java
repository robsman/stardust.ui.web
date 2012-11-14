package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Definitions;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2ModelUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;

@CommandHandler
public class PatchModelCommandHandler
{
   private static final Logger trace = LogManager.getLogger(PatchModelCommandHandler.class);
   private Bpmn2ModelUnmarshaller unmarshaller;

   
   public PatchModelCommandHandler()
   {
      unmarshaller = new Bpmn2ModelUnmarshaller();
   }

   @OnCommand(commandId = "modelElement.update")
   public void patchBpmn2ModelElement(Definitions model, BaseElement targetElement,
         JsonObject patch)
   {
      trace.info("About to patch model element " + targetElement);

      unmarshaller.populateFromJson(targetElement, patch);
   }
}
