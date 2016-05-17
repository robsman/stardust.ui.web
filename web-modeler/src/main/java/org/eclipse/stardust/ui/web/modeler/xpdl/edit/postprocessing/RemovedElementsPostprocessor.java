package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@Component
public class RemovedElementsPostprocessor implements ChangePostprocessor
{
   @Resource
   private ModelService modelService;

   @Override
   public int getInspectionPhase()
   {
      return 12000;
   }

   @Override
   public void inspectChange(Modification change)
   {
      String commandId = change.getMetadata().get("commandId");
      if (null != commandId && (commandId.equals("undoMostCurrent")))
      {
         for (EObject candidate : change.getAddedElements())
         {
            modelService.uuidMapper().map(candidate);
         }
      }
      else
      {
         for (EObject candidate : change.getRemovedElements())
         {
            modelService.uuidMapper().unmap(candidate, false);
         }
      }
   }
}