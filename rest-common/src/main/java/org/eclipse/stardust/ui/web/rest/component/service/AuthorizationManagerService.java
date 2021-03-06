/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.RuntimePermissions;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.ParticipantInfoUtil;
import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.LaunchPanel;
import org.eclipse.stardust.ui.web.common.PerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.UiElement;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.response.PermissionDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.PermissionDTO.ParticipantDTO;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.authorization.PermissionsDetails;
import org.eclipse.stardust.ui.web.viewscommon.views.authorization.UiPermissionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthorizationManagerService
{
   private static final ModelParticipantComparator MODEL_PARTICIPANT_COMPARATOR = new ModelParticipantComparator();

   private static final String PROPERTY_KEY_PREFIX = "views.authorizationManagerView.permission.model.";

   private static enum PermissionType {
      general, perspectives, launchPanels, views, processDefinitions, activities, data, globalExtensions
   }

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   /**
    * 
    * @return
    */
   public Map<String, Set<PermissionDTO>> fetchPermissions()
   {
      PermissionsDetails permissions = getPermissionDetails(true);
      Map<String, Set<PermissionDTO>> allPermissions = new TreeMap<String, Set<PermissionDTO>>();
      allPermissions.putAll(buildGeneralAndModelPermissions(permissions));
      allPermissions.putAll(buildUiPermissions(permissions));
      return allPermissions;
   }

   /**
    * @param allow
    * @param deny
    * @param selectedParticipants
    */
   public Set<PermissionDTO> updatePermissions(Set<String> allow, Set<String> deny, Set<String> selectedParticipants,
         boolean overwrite)
   {
      PermissionsDetails permissions = getPermissionDetails(true);
      Set<String> permissionsToBeUpdated = new HashSet<String>();
      if (allow != null)
      {
         permissionsToBeUpdated.addAll(allow);
         updateGrants(allow, selectedParticipants, permissions, overwrite);
      }
      if (deny != null)
      {
         permissionsToBeUpdated.addAll(deny);
         updateDeniedGrants(deny, selectedParticipants, permissions, overwrite);
      }

      savePermissions(permissions);

      // return flat permissionDTO
      permissions = getPermissionDetails(true);
      Set<PermissionDTO> updatedPermissions = new TreeSet<PermissionDTO>();

      for (String permissionId : permissionsToBeUpdated)
      {
         PermissionDTO p = new PermissionDTO(permissionId, permissionId);
         populateGrants(p, permissions);
         updatedPermissions.add(p);
      }

      return updatedPermissions;
   }

   /**
    * 
    * @param permissionId
    * @return
    */
   /*
    * public Map<String, Set<PermissionDTO>> restoreGrants(String permissionIdStr) {
    * String[] permissionIds = permissionIdStr.split(","); PermissionsDetails permissions
    * = getPermissionDetails(false);
    * 
    * for (String permissionId : permissionIds) { permissions.setGrants2(permissionId,
    * null); permissions.setDeniedGrants(permissionId, null); }
    * 
    * savePermissions(permissions); return fetchPermissions(); }
    */
   /**
    * 
    * @param sourceParticipant
    * @param targetParticipant
    * @return
    */
   public Map<String, Set<PermissionDTO>> cloneParticipant(Set<String> sourceParticipants,
         Set<String> targetParticipants)
   {
      PermissionsDetails permissions = getPermissionDetails(true);
      Collection<ParticipantDTO> participants = fetchPermissionsForParticipants(sourceParticipants, permissions);

      Set<String> allow = new HashSet<String>();
      Set<String> deny = new HashSet<String>();
      for (ParticipantDTO participantDTO : participants)
      {
         allow.addAll(participantDTO.allow);
         deny.addAll(participantDTO.deny);
      }

      updateGrants(allow, targetParticipants, permissions, false);
      updateDeniedGrants(deny, targetParticipants, permissions, false);
      savePermissions(permissions);
      return fetchPermissions();
   }

/*   *//**
    * 
    * @param participantQualifiedIds
    * @return
    *//*
   public Collection<ParticipantDTO> fetchPermissionsForParticipants(Set<String> participantQualifiedIds)
   {
      PermissionsDetails permissions = getPermissionDetails(true);
      return fetchPermissionsForParticipants(participantQualifiedIds, permissions);
   }
*/
   /**
    * Reset Participants meaning it removes all explicit permissions stored in Preference
    * Table
    * 
    * @param participantQualifiedIds
    * @return
    */
   public Map<String, Set<PermissionDTO>> restoreParticipants(Set<String> participantQualifiedIds)
   {
      PermissionsDetails permissions = getPermissionDetails(false);

      if (CollectionUtils.isEmpty(participantQualifiedIds))
      {
         // TODO: throw exception
      }

      RuntimePermissions runtimePermissions = permissions.getGeneralPermission();
      List<String> permissionIds = new ArrayList<String>(runtimePermissions.getAllPermissionIds());

      Map<String, Serializable> uiPermissions = permissions.getUIPermissionMap();

      for (String permissionIdT : uiPermissions.keySet())
      {
         String permissionId = StringUtils.substringBeforeLast(permissionIdT, ".");
         permissionIds.add(permissionId);
      }

      for (String permissionId : permissionIds)
      {
         Set<ModelParticipantInfo> grants = permissions.getGrants2(permissionId);
         restoreParticipant(participantQualifiedIds, grants);
         permissions.setGrants2(permissionId, grants);
         Set<ModelParticipantInfo> deniedGrants = permissions.getDeniedGrants(permissionId);
         restoreParticipant(participantQualifiedIds, deniedGrants);
         permissions.setDeniedGrants(permissionId, deniedGrants);
      }
      savePermissions(permissions);

      return fetchPermissions();
   }

   /**
    * Note this method returns only explicit permissions for the participant which
    * persisted in preference table
    * 
    * @param participantQualifiedIds
    * @param permissions
    * @return
    */
   private Collection<ParticipantDTO> fetchPermissionsForParticipants(Set<String> participantQualifiedIds,
         PermissionsDetails permissions)
   {
      Map<String, ParticipantDTO> participantMap = new TreeMap<String, PermissionDTO.ParticipantDTO>();

      if (CollectionUtils.isEmpty(participantQualifiedIds))
      {
         return participantMap.values();
      }

      RuntimePermissions runtimePermissions = permissions.getGeneralPermission();
      List<String> permissionIds = new ArrayList<String>(runtimePermissions.getAllPermissionIds());

      Map<String, Serializable> uiPermissions = permissions.getUIPermissionMap();

      for (String permissionIdT : uiPermissions.keySet())
      {
         String permissionId = StringUtils.substringBeforeLast(permissionIdT, ".");
         permissionIds.add(permissionId);
      }

      for (String permissionId : permissionIds)
      {
         Set<ModelParticipantInfo> grants = permissions.getGrants2(permissionId);
         updatePermissions(participantQualifiedIds, participantMap, grants, permissionId, true);
         Set<ModelParticipantInfo> deniedGrants = permissions.getDeniedGrants(permissionId);
         updatePermissions(participantQualifiedIds, participantMap, deniedGrants, permissionId, false);
      }

      return participantMap.values();
   }

   /**
    * 
    * @param participantQualifiedIds
    * @param grants
    */
   private void restoreParticipant(Set<String> participantQualifiedIds, Set<ModelParticipantInfo> grants)
   {
      ModelParticipantInfo participantInfo = null;
      if (grants == null)
      {
         return;
      }
      for (ModelParticipantInfo modelParticipantInfo : grants)
      {
         if (modelParticipantInfo instanceof QualifiedModelParticipantInfo)
         {
            if (participantQualifiedIds.contains(modelParticipantInfo.getQualifiedId()))
            {
               participantInfo = modelParticipantInfo;
               break;
            }
         }
         else
         {
            if (participantQualifiedIds.contains(modelParticipantInfo.getId()))
            {
               participantInfo = modelParticipantInfo;
               break;
            }
         }
      }
      grants.remove(participantInfo);
   }

   /**
    * @param participantQualifiedIds
    * @param participantMap
    * @param grants
    * @param permissionId
    * @param allow
    */
   private void updatePermissions(Set<String> participantQualifiedIds, Map<String, ParticipantDTO> participantMap,
         Set<ModelParticipantInfo> grants, String permissionId, boolean allow)
   {
      ParticipantDTO participant;
      String label;

      if (grants == null)
      {
         return;
      }

      for (ModelParticipantInfo modelParticipantInfo : grants)
      {
         if (modelParticipantInfo instanceof QualifiedModelParticipantInfo)
         {
            if (participantQualifiedIds.contains(modelParticipantInfo.getQualifiedId()))
            {
               if (!participantMap.containsKey(modelParticipantInfo.getQualifiedId()))
               {
                  QualifiedModelParticipantInfo qualifiedParticipantInfo = (QualifiedModelParticipantInfo) modelParticipantInfo;
                  String modelId = ModelUtils.extractModelId(qualifiedParticipantInfo.getQualifiedId());
                  Model model = ModelCache.findModelCache().getActiveModel(modelId);
                  label = I18nUtils.getParticipantName(model.getParticipant(modelParticipantInfo.getId()));
                  participant = new ParticipantDTO(modelParticipantInfo.getQualifiedId(), label);
                  participant.allow = new HashSet<String>();
                  participant.deny = new HashSet<String>();
                  participantMap.put(modelParticipantInfo.getQualifiedId(), participant);
               }
               else
               {
                  participant = participantMap.get(modelParticipantInfo.getQualifiedId());
               }
               if (allow)
               {
                  participant.allow.add(permissionId);
               }
               else
               {
                  participant.deny.add(permissionId);
               }
            }
         }
         else
         {
            if (participantQualifiedIds.contains(modelParticipantInfo.getId()))
            {
               if (!participantMap.containsKey(modelParticipantInfo.getId()))
               {
                  label = I18nUtils.getParticipantName(ModelCache.findModelCache().getParticipant(
                        modelParticipantInfo.getId()));
                  participant = new ParticipantDTO(modelParticipantInfo.getId(), label);
                  participant.allow = new HashSet<String>();
                  participant.deny = new HashSet<String>();
                  participantMap.put(modelParticipantInfo.getId(), participant);
               }
               else
               {
                  participant = participantMap.get(modelParticipantInfo.getId());
               }
               if (allow)
               {
                  participant.allow.add(permissionId);
               }
               else
               {
                  participant.deny.add(permissionId);
               }
            }
         }
      }
   }

   /**
    * 
    * @param permissions
    */
   private void savePermissions(PermissionsDetails permissions)
   {
      AdministrationService administrationService = serviceFactoryUtils.getAdministrationService();
      administrationService.setGlobalPermissions(permissions.getGeneralPermission());
      UiPermissionUtils.savePreferences(administrationService, permissions.getUIPermissionMap());
   }

   /**
    * 
    * @return
    */
   private PermissionsDetails getPermissionDetails(boolean fetchDefaultPermissions)
   {
      // fetch permission
      AdministrationService administrationService = serviceFactoryUtils.getAdministrationService();

      // UI permissions
      PermissionsDetails permissions = new PermissionsDetails(UiPermissionUtils.getAllPermissions(
            administrationService, fetchDefaultPermissions));
      // general Permissions
      RuntimePermissions runtimePermissionsDetails = (RuntimePermissions) administrationService.getGlobalPermissions();
      permissions.setGeneralPermission(runtimePermissionsDetails);

      return permissions;
   }

   /**
    * @param permissionIds
    * @param selectedParticipants
    * @param permissions
    */
   private void updateGrants(Set<String> permissionIds, Set<String> selectedParticipants,
         PermissionsDetails permissions, boolean overwrite)
   {
      for (String permissionId : permissionIds)
      {
         Set<ModelParticipantInfo> participants = permissions.getGrants2(permissionId);
         if (overwrite)
         {
            participants.clear();
         }

         for (String qualifiedParticipantId : selectedParticipants)
         {
            boolean isExist = false;

            for (ModelParticipantInfo info : participants)
            {
               if (info instanceof QualifiedModelParticipantInfo)
               {
                  if (((QualifiedModelParticipantInfo) info).getQualifiedId().equals(qualifiedParticipantId))
                  {
                     isExist = true;
                     break;
                  }
               }
               else
               {
                  if (info.getId().equals(qualifiedParticipantId))
                  {
                     isExist = true;
                     break;
                  }
               }
            }

            if (!isExist)
            {
               ModelParticipantInfo selectedParticipant = ParticipantInfoUtil
                     .newModelParticipantInfo(qualifiedParticipantId);
               participants.add(selectedParticipant);
            }
         }
         permissions.setGrants2(permissionId, participants);
      }
   }

   /**
    * @param permissionIds
    * @param selectedParticipants
    * @param permissions
    */
   private void updateDeniedGrants(Set<String> permissionIds, Set<String> selectedParticipants,
         PermissionsDetails permissions, boolean overwrite)
   {
      for (String permissionId : permissionIds)
      {
         Set<ModelParticipantInfo> participants = permissions.getDeniedGrants(permissionId);

         if (overwrite)
         {
            participants.clear();
         }

         for (String qualifiedParticipantId : selectedParticipants)
         {
            boolean isExist = false;

            for (ModelParticipantInfo info : participants)
            {
               if (info instanceof QualifiedModelParticipantInfo)
               {
                  if (((QualifiedModelParticipantInfo) info).getQualifiedId().equals(qualifiedParticipantId))
                  {
                     isExist = true;
                     break;
                  }
               }
               else
               {
                  if (info.getId().equals(qualifiedParticipantId))
                  {
                     isExist = true;
                     break;
                  }
               }
            }

            if (!isExist)
            {
               ModelParticipantInfo selectedParticipant = ParticipantInfoUtil
                     .newModelParticipantInfo(qualifiedParticipantId);
               participants.add(selectedParticipant);
            }
         }

         permissions.setDeniedGrants(permissionId, participants);
      }
   }

   /**
    * @param permissions
    * @return
    */
   private Map<String, Set<PermissionDTO>> buildGeneralAndModelPermissions(PermissionsDetails permissions)
   {
      Map<String, Set<PermissionDTO>> GeneralAndModelPermissions = new TreeMap<String, Set<PermissionDTO>>();

      RuntimePermissions runtimePermissions = permissions.getGeneralPermission();

      Set<PermissionDTO> generalPermissions = new TreeSet<PermissionDTO>();
      GeneralAndModelPermissions.put(PermissionType.general.name(), generalPermissions);

      Set<PermissionDTO> processes = new TreeSet<PermissionDTO>();
      GeneralAndModelPermissions.put(PermissionType.processDefinitions.name(), processes);

      Set<PermissionDTO> activities = new TreeSet<PermissionDTO>();
      GeneralAndModelPermissions.put(PermissionType.activities.name(), activities);

      Set<PermissionDTO> datas = new TreeSet<PermissionDTO>();
      GeneralAndModelPermissions.put(PermissionType.data.name(), datas);

      List<String> permissionIds = new ArrayList<String>(runtimePermissions.getAllPermissionIds());

      for (String permissionId : permissionIds)
      {
         if (UiPermissionUtils.isGeneralPermissionId(permissionId))
         {
            PermissionDTO p = new PermissionDTO(permissionId, MessagesViewsCommonBean.getInstance().getString(
                  PROPERTY_KEY_PREFIX + permissionId));
            /* p.type = PermissionType.generalPermission.name(); */
            populateGrants(p, permissions);
            generalPermissions.add(p);
         }
         else
         {
            PermissionDTO p = new PermissionDTO(permissionId, MessagesViewsCommonBean.getInstance().getString(
                  PROPERTY_KEY_PREFIX + permissionId));

            populateGrants(p, permissions);

            if (UiPermissionUtils.isProcessPermissionId(permissionId))
            {
               processes.add(p);
            }
            else if (UiPermissionUtils.isActivityPermissionId(permissionId))
            {
               activities.add(p);
            }
            else
            {
               datas.add(p);
            }
         }
      }
      return GeneralAndModelPermissions;
   }

   /**
    * 
    * @param permissions
    * @return
    */
   private Map<String, Set<PermissionDTO>> buildUiPermissions(PermissionsDetails permissions)
   {
      Map<String, PerspectiveDefinition> perspectives = PortalUiController.getInstance().getSystemPerspectives();

      List<IPerspectiveDefinition> allPerspectives = new ArrayList<IPerspectiveDefinition>();
      for (IPerspectiveDefinition perspectiveDef : perspectives.values())
      {
         allPerspectives.add(perspectiveDef);
      }

      // Sort Perspectives
      Collections.sort(allPerspectives, new Comparator<IPerspectiveDefinition>()
      {
         public int compare(IPerspectiveDefinition arg0, IPerspectiveDefinition arg1)
         {
            // For time being till Authorization is not implemented for
            // Admin Perspective sort in reverse (descending) order
            // so that other users can login into portal
            return arg0.getLabel().compareTo(arg1.getLabel());
         }
      });

      Map<String, Set<PermissionDTO>> uiPermissions = new TreeMap<String, Set<PermissionDTO>>();

      Map<String, PermissionDTO> globalElements = new TreeMap<String, PermissionDTO>();

      Set<PermissionDTO> perspectivePermissions = new TreeSet<PermissionDTO>();
      uiPermissions.put(PermissionType.perspectives.name(), perspectivePermissions);

      Set<PermissionDTO> globalExtnPermissions = new TreeSet<PermissionDTO>();
      uiPermissions.put(PermissionType.globalExtensions.name(), globalExtnPermissions);

      for (IPerspectiveDefinition perspective : allPerspectives)
      {
         // add perspectives
         PermissionDTO perspectiveDTO = new PermissionDTO(UiPermissionUtils.getPermissionId(perspective.getName()),
               perspective.getLabel());
         perspectivePermissions.add(perspectiveDTO);
         populateGrants(perspectiveDTO, permissions);

         // add launch panels
         perspectiveDTO.launchPanels = new TreeSet<PermissionDTO>();
         List<LaunchPanel> launchPanels = perspective.getLaunchPanels();
         for (LaunchPanel launchPanel : launchPanels)
         {
            if (launchPanel.isGlobal())
            {
               if (!globalElements.containsKey(launchPanel.getDefinedIn()))
               {
                  PermissionDTO gPerspectiveDTO = new PermissionDTO(UiPermissionUtils.getPermissionId(perspective
                        .getName()), perspective.getLabel());
                  globalExtnPermissions.add(gPerspectiveDTO);
                  globalElements.put(launchPanel.getDefinedIn(), gPerspectiveDTO);
               }

               if (globalElements.get(launchPanel.getDefinedIn()).launchPanels == null)
               {
                  globalElements.get(launchPanel.getDefinedIn()).launchPanels = new TreeSet<PermissionDTO>();
               }

               PermissionDTO lPanelDto = new PermissionDTO(UiPermissionUtils.getPermissionId(launchPanel.getName()),
                     getUiElementLabel(launchPanel));
               populateGrants(lPanelDto, permissions);
               globalElements.get(launchPanel.getDefinedIn()).launchPanels.add(lPanelDto);
            }
            else
            {
               PermissionDTO lPanelDto = new PermissionDTO(UiPermissionUtils.getPermissionId(launchPanel.getName()),
                     getUiElementLabel(launchPanel));
               populateGrants(lPanelDto, permissions);
               perspectiveDTO.launchPanels.add(lPanelDto);
            }
         }

         // add view definitions
         perspectiveDTO.views = new TreeSet<PermissionDTO>();
         List<ViewDefinition> viewDefinitions = perspective.getViews();

         for (ViewDefinition viewDefinition : viewDefinitions)
         {
            if (viewDefinition.isGlobal())
            {
               String extName = viewDefinition.getDefinedIn();

               if (!globalElements.containsKey(extName))
               {
                  PermissionDTO gPerspectiveDTO = new PermissionDTO(UiPermissionUtils.getPermissionId(extName),
                        UiPermissionUtils.getPermisionLabel(extName));
                  globalExtnPermissions.add(gPerspectiveDTO);
                  globalElements.put(viewDefinition.getDefinedIn(), gPerspectiveDTO);
               }

               if (globalElements.get(viewDefinition.getDefinedIn()).views == null)
               {
                  globalElements.get(viewDefinition.getDefinedIn()).views = new TreeSet<PermissionDTO>();
               }

               PermissionDTO viewDto = new PermissionDTO(UiPermissionUtils.getPermissionId(viewDefinition.getName()),
                     getUiElementLabel(viewDefinition));
               populateGrants(viewDto, permissions);
               globalElements.get(viewDefinition.getDefinedIn()).views.add(viewDto);
            }
            else
            {
               // add view and its permissions
               PermissionDTO viewDto = new PermissionDTO(UiPermissionUtils.getPermissionId(viewDefinition.getName()),
                     getUiElementLabel(viewDefinition));
               populateGrants(viewDto, permissions);
               perspectiveDTO.views.add(viewDto);
            }
         }
      }

      return uiPermissions;
   }

   /**
    * 
    * @param p
    * @param permissions
    */
   private void populateGrants(PermissionDTO p, PermissionsDetails permissions)
   {
      p.allow = new TreeSet<ParticipantDTO>();
      p.deny = new TreeSet<ParticipantDTO>();

      if (permissions.hasAllGrant2(p.id))
      {
         p.allow.add(ParticipantDTO.ALL);
      }

      // update grants
      Set<ModelParticipantInfo> grants = permissions.getGrants2(p.id);
      p.allow.addAll(transformGrantsToDTO(grants));

      // update denied grants
      Set<ModelParticipantInfo> deny = permissions.getDeniedGrants(p.id);
      p.deny.addAll(transformGrantsToDTO(deny));
   }

   /**
    * 
    * @param vd
    * @return
    */
   private static String getUiElementLabel(UiElement vd)
   {
      return UiPermissionUtils.getUiElementLabel(vd);
   }

   /**
    * 
    * @param prefs
    * @return
    */
   private List<ParticipantDTO> transformGrantsToDTO(Set<ModelParticipantInfo> grants)
   {
      List<ParticipantDTO> pList = new ArrayList<ParticipantDTO>();
      List<ModelParticipantInfo> grantList = new ArrayList<ModelParticipantInfo>(grants);
      Collections.sort(grantList, MODEL_PARTICIPANT_COMPARATOR);

      // add Participant node
      for (ModelParticipantInfo info : grantList)
      {
         ParticipantDTO participant;

         String label = "";

         if (info instanceof QualifiedModelParticipantInfo)
         {
            QualifiedModelParticipantInfo qualifiedParticipantInfo = (QualifiedModelParticipantInfo) info;
            String modelId = ModelUtils.extractModelId(qualifiedParticipantInfo.getQualifiedId());
            Model model = ModelCache.findModelCache().getActiveModel(modelId);
            // predefined model may not be available
            if (model != null)
            {
               label = I18nUtils.getParticipantName(model.getParticipant(info.getId()));
            }
            else
            {
               label = info.getName();
            }
            
            participant = new ParticipantDTO(info.getQualifiedId(), label);
         }
         else
         {
            if (Authorization2.OWNER.equals(info.getQualifiedId()))
            {
               label = MessagesViewsCommonBean.getInstance().getString(
                     "views.authorizationManagerView.permission.model.participant.owner");
            }
            else
            {
               label = I18nUtils.getParticipantName(ModelCache.findModelCache().getParticipant(info.getId()));
            }
            participant = new ParticipantDTO(info.getId(), label);
         }

         pList.add(participant);
      }
      
      Collections.sort(pList);
      return pList;
   }
}

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
class ModelParticipantComparator implements Comparator<ModelParticipantInfo>
{
   public int compare(ModelParticipantInfo part1, ModelParticipantInfo part2)
   {
      return part1.getName().compareTo(part2.getName());
   }
}