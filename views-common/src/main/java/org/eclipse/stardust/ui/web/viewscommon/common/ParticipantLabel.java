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
   private static final String POSTFIX_OPEN = "(";
   private static final String POSTFIX_CLOSE = ")";

   public TYPE type = TYPE.PARTICIPANT;
   private List<String> departments = new ArrayList<String>();
   private String organizationName = null;
   private String participantName = null;
   private String roleName = null;
   private String wrappedLabel = null;
   private String label = null;

   enum TYPE {
      ROLE, ORGANIZATION, PARTICIPANT
   }

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
      if (null == type)
      {
         type = TYPE.PARTICIPANT;
      }

      switch (type)
      {
      case ROLE:
         label = roleName;
         if (CollectionUtils.isEmpty(departments)) // default department
         {
            label += POSTFIX_OPEN + organizationName + DEFAULT_DEPARTMENT_IND + POSTFIX_CLOSE;
            if (label.length() > TOTAL_PERMISSIBLE_LENGTH)
            {
               wrappedLabel = StringUtils.wrapString(label, TOTAL_PERMISSIBLE_LENGTH);
            }
            else
            {
               wrappedLabel = label;
            }
         }
         else
         {
            label += POSTFIX_OPEN;

            for (String dept : departments)
            {
               label += dept + SEPARATOR;
            }
            label = label.substring(0, label.length() - 1);
            label += POSTFIX_CLOSE;

            if (label.length() > TOTAL_PERMISSIBLE_LENGTH)
            {
               wrappedLabel = StringUtils.wrapString(label, TOTAL_PERMISSIBLE_LENGTH);
            }
            else
            {
               wrappedLabel = label;
            }
         }
         break;

      case ORGANIZATION:
         label = organizationName;
         if (CollectionUtils.isEmpty(departments)) // default department
         {
            label += POSTFIX_OPEN + DEFAULT_DEPARTMENT_IND + POSTFIX_CLOSE;
            if (label.length() > TOTAL_PERMISSIBLE_LENGTH)
            {
               wrappedLabel = StringUtils.wrapString(label, TOTAL_PERMISSIBLE_LENGTH);
            }
            else
            {
               wrappedLabel = label;
            }
         }
         else
         {
            label += POSTFIX_OPEN;
            for (String dept : departments)
            {
               label += dept + SEPARATOR;
            }
            label = label.substring(0, label.length() - 1);
            label += POSTFIX_CLOSE;

            if (label.length() > TOTAL_PERMISSIBLE_LENGTH)
            {
               wrappedLabel = StringUtils.wrapString(label, TOTAL_PERMISSIBLE_LENGTH);
            }
            else
            {
               wrappedLabel = label;
            }
         }
         break;

      case PARTICIPANT:
         label = participantName;
         wrappedLabel = StringUtils.wrapString(label, TOTAL_PERMISSIBLE_LENGTH);
         break;

      default:
         break;
      }
   }

   public void addDepartment(String department)
   {
      if (null != department)
      {
         this.departments.add(department);
      }
   }

   public void setOrganizationName(String organizationName)
   {
      this.organizationName = organizationName;
   }

   public void setRoleName(String roleName)
   {
      this.roleName = roleName;
   }

   public void setType(TYPE type)
   {
      this.type = type;
   }

   public void setParticipantName(String participantName)
   {
      this.participantName = participantName;
   }
}