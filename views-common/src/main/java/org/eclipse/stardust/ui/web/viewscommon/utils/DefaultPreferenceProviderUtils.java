package org.eclipse.stardust.ui.web.viewscommon.utils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

/**
 * 
 * @author Sidharth.Singh
 * 
 */
public class DefaultPreferenceProviderUtils
{
   public static final Logger trace = LogManager.getLogger(DefaultPreferenceProviderUtils.class);

   /**
    * 
    * @return
    */
   public static String getDefaultSkinPreference()
   {
      try
      {
         String defaultSkin = (String) FacesUtils.getBeanFromContext("sungardSkinDefaultPreference");
         if (trace.isDebugEnabled())
         {
            trace.debug("Default Skin preference read from spring configuration is " + defaultSkin);
         }
         return defaultSkin;
      }
      catch (Exception e)
      {
         trace.debug("Unable to read spring configuration for default skin " + e.getLocalizedMessage());
      }
      return null;
   }
}
