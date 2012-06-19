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
package org.eclipse.stardust.ui.web.viewscommon.process.history;

import java.util.Date;

import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.model.utils.ActivityReportUtils;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.viewscommon.common.activity.QualityAssuranceCodesBean;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.QualityAssuranceUtils;



/**
 * @author Vikas.Mishra
 * 
 */
public class ActivityTableEntryUserObject extends NodeUserObject
{
   private static final long serialVersionUID = -5487464160841823357L;
   private ActivityTreeTable activityTreeTable;
   private IProcessHistoryTableEntry tableEntry;
   private String fullDetail;
   private boolean abortActivity;
   private boolean activatable;
   private boolean delegable;
   private boolean refersToActivity;
   private CriticalityCategory criticality;
   private int criticalityValue;
   private String criticalityLabel;
   QualityAssuranceCodesBean qualityAssuranceCodesBean;

   /**
    * @param treeTable
    * @param node
    * @param treeBeanPointer
    * @param componenttype
    * @param tableEntry
    */
   public ActivityTableEntryUserObject(TreeTable treeTable, TreeTableNode node, ActivityTreeTable treeBeanPointer,
         Integer componenttype, IProcessHistoryTableEntry tableEntry)
   {
      super(treeTable, node, treeBeanPointer, componenttype);

      
      this.tableEntry = tableEntry;
      this.activityTreeTable = treeBeanPointer;

      if (tableEntry instanceof EventHistoryItem)
      {
         EventHistoryItem eht = (EventHistoryItem) tableEntry;

         if (eht != null)
         {
            this.fullDetail = eht.getFullDetails();
         }
      }

      this.refersToActivity = false;
      String formatType = formatType(tableEntry, false);
      
      if (tableEntry.getRuntimeObject() instanceof ActivityInstance)
      {
         this.refersToActivity = true;

         ActivityInstance ai = (ActivityInstance) tableEntry.getRuntimeObject();
         this.abortActivity = ActivityInstanceUtils.isAbortable(ai);

         this.activatable = (ActivityInstanceUtils.isActivatable(ai) && !ai.getProcessInstance()
               .isCaseProcessInstance());

         delegable = (ActivityInstanceUtils.isDelegable(ai) && !ai.getProcessInstance().isCaseProcessInstance());
         criticalityValue = CriticalityConfigurationUtil.getPortalCriticality(ai.getCriticality());
         criticality = CriticalityConfigurationHelper.getInstance().getCriticality(criticalityValue);
         
         //set Quality Assurance codes properties
         qualityAssuranceCodesBean = new QualityAssuranceCodesBean(ai);  
         qualityAssuranceCodesBean.initializeMenu();
         
         if (ai.getActivity().isQualityAssuranceEnabled())
         {
            formatType = QualityAssuranceUtils.getQAActivityInstanceType(formatType, ai);
         }
      }
      
      //set icons
      String icon = (formatType != null) ? (ActivityInstanceUtils.getIconPath(formatType)) : null;
      setLeafIcon(icon);
      setBranchContractedIcon(icon);
      setBranchExpandedIcon(icon);
      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();
      setTooltip(msgBean.getString(ProcessHistoryTable.MSG_PREFIX + formatType));
      
   }

