package org.eclipse.stardust.ui.web.modeler.edit.model;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.ui.web.modeler.common.ModelRepository;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.conversion.BeanInvocationExecutor;
import org.eclipse.stardust.ui.web.modeler.model.conversion.ModelConverter;
import org.eclipse.stardust.ui.web.modeler.model.conversion.RequestExecutor;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerSessionRestController;

@Service
@Scope("prototype")
public class ModelConversionService
{
   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelService modelService;

   @Resource
   private ModelerSessionRestController modelerSessionRestController;

   public EObject convertModel(EObject srcModel, String targetFormat)
   {
      RequestExecutor requestExecutor = new BeanInvocationExecutor(jsonIo, modelService,
            modelerSessionRestController);

      ModelConverter converter = new ModelConverter(jsonIo, requestExecutor);

      ModelRepository modelRepository = modelService.currentSession().modelRepository();

      String srcModelId = modelRepository.getModelBinding(srcModel).getModelId(srcModel);

      String modelCopyId = converter.convertModel(srcModelId, targetFormat);

      return modelRepository.findModel(modelCopyId);
   }
}
