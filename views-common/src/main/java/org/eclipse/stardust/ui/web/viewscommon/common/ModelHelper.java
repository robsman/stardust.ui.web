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

import org.eclipse.stardust.engine.api.model.DynamicParticipantInfo;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class ModelHelper
{
   // TODO: This method probably needs to move to the I18Utils class
   /**
    * @param participantInfo
    * @return
    */
   public static String getParticipantName(ParticipantInfo participantInfo)
   {
      String participantName = "<Not Found>";

      if (participantInfo instanceof ModelParticipantInfo)
      {
         ModelParticipantInfo modelParticipantInfo = (ModelParticipantInfo) participantInfo;
         if (modelParticipantInfo.isDepartmentScoped())
         {
            DepartmentInfo departmentInfo = modelParticipantInfo.getDepartment();
            if (modelParticipantInfo instanceof OrganizationInfo)
            {
               OrganizationInfo organizationInfo = (OrganizationInfo) modelParticipantInfo;

               // Format: OrgName - DeptName
               Organization organization = (Organization) ParticipantUtils.getParticipant(organizationInfo);
               
               participantName = I18nUtils.getParticipantName(organization); 
               if (departmentInfo != null)
               {
                  participantName += " - " + departmentInfo.getName();
               }
            }
            else if (modelParticipantInfo instanceof RoleInfo)
            {
               RoleInfo roleInfo = (RoleInfo) modelParticipantInfo;
               Role role = (Role) ModelUtils.getModelCache().getParticipant(roleInfo.getId(), Role.class);

               // Format: RoleName (OrgName - DeptName)

               participantName = I18nUtils.getParticipantName(role);
               if (departmentInfo != null && !Department.DEFAULT.equals(departmentInfo))
               {
                  AdministrationService as = SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
                  Department department = as.getDepartment(departmentInfo.getOID());
                  String organizationName = I18nUtils.getParticipantName(department.getOrganization()); 
                  participantName += " (" + organizationName + " - " + department.getName() + ")";
               }
            }
         }
         else
         {
            participantName = I18nUtils.getParticipantName(ParticipantUtils.getParticipant(participantInfo));
         }
      }
      else if (participantInfo instanceof DynamicParticipantInfo)
      {
         participantName = I18nUtils.getParticipantName(ParticipantUtils.getParticipant(participantInfo));
      }

      return participantName;
   }
}
