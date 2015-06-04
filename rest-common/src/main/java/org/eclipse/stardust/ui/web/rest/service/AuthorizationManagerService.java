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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.dto.RuntimePermissionsDetails;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
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
      GeneralPermissions, UIPermissions, Perspective, LaunchPanels, LaunchPanel, Views, View, GlobalExtensions, GlobalExtension
   }

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   /**
    * 
    * @return
    */
   public List<PermissionDTO> fetchPermissions()
   {
      // fetch permission
      AdministrationService administrationService = serviceFactoryUtils.getAdministrationService();

      // UI permissions
      PermissionsDetails permissions = new PermissionsDetails(UiPermissionUtils.getAllPermissions(
            administrationService, true));
      // general Permissions
      RuntimePermissionsDetails runtimePermissionsDetails = (RuntimePermissionsDetails) administrationService
            .getGlobalPermissions();
      permissions.setGeneralPermission(runtimePermissionsDetails);

      List<PermissionDTO> allPermissions = new ArrayList<PermissionDTO>();
      allPermissions.add(buildGeneralPermissions(permissions));
      allPermissions.add(buildUiPermissions(permissions));

      return allPermissions;
   }

   /**
    * @param permissions
    * @return
    */
   private PermissionDTO buildGeneralPermissions(PermissionsDetails permissions)
   {
      RuntimePermissionsDetails runtimePermissions = permissions.getGeneralPermission();

      PermissionDTO pdto = new PermissionDTO();
      pdto.label = MessagesViewsCommonBean.getInstance().get("views.authorizationManagerView.generalPermissions");
      pdto.type = PermissionType.GeneralPermissions.name();

      pdto.permissions = new ArrayList<PermissionDTO>();

      List<String> permissionIds = new ArrayList<String>(runtimePermissions.getAllPermissionIds());

      for (String permissionId : permissionIds)
      {
         PermissionDTO p = new PermissionDTO(MessagesViewsCommonBean.getInstance().getString(
               PROPERTY_KEY_PREFIX + permissionId), permissionId);
         updateGrants(p, permissions, false);
         pdto.permissions.add(p);
      }

      return pdto;
   }

   /**
    * 
    * @param permissions
    * @return
    */
   private PermissionDTO buildUiPermissions(PermissionsDetails permissions)
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

      // global elements
      Map<String, Map<String, Set<UiElement>>> globalElements = new HashMap<String, Map<String, Set<UiElement>>>();

      PermissionDTO pdto = new PermissionDTO();
      pdto.label = MessagesViewsCommonBean.getInstance().get("views.authorizationManagerView.uiPermissions");
      pdto.type = PermissionType.UIPermissions.name();

      pdto.permissions = new ArrayList<PermissionDTO>();

      for (IPerspectiveDefinition perspective : allPerspectives)
      {
         // add perspectives
         PermissionDTO perspectiveDTO = new PermissionDTO(perspective.getLabel(),
               UiPermissionUtils.getPermissionId(perspective.getName()));
         pdto.permissions.add(perspectiveDTO);
         perspectiveDTO.type = PermissionType.Perspective.name();

         updateGrants(perspectiveDTO, permissions, true);
         perspectiveDTO.permissions = new ArrayList<PermissionDTO>();

         // add launch panels
         PermissionDTO launchPanelsDTO = new PermissionDTO();
         perspectiveDTO.permissions.add(launchPanelsDTO);
         launchPanelsDTO.label = MessagesViewsCommonBean.getInstance().get(
               "views.authorizationManagerView.launchPanels");
         launchPanelsDTO.type = PermissionType.LaunchPanels.name();
         launchPanelsDTO.permissions = new ArrayList<PermissionDTO>();
         List<LaunchPanel> launchPanels = perspective.getLaunchPanels();

         for (LaunchPanel launchPanel : launchPanels)
         {
            if (launchPanel.isGlobal())
            {
               addGlobalElement(globalElements, launchPanel, UiPermissionUtils.LAUNCH_PANEL);
            }
            else
            {
               PermissionDTO lPanelDto = new PermissionDTO(getUiElementLabel(launchPanel),
                     UiPermissionUtils.getPermissionId(launchPanel.getName()));
               lPanelDto.type = PermissionType.LaunchPanel.name();
               updateGrants(lPanelDto, permissions, true);
               launchPanelsDTO.permissions.add(lPanelDto);
            }
         }

         // add view definitions
         PermissionDTO viewsDTO = new PermissionDTO();
         perspectiveDTO.permissions.add(viewsDTO);

         viewsDTO.label = MessagesViewsCommonBean.getInstance().get("views.authorizationManagerView.views");
         viewsDTO.type = PermissionType.Views.name();
         viewsDTO.permissions = new ArrayList<PermissionDTO>();

         List<ViewDefinition> viewDefinitions = perspective.getViews();

         for (ViewDefinition viewDefinition : viewDefinitions)
         {
            if (viewDefinition.isGlobal())
            {
               addGlobalElement(globalElements, viewDefinition, UiPermissionUtils.VIEW);
            }
            else
            {
               // add view and its permissions
               PermissionDTO viewDto = new PermissionDTO(getUiElementLabel(viewDefinition),
                     UiPermissionUtils.getPermissionId(viewDefinition.getName()));
               updateGrants(viewDto, permissions, true);
               viewDto.type = PermissionType.View.name();
               viewsDTO.permissions.add(viewDto);
            }
         }
      }

      // Global permissions
      PermissionDTO globalExtnPermissions = new PermissionDTO();
      pdto.permissions.add(globalExtnPermissions);

      globalExtnPermissions.label = MessagesViewsCommonBean.getInstance().get(
            "views.authorizationManagerView.globalExtensions");
      globalExtnPermissions.type = PermissionType.GlobalExtensions.name();
      globalExtnPermissions.permissions = new ArrayList<PermissionDTO>();

      for (Entry<String, Map<String, Set<UiElement>>> entry : globalElements.entrySet())
      {
         // add extension
         PermissionDTO extnDto = new PermissionDTO();
         globalExtnPermissions.permissions.add(extnDto);

         extnDto.label = UiPermissionUtils.getPermisionLabel(entry.getKey());
         extnDto.type = PermissionType.GlobalExtension.name();
         extnDto.permissions = new ArrayList<PermissionDTO>();

         Map<String, Set<UiElement>> elementPermissions = entry.getValue();

         // add launch panels and views if available
         for (Entry<String, Set<UiElement>> elementsEntry : elementPermissions.entrySet())
         {
            // add Launch Panel / View
            PermissionDTO extnElementsDto = new PermissionDTO();
            String type = "";
            if (elementsEntry.getKey().equals(UiPermissionUtils.LAUNCH_PANEL))
            {
               extnElementsDto.type = PermissionType.LaunchPanels.name();
               type = PermissionType.LaunchPanel.name();
               extnElementsDto.label = MessagesViewsCommonBean.getInstance().get(
                     "views.authorizationManagerView.launchPanels");
            }
            else
            {
               extnElementsDto.type = PermissionType.Views.name();
               type = PermissionType.View.name();
               extnElementsDto.label = MessagesViewsCommonBean.getInstance()
                     .get("views.authorizationManagerView.views");
            }

            extnDto.permissions.add(extnElementsDto);

            // add views
            extnElementsDto.permissions = new ArrayList<PermissionDTO>();
            Set<UiElement> elements = elementsEntry.getValue();
            for (UiElement uiElement : elements)
            {
               PermissionDTO viewsDto = new PermissionDTO(getUiElementLabel(uiElement),
                     UiPermissionUtils.getPermissionId(uiElement.getName()));
               viewsDto.type = type;
               updateGrants(viewsDto, permissions, true);
               extnElementsDto.permissions.add(viewsDto);
            }
         }
      }

      return pdto;
   }

   /**
    * 
    * @param p
    * @param permissions
    */
   private void updateGrants(PermissionDTO p, PermissionsDetails permissions, boolean uiPermissions)
   {
      p.allow = new ArrayList<ParticipantDTO>();
      p.deny = new ArrayList<ParticipantDTO>();

      if (permissions.hasAllGrant(p.id))
      {
         p.allow.add(ParticipantDTO.ALL);
      }

      // check if it contains default participants
      if (permissions.isDefaultGrant(p.id))
      {
         p.containsDefaultParticipant = true;
      }

      String permissionId = p.id;

      if (uiPermissions)
      {
         permissionId = p.id + UiPermissionUtils.POSTFIX_ALLOW;
      }

      Set<ModelParticipantInfo> grants = permissions.getGrants(permissionId);
      p.allow.addAll(transformGrantsToDTO(grants));

      if (uiPermissions)
      {
         permissionId = p.id + UiPermissionUtils.POSTFIX_DENY;
      }
      else
      {
         permissionId = UiPermissionUtils.PREFIX_DENY + p.id;
      }
      Set<ModelParticipantInfo> deny = permissions.getGrants(permissionId);
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
    * @param uiElementDefs
    * @param uiElement
    * @param elementType
    */
   private void addGlobalElement(Map<String, Map<String, Set<UiElement>>> uiElementDefs, UiElement uiElement,
         String elementType)
   {
      // global launch panels and views
      String extension = uiElement.getDefinedIn();

      // add extension
      if (!uiElementDefs.containsKey(extension))
      {
         uiElementDefs.put(extension, new HashMap<String, Set<UiElement>>());
      }

      Map<String, Set<UiElement>> extensionMap = uiElementDefs.get(extension);

      // add Launch panel or views map
      if (!extensionMap.containsKey(elementType))
      {
         extensionMap.put(elementType, new HashSet<UiElement>());
      }

      // add actual view definition or launch panel definition
      extensionMap.get(elementType).add(uiElement);
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