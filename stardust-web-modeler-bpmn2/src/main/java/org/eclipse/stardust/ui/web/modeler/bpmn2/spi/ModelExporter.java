package org.eclipse.stardust.ui.web.modeler.bpmn2.spi;

import java.io.OutputStream;

import org.eclipse.bpmn2.Definitions;

public interface ModelExporter
{
   void exportModel(Definitions model, OutputStream targetStream);
}
