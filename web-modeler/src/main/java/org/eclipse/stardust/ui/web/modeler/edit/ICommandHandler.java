package org.eclipse.stardust.ui.web.modeler.edit;

import org.eclipse.stardust.model.xpdl.carnot.IModelElement;

import com.google.gson.JsonObject;

public interface ICommandHandler
{
   boolean isValidTarget(Class<?> type);

   void handleCommand(String commandId, IModelElement targetElement, JsonObject request);
}
