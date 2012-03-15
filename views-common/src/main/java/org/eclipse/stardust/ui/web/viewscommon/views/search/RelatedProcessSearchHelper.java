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
package org.eclipse.stardust.ui.web.viewscommon.views.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ProcessInstanceHierarchyFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessStateFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DataMappingWrapper;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorColumnUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 * @since 7.0
 */
public class RelatedProcessSearchHelper
{
   private static final int MAX_RESULT = 20;
   private static final String COL_PROCESS_NAME = "processName";
   private static final String COL_PROCESS_OID = "processOID";
   private static final String COL_PRIORITY = "priority";
   private static final String COL_PROCESS_START_TIME = "processStartTime";
   private static final String COL_CASE_OWNER = "caseOwner";

   private final MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();
   private DataTable<RelatedProcessTableEntry> relatedProcessTable;
   private IColumnModel processColumnModel;
   private List<ProcessInstance> sourceProcessInstances;
   private boolean matchAny;
   private Map<String, DataPath> keyDescriptors;
   private Map<String, Object> sourceDescriptors;
   private boolean searchCases;
   private boolean isSourceCase;
   private Map<Boolean, List<ProcessInstance>> resultCache;
   private String messageHeader;
   

   /**
    * initialize table
    */
   public void initialize()
   {
      resultCache = CollectionUtils.newHashMap();
      if (null != sourceProcessInstances)
      {
         if (null != sourceProcessInstances && ProcessInstanceUtils.isCaseProcessInstances(sourceProcessInstances))
         {
            isSourceCase = true;
            ProcessInstanceDetails casePIDetails = (ProcessInstanceDetails) sourceProcessInstances.get(0);
            keyDescriptors = CommonDescriptorUtils.findCaseSourceDescriptors(casePIDetails);
            sourceDescriptors = casePIDetails.getDescriptors();
         }
         else
         {
            keyDescriptors = CommonDescriptorUtils.getCommonDescriptorsMap(sourceProcessInstances, true);
            sourceDescriptors = getSourceDescriptors();
         }

         createTable();
         buildTable();
      }
      
      
   }

   /**
    * reset instance variable
    */
   public void reset()
   {
      matchAny = false;
      sourceProcessInstances = null;
      processColumnModel = null;
      relatedProcessTable = null;
      if (null != resultCache)
      {
         resultCache.clear();
      }
   }

   /**
    * call to this will re-build table
    */
   public void update()
   {
      buildTable();
   }

   /**
    * 
    * @return
    */

   public boolean isMatchAny()
   {
      return matchAny;
   }

   /**
    * 
    * @return
    */

   public boolean isSearchCases()
   {
      return searchCases;
   }

   /**
    * Evaluate the descriptors to be displayed
    */
   private List<DataMappingWrapper> createDataMappingWrapper(Collection<DataPath> dataPaths)
   {
      List<DataMappingWrapper> list = CollectionUtils.newArrayList();

      GenericDataMapping mapping;
      DataMappingWrapper dmWrapper;

      for (DataPath path : dataPaths)
      {
         mapping = new GenericDataMapping(path);
         dmWrapper = new DataMappingWrapper(mapping, null, false);
         list.add(dmWrapper);
         dmWrapper.setDefaultValue(null);

         if (CollectionUtils.isNotEmpty(sourceDescriptors) && sourceDescriptors.containsKey(path.getId()))
         {
            Object value = sourceDescriptors.get(path.getId());
            if (null != value)
            {
               if (value instanceof String && StringUtils.isNotEmpty(value.toString()))
               {
                  dmWrapper.setValue(sourceDescriptors.get(path.getId()));
               }
               else if (!(value instanceof String))
               {
                  dmWrapper.setValue(sourceDescriptors.get(path.getId()));
               }
            }
         }
      }

      return list;
   }

