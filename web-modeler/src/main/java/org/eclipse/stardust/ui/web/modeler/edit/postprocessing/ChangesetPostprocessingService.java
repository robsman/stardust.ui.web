package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import static java.util.Collections.sort;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;

@Service
public class ChangesetPostprocessingService
{
   private final List<ChangePostprocessor> processorsInExecutionOrder;

   @Autowired
   ChangesetPostprocessingService(List<ChangePostprocessor> processors)
   {
      this.processorsInExecutionOrder = newArrayList(processors);

      sort(processorsInExecutionOrder, new Comparator<ChangePostprocessor>()
      {
         @Override
         public int compare(ChangePostprocessor o1, ChangePostprocessor o2)
         {
            return o1.getInspectionPhase() - o2.getInspectionPhase();
         }
      });
   }

   public void postprocessChangeset(Modification change)
   {
      for (ChangePostprocessor postprocessor : processorsInExecutionOrder)
      {
         postprocessor.inspectChange(change);
      }
   }
}
