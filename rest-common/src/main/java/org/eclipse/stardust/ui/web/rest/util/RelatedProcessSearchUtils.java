package org.eclipse.stardust.ui.web.rest.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
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
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DataMappingWrapper;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

public class RelatedProcessSearchUtils {

	private static final int MAX_RESULT = 20;
	
	public static List<ProcessInstance> getProcessInstances(List<ProcessInstance> sourceProcessInstances, boolean matchAny, boolean searchCases) {
		boolean isSourceCase = false;
		Map<String, DataPath> keyDescriptors;
		Map<String, Object> sourceDescriptors;
		if (null != sourceProcessInstances && ProcessInstanceUtils.isCaseProcessInstances(sourceProcessInstances))
        {
           isSourceCase = true;
           ProcessInstanceDetails casePIDetails = (ProcessInstanceDetails) sourceProcessInstances.get(0);
           keyDescriptors = CommonDescriptorUtils.findCaseSourceDescriptors(casePIDetails);
           sourceDescriptors = casePIDetails.getDescriptors();
        }
        else
        {
           keyDescriptors = CommonDescriptorUtils.getKeyDescriptorsIntersectionMap(sourceProcessInstances);
           sourceDescriptors = getSourceDescriptors(sourceProcessInstances, keyDescriptors);
        }
		
		// if key descriptors is empty then no need to create n fire query
		if (CollectionUtils.isNotEmpty(keyDescriptors))
        {
           // to search Process(es)/Case(es),atleast one key descriptor must contain non empty value.
           if (!isEmptyDescriptors(keyDescriptors.values(), sourceDescriptors))
           {
        	   ProcessInstanceQuery query = createQuery(sourceProcessInstances, keyDescriptors, sourceDescriptors, searchCases, isSourceCase, matchAny);
       		   return ServiceFactoryUtils.getQueryService().getAllProcessInstances(query);
           }
        }
		return Collections.emptyList();
	}
	
	private static boolean isEmptyDescriptors(final Collection<DataPath> datas, final Map<String, Object> sourceDescriptors) {
		for (DataPath path : datas) {
			if (sourceDescriptors.containsKey(path.getId())) {
				Object value = sourceDescriptors.get(path.getId());
				if (null != value && !(value instanceof String)) {
					return false;
				} else if (value instanceof String
						&& StringUtils.isNotEmpty(value.toString())) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
    * 
    * @return
    */
   private static Map<String, Object> getSourceDescriptors(List<ProcessInstance> sourceProcessInstances, Map<String, DataPath> keyDescriptors)
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
    * method create ProcessInstanceQuery,it filter-out source process instance query only
    * fetch process instance if key descriptor match (any or all)
    * 
    * @return ProcessInstanceQuery
    */
   private static ProcessInstanceQuery createQuery(List<ProcessInstance> sourceProcessInstances, Map<String, DataPath> keyDescriptors,
		   Map<String, Object> sourceDescriptors, boolean searchCases, boolean isSourceCase, boolean matchAny)
   {
      ProcessInstanceQuery piQuery = null;
      Collection<DataPath> datas = keyDescriptors.values();

      if (searchCases)
      {
         piQuery = createQueryCaseSearch(sourceProcessInstances, datas, sourceDescriptors, matchAny);
      }
      else
      {
         if (!isSourceCase)
         {
            piQuery = createQueryPIToPISearch(sourceProcessInstances, datas, sourceDescriptors, matchAny);
         }
         else
         {
            piQuery = createQueryCaseToPISearch(sourceProcessInstances, datas, sourceDescriptors, matchAny);
         }
      }

      return piQuery;
   }
   
   /**
    * 
    * @param datas
    * @return
    */
   private static ProcessInstanceQuery createQueryCaseSearch(List<ProcessInstance> sourceProcessInstances, Collection<DataPath> datas,
		   Map<String, Object> sourceDescriptors, boolean matchAny)
   {
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findCases();
      piQuery.getFilter().add(new ProcessStateFilter(ProcessInstanceState.Active));
      excludeSourceProcesses(piQuery, sourceProcessInstances);
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
   private static ProcessInstanceQuery createQueryCaseToPISearch(List<ProcessInstance> sourceProcessInstances, Collection<DataPath> datas,
		   Map<String, Object> sourceDescriptors, boolean matchAny)
   {
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAlive();
      piQuery.getFilter().and(ProcessInstanceHierarchyFilter.ROOT_PROCESS);
      SubsetPolicy newPolicy = new SubsetPolicy(MAX_RESULT, 0, false);
      piQuery.setPolicy(newPolicy);
      piQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      excludeSourceProcesses(piQuery, sourceProcessInstances);

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
   private static ProcessInstanceQuery createQueryPIToPISearch(List<ProcessInstance> sourceProcessInstances, Collection<DataPath> datas,
		   Map<String, Object> sourceDescriptors, boolean matchAny)
   {
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAlive();
      piQuery.getFilter().and(ProcessInstanceHierarchyFilter.ROOT_PROCESS);

      SubsetPolicy newPolicy = new SubsetPolicy(MAX_RESULT, 0, false);
      piQuery.setPolicy(newPolicy);
      piQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      excludeSourceProcesses(piQuery, sourceProcessInstances);

      List<DataMappingWrapper> descriptorItems = createDataMappingWrapper(datas, sourceDescriptors);
      DataPath[] dataArrays = datas.toArray(new DataPath[datas.size()]);
      DescriptorFilterUtils.evaluateAndApplyFilters(piQuery, descriptorItems, dataArrays, !matchAny);
      return piQuery;
   }
   
   /**
    * 
    * @param query
    */
   private static void excludeSourceProcesses(Query query, List<ProcessInstance> sourceProcessInstances)
   {
      FilterAndTerm filter = query.getFilter().addAndTerm();

      for (ProcessInstance processInstance : sourceProcessInstances)
      {
         filter.and(ProcessInstanceQuery.OID.notEqual(processInstance.getOID()));
      }
   }
   
   /**
    * Evaluate the descriptors to be displayed
    */
   private static List<DataMappingWrapper> createDataMappingWrapper(Collection<DataPath> dataPaths, Map<String, Object> sourceDescriptors)
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
               if (value instanceof Character)
               {
                  value = value.toString();
               }
               
               if (value instanceof String && StringUtils.isNotEmpty(value.toString()))
               {
                  dmWrapper.setValue(value);
               }
               else if (!(value instanceof String))
               {
                  dmWrapper.setValue(value);
               }
            }
         }
      }

      return list;
   }
}
