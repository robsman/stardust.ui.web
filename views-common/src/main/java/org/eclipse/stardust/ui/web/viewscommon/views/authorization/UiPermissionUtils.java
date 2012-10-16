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
package org.eclipse.stardust.ui.web.viewscommon.views.authorization;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.ParticipantInfoUtil;
import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.LaunchPanel;
import org.eclipse.stardust.ui.web.common.PerspectiveExtension;
import org.eclipse.stardust.ui.web.common.UiElement;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;

/**
 * Client side utility class which can be used to store UI permissions as preferences at
 * PARTITION scope.
 * 
 */
/**
 * @author Yogesh.Manware
 * 
 */
public class UiPermissionUtils
{
   public static final String LAUNCH_PANEL = "launchPanel";
   public static final String ICON_GENERAL_PERM = "/plugins/views-common/images/icons/server_key.png";
   public static final String ICON_UI_PERM = "/plugins/views-common/images/icons/computer_key.png";
   public static final String ICON_GLOBAL_EXT = "/plugins/views-common/images/icons/puzzle.png";
   public static final String ICON_LAUNCH_PANEL = "/plugins/views-common/images/icons/application_key.png";
   public static final String ICON_VIEW = "/plugins/views-common/images/icons/page_white_key.png";
   public static final String VIEW = "view";
   public static final String GLOBAL_EXTNS = "globalExtensions";
   public static final String PERSPECTIVE = "perspective";
   
   public static final String PROPERTY_C_KEY = "views.authorizationManagerView.";

   /**
    * The moduleId of preferences which are used to store permissions.
    */
   private static final String PERMISSIONS = "permissions";

   /**
    * The preferencesId of preferences which are scoped as global permissions.
    */
   private static final String UI = "ui";

   private final static Map<String, String> defaultPermissions;

   /**
    * Constants for Administrator role as used by engine permissions. Can be changed to a
    * portal related constant.
    */
   private final static String ADMINISTRATOR = PredefinedConstants.ADMINISTRATOR_ROLE;
   private static final String PREFIX = "portal.ui.";
   private static final String POSTFIX_ALLOW = ".allow";
   private static final String POSTFIX_DENY = ".deny";
   private static final String PERIOD = ".";
   private static final String SPACE = " ";

   static
   {
      defaultPermissions = new HashMap<String, String>();
      // Set Administrator default
      defaultPermissions.put("portal.ui.ippAdminPerspective.allow", ADMINISTRATOR);
   }

