package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import org.eclipse.bpmn2.Definitions;

import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2CoreElementsBuilder;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.model.ModelJto;

@CommandHandler
public class ModelCommandsHandler
{
   @OnCommand(commandId = "TODO")
   public void createModel()
   {
      // TODO
      ModelJto jto = null;

      Definitions model = new Bpmn2CoreElementsBuilder().createModel(jto);

      // TODO attach? where?
   }

}
