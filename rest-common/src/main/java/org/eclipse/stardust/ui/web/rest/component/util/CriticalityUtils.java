package org.eclipse.stardust.ui.web.rest.component.util;

import java.util.List;

import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Johnson.Quadras
 *
 */
@Component
public class CriticalityUtils
{

   /**
    * Returns the List of criticality categories
    * 
    * @return List
    */
   public static List<CriticalityCategory> getCriticalityConfiguration()
   {
      List<CriticalityCategory> criticalityConfiguration = CriticalityConfigurationUtil.getCriticalityCategoriesList();
      criticalityConfiguration.add(CriticalityConfigurationUtil.getUndefinedCriticalityCategory());
      return criticalityConfiguration;
   }

   /**
    * Returns the Icon color for a criticality value.
    * 
    * @return String
    */
   public static String getCriticalityIconColor(int value, List<CriticalityCategory> criticalityConfiguration)
   {
      CriticalityCategory criticality = getCriticalityCategory(value, criticalityConfiguration);
      return criticality.getIconColor().toString();
   }

   /**
    * Returns Critcality Configuration for a cricality value.
    * 
    * @param criticality
    * @param criticalityConfiguration
    * @return CriticalityCategory
    */
   public static CriticalityCategory getCriticalityCategory(int criticality,
         List<CriticalityCategory> criticalityConfiguration)
   {

      for (CriticalityCategory cCat : criticalityConfiguration)
      {
         if (cCat.getRangeFrom() <= criticality && cCat.getRangeTo() >= criticality)
         {
            return cCat;
         }
      }

      return null;
   }

   /**
    * Returns the portal criticality value for the corresponding engine crircality
    * 
    * @param engineCriticality
    * @return
    */
   public static int getPortalCriticalityValue(double engineCriticality)
   {
      return CriticalityConfigurationUtil.getPortalCriticality(engineCriticality);
   }

}
