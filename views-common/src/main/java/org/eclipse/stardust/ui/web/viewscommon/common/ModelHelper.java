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

import org.eclipse.stardust.engine.api.dto.DepartmentDetails;
import org.eclipse.stardust.engine.api.model.DynamicParticipantInfo;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class ModelHelper
{
   public static String getParticipantName(ParticipantInfo participantInfo)
   {
      return getParticipantLabel(participantInfo).getLabel();
   }
   
   // TODO: This method probably needs to move to the I18Utils class
   
   /**
    * @param participantInfo
    * @return ParticipantLabel
    */
   public static ParticipantLabel getParticipantLabel(ParticipantInfo participantInfo)
   {
      ParticipantLabel participantlabel = new ParticipantLabel();

      if (participantInfo instanceof ModelParticipantInfo)
      {
         ModelParticipantInfo modelParticipantInfo = (ModelParticipantInfo) participantInfo;
         if (modelParticipantInfo.isDepartmentScoped())
         {
            DepartmentInfo departmentInfo = modelParticipantInfo.getDepartment();
            if (modelParticipantInfo instanceof OrganizationInfo)
            {
               OrganizationInfo organizationInfo = (OrganizationInfo) modelParticipantInfo;

               // Format: OrgName (? or period separated DeptNames)
               Organization organization = (Organization) ParticipantUtils.getParticipant(organizationInfo);
               participantlabel.setType(ParticipantLabel.TYPE.ORGANIZATION);
               participantlabel.setOrganizationName(I18nUtils.getParticipantName(organization)); 
               
               setDepartments(participantlabel, departmentInfo);
            }
            else if (modelParticipantInfo instanceof RoleInfo)
            {
               RoleInfo roleInfo = (RoleInfo) modelParticipantInfo;
               Role role = (Role) ModelUtils.getModelCache().getParticipant(roleInfo.getId(), Role.class);

               // Format: RoleName (OrgName? or period separated DeptNames)
               participantlabel.setType(ParticipantLabel.TYPE.ROLE);
               participantlabel.setRoleName(I18nUtils.getParticipantName(role));
               participantlabel.setOrganizationName(I18nUtils
                     .getParticipantName(role.getAllSuperOrganizations().get(0)));
               if (departmentInfo != null && !Department.DEFAULT.equals(departmentInfo))
               {
                  setDepartments(participantlabel, departmentInfo);
               }
            }
         }
         else
         {
            participantlabel.setParticipantName(I18nUtils.getParticipantName(ParticipantUtils
                  .getParticipant(participantInfo)));
         }
      }
      else if (participantInfo instanceof DynamicParticipantInfo)
      {
         participantlabel.setParticipantName(I18nUtils.getParticipantName(ParticipantUtils
               .getParticipant(participantInfo)));
      }

      return participantlabel;
   }
   
   /**
    * @param participantLabel
    * @param deptInfo
    */
   private static void setDepartments(ParticipantLabel participantLabel, DepartmentInfo deptInfo)
   {
      if (null != deptInfo && (deptInfo instanceof Department))
      {
         Department dept = (Department) deptInfo;
         while (null != dept)
         {
            participantLabel.addDepartment(dept.getName());
            dept = dept.getParentDepartment();
         }
      }
   }   
   
   /**
    * @param departmentInfo
    * @return
    */
   public static ParticipantLabel getDepartmentLabel(DepartmentInfo department)
   {
      ParticipantLabel participantlabel = new ParticipantLabel();
      participantlabel.setType(ParticipantLabel.TYPE.ORGANIZATION);
      
      if (department instanceof Department)
      {
         DepartmentDetails deptDetail = (DepartmentDetails) department;
         if (null != deptDetail.getOrganization() && deptDetail.getOrganization().isDepartmentScoped())
         {
            String organizationName = I18nUtils.getParticipantName(deptDetail.getOrganization());
            participantlabel.setOrganizationName(organizationName);
            setDepartments(participantlabel, deptDetail);
         }
      }
      else
      {
         participantlabel.setParticipantName(department.getName());
      }
      
      return participantlabel;
   }
}