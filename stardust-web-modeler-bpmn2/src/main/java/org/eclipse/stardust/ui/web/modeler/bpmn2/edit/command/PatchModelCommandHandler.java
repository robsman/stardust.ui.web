package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Map;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Definitions;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;

@CommandHandler
public class PatchModelCommandHandler
{
   private static final Logger trace = LogManager.getLogger(PatchModelCommandHandler.class);

   @OnCommand(commandId = "modelElement.update")
   public void patchBpmn2ModelElement(Definitions model, BaseElement targetElememt, JsonObject patch)
   {
      trace.info("About to patch model element " + targetElememt);

      // TODO
   }

   private Map<String, Object> getExtensions(BaseElement element)
   {
      List<Map<String,Object>> extensionAttributes = Bpmn2ExtensionUtils.getExtensionAttributes(element, "blub");
      Map<String, Object> result = emptyMap();
      if ( !extensionAttributes.isEmpty())
      {
         result = extensionAttributes.get(0);
      }
      return result;
   }

   private void setExtensions(BaseElement element, Map<String, String> attribs)
   {
      Bpmn2ExtensionUtils.setExtensionAttributes(element, "blub", attribs);
   }
}