   /**
    * @param type
    * @param strict
    * @return
    */
   public static String formatType(IProcessHistoryTableEntry tabEntry, boolean strict)
   {
      String type = tabEntry.getRuntimeObjectType();
      String orgType = type;

      if ("ActivityActive".equals(type) || "Resubmission".equals(type) || "ActivitySuspended".equals(type)
            || "ActivityInterrupted".equals(type) || "ActivityAborted".equals(type) || "AbortingActivity".equals(type))
      {
         type = "StateChange";
      }

      if ("RouteActivity".equals(type) || "SubprocessActivity".equals(type))
      {
         type = "Auxiliary";
      }

      if (tabEntry.getRuntimeObject() instanceof ActivityInstance)
      {
         ActivityInstance ai = (ActivityInstance) tabEntry.getRuntimeObject();

         if (ai.getActivity().isInteractive())
         {
            // If Application Activity is interactive then it's Manual
            type = "ManualActivity";
         }

         if (isAuxiliaryActivity(ai.getActivity()))
         {
            type = "Auxiliary";
         }
         else if (!"ApplicationActivity".equals(type))
         {
            if ("SubprocessActivity".equals(orgType))
            {
               if (strict)
               {
                  type = orgType;
               }
               else
               {
                  type = "ApplicationActivity";
               }
            }
            else
            {
               // All non Auxiliary activities are Manual if not Application Activity
               type = "ManualActivity";
            }
         }
      }
      return type;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.treetable.NodeUserObject#getLine2Text()
    */
   @Override
   public String getLine2Text()
   {
      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();
      StringBuffer sbLine2 = new StringBuffer();

      if (activityTreeTable.isMiniMode() && (getStartTime() != null))
      {
         sbLine2.append((String) msgBean.get("processHistory.activityTable.startLabel") + ": ").append(
               DateUtils.formatDateTime(getStartTime()));

         if (getModificationTime() != null)
         {
            sbLine2.append(" " + (String) msgBean.get("processHistory.activityTable.lastModificationLabel")).append(
                  ": " + DateUtils.formatDateTime(getModificationTime()));
         }

         if (!StringUtils.isEmpty(getState()))
         {
            sbLine2.append(" " + getState());
         }

         if (!StringUtils.isEmpty(getUser()))
         {
            sbLine2.append(" " + getUser());
         }
      }

      return sbLine2.toString();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.treetable.TreeTableUserObject#isFilterOut(org.eclipse.stardust.ui.web.common.filter.TableDataFilters)
    */
   public boolean isFilterOut(TableDataFilters dataFilters)
   {
      for (ITableDataFilter tableDataFilters : dataFilters.getList())
      {
         ITableDataFilterOnOff onOffFilter = ((ITableDataFilterOnOff) tableDataFilters);

         if (onOffFilter.isOn())
         {
            if (onOffFilter.getName().equals(formatType(tableEntry, true)))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Checks whether current Activity is Auxiliary Activity
    * 
    * @param activity
    * @return
    */
   private static boolean isAuxiliaryActivity(Activity activity)
   {
      Boolean auxiliaryAttr = null;
      Object attr = activity.getAttribute(PredefinedConstants.ACTIVITY_IS_AUXILIARY_ATT);

      if (attr instanceof Boolean)
      {
         auxiliaryAttr = (Boolean) attr;
      }
      else if (attr instanceof String && !StringUtils.isEmpty((String) attr))
      {
         auxiliaryAttr = Boolean.valueOf((String) attr);
      }

      return ActivityReportUtils.isAuxiliaryActivity(auxiliaryAttr, activity.getImplementationType());
   }
   
   public QualityAssuranceCodesBean getQualityAssuranceCodesBean()
   {
      return qualityAssuranceCodesBean;
   }
   
   public String getAssignedTo()
   {
      return ((tableEntry instanceof EventHistoryItem)) ? "" : ((AbstractProcessHistoryTableEntry) tableEntry)
            .getPerformer();
   }

   public String getDetails()
   {
      return tableEntry.getDetails();
   }

   public String getFullDetails()
   {
      return fullDetail;
   }

   @Override
   public String getLine1Text()
   {
      return getText();
   }

   public ActivityTreeTable getActivityTreeTable()
   {
      return activityTreeTable;
   }

   public boolean isMoreDetailsAvailable()
   {
      return (fullDetail != null) && !StringUtils.isEmpty(getDetails());
   }

   public boolean isRefersToActivity()
   {
      return refersToActivity;
   }

   public Date getModificationTime()
   {
      return tableEntry.getLastModificationTime();
   }

   public Long getOID()
   {
      return tableEntry.getOID();
   }

   public Date getStartTime()
   {
      return tableEntry.getStartTime();
   }

   public String getState()
   {
      return tableEntry.getState();
   }

   @Override
   public String getStyleClass()
   {
      return tableEntry.isNodePathToActivityInstance() ? "active-process-history-row" : "completed-process-history-row";
   }

   public IProcessHistoryTableEntry getTableEntry()
   {
      return tableEntry;
   }

   @Override
   public String getText()
   {
      return tableEntry.getName();
   }

   public String getUser()
   {
      return (tableEntry instanceof EventHistoryItem) ? ((EventHistoryItem) tableEntry).getPerformer() : "";
   }

   public boolean isAbortActivity()
   {
      return abortActivity;
   }

   public boolean isActivatable()
   {
      return activatable;
   }

   public boolean isDelegable()
   {
      return delegable;
   }
   
   public int getCriticalityValue()
   {
      return criticalityValue;
   }
   
   public CriticalityCategory getCriticality()
   {
      return criticality;
   }

   public String getCriticalityLabel()
   {
      return CriticalityConfigurationUtil.getCriticalityDisplayLabel(getCriticalityValue(), getCriticality());
   }
}