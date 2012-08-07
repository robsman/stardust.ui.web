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

import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrganizationType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;

/**
 * @author Shrikant.Gangal
 * 
 */
@CommandHandler
public class ParticipantChangeCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "role.create")
   public void createRole(ModelType model, JsonObject request)
   {
      String roleID = extractString(request, ModelerConstants.ID_PROPERTY);
      String roleName = extractString(request, ModelerConstants.NAME_PROPERTY);
      RoleType role = null;
      synchronized (model)
      {
         role = MBFacade.createRole(model, roleID, roleName);
      }
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      role.setElementOid(++maxOid);

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = springContext.getBean(EObjectUUIDMapper.class);
      mapper.map(role);
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "organization.create")
   public void createOrganization(ModelType model, JsonObject request)
   {
      String orgID = extractString(request, ModelerConstants.ID_PROPERTY);
      String orgName = extractString(request, ModelerConstants.NAME_PROPERTY);
      OrganizationType org = null;
      synchronized (model)
      {
         org = MBFacade.createOrganization(model, orgID, orgName);
      }
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      org.setElementOid(++maxOid);

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = springContext.getBean(EObjectUUIDMapper.class);
      mapper.map(org);
   }

   /**
    * @param org
    * @param request
    */
   @OnCommand(commandId = "role.create")
   public void addRole(OrganizationType org, JsonObject request)
   {
      String roleID = extractString(request, ModelerConstants.ID_PROPERTY);
      String roleName = extractString(request, ModelerConstants.NAME_PROPERTY);
      RoleType role = null;
      ModelType model = ModelUtils.findContainingModel(org);
      synchronized (model)
      {
         role = MBFacade.createRole(model, roleID, roleName);
         MBFacade.addOrganizationParticipant(org, role);
      }
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      role.setElementOid(++maxOid);

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = springContext.getBean(EObjectUUIDMapper.class);
      mapper.map(role);
   }

   @OnCommand(commandId = "organization.create")
   public void addOrganization(OrganizationType org, JsonObject request)
   {
      String orgID = extractString(request, ModelerConstants.ID_PROPERTY);
      String orgName = extractString(request, ModelerConstants.NAME_PROPERTY);
      OrganizationType newOrg = null;
      ModelType model = ModelUtils.findContainingModel(org);
      synchronized (model)
      {
         newOrg = MBFacade.createOrganization(model, orgID, orgName);
         MBFacade.addOrganizationParticipant(org, newOrg);
      }
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      newOrg.setElementOid(++maxOid);

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = springContext.getBean(EObjectUUIDMapper.class);
      mapper.map(newOrg);
   }
}
