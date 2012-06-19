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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.model.DynamicParticipantInfo;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedOrganizationInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroupInfo;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.TaskAssignmentConstants;



/**
 * @author anair
 * @version $Revision: $
 */
public class ParticipantUtils
{
   public static enum ParticipantType {
      ORGANIZATION, ROLE, SCOPED_ROLE, SCOPED_ORGANIZATION, USER, USERGROUP;
   }

   /**
    * @param participantInfo
    * @return
    */
   public static ParticipantType getParticipantType(ParticipantInfo participantInfo)
   {
      ParticipantType participantType = null;

      if (null != participantInfo)
      {
         if (participantInfo instanceof OrganizationInfo)
         {
            participantType = !((OrganizationInfo) participantInfo).isDepartmentScoped()
                  ? ParticipantType.ORGANIZATION
                  : ParticipantType.SCOPED_ORGANIZATION;
         }
         else if (participantInfo instanceof RoleInfo)
         {
            participantType = !((RoleInfo) participantInfo).isDepartmentScoped()
                  ? ParticipantType.ROLE
                  : ParticipantType.SCOPED_ROLE;
         }
         else if (participantInfo instanceof UserInfo)
         {
            participantType = ParticipantType.USER;
         }
         else if (participantInfo instanceof UserGroupInfo)
         {
            participantType = ParticipantType.USERGROUP;
         }
      }

      return participantType;
   }

   /**
    * @param department
    * @param organization
    * @param includeDefaultDepartments
    * @return
    */
   public static List<ModelParticipantInfo> getSubParticipants(Department department, Organization organization)
   {
      List<ModelParticipantInfo> subParticipants = CollectionUtils.newArrayList();
      try
      {
         ModelParticipantInfo modelParticipantInfo;
         List<Department> departments;

         // Add the organization itself (for all its department scopes) to the list
         modelParticipantInfo = getScopedParticipant(organization, department);
         subParticipants.add(modelParticipantInfo);
         if (organization.isDepartmentScoped())
         {
            // Get sub-departments
            departments = ServiceFactoryUtils.getQueryService().findAllDepartments(department, organization);
            for (Department dpt : departments)
            {
               subParticipants.add(dpt.getScopedParticipant(organization));
            }
         }

         // Sub-roles
         @SuppressWarnings("unchecked")
         List<Role> subRoles = organization.getAllSubRoles();
         for (Role role : subRoles)
         {
            modelParticipantInfo = getScopedParticipant(role, department);
            subParticipants.add(modelParticipantInfo);
         }

         // Sub-organizations
         @SuppressWarnings("unchecked")
         List<Organization> subOrganizations = organization.getAllSubOrganizations();
         for (Organization subOrg : subOrganizations)
         {
            subParticipants.addAll(getSubParticipants(department, subOrg));

            if (subOrg.isDepartmentScoped())
            {
               // Get sub-departments
               departments = ServiceFactoryUtils.getQueryService().findAllDepartments(department, subOrg);
               for (Department dpt : departments)
               {
                  subParticipants.addAll(getSubParticipants(dpt, subOrg));
               }
            }
         }

         return subParticipants;
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
         return subParticipants;
      }
   }

   /**
    * @param participantInfo
    * @return
    */
   public static Participant getParticipant(ParticipantInfo participantInfo)
   {
      Participant participant = null;

      // Optimization
      if (participantInfo instanceof Participant)
      {
         participant = (Participant) participantInfo;
      }
      else
      {
         switch (getParticipantType(participantInfo))
         {
         case ORGANIZATION:
         case ROLE:
         case SCOPED_ORGANIZATION:
         case SCOPED_ROLE:
            String modelId = ModelUtils.extractModelId(participantInfo.getQualifiedId());
            for (DeployedModel model : ModelUtils.getAllModels())
            {
               if (model.getId().equals(modelId))
               {
                  participant = model.getParticipant(participantInfo.getId());
                  if (null != participant)
                  {
                     break;
                  }
               }
            }
            break;

         case USER:
            participant = ServiceFactoryUtils.getUserService().getUser(participantInfo.getId());
            break;

         case USERGROUP:
            participant = ServiceFactoryUtils.getUserService().getUserGroup(participantInfo.getId());
            break;
         }
      }

      return participant;
   }

