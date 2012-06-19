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
package org.eclipse.stardust.ui.web.bcc.views.criticalityManager;

import java.util.Map;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.ui.web.bcc.views.criticalityManager.ICriticalityMgrTableEntry.CriticalityDetails;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.QualityAssuranceUtils;


/**
 * @author Shrikant.Gangal
 * 
 */
public class CriticalityOverviewNodeObject extends NodeUserObject
{
   private static final long serialVersionUID = 1L;

   private ICriticalityMgrTableEntry criticalityOverviewEntry;

   /**
    * @param treeTable
    * @param node
    * @param treeBeanPointer
    * @param componenttype
    * @param modelWithPrioy
    */
   public CriticalityOverviewNodeObject(TreeTable treeTable, TreeTableNode node, TreeTableBean treeBeanPointer,
         int componenttype, ICriticalityMgrTableEntry criticalityOverviewEntry)
   {
      super(treeTable, node, treeBeanPointer, componenttype);
      String tooltip = null;
      this.criticalityOverviewEntry = criticalityOverviewEntry;
      String imagePath = "/plugins/common/images/icons/file.gif";

      if (criticalityOverviewEntry instanceof ModelDefCriticalityMgrTableEntry)
      {
         imagePath = "/plugins/admin-portal/images/icons/model.gif";
         tooltip = ((ModelDefCriticalityMgrTableEntry) criticalityOverviewEntry).getDescription();
         
      }
      else if (criticalityOverviewEntry instanceof ProcessDefCriticalityMgrTableEntry)
      {
         if (isAuxiliaryProcess())
         {
            String processType = "AuxiliaryProcess";
            imagePath = ActivityInstanceUtils.getIconPath(processType);
         }
         else
         {
            imagePath = "/plugins/common/images/icons/process-history/active/process.png";
         }
         tooltip = ((ProcessDefCriticalityMgrTableEntry) criticalityOverviewEntry).getDescription();
      }
      else if (criticalityOverviewEntry instanceof ActivityDefCriticalityMgrTableEntry)
      {
         Activity activity = ((ActivityDefCriticalityMgrTableEntry) criticalityOverviewEntry).getActivity();
         imagePath = ActivityInstanceUtils.getIconPath(ActivityInstanceUtils.getActivityType(activity, false));
         tooltip = I18nUtils.getDescriptionAsHtml(activity, activity.getDescription());
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
      return criticalityOverviewEntry.getName();
   }

   /**
    * @return
    */
   public Boolean isAuxiliaryProcess()
   {
      // TODO - review
      ITableDataFilterOnOff tableDataFilter = (ITableDataFilterOnOff) treeTable.getDataFilters().getDataFilter(
            "auxiliaryProcess");
      ITableDataFilterOnOff onOffFilter = ((ITableDataFilterOnOff) tableDataFilter);
      if (onOffFilter.isOn())
      {
         if (onOffFilter.getName().equals(criticalityOverviewEntry.getType()))
         {
            return true;
         }
      }
      return false;
   }

   public int getThresholdState()
   {
      // TODO - add real code
      // return criticalityOverviewEntry.getThresholdState();

      return 3;
   }

   public String getThresholdStateLabel()
   {
      // TODO
      String ret = "";
      // switch(getThresholdState())
      // {
      // case 1:
      // ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.critical");
      // break;
      // case 2:
      // ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.warning");
      // break;
      // case 3:
      // ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.normal");
      // break;
      // }
      return ret;
   }

   public Map<String, CriticalityDetails> getCriticalityDetailsMap()
   {
      return criticalityOverviewEntry.getCriticalityDetailsMap();
   }

   public String getDefaultPerformerName()
   {
      return criticalityOverviewEntry.getDefaultPerformerName();
   }

   public void doCriticalityAction(ActionEvent event)
   {
      criticalityOverviewEntry.doCriticalityAction(event);
   }

   public void initialize()
   {
      criticalityOverviewEntry.initialize();
   }

   @Override
   public boolean isFilterOut(TableDataFilters dataFilters)
   {
      return false;
   }
}