   /**
    * method create Related Processes Table
    */
   private void createTable()
   {
      ColumnPreference processNameCol = new ColumnPreference(COL_PROCESS_NAME, "processName", ColumnDataType.STRING,
            COMMON_MESSAGE_BEAN.getString("views.relatedProcessSearch.table.column.processName"), true, false);
      ColumnPreference processOIDCol = new ColumnPreference(COL_PROCESS_OID, "oid", ColumnDataType.NUMBER,
            COMMON_MESSAGE_BEAN.getString("views.relatedProcessSearch.table.column.oid"), true, false);
      ColumnPreference caseOwnerCol = new ColumnPreference(COL_CASE_OWNER, "caseOwner", ColumnDataType.STRING,
            COMMON_MESSAGE_BEAN.getString("views.relatedProcessSearch.table.column.caseOwner"), true, false);
      ColumnPreference priorityCol = new ColumnPreference(COL_PRIORITY, "priority", ColumnDataType.STRING,
            COMMON_MESSAGE_BEAN.getString("views.relatedProcessSearch.table.column.priority"), true, false);
      priorityCol.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference startTimeCol = new ColumnPreference(COL_PROCESS_START_TIME, "startTime", ColumnDataType.DATE,
            COMMON_MESSAGE_BEAN.getString("views.relatedProcessSearch.table.column.startTime"), true, false);

      List<ColumnPreference> fixedBeforeCols = new ArrayList<ColumnPreference>();
      fixedBeforeCols.add(processNameCol);
      fixedBeforeCols.add(processOIDCol);
      if (searchCases)
      {
         fixedBeforeCols.add(caseOwnerCol);
      }
      fixedBeforeCols.add(priorityCol);

      List<ColumnPreference> fixedAfterCols = new ArrayList<ColumnPreference>();
      fixedAfterCols.add(startTimeCol);

      // add descriptor columns
      List<ColumnPreference> descriptorCols = getDescriptorColumns();

      if (CollectionUtils.isNotEmpty(descriptorCols))
      {
         fixedBeforeCols.addAll(descriptorCols);
      }

      processColumnModel = new DefaultColumnModel(null, fixedBeforeCols, fixedAfterCols,
            UserPreferencesEntries.M_VIEWS_COMMON, "RelatedProcessTableDialog");

      relatedProcessTable = new DataTable<RelatedProcessTableEntry>(processColumnModel, (TableDataFilters) null);
      relatedProcessTable.setRowSelector(new DataTableRowSelector("selected", isSourceCase));
      relatedProcessTable.setList(new ArrayList<RelatedProcessTableEntry>());
      relatedProcessTable.initialize();
   }

