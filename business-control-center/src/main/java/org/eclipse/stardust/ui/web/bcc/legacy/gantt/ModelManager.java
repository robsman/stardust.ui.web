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
package org.eclipse.stardust.ui.web.bcc.legacy.gantt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ActivityDetails;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;



/**
 * The Model Managers builds the process tree of the Gantt Diagram and assigns the process
 * instances to corresponding model object.
 * 
 * @author mueller1
 * 
 */
public class ModelManager
{

   public static final byte[] EMPTY_BUFFER = new byte[0];

   private Logger logger = LogManager.getLogger(ModelManager.class);

   private Collection<DeployedModel> models;

   private final int processState;

   public ModelManager(int processState)
   {
      this.processState = processState;
      initializeProcessModel();
   }

   /**
    * Method retrieves the process hierarchy including the model and instance part.
    * 
    * @param instanceManager
    * @return
    */
   public ModelTreeItem retrieveProcessHierarchy(InstanceManager instanceManager)
   {
      ProcessDefinition pDefType = getProcessDefinition(instanceManager.getProcessDefinitionId());// instanceManager.getProcessDefinition();

      ModelTreeItem root = pDefType != null ? retrieveSubprocesses(pDefType, instanceManager.getInstanceMap(),
            pDefType.getQualifiedId(), instanceManager.getReferenceDate(), new HashMap()) : new ModelTreeItem(null);

      if (ProcessProgressInstance.PROCESS_STATE_COMPLETED == processState)
      {
         root.filterCompletedProcesses();
      }
      else if (ProcessProgressInstance.PROCESS_STATE_ACTIVE == processState)
      {
         root.filterActiveProcesses();
      }
      else if (ProcessProgressInstance.PROCESS_STATE_NONE == processState)
      {
         root.filterCompletedProcesses();
         root.filterActiveProcesses();
      }

      return root;
   }

   /**
    * Method retrieves an item of the process tree in a recursive way.
    * 
    * @param pDefType
    * @param pInstances
    * @param businessKey
    * @param referenceDate
    * @param dependencyMap
    * @return
    */
   private ModelTreeItem retrieveSubprocesses(ProcessDefinition pDefType, Map pInstances, String businessKey,
         Date referenceDate, Map dependencyMap)
   {

      // creates a tree node item
      ModelTreeItem node = this.createModelTreeItem(pDefType, referenceDate, businessKey, pInstances);

      if (node != null)
      {
         List activities = pDefType.getAllActivities();

         // iteration over all activities to find subprocess definitions
         for (Iterator _iterator = activities.iterator(); _iterator.hasNext();)
         {

            Activity activity = (Activity) _iterator.next();
            String implementationProcessDefinitionId = activity instanceof ActivityDetails
                  ? ((ActivityDetails) activity).getImplementationProcessDefinitionId()
                  : null;
            if (ImplementationType.SubProcess.equals(activity.getImplementationType())
                  && !StringUtils.isEmpty(implementationProcessDefinitionId))
            {
               ProcessDefinition processDefinition = getProcessDefinition(implementationProcessDefinitionId);// TODO:Fix
                                                                                                             // this
                                                                                                             // with
                                                                                                             // FQID

               if (processDefinition != null)
               {
                  logger.info("Found subprocess activitiy: " + processDefinition.getQualifiedId());

                  // reads business keys defined for this process definition
                  List businessKeys = this.getInstanceDescriptorValues(processDefinition.getQualifiedId(),
                        processDefinition.getName());

                  // for every found business key a model element must be created
                  if (!businessKeys.isEmpty())
                  {
                     for (Iterator bkIterator = businessKeys.iterator(); bkIterator.hasNext();)
                     {

                        String bk = (String) bkIterator.next();

                        if (node.getRoot().getBusinessValue() == null || node.getRoot().getBusinessValue().equals(bk))
                        {

                           this.addModelTreeItem(processDefinition, pInstances, bk, referenceDate, dependencyMap, node);
                        }
                     }
                  }
                  else
                  {
                     this.addModelTreeItem(processDefinition, pInstances, processDefinition.getQualifiedId(),
                           referenceDate, dependencyMap, node);
                  }
               }
            }
         }
      }

      return node;

   }

