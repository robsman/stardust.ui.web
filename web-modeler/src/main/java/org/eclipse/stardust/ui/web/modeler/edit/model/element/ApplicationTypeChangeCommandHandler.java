/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.edit.model.element;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.ModelElementEditingUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.edit.utils.CommandHandlerUtils;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

/**
 * @author Shrikant.Gangal
 */
@CommandHandler
public class ApplicationTypeChangeCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   @OnCommand(commandId = "webServiceApplication.create")
   public void createWebServiceApp(ModelType model, JsonObject request)
   {
      String applicationName = extractString(request, ModelerConstants.NAME_PROPERTY);

      ApplicationType applicationType = getModelBuilderFacade().createApplication(model,
            null, applicationName, ModelerConstants.WEB_SERVICE_APPLICATION_TYPE_ID);

      // Map newly created application to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(applicationType);
   }

   @OnCommand(commandId = "messageTransformationApplication.create")
   public void createMessageTransformationApp(ModelType model, JsonObject request)
   {
      String applicationName = extractString(request, ModelerConstants.NAME_PROPERTY);

      ApplicationType applicationType = getModelBuilderFacade().createApplication(model,
            null, applicationName,
            ModelerConstants.MESSAGE_TRANSFORMATION_APPLICATION_TYPE_ID);

      // Map newly created application to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(applicationType);

   }

   @OnCommand(commandId = "camelApplication.create")
   public void createCamelApp(ModelType model, JsonObject request)
   {
      String applicationName = extractString(request, ModelerConstants.NAME_PROPERTY);

      ApplicationType applicationType = getModelBuilderFacade().createApplication(model,
            null, applicationName, ModelerConstants.CAMEL_APPLICATION_TYPE_ID);

      // Map newly created application to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(applicationType);

      // Store attributes for type
      // TODO Make general mechanism 
      
      JsonObject attributes = request.getAsJsonObject(ModelerConstants.ATTRIBUTES_PROPERTY);

      if (attributes != null
            && attributes.has("carnot:engine:camel::applicationIntegrationOverlay")
            && !attributes.get("carnot:engine:camel::applicationIntegrationOverlay")
                  .isJsonNull())
      {
         ModelBuilderFacade.setAttribute(
               applicationType,
               "carnot:engine:camel::applicationIntegrationOverlay",
               attributes.get("carnot:engine:camel::applicationIntegrationOverlay")
                     .getAsString());
         
         // Flag for new implementation
         ModelBuilderFacade.setBooleanAttribute(
               applicationType,
               "carnot:engine:camel::supportsMultipleAccessPoints", true);
      }
   }

   @OnCommand(commandId = "uiMashupApplication.create")
   public void createUiMashupApp(ModelType model, JsonObject request)
   {
      String name = extractString(request, ModelerConstants.NAME_PROPERTY);

      ApplicationType applicationType = getModelBuilderFacade().createApplication(model,
            null, name, ModelerConstants.EXTERNAL_WEB_APP_CONTEXT_TYPE_KEY);

      // Map newly created application to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(applicationType);

   }

   /**
    * @param targetElement
    * @param request
    */
   @OnCommand(commandId = "application.delete")
   public void deleteApplication(ModelType model, JsonObject request)
   {
      String appId = extractString(request, ModelerConstants.ID_PROPERTY);
      ApplicationType application = getModelBuilderFacade().findApplication(model, appId);
      if (application != null)
      {
         synchronized (model)
         {
            ModelElementEditingUtils.deleteIdentifiable(application);
         }
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
