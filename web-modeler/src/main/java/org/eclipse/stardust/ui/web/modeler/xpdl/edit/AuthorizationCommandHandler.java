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

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.modeling.authorization.Permission;
import org.eclipse.stardust.ui.web.modeler.authorization.AuthorizationUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;

/**
 * @author rainer.pielmann
 */
@CommandHandler
public class AuthorizationCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   @OnCommand(commandId = "permission.addParticipant")
   public void addParticipant(ModelType modelType, IExtensibleElement element, JsonObject request)
   {      
      String permissionID = request.get("permissionID").getAsString();
      String participantFullID = request.get(ModelerConstants.PARTICIPANT_FULL_ID).getAsString();
      List<Permission> permissions = AuthorizationUtils.getPermissions(element);
      Permission permission = AuthorizationUtils.getPermission(element, permissions, permissionID);
      AuthorizationUtils.addParticipant(element, permission, participantFullID);
      AuthorizationUtils.savePermissions(element, permissions);
   }
   
   @OnCommand(commandId = "permission.addParticipants")
   public void addParticipants(ModelType modelType, IExtensibleElement element, JsonObject request)
   {
      JsonArray participants = request.get("participants").getAsJsonArray();
      String permissionID = request.get("permissionID").getAsString();      
      List<Permission> permissions = AuthorizationUtils.getPermissions(element);
      Permission permission = AuthorizationUtils.getPermission(element, permissions, permissionID);
      AuthorizationUtils.addParticipants(element, permission, participants);
      AuthorizationUtils.savePermissions(element, permissions);     
   }
   
   @OnCommand(commandId = "permission.removeParticipant")
   public void removeParticipant(ModelType modelType, IExtensibleElement element, JsonObject request)
   {
      String permissionID = request.get("permissionID").getAsString();
      String participantFullID = request.get(ModelerConstants.PARTICIPANT_FULL_ID).getAsString();
      List<Permission> permissions = AuthorizationUtils.getPermissions(element);
      Permission permission = AuthorizationUtils.getPermission(element, permissions, permissionID);
      AuthorizationUtils.removeParticipant(element, permission, participantFullID);
      AuthorizationUtils.savePermissions(element, permissions);
   }
   
   @OnCommand(commandId = "permission.removeParticipants")
   public void removeParticipants(ModelType modelType, IExtensibleElement element, JsonObject request)
   {
      JsonArray participants = request.get("participants").getAsJsonArray();
      String permissionID = request.get("permissionID").getAsString();      
      List<Permission> permissions = AuthorizationUtils.getPermissions(element);
      Permission permission = AuthorizationUtils.getPermission(element, permissions, permissionID);
      AuthorizationUtils.removeParticipants(element, permission, participants);
      AuthorizationUtils.savePermissions(element, permissions);        
   }
   
   @OnCommand(commandId = "permission.setALL")
   public void setALL(ModelType modelType, IExtensibleElement element, JsonObject request) 
   {
      String permissionID = request.get("permissionID").getAsString();      
      List<Permission> permissions = AuthorizationUtils.getPermissions(element);
      Permission permission = AuthorizationUtils.getPermission(element, permissions, permissionID);
      permission.setALL();
      AuthorizationUtils.savePermissions(element, permissions);
   }
   
   @OnCommand(commandId = "permission.unsetALL")
   public void unsetALL(ModelType modelType, IExtensibleElement element, JsonObject request) 
   {
      String permissionID = request.get("permissionID").getAsString();      
      List<Permission> permissions = AuthorizationUtils.getPermissions(element);
      Permission permission = AuthorizationUtils.getPermission(element, permissions, permissionID);
      permission.unsetALL();
      AuthorizationUtils.savePermissions(element, permissions);
   }
   
   @OnCommand(commandId = "permission.setOWNER")
   public void setOWNER(ModelType modelType, IExtensibleElement element, JsonObject request) 
   {
      String permissionID = request.get("permissionID").getAsString();      
      List<Permission> permissions = AuthorizationUtils.getPermissions(element);
      Permission permission = AuthorizationUtils.getPermission(element, permissions, permissionID);
      permission.setOWNER();
      AuthorizationUtils.savePermissions(element, permissions);
   }
   
   @OnCommand(commandId = "permission.unsetOWNER")
   public void unsetOWNER(ModelType modelType, IExtensibleElement element, JsonObject request) 
   {
      String permissionID = request.get("permissionID").getAsString();      
      List<Permission> permissions = AuthorizationUtils.getPermissions(element);
      Permission permission = AuthorizationUtils.getPermission(element, permissions, permissionID);
      permission.unsetOWNER();
      AuthorizationUtils.savePermissions(element, permissions);
   }
   
   @OnCommand(commandId = "permission.restoreDefaults")
   public void restoreDefaults(ModelType modelType, IExtensibleElement element, JsonObject request) 
   {
      String permissionID = request.get("permissionID").getAsString();      
      List<Permission> permissions = AuthorizationUtils.getPermissions(element);
      Permission permission = AuthorizationUtils.getPermission(element, permissions, permissionID);
      permission.restoreDefaults();
      AuthorizationUtils.savePermissions(element, permissions);
   }

}