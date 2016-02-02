package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.Trigger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.ProcessDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.dto.StartableProcessDTO;
import org.eclipse.stardust.ui.web.rest.dto.request.DepartmentDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.ParticipantDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.ClientContextBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpecialWorklistCacheManager;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Component
public class StartableProcessService
{

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   /**
    * This method will return the list of all startable processes.
    * 
    * @return
    */
   public List<StartableProcessDTO> getStartableProcess()
   {
      List<StartableProcessDTO> items = new ArrayList<StartableProcessDTO>();
      User user = serviceFactoryUtils.getSessionContext().getUser();
      ProcessDefinition processDefinition;
      List<ProcessDefinition> startableProcesses = ProcessDefinitionUtils.getStartableProcesses();
      for (Iterator<ProcessDefinition> iterator = startableProcesses.iterator(); iterator.hasNext();)
      {
         List<ParticipantDTO> participantNodes = null;
         processDefinition = iterator.next();
         List<Trigger> triggers = processDefinition.getAllTriggers();
         Map<ModelParticipant, Set<Department>> mapData = new HashMap<ModelParticipant, Set<Department>>();
         ModelCache modelCache = ModelCache.findModelCache();
         Model currentModel = modelCache.getModel(processDefinition.getModelOID());
         Set<DepartmentDTO> deptList = new HashSet<DepartmentDTO>();
         for (Trigger triggerDetails : triggers)
         {
            if (PredefinedConstants.MANUAL_TRIGGER.equals(triggerDetails.getType()))
            {
               String s = (String) triggerDetails.getAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT);
               if (s != null)
               {
                  ModelParticipant modelparticipant = (ModelParticipant) currentModel.getParticipant(s);
                  if (isDepartmentScoped(modelparticipant))
                  {
                     Model scopedModel = currentModel;
                     for (Grant grant : user.getAllGrants())
                     {
                        if (!PredefinedConstants.ADMINISTRATOR_ROLE.equals(grant.getQualifiedId())
                              && !CompareHelper.areEqual(grant.getNamespace(), currentModel.getId()))
                        {
                           scopedModel = ModelCache.findModelCache().getActiveModel(grant);
                        }
                        if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(grant.getQualifiedId())
                              || CompareHelper.areEqual(grant.getNamespace(), scopedModel.getId()))
                        {
                           ModelParticipant modelparticipant1 = (ModelParticipant) scopedModel.getParticipant(grant
                                 .getId());
                           if (isAuthorized(modelparticipant, modelparticipant1))
                           {
                              if (mapData.get(modelparticipant1) == null)
                              {
                                 mapData.put(modelparticipant1, new HashSet<Department>());
                              }

                              if (grant.getDepartment() != null)
                              {
                                 if (!grant.getDepartment().getName().equals(Department.DEFAULT.getName()))
                                 {
                                    mapData.get(modelparticipant1).add(grant.getDepartment());

                                    DepartmentDTO department = new DepartmentDTO();
                                    department.id = grant.getDepartment().getId();
                                    department.name = grant.getDepartment().getName();
                                    department.description = grant.getDepartment().getDescription();
                                    deptList.add(department);
                                 }
                              }
                           }
                        }
                     }
                     participantNodes = buildDepartmentTree(mapData, processDefinition);
                  }
               }

               break;
            }
         }

         ProcessDefinitionDTO processDefinitionDTO = new ProcessDefinitionDTO();
         processDefinitionDTO.id = processDefinition.getQualifiedId();
         processDefinitionDTO.name = processDefinition.getName();
         processDefinitionDTO.modelOid = processDefinition.getModelOID();

