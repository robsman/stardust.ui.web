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
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelElement;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.DataOrder;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.DateRange;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.XPathCacheManager;

/**
 * @author rsauer
 * @version $Revision: 33090 $
 */
public class DescriptorFilterUtils
{
   private static final Logger trace = LogManager.getLogger(DescriptorFilterUtils.class);
   // Scan Client Date Format

   /**
    * @author Yogesh.Manware
    * @param query
    * @param descriptorId
    * @param dataPath
    * @param ascending
    */
   public static void applySorting(Query query, String descriptorId, DataPath dataPath, boolean ascending)
   {
      DataPathMetadata descriptorFlags = getDataPathMetadata(dataPath);
      if (descriptorFlags.isSortable())
      {
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
         String dataId = getData(dataPath).getQualifiedId();
         if (CommonDescriptorUtils.isStructuredData(dataPath))
         {
            query.orderBy(new DataOrder(dataId, dataPath.getAccessPath(), ascending));
         }
         else
         {
            query.orderBy(new DataOrder(dataId, ascending));
         }
      }
   }

   /**
    * Evaluates if a data path is filterable.
    * 
    * @param dataPath
    *           <code>DataPath</DataPath> that is to be evaluated
    * @return true if the data path is filterable
    */
   public static boolean isDataFilterable(DataPath dataPath)
   {
      return getDataPathMetadata(dataPath).isFilterable();
   }
   
   
   /**
    * Evaluates if a data path is sortable.
    * 
    * @param dataPath
    *           <code>DataPath</DataPath> that is to be evaluated
    * @return true if the data path is sortable
    */
   public static boolean isDataSortable(DataPath dataPath)
   {
      return getDataPathMetadata(dataPath).isSortable();
   }

   /**
    * Evaluates if a data path is sortable and filterable.
    * 
    * @param dataPath
    *           <code>DataPath</DataPath> that is to be evaluated
    * @return Wrapper for the sortable and filterable flag
    */
   public static DataPathMetadata getDataPathMetadata(DataPath dataPath)
   {
      Model model = ModelCache.findModelCache().getModel(dataPath.getModelOID());
      DataPathMetadata dataPathMD = new DataPathMetadata();
      if (model != null)
      {
         Data data = model.getData(dataPath.getData());
         if (data instanceof DataDetails)
         {
            DataDetails dataDetails = (DataDetails) data;
            String typeId = dataDetails.getTypeId();
            if (StructuredDataConstants.STRUCTURED_DATA.equals(typeId))
            {
               dataPathMD.structured = true;
               String myXPath = dataPath.getAccessPath();
               // Pepper models, return null for Access Path, Eclipse model returns "" for
               // Structured Enum
               myXPath = myXPath == null  ?  "" : myXPath;
               if (null != myXPath && !myXPath.contains("["))
               {   
                  // this is important and maybe needs discussion:
                  // since engine does only support queries on simple XPaths (e.g. no
                  // indexes, functions, etc.)
                  // the XPath must be simplified (e.g. indexes will be removed)
                  myXPath = StructuredDataXPathUtils.getXPathWithoutIndexes(myXPath);
   
                  dataPathMD.xPath = myXPath;
                  
                  // For performance get data from Cache
                  IXPathMap xPathMap = XPathCacheManager.getInstance().getXpathMap(model, dataPath);
                  if (null == xPathMap)
                  {
                     trace.warn("Invalid structured data reference. Data path id was '" + dataPath.getId()
                           + "' and has referenced the following access path '" + myXPath + "'");
                  }
                  else
                  {
                     // but, if, for example, indexes are removed, the semantics of the
                     // query is
                     // then different!
                     // my current favourite solution would be not allowing entering
                     // indexes in
                     // the modeller,
                     // in the XPath dialog for process data descriptors
                     TypedXPath typedXPath = xPathMap.getXPath(myXPath);

                     if (typedXPath == null)
                     {
                        trace.warn("Invalid structured data reference. Data path id was '" + dataPath.getId()
                              + "' and has referenced the following access path '" + myXPath + "'");
                     }
                     // test, if the XPath returns a primitive
                     else if (typedXPath.getType() != BigData.NULL)
                     {
                        // it is a list of primitives or a single primitive
                        dataPathMD.filterable = true;
                        dataPathMD.sortable = !typedXPath.isList();
                        dataPathMD.typedXPath = typedXPath;
                     }
                  }
               }
               else
               {
                  trace.warn("Invalid structured data reference. Data path id was '" + dataPath.getId()
                        + "' and has referenced the following access path '" + myXPath + "'");
               }
            }
            else if ("primitive".equals(typeId) && StringUtils.isEmpty(dataPath.getAccessPath()))
            {
               if (!PredefinedConstants.CURRENT_DATE.equals(data.getId())
                     && !PredefinedConstants.ROOT_PROCESS_ID.equals(data.getId()))
               {
                  dataPathMD = new DataPathMetadata(true, true);
               }
               Class mappedType = dataPath.getMappedType();
               if (Float.class.equals(mappedType) || Double.class.equals(mappedType))
               {
                  dataPathMD.sortable = false;
               }
               
               // Calendar is primitive but does not support filtering and sorting except BusinessDate
               if (Calendar.class.equals(mappedType))
               {
                  if (!PredefinedConstants.BUSINESS_DATE.equals(data.getId()))
                  {
                     dataPathMD = new DataPathMetadata(false, false);
                  }
               }
            }
         }
      }
      return dataPathMD;
   }