   /**
    * @param modelParticipant
    * @param department
    * @return
    */
   public static QualifiedModelParticipantInfo getScopedParticipant(ModelParticipant modelParticipant,
         Department department)
   {
      return (department != null) ? department.getScopedParticipant(modelParticipant) : modelParticipant;
   }

   /**
    * @param qualifiedParticipantId
    * @return
    */
   public static String extractParticipantId(String qualifiedParticipantId)
   {
      if (qualifiedParticipantId.startsWith("{"))
      {
         QName qname = QName.valueOf(qualifiedParticipantId);
         qualifiedParticipantId = qname.getLocalPart();
      }

      return qualifiedParticipantId;
   }

   /**
    * @param participantInfo1
    * @param participantInfo2
    * @return
    */
   public static boolean areEqual(ParticipantInfo participantInfo1, ParticipantInfo participantInfo2)
   {
      // Optimization
      if (participantInfo1 == participantInfo2)
      {
         return true;
      }

      boolean areEqual = false;

      if (participantInfo1 instanceof ModelParticipantInfo && participantInfo2 instanceof ModelParticipantInfo)
      {
         boolean idResult = false;
         if (participantInfo1 instanceof QualifiedModelParticipantInfo
               && participantInfo2 instanceof QualifiedModelParticipantInfo)
         {
            QualifiedModelParticipantInfo qInfo1 = (QualifiedModelParticipantInfo) participantInfo1;
            QualifiedModelParticipantInfo qInfo2 = (QualifiedModelParticipantInfo) participantInfo2;
            idResult = qInfo1.getQualifiedId().equals(qInfo2.getQualifiedId());
         }
         else
         {
            idResult = participantInfo1.getId().equals(participantInfo2.getId());
         }

         if (idResult)
         {
            DepartmentInfo dep1 = ((ModelParticipantInfo) participantInfo1).getDepartment();
            DepartmentInfo dep2 = ((ModelParticipantInfo) participantInfo2).getDepartment();
            long dep1Oid = dep1 == null ? 0 : dep1.getOID();
            long dep2Oid = dep2 == null ? 0 : dep2.getOID();

            areEqual = (dep1Oid == dep2Oid);
         }
      }
      else
      {
         ParticipantType participantType1 = getParticipantType(participantInfo1);
         ParticipantType participantType2 = getParticipantType(participantInfo2);

         if ((participantType1 == ParticipantType.USER && participantType2 == ParticipantType.USER)
               || (participantType1 == ParticipantType.USERGROUP && participantType2 == ParticipantType.USERGROUP))
         {
            areEqual = participantInfo1.getId().equals(participantInfo2.getId());
         }
      }

      return areEqual;
   }

   /**
    * @param user
    * @return
    */
   public static CategorizedParticipants categorizeParticipants(User user)
   {
      List<ModelParticipant> pendingParticipants = new LinkedList<ModelParticipant>();
      ModelCache modelCache = ModelCache.findModelCache();
      for (Grant grant : user.getAllGrants())
      {
         Model model = modelCache.getActiveModel(grant);
         if (model != null)
         {
            Participant participant = model.getParticipant(grant.getId());
            if (participant instanceof ModelParticipant)
            {
               pendingParticipants.add((ModelParticipant) participant);
            }
         }
      }

      Set<ModelParticipant> resolvedParticipants = new HashSet<ModelParticipant>();
      Set<String> participants = new TreeSet<String>();
      while (!pendingParticipants.isEmpty())
      {
         ModelParticipant participant = (ModelParticipant) pendingParticipants.remove(0);
         if (!resolvedParticipants.contains(participant))
         {
            participants.add(participant.getId());
            resolvedParticipants.add(participant);

            for (Iterator< ? > i = participant.getAllSuperOrganizations().iterator(); i.hasNext();)
            {
               ModelParticipant superOrg = (ModelParticipant) i.next();
               if (!resolvedParticipants.contains(superOrg))
               {
                  pendingParticipants.add(superOrg);
               }
            }
         }
      }

      boolean enableAssemblyLineMode = Parameters.instance().getBoolean(
            ProcessPortalConstants.ASSEMBLY_LINE_MODE_ENABLED, true);

      Set<String> assemblyLineParticipants = new HashSet<String>();
      Set<String> workshopParticipants = new HashSet<String>();

      List<DeployedModel> activeModels = modelCache.getActiveModels();
      for (DeployedModel activeModel : activeModels)
      {
         for (Iterator<String> i = participants.iterator(); i.hasNext();)
         {
            String participantId = i.next();
            Participant participant = activeModel.getParticipant(participantId);
            Object mode = (null != participant) //
                  ? participant.getAllAttributes().get(TaskAssignmentConstants.ASSIGNMENT_MODE)
                  : null;
            if (enableAssemblyLineMode && TaskAssignmentConstants.WORK_MODE_ASSEMBLY_LINE.equals(mode))
            {
               // assembly line
               assemblyLineParticipants.add(participantId);
            }
            else
            {
               // work shop, means as usual
               workshopParticipants.add(participantId);
            }
         }
      }
      return new CategorizedParticipants(workshopParticipants, assemblyLineParticipants);
   }

