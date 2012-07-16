package org.eclipse.stardust.ui.web.modeler.edit;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Component
@Scope("singleton")
public class TestChangeListener implements IChangeListener
{

   @Override
   public void onCommand(JsonObject commandJson)
   {
      // TODO Auto-generated method stub
      System.out.println("Received command: " + commandJson);
   }

}