   /**
    * Applies a given descriptor filter model to a query. Currently the following query
    * types are supported: <code>ActivityInstanceQuery</code>, <code>WorklistQuery</code>
    * and <code>ProcessInstanceQuery</code>
    * 
    * @author Yogesh.Manware
    * @param term
    *           <code>FilterTerm</code> that is used to append the descriptor filter
    * @param filterModel
    *           Descriptor filter model
    * @param queryClass
    *           Type of query
    * @param caseSensitive
    *           Set a case sensitive flag for a condition where it makes sense
    */
   public static void applyDescriptorDataFilters(FilterTerm predicate, IDescriptorFilterModel filterModel,
         Class< ? extends Query> queryClass, boolean caseSensitive)
   {
      if ((null != filterModel) && filterModel.isFilterEnabled())
      {
         List filterableData = filterModel.getFilterableData();
         if ((null != filterableData) && !filterableData.isEmpty())
         {
            for (int i = 0; i < filterableData.size(); ++i)
            {
               GenericDataMapping mapping = (GenericDataMapping) filterableData.get(i);
               DataPath dataPath = (DataPath) mapping.getModelElement();
               Serializable filterValue = filterModel.getFilterValue(mapping.getId());

               if (null != filterValue)
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("setting data filter: '" + mapping.getId() + "=" + filterValue + "'");
                  }

                  DataFilter dataFilter = null;
                  DataFilter dataFilterOrTerm=null;

                  // for String
                  if (Character.class.equals(dataPath.getMappedType()) || String.class.equals(dataPath.getMappedType()))
                  {
                     if(filterValue instanceof Collection<?>)
                     {
                        FilterOrTerm term = predicate.addOrTerm();
                        if(filterValue instanceof Set<?>)
                        {
                           Set<Object> vals= (Set<Object>) filterValue;
                           for(Object obj:vals)
                           {
                              dataFilterOrTerm = getStringFilter(mapping, dataPath, obj.toString(), caseSensitive);
                              term.add(dataFilterOrTerm);
                           }
                        }
                     }
                     else
                     {
                        dataFilter = getStringFilter(mapping, dataPath, filterValue, caseSensitive);
                     }
                  }// for boolean
                  else if (Boolean.class.equals(dataPath.getMappedType()))
                  {
                     dataFilter = getBooleanFilter(dataPath, filterValue);
                  }
                  // for single number
                  else if (filterValue instanceof Number)
                  {
                     Number filterValueNumber = getNumberFilterValue(mapping, (Number) filterValue);
                     dataFilter = getNumberFilter(dataPath, filterValueNumber);
                  }// for number range
                  else if (filterValue instanceof NumberRange)
                  {
                     Number fromValue = getNumberFilterValue(mapping, ((NumberRange) filterValue).getFromValue());
                     Number toValue = getNumberFilterValue(mapping, ((NumberRange) filterValue).getToValue());
                     dataFilter = getNumberRangeFilter(dataPath, fromValue, toValue);
                  }
                  // for Date range
                  else if (filterValue instanceof DateRange)
                  {
                     dataFilter = getDateFilter(dataPath, (DateRange) filterValue);
                  }
                  else if(dataPath.getMappedType() instanceof Class<?>)
                  {
                     dataFilter = getStringFilter(mapping, dataPath, filterValue, caseSensitive);
                  }

                  if (mapping.getDataId().equals("PROCESS_PRIORITY"))
                  {
                     /*
                      * @Workaround: Value '2' is only a synonym for null (see
                      * filter-field.xhtml for a more detailed description).
                      */
                     if (((Number) filterValue).intValue() != 2)
                     {
                        if (ActivityInstanceQuery.class.equals(queryClass))
                        {
                           predicate.add(ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY.isEqual(((Number) filterValue)
                                 .intValue()));
                        }
                        else if (WorklistQuery.class.equals(queryClass))
                        {
                           predicate.add(WorklistQuery.PROCESS_INSTANCE_PRIORITY.isEqual(((Number) filterValue)
                                 .intValue()));
                        }
                        else if (ProcessInstanceQuery.class.equals(queryClass))
                        {
                           predicate.add(ProcessInstanceQuery.PRIORITY.isEqual(((Number) filterValue).intValue()));
                        }
                     }
                  }
                  else if (mapping.getDataId().equals(PredefinedConstants.ROOT_PROCESS_ID)
                        && ProcessInstanceQuery.class.equals(queryClass))
                  {
                     predicate.add(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(((Number) filterValue)
                           .intValue()));
                  }
                  else if (mapping.getDataId().equals(PredefinedConstants.PROCESS_ID))
                  {
                     if (ActivityInstanceQuery.class.equals(queryClass))
                     {
                        predicate.add(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(((Number) filterValue)
                              .intValue()));
                     }
                     else if (WorklistQuery.class.equals(queryClass))
                     {
                        predicate.add(WorklistQuery.PROCESS_INSTANCE_OID.isEqual(((Number) filterValue).intValue()));
                     }
                     else if (ProcessInstanceQuery.class.equals(queryClass))
                     {
                        predicate.add(ProcessInstanceQuery.OID.isEqual(((Number) filterValue).intValue()));
                     }
                  }
                  else
                  {
                     // For multiple ENUM's 'OR' term is formed, dataFilter is null
                     if (dataFilter == null && dataFilterOrTerm == null)
                     {
                        if (trace.isDebugEnabled())
                        {
                           trace.debug("Performing equal filter with filter value " + filterValue);
                        }
                        if (isStructuredData(mapping))
                        {
                           if (mapping.getModelElement() instanceof DataPath)
                           {
                              dataFilter = (DataFilter) DataFilter.isEqual(mapping.getDataId(),
                                    ((DataPath) mapping.getModelElement()).getAccessPath(), filterValue);
                           }
                        }
                        else
                        {
                           dataFilter = DataFilter.isEqual(mapping.getDataId(), filterValue);
                        }
                     }
                     if(dataFilter != null)
                     {
                        predicate.add(dataFilter);   
                     }
                  }
               }
               else
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Skipping descriptor filter as of empty filter value.");
                  }
               }
            }
         }
      }
   }

   /**
    * @param query
    * @param descriptorItems
    * @param commonDescriptors
    */
   public static void evaluateAndApplyFilters(Query query, List<DataMappingWrapper> descriptorItems,
         DataPath[] commonDescriptors)
   {
      evaluateAndApplyFilters(query, descriptorItems, commonDescriptors, true);
   }

   /**
    * @param query
    * @param descriptorItems
    * @param commonDescriptors
    */
   public static void evaluateAndApplyFilters(Query query, List<DataMappingWrapper> descriptorItems,
         DataPath[] commonDescriptors, boolean matchAll)
   {
      GenericDescriptorFilterModel filterModel = null;

      if (!CollectionUtils.isEmpty(descriptorItems) && null != descriptorItems)
      {
         filterModel = GenericDescriptorFilterModel.create(commonDescriptors);
         filterModel.setFilterEnabled(true);

         for (Iterator<DataMappingWrapper> iterator = descriptorItems.iterator(); iterator.hasNext();)
         {
            DataMappingWrapper dmWrapper = (DataMappingWrapper) iterator.next();
            DataMapping dataFilter = dmWrapper.getDataMapping();
            if (!(dataFilter instanceof GenericDataMapping))
            {
               dataFilter = new GenericDataMapping(dataFilter);
            }
            // For ENUM, Set of descriptor values are set for dataId, create Filter using List
            if (dmWrapper.getValueList().size() > 1)
            {
               filterModel.setFilterValues(dataFilter.getId(), dmWrapper.getValueList());
            }
            else if (dmWrapper.getValue() != null)
            {
               filterModel.setFilterValue(dataFilter.getId(), (Serializable) dmWrapper.getValue());
            }
         }
      }
      if (null != descriptorItems && !CollectionUtils.isEmpty(descriptorItems))
      {
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
         applyFilters(query, filterModel, matchAll);
      }
   }

   /**
    * @param query
    * @param filterModel
    */
   public static void applyFilters(Query query, IFilterModel filterModel)
   {
      applyFilters(query, filterModel, true);
   }

   /**
    * @param query
    * @param filterModel
    */
   public static void applyFilters(Query query, IFilterModel filterModel, boolean matchAll)
   {
      DescriptorFilterProvider filterProvider = new DescriptorFilterProvider();
      filterProvider.setFilterId(DescriptorFilterProvider.FILTER_ID);
      filterProvider.setFilterModel(filterModel);
      filterProvider.setFilterTerm(matchAll ? query.getFilter().addAndTerm() : query.getFilter().addOrTerm());
      filterProvider.applyFilter(query);

   }

   /**
    * returns String filter for provided datapath and value
    * 
    * @author Yogesh.Manware
    * @param dataPath
    * @param filterValue
    * @return
    */
   public static DataFilter getStringFilter(DataPath dataPath, Object filterValue, boolean caseSensitive)
   {
      return getStringFilter(null, dataPath, filterValue, caseSensitive);
   }
   
   /**
    * 
    * @param mapping
    * @param dataPath
    * @param filterValue
    * @param caseSensitive
    * @return
    */
   public static DataFilter getStringFilter(GenericDataMapping mapping, DataPath dataPath, Object filterValue, boolean caseSensitive)
   {
      DataFilter dataFilter = null;
      boolean isCaseDescriptor = isCaseDescriptor(dataPath);
      String dataId = isCaseDescriptor ? dataPath.getId() : getData(dataPath).getQualifiedId();

      // Check if it is a character
      if (Character.class.equals(dataPath.getMappedType()))
      {
         char charValue = ' ';
         if (filterValue instanceof Character)
         {
            charValue = ((Character) filterValue).charValue();
         }
         else if (filterValue != null && !StringUtils.isEmpty((String) filterValue))
         {
            charValue = ((String) filterValue).charAt(0);

         }
         if (!Character.isSpaceChar(charValue))
         {
            if (CommonDescriptorUtils.isStructuredData(dataPath))
            {
               // Grab the xpath here.......
               String xPath = dataPath.getAccessPath();
               dataFilter = (DataFilter) DataFilter.isEqual(dataId, xPath, charValue);
            }
            else
            {
               dataFilter = DataFilter.isEqual(dataId, charValue);
            }
         }
      }// For String type
      else if (String.class.equals(dataPath.getMappedType()) || dataPath.getMappedType() instanceof Class<?>)
      {
         if (filterValue instanceof String && !StringUtils.isEmpty((String) filterValue))
         {
            String filterString = (String) filterValue;

            String filterValueStr = (String) filterValue;

            filterValueStr = filterValueStr.replace('*', '%').trim();
            if (trace.isDebugEnabled())
            {
               trace.debug("Performing like filter with filter value " + filterString);
            }
            if (isCaseDescriptor)
            {
               dataFilter=DataFilter.likeCaseDescriptor(dataId, filterValueStr);
            }
            else
            {
               if (CommonDescriptorUtils.isStructuredData(dataPath))
               {
                  if (null != mapping && CommonDescriptorUtils.isEnumerationType(mapping))
                  {
                     dataFilter = DataFilter.isEqual(dataId, dataPath.getAccessPath(), filterValueStr);
                  }
                  else
                  {
                     // Grab the xpath here.......
                     String xPath = dataPath.getAccessPath();
                     dataFilter = DataFilter.like(dataId, xPath, filterValueStr, caseSensitive);                     
                  }
               }
               else
               {
                  if (CommonDescriptorUtils.isEnumerationPrimitive(dataPath))
                  {
                     dataFilter = DataFilter.isEqual(dataId, filterValueStr);
                  }
                  else
                  {
                     dataFilter = DataFilter.like(dataId, filterValueStr, caseSensitive);
                  }
               }
            }
         }
         else if(filterValue instanceof Integer)
         {
            dataFilter = DataFilter.isEqual(dataId,(Integer) filterValue);
         }
      }
      return dataFilter;
   }

   /**
    * returns boolean filter for provided datapath and value
    * 
    * @author Yogesh.Manware
    * @param dataPath
    * @param filterValue
    * @return
    */
   public static DataFilter getBooleanFilter(DataPath dataPath, Object filterValue)
   {
      DataFilter dataFilter = null;

      if (filterValue != null && filterValue instanceof Boolean)
      {
         Boolean booleanValue = (Boolean) filterValue;
         String dataId = isCaseDescriptor(dataPath) ? dataPath.getId() : getData(dataPath).getQualifiedId();
         if (CommonDescriptorUtils.isStructuredData(dataPath))
         {
            // Grab the xpath here.......
            String xPath = dataPath.getAccessPath();
            dataFilter = (DataFilter) DataFilter.isEqual(dataId, xPath, booleanValue);
         }
         else
         {
            dataFilter = DataFilter.isEqual(dataId, booleanValue);
         }
      }
      return dataFilter;
   }

   /**
    * return number filter for the provided datapath
    * 
    * @author Yogesh.Manware
    * @param dataPath
    * @param from
    * @param to
    * @return
    */
   private static DataFilter getNumberFilter(DataPath dataPath, Number numberValue)
   {
      String dataId = isCaseDescriptor(dataPath) ? dataPath.getId() : getData(dataPath).getQualifiedId();
      DataFilter dataFilter = null;

      if (numberValue != null)
      {
         if (CommonDescriptorUtils.isStructuredData(dataPath))
         {
            dataFilter = (DataFilter) DataFilter.isEqual(dataId, dataPath.getAccessPath(), numberValue);
         }
         else
            dataFilter = DataFilter.isEqual(dataId, numberValue);
      }
      return dataFilter;
   }

   /**
    * return number range filter for the provided datapath
    * 
    * @author Yogesh.Manware
    * @param dataPath
    * @param from
    * @param to
    * @return
    */
   private static DataFilter getNumberRangeFilter(DataPath dataPath, Number from, Number to)
   {
      String dataId = isCaseDescriptor(dataPath) ? dataPath.getId() : getData(dataPath).getQualifiedId();
      DataFilter dataFilter = null;

      if (from != null && to != null)
      {
         if (CommonDescriptorUtils.isStructuredData(dataPath))
         {
            dataFilter = DataFilter.between(dataId, dataPath.getAccessPath(), from, to);
         }
         else
            dataFilter = DataFilter.between(dataId, from, to);
      }
      else if (to != null)
      {
         if (CommonDescriptorUtils.isStructuredData(dataPath))
         {
            dataFilter = DataFilter.lessOrEqual(dataId, dataPath.getAccessPath(), to);
         }
         else
            dataFilter = DataFilter.lessOrEqual(dataId, to);
      }
      else if (from != null)
      {
         if (CommonDescriptorUtils.isStructuredData(dataPath))
         {
            dataFilter = DataFilter.greaterOrEqual(dataId, dataPath.getAccessPath(), from);
         }
         else
            dataFilter = DataFilter.greaterOrEqual(dataId, from);
      }
      return dataFilter;
   }

   /**
    * 
    * @return
    */
   public static DataFilter getDateFilter(DataPath dataPath, Serializable value)
   {
      DataFilter dataFilter = null;

      // for String
      if (Character.class.equals(dataPath.getMappedType()) || String.class.equals(dataPath.getMappedType()))
      {
         dataFilter = getStringFilter(dataPath, value, false);
      }// for boolean
      else if (Boolean.class.equals(dataPath.getMappedType()))
      {
         dataFilter = getBooleanFilter(dataPath, (Boolean) value);
      }
      // for single number
      else if (value instanceof Number)
      {
         Number filterValueNumber = getNumberFilterValue(dataPath.getMappedType(), (Number) value);
         dataFilter = DataFilter.isEqual(getData(dataPath).getQualifiedId(), filterValueNumber);
      }
      else
      {
         dataFilter = DataFilter.isEqual(getData(dataPath).getQualifiedId(), value);
      }

      return dataFilter;
   }
   
   /**
    * return date filter for the provided datapath of type date
    * 
    * @author Yogesh.Manware
    * @param dataPath
    * @param fromTime
    * @param toTime
    * @return
    */
   private static DataFilter getDateFilter(DataPath dataPath, DateRange dateRange)
   {
      String dataId = getData(dataPath).getQualifiedId();
      DataFilter dataFilter = null;
      Serializable fromDateValue = dateRange.getFromDateValue();
      Serializable toDateValue = dateRange.getToDateValue();
      
      if (PredefinedConstants.BUSINESS_DATE.equals(dataPath.getData()))
      {
         dataId = PredefinedConstants.BUSINESS_DATE;
         Calendar cal = Calendar.getInstance();
         if (dateRange.getFromDateValue() != null)
         {
            cal.setTime(dateRange.getFromDateValue());
            cal.set(Calendar.HOUR_OF_DAY, 00);
            cal.set(Calendar.MINUTE, 0);
            fromDateValue = cal;
         }
         if (dateRange.getToDateValue() != null)
         {
            cal = Calendar.getInstance();
            cal.setTime(dateRange.getToDateValue());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            toDateValue = cal;
         }
      }

      if (fromDateValue != null && toDateValue != null)
      {
         if (CommonDescriptorUtils.isStructuredData(dataPath))
         {
            dataFilter = DataFilter.between(dataId, dataPath.getAccessPath(), fromDateValue, toDateValue);
         }
         else
         {
            dataFilter = DataFilter.between(dataId, fromDateValue, toDateValue);
         }
      }
      else if (toDateValue != null)
      {
         if (CommonDescriptorUtils.isStructuredData(dataPath))
         {
            dataFilter = DataFilter.lessOrEqual(dataId, dataPath.getAccessPath(), toDateValue);
         }
         else
         {
            dataFilter = DataFilter.lessOrEqual(dataId, toDateValue);
         }
      }
      else if (fromDateValue != null)
      {
         if (CommonDescriptorUtils.isStructuredData(dataPath))
         {
            dataFilter = DataFilter.greaterOrEqual(dataId, dataPath.getAccessPath(), fromDateValue);
         }
         else
         {
            dataFilter = DataFilter.greaterOrEqual(dataId, fromDateValue);
         }
      }
      return dataFilter;
   }

   /**
    * @param mapping
    * @param filterValue
    * @return
    */
   private static Number getNumberFilterValue(GenericDataMapping mapping, Number filterValue)
   {
      Number returnValue = null;
      if (null != filterValue)
      {
         Class< ? > mappedType = mapping.getMappedType();
         try
         {
            if (Byte.class.equals(mappedType))
            {
               returnValue = Byte.valueOf(filterValue.byteValue());
            }
            else if (Short.class.equals(mappedType))
            {
               returnValue = Short.valueOf(filterValue.shortValue());
            }
            else if (Integer.class.equals(mappedType))
            {
               returnValue = Integer.valueOf(filterValue.intValue());
            }
            else if (Long.class.equals(mappedType))
            {
               returnValue = Long.valueOf(filterValue.longValue());
            }
            else if (Float.class.equals(mappedType))
            {
               returnValue = Float.valueOf(filterValue.floatValue());
            }
            else if (Double.class.equals(mappedType))
            {
               returnValue = Double.valueOf(filterValue.doubleValue());
            }
         }
         catch (ClassCastException e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Invalid filter value for '" + mapping.getId() + "'. Value is '" + filterValue
                     + "' and should be a type of '" + mappedType + "'");
            }
         }
      }
      return returnValue;
   }
   
   /**
    * @param mapping
    * @param filterValue
    * @return
    */
   public static Number getNumberFilterValue(Class< ? > mappedType, Number filterValue)
   {
      Number returnValue = null;
      if (null != filterValue)
      {        
        
            if (Byte.class.equals(mappedType))
            {
               returnValue = Byte.valueOf(filterValue.byteValue());
            }
            else if (Short.class.equals(mappedType))
            {
               returnValue = Short.valueOf(filterValue.shortValue());
            }
            else if (Integer.class.equals(mappedType))
            {
               returnValue = Integer.valueOf(filterValue.intValue());
            }
            else if (Long.class.equals(mappedType))
            {
               returnValue = Long.valueOf(filterValue.longValue());
            }
            else if (Float.class.equals(mappedType))
            {
               returnValue = Float.valueOf(filterValue.floatValue());
            }
            else if (Double.class.equals(mappedType))
            {
               returnValue = Double.valueOf(filterValue.doubleValue());
            }

      }
      return returnValue;
   }
   

   /**
    * @param mapping
    * @return
    */
   private static boolean isStructuredData(GenericDataMapping mapping)
   {
      ModelElement modelElement = mapping.getModelElement();
      if (modelElement instanceof DataPath)
      {
         Model model = ModelCache.findModelCache().getModel(modelElement.getModelOID());
         Data data = model.getData(mapping.getDataId());
         if (data instanceof DataDetails)
         {
            DataDetails dataDetails = (DataDetails) data;
            String typeId = dataDetails.getTypeId();
            if (StructuredDataConstants.STRUCTURED_DATA.equals(typeId))
            {
               return true;
            }
         }

      }
      return false;
   }
   
   /**
    * 
    * @param descriptorItems
    * @param filter
    * @return
    */
   public static FilterAndTerm createCaseDescriptors(List<DataMappingWrapper> descriptorItems, FilterAndTerm filter)
   {
      for (Iterator<DataMappingWrapper> iterator = descriptorItems.iterator(); iterator.hasNext();)
      {
         DataMappingWrapper dmWrapper = (DataMappingWrapper) iterator.next();
         DataMapping dataFilter = dmWrapper.getDataMapping();
         Object value = (Serializable) dmWrapper.getValue();
         if (null == value || (value instanceof String && StringUtils.isEmpty(value.toString())))
         {
            continue;
         }
         else
         {
            if (PredefinedConstants.CASE_DESCRIPTION_ELEMENT.equals(dataFilter.getId()))
            {
               String newValue = "%" + value + "%";
               filter.add(DataFilter.likeCaseDescriptor(dataFilter.getId(), newValue));
            }
            else
            {
               filter.add(DataFilter.equalsCaseDescriptor(dataFilter.getId(), value));
            }
         }
      }
      return filter;
   }

   public static class DataPathMetadata
   {
      public TypedXPath typedXPath;

      private boolean sortable;

      private boolean filterable;
      
      private boolean structured;

      private String xPath;

      public String getxPath()
      {
         return xPath;
      }

      public DataPathMetadata()
      {
         sortable = filterable = structured = false;
      }

      public DataPathMetadata(boolean sortable, boolean filterable)
      {
         this.sortable = sortable;
         this.filterable = filterable;
      }

      public boolean isSortable()
      {
         return sortable;
      }

      public boolean isFilterable()
      {
         return filterable;
      }
      
      public boolean isStructured()
      {
         return structured;
      }
      
      public boolean isEnum()
      {
         if (typedXPath != null)
         {
            return typedXPath.isEnumeration();
         }
         return false;
      }

      /**
       * @return
       */
      public List<String> getEnumValues()
      {
         List<String> enumList = null;
         if (isEnum())
         {
            return typedXPath.getEnumerationValues();
         }
         return enumList;
      }

      public TypedXPath getTypedXPath()
      {
         return typedXPath;
      }
   }

   public static boolean isCaseDescriptor(DataPath dataPath)
   {
      if (PredefinedConstants.CASE_DATA_ID.equals(dataPath.getData()))
      {
         return true;
      }
      return false;
   }
   
   /**
    * @param dataPath
    * @return Data
    */
   public static Data getData(DataPath dataPath)
   {
      if (null != dataPath && !DescriptorColumnUtils.isCompositeOrLinkDescriptor(dataPath))
      {
         Model model = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(dataPath.getModelOID());
         return model.getData(dataPath.getData());
      }
      return null;
   }

   public static Object convertDataPathValue(Class dataClass, Object dataPathValue) throws Exception
   {
      Object value = null;
      try
      {
         if (dataClass == Long.class || dataClass == Integer.class || dataClass == Short.class
               || dataClass == Byte.class || dataClass == Float.class || dataClass == Double.class
               || dataClass == BigDecimal.class)
         {
            value = convertToNumber(dataPathValue, dataClass);
         }
         else if (dataClass == Boolean.class)
         {
            value = Boolean.valueOf(dataPathValue.toString());
         }
         else if (dataClass == Date.class || dataClass == Calendar.class)
         {
            if (dataPathValue instanceof Date)
            {
               value = getDateValue((Date) dataPathValue, dataClass);
            }
            if (dataPathValue instanceof Calendar)
            {
               value = getDateValue((Date) dataPathValue, dataClass);
            }
            else if (dataPathValue instanceof String)
            {
               Date dateValue = DateUtils.parseDateTime(dataPathValue.toString());
               if (null == dateValue)
               {
                  try
                  {
                     Long dateLongValue = Long.valueOf(dataPathValue.toString());
                     dateValue = new Date(dateLongValue);
                  }
                  catch(NumberFormatException e)
                  {
                     trace.info("Date not in Long format :: ",e);
                     dateValue = DateUtils.parseDateTime(dataPathValue.toString(), DateUtils.getDateFormat(), Locale.getDefault(),
                           TimeZone.getDefault());
                  }
                  
               }
               value = getDateValue(dateValue, dataClass);
            }
            else if (dataPathValue instanceof Long)
            {
               Long longValue = (Long) dataPathValue;
               Date dateValue = new Date(longValue);
               value = getDateValue(dateValue, dataClass);
            }
         }
         else if(dataClass == String.class)
         {
            value = dataPathValue.toString();
         }
         else
         {
            value = dataPathValue;
         }
      }
      catch (Exception e)
      {
         throw e;
      }
      return value;
   
   }

   public static Object getDateValue(Date value, Class mappedClass)
   {
      Object valueToSet = value;
      if (mappedClass == Calendar.class)
      {
         Calendar cal = Calendar.getInstance();
         cal.clear();
         cal.setTime(value);
         valueToSet = cal.getTime();
      }
      return valueToSet;
   }

   public static Number convertToNumber(Object value, Class type) throws Exception
   {
      Number localValue = null;
      if (value != null && StringUtils.isNotEmpty(value.toString()))
      {
         try
         {
            String strVal = value.toString();
            if (type == Long.class)
            {
               localValue = new Long(strVal);
            }
            if (type == Integer.class)
            {
               localValue = new Integer(strVal);
            }
            else if (type == Short.class)
            {
               localValue = new Short(strVal);
            }
            else if (type == Byte.class)
            {
               localValue = new Byte(strVal);
            }
            else if (type == Double.class)
            {
               localValue = new Double(strVal);
            }
            else if (type == Float.class)
            {
               localValue = new Float(strVal);
            }
            else if (type == BigDecimal.class)
            {
               localValue = new BigDecimal(strVal);
            }
         }
         catch (Exception e)
         {
            throw e;
         }
      }
      return localValue;
   }

}