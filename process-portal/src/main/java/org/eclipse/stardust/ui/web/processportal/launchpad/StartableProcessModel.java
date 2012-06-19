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

import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.processportal.common.PPUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;




/**
 * @author roland.stamm
 * 
 */
public class StartableProcessModel
{

   private ProcessDefinition processDefinition;

   private DefaultTreeModel model = null;

   private Set<Department> departmentList = null;

   /**
    * @param processDefinition
    */
   public StartableProcessModel(ProcessDefinition processDefinition,
         DefaultTreeModel model, Set<Department> departmentList)
   {
      super();
      this.processDefinition = processDefinition;
      this.model = model;
      this.departmentList = departmentList;
   }

   /**
    * @return
    */
   public String selectAction()
   {
      try
      {
         select();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

      return null;
   }

   /**
    * 
    */
   private void select()
   {
      if (model == null && departmentList != null && !departmentList.isEmpty()
            && departmentList.size() == 1)
      {
         for (Department d : departmentList)
         {
            addDepartmentData(d);
         }
      }
      else if (model != null)
      {
         DepartmentDialogBean deptBean = DepartmentDialogBean.getCurrent();
         deptBean.setModel(model);
         deptBean.openPopup();
      }
      else if (model == null)
      {
         // TODO support asynchronous/synchronous start selection?
         ActivityInstance nextActivityInstance = PPUtils.activateNextActivityInstance(PPUtils.startProcess(
               processDefinition, true));
         if (nextActivityInstance != null)
         {

            Map<String, Object> params = CollectionUtils.newTreeMap();
            if (WorklistsBean.getInstance().isAssemblyLineActivity(nextActivityInstance.getActivity()))
            {
               params.put("assemblyLineActivity", true);
               params.put("worklistsBean", WorklistsBean.getInstance());
            }         
            ActivityInstanceUtils.openActivity(nextActivityInstance, params);
         }
         else
         {
            MessageDialog.addInfoMessage(MessagePropertiesBean
                        .getInstance().getParamString("common.processStarted.message", new String[] {getName()}));
         }
      }
      
   }

   private void addDepartmentData(Department department)
   {
      Map<String, Object> map = null;
      if (department != null)
      {
         map = CollectionUtils.newMap();
         Department department1 = department;
         do
         {
            if (department1 == null)
            {
               break;
            }
            Organization organization = department1.getOrganization();
            if (organization != null)
            {
               String s = (String) organization.getAttribute("carnot:engine:dataId");
               if (s != null)
               {
                  ModelCache modelCache = ModelCache.findModelCache();
                  Model model = modelCache.getModel(organization.getModelOID());

                  Data data = model.getData(s);
                  String s1 = data.getTypeId();
                  if ("primitive".equals(s1))
                  {
                     map.put(s, department1.getId());
                  }
                  else if ("struct".equals(s1))
                  {
                     Object obj = map.get(s);
                     if (!(obj instanceof Map))
                     {
                        obj = CollectionUtils.newMap();
                        map.put(s, obj);
                     }
                     Map map1 = (Map) obj;
                     String s2 = (String) organization
                           .getAttribute("carnot:engine:dataPath");
                     boolean flag = false;
                     do
                     {
                        int i;
                        if (0 >= (i = s2.indexOf('/')))
                        {
                           break;
                        }
                        String s3 = s2.substring(0, i).trim();
                        s2 = s2.substring(i + 1);
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
                     s2 = s2.trim();
                     if (s2.length() > 0)
                     {
                        map1.put(s2, department1.getId());
                     }
                  }
                  else
                  {
                     throw new PublicException((new StringBuilder()).append(
                           "Unsupported data type in manual triggers: ").append(s1)
                           .toString());
                  }
                  department1 = department1.getParentDepartment();
               }
            }
         }
         while (true);
      }

      ActivityInstance nextActivityInstance = PPUtils
            .activateNextActivityInstance(PPUtils.startProcess(getProcessDefinition(),
                  map, true));

      if (nextActivityInstance != null)
      {
         ActivityInstanceUtils.openActivity(nextActivityInstance);
      }
      else
      {
         MessageDialog.addInfoMessage(MessagePropertiesBean
                     .getInstance().getParamString("common.processStarted.message", new String[] {getName()}));
      }

   }

   public ProcessDefinition getProcessDefinition()
   {
      return processDefinition;
   }

   public String getName()
   {
      return I18nUtils.getProcessName(processDefinition);
   }
   
   public String getTitle()
   {
      return I18nUtils.getDescriptionAsHtml(processDefinition, processDefinition.getDescription());
   }

}
