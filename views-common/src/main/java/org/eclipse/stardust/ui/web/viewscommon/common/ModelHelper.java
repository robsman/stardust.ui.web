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

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.DepartmentInfoDetails;
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
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

/**
 * @author Subodh.Godbole
 * @author Yogesh.Manware
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
               participantlabel.setParticipantName(I18nUtils.getParticipantName(organization));
               setHierarchyDetails(participantlabel, organization, departmentInfo);
            }
            else if (modelParticipantInfo instanceof RoleInfo)
            {
               RoleInfo roleInfo = (RoleInfo) modelParticipantInfo;
               Role role = (Role) ModelUtils.getModelCache().getParticipant(roleInfo.getId(), Role.class);

               // Format: RoleName (OrgName? or period separated DeptNames)
               participantlabel.setParticipantName(I18nUtils.getParticipantName(role));
               setHierarchyDetails(participantlabel, role.getAllSuperOrganizations().get(0), departmentInfo);
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
    * @param organization
    * @param deptInfo
    */
   private static void setHierarchyDetails(ParticipantLabel participantLabel, Organization organization,
         DepartmentInfo deptInfo)
   {
      Department dept = null;

      if (deptInfo instanceof Department)
      {
         dept = (Department) deptInfo;
      }
      else if (deptInfo instanceof DepartmentInfoDetails)
      {
         dept = ServiceFactoryUtils.getAdministrationService().getDepartment(deptInfo.getOID());
      }

      while (null != organization)
      {
         String orgId = "";
         if (null != dept && (dept instanceof Department))
         {
            orgId = dept.getOrganization().getQualifiedId();
         }

         if (!orgId.equals(organization.getQualifiedId()))
         {
            // add Organization
            participantLabel.addOrganization(I18nUtils.getParticipantName(organization),
                  organization.definesDepartmentScope());
         }
         else
         {
            participantLabel.addDepartment(dept.getName());
            dept = dept.getParentDepartment();
         }

         List<Organization> allSuperOrganizations = organization.getAllSuperOrganizations();
         if (CollectionUtils.isNotEmpty(allSuperOrganizations))
         {
            organization = allSuperOrganizations.get(0);
         }
         else
         {
            organization = null;
            break;
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
      if (department instanceof Department)
      {
         Department deptDetail = (Department) department;
         if (null != deptDetail.getOrganization() && deptDetail.getOrganization().isDepartmentScoped())
         {
            String organizationName = I18nUtils.getParticipantName(deptDetail.getOrganization());
            participantlabel.setParticipantName(organizationName);
            setHierarchyDetails(participantlabel, deptDetail.getOrganization(), deptDetail);
         }
      }
      else
      {
         participantlabel.setParticipantName(department.getName());
      }
      return participantlabel;
   }
}