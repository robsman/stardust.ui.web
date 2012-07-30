package org.eclipse.stardust.ui.web.modeler.edit;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;

@Component
@Scope("singleton")
public class TestChangeListener implements IChangeListener
{

   @Override
   public void onCommand(EditingSession session, JsonObject commandJson)
   {
      System.out.println("[session: " + session.getId() + "] - command: " + commandJson);
   }

}
