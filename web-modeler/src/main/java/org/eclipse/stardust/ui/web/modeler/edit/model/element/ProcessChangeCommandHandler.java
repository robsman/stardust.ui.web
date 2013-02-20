/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.edit.model.element;

import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newProcessDefinition;
import static org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants.ID_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.ACTIVITIES_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.ATTRIBUTES_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.CONTROL_FLOWS_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.DATA_FLOWS_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.EVENTS_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.GATEWAYS_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.MODEL_ID_PROPERTY;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.DiagramModeType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrientationType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.edit.utils.CommandHandlerUtils;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

/**
 * @author Shrikant.Gangal
 *
 */
@CommandHandler
public class ProcessChangeCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   @OnCommand(commandId = "process.create")
   public void createProcess(ModelType model, JsonObject request)
   {
      ProcessDefinitionType processDefinition = getModelBuilderFacade().createProcess(model, null, extractString(request, ModelerConstants.NAME_PROPERTY), 
            extractString(request, "defaultLaneName"), extractString(request, "defaultPoolName"));

      // Add process definition to UUID map.
      
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(processDefinition);
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "process.delete")
   public void deleteProcess(ModelType model, JsonObject request)
   {
      String id = extractString(request, ModelerConstants.ID_PROPERTY);
      ProcessDefinitionType processDefinition = getModelBuilderFacade().findProcessDefinition(model, id);
      synchronized (model)
      {
    	  model.getProcessDefinition().remove(processDefinition);
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
