package org.eclipse.stardust.ui.web.modeler.edit;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

public interface ICommandHandler
{
   boolean isValidTarget(Class<?> type);

   void handleCommand(String commandId, EObject targetElement, JsonObject request);
}
