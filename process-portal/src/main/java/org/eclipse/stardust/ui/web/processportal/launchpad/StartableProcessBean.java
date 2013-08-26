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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.Trigger;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.springframework.beans.factory.InitializingBean;


import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.SlideDown;

/**
 * @author roland.stamm
 * 
 */
public class StartableProcessBean extends AbstractLaunchPanel implements InitializingBean
{

   private static final long serialVersionUID = -1301932649062878672L;

   private Effect effect = new SlideDown();;

   private List<StartableProcessModel> items;

   private DefaultTreeModel model = null;

   /**
    * @param name
    */
   public StartableProcessBean()
   {
      super("processes");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      items = CollectionUtils.newArrayList();
      getEffect().setFired(false);
      setExpanded(true);
      update();
   }

   /**
    * 
    */
   public void clear()
   {
      setExpanded(false);
      items.clear();
   }

   /**
    * 
    */
   public void update()
   {
      items.clear();
      User user = ServiceFactoryUtils.getSessionContext().getUser();
      ProcessDefinition processDefinition;
      List<ProcessDefinition> startableProcesses = ProcessDefinitionUtils.getStartableProcesses();
      for (Iterator<ProcessDefinition> iterator = startableProcesses.iterator(); iterator.hasNext();)
      {
         processDefinition = iterator.next();
         List<Trigger> triggers = processDefinition.getAllTriggers();
         Map<ModelParticipant, Set<Department>> mapData = new HashMap<ModelParticipant, Set<Department>>();
         ModelCache modelCache = ModelCache.findModelCache();
         Model currentModel = modelCache.getModel(processDefinition.getModelOID());
         Set<Department> deptList = new HashSet<Department>();
         model = null;
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
                     for (Grant grant : user.getAllGrants())
                     {
                        if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(grant.getQualifiedId())
                              || CompareHelper.areEqual(grant.getNamespace(), currentModel.getId()))
                        {
                           ModelParticipant modelparticipant1 = (ModelParticipant) currentModel.getParticipant(grant
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
                                    deptList.add(grant.getDepartment());
                                 }
                              }
                           }
                        }
                     }
                     buildDepartmentTree(mapData, processDefinition);
                  }
                  else
                  {
                     model = null;
                  }
               }

               break;
            }
         }
         items.add(new StartableProcessModel(processDefinition, model, deptList));
      }

      Collections.sort(items, new Comparator<StartableProcessModel>()
      {
         public int compare(StartableProcessModel arg0, StartableProcessModel arg1)
         {
            if (arg0 != null && arg1 != null && arg0.getName() != null)
               return arg0.getName().compareTo(arg1.getName());

            return 0;
         }
      });
   }

   /**
    * Returns whether modelParticipant is scoped
    * 
    * @param modelParticipant
    * @return
    */
   private boolean isDepartmentScoped(ModelParticipant modelParticipant)
   {
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
    * Creates Department Tree
    * 
    * @param mapData
    * @param processDefinition
    */
   private void buildDepartmentTree(Map<ModelParticipant, Set<Department>> mapData, ProcessDefinition processDefinition)
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

      boolean allowTree = deptList != null && !deptList.isEmpty() && deptList.size() > 1 ? true : false;
      if (allowTree)
      {
         DefaultMutableTreeNode rootNode = addNode(null, "", null, null);
         model = new DefaultTreeModel(rootNode);
         for (ModelParticipant modelParticipant : mapData.keySet())
         {
            DefaultMutableTreeNode participantNode = addNode(rootNode, I18nUtils.getParticipantName(modelParticipant),
                  null, processDefinition);
            if (mapData.get(modelParticipant) != null)
            {

               for (Department d : mapData.get(modelParticipant))
               {
                  if (d != null)
                  {
                     DefaultMutableTreeNode deptNode = addNode(participantNode, d.getName(), d, processDefinition);
                  }
               }
            }
         }
      }
      else
      {
         model = null;
      }
   }

   /**
    * Adds Nodes
    * 
    * @param parent
    * @param title
    * @param department
    * @param processDefinition
    * @return
    */
   private DefaultMutableTreeNode addNode(DefaultMutableTreeNode parent, String title, Department department,
         ProcessDefinition processDefinition)
   {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode();
      DepartmentUserObject deptObject = null;
      if (parent != null)
      {
         deptObject = new DepartmentUserObject(node, department, false);
      }
      else
      {
         deptObject = new DepartmentUserObject(node, department, true);
      }

      node.setUserObject(deptObject);
      deptObject.setDepartment(department);
      deptObject.setProcessDefinition(processDefinition);

      if (department != null)
      {
         if (title != null)
         {
            deptObject.setText(title);

         }
         else
         {
            deptObject.setText(department.getName());
         }
         deptObject.setLeaf(false);
         node.setAllowsChildren(true);
      }
      else
      {
         if (title != null)
         {
            deptObject.setText(title);
            deptObject.setLeaf(false);
            node.setAllowsChildren(true);
         }
      }

      // finally add the node to the parent.
      if (parent != null)
      {
         parent.add(node);
      }
      return node;
   }

   public List<StartableProcessModel> getItems()
   {
      return items;
   }

   public Effect getEffect()
   {
      return effect;
   }

   /**
    * @param modelparticipant
    * @param modelparticipant1
    * @return
    */
   private boolean isAuthorized(ModelParticipant modelparticipant, ModelParticipant modelparticipant1)
   {
      if(null == modelparticipant1)
      {
         return false;
      }
      if (modelparticipant == modelparticipant1)
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

}
