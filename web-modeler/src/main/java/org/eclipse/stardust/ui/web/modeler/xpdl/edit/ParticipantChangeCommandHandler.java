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

package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils.CommandHandlerUtils;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

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
      String roleName = extractString(request, ModelerConstants.NAME_PROPERTY);
      RoleType role = null;
      synchronized (model)
      {
         role = getModelBuilderFacade().createRole(model, null, roleName);
      }
 
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
      String orgName = extractString(request, ModelerConstants.NAME_PROPERTY);
      OrganizationType org = null;
      synchronized (model)
      {
         org = getModelBuilderFacade().createOrganization(model, null, orgName);
      }

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(org);
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "conditionalPerformer.create")
   public void createConditionalPerformer(ModelType model, JsonObject request)
   {
      String conditionalPerformerName = extractString(request,
            ModelerConstants.NAME_PROPERTY);
      ConditionalPerformerType conditionalPerformer = null;
      synchronized (model)
      {
         conditionalPerformer = getModelBuilderFacade().createConditionalPerformer(model,
               null, conditionalPerformerName);
      }

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(conditionalPerformer);
   }

   /**
    * @param org
    * @param request
    */
   @OnCommand(commandId = "role.create")
   public void addRole(ModelType model, OrganizationType org, JsonObject request)
   {
      String roleID = extractString(request, ModelerConstants.ID_PROPERTY);
      String roleName = extractString(request, ModelerConstants.NAME_PROPERTY);
      RoleType role = null;
      synchronized (model)
      {
         role = getModelBuilderFacade().createRole(model, roleID, roleName);
         getModelBuilderFacade().addOrganizationParticipant(org, role);
      }

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(role);
   }

   /**
    * @param org
    * @param request
    */
   @OnCommand(commandId = "organization.create")
   public void addOrganization(ModelType model, OrganizationType org, JsonObject request)
   {
      String orgID = extractString(request, ModelerConstants.ID_PROPERTY);
      String orgName = extractString(request, ModelerConstants.NAME_PROPERTY);
      OrganizationType newOrg = null;
      synchronized (model)
      {
         newOrg = getModelBuilderFacade().createOrganization(model, orgID, orgName);
         getModelBuilderFacade().addOrganizationParticipant(org, newOrg);
      }

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(newOrg);
   }

   /**
    * @param org
    * @param request
    */
   @OnCommand(commandId = "organization.updateTeamLeader")
   public void updateTeamLeader(ModelType model, OrganizationType org, JsonObject request)
   {
      String teamLeaderUUID = extractString(request, ModelerConstants.UUID_PROPERTY);
      RoleType tealLeader = (RoleType) modelService().uuidMapper().getEObject(teamLeaderUUID);
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
            List<OrganizationType> parentOrgs = ModelBuilderFacade.getParentOrganizations(
                  model, modelParticipantInfo);

            for (OrganizationType org : parentOrgs)
            {
               ParticipantType removeMember = null;
               for(ParticipantType child : org.getParticipant())
               {
                  if(modelParticipantInfo.equals(child.getParticipant()))
                  {
                     removeMember = child;
                     break;
                  }
               }

               if(removeMember != null)
               {
                  org.getParticipant().remove(removeMember);
               }
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
      }

      List<OrganizationType> parentOrgs = ModelBuilderFacade.getParentOrganizations(model, org);
      for (OrganizationType organization : parentOrgs)
      {
         ParticipantType removeMember = null;
         for(ParticipantType child : organization.getParticipant())
         {
            if(org.equals(child.getParticipant()))
            {
               removeMember = child;
               break;
            }
         }

         if(removeMember != null)
         {
            organization.getParticipant().remove(removeMember);
         }
      }
      model.getOrganization().remove(org);
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