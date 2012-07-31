/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.edit.model.element;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;

/**
 * @author Shrikant.Gangal
 *
 */
@CommandHandler
public class StructuredTypeChangeCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   public static final String TYPE_PROPERTY = "type";

   @OnCommand(commandId = "structuredDataType.create")
   public void handleCommand(ModelType model, JsonObject request)
   {
      String typeId = extractString(request, ModelerConstants.ID_PROPERTY);
      String typeName = extractString(request, ModelerConstants.NAME_PROPERTY);

      synchronized (model)
      {
         EObjectUUIDMapper mapper = springContext.getBean(EObjectUUIDMapper.class);
         mapper.map(MBFacade.createTypeDeclaration(model, typeId, typeName));
      }
   }
}
