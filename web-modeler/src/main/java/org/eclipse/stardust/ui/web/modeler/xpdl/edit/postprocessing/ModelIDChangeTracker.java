package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.FeatureChange;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.builder.utils.ExternalReferenceUtils;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@Component
public class ModelIDChangeTracker implements ChangePostprocessor
{
   @Resource
   private ModelService modelService;

   @Override
   public int getInspectionPhase()
   {
      return 100;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getModifiedElements())
      {
         if (candidate instanceof ModelType)
         {
            trackModification(candidate, true, change);
         }
      }
   }

   private void trackModification(EObject candidate, boolean removed, Modification change)
   {
      String oldID = null;
      EList<FeatureChange> featureChanges = change.getChangeDescription()
            .getObjectChanges().get(candidate);
      if (featureChanges != null) {
         for (Iterator<FeatureChange> i = featureChanges.iterator(); i.hasNext();)
         {
            FeatureChange featureChange = i.next();
            if (featureChange.getFeature().getName().equals("id"))
            {
               oldID = (String) featureChange.getValue();
               Map<String,ModelType> models = modelService.getModelManagementStrategy().getModels(false);
               ModelType model = models.remove(oldID);
               models.put(((ModelType)candidate).getId(), model);

               List<ModelType> referingModels = ExternalReferenceUtils.getReferingModels(oldID, models.values());
               for (Iterator<ModelType> k = referingModels.iterator(); k.hasNext();)
               {
                  ModelType referingModel = k.next();
                  ExternalReferenceUtils.fixExternalReferences(models, referingModel);
               }
            }
         }
      }

   }
}