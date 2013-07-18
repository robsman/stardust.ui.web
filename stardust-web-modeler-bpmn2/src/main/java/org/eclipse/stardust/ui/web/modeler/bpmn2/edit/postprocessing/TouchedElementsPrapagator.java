package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.postprocessing;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.ui.web.modeler.bpmn2.edit.TouchedElementsCollector;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.springframework.stereotype.Component;

@Component
public class TouchedElementsPrapagator implements ChangePostprocessor
{
   @Resource
   private TouchedElementsCollector collector;

   @Override
   public int getInspectionPhase()
   {
      return 100;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject element : collector.getTouchedElements())
      {
         change.markAlsoModified(element);
      }
   }

}
