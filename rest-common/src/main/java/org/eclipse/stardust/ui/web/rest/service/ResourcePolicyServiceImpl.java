/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import static org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility.ALLOW;
import static org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility.DENY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AccessControlEntry;
import org.eclipse.stardust.engine.api.runtime.AccessControlEntry.EntryType;
import org.eclipse.stardust.engine.api.runtime.AccessControlPolicy;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Privilege;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrincipal;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrivilege;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.ResourcePolicyDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ResourcePolicyContainerDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.security.Participant;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 *          note that most of the code from this class is copied from SecurityDialog
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class ResourcePolicyServiceImpl implements ResourcePolicyService
{
   private static final Logger trace = LogManager.getLogger(ResourcePolicyServiceImpl.class);

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   /**
    *
    */
   public ResourcePolicyContainerDTO getPolicy(String resourceId, boolean isFolder)
   {
      resourceId = DocumentMgmtUtility.checkAndGetCorrectResourceId(resourceId);
      
      Map<String, QualifiedModelParticipantInfo> allParticipants = getAllParticipant();

      List<ResourcePolicyDTO> resourcePolicyDTOs = new ArrayList<ResourcePolicyDTO>();
      List<ResourcePolicyDTO> resourcePolicyInheritedDTOs = new ArrayList<ResourcePolicyDTO>();
      DocumentManagementService dms = getDMS();
      if (resourceId == null || resourceId.equals(""))
         return null;

      Set<AccessControlPolicy> policies = dms.getPolicies(resourceId);
      resourcePolicyDTOs.addAll(buildResourcePolicies(policies, allParticipants, null, isFolder));

      Set<AccessControlPolicy> effectivePolicies = dms.getEffectivePolicies(resourceId);
      resourcePolicyInheritedDTOs.addAll(buildResourcePolicies(effectivePolicies, allParticipants, resourcePolicyDTOs, isFolder));

      ResourcePolicyContainerDTO policyContainerDTO = new ResourcePolicyContainerDTO();

      Comparator<ResourcePolicyDTO> participantComparator = new Comparator<ResourcePolicyDTO>()
      {
         @Override
         public int compare(ResourcePolicyDTO resourcePolicyDTO1, ResourcePolicyDTO resourcePolicyDTO2)
         {
            return resourcePolicyDTO1.participant.name.compareTo(resourcePolicyDTO2.participant.name);
         }
      };

      Collections.sort(resourcePolicyDTOs, participantComparator);
      Collections.sort(resourcePolicyInheritedDTOs, participantComparator);

      policyContainerDTO.own = resourcePolicyDTOs;
      policyContainerDTO.ineherited = resourcePolicyInheritedDTOs;

      return policyContainerDTO;
   }

   /**
    *
    */
   public void savePolicy(String resourceId, List<ResourcePolicyDTO> resourcePolicies, boolean isFolder)
   {
      resourceId = DocumentMgmtUtility.checkAndGetCorrectResourceId(resourceId);
      
      AccessControlPolicy accessControlPolicy = getAccessControlPolicy(resourceId);
      
      if (accessControlPolicy == null)
      {
         throw new I18NException(MessagesViewsCommonBean.getInstance().getParamString(
               "views.myDocumentsTreeView.securityDialog.resourceNotFound", resourceId));
      }
      
      accessControlPolicy.removeAllAccessControlEntries();

      // TODO: this is not good from performance perspective but kept it in order
      // to continue supporting legacy code
      Map<String, DmsPrincipal> allDmsPrincipals = getAllDmsPrincipal();

      for (ResourcePolicyDTO resourcePolicyDTO : resourcePolicies)
      {
         if (StringUtils.isEmpty(resourcePolicyDTO.participantQualifiedId))
         {
            resourcePolicyDTO.participantQualifiedId = resourcePolicyDTO.participant.qualifiedId;
         }

         DmsPrincipal dmsPrincipal = allDmsPrincipals.get(resourcePolicyDTO.participantQualifiedId);
         
         if (dmsPrincipal == null)
         {
            // check if it is 'everyone'
            if (CommonProperties.EVERYONE.equals(resourcePolicyDTO.participantQualifiedId))
            {
               dmsPrincipal = new DmsPrincipal(CommonProperties.EVERYONE);
            }
         }
         
         if (dmsPrincipal == null)
         {
            throw new I18NException(MessagesViewsCommonBean.getInstance().getParamString(
                  "views.myDocumentsTreeView.securityDialog.participantNotFound",
                  resourcePolicyDTO.participantQualifiedId));
         }

         if (isFolder && StringUtils.isNotEmpty(resourcePolicyDTO.create))
         {
            accessControlPolicy.addAccessControlEntry(dmsPrincipal, Collections
                  .<Privilege> singleton(DmsPrivilege.CREATE_PRIVILEGE),
                  resourcePolicyDTO.create.toUpperCase().equals(EntryType.ALLOW.toString())
                        ? EntryType.ALLOW
                        : EntryType.DENY);
         }

         if (StringUtils.isNotEmpty(resourcePolicyDTO.delete))
         {
            accessControlPolicy.addAccessControlEntry(dmsPrincipal, Collections
                  .<Privilege> singleton(DmsPrivilege.DELETE_PRIVILEGE),
                  resourcePolicyDTO.delete.toUpperCase().equals(EntryType.ALLOW.toString())
                        ? EntryType.ALLOW
                        : EntryType.DENY);

            accessControlPolicy.addAccessControlEntry(dmsPrincipal, Collections
                  .<Privilege> singleton(DmsPrivilege.DELETE_CHILDREN_PRIVILEGE), resourcePolicyDTO.delete
                  .toUpperCase().equals(EntryType.ALLOW.toString()) ? EntryType.ALLOW : EntryType.DENY);
         }
         if (StringUtils.isNotEmpty(resourcePolicyDTO.modify))
         {
            accessControlPolicy.addAccessControlEntry(dmsPrincipal, Collections
                  .<Privilege> singleton(DmsPrivilege.MODIFY_PRIVILEGE),
                  resourcePolicyDTO.modify.toUpperCase().equals(EntryType.ALLOW.toString())
                        ? EntryType.ALLOW
                        : EntryType.DENY);
         }
         if (StringUtils.isNotEmpty(resourcePolicyDTO.read))
         {
            accessControlPolicy.addAccessControlEntry(dmsPrincipal, Collections
                  .<Privilege> singleton(DmsPrivilege.READ_PRIVILEGE),
                  resourcePolicyDTO.read.toUpperCase().equals(EntryType.ALLOW.toString())
                        ? EntryType.ALLOW
                        : EntryType.DENY);
         }
         if (StringUtils.isNotEmpty(resourcePolicyDTO.readAcl))
         {
            accessControlPolicy.addAccessControlEntry(dmsPrincipal, Collections
                  .<Privilege> singleton(DmsPrivilege.READ_ACL_PRIVILEGE), resourcePolicyDTO.readAcl.toUpperCase()
                  .equals(EntryType.ALLOW.toString()) ? EntryType.ALLOW : EntryType.DENY);
         }
         if (StringUtils.isNotEmpty(resourcePolicyDTO.modifyAcl))
         {
            accessControlPolicy.addAccessControlEntry(dmsPrincipal, Collections
                  .<Privilege> singleton(DmsPrivilege.MODIFY_ACL_PRIVILEGE), resourcePolicyDTO.modifyAcl.toUpperCase()
                  .equals(EntryType.ALLOW.toString()) ? EntryType.ALLOW : EntryType.DENY);
         }
      }

      getDMS().setPolicy(resourceId, accessControlPolicy);
   }

   /**
    * @param policies
    * @param allParticipants
    * @param ownResoursePolicies
    * @return
    */
   private List<ResourcePolicyDTO> buildResourcePolicies(Set<AccessControlPolicy> policies,
         Map<String, QualifiedModelParticipantInfo> allParticipants, List<ResourcePolicyDTO> ownResoursePolicies, boolean isFolder)
   {
      boolean prepareIinheritedPolicies = false;
      if (ownResoursePolicies != null)
      {
         prepareIinheritedPolicies = true;
      }

      Map<String, ResourcePolicyDTO> resourcePoliciesMap = CollectionUtils.newHashMap();
      List<ResourcePolicyDTO> resourcePolicies = new ArrayList<ResourcePolicyDTO>();
      Iterator<AccessControlPolicy> effectivePoliciesIter = policies.iterator();

      ResourcePolicyDTO resourcePolicyDTO = null;
      boolean addPolicy = true;
      while (effectivePoliciesIter.hasNext())
      {
         AccessControlPolicy accessControlPolicy = effectivePoliciesIter.next();
         Iterator<AccessControlEntry> accessCtrPolIter = accessControlPolicy.getAccessControlEntries().iterator();
         while (accessCtrPolIter.hasNext())
         {
            AccessControlEntry accessControlEntry = accessCtrPolIter.next();
            try
            {
               if (null != resourcePolicyDTO
                     && resourcePoliciesMap.containsKey(accessControlEntry.getPrincipal().getName()))
               {
                  resourcePolicyDTO = resourcePoliciesMap.get(accessControlEntry.getPrincipal().getName());
                  addPolicy = false;
               }
               else
               {
                  Participant participant = new Participant(accessControlEntry.getPrincipal(),
                        allParticipants.get(accessControlEntry.getPrincipal().getName()));
                  ResourcePolicyDTO.ParticipantDTO participantDTO = new ResourcePolicyDTO.ParticipantDTO(participant);
                  resourcePolicyDTO = new ResourcePolicyDTO();
                  resourcePolicyDTO.participant = participantDTO;
                  resourcePoliciesMap.put(participant.getPrincipal().getName(), resourcePolicyDTO);
                  addPolicy = true;
               }

               Set<Privilege> privileges = accessControlEntry.getPrivileges();
               Iterator<Privilege> privilegeIter = privileges.iterator();
               while (privilegeIter.hasNext())
               {
                  Privilege privilege = privilegeIter.next();
                  if (privilege.equals(DmsPrivilege.ALL_PRIVILEGES))
                  {
                     if (isFolder)
                     {
                        resourcePolicyDTO.create = RepositoryUtility.ALLOW;
                     }

                     resourcePolicyDTO.read = RepositoryUtility.ALLOW;
                     resourcePolicyDTO.modify = RepositoryUtility.ALLOW;
                     resourcePolicyDTO.delete = RepositoryUtility.ALLOW;
                     resourcePolicyDTO.readAcl = RepositoryUtility.ALLOW;
                     resourcePolicyDTO.modifyAcl = RepositoryUtility.ALLOW;
                  }
                  else
                  {
                     if (isFolder && privilege.equals(DmsPrivilege.CREATE_PRIVILEGE))
                     {
                        resourcePolicyDTO.create = (accessControlEntry.getType().toString().equals(ALLOW.toUpperCase())
                              ? ALLOW
                              : DENY);
                     }
                     if (privilege.equals(DmsPrivilege.DELETE_PRIVILEGE))
                     {
                        resourcePolicyDTO.delete = (accessControlEntry.getType().toString().equals(ALLOW.toUpperCase())
                              ? ALLOW
                              : DENY);
                     }
                     if (privilege.equals(DmsPrivilege.MODIFY_PRIVILEGE))
                     {
                        resourcePolicyDTO.modify = (accessControlEntry.getType().toString().equals(ALLOW.toUpperCase())
                              ? ALLOW
                              : DENY);
                     }
                     if (privilege.equals(DmsPrivilege.READ_PRIVILEGE))
                     {
                        resourcePolicyDTO.read = (accessControlEntry.getType().toString().equals(ALLOW.toUpperCase())
                              ? ALLOW
                              : DENY);
                     }
                     if (privilege.equals(DmsPrivilege.READ_ACL_PRIVILEGE))
                     {
                        resourcePolicyDTO.readAcl = (accessControlEntry.getType().toString()
                              .equals(ALLOW.toUpperCase()) ? ALLOW : DENY);
                     }
                     if (privilege.equals(DmsPrivilege.MODIFY_ACL_PRIVILEGE))
                     {
                        resourcePolicyDTO.modifyAcl = (accessControlEntry.getType().toString()
                              .equals(ALLOW.toUpperCase()) ? ALLOW : DENY);
                     }
                  }
               }

               if (prepareIinheritedPolicies)
               {
                  if (!checkIfInherited(resourcePolicyDTO, ownResoursePolicies) && addPolicy)
                  {
                     resourcePolicies.add(resourcePolicyDTO);
                  }
               }
               else
               {
                  if (addPolicy)
                  {
                     resourcePolicies.add(resourcePolicyDTO);
                  }
               }
            }
            catch (Exception e)
            {
               trace.debug("Error occurred while creating permissions: " + accessControlEntry.getPrincipal().getName());
            }
         }
      }
      return resourcePolicies;
   }

   /**
    * @param resourcePolicyDTO
    * @param resourcePolicyDTOs
    * @return
    */
   private boolean checkIfInherited(ResourcePolicyDTO resourcePolicyDTO, List<ResourcePolicyDTO> resourcePolicyDTOs)
   {
      for (ResourcePolicyDTO resourPolDto : resourcePolicyDTOs)
      {
         if (resourPolDto.participant.qualifiedId.equals(resourcePolicyDTO.participant.qualifiedId))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * @return
    */
   private Map<String, QualifiedModelParticipantInfo> getAllParticipant()
   {
      List<QualifiedModelParticipantInfo> allParticipants = ParticipantUtils.fetchAllParticipants(true);
      Map<String, QualifiedModelParticipantInfo> participants = new HashMap<String, QualifiedModelParticipantInfo>();
      for (QualifiedModelParticipantInfo qualifiedModelParticipantInfo : allParticipants)
      {
         DmsPrincipal principal = new DmsPrincipal(qualifiedModelParticipantInfo,
               ModelUtils.extractModelId(qualifiedModelParticipantInfo.getQualifiedId()));

         if (!participants.containsKey(principal.getName()))
         {
            participants.put(principal.getName(), qualifiedModelParticipantInfo);
         }
      }
      return participants;
   }

   /**
    * @return
    */
   private Map<String, DmsPrincipal> getAllDmsPrincipal()
   {
      List<QualifiedModelParticipantInfo> allParticipants = ParticipantUtils.fetchAllParticipants(true);
      Map<String, DmsPrincipal> dmsPrincipals = new HashMap<String, DmsPrincipal>();
      for (QualifiedModelParticipantInfo qualifiedModelParticipantInfo : allParticipants)
      {
         DmsPrincipal principal = new DmsPrincipal(qualifiedModelParticipantInfo,
               ModelUtils.extractModelId(qualifiedModelParticipantInfo.getQualifiedId()));

         if (!dmsPrincipals.containsKey(principal.getName()))
         {
            dmsPrincipals.put(principal.getName(), principal);
         }
      }
      return dmsPrincipals;
   }

   /**
    * @param resourceId
    * @return
    */
   private AccessControlPolicy getAccessControlPolicy(String resourceId)
   {
      DocumentManagementService dms = getDMS();
      Set<AccessControlPolicy> applicablePolicies = null;
      AccessControlPolicy accessControlPolicy = null;
      try
      {
         applicablePolicies = dms.getPolicies(resourceId);
         accessControlPolicy = applicablePolicies.iterator().next();
      }
      catch (java.util.NoSuchElementException nee)
      {
         try
         {
            applicablePolicies = dms.getApplicablePolicies(resourceId);
            accessControlPolicy = applicablePolicies.iterator().next();
         }
         catch (Exception e)
         {
            trace.error(e);
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }
      return accessControlPolicy;
   }

   /**
    * @return
    */
   private DocumentManagementService getDMS()
   {
      return serviceFactoryUtils.getDocumentManagementService();
   }
}