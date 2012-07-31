/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.edit.model.element;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.modeler.edit.ICommandHandler;

/**
 * @author Shrikant.Gangal
 * 
 */
@Component
@Scope("prototype")
public class ApplicationTypeChangeCommandHandler implements ICommandHandler
{
   @Resource
   private ApplicationContext springContext;

   @Override
   public boolean isValidTarget(Class<? > type)
   {
      return ApplicationType.class.isAssignableFrom(type);
   }

   @Override
   public void handleCommand(String commandId, EObject targetElement, JsonObject request)
   {
      ModelType model = (ModelType) targetElement;
      ApplicationType applicationType = AbstractElementBuilder.F_CWM.createApplicationType();

      //Map newly created application to a UUID 
      EObjectUUIDMapper mapper = springContext.getBean(EObjectUUIDMapper.class);
      mapper.map(applicationType);

      model.getApplication().add(applicationType);

      applicationType.setId(extractString(request, ModelerConstants.ID_PROPERTY));
      applicationType.setName(extractString(request, ModelerConstants.NAME_PROPERTY));

      if ("webServiceApplication.create".equals(commandId))
      {
         applicationType.setType(MBFacade.findApplicationTypeType(model,
               ModelerConstants.WEB_SERVICE_APPLICATION_TYPE_ID));
      }
      else if ("messageTransformationApplication.create".equals(commandId))
      {
         // TODO - check if needed
         AttributeUtil.setAttribute(applicationType,
               ModelerConstants.APPLICATION_TYPE_PROPERTY,
               ModelerConstants.MESSAGE_TRANSFORMATION_APPLICATION_TYPE_ID);

         // TODO
         // applicationType.setType(MBFacade.findApplicationTypeType(model,
         // ModelerConstants.MESSAGE_TRANSFORMATION_APPLICATION_TYPE_ID));
      }
      else if ("camelApplication.create".equals(commandId))
      {
         // TODO - check if needed
         AttributeUtil.setAttribute(applicationType,
               ModelerConstants.APPLICATION_TYPE_PROPERTY,
               ModelerConstants.CAMEL_APPLICATION_TYPE_ID);

         // TODO
         // applicationType.setType(MBFacade.findApplicationTypeType(model,
         // ModelerConstants.MESSAGE_TRANSFORMATION_APPLICATION_TYPE_ID));
      }
      else if ("uiMashupApplication.create".equals(commandId))
      {
         // TODO - check if needed
         AttributeUtil.setAttribute(applicationType,
               ModelerConstants.APPLICATION_TYPE_PROPERTY,
               ModelerConstants.INTERACTIVE_APPLICATION_TYPE_KEY);

         // TODO
         // applicationType.setType(MBFacade.findApplicationTypeType(model,
         // ModelerConstants.MESSAGE_TRANSFORMATION_APPLICATION_TYPE_ID));
      }
   }
}
