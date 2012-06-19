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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;


/**
 * A ModelTreeItem contains a ProcessProgressModel and ProcessProgressInstance object.
 * Every ModelTreeItem has a ProgressStatus object assigned, that is responsible to
 * provide the status needed when creating the graphical elements of the Gantt Diagram
 * View. Furthermore a ModelTreeItem reflects a hierarchy, hence it provides the set of
 * its children.
 * 
 * @author mueller1
 * 
 */
public class ModelTreeItem
{

   private static Logger logger = LogManager.getLogger(ModelTreeItem.class);

   private String name;

   private long startingPoint = 0;

   private ProcessProgressModel root;

   private ProcessProgressInstance instance;

   private SortedSet<ModelTreeItem> children;

   private ProgressStatus progressStatus;

   private boolean visible = false;

   private TimeUnit timeUnit;

   public ModelTreeItem(ProcessProgressModel root)
   {
      if (root != null)
      {
         logger.info("Create new ModelTreeItem element with id: " + root.getId());

         this.name = root.getName();
      }
      this.root = root;
      this.children = new TreeSet<ModelTreeItem>(new ModelTreeComparator());
   }

   public List<ModelTreeItem> getChildrenList()
   {
      return new ArrayList<ModelTreeItem>(children);
   }

   public SortedSet<ModelTreeItem> getChildren()
   {
      return children;
   }

   public void setChildren(SortedSet<ModelTreeItem> children)
   {
      this.children = children;
   }

   public void addChildren(SortedSet<ModelTreeItem> nodes)
   {
      this.children.addAll(nodes);
   }

   public void addChild(ModelTreeItem node)
   {
      this.children.add(node);
   }

   public ProcessProgressModel getRoot()
   {
      return root;
   }

   public void setRoot(ProcessProgressModel root)
   {
      this.root = root;
   }

   public ProcessProgressInstance getInstance()
   {
      return instance;
   }

   public void setInstance(ProcessProgressInstance instance)
   {
      this.instance = instance;
   }

   public ProgressStatus getProgressStatus()
   {
      return this.progressStatus;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setStartingPoint(long startingPoint, ModelTreeItem root)
   {
      this.startingPoint = startingPoint;
      this.initializeProgressStatus(getPredecessorItem(root));

      for (ModelTreeItem node : children)
      {
         node.updateTimeUnit(root.getTimeUnit());
         node.setStartingPoint(this.startingPoint, root);
      }
   }

   public long getMinStartTime(long currentMinStartTime)
   {
      long minStartTime = this.instance != null
            ? this.instance.getStartTime().getTime()
            : Long.MAX_VALUE;

      for (ModelTreeItem item : children)
      {
         minStartTime = item.getMinStartTime(minStartTime);
      }
      return Math.min(currentMinStartTime, minStartTime);
   }

   public long getMinPlannedStartTime(long currentMinPlannedStartTime)
   {

      long minPlannedStartTime = this.root != null
            && this.root.getPlannedStartTime() != null ? this.root.getPlannedStartTime()
            .getTime() : Long.MAX_VALUE;

      for (ModelTreeItem item : children)
      {
         minPlannedStartTime = item.getMinPlannedStartTime(minPlannedStartTime);
      }

      return Math.min(currentMinPlannedStartTime, minPlannedStartTime);
   }

   public void initializeProgressStatus(ModelTreeItem predecessor)
   {
      this.progressStatus = new ProgressStatus(this.root, this.instance,
            this.startingPoint, predecessor, timeUnit);
   }

   public boolean isVisible()
   {
      return visible;
   }

   /**
    * Removes all children having no completed process.
    * 
    * @return true if this item has a completed process, false if not
    */
   public boolean filterCompletedProcesses()
   {
      return filterProcesses(ProcessInstanceState.Active);
   }

   public boolean filterActiveProcesses()
   {
      return filterProcesses(ProcessInstanceState.Completed);
   }
   
   private boolean filterProcesses(ProcessInstanceState state)
   {
      boolean filtered = (instance != null && instance.getProcessInstance() != null)
            ? !state.equals(instance.getProcessInstance().getState())
            : false;

      Set<ModelTreeItem> removedItems = new HashSet<ModelTreeItem>();

      for (ModelTreeItem child : children)
      {
         if (child.filterProcesses(state))
         {
            filtered = true;
         }
         else
         {
            removedItems.add(child);
         }
      }

      children.removeAll(removedItems);

      return filtered;
   }

   public void setVisible(Boolean visible)
   {
      this.visible = visible != null ? visible.booleanValue() : false;
   }

   public void initializeVisibilityState(Map visibilityMap, Boolean visible)
   {
      for (ModelTreeItem item : children)
      {
         String key = item.getRoot().getNameAsKey();
         visibilityMap.put(key, visible);
      }
   }

   public List getAllChildren(List children)
   {
      for (ModelTreeItem item : this.children)
      {
         children = item.getAllChildren(children);
      }
      children.add(this);
      return children;
   }

   private ModelTreeItem getPredecessorItem(ModelTreeItem root)
   {
      if (root.getRoot().getId().equals(this.getRoot().getPredecessorId()))
      {
         return root;
      }
      else
      {
         for (ModelTreeItem child : root.children)
         {
            return getPredecessorItem(child);
         }
      }

      return null;
   }

   public void updateTimeUnit(TimeUnit timeUnit)
   {
      this.timeUnit = timeUnit;
      if (progressStatus != null)
      {
         progressStatus.setTimeUnit(timeUnit);
         progressStatus.calculateDiagramBars();
      }
   }

   public TimeUnit getTimeUnit()
   {
      return timeUnit;
   }

}
