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
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;



/**
 * @author Vikas.Mishra
 * 
 */
public class ProcessTableEntryUserObject extends NodeUserObject
{
   private static final long serialVersionUID = -5487464160841823357L;
   private ProcessInstanceHistoryItem tableEntry;
   private ProcessTreeTable processTreeTable;
   private String fullDetail;
   private boolean abortActivity;
   private boolean activatable;
   private boolean delegable;
   private boolean refersToActivity;
   private boolean selected;
   private boolean canModifyProcessInstance;

   /**
    * @param treeTable
    * @param node
    * @param treeBeanPointer
    * @param componenttype
    * @param tableEntry
    */
   public ProcessTableEntryUserObject(TreeTable treeTable, TreeTableNode node, ProcessTreeTable processTreeTable,
         Integer componentType, ProcessInstanceHistoryItem tableEntry)
   {
      super(treeTable, node, processTreeTable, componentType);
      this.tableEntry = tableEntry;
      this.canModifyProcessInstance = AuthorizationUtils.hasPIModifyPermission(getProcessInstance());
      this.processTreeTable = processTreeTable;

      String formatType = tableEntry.getRuntimeObjectType();
      String icon = (formatType != null) ? (ActivityInstanceUtils.getIconPath(formatType)) : null;
      setLeafIcon(icon);
      setBranchContractedIcon(icon);
      setBranchExpandedIcon(icon);
      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();
      setTooltip(msgBean.getString(ProcessHistoryTable.MSG_PREFIX + formatType));

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
            if (onOffFilter.getName().equals(tableEntry.getRuntimeObjectType())
                  && !tableEntry.getOID().equals(processTreeTable.getCurrentProcessInstance().getOID()))
            {
               return true;
            }
         }
      }
      return false;
   }

   public Map<String, Object> getDescriptorValues()
   {
      return tableEntry.getDescriptorValues();
   }

   public String getDetails()
   {
      return tableEntry.getDetails();
   }

   public String getDuration()
   {
      return tableEntry.getDuration();
   }

   public Date getEndTime()
   {
      return tableEntry.getEndTime();
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

   public Date getModificationTime()
   {
      return tableEntry.getLastModificationTime();
   }

   public int getNotesCount()
   {
      return tableEntry.getNotesCount();
   }

   public Long getOID()
   {
      return tableEntry.getOID();
   }

   public int getOldPriority()
   {
      return tableEntry.getOldPriority();
   }

   public int getPriority()
   {
      return tableEntry.getPriority();
   }

   public List<ProcessDescriptor> getProcessDescriptorsList()
   {
      return tableEntry.getProcessDescriptorsList();
   }

   public ProcessInstance getProcessInstance()
   {
      return (ProcessInstance) tableEntry.getRuntimeObject();
   }

   public long getProcessInstanceRootOID()
   {
      return tableEntry.getProcessInstanceRootOID();
   }

   public Date getStartTime()
   {
      return tableEntry.getStartTime();
   }

   public String getStartingUser()
   {
      return tableEntry.getStartingUser();
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
      return tableEntry.getPerformer();
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

   public boolean isEnableRecover()
   {
      return tableEntry.isEnableRecover();
   }

   public boolean isEnableTerminate()
   {
      return tableEntry.isEnableTerminate();
   }

   public boolean isMoreDetailsAvailable()
   {
      return (fullDetail != null) && !StringUtils.isEmpty(getDetails());
   }

   public boolean isRefersToActivity()
   {
      return refersToActivity;
   }

   public boolean isSelected()
   {
      return selected;
   }

   public void setPriority(int priority)
   {
      tableEntry.setPriority(priority);
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }

   @Override
   public String getLine2Text()
   {
      return null;
   }

   /**
    * @return the modifyProcessInstance
    */
   public boolean isCanModifyProcessInstance()
   {
      return this.canModifyProcessInstance;
   }
   
   public boolean isCaseInstance()
   {
      return tableEntry.isCaseInstance();
   }

   public boolean isEnableDetach()
   {
      return tableEntry.isEnableDetach();
   }
}