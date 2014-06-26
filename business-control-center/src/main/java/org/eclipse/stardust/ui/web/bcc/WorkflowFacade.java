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
package org.eclipse.stardust.ui.web.bcc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.compatibility.extensions.dms.DmsConstants;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistStatistics.ParticipantStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistStatistics.UserStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistStatisticsQuery;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.BusinessControlCenterLocalizerKey;
import org.eclipse.stardust.ui.web.bcc.jsf.InvalidServiceException;
import org.eclipse.stardust.ui.web.bcc.jsf.PageMessage;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.common.ParticipantDepartmentPair;
import org.eclipse.stardust.ui.web.viewscommon.common.ParticipantDepartmentPairComparator;
import org.eclipse.stardust.ui.web.viewscommon.common.Resetable;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityInstanceWithPrio;
import org.eclipse.stardust.ui.web.viewscommon.helper.processTable.ProcessInstanceTableEntry;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



public class WorkflowFacade implements Resetable
{
   protected final static Logger trace = LogManager.getLogger(WorkflowFacade.class);

   // private static WorkflowFacade instance;

   private ServiceFactory serviceFactory;

   private Map/* <ParticipantDepartmentPair, RoleItem> */roleItems;

   private Map/* <Long, UserItem> */userItems;

   private Map/* <String, Participant> */participants;

   private Map/* <ParticipantDepartmentPair, Role> */teamleadRoles;

   private Map/* <Long, DocumentSet> */documentSetForPiMap;

   private final static String WORKFLOW_FACADE = "carnotBc/WorkflowFacade";

   private User loginUser;

   private User currentUser;

   private WorkflowFacade(ServiceFactory serviceFactory)
   {
      this.serviceFactory = serviceFactory;
      initVars();
   }

   public void initVars()
   {
      roleItems = new HashMap/* <ParticipantDepartmentPair, RoleItem> */();
      userItems = new HashMap/* <Long, UserItem> */();
      participants = new HashMap/* <String, Participant> */();
      teamleadRoles = new HashMap/* <ParticipantDepartmentPair, Role> */();
      documentSetForPiMap = new HashMap/* <Long, DocumentSet> */();
      buildRoleAndUserItemList();
      try
      {
         loginUser = getLoginUser();
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
         loginUser = null;
      }
   }

   public DocumentManagementService getDocumentManagementService()
   {
      return getServiceFactory().getDocumentManagementService();
   }

   private User modifyUser(User user)
   {
      try
      {
         return getUserService().modifyUser(user);
      }
      catch (AccessForbiddenException e)
      {
         PageMessage.setMessage(FacesMessage.SEVERITY_WARN, Localizer
               .getString(LocalizerKey.ACCESS_FORBIDDEN), null);
      }
      catch (Exception e)
      {
         String message = e.getMessage();
         if (StringUtils.isEmpty(message))
         {
            message = Localizer.getString(
                  BusinessControlCenterLocalizerKey.CANNOT_MODIFY_USER, "USER", user
                        .getAccount());
         }
         FacesContext.getCurrentInstance().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR, "", message));
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   private void buildRoleAndUserItemList()
   {

     
      List<ModelParticipantInfo> roles = CollectionUtils.newArrayList();
      
      ModelCache modelCache = ModelCache.findModelCache();

      for (Participant p:modelCache.getAllParticipants())
      {        
         this.participants.put(p.getQualifiedId(), p);
         if (p instanceof Role || p instanceof Organization)
         {
            ModelParticipantInfo mp = (ModelParticipantInfo) p;
            roles.add(mp);
            
            boolean isTeamLead = isTeamLead(mp);            
                 
            if (isTeamLead)
            {
               teamleadRoles.put(ParticipantUtils.getParticipantUniqueKey(p), p);
            }

            if (mp.isDepartmentScoped())
            {

               List<ModelParticipantInfo> runtimeScopes = getRuntimeScopes((ModelParticipantInfo) p);
               for (ModelParticipantInfo modelParticipantInfo : runtimeScopes)
               {
                  roles.add(modelParticipantInfo);
                  if (isTeamLead)// this is a team lead role
                  {                     
                     String participantKey = ParticipantUtils.getParticipantUniqueKey(modelParticipantInfo);
                     teamleadRoles.put(participantKey, modelParticipantInfo);
                  
                  }
               }
            }

         }

      }
      
      UserQuery query = UserQuery.findActive();
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
      query.setPolicy(userPolicy);
      List<User> users = getQueryService().getAllUsers(query);
      
      Pair/* <Map<String, RoleItem>, Map<Long, UserItem>> */pair = getWorklistStatistics(roles, users);
      ParticipantDepartmentPairComparator pairComparator = new ParticipantDepartmentPairComparator();
      roleItems = new TreeMap(pairComparator);
      roleItems.putAll((Map) pair.getFirst());

      userItems = (Map) pair.getSecond();
   }
   
