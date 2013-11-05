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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelElement;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.Resource;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.dms.data.AuditTrailUtils;
import org.eclipse.stardust.engine.extensions.xml.data.XPathUtils;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.DateRange;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalErrorClass;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;
import org.eclipse.stardust.ui.web.viewscommon.common.model.IInputFieldChangeListener;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IGenericInputField;
import org.eclipse.stardust.ui.web.viewscommon.common.structureddata.ComplexTypeWrapper;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;


public class DataMappingWrapper implements IGenericInputField, Serializable
{
   protected final static Logger trace = LogManager.getLogger(DataMappingWrapper.class);

   private static final long serialVersionUID = 1l;

   private DataMapping dataMapping;
   private boolean readOnly;
   private Object value;
   private String type;
   private DateRange dateRangeValue;
   private String distinctiveId = null;
   private List<SelectItem> enumList = CollectionUtils.newArrayList();

   // these are only set in case of structured data (collection values: List, Map)
   private transient ComplexTypeWrapper structuredValue;

   // TODO extend to multiple observers
   private IInputFieldChangeListener changeListener;

   public DataMappingWrapper(DataMapping dataMapping, Object defaultValue)
   {
      this(dataMapping, defaultValue, true);
   }

   public DataMappingWrapper(DataMapping dataMapping, Object defaultValue, boolean readOnly)
   {
      this.dataMapping = dataMapping;
      this.readOnly = readOnly;
      // since finding out the type requires iterations, cache the result of determineType()
      this.type = determineType();
      this.dateRangeValue = new DateRange();

      if (ProcessPortalConstants.STRUCTURED_TYPE.equals(getType()))
      {
         DataDetails data = getDataDetails();

         Object unwrappedValue = defaultValue;
         if (StringUtils.isEmpty(dataMapping.getDataPath()))
         {
            unwrappedValue = AuditTrailUtils.unwrap(defaultValue, data);
         }
         this.structuredValue = new ComplexTypeWrapper(dataMapping, unwrappedValue);
         // get initialized value back
         this.value = structuredValue.getComplexType();
      }
      else
      {
         value = defaultValue;
      }
   }
   
   public String getType()
   {
      return this.type;
   }

   public String determineType()
   {
      String dataTypeId = getDataDetails().getTypeId();
      String type = null;
      Class dataClass = dataMapping != null ? dataMapping.getMappedType() : null;
      if(dataClass == Boolean.class)
      {
         type = ProcessPortalConstants.BOOLEAN_TYPE;
      }
      else if(dataClass == Date.class || dataClass == Calendar.class)
      {
         if (StructuredTypeRtUtils.isStructuredType(dataTypeId) || StructuredTypeRtUtils.isDmsType(dataTypeId))
         {
            Model model = ModelCache.findModelCache().getModel(getDataDetails().getModelOID());
            // for structured data, there are "date", "dateTime" and "time"
            // for "time", a textfield should be displayed
            IXPathMap xPathMap = ClientXPathMap.getXpathMap(model, model.getData(dataMapping.getDataId()));
            String xPath = StructuredDataXPathUtils.getXPathWithoutIndexes(dataMapping.getDataPath());
            if (xPathMap.getXPath(xPath).getXsdTypeName().equals(ProcessPortalConstants.XSD_TIME_TYPE_NAME))
            {
               type = ProcessPortalConstants.TIME_TYPE;
            }
            else if (xPathMap.getXPath(xPath).getXsdTypeName().equals(ProcessPortalConstants.DATE_TYPE))
            {
               type = ProcessPortalConstants.DATE_TYPE;
            }
            else
            {
               type = ProcessPortalConstants.TIMESTAMP_TYPE;
            }
         }
         else
         {
            // for primitives, there is no "time" type, so TIMESTAMP_TYPE is always correct
            type = ProcessPortalConstants.TIMESTAMP_TYPE;
         }
      }
      else if(dataClass == Long.class || dataClass == Integer.class || dataClass == Short.class || dataClass == Byte.class)
      {
         type = ProcessPortalConstants.LONG_TYPE;
         if(dataMapping.getDataId().equals("PROCESS_PRIORITY"))
         {
            type = ProcessPortalConstants.PRIORITY_TYPE;
         }
      }
      else if(dataClass == Float.class || dataClass == Double.class)
      {
         type = ProcessPortalConstants.DOUBLE_TYPE;
      }
      else if(dataClass == String.class || dataClass == Character.class)
      {
         Object carnotType = getDataDetails().getAttribute("carnot:engine:type");
         if (carnotType != null && carnotType.equals(ProcessPortalConstants.ENUM_TYPE))
         {
            populateEnumList();
            type = ProcessPortalConstants.ENUM_TYPE;
         }
         else
         {
            if (dataTypeId.equals("struct"))
            {
               populateEnumList();
               if(!enumList.isEmpty())
               {
                  type = ProcessPortalConstants.ENUM_TYPE;   
               }
               else
               {
                  type = ProcessPortalConstants.STRING_TYPE;
               }
            }
            else
            {
               type = ProcessPortalConstants.STRING_TYPE;
            }
                        
         }
      }
      else if (dataClass == Map.class || dataClass == List.class
            || Resource.class.isAssignableFrom(dataClass))
      {
         if (StructuredTypeRtUtils.isStructuredType(dataTypeId) || StructuredTypeRtUtils.isDmsType(dataTypeId))
         {
            // Only for Maps and Lists values structured data.
            // "STRUCTURED_TYPE" should not be returned for serializables.
            type = ProcessPortalConstants.STRUCTURED_TYPE;
         }
      }
      return type;
   }

