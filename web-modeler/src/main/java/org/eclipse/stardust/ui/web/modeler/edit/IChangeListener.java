package org.eclipse.stardust.ui.web.modeler.edit;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;

public interface IChangeListener
{
   void onCommand(EditingSession session, JsonObject commandJson);
}