   private boolean isTeamLead(ModelParticipantInfo participant)
   {
      if (participant instanceof Role)
      {
         Role role = (Role) participant;
         return !role.getTeams().isEmpty();
      }
      return false;
   }

   /**
    * @param modelParticipantInfo
    * @return
    */
   private List<ModelParticipantInfo> getRuntimeScopes(ModelParticipantInfo modelParticipantInfo)
   {
      List<ModelParticipantInfo> modelParticipants = new ArrayList<ModelParticipantInfo>();

      if (modelParticipantInfo instanceof Organization)
      {
         Organization organization = (Organization) modelParticipantInfo;
         List<Department> departments = getQueryService().findAllDepartments(null,
               organization);

         for (Department department : departments)
         {
            modelParticipants.add(department.getScopedParticipant(organization));
         }
      }
      else if (modelParticipantInfo instanceof Role)
      {
         Role role = (Role) modelParticipantInfo;
         Organization parentOrganization = null;
         List<Organization> leadsOrganizations = role.getTeams();
         if (CollectionUtils.isEmpty(leadsOrganizations))
         {
            List<Organization> worksForOrganizations = role.getClientOrganizations();
            if ((worksForOrganizations != null) && (worksForOrganizations.size() > 0))
            {
               parentOrganization = worksForOrganizations.get(0);
            }
         }
         else
         {
            parentOrganization = leadsOrganizations.get(0);
         }
         List<Department> departments = getQueryService().findAllDepartments(null,
               parentOrganization);

         for (Department department : departments)
         {
            modelParticipants.add(department.getScopedParticipant(role));
         }
      }

      return modelParticipants;
   }

   private void addGrant(User user, ModelParticipantInfo modelParticipantInfo)
   {
      user.addGrant(modelParticipantInfo);
   }

   private void removeGrant(User user, ModelParticipantInfo modelParticipantInfo)
   {
      user.removeGrant(modelParticipantInfo);
   }

   protected ServiceFactory getServiceFactory()
   {
      if (serviceFactory == null)
      {
         throw new InvalidServiceException("", Localizer
               .getString(BusinessControlCenterLocalizerKey.INVALID_SERVICE_FACTORY));
      }
      return serviceFactory;
   }

   public QueryService getQueryService()
   {
      QueryService service = getServiceFactory().getQueryService();
      if (service == null)
      {
         throw new InvalidServiceException("", Localizer
               .getString(BusinessControlCenterLocalizerKey.INVALID_QUERY_SERVICE));
      }
      return service;
   }

   protected UserService getUserService()
   {
      UserService service = getServiceFactory().getUserService();
      if (service == null)
      {
         throw new InvalidServiceException("", Localizer
               .getString(BusinessControlCenterLocalizerKey.INVALID_USER_SERVICE));
      }
      return service;
   }

   public WorkflowService getWorkflowService()
   {
      WorkflowService service = getServiceFactory().getWorkflowService();
      if (service == null)
      {
         throw new InvalidServiceException("", Localizer
               .getString(BusinessControlCenterLocalizerKey.INVALID_WORKFLOW_SERVICE));
      }
      return service;
   }

   public AdministrationService getAdministrationService()
   {
      AdministrationService service = getServiceFactory().getAdministrationService();
      if (service == null)
      {
         throw new InvalidServiceException(
               "",
               Localizer
                     .getString(BusinessControlCenterLocalizerKey.INVALID_ADMINISTARTION_SERVICE));
      }
      return service;
   }

   public static WorkflowFacade createWorkflowFacade(ServiceFactory serviceFactory)
   {
      trace.info("Initializing Business Control Center");

      WorkflowFacade facade = new WorkflowFacade(serviceFactory);
      SessionContext context = SessionContext.findSessionContext();
      if (context != null)
      {
         context.bind(WORKFLOW_FACADE, facade);
         return facade;
      }
      return null;
   }

