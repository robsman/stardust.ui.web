package org.eclipse.stardust.ui.web.viewscommon.common;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.stardust.common.CollectionUtils;

public class PortalResourceBundle
{

   private ConcurrentMap<String, String> failedLookups = CollectionUtils.newConcurrentHashMap();

   private ResourceBundle bundle;

   public PortalResourceBundle(ResourceBundle bundle)
   {
      this.bundle = bundle;
   }

   public String getString(String key)
   {
      if (failedLookups.containsKey(key))
      {
         return null;
      }

      String string = null;
      String failureMsg = null;

      try
      {
         string = bundle.getString(key);
      }
      catch (MissingResourceException e)
      {
         failureMsg = "cannot find '" + key + "' in ResourceBundle";
      }
      catch (Exception e)
      {
         failureMsg = "error getting value of '" + key + "' in resource bundle '";
      }

      if (failureMsg != null)
      {
         failedLookups.putIfAbsent(key, failureMsg);
      }

      return string;
   }

   public String getErrorMsg(String key)
   {
      // Get errorMsg from miss cache
      return failedLookups.get(key);
   }

}
