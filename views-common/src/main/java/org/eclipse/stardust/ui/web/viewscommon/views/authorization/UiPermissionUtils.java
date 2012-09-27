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
import java.util.Set;
import java.util.Map.Entry;

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
   /**
    * The moduleId of preferences which are used to store permissions.
    */
   private static final String PERMISSIONS = "permissions";

   /**
    * The preferencesId of preferences which are scoped as global permissions.
    */
   private static final String UI = "ui";

   /**
    * Static default permissions <br>
    * 
    * TODO The absence of the preference can be interpreted as allow=all, only preferences
    * not obeying the default should be defined.
    */
   private final static Map<String, String> defaultPermissions;

   /**
    * Constants for Administrator role as used by engine permissions. Can be changed to a
    * portal related constant.
    */
   private final static String ADMINISTRATOR = PredefinedConstants.ADMINISTRATOR_ROLE;

   /**
    * Constant for ALL participants as used by engine permissions. Can be changed to a
    * portal related constant.
    */
   private final static String ALL = Authorization2.ALL;

   private static final String PREFIX = "portal.ui.";
   private static final String POSTFIX_ALLOW = ".allow";
   private static final String POSTFIX_DENY = ".deny";

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
      if (grants != null && grants.size() > 0)
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
    * @param permissions
    */
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