   /**
    * 
    */
   private void populateEnumList()
   {
      enumList.clear();
      Set<TypedXPath> xpathMap = XPathUtils.getXPaths(ModelCache.findModelCache().getModel(dataMapping.getModelOID()),
            dataMapping);
      Iterator<TypedXPath> xpathIterator = xpathMap.iterator();
      while (xpathIterator.hasNext())
      {
         TypedXPath xPath = xpathIterator.next();
         if (xPath.isEnumeration())
         {
            List<String> enumValList = xPath.getEnumerationValues();
            for (String enumVal : enumValList)
            {
               enumList.add(new SelectItem(enumVal, enumVal));
            }
         }
      }
   }
   
   private DataDetails getDataDetails()
   {
      Model model = ModelCache.findModelCache().getModel(dataMapping.getModelOID());
      Data data = model.getData(dataMapping.getDataId());

      return (DataDetails)data;
   }

   public boolean isDisabled()
   {
      return readOnly;
   }

   public void addChangeListener(IInputFieldChangeListener changeListener)
   {
      this.changeListener = changeListener;
   }

   public DataMapping getDataMapping()
   {
      return dataMapping;
   }

   public void setReadOnly(boolean readOnly)
   {
      this.readOnly = readOnly;
   }
   public void setDefaultValue(Object defaultValue)
   {
      value = defaultValue;
   }
   public Object getValue()
   {
      if (ProcessPortalConstants.STRUCTURED_TYPE.equals(getType()) && StringUtils.isEmpty(dataMapping.getDataPath()))
      {
         return AuditTrailUtils.wrap(value, getDataDetails());
      }
      if (value instanceof DateRange)
      {
         value = ((DateRange) value).getFromDateValue() == null
               && ((DateRange) value).getToDateValue() == null ? null : value;
         return value;
      }
      else
      {
         return value;
      }
   }

   public String getMappedType()
   {
      Class mappedType = dataMapping != null ? dataMapping.getMappedType() : null;
      return mappedType != null ? mappedType.getName() : null;
   }

   public String getConvertorId()
   {
      // Time Stamp
      if (type.equals(ProcessPortalConstants.TIMESTAMP_TYPE))
      {
         return "customDateTimeConverter";
      }
      // Date
      else if (type.equals(ProcessPortalConstants.DATE_TYPE))
      {
         return "customDateConverter";
      }
      else
      // Time
      {
         return "customTimeConverter";
      }
   }

   public String getLabel()
   {
      ModelElement modelElement = dataMapping;
      if(dataMapping instanceof GenericDataMapping)
      {
         modelElement = ((GenericDataMapping)dataMapping).getModelElement();
      }
      return I18nUtils.getLabel(modelElement,
            modelElement != null ? dataMapping.getName() : "");
   }

   public Boolean getBooleanValue()
   {
      return (value instanceof Boolean) ? (Boolean) value : null;
   }

   public void booleanValueChangeListener(ValueChangeEvent event)
   {
      if (event.getNewValue() != null)
      {
         setBooleanValueStr(event.getNewValue().toString());
      }
      else
      {
         setBooleanValueStr(null);
      }
   }

   public void setBooleanValueStr(String value)
   {
      if (StringUtils.isNotEmpty(value))
      {
         this.value = Boolean.valueOf(value);
         broadcastChange(this.value);
      }
      else
      {
         this.value = null;
      }
      trace.debug("set Boolean: " + this.value);
   }

