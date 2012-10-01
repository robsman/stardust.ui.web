package org.eclipse.stardust.ui.web.modeler.edit.spi;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;

public interface ChangePostprocessor
{
   /**
    * Phase identifier, which is used to partially order post-processor execution.
    * <p>
    * Processors will be invoked in order of increasing phase IDs.
    *
    * @return The phase identifier.
    */
   int getInspectionPhase();

   void inspectChange(Modification change);
}
