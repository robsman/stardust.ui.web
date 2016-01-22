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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.dto.DataPathDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.Reference;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.XPathAnnotations;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.plugin.support.ServiceLoaderUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DataMappingWrapper;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils.DataPathMetadata;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentInfo;
import org.eclipse.stardust.ui.web.viewscommon.spi.descriptor.ISemanticalDescriptorComparator;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.TypedDocument;



/**
 * @author yogesh.manware
 * 
 */
public class CommonDescriptorUtils
{
   private static final Logger trace = LogManager.getLogger(CommonDescriptorUtils.class);
   
   
   /**
    * Returns process instance's descriptors list taking workflow configurations into
    * consideration
    * 
    * @param processInstance
    * @return
    */
   public static List<ProcessDescriptor> createProcessDescriptors(ProcessInstance processInstance)
   {
      ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) processInstance;
      ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
            processInstance.getProcessID());
      return createProcessDescriptors(processInstanceDetails.getDescriptors(), processDefinition,
            isSuppressBlankDescriptorsEnabled());
   }
   
   
   /**
    * @param processInstance
    * @param evaluateBlankDescriptors
    * @return
    */
   public static List<ProcessDescriptor> createProcessDescriptors(ProcessInstance processInstance,
         boolean evaluateBlankDescriptors)
   {
      ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) processInstance;
      ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
            processInstance.getProcessID());
      return createProcessDescriptors(processInstanceDetails.getDescriptors(), processDefinition,
            evaluateBlankDescriptors);
   }

   /**
    * method to check if object type contain empty value
    * add more types if required
    * @param value
    * @return
    */
   private static boolean isEmpty(Object value)
   {
      if (null == value)
      {
         return true;
      }
      else if (value instanceof List && CollectionUtils.isEmpty((List<?>) value))
      {
         return true;
      }
      else
      {
         return StringUtils.isEmpty(String.valueOf(value));
      }
   }
   
   public static String getDocumentIcon(String fileName, String contentType)
   {
      return MimeTypesHelper.detectMimeType(fileName, contentType).getIcon();
   }
   
   /**
    * 
    * @param descriptorValues
    * @param instance
    */
   public static void updateProcessDocumentDescriptors(Map<String, Object> descriptorValues, ProcessInstance instance,
         ProcessDefinition processDefinition)
   {
      if (CollectionUtils.isEmpty(descriptorValues))
      {
         return;
      }
      try
      {
         Map<String, DataPathDetails> datapathMap = getDatapathMap(processDefinition);
         Map<String, TypedDocument> typedDocumentsData = new HashMap<String, TypedDocument>();
         Map<String, DataPath> outDataMappings = new HashMap<String, DataPath>();
         Model model = ModelUtils.getModel(instance.getModelOID());
         TypedDocument typedDocument;
         String dataDetailsQId;
         trace.debug("Inside Update Document Descriptors");
         for (Entry<String, DataPathDetails> entry : datapathMap.entrySet())
         {
            DataPathDetails dataPathDetails = entry.getValue();
            if (dataPathDetails.isDescriptor())
            {
               Object obj = descriptorValues.get(entry.getKey());
               if (null != obj)
               {
                  DataDetails dataDetails = (DataDetails) model.getData(dataPathDetails.getData());
                  if (obj instanceof Collection< ? >)
                  {
                     List<DocumentInfo> documentList = CollectionUtils.newArrayList();
                     List<Object> documents = (List<Object>) obj;
                     for (Object doc : documents)
                     {
                        if (doc instanceof DocumentInfo)
                        {
                           documentList.add((DocumentInfo) doc);
                        }
                        else if (doc instanceof Document)
                        {
                           Document processAttachment = (Document) doc;
                           documentList.add(new DocumentInfo(getDocumentIcon(processAttachment.getName(),
                                 processAttachment.getContentType()), processAttachment));
                        }
                     }
                     descriptorValues.put(entry.getKey(), documentList);
                  }
                  else if (null != dataDetails && DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataDetails.getTypeId()))
                  {
                     dataDetailsQId = dataDetails.getQualifiedId();
                     Direction direction = dataPathDetails.getDirection();
                     if (Direction.IN.equals(direction) && !typedDocumentsData.containsKey(dataDetailsQId))
                     {
                        try
                        {
                           if (obj instanceof Document)
                           {
                              typedDocument = new TypedDocument(instance, dataPathDetails, dataDetails, (Document) obj);
                              if (outDataMappings.containsKey(dataDetailsQId))
                              {
                                 typedDocument.setDataPath(outDataMappings.get(dataDetailsQId));
                                 typedDocument.setOutMappingExist(true);
                              }
                              typedDocumentsData.put(dataDetailsQId, typedDocument);
                           }
                        }
                        catch (Exception e)
                        {
                           trace.error(e);
                        }
                     }
                     else if (Direction.OUT.equals(direction))
                     {
                        if (typedDocumentsData.containsKey(dataDetailsQId))
                        {
                           typedDocument = typedDocumentsData.get(dataDetailsQId);
                           if (!typedDocument.isOutMappingExist())
                           {
                              typedDocument.setDataPath(dataPathDetails);
                              typedDocument.setOutMappingExist(true);
                           }
                        }
                        else
                        {
                           outDataMappings.put(dataDetailsQId, dataPathDetails);
                        }
                     }
                  }
               }
            }
         }
         if (!CollectionUtils.isEmpty(typedDocumentsData))
         {
            List<TypedDocument> typedDocumentList = new ArrayList<TypedDocument>(typedDocumentsData.values());
            for (TypedDocument typedDoc : typedDocumentList)
            {
               String dataPathId = typedDoc.getDataPath().getId();
               Object value = descriptorValues.get(dataPathId);
               DocumentInfo documentInfo;
               if (value == null)
               {
                  continue;
               }
               Document document = typedDoc.getDocument();
               String icon = null;
               if (null != document)
               {
                  icon = getDocumentIcon(document.getName(), document.getContentType());
                  documentInfo = new DocumentInfo(icon, document);
               }
               else
               {
                  icon = ResourcePaths.I_EMPTY_CORE_DOCUMENT;
                  documentInfo = new DocumentInfo(icon, typedDoc);
               }
               descriptorValues.put(dataPathId, documentInfo);
            }
         }
         trace.debug("Update Document Descriptors complete");
      }
      catch (Exception e)
      {
         trace.error("Error while updating Process Document Descriptors", e);
      }
   }
   
   /**
    * @param descriptors
    * @param processDefinition
    * @param evaluateBlankDescriptors
    * @return
    */
   @SuppressWarnings("unchecked")
