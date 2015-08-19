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
package org.eclipse.stardust.ui.web.modeler.authorization;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XPDLFinderUtils;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.ActivityUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.modeling.authorization.Constants;
import org.eclipse.stardust.modeling.authorization.Permission;


public class AuthorizationUtils
{
   public static final String ACTIVITY_SCOPE = "activity"; //$NON-NLS-1$
   public static final String DATA_SCOPE = "data"; //$NON-NLS-1$
   public static final String PROCESS_DEFINITION_SCOPE = "processDefinition"; //$NON-NLS-1$
   public static final String MODEL_SCOPE = "model"; //$NON-NLS-1$
   
   public static List<Permission> getPermissions(IExtensibleElement element)
   {
      ArrayList<Permission> permissions = new ArrayList<Permission>();
      String scope = getScope(element);
      if (element != null)
      {
         List<IConfigurationElement> extensions = AuthorizationExtensionRegistry.getInstance().getExtensionList(
               Constants.PLUGIN_ID, "org.eclipse.stardust.modeling.authorization.modelElementPermission");
         for (IConfigurationElement config : extensions)
         {
            if (config.getAttribute(Constants.SCOPE_ATTRIBUTE).equals(scope))
            {
               Permission permission = new Permission(config, (IExtensibleElement) element);
               if (!isInteractiveActivity(element) && permission.isDefaultOwner())
               {
                  ModelType model = ModelUtils.findContainingModel(element);
                  RoleType admin = (RoleType) ModelUtils.findIdentifiableElement(
                        model.getRole(), PredefinedConstants.ADMINISTRATOR_ROLE);
                  permission.setDefault(admin);
               }
               permissions.add(permission);
            }
         }
      }
      return permissions;
   }
   
   public static String getScope(IExtensibleElement element)
   {
      if (element instanceof ModelType)
      {
         return MODEL_SCOPE;
      }
      if (element instanceof ProcessDefinitionType)
      {
         return PROCESS_DEFINITION_SCOPE;
      }
      if (element instanceof DataType)
      {
         return DATA_SCOPE;
      }
      if (element instanceof ActivityType)
      {
         return ACTIVITY_SCOPE;
      }
      return null;
   }
      
   public static boolean isInteractiveActivity(IExtensibleElement element)
   {
      if (element instanceof ActivityType)
      {
         if ((ActivityUtil.isInteractive((ActivityType) element)))
         {
            return true;
         }
      }
      return false;
   }

   public static JsonArray getPermissionsJson(IExtensibleElement element)
   {
      List<Permission> permissions = getPermissions(element);
      JsonArray permissionsJson = new JsonArray();
      for (Iterator<Permission> i = permissions.iterator(); i.hasNext();)
      {
         JsonObject permissionJson = new JsonObject();
         Permission permission = i.next();
         JsonArray defaultParticipantsJson = createParticipantsArray(permission.getDefaultParticipants(), permission);
         JsonArray fixedParticipantsJson = createParticipantsArray(permission.getFixedParticipants(), permission);
         JsonArray participantsJson = createParticipantsArray(permission.getParticipants(), permission);
         permissionJson.addProperty("id", permission.getId());
         permissionJson.addProperty("isEmpty", permission.isEmpty());
         permissionJson.addProperty("isAll", permission.isALL());
         permissionJson.addProperty("defaultAll", permission.isDefaultAll());
         permissionJson.addProperty("defaultOwner", permission.isDefaultOwner());  
         permissionJson.add("participants", participantsJson);
         permissionJson.add("defaultParticipants", defaultParticipantsJson);
         permissionJson.add("fixedParticipants", fixedParticipantsJson);            
         permissionsJson.add(permissionJson);
      }
      return permissionsJson;
   }

   private static JsonArray createParticipantsArray(List<IModelParticipant> participants,
         Permission permission)
   {
      JsonArray participantsJson = new JsonArray();
      for (Iterator<IModelParticipant> j = participants.iterator(); j.hasNext();)
      {
         IModelParticipant participant = j.next();
         ModelType model = ModelUtils.findContainingModel(participant);
         JsonObject participantJson = new JsonObject();
         participantJson.addProperty(ModelerConstants.PARTICIPANT_FULL_ID,
               model.getId() + ":" + participant.getId());
         participantsJson.add(participantJson);
      }
      return participantsJson;
   }

   public static void addParticipant(IExtensibleElement element, Permission permission,
         String participantFullID)
   {
      ModelType model = ModelUtils.findContainingModel(element);
      IModelParticipant participant = XPDLFinderUtils.findParticipant(model, participantFullID.split(":")[1]);
      permission.addParticipant(participant);
   }

   public static void addParticipants(IExtensibleElement element, Permission permission,
         JsonArray participants)
   {
      ModelType model = ModelUtils.findContainingModel(element);
      for (Iterator<JsonElement> j = participants.iterator(); j.hasNext();)
      {
        JsonObject participantJson = j.next().getAsJsonObject();
        String participantID = participantJson
                     .get(ModelerConstants.PARTICIPANT_FULL_ID).getAsString()
                     .split(":")[1];
         IModelParticipant participant = XPDLFinderUtils.findParticipant(model,
                     participantID);
        permission.addParticipant(participant);      
      }
   }
   
   public static void removeParticipant(IExtensibleElement element, Permission permission,
         String participantFullID)
   {
      ModelType model = ModelUtils.findContainingModel(element);
      IModelParticipant participant = XPDLFinderUtils.findParticipant(model, participantFullID.split(":")[1]);
      permission.removeParticipant(participant);
   }

   public static void removeParticipants(IExtensibleElement element, Permission permission,
         JsonArray participants)
   {
      ModelType model = ModelUtils.findContainingModel(element);
      for (Iterator<JsonElement> j = participants.iterator(); j.hasNext();)
      {
        JsonObject participantJson = j.next().getAsJsonObject();
        String participantID = participantJson
                     .get(ModelerConstants.PARTICIPANT_FULL_ID).getAsString()
                     .split(":")[1];
         IModelParticipant participant = XPDLFinderUtils.findParticipant(model,
                     participantID);
        permission.removeParticipant(participant);      
      }
   }
   
   public static void savePermissions(IExtensibleElement element, List<Permission> permissions)
   {
      List<AttributeType> attributes = element.getAttribute();
      for (int i = attributes.size() - 1; i >= 0; i--)
      {
         AttributeType attribute = attributes.get(i);
         if (attribute.getName() != null && attribute.getName().startsWith(Permission.SCOPE))
         {
            attributes.remove(i);
         }
      }
      for (Permission permission : permissions)
      {
         permission.save(element);
      }
   }
   
   public static Permission getPermission(IExtensibleElement element, List<Permission> permissions, String permissionID)
   {      
      for (Iterator<Permission> i = permissions.iterator(); i.hasNext();)
      {
         Permission permission = i.next();
         if (permission.getId().equals(permissionID))
         {
            return permission;
         }
      }
      return null;
   }


   
}