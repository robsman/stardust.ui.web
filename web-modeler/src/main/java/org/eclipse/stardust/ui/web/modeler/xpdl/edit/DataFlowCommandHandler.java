package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findContainingActivity;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.xpdl.marshalling.EventMarshallingUtils.isIntermediateEventHost;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EventHandlerType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.common.exception.ModelerErrorClass;
import org.eclipse.stardust.ui.web.modeler.common.exception.ModelerException;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils.CommandHandlerUtils;
import org.eclipse.stardust.ui.web.modeler.xpdl.marshalling.ModelElementUnmarshaller;

@CommandHandler
public class DataFlowCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   @OnCommand(commandId = "dataFlow.create")
   public void createDataFlow(ModelType model, EventHandlerType eventHandler,
         JsonObject changes)
   {
      String dataFullID = extractString(changes, ModelerConstants.DATA_FULL_ID_PROPERTY);

      EObjectUUIDMapper mapper = modelService().uuidMapper();

      DataType data = null;
      try
      {
         try
         {
            // either resolves the data locally or imports it, if needed
            data = getModelBuilderFacade().importData(model, dataFullID);
         }
         catch (IllegalArgumentException e)
         {
            throw new ModelerException(ModelerErrorClass.DATA_ID_ALREADY_EXISTS);
         }

         if (null == mapper.getUUID(data))
         {
            mapper.map(data);
         }
      }
      catch (ObjectNotFoundException x)
      {
      }

      DirectionType mappingDirection = changes.get(ModelerConstants.DIRECTION_PROPERTY)
            .getAsString().equals(ModelerConstants.DATAMAPPING_IN)
            ? DirectionType.IN_LITERAL
            : DirectionType.OUT_LITERAL;

      ActivityType hostActivity = findContainingActivity(eventHandler);
      if (isIntermediateEventHost(hostActivity))
      {
         ModelElementUnmarshaller modelUnmarshaller = (ModelElementUnmarshaller) modelService()
               .findModelBinding(model).getUnmarshaller();

         DataMappingType dataMapping = modelUnmarshaller.createDataMapping(hostActivity,
               data, changes, mappingDirection, changes);

         // associate mapping with event handler
         String context = "event-" + eventHandler.getId();
         dataMapping.setContext(context);
      }
   }

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }
}
