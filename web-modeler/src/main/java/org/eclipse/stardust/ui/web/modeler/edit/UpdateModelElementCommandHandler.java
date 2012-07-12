/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.edit;

import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementUnmarshaller;

import com.google.gson.JsonObject;

public class UpdateModelElementCommandHandler implements ICommandHandler
{
   @Override
   public boolean isValidTarget(Class< ? > type)
   {
      return IIdentifiableElement.class.isAssignableFrom(type);
   }

   @Override
   public void handleCommand(String commandId, IModelElement targetElement, JsonObject request)
   {
	   ModelElementUnmarshaller.getInstance().populateFromJson(targetElement, request);
   }
}
