package org.eclipse.stardust.ui.web.modeler.bpmn2.export;

import java.io.OutputStream;

import org.eclipse.bpmn2.Definitions;

import org.eclipse.stardust.model.bpmn2.transform.TransformationControl;
import org.eclipse.stardust.model.bpmn2.transform.xpdl.DialectStardustXPDL;
import org.eclipse.stardust.ui.web.modeler.bpmn2.spi.ModelExporter;

public class XpdlModelExporter implements ModelExporter
{
   @Override
   public void exportModel(Definitions model, OutputStream targetStream)
   {
      // TODO transform BPMN2 into XPDL
      TransformationControl transformer = TransformationControl.getInstance(new DialectStardustXPDL());
      transformer.transformToTarget(model, targetStream);
   }

}
