/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.edit.diagram.node;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.AnnotationSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.edit.utils.CommandHandlerUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

/**
 *
 * @author Marc.Gille
 *
 */
@CommandHandler
public class AnnotationCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   /**
    * @param parentLaneSymbol
    * @param request
    */
   @OnCommand(commandId = "annotationSymbol.create")
   public void createAnnotation(ModelType model, LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);

      int xProperty = extractInt(request, ModelerConstants.X_PROPERTY);
      int yProperty = extractInt(request, ModelerConstants.Y_PROPERTY);
      int widthProperty = extractInt(request, ModelerConstants.WIDTH_PROPERTY);
      int heightProperty = extractInt(request, ModelerConstants.HEIGHT_PROPERTY);
      
      String content = "";
      if (request.has(ModelerConstants.CONTENT_PROPERTY))
      {
         content = extractString(request, ModelerConstants.CONTENT_PROPERTY);
      }

      synchronized (model)
      {
         getModelBuilderFacade().createAnnotationSymbol(model, processDefinition,
               parentLaneSymbol.getId(), xProperty, yProperty, widthProperty,
               heightProperty, content);
      }
   }

   /**
    *
    * @param parentLaneSymbol
    * @param request
    */
   @OnCommand(commandId = "annotationSymbol.delete")
   public void deleteAnnotation(ModelType model, LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);
      Long annotationOId = GsonUtils.extractLong(request, ModelerConstants.OID_PROPERTY);
      AnnotationSymbolType delAnnSymbol = getModelBuilderFacade().findAnnotationSymbol(
            parentLaneSymbol, annotationOId);

      if (null != delAnnSymbol)
      {
         synchronized (model)
         {
            // TODO : delete connection is not supported currently
            // ModelElementEditingUtils.deleteTransitionConnectionsForSymbol(processDefinition,
            // annSymbol);
            parentLaneSymbol.getAnnotationSymbol().remove(delAnnSymbol);
            processDefinition.getDiagram()
                  .get(0)
                  .getAnnotationSymbol()
                  .remove(delAnnSymbol);
         }
      }
   }

   
   
   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }
}
