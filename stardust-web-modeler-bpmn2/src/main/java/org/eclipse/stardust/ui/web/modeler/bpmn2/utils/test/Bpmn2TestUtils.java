package org.eclipse.stardust.ui.web.modeler.bpmn2.utils.test;

import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.findContainingModel;

import java.util.UUID;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BPMNDiagram;

import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2CoreElementsBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2DiBuilder;
import org.eclipse.stardust.ui.web.modeler.model.ModelJto;
import org.eclipse.stardust.ui.web.modeler.model.ProcessDefinitionJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ProcessDiagramJto;

public class Bpmn2TestUtils
{
   public static final String MODEL_NAME = "Simple BPMN2 Model";

   public static final String PROCESS_NAME = "Test Process";

   public static final String PROCESS_ID = UUID.randomUUID().toString();

   private static final Bpmn2CoreElementsBuilder CORE_ELEMENTS_BUILDER = new Bpmn2CoreElementsBuilder();

   private static final Bpmn2DiBuilder DI_BUILDER = new Bpmn2DiBuilder();

   public static Definitions createModel()
   {
      ModelJto jto = new ModelJto();
      jto.name = MODEL_NAME;

      return CORE_ELEMENTS_BUILDER.createModel(jto);
   }

   public static Process createTestProcess(Definitions model)
   {
      ProcessDefinitionJto jto = new ProcessDefinitionJto();
      jto.uuid = PROCESS_ID;
      jto.id = PROCESS_ID;
      jto.name = PROCESS_NAME;

      Process process = CORE_ELEMENTS_BUILDER.createProcess(model, jto);
      CORE_ELEMENTS_BUILDER.attachProcess(model, process);

      return process;
   }

   public static BPMNDiagram createTestProcessDiagram(Process process)
   {
      ProcessDiagramJto jto = new ProcessDiagramJto();
      jto.name = "Default";

      BPMNDiagram diagram = DI_BUILDER.createDiagram(process, jto);

      DI_BUILDER.attachDiagram(findContainingModel(process), diagram);

      return diagram;
   }

}
