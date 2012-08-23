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
import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.ConditionalPerformerType;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrganizationType;
import org.eclipse.stardust.model.xpdl.carnot.ParticipantType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

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
         role = getModelBuilderFacade().createRole(model, roleID, roleName);
      }
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      role.setElementOid(++maxOid);

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
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
         org = getModelBuilderFacade().createOrganization(model, orgID, orgName);
      }
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      org.setElementOid(++maxOid);

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
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
         role = getModelBuilderFacade().createRole(model, roleID, roleName);
         getModelBuilderFacade().addOrganizationParticipant(org, role);
      }
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      role.setElementOid(++maxOid);

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(role);
   }

   /**
    * @param org
    * @param request
    */
   @OnCommand(commandId = "organization.create")
   public void addOrganization(OrganizationType org, JsonObject request)
   {
      String orgID = extractString(request, ModelerConstants.ID_PROPERTY);
      String orgName = extractString(request, ModelerConstants.NAME_PROPERTY);
      OrganizationType newOrg = null;
      ModelType model = ModelUtils.findContainingModel(org);
      synchronized (model)
      {
         newOrg = getModelBuilderFacade().createOrganization(model, orgID, orgName);
         getModelBuilderFacade().addOrganizationParticipant(org, newOrg);
      }
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      newOrg.setElementOid(++maxOid);

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(newOrg);
   }

   /**
    * @param org
    * @param request
    */
   @OnCommand(commandId = "organization.updateTeamLeader")
   public void updateTeamLeader(OrganizationType org, JsonObject request)
   {
      String teamLeaderUUID = extractString(request, ModelerConstants.UUID_PROPERTY);
      RoleType tealLeader = (RoleType) modelService().uuidMapper().getEObject(teamLeaderUUID);
      ModelType model = ModelUtils.findContainingModel(org);
      synchronized (model)
      {
         getModelBuilderFacade().setTeamLeader(org, tealLeader);
      }
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "participant.delete")
   public void deleteParticipant(ModelType model, JsonObject request)
   {
      String participantId = extractString(request, ModelerConstants.ID_PROPERTY);
      IModelParticipant modelParticipantInfo = getModelBuilderFacade().findParticipant(
            model, participantId);
      if (modelParticipantInfo instanceof RoleType)
      {
         synchronized (model)
         {
            List<OrganizationType> parentOrgs = getModelBuilderFacade()
                  .getParentOrganizations(model, modelParticipantInfo);

            for (OrganizationType org : parentOrgs)
            {
               // TODO - check why if a participant is deleted from member list get null pointer at
               // Modification.determineChangedElement
//               Iterator<ParticipantType> iter = org.getParticipant().iterator();
//               while (iter.hasNext())
//               {
//                  if (modelParticipantInfo.equals(iter.next().getParticipant()))
//                  {
//                     iter.remove();
//                  }
//               }
               if (modelParticipantInfo.equals(org.getTeamLead()))
               {
                  org.setTeamLead(null);
               }
            }
            model.getRole().remove(modelParticipantInfo);
         }
      }
      else if (modelParticipantInfo instanceof ConditionalPerformerType)
      {
         model.getConditionalPerformer().remove(modelParticipantInfo);
      }
      else if (modelParticipantInfo instanceof OrganizationType)
      {
         synchronized (model)
         {
            removeOrganization(model, (OrganizationType) modelParticipantInfo);
         }
      }
   }

   /**
    * @param model
    * @param org
    */
   private void removeOrganization(ModelType model, OrganizationType org)
   {
      Iterator<ParticipantType> iter = ((OrganizationType) org).getParticipant()
            .iterator();
      while (iter.hasNext())
      {
         ParticipantType participant = iter.next();
         if (participant.getParticipant() instanceof OrganizationType)
         {
            removeOrganization(model, (OrganizationType) participant.getParticipant());
         }
         else
         {
            model.getRole().remove(participant.getParticipant());
         }

         // TODO - check why if a participant is deleted from member list get null pointer at
         // Modification.determineChangedElement
         //iter.remove();
      }
      model.getOrganization().remove(org);
   }

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return new ModelBuilderFacade(springContext.getBean(ModelService.class)
            .getModelManagementStrategy());
   }
}
