/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.net.URLDecoder;

import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelVariable;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContext;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@CommandHandler
public class ConfigVariableChangeCommandHandler
{
   @OnCommand(commandId = "configVariable.delete")
   public void deleteConfigurationVariable(ModelType model, JsonObject request)
   {
      VariableContext variableContext = new VariableContext();
      variableContext.initializeVariables(model);
      variableContext.refreshVariables(model);
      variableContext.saveVariables();

      JsonObject deleteOptionsJson = request.get("deleteOptions").getAsJsonObject();
      String mode = deleteOptionsJson.get("mode").getAsString();

      JsonElement jsonVarName = request.get("variableName");
      String variableName = jsonVarName.getAsString();
      variableName = URLDecoder.decode(variableName);
      ModelVariable modelVariableByName = variableContext.getModelVariableByName(variableName);
      if (modelVariableByName != null)
      {
         modelVariableByName.setRemoved(true);
         String newValue = null;

         if (mode.equals("withLiteral"))
         {
            JsonElement jsonValue = deleteOptionsJson.get("literalValue");
            newValue = jsonValue.getAsString();
         }
         else if (mode.equals("defaultValue"))
         {
            newValue = modelVariableByName.getDefaultValue();
         }
         else
         {
            newValue = "";
         }

         variableContext.replaceVariable(modelVariableByName, newValue);
         variableContext.saveVariables();
      }
   }

   @OnCommand(commandId = "configVariable.update")
   public void updateConfigurationVariable(ModelType model, JsonObject request)
   {
      VariableContext variableContext = new VariableContext();

      variableContext.initializeVariables(model);
      variableContext.refreshVariables(model);
      variableContext.saveVariables();

      ModelVariable modelVariable = variableContext.getModelVariableByName(request.get("variableName").getAsString());

      if (request.has("defaultValue"))
      {
         modelVariable.setDefaultValue(request.get("defaultValue").getAsString());
      }
      if (request.has("description"))
      {
         modelVariable.setDescription(request.get("description").getAsString());
      }
      variableContext.saveVariables();

   }
}