   public static WorkflowFacade getWorkflowFacade()
   {
      SessionContext context = SessionContext.findSessionContext();
      WorkflowFacade facade = null;
      if (context != null)
      {
         facade = (WorkflowFacade) context.lookup(WORKFLOW_FACADE);
      }
      if (facade == null)
      {
         facade = createWorkflowFacade(ServiceFactoryUtils.getServiceFactory());
      }

      if (facade == null)
      {
         throw new InvalidServiceException("", Localizer.getString(BusinessControlCenterLocalizerKey.INVALID_SESSION));
      }
      return facade;
   }

   public boolean isValueBindingNullable()
   {
      return false;
   }

   public void reset()
   {
      initVars();
   }

   public List<RoleItem> getAllRoles()
   {
      try
      {
         return new ArrayList<RoleItem>(roleItems.values());
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
      return null;
   }
   
   /**
    * 
    * @return
    */
   public List<RoleItem> getAllRolesExceptCasePerformer()
   {
      List<RoleItem> roles = getAllRoles();
      Iterator<RoleItem> roleIterator = roles.iterator();
      while (roleIterator.hasNext())
      {
         RoleItem roleItem = roleIterator.next();
         if (PredefinedConstants.CASE_PERFORMER_ID.equals(roleItem.getRole().getId()))
         {
            roleIterator.remove();
         }
      }
      return roles;
   }

   /**
    * @param modelParticipantInfo
    * @return
    */
   public RoleItem getRoleItem(ModelParticipantInfo modelParticipantInfo)
   {
      RoleItem roleItem = null;
      if (modelParticipantInfo != null)
      {
         for (RoleItem item : getAllRoles())
         {
            if (item.getRole().getQualifiedId().equals(modelParticipantInfo.getQualifiedId()))
            {
               if (modelParticipantInfo.isDepartmentScoped())
               {
                  if (org.eclipse.stardust.ui.web.common.util.StringUtils.areEqual(
                        modelParticipantInfo.getDepartment(), item.getRole().getDepartment()))
                  {
                     roleItem = item;
                     break;
                  }
               }
               else
               {
                  roleItem = item;
                  break;
               }
            }
         }
         
         if (roleItem == null)
         {
            WorklistStatistics worklistStatistices = (WorklistStatistics) getQueryService().getAllUsers(
                  WorklistStatisticsQuery.forAllUsers());
            ParticipantStatistics pStatistics = worklistStatistices.getModelParticipantStatistics(modelParticipantInfo);
            roleItem = new RoleItem(modelParticipantInfo);
            if (pStatistics != null)
            {
               roleItem.addWorklistEntry(pStatistics.nWorkitems);
               roleItem.addUser(pStatistics.nUsers);
               roleItem.addLoggedInUser(pStatistics.nLoggedInUsers);
            }
            return roleItem;
         }

      }
      return roleItem;
   }

   public UserItem getUserItem(long userOid)
   {
      if (CollectionUtils.isEmpty(userItems))
      {
         buildRoleAndUserItemList();
      }

      UserItem userItem = (UserItem) userItems.get(new Long(userOid));
      if (null == userItem)
      {
         buildRoleAndUserItemList();
         userItem = (UserItem) userItems.get(new Long(userOid));
      }
     return userItem;
   }

   public UserItem getUserItem(User user)
   {
      UserItem userItem = getUserItem(user.getOID());
      if (userItem != null)
      {
         userItem.setUser(user);
      }
      return userItem;
   }

   public List<UserItem> getAllUsersAsUserItems(UserQuery query)
   {
      Users users = getAllUsers(query);
      return getAllUsersAsUserItems(users);
   }

   public List<UserItem> getAllUsersAsUserItems(Users users)
   {
      List<UserItem> userItems = CollectionUtils.newArrayList();

      for (User user : users)
      {
         UserItem userItem = getUserItem(user);
         if (userItem != null)
         {
            userItems.add(userItem);
         }
      }

      return userItems;
   }

   public ActivityInstances getAllActivityInstances(ActivityInstanceQuery query)
   {
      try
      {
         return getQueryService().getAllActivityInstances(query);
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
      return null;
   }

   public long getActivityInstancesCount(ActivityInstanceQuery query)
   {
      try
      {
         return getQueryService().getActivityInstancesCount(query);
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
      return 0;
   }

   private void setFullUserDetails(UserItem userItem)
   {
      List<UserItem> list = CollectionUtils.newArrayList();
      list.add(userItem);
      setFullUserDetails(list);
   }

   private void setFullUserDetails(List<UserItem> userItems)
   {
      UserQuery query = UserQuery.findAll();
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Full);
      userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
      query.setPolicy(userPolicy);
      
      Iterator uIter = userItems.iterator();
      FilterOrTerm filter = query.getFilter().addOrTerm();
      Map lookupMap = new HashMap();
      while (uIter.hasNext())
      {
         UserItem userItem = (UserItem) uIter.next();
         long uOid = userItem.getUser().getOID();
         if (!UserDetailsLevel.Full.equals(userItem.getUser().getDetailsLevel()))
         {
            filter.add(UserQuery.OID.isEqual(uOid));
            lookupMap.put(new Long(uOid), userItem);
         }
      }
      if (!lookupMap.isEmpty())
      {
         uIter = getAllUsers(query).iterator();
         while (uIter.hasNext())
         {
            User user = (User) uIter.next();
            UserItem userItem = (UserItem) lookupMap.get(new Long(user.getOID()));
            if (userItem != null)
            {
               userItem.setUser(user);
            }
         }
      }
   }

   public long addUserToRole(RoleItem role, List/* <UserItem> */users)
   {
      long userCount = 0;
      if (users != null)
      {
         setFullUserDetails(users);
         for (Iterator/* <UserItem> */userIter = users.iterator(); userIter.hasNext();)
         {
            UserItem userItem = (UserItem) userIter.next();
            User user = userItem.getUser();
            addGrant(user, role.getRole());
            user = modifyUser(user);
            if (user != null)
            {
               userItem.setUser(user);
               userItem.addRoles(1);
               userItem.addIndirectItemCount(role.getWorklistCount());
               userCount++;
            }
         }
      }
      role.addUser(userCount);
      return userCount;
   }

   public long addRolesToUser(UserItem userItem, List<RoleItem> roles)
   {
      long roleCount = 0;
      if (roles != null)
      {
         setFullUserDetails(userItem);
         for (Iterator<RoleItem> roleIter = roles.iterator(); roleIter.hasNext();)
         {
            RoleItem roleItem = (RoleItem) roleIter.next();
            User user = userItem.getUser();
            addGrant(user, roleItem.getRole());
            user = modifyUser(user);
            if (user != null)
            {
               userItem.setUser(user);
               userItem.addIndirectItemCount(roleItem.getWorklistCount());
               roleItem.addUser(1);
               roleCount++;
            }
         }
      }
      userItem.addRoles(roleCount);
      return roleCount;
   }

   public long removeUserFromRole(RoleItem role, List/* <UserItem> */users)
   {
      long userCount = 0;
      if (users != null)
      {
         setFullUserDetails(users);
         for (Iterator/* <UserItem> */userIter = users.iterator(); userIter.hasNext();)
         {
            UserItem userItem = (UserItem) userIter.next();
            User user = userItem.getUser();
            removeGrant(user, role.getRole());
            user = modifyUser(user);
            if (user != null)
            {
               userItem.setUser(user);
               userItem.removeRoles(1);
               userItem.removeIndirectItemCount(role.getWorklistCount());
               userCount++;
            }
         }
      }
      role.removeUser(userCount);
      return userCount;
   }

   public long removeRolesFromUser(UserItem userItem, List<RoleItem> roles)
   {
      long roleCount = 0;
      if (CollectionUtils.isNotEmpty(roles))
      {
         setFullUserDetails(userItem);
         for (RoleItem roleItem:roles)
         {            
            User user = userItem.getUser();
            removeGrant(user, roleItem.getRole());
            user = modifyUser(user);
            if (user != null)
            {
               userItem.setUser(user);
               userItem.removeIndirectItemCount(roleItem.getWorklistCount());
               roleItem.removeUser(1);
               roleCount++;
            }
         }
      }
      userItem.removeRoles(roleCount);
      return roleCount;
   }

   public Participant getParticipant(String id)
   {
      Participant participant = (Participant) participants.get(id);
      if (participant == null)
      {
         // Darf eigentlich nicht vorkommen.
         participant = getQueryService().getParticipant(id);
      }
      return participant;
   }

   public long getProcessInstancesCount(ProcessInstanceQuery query)
   {
      try
      {
         return getQueryService().getProcessInstancesCount(query);
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
      return 0;
   }

   public ProcessInstances getAllProcessInstances(ProcessInstanceQuery query)
   {
      try
      {
         return getQueryService().getAllProcessInstances(query);
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
      return null;
   }

   public long setProcessPriorities(Map processMap)
   {
      long count = 0;
      if (processMap != null)
      {
         for (Iterator entryIter = processMap.entrySet().iterator(); entryIter.hasNext();)
         {
            Map.Entry entry = (Map.Entry) entryIter.next();
            long processOid = ((Long) entry.getKey()).longValue();
            Object value = entry.getValue();
            try
            {
               if (value instanceof ActivityInstanceWithPrio)
               {
                  ActivityInstanceWithPrio ai = (ActivityInstanceWithPrio) value;
                  ProcessInstanceUtils.setProcessPriority(processOid, ai.getPriority());
                  ai.applyChanges();
                  ++count;
               }
               if (value instanceof ProcessInstanceTableEntry)
               {
                  ProcessInstanceTableEntry pi = (ProcessInstanceTableEntry) value;
                  ProcessInstanceUtils.setProcessPriority(processOid, pi.getPriority());
                  pi.applyChanges();
                  ++count;
               }
            }
            catch (Exception e)
            {
               PageMessage.setMessage(FacesMessage.SEVERITY_ERROR, "", e.getMessage());
            }
         }
      }
      return count;
   }

   public void activitiesDelegated(List/* <ActivityInstance> */activityInstances,
         Participant participant)
   {
      if (!CollectionUtils.isEmpty(activityInstances))
      {
         buildRoleAndUserItemList();
      }
   }

   public Object getInDataPath(long processInstanceOID, String dataPath)
   {
      return getInDataPaths(processInstanceOID).get(dataPath);
   }

   public Map getInDataPaths(long processInstanceOID)
   {
      return getWorkflowService().getInDataPaths(processInstanceOID, null);
   }

   public Collection/* <Participant> */getAllParticipants()
   {
      return participants.values();
   }

   public Users getAllUsers(UserQuery query)
   {
      return getQueryService().getAllUsers(query);
   }

   public List/* <ActivityInstanceWithPrio> */getAliveActivityInstances(
         ProcessDefinition pd)
   {
      List/* <ActivityInstanceWithPrio> */aiList = new ArrayList/*
                                                                 * <ActivityInstanceWithPrio
                                                                 * >
                                                                 */();
      if (pd != null)
      {
         ActivityInstanceQuery query = ActivityInstanceQuery.findAlive(pd.getId());
         ActivityInstances ais = getAllActivityInstances(query);
         for (Iterator aiIter = ais.iterator(); aiIter.hasNext();)
         {
            ActivityInstance ai = (ActivityInstance) aiIter.next();
            aiList.add(new ActivityInstanceWithPrio(ai));
         }
      }
      return aiList != null ? aiList : new ArrayList/* <ActivityInstanceWithPrio> */();
   }

   public Collection<ProcessDefinition> getAllProcessDefinitions()
   {
      Iterator<DeployedModel> modelIter = ModelCache.findModelCache().getAllModels().iterator();
      Set<String> receivedProcesses = new HashSet<String>();
      List<ProcessDefinition> processes = new ArrayList<ProcessDefinition>();
      while (modelIter.hasNext())
      {
         Model model = (Model) modelIter.next();
         Iterator<ProcessDefinition> pdIter = model.getAllProcessDefinitions().iterator();
         while (pdIter.hasNext())
         {
            ProcessDefinition pd =  pdIter.next();
            if (!receivedProcesses.contains(pd.getQualifiedId()))
            {
               receivedProcesses.add(pd.getQualifiedId());
               processes.add(pd);
            }
         }
      }
      return processes;
   }
   /**
    * 
    * @param processId
    * @return
    */
   @Deprecated
   public Collection<Activity> getAllActivities(String processId)
   {
      Iterator modelIter = ModelCache.findModelCache().getAllModels().iterator();
      Set receivedActivities = new HashSet();
      List activities = new ArrayList();
      while (modelIter.hasNext())
      {
         Model model = (Model) modelIter.next();
         ProcessDefinition pd = model.getProcessDefinition(processId);
         if (pd != null)
         {
            Iterator aIter = pd.getAllActivities().iterator();
            while (aIter.hasNext())
            {
               Activity activity = (Activity) aIter.next();
               if (!receivedActivities.contains(activity.getId()))
               {
                  receivedActivities.add(activity.getId());
                  activities.add(activity);
               }
            }
         }
      }
      return activities;
   }
   /**
    * method returns all activity by a process id in all model version (based on FQID of process definition)
    * @param pd
    * @return
    */
   public Collection<Activity> getAllActivities(ProcessDefinition pd)
   {
      String modelId = ModelUtils.extractModelId(pd.getQualifiedId());
      Collection<DeployedModel> models = ModelCache.findModelCache().getAllModels();
      Set<String> receivedActivities = new HashSet<String>();
      List<Activity> activities = new ArrayList<Activity>();

      for (DeployedModel model : models)
      {
         if(model.getId().equals(modelId))
         {
            ProcessDefinition processDefinition = model.getProcessDefinition(pd.getId());
            if (processDefinition != null)
            {
               Iterator<Activity> aIter = processDefinition.getAllActivities().iterator();
               while (aIter.hasNext())
               {
                  Activity activity = aIter.next();
                  if (!receivedActivities.contains(activity.getId()))
                  {
                     receivedActivities.add(activity.getId());
                     activities.add(activity);
                  }
               }
            }
            
         }

      }
      return activities;
   }

   public List getDocumentSetForPI(ProcessInstance pi)
   {
      List documentSet = null;
      if (pi != null)
      {
         documentSet = (List) documentSetForPiMap.get(new Long(pi.getOID()));
         if (documentSet == null)
         {
            documentSet = (List) getInDataPath(pi.getOID(),
                  DmsConstants.PATH_ID_ATTACHMENTS);
         }
      }
      return documentSet;
   }

   public void setDocumentSetForPI(ProcessInstance pi, List documentSet,
         boolean propagateToAT)
   {
      if (pi != null && documentSet != null)
      {
         documentSetForPiMap.put(new Long(pi.getOID()), documentSet);
         if (propagateToAT)
         {
            getWorkflowService().setOutDataPath(pi.getOID(),
                  DmsConstants.PATH_ID_ATTACHMENTS, documentSet);
         }
      }
   }

   public void abortProcessInstance(ProcessInstance pi)
   {
      if (pi != null)
      {
         documentSetForPiMap.remove(new Long(pi.getOID()));
         try
         {
            getAdministrationService().abortProcessInstance(pi.getOID());
         }
         catch (InvalidServiceException e)
         {
            PageMessage.setMessage(e);
         }
      }
   }

   public void abortActivityInstance(ActivityInstance activity, AbortScope abortScope)
   {
      getWorkflowService().abortActivityInstance(activity.getOID(), abortScope);
   }

   public User getLoginUser()
   {
      if (loginUser == null)
      {
         loginUser = SessionContext.findSessionContext().getUser();
      }
      return loginUser;
   }

   // @SuppressWarnings("unchecked")
   public static boolean isUserAdmin(User user)
   {
      if (user != null)
      {
         for (Grant grant : user.getAllGrants())
         {
            if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(grant.getQualifiedId()))
            {
               return true;
            }
         }
      }
      return false;
   }

   public boolean isCurrentUserAdmin()
   {
      User user = getLoginUser();
      return isUserAdmin(user);
   }

   public String getNotes(ActivityInstance activityInstance)
   {
      if (activityInstance != null
            && activityInstance.isScopeProcessInstanceNoteAvailable())
      {
         try
         {
            WorkflowService ws = getWorkflowService();
            ProcessInstance pi = ws.getProcessInstance(activityInstance
                  .getProcessInstanceOID());
            if (pi.getOID() != pi.getScopeProcessInstanceOID())
            {
               pi = ws.getProcessInstance(pi.getScopeProcessInstanceOID());
            }
            ProcessInstanceAttributes pia = pi.getAttributes();
            List notes = pia.getNotes();
            if (notes.isEmpty())
            {
               return "";
            }
            else
            {
               Note note = (Note) notes.get(notes.size() - 1);
               return note.getText();
            }
         }
         catch (InvalidServiceException e)
         {
         }
      }
      return null;
   }

   public boolean isNoteEnabled(ActivityInstance activityInstance)
   {
      /*
       * ModelCache modelCache = ModelCache.findModelCache(); if(activityInstance != null
       * && modelCache != null) { Model model =
       * modelCache.getModel(activityInstance.getModelOID()); if(model != null) {
       * ProcessDefinition pd = model.getProcessDefinition(
       * activityInstance.getProcessDefinitionId()); Iterator dataPathIter =
       * pd.getAllDataPaths().iterator(); while(dataPathIter.hasNext()) { DataPath
       * dataPath = (DataPath)dataPathIter.next();
       * if(BusinessControlCenterConstants.NOTES.equals(dataPath.getId()) &&
       * String.class.isAssignableFrom(dataPath.getMappedType())) { return true; } } } }
       */
      return true;
   }

   public Collection/* <Participant> */getCumulatedRoles()
   {
      ModelCache modelCache = ModelCache.findModelCache();
      Map/* <String, Participant> */participants = new HashMap/* <String, Participant> */();
      if (modelCache != null)
      {
         Iterator/* <Model> */modelIter = modelCache.getAllModels().iterator();
         while (modelIter.hasNext())
         {
            Model model = (Model) modelIter.next();
            for (Iterator pIter = model.getAllParticipants().iterator(); pIter.hasNext();)
            {
               Participant participant = (Participant) pIter.next();
               String participantId = participant.getId();
               if ((participant instanceof Role || participant instanceof Organization)
                     && !participants.containsKey(participantId))
               {
                  participants.put(participantId, participant);
               }
            }
         }
      }
      return participants.values();
   }

   public Collection/* <Participant> */getCommonParticipantsFromModels(
         Set/* <Integer> */models)
   {
      ModelCache modelCache = ModelCache.findModelCache();
      Map/* <String, Participant> */participants = new HashMap/* <String, Participant> */();
      List/* <String> */commonParticipants = new ArrayList/* <String> */();
      List/* <Participant> */result = new ArrayList/* <Participant> */();
      if (modelCache != null && models != null)
      {
         Iterator/* <Model> */modelIter = modelCache.getAllModels().iterator();
         while (modelIter.hasNext())
         {
            Model model = (Model) modelIter.next();
            if (models.contains(new Integer(model.getModelOID())))
            {
               if (participants.isEmpty())
               {
                  for (Iterator pIter = model.getAllParticipants().iterator(); pIter
                        .hasNext();)
                  {
                     Participant participant = (Participant) pIter.next();
                     String participantId = participant.getId();
                     if (participant instanceof Role
                           || participant instanceof Organization)
                     {
                        participants.put(participantId, participant);
                        commonParticipants.add(participantId);
                     }
                  }
               }
               else
               {
                  List/* <String> */modelParticipants = new ArrayList/* <String> */();
                  for (Iterator pIter = model.getAllParticipants().iterator(); pIter
                        .hasNext();)
                  {
                     Participant participant = (Participant) pIter.next();
                     modelParticipants.add(participant.getId());
                  }
                  commonParticipants.retainAll(modelParticipants);
               }
            }
         }
      }
      for (Iterator pIter = participants.values().iterator(); pIter.hasNext();)
      {
         Participant participant = (Participant) pIter.next();
         if (commonParticipants.contains(participant.getId()))
         {
            result.add(participant);
         }
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   private Pair/* <Map<ParticipantDepartmentPair, RoleItem>, Map<String, UserItem>> */getWorklistStatistics(
         List/* <Role> */roles, List/* <User> */users)
   {
      WorklistStatistics worklistStatistices = (WorklistStatistics) getQueryService()
            .getAllUsers(WorklistStatisticsQuery.forAllUsers());
      Map/* <ParticipantDepartmentPair, RoleItem> */roleItems = new HashMap/*
                                                                            * <ParticipantDepartmentPair
                                                                            * , RoleItem>
                                                                            */();
      Map/* <String, UserItem> */userItems = new HashMap/* <String, UserItem> */();
      for (Iterator entryIter = roles.iterator(); entryIter.hasNext();)
      {
         ModelParticipantInfo role = (ModelParticipantInfo) entryIter.next();
         ParticipantStatistics pStatistics = worklistStatistices
               .getModelParticipantStatistics(role);
         RoleItem roleItem = new RoleItem(role);
         if (pStatistics != null)
         {
            roleItem.addWorklistEntry(pStatistics.nWorkitems);
            roleItem.addUser(pStatistics.nUsers);
            roleItem.addLoggedInUser(pStatistics.nLoggedInUsers);
         }
         roleItems.put(ParticipantDepartmentPair.getParticipantDepartmentPair(role),
               roleItem);
      }
      for (Iterator entryIter = users.iterator(); entryIter.hasNext();)
      {
         User user = (User) entryIter.next();
         UserStatistics uStatistics = worklistStatistices
               .getUserStatistics(user.getOID());
         UserItem userItem = new UserItem(user, uStatistics != null
               ? uStatistics.loggedIn
               : false);
         if (uStatistics != null)
         {
            userItem.addDirectItemCount(uStatistics.nPrivateWorkitems);
            userItem.addIndirectItemCount(uStatistics.nSharedWorkitems);
            userItem.addRoles(uStatistics.nGrants);
         }
         userItems.put(new Long(user.getOID()), userItem);
      }
      return new Pair/* <Map<ParticipantDepartmentPair, RoleItem>, Map<String, UserItem>> */(
            roleItems, userItems);
   }

   public List getTeamleadRoles()
   {
      return new ArrayList(teamleadRoles.values());
   }

   public UserQuery getTeamQuery(boolean includeTeamLeader)
   {
      return getTeamQuery(includeTeamLeader, false);
   }
   
   /**
    * For deputy, if user is Admin, all users should be visible
    * 
    * @param includeTeamLeader
    * @param excludeFilterForAdmin
    * @return
    */
   public UserQuery getTeamQuery(boolean includeTeamLeader, boolean excludeFilterForAdmin)
   {
      UserQuery query = UserQuery.findActive();
      User user = getLoginUser();
      if (user != null)
      {       
         if (excludeFilterForAdmin && user.isAdministrator())
         {
            // Deputy- For Admin user, return all users
            return query;
         }
         FilterTerm filter = query.getFilter().addOrTerm();
         
         ModelParticipantInfo modelParticipantInfo;
         Model model;
         Role role;
         Department department;
         
         List<Grant> grants = user.getAllGrants();
         for (Grant grant:grants)
         {            
            if (!grant.isOrganization())
            {
               model = ModelCache.findModelCache().getActiveModel(grant);
               role = model.getRole(grant.getId());
               department = grant.getDepartment();
               
               if (department != null)
               {
                  modelParticipantInfo = department.getScopedParticipant(role);
               }
               else
               {
                  modelParticipantInfo = role;
               }

               if (null != modelParticipantInfo)
               {
                  String participantKey = ParticipantUtils.getParticipantUniqueKey(modelParticipantInfo);
                  if (teamleadRoles.containsKey(participantKey))
                  {
                     filter.add(ParticipantAssociationFilter.forTeamLeader((RoleInfo) modelParticipantInfo));
                  }
               }
            }
         }
         if (includeTeamLeader)
         {
            filter.add(UserQuery.OID.isEqual(user.getOID()));
         }
      }
      else
      {
         query.where(UserQuery.OID.isEqual(0));
      }
      return query;
   }

   /**
    * @return
    */
   public boolean isTeamLead()
   {
      User user = getLoginUser();
      if (user != null)
      {
         ModelParticipantInfo modelParticipantInfo;
         Model model;
         Role role;
         Department department;

         List<Grant> grants = user.getAllGrants();
         for (Grant grant:grants)
         {
            if (!grant.isOrganization())
            {
               model = ModelCache.findModelCache().getActiveModel(grant);
               role = model.getRole(grant.getId());
               department = grant.getDepartment();

               if (department != null)
               {
                  modelParticipantInfo = department.getScopedParticipant(role);
               }
               else
               {
                  modelParticipantInfo = role;
               }

               String participantKey = ParticipantUtils.getParticipantUniqueKey(modelParticipantInfo);
               if (teamleadRoles.containsKey(participantKey))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }

   /**
    * @param excludeThisUser
    * @return
    */
   public UserQuery getUsersWithSimilarGrants(boolean excludeThisUser)
   {
      UserQuery query = UserQuery.findActive();
      User user = getLoginUser();
      if (user != null)
      {
         FilterTerm filter = query.getFilter().addOrTerm();

         ModelParticipantInfo modelParticipantInfo;
         Model model;
         Role role;
         Department department;

         List<Grant> grants = user.getAllGrants();
         for (Grant grant:grants)
         {
            if (!grant.isOrganization())
            {
               model = ModelCache.findModelCache().getActiveModel(grant);
               role = model.getRole(grant.getId());
               department = grant.getDepartment();

               if (department != null)
               {
                  modelParticipantInfo = department.getScopedParticipant(role);
               }
               else
               {
                  modelParticipantInfo = role;
               }

               String participantKey = ParticipantUtils.getParticipantUniqueKey(modelParticipantInfo);
               if (teamleadRoles.containsKey(participantKey))
               {
                  filter.add(ParticipantAssociationFilter.forParticipant(((RoleInfo) modelParticipantInfo)));
               }
            }
         }

         if (excludeThisUser)
         {
            //filter.add(UserQuery.OID.isEqual(user.getOID()));
         }
      }
      else
      {
         query.where(UserQuery.OID.isEqual(0));
      }
      return query;
   }

   public User getUser()
   {
      if (this.currentUser == null)
      {
         if (this.getServiceFactory() != null)
         {
            this.currentUser = getWorkflowService().getUser();
         }
      }

      return this.currentUser;
   }
}