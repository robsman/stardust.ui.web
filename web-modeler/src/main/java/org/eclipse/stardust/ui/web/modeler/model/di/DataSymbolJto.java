package org.eclipse.stardust.ui.web.modeler.model.di;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.model.DataJto;

public class DataSymbolJto extends NodeSymbolJto<DataJto>
{
   public DataSymbolJto()
   {
      this.type = ModelerConstants.DATA_SYMBOL;
   }
}