   public String getBooleanValueStr()
   {
      if (this.value != null)
      {
         return this.value.toString();
      }
      return "";
   }

   public String getStringValue()
   {
      return value != null ? value.toString() : null;
   }
   public void setStringValue(String value)
   {
      Object castedValue = null;
      Class dataClass = dataMapping != null ? dataMapping.getMappedType() : null;
      // cast if Character
      if (dataClass == Character.class && !StringUtils.isEmpty(value))
      {
         Character ch = new Character(value.toCharArray()[0]);
         castedValue = ch;
      }
      else
      {
         castedValue = value;
      }
      broadcastChange(castedValue);

      this.value = castedValue;
      trace.debug("set String: " + castedValue);
   }

   public List<SelectItem> getEnumList()
   {
      return enumList;
   }

   public void enumValueChangeListener(ValueChangeEvent event)
   {
      if (event.getNewValue() != null)
      {
         setEnumValueStr(event.getNewValue().toString());
      }
      else
      {
         setEnumValueStr(null);
      }
   }

   public String getEnumValueStr()
   {
      if (this.value != null)
      {
         return this.value.toString();
      }
      return "";
   }

   public void setEnumValueStr(String value)
   {
      if (StringUtils.isNotEmpty(value))
      {
         this.value = value;
         broadcastChange(this.value);
      }
      else
      {
         this.value = null;
      }
      trace.debug("set ENUM: " + this.value);
   }
   
   public Long getLongValue()
   {
      try
      {
         return value != null ? Long.valueOf(value.toString()) : null;
      }
      catch (Exception e)
      {
         return null;
      }
   }
   public void setLongValue(Long value)
   {
      broadcastChange(value);

      Class dataClass = dataMapping.getMappedType();
      this.value = convertToNumber(value, dataClass);
      trace.debug("set Long: " + this.value);
   }

   public Double getDoubleValue()
   {
      try
      {
         return value != null ? Double.valueOf(value.toString()) : null;
      }
      catch (Exception e)
      {
         return null;
      }
   }
   public void setDoubleValue(Double value)
   {
      broadcastChange(value);

      Class dataClass = dataMapping.getMappedType();
      this.value = convertToNumber(value, dataClass);
      trace.debug("set Double: " + this.value);
   }

   /**
    * method determine type and set value as per type
    * @param value
    */
   public void setValue(Object value)
   {
      if ("Long".equals(type))
      {
         if (value instanceof Long)
         {
            setLongValue((Long) value);
         }
         else if(value instanceof Integer)
         {
            setLongValue( Long.valueOf(((Integer)value).intValue()));
         }
         else if (value instanceof String)
         {
            setLongValue(Long.parseLong((String)value));
         }
      }
      else if ("Double".equals(type))
      {
         if(value instanceof Double)
         {
            setDoubleValue((Double) value);
         }
         else if (value instanceof String)
         {
            setDoubleValue(Double.parseDouble((String)value));
         }
      }
      else if ("String".equals(type) && value instanceof String)
      {
         setStringValue( (String)value);
      }
      else if ("Boolean".equals(type))
      {
         if (value instanceof Boolean)
         {
            setBooleanValue((Boolean) value);
         }
         else if (value instanceof String)
         {
            setBooleanValue(Boolean.parseBoolean((String)value));
         }
      }
      else if ("Date".equals(type))
      {
         if(value instanceof Date)
         {
            setDateValue((Date) value);
         }
         else if (value instanceof String)
         {
            setDateValue(DateUtils.parseDateTime((String)value));
         }
      }
      else if (null != value && (value instanceof Character || value instanceof String))
      {
         setStringValue(value.toString());
      }
      else
      {
         this.value = value;
      }
   }

   private Number convertToNumber(Number value, Class type)
   {
      Number localValue = null;
      if(value != null)
      {
         try
         {
            if(type == Long.class)
            {
               localValue = new Long(value.longValue());
            }
            if(type == Integer.class)
            {
               localValue = new Integer(value.intValue());
            }
            else if(type == Short.class)
            {
               localValue = new Short(value.shortValue());
            }
            else if(type == Byte.class)
            {
               localValue = new Byte(value.byteValue());
            }
            else if(type == Double.class)
            {
               localValue = new Double(value.doubleValue());
            }
            else if(type == Float.class)
            {
               localValue = new Float(value.floatValue());
            }
         }
         catch (Exception e)
         {
            FacesMessage facesMsg = ExceptionHandler.getFacesMessage(
                  new PortalException(PortalErrorClass.UNABLE_TO_CONVERT_DATAMAPPING_VALUE, e));

            throw new ValidatorException(facesMsg);
         }
      }
      return localValue;
   }

