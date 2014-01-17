package org.eclipse.stardust.ui.web.modeler.ui;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.ui.web.modeler.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@Service
@Scope("singleton")
public class ModelerConfigurationService
{
   private static final Logger trace = LogManager
         .getLogger(ModelerConfigurationService.class);

   @Resource
   ModelService modelService;

   /**
    *
    * @return
    */
   public JsonObject getPreferences()
   {
      String defaultProfile = null;
      String showTechnologyPreview = null;

      try
      {
         Map<String, Serializable> props = modelService
               .getServiceFactory()
               .getAdministrationService()
               .getPreferences(PreferenceScope.USER, UserPreferencesEntries.M_MODULE,
                     UserPreferencesEntries.REFERENCE_ID).getPreferences();

         // Default Profile
         String defaultProfileKey = UserPreferencesEntries.M_MODULE + "."
               + UserPreferencesEntries.V_MODELER + "."
               + UserPreferencesEntries.F_DEFAULT_PROFILE;
         defaultProfile = (String) props.get(defaultProfileKey);

         // Show Technology Preview
         String showTechnologyPreviewKey = UserPreferencesEntries.M_MODULE + "."
               + UserPreferencesEntries.V_MODELER + "."
               + UserPreferencesEntries.F_TECH_PREVIEW;
         showTechnologyPreview = (String) props.get(showTechnologyPreviewKey);
      }
      catch (Exception e)
      {
         trace.error("Error occurred while fetching preferences", e);
      }

      if (isEmpty(defaultProfile))
      {
         defaultProfile = UserPreferencesEntries.PROFILE_BA;
      }

      if (isEmpty(showTechnologyPreview))
      {
         showTechnologyPreview = "false";
      }

      JsonObject preferencesJson = new JsonObject();
      preferencesJson.addProperty("defaultProfile", defaultProfile);
      preferencesJson.addProperty("showTechnologyPreview",
            Boolean.parseBoolean(showTechnologyPreview));

      return preferencesJson;
   }
}
