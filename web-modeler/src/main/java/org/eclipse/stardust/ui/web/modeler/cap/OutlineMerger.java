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
package org.eclipse.stardust.ui.web.modeler.cap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ConditionalPerformerType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;

// copies the elements from the copyset to the outline
public class OutlineMerger extends AbstractMerger
{



   // init with targetModel and the elements to be pasted, storage contains important objects
   public OutlineMerger(ModelType targetModel, List input, StoreObject storage)
   {
      super(targetModel, storage);
      MergerUtil.setStorage(storage);

      for (int i = 0; i < input.size(); i++)
      {
         EObject child = (EObject) input.get(i);
         // get all children if is a model diagram
         // we copy a model diagram - it may contain a process
         if(child instanceof DiagramType && child.eContainer() instanceof ModelType)
         {
            // if it is the same model, we use the same objects
            if(!isSameModel)
            {
               // get objects from diagram, a model diagram has no activities, but processes
               MergerUtil.getObjectsFromDiagram(child, elements, globalElements, processes, activities, copier, isSameModel);
            }
            // MergerUtil.getGlobalObjectsFromModelDiagram(child, elements, diagramProcesses);
            diagrams.put((DiagramType) child, (DiagramType) copier.copy(child));
         }
         else if(child instanceof ProcessDefinitionType)
         {
            processes.put((ProcessDefinitionType) child, (ProcessDefinitionType) copier.copy(child));
         }
         else if(child instanceof ActivityType
               || child instanceof TriggerType)
         {
            processChildren.put(child, copier.copy(child));
            if(!isSameModel)
            {
               MergerUtil.checkAuthorizations(child, globalElements, copier);
            }
         }
         else
         {
            // other elements selected to be copied
            if(!elements.containsKey(child))
            {
               elements.put(child, copier.copy(child));
            }
            if(!isSameModel)
            {
               MergerUtil.checkAuthorizations(child, globalElements, copier);
               if(child instanceof ConditionalPerformerType)
               {
                  DataType dataType = ((ConditionalPerformerType) child).getData();
                  if(dataType != null)
                  {
                     if(!globalElements.containsKey(dataType))
                     {
                        globalElements.put(dataType, copier.copy(dataType));
                        MergerUtil.checkAuthorizations(dataType, globalElements, copier);
                     }
                  }
                  String dataId = AttributeUtil.getAttributeValue((IExtensibleElement) child, PredefinedConstants.CONDITIONAL_PERFORMER_REALM_DATA);
                  if(!StringUtils.isEmpty(dataId))
                  {
                     if(dataType == null || !dataType.getId().equals(dataId))
                     {
                        DataType realmData = (DataType) ModelUtils.findElementById(storage.getOriginalModelCopy().getData(), dataId);
                        if(realmData != null && !globalElements.containsKey(realmData))
                        {
                           globalElements.put(realmData, copier.copy(realmData));
                           MergerUtil.checkAuthorizations(realmData, globalElements, copier);
                        }
                     }
                  }
               }
            }
         }
      }
      collectProcessContent();
      // only if not same model
      if(!isSameModel)
      {
         checkForDialog(targetModel);
      }
      collectElements();
      // must be called after collectElements()
      copier.copyReferences();
   }

   public void merge()
   {
      if(!mergeGlobal())
      {
         return;
      }
      // seems that copy process already copies children like trigger, activities
      Iterator it = processElements.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         if(!mergeElement(entry))
         {
            modelChanged = false;
            return;
         }
      }
      it = processElements.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         addModelElement(entry, false);
      }

      if(!processes.isEmpty() && !isSameModel)
      {
         fixTransitions();
      }

      mergeConfigurationVariables();
   }

   // seems to be a bug, that transitions are not connected to activities
   // so we must fix references (because of a validation bug)
   private void fixTransitions()
   {
      Iterator it = processes.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         ProcessDefinitionType copy = (ProcessDefinitionType) entry.getValue();
         EList transitions = copy.getTransition();
         Iterator tr = transitions.iterator();
         while (tr.hasNext())
         {
            TransitionType transition = (TransitionType) tr.next();
            // the old activity
            ActivityType fromActivity = transition.getFrom();
            if(fromActivity == null)
            {
               continue;
            }
            // id has not changed since we copied the whole process, the new activity
            ActivityType targetFromActivity = (ActivityType) ModelUtils.findElementById(copy.getActivity(), fromActivity.getId());
            EList outTransitions = targetFromActivity.getOutTransitions();
            if(!outTransitions.contains(transition))
            {
               outTransitions.add(transition);
            }
            ActivityType toActivity = transition.getTo();
            ActivityType targetToActivity = (ActivityType) ModelUtils.findElementById(copy.getActivity(), toActivity.getId());
            EList inTransitions = targetToActivity.getInTransitions();
            if(!inTransitions.contains(transition))
            {
               inTransitions.add(transition);
            }
         }
      }
   }

}