public static List<ProcessDescriptor> createProcessDescriptors(Map<String, Object> descriptors,
         ProcessDefinition processDefinition, boolean evaluateBlankDescriptors,boolean includeDocuments)
   {
      trace.debug("Inside Create Process Descriptors");
      //escape special character to avoid UI
      descriptors = escapeDescriptors(descriptors);
      boolean suppressBlankDescriptors = false;
      List<ProcessDescriptor> processDescriptors = new ArrayList<ProcessDescriptor>();
      
      if (evaluateBlankDescriptors)
      {
         if (isSuppressBlankDescriptorsEnabled())
         {
            suppressBlankDescriptors = true;
         }
      }
      try
      {
         Map<String, DataPathDetails> datapathMap = getDatapathMap(processDefinition);

         ProcessDescriptor processDescriptor ;
         for (Entry<String, DataPathDetails> entry : datapathMap.entrySet())
         {
            Object descriptorValue = descriptors.get(entry.getKey());
            if (suppressBlankDescriptors && isEmpty(descriptorValue))
            {
               // filter out the descriptor
            }
            else
            {
               DataPathDetails dataPathDetails = entry.getValue();
               Model model = ModelCache.findModelCache().getModel(dataPathDetails.getModelOID());
               DataDetails dataDetails = model != null ? (DataDetails) model.getData(dataPathDetails.getData()) : null;
               if ((DmsConstants.DATA_ID_ATTACHMENTS.equals(dataPathDetails.getData()))
                     || (null != dataDetails && (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataDetails.getTypeId()) || DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST
                           .equals(dataDetails.getTypeId()))))
               {

            	   if (includeDocuments) {
            		   List<DocumentInfo> documents = new ArrayList<DocumentInfo>();
            		   if(descriptorValue instanceof DocumentInfo){
            			   documents.add((DocumentInfo) descriptorValue);
            		   }else if(descriptorValue instanceof List){
            			   documents = (List<DocumentInfo>) descriptorValue;
            		   }
            		   ProcessDocumentDescriptor docDescriptor = new ProcessDocumentDescriptor(
            		         entry.getKey(),
            		         I18nUtils.getDataPathName(entry.getValue()), null, documents);
            		   processDescriptors.add(docDescriptor);
            	   } else {
            		   continue;
            	   }
               }
               else
               {
                  try
                  {
                  // Format numeric descriptor values with thousand's seperator if
                     // supported by
                     // underlying data
                     if (dataPathDetails.isDescriptor())
                     {
                        Model refModel = model;
                        Data data = null;
                        if (!isEmpty(descriptorValue))
                        {
                           if (null != dataDetails
                                 && StructuredDataConstants.STRUCTURED_DATA.equals(dataDetails.getTypeId()))
                           {
                              Reference ref = dataDetails.getReference();
                              if (null == ref)
                              {
                                 data = model.getData(dataPathDetails.getData());
                                 ref = data.getReference();
                              }
                              if(null != ref)
                              {
                                 if (ref.getModelOid() != model.getModelOID())
                                 {
                                    refModel = ModelCache.findModelCache().getModel(ref.getModelOid());
                                 }
                              }
                              else
                              {
                                 if(data.getModelOID() != model.getModelOID())
                                 {
                                    refModel = ModelCache.findModelCache().getModel(data.getModelOID());
                                 }
                              }
                              Class dataClass = dataPathDetails.getMappedType();
                              IXPathMap xPathMap = ClientXPathMap.getXpathMap(refModel, dataDetails);
                              String xPath = StructuredDataXPathUtils.getXPathWithoutIndexes(dataPathDetails
                                    .getAccessPath());
                              TypedXPath typedXPath = xPathMap.getXPath(xPath);
                              if (null != typedXPath)
                              {
                                 Number value = getNumericValue(dataClass, descriptorValue);
                                 if (value == null && "decimal".equals(typedXPath.getXsdTypeName()))
                                 {
                                    value = new BigDecimal(descriptorValue.toString());
                                 }
                                 if (value != null)
                                 {
                                    XPathAnnotations annotation = typedXPath.getAnnotations();
                                    if (annotation != null)
                                    {
                                       String descriptorLabelValue = annotation.getElementValue(
                                             XPathAnnotations.IPP_ANNOTATIONS_NAMESPACE, new String[] {
                                                   "ui", Constants.INPUT_PREFERENCES_NUMBER_GROUP_KEY_LABEL});
                                       if (null != descriptorLabelValue && Boolean.valueOf(descriptorLabelValue))
                                       {
                                          String numberValue = formatNumberInLocale(value);
                                          descriptors.put(entry.getKey(), numberValue);
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
                  catch (Exception e)
                  {
                     trace.error("Error occured while adding Thousands Seperator", e);
                  }
                  GenericDataMapping mapping = new GenericDataMapping(dataPathDetails);
                  DataMappingWrapper dmWrapper = new DataMappingWrapper(mapping, null, false);

                  processDescriptor = new ProcessDescriptor(entry.getKey(), I18nUtils.getDataPathName(entry.getValue()),
                        formatDescriptorValue(descriptors.get(entry.getKey()), dmWrapper.getType()));
                  processDescriptors.add(processDescriptor);
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.error("Error occured at create Process Descriptors", e);
      }
      return processDescriptors;
   }

   /**
    * 
    * @param descriptors
    * @param processDefinition
    * @param evaluateBlankDescriptors
    * @return
    */
	public static List<ProcessDescriptor> createProcessDescriptors(
			Map<String, Object> descriptors,
			ProcessDefinition processDefinition,
			boolean evaluateBlankDescriptors) {
		return createProcessDescriptors(descriptors, processDefinition,
				evaluateBlankDescriptors, false);
	}
   /**
    * @param processInstanceOID
    * @param dataPath
    * @return
    */
   public static Object getInDataPath(long processInstanceOID, String dataPath)
   {
      WorkflowService service = SessionContext.findSessionContext().getServiceFactory().getWorkflowService();
      return service.getInDataPaths(processInstanceOID, null).get(dataPath);
   }

   
   /**
    * Returns an array with all descriptors which exists in the given processes.
    * Furthermore you have the option to get only descriptors that are filterable.
    * Descriptors are ordered by the order of processes and the order of descriptors in
    * the modeller.
    * 
    * @param processes
    *            List of all processes for which the descriptors should be resolved
    * @param onlyFilterable
    *            Restrict the descriptors to get only filterable items or not
    * @return Descriptors for the given processes
    */
   public static DataPath[] getAllDescriptors(List<ProcessDefinition> processes, 
         boolean onlyFilterable)
   {
      List<DataPath> allDescriptors = new ArrayList<DataPath>();
      for (Iterator<ProcessDefinition> iter = processes.iterator(); iter.hasNext();)
      {
         ProcessDefinition process = iter.next();

         for (Iterator descrItr = process.getAllDataPaths().iterator(); descrItr
               .hasNext();)
         {
            DataPath path = (DataPath) descrItr.next();
            if (Direction.IN.equals(path.getDirection()) && path.isDescriptor()
                  && ((onlyFilterable && DescriptorFilterUtils.isDataFilterable(path)) || !onlyFilterable))
            {
               allDescriptors.add(path);
            }
         }
      }
      DataPath[] descriptors = allDescriptors.toArray(new DataPath[0]);
      return descriptors;
   }

 
   /**
    * @param onlyFilterable
    * @return
    */
   public static Map<String, DataPath> getAllDescriptors(boolean onlyFilterable)
   {
      List<ProcessDefinition> allProcessDefinitions = ProcessDefinitionUtils.getAllAccessibleProcessDefinitionsfromAllVersions();
      DataPath[] descriptorsList = getAllDescriptors(allProcessDefinitions, onlyFilterable);
      Map<String, DataPath> allDescriptors = new TreeMap<String, DataPath>();
      for (DataPath dataPath : descriptorsList)
      {
         if (dataPath.isDescriptor())
         {
            if (!allDescriptors.containsKey(dataPath.getId()))
            {
               allDescriptors.put(dataPath.getId(), dataPath);
            }
         }
      }
      return allDescriptors;
   }

 
   /**
    * @param process
    * @param onlyFilterable
    * @return
    */
   public static Map<DataPath, DataPathMetadata> getAllDescriptorsWithMetadata(ProcessDefinition process, boolean onlyFilterable)
   {
      Map<DataPath, DataPathMetadata> allDescriptors = new HashMap<DataPath, DataPathMetadata>();

      for (Iterator descrItr = process.getAllDataPaths().iterator(); descrItr.hasNext();)
      {
         DataPath path = (DataPath) descrItr.next();
         if (Direction.IN.equals(path.getDirection()) && path.isDescriptor())
         {
            DataPathMetadata metadata = DescriptorFilterUtils.getDataPathMetadata(path);

            if (!onlyFilterable || metadata.isFilterable())
            {
               allDescriptors.put(path, metadata);
            }
         }
      }

      return allDescriptors;
   }
   
   /**
    * Default value will be true.
    * @param prefScope
    * @return
    */
   public static boolean isSuppressBlankDescriptorsEnabled()
   {
      UserPreferencesHelper userPrefHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON,
            PreferenceScope.USER);
      return userPrefHelper.getBoolean(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_SUPPRESS_BLANK_DESCRIPTORS, true);
   }

   /**
    * @param processDefinition
    * @return
    */
   private static Map<String, DataPathDetails> getDatapathMap(ProcessDefinition processDefinition)
   {
      List<DataPathDetails> dataPaths = processDefinition.getAllDataPaths();
      Map<String, DataPathDetails> datapathMap = new LinkedHashMap<String, DataPathDetails>();
      DataPathDetails dataPathDetails;
      int size = dataPaths.size();
      for (int i = 0; i < size; i++)
      {
         dataPathDetails = (DataPathDetails) dataPaths.get(i);
         if (dataPathDetails.isDescriptor())
         {
            datapathMap.put(dataPathDetails.getId(), dataPathDetails);
         }
      }
      return datapathMap;
   }   
   
   /**
    * Returns an array with all COMMON descriptors which exists in the given processes.
    * Furthermore you have the option to get only descriptors that are filterable.
    * Descriptors are ordered by the order of processes and the order of descriptors in
    * the modeller.
    * 
    * @param processes
    *           List of all processes for which the descriptors should be resolved
    * @param onlyFilterable
    *           Restrict the descriptors to get only filterable items or not
    * @return Common descriptors for the given processes
    */
   public static DataPath[] getCommonDescriptors(List<ProcessDefinition> processes, boolean onlyFilterable)
   {
      boolean firstProcess = true;
      ISemanticalDescriptorComparator comparator = getSemanticalDescriptorComparator();
      // We have to use this type of Map because of the predictable order of the keys
      Map<String, DataPath> allDescriptors = new LinkedHashMap<String, DataPath>();
      for (int i = 0; i < processes.size(); ++i)
      {
         ProcessDefinition process = processes.get(i);
   
         Set<String> commonDescriptors = new HashSet<String>();
   
         for (Iterator descrItr = process.getAllDataPaths().iterator(); descrItr.hasNext();)
         {
            DataPath path = (DataPath) descrItr.next();
            if (Direction.IN.equals(path.getDirection()) && path.isDescriptor()
                  && ((onlyFilterable && DescriptorFilterUtils.isDataFilterable(path)) || !onlyFilterable))
            {
               if (firstProcess)
               {
                  allDescriptors.put(path.getId(), path);
                  commonDescriptors.add(path.getId());
               }
               else
               {
                  DataPath other = (DataPath) allDescriptors.get(path.getId());
                  if (null != other)
                  {
                     if (comparator.compare(path, other) == 0)
                     {
                        commonDescriptors.add(path.getId());
                     }
                  }
               }
            }
         }
         // get rid of all descriptors not common with the current process
         allDescriptors.keySet().retainAll(commonDescriptors);
   
         firstProcess = false;
      }
      DataPath[] descriptors = (DataPath[]) allDescriptors.values().toArray(new DataPath[0]);
      return descriptors;
   }

   
   /**
    * @return SemanticalDescriptorComparator
    */
   public static ISemanticalDescriptorComparator getSemanticalDescriptorComparator()
   {
      Iterator<ISemanticalDescriptorComparator> serviceProviders = ServiceLoaderUtils
            .searchProviders(ISemanticalDescriptorComparator.class);

      ISemanticalDescriptorComparator semanticalDescriptorComparator = null;

      if (null != serviceProviders)
      {
         while (serviceProviders.hasNext())
         {
            semanticalDescriptorComparator = serviceProviders.next();
            if (null != semanticalDescriptorComparator)
            {
               return semanticalDescriptorComparator;
            }
         }
      }
      return null;
   }
   
   
   /**
    * Evaluates if a data path is a structured data.
    * 
    * @param dataPath
    *           <code>DataPath</code> that is to be evaluated
    * @return true if it is a structured data
    */
   public static boolean isStructuredData(DataPath dataPath)
   {
      Model model = ModelCache.findModelCache().getModel(dataPath.getModelOID());
      if (model != null)
      {
         Data data = model.getData(dataPath.getData());
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
    * @param dataPath
    * @return
    */
   public static boolean isEnumerationPrimitive(DataPath dataPath)
   {
      try
      {
         Model model = ModelCache.findModelCache().getModel(dataPath.getModelOID());
         if (model != null)
         {
            Data data = model.getData(dataPath.getData());
            Object carnotType = data.getAttribute("carnot:engine:type");
            if(carnotType != null && carnotType.equals(ProcessPortalConstants.ENUM_TYPE))
            {
                  return true;                  
            }
         }
      }
      catch (Exception e)
      {
         return false;
      }
      return false;
   }
   
  /**
   * 
   * @param dataMapping
   * @return
   */
   public static boolean isEnumerationType(DataMapping dataMapping)
   {
      boolean isEnum = false;

      Set<TypedXPath> xpaths = ModelUtils.getXPaths(dataMapping);
      for (TypedXPath path : xpaths)
      {
         if (path.getParentXPath() == null)
         {
            isEnum = path.isEnumeration();
            break;
         }
      }
      return isEnum;
   }
   
   public static Number getNumericValue(Class type, Object obj)
   {
      Number val = null;
      String value = obj.toString();
      if (type == Integer.class)
      {
         val = Integer.valueOf(value);
      }
      else if (type == Long.class)
      {
         val = Long.valueOf(value);
      }
      else if (type == Short.class)
      {
         val = Short.valueOf(value);
      }
      else if (type == Byte.class)
      {
         val = Byte.valueOf(value);
      }
      else if (type == Float.class)
      {
         val = Float.valueOf(value);
      }
      else if (type == Double.class)
      {
         val = Double.valueOf(value);
      }
      else if (type == BigDecimal.class)
      {
         double doubleValue= Double.valueOf(value.toString());
         val = BigDecimal.valueOf(doubleValue);
      }
      return val;
   }
   
   public static String formatNumberInLocale(Number number)
   {
      NumberFormat numberFormatter;
      numberFormatter = NumberFormat.getNumberInstance(FacesUtils.getLocaleFromRequest());
      return numberFormatter.format(number);
   }
   
   /**
    * Format Descriptors based on their types
    * 
    * @param valueObj
    * @param dateType
    * @return
    */
   public static String formatDescriptorValue(Object valueObj, String dateType)
   {
      String value = "";
      if (valueObj instanceof Date || valueObj instanceof Calendar)
      {
         Date dateValue = valueObj instanceof Calendar ? ((Calendar) valueObj).getTime() : (Date) valueObj;

         if (StringUtils.isNotEmpty(dateType))
         {
            if (dateType.equalsIgnoreCase(ProcessPortalConstants.DATE_TYPE))
            {
               value = DateUtils.formatDate((Date) valueObj, java.util.TimeZone.getDefault());
            }
            else if (dateType.equalsIgnoreCase(ProcessPortalConstants.TIMESTAMP_TYPE))
            {
               value = DateUtils.formatDateTime(dateValue);
            }
         }
         if (StringUtils.isEmpty(value))
         {
            value = DateUtils.formatDateTime(dateValue);
         }
      }
      // Added for I18N of boolean descriptors
      else if (valueObj instanceof Boolean)
      {
         MessagePropertiesBean props = MessagePropertiesBean.getInstance();
         value = (Boolean) valueObj ? props.getString("common.true") : props.getString("common.false");
      }
      else
      {
         value = valueObj != null ? valueObj.toString() : "";
      }
      return value;
   }

   /**
    * method to escape characters for descriptor ,those are not support to display
    * properly in Portal UI
    * 
    * @return
    */
   public static Map<String, Object> escapeDescriptors(Map<String, Object> descriptors)
   {
      for (Entry<String, Object> entry : descriptors.entrySet())
      {
         Object value = entry.getValue();
         String key = entry.getKey();
         if (value instanceof Character)
         {
            Character character = ((Character) value);
            char ch = character.charValue();
            if (ch == 0)
            {
               descriptors.put(key, "");
            }
            else
            {
               descriptors.put(key, String.valueOf(character).trim());
            }
         }         
      }
      return descriptors;
   }
  
   /**
    * 
    * @param sourceProcessInstances
    * @return
    */
   public static Map<String, DataPath> getKeyDescriptorsIntersectionMap(List<ProcessInstance> sourceProcessInstances)
   {
      Map<String, DataPath> keyDescriptorsMap = CollectionUtils.newHashMap();

      List<ProcessDefinition> commonProcessDefinitions = CollectionUtils.newArrayList();
      for (ProcessInstance processInstance : sourceProcessInstances)
      {
         ProcessDefinition pd = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
               processInstance.getProcessID());
         commonProcessDefinitions.add(pd);
      }   
      DataPath[] commonDatas = getKeyDescriptorsIntersection(commonProcessDefinitions);

      for (DataPath dataPath : commonDatas)
      {
         keyDescriptorsMap.put(dataPath.getId(), dataPath);
      }
      return keyDescriptorsMap;
   }

   /**
    * 
    * @param processes
    * @return
    */

   public static DataPath[] getKeyDescriptorsIntersection(List<ProcessDefinition> processes)
   {
      boolean firstProcess = true;

      // We have to use this type of Map because of the predictable order of the keys
      Map<String, DataPath> intersectionMap = new LinkedHashMap<String, DataPath>();

      for (ProcessDefinition pd : processes)
      {
         // for first process put all key descriptor in Map
         if (firstProcess)
         {
            firstProcess = false;

            for (Object item : pd.getAllDataPaths())
            {
               DataPath path = (DataPath) item;
               if (null != path && path.isKeyDescriptor() && Direction.IN.equals(path.getDirection()))
               {
                  intersectionMap.put(path.getId(), path);
               }
            }

         }
         // from second process on words check intersectionMap descriptors present in
         // process
         // if not present then remove from map
         else
         {
            for (Iterator<DataPath> itr = intersectionMap.values().iterator(); itr.hasNext();)
            {
               boolean contains = false;
               DataPath first = itr.next();

               for (Object item : pd.getAllDataPaths())
               {
                  DataPath second = (DataPath) item;
                  if (null != second && second.isKeyDescriptor() && Direction.IN.equals(second.getDirection())
                        && equalsDescriptor(first, second))
                  {
                     contains = true;
                     break;
                  }
               }
               // if descriptor is not present then remove from map
               if (!contains)
               {
                  itr.remove();
               }
            }
         }

      }

      DataPath[] descriptors = (DataPath[]) intersectionMap.values().toArray(new DataPath[0]);
      return descriptors;
   }
   /**
    * 
    * @param first
    * @param secound
    * @return
    */
   private static boolean equalsDescriptor(DataPath first, DataPath secound)
   {
      if (null != first && null != secound && first.getId().equals(secound.getId()))
      {
         Data data1 = DescriptorFilterUtils.getData(secound);
         Data data2 = DescriptorFilterUtils.getData(first);
         if (data1.equals(data2))
         {
            return true;
         }
      }
      return false;
   }
   
   
   /**
    * 
    * @param sourceProcessInstance
    * @return descriptor defined as key descriptor
    */
   public static Map<String, DataPath> getKeyDescriptors(ProcessInstance processInstance)
   {

      ProcessDefinition pd = ProcessInstanceUtils.getProcessDefination(processInstance);
      List<DataPath> dataList = pd.getAllDataPaths();
      Map<String, DataPath> keyDescriptors = CollectionUtils.newHashMap();
      for (DataPath path : dataList)
      {
         if (path.isKeyDescriptor() && Direction.IN.equals(path.getDirection()) 
               && DescriptorFilterUtils.isDataFilterable(path))
         {
            keyDescriptors.put(path.getId(), path);
         }
      }
      return keyDescriptors;

   }

   /**
    * method calculate descriptor for Case instance
    * @param casePi
    * @param membersPi
    */
   @SuppressWarnings("rawtypes")
   public static void reCalculateCaseDescriptors(ProcessInstance casePi)
   {
      Map<String, Object> descriptorMap = CollectionUtils.newHashMap();
      Map<String, Pair<Class, Object>> descriptors = CollectionUtils.newTreeMap();

      List<ProcessInstance> children = ProcessInstanceUtils.findChildren(casePi);
      List<ProcessDefinition> commonProcessDefinitions = CollectionUtils.newArrayList();

      for (ProcessInstance childPi : children)
      {
         ProcessDefinition pd = ProcessDefinitionUtils.getProcessDefinition(childPi.getModelOID(),
               childPi.getProcessID());
         commonProcessDefinitions.add(pd);
      }
     // DataPath[] commonDatas = getCommonDescriptors(commonProcessDefinitions, true);
      DataPath[] commonDatas = getKeyDescriptorsIntersection(commonProcessDefinitions);    
      
      for (DataPath dataPath : commonDatas)
      {
         if (dataPath.isKeyDescriptor())
         {
            Pair<Class, Object> firstTypeValuePair = null;
            for (ProcessInstance childPi : children)
            {
               Object value = ((ProcessInstanceDetails) childPi).getDescriptorValue(dataPath.getId());
               if (null != value)
               {
                  Class mappedType = dataPath.getMappedType();
                  Pair<Class, Object> typeValuePair = new Pair<Class, Object>(mappedType, value);

                  if (null != typeValuePair && firstTypeValuePair == null)
                  {
                     firstTypeValuePair = typeValuePair;
                     descriptors.put(dataPath.getId(), firstTypeValuePair);
                  }
                  else if (null != typeValuePair && null != firstTypeValuePair
                        && !CompareHelper.areEqual(typeValuePair, firstTypeValuePair))
                  {
                     descriptors.remove(dataPath.getId());
                  }
               }
               else
               {
                  descriptors.remove(dataPath.getId());
               }
            }
         }

      }    
      
      Set<Entry<String, Pair<Class, Object>>> entrySet = descriptors.entrySet();
      for (Entry<String, Pair<Class, Object>> entry : entrySet)
      {
         Object value = entry.getValue().getSecond();
         if (value == null || (value instanceof String && "".equals(value.toString())))
         {
            continue;
         }
         descriptorMap.put(entry.getKey(), entry.getValue().getSecond());
      }
      // remove old values by setting null for data id
      List<DataPath> descDefinations = ((ProcessInstanceDetails) casePi).getDescriptorDefinitions();
      if (CollectionUtils.isNotEmpty(descDefinations))
      {
         for (DataPath path : descDefinations)
         {
            if (!descriptorMap.containsKey(path.getId()) & !(PredefinedConstants.CASE_NAME_ELEMENT.equals(path.getId()) || PredefinedConstants.CASE_DESCRIPTION_ELEMENT.equals(path.getId())))
            {
               descriptorMap.put(path.getId(), null);
            }
         }
      }
      
      ServiceFactoryUtils.getWorkflowService().setOutDataPaths(casePi.getOID(), descriptorMap);
   }
   
   /**
    * Returns case instance's descriptors list taking workflow configurations into
    * consideration
    * 
    * @param processInstance
    * @return
    */
   public static List<ProcessDescriptor> createCaseDescriptors(ProcessInstance processInstance)
   {
      ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) processInstance;
      ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
            processInstance.getProcessID());
      return createCaseDescriptors(processInstanceDetails.getDescriptorDefinitions(),
            processInstanceDetails.getDescriptors(), processDefinition, isSuppressBlankDescriptorsEnabled());
   }

   /**
    * 
    * @param descriptors
    * @param descriptorList
    * @param processDefinition
    * @param evaluateBlankDescriptors
    * @return
    */
   public static List<ProcessDescriptor> createCaseDescriptors(List<DataPath> descriptors,
         Map<String, Object> descriptorsMap, ProcessDefinition processDefinition, boolean evaluateBlankDescriptors)
   {
      descriptorsMap= escapeDescriptors(descriptorsMap);
      boolean suppressBlankDescriptors = false;
      List<ProcessDescriptor> processDescriptors = new ArrayList<ProcessDescriptor>();
      ProcessDescriptor processDescriptor;

      if (evaluateBlankDescriptors)
      {
         if (isSuppressBlankDescriptorsEnabled())
         {
            suppressBlankDescriptors = true;
         }
      }

      for (DataPath dp : descriptors)
      {     
         Object val = descriptorsMap.get(dp.getId());

         if (suppressBlankDescriptors
               && (null == val || StringUtils.isEmpty(String.valueOf(val))))
         {
            // filter out the descriptor
         }
         else
         {          
            // Read the DataPath Id key value from descriptorList 
            
            processDescriptor = new ProcessDescriptor(dp.getId(), I18nUtils.getDataPathName(dp), formatDescriptorValue(val, dp
                  .getMappedType().toString()));
            processDescriptors.add(processDescriptor);
         }
      }
      return processDescriptors;
   }
   
   /**
    * Update the GroupName or Description descriptor which is displayed as CaseName and
    * Descriptor.
    * 
    * @param pi
    * @param descriptorValue
    * @param descriptionChange
    *           true: to update Description value, false : to update CaseName
    */
   public static void updateCaseDescriptor(ProcessInstance pi, Object descriptorValue, boolean descriptionChange)
   {
      if (!descriptionChange)
      {
         ServiceFactoryUtils.getWorkflowService().setOutDataPath(pi.getOID(), PredefinedConstants.CASE_NAME_ELEMENT,
               descriptorValue);
      }
      else
      {
         ServiceFactoryUtils.getWorkflowService().setOutDataPath(pi.getOID(),
               PredefinedConstants.CASE_DESCRIPTION_ELEMENT, descriptorValue);
      }
   }
   
   /**
    * method find and returns source Descriptors if Case Instance  
    * @param caseProcessInstance
    * @return Map<String, DataPath> 
    */
   public static Map<String, DataPath> findCaseSourceDescriptors(ProcessInstance caseProcessInstance)
   {
      Map<String, DataPath> dataPathMap = CollectionUtils.newHashMap();
      ProcessInstanceDetails casePIDetails = (ProcessInstanceDetails) caseProcessInstance;
      Map<String, Object> caseDescriptors = casePIDetails.getDescriptors();
      Map<String, DataPath> caseDescriptorDefinitions = getCaseDescriptorDefinitions(caseProcessInstance);

      if (CollectionUtils.isNotEmpty(caseDescriptors))
      {
         List<ProcessInstance> subProcesses = ProcessInstanceUtils.findChildren(caseProcessInstance);

         for (ProcessInstance pi : subProcesses)
         {
            List<DataPath> datas = ((ProcessInstanceDetails) pi).getDescriptorDefinitions();
            for (DataPath path : datas)
            {
               DataPath caseDataPath = caseDescriptorDefinitions.get(path.getId());
               if (path.isKeyDescriptor() && !dataPathMap.containsKey(path.getId()) && null != caseDataPath
                     && caseDataPath.getMappedType().equals(path.getMappedType()))
               {
                  dataPathMap.put(path.getId(), path);
               }
            }

         }
      }
      return dataPathMap;
   }

   /**
    * 
    * @param caseProcessInstance
    */
   public static Map<String, DataPath> getCaseDescriptorDefinitions(ProcessInstance caseProcessInstance)
   {
      Map<String, DataPath> datas = CollectionUtils.newHashMap();
      
      ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) caseProcessInstance;
      List<DataPath> descList = processInstanceDetails.getDescriptorDefinitions();
      for (DataPath path : descList)
      {
         datas.put(path.getId(), path);
      }
      return datas;
   }
}