   /**
    * @param permissionId
    * @param grants
    * @return
    */
   public static boolean isDefaultPermission(String permissionId, List<String> grants)
   {
      if (CollectionUtils.isNotEmpty(grants))
      {
         String permission = defaultPermissions.get(permissionId);
         if (permission != null && grants.size() == 1 && grants.get(0).equals(permission))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Gets a map of all UI permissions and its grants.
    * 
    * @param administrationService
    *           Needed to access the preferences API.
    * @param includeDefaultPermissions
    *           Default permissions can be included or excluded from the map.
    * @return all UI permissions and their set grants.
    */
   public static Map<String, List<String>> getAllPermissions(AdministrationService administrationService,
         boolean includeDefaultPermissions)
   {
      final Map<String, Serializable> permissions = getPreferences(administrationService);

      Map<String, List<String>> filteredPermissions = filterPermissions(permissions, includeDefaultPermissions);

      return filteredPermissions;
   }

   /**
    * @param administrationService
    * @param preferencesMap
    */
   public static void savePreferences(AdministrationService administrationService,
         Map<String, Serializable> preferencesMap)
   {
      String preferenceId = UI;

      Preferences preferences = new Preferences(PreferenceScope.PARTITION, PERMISSIONS, preferenceId, preferencesMap);

      administrationService.savePreferences(preferences);
   }

   /**
    * @author Yogesh.Manware
    * @param permissionId
    * @return
    */
   public static String getPermissionIdAllow(String permissionId)
   {
      return PREFIX + permissionId + POSTFIX_ALLOW;
   }

   /**
    * @param permissionId
    * @return
    */
   public static String getPermissionIdDeny(String permissionId)
   {
      return PREFIX + permissionId + POSTFIX_DENY;
   }

   /**
    * @param permissionId
    * @return
    */
   public static String getPortalPermissionId(String permissionId)
   {
      if (permissionId.contains(PREFIX))
      {
         String s2 = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringAfterLast(permissionId, PREFIX);
         return org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringBeforeLast(s2, ".");
      }
      return permissionId;
   }

   /**
    * @param permissionId
    * @return
    */
   public static String getPermisionLabel(String permissionId)
   {
      String portalPermissionId = getPortalPermissionId(permissionId);
      Map<String, IPerspectiveDefinition> perspectives = PortalUiController.getInstance().getPerspectives();

      for (Entry<String, IPerspectiveDefinition> pEntry : perspectives.entrySet())
      {
         IPerspectiveDefinition perspective = pEntry.getValue();

         if (portalPermissionId.equals(pEntry.getKey()))
         {
            return perspective.getLabel();
         }

         List<LaunchPanel> launchPanels = perspective.getLaunchPanels();
         for (LaunchPanel launchPanel : launchPanels)
         {
            if (portalPermissionId.equals(launchPanel.getName()))
            {
               return getUiElementLabel(launchPanel);
            }
         }

         List<ViewDefinition> viewDefinitions = perspective.getViews();
         for (ViewDefinition viewDefinition : viewDefinitions)
         {
            if (portalPermissionId.equals(viewDefinition.getName()))
            {
               return getUiElementLabel(viewDefinition);
            }
         }

         Map<String, PerspectiveExtension> extensions = perspective.getExtensions();
         for (Entry<String, PerspectiveExtension> extEntry : extensions.entrySet())
         {
            if (portalPermissionId.equals(extEntry.getKey()))
            {
               return extEntry.getValue().getLabel();
            }
         }
      }

      return portalPermissionId;
   }

   /**
    * @param uielement
    * @return
    */
   public static String getUiElementLabel(UiElement uielement)
   {
      String title;

      if (uielement.hasMessage(UiElement.PRE_LABEL_TITLE, null))
      {
         title = uielement.getMessage(UiElement.PRE_LABEL_TITLE, null);
      }
      else if (uielement.hasMessage(UiElement.PRE_TITLE, null))
      {
         title = uielement.getMessage(UiElement.PRE_TITLE, null);
      }
      else
      {
         title = uielement.getMessage(UiElement.PRE_LABEL, null);
      }

      return title;
   }

   /**
    * @param permissionId
    * @return
    */
   public static boolean isGeneralPermissionId(String permissionId)
   {
      if (!permissionId.startsWith(PREFIX))
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * @param grants
    * @return
    */
   public static Set<ModelParticipantInfo> externalize(List<String> grants)
   {
      HashSet<ModelParticipantInfo> externalGrants = new HashSet<ModelParticipantInfo>();
      if (grants != null && !grants.contains(Authorization2.ALL))
      {
         for (String grant : grants)
         {
            QName qualifier = QName.valueOf(grant);
            externalGrants.add(ParticipantInfoUtil.newModelParticipantInfo(qualifier.getNamespaceURI(),
                  qualifier.getLocalPart()));
         }
      }
      return externalGrants;
   }

   /**
    * @param grants
    * @return
    */
   public static List<String> internalize(Set<ModelParticipantInfo> grants)
   {
      if (grants != null)
      {
         List<String> grantIds = new LinkedList<String>();

         for (ModelParticipantInfo modelParticipantInfo : grants)
         {
            if (modelParticipantInfo.getDepartment() != null)
            {
               throw new IllegalArgumentException(Department.class.getName());
            }
            if (modelParticipantInfo instanceof QualifiedModelParticipantInfo)
            {
               grantIds.add(((QualifiedModelParticipantInfo) modelParticipantInfo).getQualifiedId());
            }
            else
            {
               grantIds.add(modelParticipantInfo.getId());
            }
         }
         return grantIds;
      }
      return null;
   }

   /**
    * @return ui permissions
    */
   public static Map<String, UiPermission> getUiPermssions()
   {
      Map<String, UiPermission> uiPermissions = new HashMap<String, UiPermission>();

      Map<String, IPerspectiveDefinition> perspectives = PortalUiController.getInstance().getPerspectives();

      // global elements
      Map<String, Map<String, Set<UiElement>>> globalElements = new HashMap<String, Map<String, Set<UiElement>>>();

      for (Entry<String, IPerspectiveDefinition> perspEntry : perspectives.entrySet())
      {
         IPerspectiveDefinition perspective = perspEntry.getValue();

         // add perspective
         UiPermission persp = new UiPermission(perspective.getName(), perspective.getLabel() + SPACE
               + getMessage(PERSPECTIVE), PermissionUserObject.ICON_PERMISSION, null);
         uiPermissions.put(persp.getPermissionId(), persp);

         // add launch panels
         List<LaunchPanel> launchPanels = perspective.getLaunchPanels();

         for (LaunchPanel launchPanel : launchPanels)
         {
            if (launchPanel.isGlobal())
            {
               addGlobalElement(globalElements, launchPanel, LAUNCH_PANEL);
            }
            else
            {
               UiPermission launchPanelP = new UiPermission(launchPanel.getName(), perspective.getLabel() + PERIOD
                     + getUiElementLabel(launchPanel) + SPACE + getMessage(LAUNCH_PANEL), ICON_LAUNCH_PANEL, persp);

               uiPermissions.put(launchPanelP.getPermissionId(), launchPanelP);
            }
         }

         // add view definitions
         List<ViewDefinition> viewDefinitions = perspective.getViews();
         for (ViewDefinition viewDefinition : viewDefinitions)
         {
            if (viewDefinition.isGlobal())
            {
               addGlobalElement(globalElements, viewDefinition, VIEW);
            }
            else
            {
               // add view
               UiPermission viewP = new UiPermission(viewDefinition.getName(), perspective.getLabel() + PERIOD
                     + getUiElementLabel(viewDefinition) + SPACE + getMessage(VIEW), ICON_VIEW, persp);

               uiPermissions.put(viewP.getPermissionId(), viewP);
            }
         }
      }

      // add global views
      String globalExtLabel = getMessage(GLOBAL_EXTNS);

      for (Entry<String, Map<String, Set<UiElement>>> entry : globalElements.entrySet())
      {

         Map<String, Set<UiElement>> elementPermissions = entry.getValue();

         // add launch panels and views if available
         for (Entry<String, Set<UiElement>> elementsEntry : elementPermissions.entrySet())
         {
            // add Launch Panel / View
            String label = getMessage(elementsEntry.getKey());

            // add views
            Set<UiElement> elements = elementsEntry.getValue();
            for (UiElement uiElement : elements)
            {
               UiPermission globalP = new UiPermission(uiElement.getName(), globalExtLabel + PERIOD
                     + UiPermissionUtils.getPermisionLabel(entry.getKey()) + PERIOD + getUiElementLabel(uiElement)
                     + SPACE + label, ICON_GLOBAL_EXT, null);

               uiPermissions.put(globalP.getPermissionId(), globalP);
            }
         }
      }
      return uiPermissions;
   }

   /**
    * @param key
    * @return
    */
   private static String getMessage(String key)
   {
      return MessagesViewsCommonBean.getInstance().getString(PROPERTY_C_KEY + key);
   }

   /**
    * @param uiElementDefs
    * @param uiElement
    * @param elementType
    */
   private static void addGlobalElement(Map<String, Map<String, Set<UiElement>>> uiElementDefs, UiElement uiElement,
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
    * @param permissions
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   private static void addDefaultPermissions(Map<String, List<String>> permissions)
   {
      for (Entry<String, String> entry : defaultPermissions.entrySet())
      {
         List value = permissions.get(entry.getKey());
         if (value == null)
         {
            List values = new LinkedList<String>();
            values.add(entry.getValue());
            permissions.put(entry.getKey(), values);
         }
      }
   }

   /**
    * @param administrationService
    * @return
    */
   private static Map<String, Serializable> getPreferences(AdministrationService administrationService)
   {
      String preferenceId = UI;

      Preferences preferences = administrationService.getPreferences(PreferenceScope.PARTITION, PERMISSIONS,
            preferenceId);

      return preferences != null ? preferences.getPreferences() : null;
   }

   /**
    * @param preferencesMap
    * @param includeDefaultPermissions
    * @return
    */
   @SuppressWarnings("unchecked")
   private static Map<String, List<String>> filterPermissions(Map<String, Serializable> preferencesMap,
         boolean includeDefaultPermissions)
   {
      Map<String, List<String>> permissions = CollectionUtils.newHashMap();

      for (java.util.Map.Entry<String, Serializable> entry : preferencesMap.entrySet())
      {
         if (entry.getValue() != null && entry.getValue() instanceof List)
         {
            permissions.put(entry.getKey(), (List<String>) entry.getValue());
         }
      }

      if (includeDefaultPermissions)
      {
         addDefaultPermissions(permissions);
      }
      return permissions;
   }

}
