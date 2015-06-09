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
package org.eclipse.stardust.ui.web.rest.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.RuntimePermissions;
import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.LaunchPanel;
import org.eclipse.stardust.ui.web.common.PerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.UiElement;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.rest.service.dto.PermissionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.PermissionDTO.ParticipantDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
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
   public Map<String, List<PermissionDTO>> fetchPermissions()
   {
      // fetch permission
      AdministrationService administrationService = serviceFactoryUtils.getAdministrationService();

      // UI permissions
      PermissionsDetails permissions = new PermissionsDetails(UiPermissionUtils.getAllPermissions(
            administrationService, true));
      // general Permissions
      RuntimePermissions runtimePermissionsDetails = (RuntimePermissions) administrationService.getGlobalPermissions();
      permissions.setGeneralPermission(runtimePermissionsDetails);

      Map<String, List<PermissionDTO>> allPermissions = new HashMap<String, List<PermissionDTO>>();
      allPermissions.putAll(buildGeneralAndModelPermissions(permissions));
      allPermissions.putAll(buildUiPermissions(permissions));

      return allPermissions;
   }

   /**
    * @param permissions
    * @return
    */
   private Map<String, List<PermissionDTO>> buildGeneralAndModelPermissions(PermissionsDetails permissions)
   {
      Map<String, List<PermissionDTO>> GeneralAndModelPermissions = new HashMap<String, List<PermissionDTO>>();

      RuntimePermissions runtimePermissions = permissions.getGeneralPermission();

      List<PermissionDTO> generalPermissions = new ArrayList<PermissionDTO>();
      GeneralAndModelPermissions.put(PermissionType.general.name(), generalPermissions);

      List<PermissionDTO> processes = new ArrayList<PermissionDTO>();
      GeneralAndModelPermissions.put(PermissionType.processDefinitions.name(), processes);

      List<PermissionDTO> activities = new ArrayList<PermissionDTO>();
      GeneralAndModelPermissions.put(PermissionType.activities.name(), activities);

      List<PermissionDTO> datas = new ArrayList<PermissionDTO>();
      GeneralAndModelPermissions.put(PermissionType.data.name(), datas);

      List<String> permissionIds = new ArrayList<String>(runtimePermissions.getAllPermissionIds());

      for (String permissionId : permissionIds)
      {
         if (UiPermissionUtils.isGeneralPermissionId(permissionId))
         {
            PermissionDTO p = new PermissionDTO(permissionId, MessagesViewsCommonBean.getInstance().getString(
                  PROPERTY_KEY_PREFIX + permissionId));
            /* p.type = PermissionType.generalPermission.name(); */
            updateGrants(p, permissions);
            generalPermissions.add(p);
         }
         else
         {
            PermissionDTO p = new PermissionDTO(permissionId, MessagesViewsCommonBean.getInstance().getString(
                  PROPERTY_KEY_PREFIX + permissionId));

            updateGrants(p, permissions);

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
   private Map<String, List<PermissionDTO>> buildUiPermissions(PermissionsDetails permissions)
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

      Map<String, List<PermissionDTO>> uiPermissions = new HashMap<String, List<PermissionDTO>>();

      Map<String, PermissionDTO> globalElements = new HashMap<String, PermissionDTO>();

      List<PermissionDTO> perspectivePermissions = new ArrayList<PermissionDTO>();
      uiPermissions.put(PermissionType.perspectives.name(), perspectivePermissions);

      List<PermissionDTO> globalExtnPermissions = new ArrayList<PermissionDTO>();
      uiPermissions.put(PermissionType.globalExtensions.name(), perspectivePermissions);

      for (IPerspectiveDefinition perspective : allPerspectives)
      {
         // add perspectives
         PermissionDTO perspectiveDTO = new PermissionDTO(UiPermissionUtils.getPermissionId(perspective.getName()),
               perspective.getLabel());
         perspectivePermissions.add(perspectiveDTO);
         updateGrants(perspectiveDTO, permissions);

         // add launch panels
         perspectiveDTO.launchPanels = new ArrayList<PermissionDTO>();
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
                  globalElements.get(launchPanel.getDefinedIn()).launchPanels = new ArrayList<PermissionDTO>();
               }

               PermissionDTO lPanelDto = new PermissionDTO(UiPermissionUtils.getPermissionId(launchPanel.getName()),
                     getUiElementLabel(launchPanel));
               updateGrants(lPanelDto, permissions);
               globalElements.get(launchPanel.getDefinedIn()).launchPanels.add(lPanelDto);
            }
            else
            {
               PermissionDTO lPanelDto = new PermissionDTO(UiPermissionUtils.getPermissionId(launchPanel.getName()),
                     getUiElementLabel(launchPanel));
               updateGrants(lPanelDto, permissions);
               perspectiveDTO.launchPanels.add(lPanelDto);
            }
         }

         // add view definitions
         perspectiveDTO.views = new ArrayList<PermissionDTO>();
         List<ViewDefinition> viewDefinitions = perspective.getViews();

         for (ViewDefinition viewDefinition : viewDefinitions)
         {
            if (viewDefinition.isGlobal())
            {
               if (!globalElements.containsKey(viewDefinition.getDefinedIn()))
               {
                  PermissionDTO gPerspectiveDTO = new PermissionDTO(UiPermissionUtils.getPermissionId(perspective
                        .getName()), perspective.getLabel());
                  globalExtnPermissions.add(gPerspectiveDTO);
                  globalElements.put(viewDefinition.getDefinedIn(), gPerspectiveDTO);
               }

               if (globalElements.get(viewDefinition.getDefinedIn()).views == null)
               {
                  globalElements.get(viewDefinition.getDefinedIn()).views = new ArrayList<PermissionDTO>();
               }

               PermissionDTO viewDto = new PermissionDTO(UiPermissionUtils.getPermissionId(viewDefinition.getName()),
                     getUiElementLabel(viewDefinition));
               updateGrants(viewDto, permissions);
               globalElements.get(viewDefinition.getDefinedIn()).views.add(viewDto);
            }
            else
            {
               // add view and its permissions
               PermissionDTO viewDto = new PermissionDTO(UiPermissionUtils.getPermissionId(viewDefinition.getName()),
                     getUiElementLabel(viewDefinition));
               updateGrants(viewDto, permissions);
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
   private void updateGrants(PermissionDTO p, PermissionsDetails permissions)
   {
      p.allow = new ArrayList<ParticipantDTO>();
      p.deny = new ArrayList<ParticipantDTO>();

      if (permissions.hasAllGrant(p.id))
      {
         p.allow.add(ParticipantDTO.ALL);
      }

      /*
       * // check if it contains default participants if
       * (permissions.isDefaultGrant(p.id)) { p.containsDefaultParticipant = true; }
       */

      String permissionId = p.id;

      // update grants
      Set<ModelParticipantInfo> grants = permissions.getGrants2(permissionId);
      p.allow.addAll(transformGrantsToDTO(grants));

      // update denied grants
      Set<ModelParticipantInfo> deny = permissions.getDeniedGrants(permissionId);
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
   private ArrayList<ParticipantDTO> transformGrantsToDTO(Set<ModelParticipantInfo> grants)
   {
      ArrayList<ParticipantDTO> pList = new ArrayList<ParticipantDTO>();
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
            label = I18nUtils.getParticipantName(model.getParticipant(info.getId()));
            participant = new ParticipantDTO(info.getQualifiedId(), label);
         }
         else
         {
            label = I18nUtils.getParticipantName(ModelCache.findModelCache().getParticipant(info.getId()));
            participant = new ParticipantDTO(info.getId(), label);
         }

         pList.add(participant);
      }

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