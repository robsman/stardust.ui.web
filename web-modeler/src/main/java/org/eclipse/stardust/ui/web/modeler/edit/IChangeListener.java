package org.eclipse.stardust.ui.web.modeler.edit;

import com.google.gson.JsonObject;

public interface IChangeListener
{
   void onCommand(JsonObject commandJson);
}
