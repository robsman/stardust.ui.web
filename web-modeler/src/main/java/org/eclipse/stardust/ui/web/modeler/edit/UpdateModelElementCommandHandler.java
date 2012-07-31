/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.edit;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementUnmarshaller;

@CommandHandler
public class UpdateModelElementCommandHandler
{
   @OnCommand(commandId = "modelElement.update")
   public void updateElement(EObject targetElement, JsonObject request)
   {
      ModelElementUnmarshaller.getInstance().populateFromJson(targetElement, request);
   }
}
