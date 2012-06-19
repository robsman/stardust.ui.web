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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import org.eclipse.stardust.engine.api.model.DynamicParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.Department;


/**
 * Encapsulates a {@link QualifiedModelParticipantInfo}, {@link DynamicParticipantInfo} or
 * {@link Department} object.
 */
public class ParticipantItem
{
   private QualifiedModelParticipantInfo qualifiedParticipantInfo;
   private DynamicParticipantInfo dynamicParticipantInfo;
   private Department department;

   public ParticipantItem(QualifiedModelParticipantInfo qualifiedParticipantInfo)
   {
      this.qualifiedParticipantInfo = qualifiedParticipantInfo;
   }

   public ParticipantItem(DynamicParticipantInfo dynamicParticipantInfo)
   {
      this.dynamicParticipantInfo = dynamicParticipantInfo;
   }

   public ParticipantItem(Department department)
   {
      this.department = department;
   }

   // Public methods
   public boolean isModelParticipant()
   {
      return (null != qualifiedParticipantInfo);
   }

   public boolean isDynamicParticipant()
   {
      return (null != dynamicParticipantInfo);
   }

   public boolean isDepartment()
   {
      return (null != department);
   }

   // Getter methods
   public QualifiedModelParticipantInfo getQualifiedModelParticipantInfo()
   {
      return qualifiedParticipantInfo;
   }

   public DynamicParticipantInfo getDynamicParticipantInfo()
   {
      return dynamicParticipantInfo;
   }

   public Department getDepartment()
   {
      return department;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }

      if (obj == null)
      {
         return false;
      }

      if (getClass() != obj.getClass())
      {
         return false;
      }

      ParticipantItem other = (ParticipantItem) obj;

      // ModelParticipantInfo equality
      if (null != qualifiedParticipantInfo && null != other.qualifiedParticipantInfo)
      {
         if (qualifiedParticipantInfo.getRuntimeElementOID() == other.qualifiedParticipantInfo.getRuntimeElementOID())
         {
            long dptOid1 = (null != qualifiedParticipantInfo.getDepartment()) ? qualifiedParticipantInfo
                  .getDepartment().getOID() : 0;
            long dptOid2 = (null != other.qualifiedParticipantInfo.getDepartment()) ? other.qualifiedParticipantInfo
                  .getDepartment().getOID() : 0;
            return (dptOid1 == dptOid2);
         }
      }

      // DynamicParticipantInfo equality
      if (null != dynamicParticipantInfo && null != other.dynamicParticipantInfo)
      {
         return dynamicParticipantInfo.getOID()== other.dynamicParticipantInfo.getOID();
      }

      // Department equality
      if (null != department && null != other.department)
      {
         return (department.getOID() == other.department.getOID());
      }

      return false;
   }
}