   /**
    *
    */
   private void buildTable()
   {

      List<RelatedProcessTableEntry> tablelList = CollectionUtils.newArrayList();
      try
      {
         // if key descriptors is empty then no need to create n fire query
         if (CollectionUtils.isNotEmpty(keyDescriptors))
         {

            List<ProcessInstance> processInstances = null;
            
            //first check in cache
            if (resultCache.containsKey(matchAny))
            {
               processInstances = resultCache.get(matchAny);
            }
            else
            {
               ProcessInstanceQuery query = createQuery();
               processInstances = ServiceFactoryUtils.getQueryService().getAllProcessInstances(query);
               resultCache.put(matchAny, processInstances);
            }
            if (CollectionUtils.isNotEmpty(processInstances))
            {
               for (ProcessInstance processInstance : processInstances)
               {
                  tablelList.add(new RelatedProcessTableEntry(processInstance));
               }
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

      relatedProcessTable.setList(tablelList);
   }

   /**
    * 
    * @param datas
    * @return
    */
   private ProcessInstanceQuery createQueryCaseSearch(Collection<DataPath> datas)
   {
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findCases();
      piQuery.getFilter().add(new ProcessStateFilter(ProcessInstanceState.Active));
      excludeSourceProcesses(piQuery);
      FilterTerm filter = matchAny ? piQuery.getFilter().addOrTerm() : piQuery.getFilter().addAndTerm();

      for (DataPath path : datas)
      {
         if (sourceDescriptors.containsKey(path.getId()))
         {
            Object value = sourceDescriptors.get(path.getId());
            
            if (null==value ||StringUtils.isEmpty(value.toString()))
            {
               filter.add(DataFilter.equalsCaseDescriptor(path.getId(), ""));
            }            
            else
            {
               filter.add(DataFilter.equalsCaseDescriptor(path.getId(), sourceDescriptors.get(path.getId())));
            }
         }
      }
      return piQuery;

   }

   /**
    * method should be used create Query only if selected process in case instance to
    * search ProcessInstances
    * 
    * @param datas
    * @return
    */
   private ProcessInstanceQuery createQueryCaseToPISearch(Collection<DataPath> datas)
   {
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAlive();
      piQuery.getFilter().and(ProcessInstanceHierarchyFilter.ROOT_PROCESS);
      SubsetPolicy newPolicy = new SubsetPolicy(MAX_RESULT, 0, false);
      piQuery.setPolicy(newPolicy);
      piQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      excludeSourceProcesses(piQuery);

      try
      {
         FilterTerm filter = matchAny ? piQuery.getFilter().addOrTerm() : piQuery.getFilter().addAndTerm();
         for (DataPath dataPath : datas)
         {
            Serializable value = (Serializable) sourceDescriptors.get(dataPath.getId());
            DataFilter dataFilter = DescriptorFilterUtils.getDateFilter(dataPath, value);

            if (null != dataFilter)
            {
               filter.add(dataFilter);
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

      return piQuery;

   }

   /**
    * method should be used create Query only if selected process in non-case instance to
    * search ProcessInstances
    * 
    * @param datas
    * @return
    */
   private ProcessInstanceQuery createQueryPIToPISearch(Collection<DataPath> datas)
   {
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAlive();
      piQuery.getFilter().and(ProcessInstanceHierarchyFilter.ROOT_PROCESS);

      SubsetPolicy newPolicy = new SubsetPolicy(MAX_RESULT, 0, false);
      piQuery.setPolicy(newPolicy);
      piQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      excludeSourceProcesses(piQuery);

      List<DataMappingWrapper> descriptorItems = createDataMappingWrapper(datas);
      DataPath[] dataArrays = datas.toArray(new DataPath[datas.size()]);
      DescriptorFilterUtils.evaluateAndApplyFilters(piQuery, descriptorItems, dataArrays, !matchAny);
      return piQuery;
   }

   /**
    * method create ProcessInstanceQuery,it filter-out source process instance query only
    * fetch process instance if key descriptor match (any or all)
    * 
    * @return ProcessInstanceQuery
    */
   /**
    * method create ProcessInstanceQuery,it filter-out source process instance query only
    * fetch process instance if key descriptor match (any or all)
    * 
    * @return ProcessInstanceQuery
    */
   private ProcessInstanceQuery createQuery()
   {
      ProcessInstanceQuery piQuery = null;
      Collection<DataPath> datas = keyDescriptors.values();

      if (searchCases)
      {
         piQuery = createQueryCaseSearch(datas);
      }
      else
      {
         if (!isSourceCase)
         {
            piQuery = createQueryPIToPISearch(datas);
         }
         else
         {
            piQuery = createQueryCaseToPISearch(datas);
         }
      }

      return piQuery;
   }

   /**
    * 
    * @param query
    */
   private void excludeSourceProcesses(Query query)
   {
      FilterAndTerm filter = query.getFilter().addAndTerm();

      for (ProcessInstance processInstance : sourceProcessInstances)
      {
         filter.and(ProcessInstanceQuery.OID.notEqual(processInstance.getOID()));
      }
   }

   /**
    * 
    * @return
    */
   public DataTable<RelatedProcessTableEntry> getRelatedProcessTable()
   {
      return relatedProcessTable;
   }

   /**
    * create Descriptor Columns for table and set visible property =true for all cases.
    */
   public List<ColumnPreference> getDescriptorColumns()
   {
      List<ColumnPreference> cols = DescriptorColumnUtils.createDescriptorColumns(relatedProcessTable, keyDescriptors);

      for (ColumnPreference pref : cols)
      {
         // always set visible=true
         pref.setVisible(true);
      }

      return cols;
   }

   public List<ProcessInstance> getSourceProcessInstances()
   {
      return sourceProcessInstances;
   }

   /**
    * 
    * @return SelectedProcessInstance table row
    */
   public RelatedProcessTableEntry getSelectedProcessInstance()
   {
      for (RelatedProcessTableEntry tableEntry : relatedProcessTable.getList())
      {
         if (tableEntry.isSelected())
         {
            return tableEntry;
         }
      }

      return null;
   }

   /**
    * 
    * @return SelectedProcessInstance table row
    */
   public List<RelatedProcessTableEntry> getSelectedProcessInstances()
   {
      List<RelatedProcessTableEntry> selectedList = CollectionUtils.newArrayList();

      for (RelatedProcessTableEntry tableEntry : relatedProcessTable.getList())
      {
         if (tableEntry.isSelected())
         {
            selectedList.add(tableEntry);
         }
      }
      return selectedList;
   }

   /**
    * 
    * @return
    */
   private Map<String, Object> getSourceDescriptors()
   {
      Map<String, Object> descriptors = CollectionUtils.newHashMap();

      for (ProcessInstance processInstance : sourceProcessInstances)
      {
         // check if source Pi not contains descriptors then load again with descriptors
         ProcessInstanceDetails piDetail = (ProcessInstanceDetails) processInstance;
         if (null != piDetail.getDescriptors())
         {
            piDetail = (ProcessInstanceDetails) ProcessInstanceUtils.getProcessInstance(processInstance.getOID(),
                  false, true);
         }
         descriptors.putAll(piDetail.getDescriptors());
      }
      descriptors.keySet().retainAll(keyDescriptors.keySet());

      return descriptors;
   }

   /**
    * 
    * @param sourceProcessInstances
    */
   public void setSourceProcessInstances(List<ProcessInstance> sourceProcessInstances)
   {
      this.sourceProcessInstances = sourceProcessInstances;
   }

   /**
    * 
    * @param matchAny
    */
   public void setMatchAny(boolean matchAny)
   {
      this.matchAny = matchAny;
   }

   /**
    * 
    * @param searchCase
    */
   public void setSearchCases(boolean searchCase)
   {
      this.searchCases = searchCase;
   }
   
   public String getMessageHeader()
   {
      if (searchCases)
      {
         messageHeader = matchAny ? COMMON_MESSAGE_BEAN
               .getString("views.joinProcessDialog.descriptorAnyMatchingCases.message") : COMMON_MESSAGE_BEAN
               .getString("views.joinProcessDialog.descriptorAllMatchingCases.message");
      }
      else
      {
         messageHeader = matchAny ? COMMON_MESSAGE_BEAN
               .getString("views.joinProcessDialog.descriptorAnyMatchingProcesses.message") : COMMON_MESSAGE_BEAN
               .getString("views.joinProcessDialog.descriptorAllMatchingProcesses.message");
      }
      return messageHeader;
   }

}