   /**
    * @author Subodh.Godbole
    * 
    */
   public static class CategorizedParticipants
   {
      private final Set<String> workshopParticipants;
      private final Set<String> assemblyLineParticipants;

      /**
       * @param workshopParticipants
       * @param assemblyLineParticipants
       */
      public CategorizedParticipants(Set<String> workshopParticipants, Set<String> assemblyLineParticipants)
      {
         this.workshopParticipants = workshopParticipants;
         this.assemblyLineParticipants = assemblyLineParticipants;
      }

      public Set<String> getWorkshopParticipants()
      {
         return workshopParticipants;
      }

      public Set<String> getAssemblyLineParticipants()
      {
         return assemblyLineParticipants;
      }
   }
   
   /**
    * returns unique view key for worklist
    * 
    * @param participantInfo
    * @return
    */
   public static String getWorklistViewKey(ParticipantInfo participantInfo)
   {
      String viewKey = "";
      if (participantInfo instanceof ModelParticipantInfo)
      {
         ModelParticipantInfo modelParticipantInfo = (ModelParticipantInfo) participantInfo;
         if (modelParticipantInfo.isDepartmentScoped())
         {
            DepartmentInfo departmentInfo = modelParticipantInfo.getDepartment();
            if (modelParticipantInfo instanceof OrganizationInfo)
            {
               OrganizationInfo organizationInfo = (OrganizationInfo) modelParticipantInfo;
               viewKey = String.valueOf(organizationInfo.getRuntimeElementOID());
               if (departmentInfo != null)
               {
                  viewKey += "-" + String.valueOf(departmentInfo.getOID());
               }
            }
            else if (modelParticipantInfo instanceof RoleInfo)
            {
               RoleInfo roleInfo = (RoleInfo) modelParticipantInfo;
               viewKey = String.valueOf(roleInfo.getRuntimeElementOID());
               if (departmentInfo != null && !Department.DEFAULT.equals(departmentInfo))
               {
                  viewKey += "-" + departmentInfo.getRuntimeOrganizationOID() + "-" + departmentInfo.getOID();
               }
            }
         }
         else
         {
            return modelParticipantInfo.getQualifiedId();
         }
      }
      else if (participantInfo instanceof DynamicParticipantInfo)
      {
         viewKey = String.valueOf(((DynamicParticipantInfo) participantInfo).getOID());
      }
      return viewKey;
   }

   /**
    * returns all available participants for all models
    * 
    * @return
    */
   public static List<QualifiedModelParticipantInfo> getAllModelParticipants()
   {
      Collection<DeployedModel> allModels = ModelUtils.getAllModels();
      List<QualifiedModelParticipantInfo> allParticipants = new ArrayList<QualifiedModelParticipantInfo>();
      for (Model model : allModels)
      {
         List<Participant> participants = model.getAllParticipants();
         for (Participant participant : participants)
         {
            if (participant instanceof QualifiedModelParticipantInfo)
            {
               allParticipants.add((QualifiedModelParticipantInfo) participant);
            }
         }
      }
      return allParticipants;
   }