         StartableProcessDTO startableProcessDTO = new StartableProcessDTO();
         startableProcessDTO.name = I18nUtils.getProcessName(processDefinition);
         startableProcessDTO.processDefinition = processDefinitionDTO;
         startableProcessDTO.participantNodes = participantNodes;
         startableProcessDTO.deptList = deptList;
         items.add(startableProcessDTO);
      }

      Collections.sort(items, new Comparator<StartableProcessDTO>()
      {
         public int compare(StartableProcessDTO arg0, StartableProcessDTO arg1)
         {
            if (arg0 != null && arg1 != null && arg0.name != null)
               return arg0.name.compareTo(arg1.name);

            return 0;
         }
      });

      return items;

   }

   /**
    * This method will start the normal process
    * 
    * @param processId
    * @return
    */
   public JsonObject startProcess(String processId)
   {
      ActivityInstance nextActivityInstance = null;
      JsonObject result = new JsonObject();
      ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(processId);
      ProcessInstance processInstance = startProcess(processDefinition, true);

      if (!(ProcessInstanceUtils.isTransientProcess(processInstance) || ProcessInstanceUtils
            .isCompletedProcess(processInstance)))
      {
         nextActivityInstance = activateNextActivityInstance(processInstance);

         if (nextActivityInstance != null)
         {
            /*
             * if
             * (WorklistsBean.getInstance().isAssemblyLineActivity(nextActivityInstance.
             * getActivity())) {
             */
            result.addProperty("assemblyLineActivity", true);
            /* } */
            result.addProperty("activityInstanceOid", nextActivityInstance.getOID());
         }
      }
      if (nextActivityInstance == null)
      {
         result.addProperty("processStarted", true);

      }
      return result;
   }

   /**
    * @param pi
    * @return
    */
   public ActivityInstance activateNextActivityInstance(ProcessInstance pi)
   {
      ActivityInstance rtAi = serviceFactoryUtils.getWorkflowService().activateNextActivityInstanceForProcessInstance(
            pi.getOID());
      if (rtAi != null)
      {
         sendActivityEvent(null, ActivityEvent.activated(rtAi));

         return rtAi;
      }
      return null;
   }

   /**
    * @param oldAi
    * @param activityEvent
    */
   public static void sendActivityEvent(ActivityInstance oldAi, ActivityEvent activityEvent)
   {
      ParticipantWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      if (ProcessWorklistCacheManager.isInitialized())
      {
         ProcessWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      }
      SpecialWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      ClientContextBean.getCurrentInstance().getClientContext().sendActivityEvent(activityEvent);
   }

   public ProcessInstance startProcess(ProcessDefinition processDefinition, boolean synchronous)
   {
      WorkflowService wfs = serviceFactoryUtils.getWorkflowService();
      return wfs.startProcess(processDefinition.getQualifiedId(), null, synchronous);
   }

   /**
    * Creates Department Tree
    * 
    * @param mapData
    * @param processDefinition
    * @param model
    */
   private List<ParticipantDTO> buildDepartmentTree(Map<ModelParticipant, Set<Department>> mapData,
         ProcessDefinition processDefinition)
   {
      Set<Department> deptList = new HashSet<Department>();
      for (ModelParticipant modelParticipant : mapData.keySet())
      {
         if (mapData.get(modelParticipant) != null)
         {

            for (Department d : mapData.get(modelParticipant))
            {
               if (d != null)
               {
                  deptList.add(d);
               }
            }
         }
      }

      List<ParticipantDTO> participantNodes = new ArrayList<ParticipantDTO>();

      boolean allowTree = deptList != null && !deptList.isEmpty() && deptList.size() > 1 ? true : false;
      if (allowTree)
      {
         for (ModelParticipant modelParticipant : mapData.keySet())
         {
            ParticipantDTO participantNode = createNode(I18nUtils.getParticipantName(modelParticipant));
            if (mapData.get(modelParticipant) != null)
            {

               for (Department d : mapData.get(modelParticipant))
               {
                  if (d != null)
                  {
                     createChildNode(participantNode, d.getName(), d, processDefinition);
                  }
               }
            }

            participantNodes.add(participantNode);
         }
      }

      return participantNodes;
   }

   /**
    * 
    * @param participantNode
    * @param name
    * @param d
    * @param processDefinition
    */
   private void createChildNode(ParticipantDTO participantNode, String name, Department d,
         ProcessDefinition processDefinition)
   {
      ParticipantDTO participantDTO = new ParticipantDTO();
      participantDTO.name = name;
      participantDTO.id = d.getId();
      participantDTO.description = d.getDescription();
      participantDTO.OID = d.getOID();
      participantNode.children.add(participantDTO);
   }

   /**
    * 
    * @param participantName
    * @return
    */
   private ParticipantDTO createNode(String participantName)
   {
      ParticipantDTO participantDTO = new ParticipantDTO();
      participantDTO.name = participantName;
      participantDTO.children = new ArrayList<ParticipantDTO>();
      return participantDTO;
   }

   /**
    * Returns whether modelParticipant is scoped
    * 
    * @param modelParticipant
    * @return
    */
   private boolean isDepartmentScoped(ModelParticipant modelParticipant)
   {
      if (null == modelParticipant)
      {
         return false;
      }
      if (modelParticipant.definesDepartmentScope())
      {
         return true;
      }
      List<Organization> list = modelParticipant.getAllSuperOrganizations();
      for (Organization organization : list)
      {
         if (isDepartmentScoped((ModelParticipant) organization))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * @param modelparticipant
    * @param modelparticipant1
    * @return
    */
   private boolean isAuthorized(ModelParticipant modelparticipant, ModelParticipant modelparticipant1)
   {
      if (null == modelparticipant1)
      {
         return false;
      }
      if (ParticipantUtils.areEqual(modelparticipant, modelparticipant1))
      {
         return true;
      }
      List<Organization> list = modelparticipant1.getAllSuperOrganizations();
      for (Iterator<Organization> iterator = list.iterator(); iterator.hasNext();)
      {
         Organization organization = iterator.next();
         if (isAuthorized(modelparticipant, ((ModelParticipant) (organization))))
         {
            return true;
         }
      }

      if (modelparticipant instanceof Role)
      {
         return isTeamLead(modelparticipant, modelparticipant1);
      }
      else
      {
         return false;
      }
   }

   /**
    * @param modelparticipant
    * @param modelparticipant1
    * @return
    */
   private boolean isTeamLead(ModelParticipant modelparticipant, ModelParticipant modelparticipant1)
   {
      if ((modelparticipant1 instanceof Organization)
            && modelparticipant == ((Organization) modelparticipant1).getTeamLead())
      {
         return true;
      }
      List<Organization> list = modelparticipant1.getAllSuperOrganizations();
      for (Iterator<Organization> iterator = list.iterator(); iterator.hasNext();)
      {
         Organization organization = iterator.next();
         if (isTeamLead(modelparticipant, ((ModelParticipant) (organization))))
         {
            return true;
         }
      }

      return false;
   }

   /**
    * 
    * @param oid
    * @param processId
    * @return
    */
   public ActivityInstance startProcessOnSelectDepartment(long oid, String processId)
   {

      Map processDataMap = null;
      Department department = serviceFactoryUtils.getAdministrationService().getDepartment(oid);
      ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(processId);
      if (department != null)
      {
         processDataMap = CollectionUtils.newMap();
         Department currentDept = department;
         while (currentDept != null)
         {
            Organization organization = currentDept.getOrganization();
            if (organization != null)
            {
               String dataId = (String) organization.getAttribute("carnot:engine:dataId");
               if (dataId != null)
               {
                  ModelCache modelCache = ModelCache.findModelCache();
                  Model model = modelCache.getModel(organization.getModelOID());

                  Data data = model.getData(dataId);
                  String typeId = data.getTypeId();
                  if ("primitive".equals(typeId))
                  {
                     processDataMap.put(dataId, currentDept.getId());
                  }
                  else if ("struct".equals(typeId))
                  {
                     extractDataForStructureType(processDataMap, currentDept, organization, dataId);
                  }
                  else
                  {
                     throw new PublicException((new StringBuilder())
                           .append("Unsupported data type in manual triggers: ").append(typeId).toString());
                  }
                  currentDept = currentDept.getParentDepartment();
               }
            }
         }
      }

      return activateNextActivityInstance(startProcess(processDefinition, processDataMap, true));

   }

   /**
    * @param processDefinition
    * @param synchronous
    * @return
    */
   public ProcessInstance startProcess(ProcessDefinition processDefinition, Map<String, ? > data, boolean synchronous)
   {
      WorkflowService wfs = serviceFactoryUtils.getWorkflowService();
      return wfs.startProcess(processDefinition.getQualifiedId(), data, synchronous);
   }

   /**
    * 
    * @param processDataMap
    * @param currentDept
    * @param organization
    * @param dataId
    */
   private void extractDataForStructureType(Map processDataMap, Department currentDept, Organization organization,
         String dataId)
   {
      Object obj = processDataMap.get(dataId);
      if (!(obj instanceof Map))
      {
         obj = CollectionUtils.newMap();
         processDataMap.put(dataId, obj);
      }
      Map map1 = (Map) obj;
      String dataPath = (String) organization.getAttribute("carnot:engine:dataPath");
      if (StringUtils.isEmpty(dataPath))
      {
         processDataMap.put(dataId, currentDept.getId());
      }
      else
      {
         do
         {
            int i;
            if (0 >= (i = dataPath.indexOf('/')))
            {
               break;
            }
            String s3 = dataPath.substring(0, i).trim();
            dataPath = dataPath.substring(i + 1);
            if (s3.length() > 0)
            {
               Map map2 = (Map) map1.get(s3);
               if (map2 == null)
               {
                  map2 = CollectionUtils.newMap();
                  map1.put(s3, map2);
               }
               map1 = map2;
            }
         }
         while (true);
         dataPath = dataPath.trim();
         if (dataPath.length() > 0)
         {
            map1.put(dataPath, currentDept.getId());
         }
      }
   }

}
