/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.edit.model.element;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.ICommandHandler;

/**
 * @author Shrikant.Gangal
 *
 */
@Component
@Scope("prototype")
public class StructuredTypeChangeCommandHandler implements ICommandHandler
{
   @Resource
   private ApplicationContext springContext;

   public static final String TYPE_PROPERTY = "type";

   @Override
   public boolean isValidTarget(Class<? > type)
   {
      return ModelType.class.isAssignableFrom(type);
   }

   @Override
   public void handleCommand(String commandId, EObject targetElement, JsonObject request)
   {
      ModelType model = (ModelType) targetElement;
      String typeId = extractString(request, ModelerConstants.ID_PROPERTY);
      String typeName = extractString(request, ModelerConstants.NAME_PROPERTY);

      synchronized (model)
      {
         EObjectUUIDMapper mapper = springContext.getBean(EObjectUUIDMapper.class);
         mapper.map(MBFacade.createTypeDeclaration(model, typeId, typeName));
      }
   }
}
