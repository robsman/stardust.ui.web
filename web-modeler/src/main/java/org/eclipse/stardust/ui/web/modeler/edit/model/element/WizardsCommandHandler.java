package org.eclipse.stardust.ui.web.modeler.edit.model.element;

import javax.annotation.Resource;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@CommandHandler
public class WizardsCommandHandler
{
   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelService modelService;

   @OnCommand(commandId = "serviceWrapperProcess.create")
   public void createServiceWrapperProcess(ModelType model, JsonObject request)
   {
      modelService.createWrapperProcess(model.getId(), request);
   }

   @OnCommand(commandId = "processInterfaceTestWrapperProcess.create")
   public void createProcessInterfaceTestWrapperProcess(ModelType model, JsonObject request)
   {
      modelService.createProcessInterfaceTestWrapperProcess(model.getId(), request);
   }
}
