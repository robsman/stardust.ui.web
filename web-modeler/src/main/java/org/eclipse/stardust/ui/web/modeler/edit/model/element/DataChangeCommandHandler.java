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

package org.eclipse.stardust.ui.web.modeler.edit.model.element;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.Iterator;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

/**
 * @author Shrikant.Gangal
 *
 */
@CommandHandler
public class DataChangeCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "primitiveData.create")
   public void createPrimitiveData(ModelType model, JsonObject request)
   {
      String id = extractString(request, ModelerConstants.ID_PROPERTY);
      String name = extractString(request, ModelerConstants.NAME_PROPERTY);
      String primitiveType = extractString(request, ModelerConstants.PRIMITIVE_TYPE);
      DataType data = getModelBuilderFacade().createPrimitiveData(model, id, name, primitiveType);

      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      data.setElementOid(++maxOid);

      //Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(data);
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "structuredData.create")
   public void createStructuredData(ModelType model, JsonObject request)
   {
      String id = extractString(request, ModelerConstants.ID_PROPERTY);
      String name = extractString(request, ModelerConstants.NAME_PROPERTY);
      String dataFullID = extractString(request, ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY);
      String stripFullId_ = getModelBuilderFacade().getModelId(extractString(request,
            ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY));
      if (StringUtils.isEmpty(stripFullId_))
      {
         stripFullId_ = model.getId();
      }

      DataType data = getModelBuilderFacade().createStructuredData(model, id, name,
            dataFullID);

      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      data.setElementOid(++maxOid);

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(data);
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "documentData.create")
   public void createDocumentData(ModelType model, JsonObject request)
   {
      String id = extractString(request, ModelerConstants.ID_PROPERTY);
      String name = extractString(request, ModelerConstants.NAME_PROPERTY);

      DataType data = getModelBuilderFacade().createDocumentData(model, id, name, null);

      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      data.setElementOid(++maxOid);

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(data);
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "data.delete")
   public void deletetData(ModelType model, JsonObject request)
   {
      String id = extractString(request, ModelerConstants.ID_PROPERTY);
      DataType data = getModelBuilderFacade().findData(model, id);

      synchronized (model)
      {
         deleteDataDymbolsForData(model, data.getId());
         model.getData().remove(data);
      }
   }

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }

   /**
    * @param model
    * @param dataId
    */
   private void deleteDataDymbolsForData(ModelType model, String dataId)
   {
      for (ProcessDefinitionType pdt : model.getProcessDefinition())
      {
         for (DiagramType diagram : pdt.getDiagram())
         {
            for (PoolSymbol poolSymbol : diagram.getPoolSymbols())
            {
               for (LaneSymbol childLaneSymbol : poolSymbol.getChildLanes())
               {
                  Iterator<DataSymbolType> iter = childLaneSymbol.getDataSymbol()
                        .iterator();
                  while (iter.hasNext())
                  {
                     DataSymbolType dataSymbol = iter.next();
                     if (dataId.equals(dataSymbol.getData().getId()))
                     {
                        iter.remove();
                     }
                  }
               }
            }
         }
      }
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return new ModelBuilderFacade(springContext.getBean(ModelService.class)
            .getModelManagementStrategy());
   }
}
