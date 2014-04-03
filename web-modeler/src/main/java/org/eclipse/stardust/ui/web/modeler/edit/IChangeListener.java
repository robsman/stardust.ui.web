package org.eclipse.stardust.ui.web.modeler.edit;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;

public interface IChangeListener
{
   void onCommand(EditingSession session, CommandJto commandJto, JsonObject changeJson);
}
