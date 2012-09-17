package org.eclipse.stardust.ui.web.modeler.edit.spi;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;

public interface ChangePostprocessor
{

   void inspectChange(Modification change);

}
