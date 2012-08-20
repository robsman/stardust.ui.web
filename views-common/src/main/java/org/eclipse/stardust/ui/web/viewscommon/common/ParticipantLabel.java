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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils;

/**
 * @author Yogesh.Manware
 * 
 */
public class ParticipantLabel
{
   public static final int TOTAL_PERMISSIBLE_LENGTH = 30;

   private String participantName = "<Not Found>";
   private String departmentName = null;
   private String organizationName = null;
   private String wrappedLabel = null;
   private String label = null;

   enum TYPE {
      PRT, PRT_DPT, PRT_ORG_DPT
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
      TYPE type = TYPE.PRT;

      if (null != departmentName && null == organizationName)
      {
         type = TYPE.PRT_DPT;
      }
      else if (null != departmentName && null != organizationName)
      {
         type = TYPE.PRT_ORG_DPT;
      }

      // get configured value if available
      int length = Parameters.instance().getInteger("Portal.WorklistPermissibleLength", TOTAL_PERMISSIBLE_LENGTH);

      switch (type)
      {
      case PRT:
         label = participantName;
         wrappedLabel = StringUtils.wrapString(participantName, length);
         break;

      case PRT_DPT:
         label = participantName;
         label += " - " + departmentName;

         if (label.length() > TOTAL_PERMISSIBLE_LENGTH)
         {
            length = length / 2;
            wrappedLabel = StringUtils.wrapString(participantName, length);
            wrappedLabel += " - " + StringUtils.wrapString(departmentName, length);
         }
         else
         {
            wrappedLabel = label;
         }
         break;

      case PRT_ORG_DPT:
         label = participantName;
         label += " (" + organizationName + " - " + departmentName + ")";

         if (label.length() > TOTAL_PERMISSIBLE_LENGTH)
         {
            length = length / 3;
            wrappedLabel = StringUtils.wrapString(participantName, length);
            wrappedLabel += " (" + StringUtils.wrapString(organizationName, length) + " - "
                  + StringUtils.wrapString(departmentName, length) + ")";
         }
         else
         {
            wrappedLabel = label;
         }
         break;

      default:
         break;
      }
   }

   public void setParticipantName(String participantName)
   {
      this.participantName = participantName;
   }

   public void setDepartmentName(String departmentName)
   {
      this.departmentName = departmentName;
   }

   public void setOrganizationName(String organizationName)
   {
      this.organizationName = organizationName;
   }

   public String getParticipantName()
   {
      return participantName;
   }

   public String getDepartmentName()
   {
      return departmentName;
   }

   public String getOrganizationName()
   {
      return organizationName;
   }
}