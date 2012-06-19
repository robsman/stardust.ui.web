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
package org.eclipse.stardust.ui.web.viewscommon.common.criticality;

import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;

/**
 * @author Shrikant.Gangal
 *
 * A session scoped bean that holds the criticality configuration.
 * 
 */
public class CriticalityConfigurationHelper
{
   private static final String BEAN_NAME = "criticalityConfigurationHelper";
   private List<CriticalityCategory> criticalityConfiguration;
   
   public CriticalityConfigurationHelper()
   {
      initialize();
   }
   
   /**
    * @return
    */
   public static CriticalityConfigurationHelper getInstance()
   {
      return (CriticalityConfigurationHelper) FacesContext.getCurrentInstance().getApplication()
            .getVariableResolver().resolveVariable(FacesContext.getCurrentInstance(),
                  BEAN_NAME);
   }
   
   public List<CriticalityCategory> getCriticalityConfiguration()
   {
      Collections.sort(criticalityConfiguration);
      return criticalityConfiguration;
   }
   
   public void initialize()
   {
      criticalityConfiguration = CriticalityConfigurationUtil.getCriticalityCategoriesList();
      criticalityConfiguration.add(CriticalityConfigurationUtil.getUndefinedCriticalityCategory());
   }
   
   public CriticalityCategory getCriticality(int criticality)
   {
      for (CriticalityCategory cCat : criticalityConfiguration) {
         if (cCat.getRangeFrom() <= criticality && cCat.getRangeTo() >= criticality)
         {
            return cCat;
         }
      }
      
      return null;
   }
}