   private ModelTreeItem createModelTreeItem(ProcessDefinition defType, Date referenceDate, String businessKey,
         Map instances)
   {
      ModelTreeItem node = null;

      // creates a new model object
      ProcessProgressModel model = new ProcessProgressModel(defType, referenceDate, businessKey);

      logger.info("Lookup instance with key: " + model.getBusinessId());

      // looks up an instance that has to be bound to a model
      ProcessProgressInstance instance = (ProcessProgressInstance) instances.get(model.getBusinessId());

      // creates a model tree item containing both, the model and instance
      node = new ModelTreeItem(model);

      if (instance != null)
      {
         logger.info("Found instance with key: " + model.getBusinessId());

         List dataPaths = defType.getAllDataPaths();
         instance.setDescriptorKeys(dataPaths);

         // binds the instance to the model
         node.setInstance(instance);
         instances.remove(model.getBusinessId());
         // }
      }

      return node;
   }

   private void addModelTreeItem(ProcessDefinition pDefType, Map pInstances, String businessKey, Date referenceDate,
         Map dependencyMap, ModelTreeItem node)
   {

      // recursive call to retrieve all subprocess elements
      ModelTreeItem childNode = retrieveSubprocesses(pDefType, pInstances, businessKey, referenceDate, dependencyMap);

      if (childNode != null)
      {

         // only consider process definition not defined as ignorable
         if (!childNode.getRoot().isIgnorable())
         {
            node.addChild(childNode);

         }
         else
         {

            SortedSet children = childNode.getChildren();
            node.addChildren(children);

            logger.info("Ignore process definition with id : " + pDefType.getId());
         }
      }
   }

   private ProcessDefinition getProcessDefinition(String processId)
   {
      String modelId = ModelUtils.extractModelId(processId);
      if (modelId != null)
      {
         for (DeployedModel model : ModelUtils.getAllModels())
         {
            if (model.getId().equals(modelId))
            {
               for (Object pd : model.getAllProcessDefinitions())
               {
                  ProcessDefinition processDefinition = (ProcessDefinition) pd;
                  if (processDefinition.getQualifiedId().equals(processId))
                  {
                     return processDefinition;
                  }
               }
            }
         }
      }
      else
      {

         for (DeployedModel model : ModelUtils.getAllModels())
         {
            ProcessDefinition processDefinition = model.getProcessDefinition(processId);
            if (processDefinition != null)
            {
               return processDefinition;
            }
         }
      }

      return null;
   }

   /**
    * Method loads the EMF object model.
    */
   private void initializeProcessModel()
   {

      try
      {
         models = ModelCache.findModelCache().getAllModels();
      }
      catch (Exception e)
      {
         throw new RuntimeException(MessagesBCCBean.getInstance().getString("messages.common.cannotLoadProcessModel")
               + e.getMessage());
      }
   }

   private List<String> getInstanceDescriptorValues(String processDefinitionId, String processDefinitionName)
   {

      List<String> descriptorValues = new ArrayList<String>();

      StringBuffer key = new StringBuffer();
      key.append(processDefinitionId);
      key.append(PropertyProvider.PROPERTY_KEY_SEPARATOR);
      key.append(PropertyProvider.INSTANCE_DESCRIPTOR_VALUES);

      String values = PropertyProvider.getInstance().getProperty(key.toString());

      if (values != null)
      {
         StringTokenizer strTok = new StringTokenizer(values, ",");
         while (strTok.hasMoreElements())
         {
            String str = (String) strTok.nextElement();
            descriptorValues.add(str.trim());
         }
      }

      return descriptorValues;
   }

}
