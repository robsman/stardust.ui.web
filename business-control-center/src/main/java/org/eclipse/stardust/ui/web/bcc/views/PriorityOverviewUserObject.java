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
package org.eclipse.stardust.ui.web.bcc.views;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.bcc.jsf.ActivityDefinitionWithPrio;
import org.eclipse.stardust.ui.web.bcc.jsf.ModelWithPrio;
import org.eclipse.stardust.ui.web.bcc.jsf.PriorityOverviewEntry;
import org.eclipse.stardust.ui.web.bcc.jsf.ProcessDefinitionWithPrio;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class PriorityOverviewUserObject extends NodeUserObject
{
   private static final long serialVersionUID = 1L;

   private PriorityOverviewEntry prioOverviewEntry;

   /**
    * @param treeTable
    * @param node
    * @param treeBeanPointer
    * @param componenttype
    * @param modelWithPrio
    */
   public PriorityOverviewUserObject(TreeTable treeTable, TreeTableNode node,
         TreeTableBean treeBeanPointer, int componenttype,
         PriorityOverviewEntry prioOverviewEntry)
   {
      super(treeTable, node, treeBeanPointer, componenttype);
      this.prioOverviewEntry = prioOverviewEntry;
      String imagePath = "/plugins/views-common/images/icons/page_white.png";
      String tooltip = null;
      if (prioOverviewEntry instanceof ModelWithPrio)
      {
         imagePath = "/plugins/views-common/images/icons/model.gif";
         tooltip = ((ModelWithPrio) prioOverviewEntry).getDescription();
      }
      else if (prioOverviewEntry instanceof ProcessDefinitionWithPrio)
      {
         if (isAuxiliaryProcess())
         {
            String processType = "AuxiliaryProcess";
            imagePath = ActivityInstanceUtils.getIconPath(processType);
         }
         else
         {
            imagePath = "/plugins/views-common/images/icons/process.png";
         }
         ProcessDefinition pd = ((ProcessDefinitionWithPrio) prioOverviewEntry).getProcessDefinition();
         tooltip = I18nUtils.getDescriptionAsHtml(pd, pd.getDescription());
      }
      else if (prioOverviewEntry instanceof ActivityDefinitionWithPrio)
      {
         Activity ai = ((ActivityDefinitionWithPrio) prioOverviewEntry).getActivity();
         ai = ((ActivityDefinitionWithPrio) prioOverviewEntry).getProcessDefinition().getActivity(ai.getId());
         imagePath = ActivityInstanceUtils.getIconPath(ActivityInstanceUtils.getActivityType(ai, false));
         tooltip = I18nUtils.getDescriptionAsHtml(ai, ai.getDescription());
      }
      setTooltip(tooltip);
      setLeafIcon(imagePath);
      setBranchContractedIcon(imagePath);
      setBranchExpandedIcon(imagePath);
      setExpanded(false);
   }

   @Override
   public String getLine1Text()
   {
      return getName();
   }

   @Override
   public String getLine2Text()
   {
      return null;
   }

   public String getFilterType()
   {
      return "";
   }

   public String getName()
   {
      return prioOverviewEntry.getName();
   }

   public String getTitle()
   {
      if (prioOverviewEntry instanceof ActivityDefinitionWithPrio)
      {
         return MessagesBCCBean.getInstance().getString("views.processOverviewView.tooltip.activityTable");
      }
      else
      {
         return MessagesBCCBean.getInstance().getString("views.processOverviewView.tooltip.processTable");
      }
   }
   
   /**
    * @return
    */
   public Boolean isAuxiliaryProcess()
   {
      ITableDataFilterOnOff tableDataFilter = (ITableDataFilterOnOff) treeTable.getDataFilters().getDataFilter(
            "auxiliaryProcess");
      ITableDataFilterOnOff onOffFilter = ((ITableDataFilterOnOff) tableDataFilter);
      if (onOffFilter.isOn())
      {
         if (onOffFilter.getName().equals(prioOverviewEntry.getType()))
         {
            return true;
         }
      }
      return false;
   }
   
   public int getThresholdState()
   {
      return prioOverviewEntry.getThresholdState();
   }

   public String getThresholdStateLabel()
   {
      String ret = "";
      switch(getThresholdState())
      {
      case 1:
         ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.critical");
         break;
      case 2:
         ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.warning");
         break;
      case 3:
         ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.normal");
         break;
      }
      return ret;
   }

   public long getNormalPriority()
   {
      return prioOverviewEntry.getPriorities().getNormalPriority();
   }

   public long getLowPriority()
   {
      return prioOverviewEntry.getPriorities().getLowPriority();
   }

   public long getHighPriority()
   {
      return prioOverviewEntry.getPriorities().getHighPriority();
   }

   public long getTotalPriority()
   {
      return prioOverviewEntry.getPriorities().getTotalPriority();
   }

   public long getCriticalNormalPriority()
   {
      return prioOverviewEntry.getCriticalPriorities().getNormalPriority();
   }

   public long getCriticalLowPriority()
   {
      return prioOverviewEntry.getCriticalPriorities().getLowPriority();
   }

   public long getCriticalHighPriority()
   {
      return prioOverviewEntry.getCriticalPriorities().getHighPriority();
   }

   public long getCriticalTotalPriority()
   {
      return prioOverviewEntry.getCriticalPriorities().getTotalPriority();
   }

   public String getDefaultPerformerName()
   {
      return prioOverviewEntry.getDefaultPerformerName();
   }

   public void doPriorityAction(ActionEvent event)
   {
      prioOverviewEntry.doPriorityAction(event);
   }

   @Override
   public boolean isFilterOut(TableDataFilters dataFilters)
   {
      return false;
   }
}
