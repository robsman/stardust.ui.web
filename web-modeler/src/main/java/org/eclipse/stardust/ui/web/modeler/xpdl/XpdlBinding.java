package org.eclipse.stardust.ui.web.modeler.xpdl;

import java.util.HashMap;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.spi.SpiExtensionRegistry;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContextHelper;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.modeling.validation.Issue;
import org.eclipse.stardust.modeling.validation.ValidationService;
import org.eclipse.stardust.modeling.validation.ValidatorRegistry;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.eclipse.stardust.ui.web.modeler.spi.ModelFormat;
import org.eclipse.stardust.ui.web.modeler.spi.ModelingSessionScoped;
import org.eclipse.stardust.ui.web.modeler.xpdl.validation.ValidationExtensionRegistry;

@Service
@ModelFormat(ModelFormat.XPDL)
@ModelingSessionScoped
public class XpdlBinding extends ModelBinding<ModelType>
{
   private static final Logger trace = LogManager.getLogger(XpdlBinding.class);

   static
   {
      ModelBinding.trace.info("Loaded XPDL model binding.");

      trace.info("Bootstrapping SPI extension registry ...");
      SpiExtensionRegistry.instance().setExtensionRegistry(StardustExtensionRegistry.instance());
      trace.info("Successfully bootstrapped SPI extension registry");
   }

   @Resource
   private XpdlNavigator navigator;

   @Resource
   private ModelElementMarshaller marshaller;

   @Resource
   private ModelElementUnmarshaller unmarshaller;

   @Autowired
   public XpdlBinding(ModelingSession session)
   {
      super(session);
   }

   @Override
   public boolean isCompatible(EObject model)
   {
      return model instanceof ModelType;
   }

   @Override
   public String getModelFormat(ModelType model)
   {
      return "xpdl";
   }

   @Override
   public String getModelId(ModelType model)
   {
      return model.getId();
   }

   @Override
   public XpdlNavigator getNavigator()
   {
      return navigator;
   }

   @Override
   public ModelElementMarshaller getMarshaller()
   {
      return marshaller;
   }

   @Override
   public ModelElementUnmarshaller getUnmarshaller()
   {
      return unmarshaller;
   }

   @Override
   public boolean isReadOnly(ModelType model)
   {
      if (null != model)
      {
         AttributeType attribute = AttributeUtil.getAttribute(model,
               "stardust:security:hash");
         if ((attribute != null) && (attribute.getValue() != null)
               && (attribute.getValue().length() > 0))
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public JsonArray validateModel(ModelType model)
   {
      String modelId = model.getId();

      trace.debug("Validating model " + modelId);

      VariableContextHelper instance = VariableContextHelper.getInstance();
      instance.clear();
      instance.storeVariables(model, false);

      ValidatorRegistry.setFilters(new HashMap<String, String>());
      ValidatorRegistry.setValidationExtensionRegistry(ValidationExtensionRegistry.getInstance());
      ValidationService validationService = ValidationService.getInstance();

      JsonArray issuesJson = new JsonArray();

      Issue[] issues = validationService.validateModel(model);

      for (int i = 0; i < issues.length; i++ )
      {
         Issue issue = issues[i];
         JsonObject issueJson = new JsonObject();

         System.out.println("Found issue " + issue);

         issueJson.addProperty("message", issue.getMessage());
         issueJson.addProperty("severity", issue.getSeverity());

         EObject modelElement = issue.getModelElement();

         String modelElementId = null;

         if (modelElement != null && modelElement instanceof IIdentifiableModelElement)
         {
            modelElementId = modelId + "/"
                  + ((IIdentifiableModelElement) modelElement).getId() + "/"
                  + ((IIdentifiableModelElement) modelElement).getElementOid();
         }
         else if (modelElement != null && modelElement instanceof ModelType)
         {
            modelElementId = modelId + "/" + modelId + "/"
                  + ((ModelType) modelElement).getOid();
         }
         else if (modelElement != null && modelElement instanceof TypeDeclarationType)
         {
            modelElementId = modelId + "/" + modelId + "/"
                  + ((TypeDeclarationType) modelElement).getId();
         }
         else if (modelElement != null && modelElement instanceof ExternalPackage)
         {
            modelElementId = modelId + "/" + modelId + "/"
                  + ((ExternalPackage) modelElement).getId();
         }
         else if (modelElement != null && modelElement instanceof DataMappingType)
         {
            modelElementId = modelId + "/" + modelId + "/"
                  + ((DataMappingType) modelElement).getId();
         }

         issueJson.addProperty("modelElement", modelElementId);
         issuesJson.add(issueJson);
      }

      return issuesJson;
   }
}
