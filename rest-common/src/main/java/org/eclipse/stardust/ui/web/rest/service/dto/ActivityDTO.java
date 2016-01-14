/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isAuxiliaryActivity;

import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.InitializingDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;


/**
 * @author Anoop.Nair
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@DTOClass
public class ActivityDTO extends AbstractDTO implements InitializingDTO
{
   @DTOAttribute("id")
   public String id;
   
   @DTOAttribute("qualifiedId")
   public String qualifiedId;
   
   @DTOAttribute("name")
   public String name;

   @DTOAttribute("description")
   public String description;

   @DTOAttribute("implementationType.id")
   public String implementationTypeId;

   @DTOAttribute("implementationType.name")
   public String implementationTypeName;
   
   public Boolean auxillary;
   
   @DTOAttribute("interactive")
   public Boolean interactive;
   
   @DTOAttribute("runtimeElementOID")
   public Long runtimeElementOid;

   @Override
   public void afterAttributesSet(Object sourceInstance) throws Exception
   {
      Activity activity = (Activity)sourceInstance;
      
      this.auxillary = isAuxiliaryActivity(activity);
      this.name = I18nUtils.getActivityName(activity);
   }
}
