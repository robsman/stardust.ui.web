package org.eclipse.stardust.ui.web.modeler.edit;

import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("session")
public class EditingSessionManager
{
   private EditingSession editingSession;

   public EditingSession getSession(ModelType... models)
   {
      if (null == editingSession)
      {
         createEditingSession();
      }

      for (ModelType model : models)
      {
         if ( !editingSession.isTrackingModel(model))
         {
            editingSession.trackModel(model);
         }
      }

      return editingSession;
   }

   public EditingSession createEditingSession()
   {
      if (null == editingSession)
      {
         editingSession = new EditingSession();
      }
      return editingSession;
   }
}
