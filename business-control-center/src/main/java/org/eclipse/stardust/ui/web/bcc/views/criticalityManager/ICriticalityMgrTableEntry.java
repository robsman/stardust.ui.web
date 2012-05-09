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
package org.eclipse.stardust.ui.web.bcc.views.criticalityManager;

import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.ui.web.common.util.StringUtils;

/**
 * Represents one row of the criticality manager tree table. Separate implementation for
 * each type of row would needed (Acticvity Def., Process Def., Model).
 * 
 * @author Shrikant.Gangal
 * 
 */
public interface ICriticalityMgrTableEntry
{
   Map<String, CriticalityDetails> getCriticalityDetailsMap();

   void initialize();

   String getDefaultPerformerName();

   List getChildren();

   String getType();

   String getName();

   void doCriticalityAction(ActionEvent event);

   /**
    * @author Shrikant.Gangal
    * 
    */
   public static class CriticalityDetails
   {
      long count;

      String criticalityLabel;

      ICriticalityMgrTableEntry rowObject;

      public CriticalityDetails(ICriticalityMgrTableEntry rowObject)
      {
         this.rowObject = rowObject;
      }

      public long getCount()
      {
         return count;
      }

      public void setCount(long count)
      {
         this.count = count;
      }

      public String getCriticalityLabel()
      {
         return criticalityLabel;
      }

      public void setCriticalityLabel(String criticalityLabel)
      {
         this.criticalityLabel = criticalityLabel;
      }

      /**
       * Stripes the whitespaces from label.
       * Used for automation tags where we cannot have spaces.
       * 
       * @return
       */
      public String getCriticalityLabelStripped()
      {
         if (StringUtils.isNotEmpty(criticalityLabel))
         {
            return StringUtils.replace(criticalityLabel, " ", "");
         }

         return "";
      }

      public void doCriticalityAction(ActionEvent event)
      {
         rowObject.doCriticalityAction(event);
      }
   }
}