   /**
    * return all model participants along with runtime participants
    * 
    * @author yogesh.manware
    * @return
    */

   public static List<QualifiedModelParticipantInfo> fetchAllParticipants()
   {
      List<QualifiedModelParticipantInfo> allModelParticipants = getAllModelParticipants();

      // This list contains runtime participants along with model participants
      List<QualifiedModelParticipantInfo> allParticipants = new ArrayList<QualifiedModelParticipantInfo>(
            allModelParticipants);

      for (QualifiedModelParticipantInfo modelParticipantInfo : allModelParticipants)
      {
         if (modelParticipantInfo instanceof QualifiedOrganizationInfo && modelParticipantInfo.isDepartmentScoped())
         {
            QualifiedOrganizationInfo qualifiedOrganizationInfo = (QualifiedOrganizationInfo) modelParticipantInfo;
            recursiveSearchforRuntimeParticipants(allParticipants, qualifiedOrganizationInfo, null);
         }
      }
      return allParticipants;
   }

   /**
    * @param allParticipants
    * @param qualifiedOrganizationInfo
    * @param department
    */
   private static void recursiveSearchforRuntimeParticipants(List<QualifiedModelParticipantInfo> allParticipants,
         QualifiedOrganizationInfo qualifiedOrganizationInfo, Department department)
   {
      // fetch subroles for a scoped organization
      Organization organization = (Organization) ParticipantUtils.getParticipant(qualifiedOrganizationInfo);
      List<Role> subRoles = organization.getAllSubRoles();
      List<Organization> subOrganizations = organization.getAllSubOrganizations();
      if (null != department)
      {
         addDepartmentParticipants(allParticipants, subOrganizations, subRoles, department);
      }
      // fetch all departments under scoped organization
      QueryService queryService = ServiceFactoryUtils.getQueryService();
      List<Department> deptList = queryService.findAllDepartments(qualifiedOrganizationInfo.getDepartment(),
            qualifiedOrganizationInfo);

      for (Department department1 : deptList)
      {
         // add department
         QualifiedModelParticipantInfo qualifiedParticipantInfo = department1.getScopedParticipant(department1
               .getOrganization());
         allParticipants.add(qualifiedParticipantInfo);
         addDepartmentParticipants(allParticipants, subOrganizations, subRoles, department1);
      }
   }

   /**
    * @param allParticipants
    * @param subOrganizations
    * @param subRoles
    * @param department
    */
   private static void addDepartmentParticipants(List<QualifiedModelParticipantInfo> allParticipants,
         List<Organization> subOrganizations, List<Role> subRoles, Department department)
   {
      for (Role subRole : subRoles)
      {
         // add scoped roles
         QualifiedModelParticipantInfo participantInfo = ParticipantUtils.getScopedParticipant(subRole, department);
         allParticipants.add(participantInfo);
      }
      for (Organization org : subOrganizations)
      {
         // add scoped roles
         QualifiedModelParticipantInfo participantInfo = ParticipantUtils.getScopedParticipant(org, department);
         allParticipants.add(participantInfo);
         recursiveSearchforRuntimeParticipants(allParticipants, (QualifiedOrganizationInfo) participantInfo, department);
      }
   }

   /**
    * return QualifiedModelParticipantInfo for a provided grant
    * 
    * @author yogesh.manware
    * @param allAvailableParticipants
    * @param grant
    * @return
    */
   public static QualifiedModelParticipantInfo getParticipantModelInfo(
         List<QualifiedModelParticipantInfo> allAvailableParticipants, Grant grant)
   {
      for (QualifiedModelParticipantInfo modelParticipantInfo : allAvailableParticipants)
      {
         if (grant.getQualifiedId().equals(modelParticipantInfo.getQualifiedId()))
         {
            DepartmentInfo department_grant = grant.getDepartment();
            DepartmentInfo department_Available = modelParticipantInfo.getDepartment();

            if (department_grant == null && department_Available == null)
            {
               return modelParticipantInfo;
            }
            // check if the departments id is same as qualified id scoped departments are
            // SAME
            if (department_grant != null && department_Available != null
                  && department_grant.getOID() == department_Available.getOID())
            {
               return modelParticipantInfo;
            }
         }
      }
      return null;
   }
}
