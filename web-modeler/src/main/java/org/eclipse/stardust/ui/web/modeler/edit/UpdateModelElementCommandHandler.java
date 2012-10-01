/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.edit;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@CommandHandler
public class UpdateModelElementCommandHandler
{
   @Resource
   private ModelService modelService;

   @OnCommand(commandId = "modelElement.update")
   public void updateElement(ModelType model, EObject targetElement, JsonObject request)
   {
      modelService.currentSession().modelElementUnmarshaller().populateFromJson(targetElement, request);
   }
}
