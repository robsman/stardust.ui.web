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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils;

/**
 * @author Yogesh.Manware
 * 
 */
public class ParticipantLabel
{
   public static final int TOTAL_PERMISSIBLE_LENGTH = 30;
   private static final String DEFAULT_DEPARTMENT_IND = "?";
   private static final String SEPARATOR = ".";
   private static final String POSTFIX_OPEN = " (";
   private static final String POSTFIX_CLOSE = ")";

   private List<String> hierarchyDetails = new ArrayList<String>();
   private String participantName = null;
   private String wrappedLabel = null;
   private String label = null;

   /**
    * @return
    */
   public String getLabel()
   {
      if (null == label)
      {
         initialize();
      }

      return label;
   }

   /**
    * @return
    */
   public String getWrappedLabel()
   {
      if (null == wrappedLabel)
      {
         initialize();
      }
      return wrappedLabel;
   }

   /**
    * initialize
    */
   private void initialize()
   {
      label = participantName;

      if (CollectionUtils.isNotEmpty(hierarchyDetails))
      {
         label += POSTFIX_OPEN;
         for (String dept : hierarchyDetails)
         {
            label += dept + SEPARATOR;
         }
         label = label.substring(0, label.length() - 1);

         label += POSTFIX_CLOSE;
      }

      wrappedLabel = StringUtils.wrapString(label, TOTAL_PERMISSIBLE_LENGTH);
   }

   public void addDepartment(String department)
   {
      if (null != department)
      {
         this.hierarchyDetails.add(department);
      }
   }

   public void addOrganization(String organization, boolean scoped)
   {
      if (null != organization)
      {
         if (scoped || CollectionUtils.isNotEmpty(hierarchyDetails))
         {
            this.hierarchyDetails.add(organization + (scoped ? DEFAULT_DEPARTMENT_IND : ""));
         }
      }
   }

   public void setParticipantName(String participantName)
   {
      this.participantName = participantName;
   }
}