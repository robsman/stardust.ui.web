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
package org.eclipse.stardust.ui.web.viewscommon.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.HistoricalEventType;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter.DataType;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterBetween;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterDate;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterNumber;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.DateRange;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.common.ProcessAttachmentColumnPreference;
import org.eclipse.stardust.ui.web.viewscommon.common.ProcessDocumentColumnPreference;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentInfo;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.springframework.util.CollectionUtils;



/**
 * @author yogesh.manware
 * 
 */
public class DescriptorColumnUtils
{

   private static final String DESC_TRUE = "true";
   private static final String DESC_FALSE = "false";
   /**
    * @return
    */
   public static List<ColumnPreference> createDescriptorColumns()
   {
      return createDescriptorColumns(null, CommonDescriptorUtils.getAllDescriptors(false));
   }
   
   /**
    * contentUrl for Document Descriptor rendering (Process Attachment and Type Documents)
    * 
    * @param contentUrl
    * @return
    */
   public static List<ColumnPreference> createDescriptorColumns(String contentUrl)
   {
      return createDescriptorColumns(null, CommonDescriptorUtils.getAllDescriptors(false), contentUrl);
   }
   
   
   /**
    * creates filterable columns on the provided table
    * @param table
    * @param allDescriptors
    * @return
    */
   public static List<ColumnPreference> createDescriptorColumns(
         DataTable<? extends DefaultRowModel> table, Map<String, DataPath> allDescriptors, String contentUrl)
   {
      List<ColumnPreference> descriptorColumns = new ArrayList<ColumnPreference>();

      for (Entry<String, DataPath> descriptor : allDescriptors.entrySet())
      {
         String descriptorId = descriptor.getKey();
         DataPath dataPath = descriptor.getValue();
         
         Model model = ModelCache.findModelCache().getModel(dataPath.getModelOID());
         DataDetails dataDetails = model != null ? (DataDetails) model.getData(dataPath.getData()) : null;
         
         if ((contentUrl != null)
               && (null != dataDetails && (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataDetails.getTypeId()) || DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST
                     .equals(dataDetails.getTypeId()))))
         {
            ColumnPreference descriptorColumn = new ProcessDocumentColumnPreference(descriptorId, "descriptorValues."
                  + descriptorId + "", I18nUtils.getDataPathName(dataPath), contentUrl, false, false);

            descriptorColumn.setEscape(false);
            descriptorColumns.add(descriptorColumn);
         }
         else if ((contentUrl != null) && ((DmsConstants.DATA_ID_ATTACHMENTS.equals(dataPath.getData()))))
         {
            ColumnPreference descriptorColumn = new ProcessAttachmentColumnPreference(descriptorId, "descriptorValues."
                  + descriptorId + "", I18nUtils.getDataPathName(dataPath), contentUrl, false, false);

            descriptorColumn.setEscape(false);
            descriptorColumns.add(descriptorColumn);
         }
         else
         {
            ColumnDataType columnType = determineColumnType(dataPath);
            // double and float are not sortable
            boolean sortable = DescriptorFilterUtils.isDataSortable(dataPath);
            
            ColumnPreference descriptorColumn = new ColumnPreference(descriptorId,
                  "descriptorValues." + descriptorId + "", columnType, I18nUtils.getDataPathName(dataPath), false, sortable);
            descriptorColumn.setEscape(false);
            descriptorColumns.add(descriptorColumn);
         }
      }
      return descriptorColumns;
   }
   
   /**
    * creates filterable columns on the provided table
    * @param table
    * @param allDescriptors
    * @return
    */
   public static List<ColumnPreference> createDescriptorColumns(
         DataTable<? extends DefaultRowModel> table, Map<String, DataPath> allDescriptors)
   {
      return createDescriptorColumns(table, allDescriptors, null);
   }

   /**
    * @param table
    * @param allDescriptors
    */
   public static void setDescriptorColumnFilters(IColumnModel columnModel, Map<String, DataPath> allDescriptors)
   {
      for (ColumnPreference colPref : columnModel.getSelectableColumns())
      {
         if (colPref.getColumnProperty().startsWith("descriptorValues."))
         {
            String property = colPref.getColumnProperty();
            if (property.indexOf("descriptorValues.") != -1)
            {
               String descriptorId = property.substring(property.indexOf(".") + 1);
                  
               DataPath dataPath = allDescriptors.get(descriptorId);
               if (null != dataPath && DescriptorFilterUtils.isDataFilterable(allDescriptors.get(descriptorId)))
               {
                  TableDataFilterPopup dataFilterPopup = createFilterPopup(dataPath, colPref.getColumnDataType());
                  colPref.setColumnDataFilterPopup(dataFilterPopup);
               }
            }
         }
      }
   }

   /**
    * 
    * Modify the input parameter 'filter' applying descriptor filters
    * 
    * @param tableDataFilter
    *           it is configured in relevant table
    * @param filter
    *           this filter will be modified by this method
    * @author yogesh.manware
    */
   public static Serializable getFilterValue(ITableDataFilter tableDataFilter, DataPath dataPath)
   {
      try
      {
         // Boolean type desc
         if (tableDataFilter instanceof TableDataFilterOnOff)
         {
            TableDataFilterOnOff onOffFilter = ((TableDataFilterOnOff) tableDataFilter);
            if (onOffFilter.isFilterSet())
            {
               Boolean filterValue = onOffFilter.isOn();
               return filterValue;
            }
         }
         // String type desc
         else if (tableDataFilter instanceof TableDataFilterSearch)
         {
            String filterByValue;
            filterByValue = ((TableDataFilterSearch) tableDataFilter).getValue();
            return filterByValue;
         }
         // Number type desc
         else if (tableDataFilter instanceof TableDataFilterNumber)
         {
            if (ITableDataFilter.FilterCriteria.NUMBER.equals(tableDataFilter.getFilterCriteria()))
            {
               return (Number) ((ITableDataFilterBetween) tableDataFilter).getStartValueAsDataType();
            }
            else
            {
               Number from = (Number) ((ITableDataFilterBetween) tableDataFilter).getStartValueAsDataType();
               Number to = (Number) ((ITableDataFilterBetween) tableDataFilter).getEndValueAsDataType();
               return new NumberRange(from, to);
            }
         }
         // Date type desc
         else if (tableDataFilter instanceof TableDataFilterDate)
         {
            Date fromTime = (Date) ((ITableDataFilterBetween) tableDataFilter).getStartValueAsDataType();
            Date toTime = (Date) ((ITableDataFilterBetween) tableDataFilter).getEndValueAsDataType();
            return new DateRange(fromTime, toTime);
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      return null;
   }

   /**
    * 
    * @param colPref
    * @param allDescriptors
    * @param separator
    * @return
    */
   public static Object exportDescriptorColumn(ColumnPreference colPref, Map<String, Object> allDescriptors, String separator)
   {
      String property = colPref.getColumnProperty();
      if (property.indexOf("descriptorValues.") != -1)
      {
         String descriptorId = property.substring(property.indexOf(".") + 1);
            
         Object descriptorValue = allDescriptors.get(descriptorId);
         if (null != descriptorValue)
         {
            if (descriptorValue instanceof Collection< ? >)
            {
               List descVals = (List) descriptorValue;
               if (!CollectionUtils.isEmpty(descVals) && descVals.get(0) instanceof DocumentInfo)
               {

                  List<DocumentInfo> docList = (List<DocumentInfo>) descriptorValue;
                  StringBuffer exportData = new StringBuffer("");
                  for (DocumentInfo doc : docList)
                  {
                     exportData.append(doc.getName()).append(separator);
                  }
                  String data = exportData.toString();
                  if (data.length() > 0)
                  {
                     data = data.substring(0, data.length() - separator.length());
                  }
                  return data;
               }
            }
            if (descriptorValue instanceof DocumentInfo)
            {
               DocumentInfo document = (DocumentInfo) descriptorValue;
               return document.getName();
            }
            else
            {
               return descriptorValue.toString();
            }
         }
      }
      return null;
   }
   
   /**
    * @param descriptors
    * @param separator
    * @return
    */
   public static String exportDescriptors(List<ProcessDescriptor> descriptors, String separator)
   {
      StringBuffer exportData = new StringBuffer("");
      MessagePropertiesBean props = MessagePropertiesBean.getInstance();
      for (ProcessDescriptor desc : descriptors)
      {
         // Added for I18N of boolean descriptors
         if (desc.getValue().equals(DESC_TRUE))
         {
            exportData.append(desc.getKey()).append(" : ").append(props.getString("common.true")).append(separator);
         }
         else if (desc.getValue().equals(DESC_FALSE))
         {
            exportData.append(desc.getKey()).append(" : ").append(props.getString("common.false")).append(separator);
         }
         else
         {
            exportData.append(desc.getKey()).append(" : ").append(desc.getValue()).append(separator);
         }

      }

      String data = exportData.toString();
      if (data.length() > 0)
      {
         data = data.substring(0, data.length() - separator.length());
      }
      return data;
   }

   /**
    * create filter popups
    * @param dataPath
    * @param columnDataType
    * @return
    */
   private static TableDataFilterPopup createFilterPopup(DataPath dataPath, ColumnDataType columnDataType)
   {
      TableDataFilterPopup popup;
      
      // Date Range Filter
      if (ColumnDataType.DATE.equals(columnDataType))
      {
         popup = new TableDataFilterPopup(
               new TableDataFilterDate(dataPath.getId(), "", DataType.DATE, true, null, null));
      }
      else if (ColumnDataType.DATE_WITHOUT_TIME.equals(columnDataType))
      {
         popup = new TableDataFilterPopup(
               new TableDataFilterDate(dataPath.getId(), "", DataType.DATE_WITHOUT_TIME, true, null, null));
      }

      // Number filter
      else if (ColumnDataType.NUMBER.equals(columnDataType))
      {
         popup = new TableDataFilterPopup(new TableDataFilterNumber(dataPath.getId(), "",
               determineNumberDataType(dataPath.getMappedType()), true, null, null));
      }
      // Boolean Filter
      else if (ColumnDataType.BOOLEAN.equals(columnDataType))
      {
         popup = new TableDataFilterPopup(new TableDataFilterOnOff(dataPath.getId(), "", true, null));
      }
      // String Filter
      else
      {
         popup = new TableDataFilterPopup(new TableDataFilterSearch());
      }
      return popup;
   }

   /**
    * @param mappedType
    * @return
    */
   private static ColumnDataType determineColumnType(DataPath dataPath)
   {
      Class mappedType = dataPath.getMappedType();
      
      if (Boolean.class.equals(mappedType))
      {
         return ColumnDataType.BOOLEAN;
      }
      if (Date.class.equals(mappedType))
      {
         GenericDataMapping mapping = new GenericDataMapping(dataPath);
         DataMappingWrapper dmWrapper = new DataMappingWrapper(mapping, null, false);
         if (ProcessPortalConstants.TIMESTAMP_TYPE.equals(dmWrapper.getType()))
         {
            return ColumnDataType.DATE;
         }
         else
         {
            return ColumnDataType.DATE_WITHOUT_TIME;
         }
      }
      else if (determineNumberDataType(mappedType) != null)
      {
         return ColumnDataType.NUMBER;
      }
      else
      {
         return ColumnDataType.STRING;
      }
   }

   /**
    * 
    * @param mappedType
    * @return
    */
   private static boolean determineDocumentTypeColumn(Class mappedType)
   {
      if (Document.class.equals(mappedType))
      {
         return true;
      }
      // To support List<Documents> apart from DocumentInfo
      else if (Collection.class.isAssignableFrom(mappedType))
      {
         return true;
      }
      return false;
   }
   
   /**
    * @param mappedType
    * @return
    */
   private static DataType determineNumberDataType(Class mappedType)
   {
      if (Byte.class.equals(mappedType))
      {
         return DataType.BYTE;
      }
      else if (Short.class.equals(mappedType))
      {
         return DataType.SHORT;
      }
      else if (Integer.class.equals(mappedType))
      {
         return DataType.INTEGER;
      }
      else if (Long.class.equals(mappedType))
      {
         return DataType.LONG;
      }
      else if (Float.class.equals(mappedType))
      {
         return DataType.FLOAT;
      }
      else if (Double.class.equals(mappedType))
      {
         return DataType.DOUBLE;
      }
      return null;
   }

   /**
    * 
    * @param scopeProcessInstance
    * @return
    */
   public static List<HistoricalEvent> getProcessDescriptorsHistory(ProcessInstance scopeProcessInstance)
   {
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
      FilterOrTerm orTerm =  piQuery.getFilter().addOrTerm();
                  
      piQuery.setPolicy(new HistoricalEventPolicy(
            new HistoricalEventType[] {HistoricalEventType.DataChange}));

      orTerm.add(new ProcessInstanceFilter(scopeProcessInstance.getOID(), false));
      
      ServiceFactory sf = SessionContext.findSessionContext().getServiceFactory();
      QueryService qs = (sf != null) ? sf.getQueryService() : null;
   
      if (qs != null)
      {
         ProcessInstance pi = qs.getAllProcessInstances(piQuery).get(0);
         List<HistoricalEvent> events = pi.getHistoricalEvents();
         return events;
      }
      
      return null;
   }
}