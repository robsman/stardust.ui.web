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
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

/**
 * @author Shrikant.Gangal
 *
 */
@CommandHandler
public class StructuredTypeChangeCommandHandler
{
   @Resource
   private ApplicationContext springContext;
   private ModelBuilderFacade modelBuilderFacade;

   public static final String TYPE_PROPERTY = "type";

	/**
	 * @param model
	 * @param request
	 */
	@OnCommand(commandId = "structuredDataType.create")
	public void createStructuredDataType(ModelType model, JsonObject request) {
		String typeId = extractString(request, ModelerConstants.ID_PROPERTY);
		String typeName = extractString(request, ModelerConstants.NAME_PROPERTY);

      synchronized (model)
      {
         EObjectUUIDMapper mapper = modelService().uuidMapper();
         mapper.map(getModelBuilderFacade().createTypeDeclaration(model, typeId, typeName));
      }
	}

	/**
	 * @param model
	 * @param request
	 */
	@OnCommand(commandId = "structuredDataType.delete")
	public void deleteStructuredDataType(ModelType model, JsonObject request) {
		String structuredDataTypeId = extractString(request,
				ModelerConstants.ID_PROPERTY);

		TypeDeclarationType structuredDataType = getModelBuilderFacade()
				.findTypeDeclaration(model, structuredDataTypeId);
		synchronized (model) {
			model.getTypeDeclarations().getTypeDeclaration()
					.remove(structuredDataType);
		}
	}

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }
   
   private ModelBuilderFacade getModelBuilderFacade()
   {
      if (modelBuilderFacade == null)
      {
         modelBuilderFacade = new ModelBuilderFacade(springContext.getBean(ModelService.class)
               .getModelManagementStrategy());
      }
      return modelBuilderFacade;
   }
}
