package org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.model.xpdl.builder.utils.ExternalReferenceUtils;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationTypeType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;

public class DecoratorApplicationUtils
{
   public static boolean isDecoratorApplication(ApplicationType application)
   {
      ApplicationTypeType applicationType = application.getType();
      return applicationType != null && "decoratorApp".equals(applicationType.getId());
   }

   public static void addExternalReference(ApplicationType application,
         ModelBuilderFacade modelBuilderFacade)
   {
      String elementId = AttributeUtil.getAttributeValue(application,
            "stardust:application::decorator::elementId");
      String elementType = AttributeUtil.getAttributeValue(application,
            "stardust:application::decorator::elementType");
      String modelId = AttributeUtil.getAttributeValue(application,
            "stardust:application::decorator::modelId");
      ModelType consumerModel = ModelUtils.findContainingModel(application);
      if (StringUtils.isNotEmpty(modelId))
      {
         int beginIndex = modelId.length() + 1;
         ModelType providerModel = null;
         if (StringUtils.isNotEmpty(elementType)
               && elementType.equalsIgnoreCase("application"))
         {
            ApplicationType decoratedApplication = modelBuilderFacade
                  .getApplication(modelId, elementId.substring(beginIndex));
            providerModel = ModelUtils.findContainingModel(decoratedApplication);

         }
         else if (StringUtils.isNotEmpty(elementType)
               && elementType.equalsIgnoreCase("process"))
         {
            ProcessDefinitionType decoratedProcess = modelBuilderFacade
                  .getProcessDefinition(modelId, elementId.substring(beginIndex));
            providerModel = ModelUtils.findContainingModel(decoratedProcess);
         }

         List<ModelType> models = new ArrayList<ModelType>();
         models.add(providerModel);
         if (!ExternalReferenceUtils.isModelReferenced(consumerModel, models))
         {
            ExternalReferenceUtils.updateReferences(consumerModel, providerModel);
         }
      }
   }
}
