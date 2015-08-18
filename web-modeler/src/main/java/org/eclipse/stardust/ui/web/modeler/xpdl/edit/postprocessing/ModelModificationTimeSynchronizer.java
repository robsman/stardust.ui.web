package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.util.Date;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.RecordingModelManagementStrategy;

@Component
public class ModelModificationTimeSynchronizer implements ChangePostprocessor
{
   @Resource
   private ModelService modelService;

   @Override
   public int getInspectionPhase()
   {
      // pretty low priority
      return 500;
   }

   @Override
   public void inspectChange(Modification change)
   {
      Set<ModelType> modifiedXpdlModels = newHashSet();
      for (EObject changedElement : change.getModifiedElements())
      {
         ModelType xpdlModel = change.findContainer(changedElement, ModelType.class);
         if (null != xpdlModel)
         {
            modifiedXpdlModels.add(xpdlModel);
         }
      }
      for (EObject changedElement : change.getAddedElements())
      {
         ModelType xpdlModel = change.findContainer(changedElement, ModelType.class);
         if (null != xpdlModel)
         {
            modifiedXpdlModels.add(xpdlModel);
         }
      }
      for (EObject changedElement : change.getRemovedElements())
      {
         ModelType xpdlModel = change.findContainer(changedElement, ModelType.class);
         if (null != xpdlModel)
         {
            modifiedXpdlModels.add(xpdlModel);
         }
      }

      //TODO:Use a TimestampProvider here
      Date now = new Date();
      for (ModelType xpdlModel : modifiedXpdlModels)
      {
         if (!modelService.getModelBuilderFacade().isReadOnly(xpdlModel))
         { 
            if (modelService.getModelBuilderFacade().getModelManagementStrategy() instanceof RecordingModelManagementStrategy)
            {
               modelService.getModelBuilderFacade().setModified(xpdlModel, "0");
            }
            else
            {
               modelService.getModelBuilderFacade().setModified(xpdlModel, now);
            }
         }
      }
   }

}
