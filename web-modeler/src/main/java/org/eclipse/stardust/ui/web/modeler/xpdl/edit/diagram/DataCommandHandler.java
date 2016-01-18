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

package org.eclipse.stardust.ui.web.modeler.xpdl.edit.diagram;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.exception.ModelerErrorClass;
import org.eclipse.stardust.model.xpdl.builder.exception.ModelerException;
import org.eclipse.stardust.model.xpdl.builder.utils.*;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils.CommandHandlerUtils;
import org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils.ModelElementEditingUtils;

/**
 *
 * @author Sidharth.Singh
 *
 */
@CommandHandler
public class DataCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   /**
    *
    * @param parentLaneSymbol
    * @param model
    * @param processDefinition
    * @param request
    */
   @OnCommand(commandId = "dataSymbol.create")
   public void createData(ModelType model, LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);
      String dataFullID = extractString(request, ModelerConstants.DATA_FULL_ID_PROPERTY);
      String dataID = extractString(request, ModelerConstants.DATA_ID_PROPERTY);
      String dataName = extractString(request, ModelerConstants.DATA_NAME_PROPERTY);
      int xProperty = extractInt(request, ModelerConstants.X_PROPERTY);
      int yProperty = extractInt(request, ModelerConstants.Y_PROPERTY);
      int widthProperty = extractInt(request, ModelerConstants.WIDTH_PROPERTY);
      int heightProperty = extractInt(request, ModelerConstants.HEIGHT_PROPERTY);

      synchronized (model)
      {
         EObjectUUIDMapper mapper = modelService().uuidMapper();

         DataType data = null;

         try
         {
            try
            {
               data = getModelBuilderFacade().importData(model, dataFullID);
            }
            catch (IllegalArgumentException e)
            {
               throw new ModelerException(ModelerErrorClass.DATA_ID_ALREADY_EXISTS);
            }
            
            if (null == data)
            {
               data = getModelBuilderFacade().createPrimitiveData(model, dataID,
                     dataName, ModelerConstants.STRING_PRIMITIVE_DATA_TYPE);
            }
            if (mapper.getUUID(data) == null)
            {
               mapper.map(data);
            }
         }
         catch (ObjectNotFoundException x)
         {
         }

         DataSymbolType dataSymbol = getModelBuilderFacade().createDataSymbol(model, data, processDefinition,
               parentLaneSymbol.getId(), xProperty, yProperty, widthProperty, heightProperty);
         mapper.map(dataSymbol);
      }
   }

   /**
    *
    * @param parentLaneSymbol
    * @param request
    */
   @OnCommand(commandId = "dataSymbol.delete")
   public void deleteData(ModelType model, LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);
      Long dataOID = extractLong(request, ModelerConstants.OID_PROPERTY);

      DataSymbolType dataSymbol = XPDLFinderUtils.findDataSymbolRecursively(parentLaneSymbol, dataOID);
      synchronized (model)
      {
         if(dataSymbol != null)
         {
            ModelElementEditingUtils.deleteDataMappingConnection(dataSymbol.getDataMappings());
            processDefinition.getDiagram().get(0).getDataSymbol().remove(dataSymbol);
            parentLaneSymbol.getDataSymbol().remove(dataSymbol);
         }
      }
   }

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }
}