   public Date getDateValue()
   {
      Date returnValue = null;
      if (value instanceof Calendar)
      {
         returnValue = ((Calendar) value).getTime();
      }
      else if (value instanceof Date)
      {
         returnValue = (Date) value;
      }
      return returnValue;
   }

   public void setDateValue(Date value)
   {
      broadcastChange(value);

      Object valueToSet = value;
      if(dataMapping.getMappedType() == Calendar.class)
      {
         Calendar cal = Calendar.getInstance();
         cal.clear();
         cal.setTime(value);
         valueToSet = cal;
      }
      this.value = valueToSet;
      trace.debug("set Date: " + this.value);
   }

   public Date getToDateValue()
   {
      return dateRangeValue.getToDateValue();
   }

   public Date getFromDateValue()
   {
      return dateRangeValue.getFromDateValue();
   }

   public void setToDateValue(Date toDateValue)
   {
        dateRangeValue.setToDateValue(toDateValue);
        this.value = dateRangeValue;
   }

   public void setFromDateValue(Date fromDateValue)
   {
        dateRangeValue.setFromDateValue(fromDateValue);
        this.value = dateRangeValue;
   }

   public ComplexTypeWrapper getStructuredValue() {
      return this.structuredValue;
   }

   private void broadcastChange(Object newValue)
   {
      if (null != changeListener)
      {
         changeListener.inputFieldValueChanged(this.value, newValue);
      }
   }

   protected static DataMappingWrapper[] getDescriptors(List dataPaths, IDescriptorProvider descriptorProvider)
   {
      DataMappingWrapper[] descriptors = null;
      List mappings = GenericDataMapping.generateGenericDataMappingFromDataPaths(
            dataPaths, true);
      if ((null != mappings) && !mappings.isEmpty())
      {
         descriptors = new DataMappingWrapper[mappings.size()];
      }
      for (int i = 0; i < mappings.size(); ++i)
      {
         GenericDataMapping mapping = (GenericDataMapping) mappings.get(i);
         Object descriptorValue = descriptorProvider.getDescriptorValue(
               mapping.getModelElement().getId());;
         descriptors[i] = new DataMappingWrapper(mapping, descriptorValue, true);
         if(descriptorValue == null)
         {  // Fix bug in DataMappingWrapper if the DataMapping is a structured data
            descriptors[i].setDefaultValue(null);
         }
      }
      return descriptors;
   }

   public static DataMappingWrapper[] getDescriptors(final ProcessInstance processInstance)
   {
      DataMappingWrapper[] descriptors = null;
      if (processInstance != null)
      {
         ModelCache modelCache = ModelCache.findModelCache();
         Model model = modelCache != null ?
               modelCache.getModel(processInstance.getModelOID()) : null;
         ProcessDefinition process = model != null ? model.getProcessDefinition(
               processInstance.getProcessID()) : null;
         List dataPaths = process != null ? process.getAllDataPaths() : Collections.EMPTY_LIST;
         if(processInstance instanceof IDescriptorProvider)
         {
            descriptors = getDescriptors(dataPaths, (IDescriptorProvider)processInstance);
         }
      }
      return descriptors;
   }

   public static DataMappingWrapper[] getDescriptors(final ActivityInstance activityInstance)
   {
      DataMappingWrapper[] descriptors = null;
      if (activityInstance != null)
      {
         ModelCache modelCache = ModelCache.findModelCache();
         Model model = modelCache != null ?
               modelCache.getModel(activityInstance.getModelOID()) : null;
         ProcessDefinition process = model != null ? model.getProcessDefinition(
               activityInstance.getProcessDefinitionId()) : null;
         List dataPaths = process != null ? process.getAllDataPaths() : Collections.EMPTY_LIST;
         descriptors = getDescriptors(dataPaths, new IDescriptorProvider() {

            public Object getDescriptorValue(String id)
            {
               return activityInstance.getDescriptorValue(id);
            }

            public List<DataPath> getDescriptorDefinitions()
            {
               return activityInstance.getDescriptorDefinitions();
            }

         });
      }
      return descriptors;
   }

   public void setBooleanValue(Boolean value)
   {
   }

   /**
    *  This method returns unique id to UI helping to handle errors
    * @return
    */
   public String getId()
   {
      if (null == distinctiveId)
      {
         distinctiveId = String.valueOf(DocumentMgmtUtility.generateUniqueId(getType()));
      }
      return distinctiveId;
   }
}
