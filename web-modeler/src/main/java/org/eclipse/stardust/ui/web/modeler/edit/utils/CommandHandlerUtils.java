package org.eclipse.stardust.ui.web.modeler.edit.utils;

import org.springframework.context.ApplicationContext;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

public class CommandHandlerUtils
{
   public static ModelBuilderFacade getModelBuilderFacade(ApplicationContext springContext)
   {
      return new ModelBuilderFacade(springContext.getBean(ModelService.class)
            .getModelManagementStrategy());
   }
}
