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
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

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
   private MBFacade facade;

   /**
    *
    * @param parentLaneSymbol
    * @param model
    * @param processDefinition
    * @param request
    */
   @OnCommand(commandId = "dataSymbol.create")
   public void createData(LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ModelType model = ModelUtils.findContainingModel(parentLaneSymbol);
      ProcessDefinitionType processDefinition = ModelUtils
            .findContainingProcess(parentLaneSymbol);
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

         DataType data;

         try
         {
            data = facade().importData(model, dataFullID);
         }
         catch (ObjectNotFoundException x)
         {
            if (true)
            {
               data = facade().createPrimitiveData(model, dataID, dataName,
                     ModelerConstants.STRING_PRIMITIVE_DATA_TYPE);
               mapper.map(data);
            }
         }

         DataSymbolType dataSymbol = facade().createDataSymbol(model, data,
               processDefinition, parentLaneSymbol.getId(), xProperty, yProperty,
               widthProperty, heightProperty);
         mapper.map(dataSymbol);
      }
   }

   /**
    *
    * @param parentLaneSymbol
    * @param request
    */
   @OnCommand(commandId = "dataSymbol.delete")
   public void deleteData(LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ModelType model = ModelUtils.findContainingModel(parentLaneSymbol);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);
      Long dataOID = extractLong(request, ModelerConstants.OID_PROPERTY);
      String dataFullID = extractString(request, ModelerConstants.DATA_FULL_ID_PROPERTY);
      DataType data = facade().importData(model, dataFullID);
      DataSymbolType dataSymbol = facade().findDataSymbolRecursively(parentLaneSymbol,
            dataOID);
      synchronized (model)
      {
         data.getDataSymbols().remove(dataSymbol);
         processDefinition.getDiagram().get(0).getDataSymbol().remove(dataSymbol);
         parentLaneSymbol.getDataSymbol().remove(dataSymbol);
      }

   }

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }
   
   private MBFacade facade()
   {
      if (facade == null)
      {
         facade = new MBFacade(springContext.getBean(ModelService.class)
               .getModelManagementStrategy());
      }
      return facade;
   }
}
