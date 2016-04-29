package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@Component
public class ResponsePostprocessor implements ChangePostprocessor
{
   @Resource
   private ModelService modelService;

   @Override
   public int getInspectionPhase()
   {
      return 10000;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getModifiedElements())
      {
         if (candidate instanceof ModelType)
         {
            trackModification(candidate, false, change);
            break;
         }
      }
   }

   private void trackModification(EObject candidate, boolean removed, Modification change)
   {
      String commandId = change.getMetadata().get("commandId");
      if (null != commandId && (commandId.equals("typeDeclaration.create")
            || (commandId.equals("structuredDataType.create")
                  || (commandId.equals("structuredDataType.delete")))))
      {
         if (!change.getAddedElements().isEmpty())
         {
            change.markUnmodified(candidate);
         }
      }
   }
}