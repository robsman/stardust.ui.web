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

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
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
      DataType data = MBFacade.getInstance().createPrimitiveData(model, id, name, primitiveType);

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
      String stripFullId_ = MBFacade.getInstance().getModelId(extractString(request,
            ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID));
      if (StringUtils.isEmpty(stripFullId_))
      {
         stripFullId_ = model.getId();
      }
      String structuredDataFullId = MBFacade.getInstance().stripFullId(extractString(request,
            ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID));
      DataType data = new MBFacade(modelService().getModelManagementStrategy()).createStructuredData(model, stripFullId_, id, name,
            structuredDataFullId);

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

      DataType data = MBFacade.getInstance().createDocumentData(model, id, name, null);

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
      DataType data = MBFacade.getInstance().findData(model, id);
      synchronized (model)
      {
         model.getData().remove(data);
      }
   }

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }
}
