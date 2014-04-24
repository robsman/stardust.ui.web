package org.eclipse.stardust.ui.web.modeler.edit.spi;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;

public interface CommandHandlingMediator
{
   void broadcastChange(EditingSession session, CommandJto commandJto, JsonObject changeJson);

   Modification handleCommand(EditingSession editingSession, String commandId,
         List<ChangeRequest> changes);

   class ChangeRequest
   {
      private final EObject model;

      private final EObject contextElement;

      private final JsonObject changeDescriptor;

      public ChangeRequest(EObject model, EObject contextElement,
            JsonObject changeDescriptor)
      {
         this.model = model;
         this.contextElement = contextElement;
         this.changeDescriptor = changeDescriptor;
      }

      public EObject getModel()
      {
         return model;
      }

      public EObject getContextElement()
      {
         return contextElement;
      }

      public JsonObject getChangeDescriptor()
      {
         return changeDescriptor;
      }

   }
}
