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
package org.eclipse.stardust.ui.web.processportal.view;

import java.io.Serializable;
import java.util.*;

import javax.faces.convert.DateTimeConverter;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

import com.icesoft.faces.component.ext.HtmlInputText;
import com.icesoft.faces.component.ext.HtmlOutputText;
import com.icesoft.faces.component.ext.HtmlPanelGrid;
import com.icesoft.faces.component.ext.HtmlSelectBooleanCheckbox;
import com.icesoft.faces.component.selectinputdate.SelectInputDate;


/**
 * Handles creation of basic Form for DataMappings most basic datatypes. (Integer, String,
 * Date, Boolean)
 * 
 * @author roland.stamm
 * 
 */
public class DataMappingsFormFactory
{
   public enum IceDirection {
      IN, OUT, IN_OUT
   }

   private static final String BEAN_MAPPING_CLOSE = "']}";

   private static final String BEAN_MAPPING = "#{activityDetailsBean.dataMappingValues['";

   private Logger log = LogManager.getLogger(this.getClass());

   private Map<String, Serializable> dataMappingValues;

   private List<String> dataMappingsOut;

   private HtmlPanelGrid dataMappingsGrid;

   private long activityOID;

   /**
    * @param ao
    * @param dataMappingsGrid
    * @param dataMappingValues
    * @param dataMappingsOut
    */
   public DataMappingsFormFactory(ActivityInstance ao, HtmlPanelGrid dataMappingsGrid,
         Map<String, Serializable> dataMappingValues, List<String> dataMappingsOut)
   {
      this.dataMappingsGrid = dataMappingsGrid;
      this.dataMappingValues = dataMappingValues;
      this.dataMappingsOut = dataMappingsOut;
      this.activityOID = ao.getOID();

      createForm(ao);
   }

   /**
    * @param ao
    */
   @SuppressWarnings("unchecked")
   private void createForm(ActivityInstance ao)
   {

      ApplicationContext ac = ao.getActivity().getApplicationContext(
            PredefinedConstants.DEFAULT_CONTEXT);

      List<DataMapping> dataMappings = ac.getAllDataMappings();

      Map<String, DataMapping> inMap = new HashMap<String, DataMapping>();
      List<DataMapping> inoutList = new ArrayList<DataMapping>();
      List<DataMapping> outList = new ArrayList<DataMapping>();

      for (Iterator< ? > iterator2 = dataMappings.iterator(); iterator2.hasNext();)
      {
         DataMapping data = (DataMapping) iterator2.next();

         // log.warn(data.getId() + ":" + data.getMappedType().getName());
         if (data.getDirection().equals(Direction.IN))
         {
            // is 'in' if it remains in the list
            inMap.put(data.getId(), data);
         }
         else
         {
            if (data.getDirection().equals(Direction.OUT))
            {

               DataMapping dataMapping = inMap.get(data.getId());
               if (dataMapping != null)
               {
                  if (dataMapping.getId().equals(data.getId()))
                  {
                     // is 'inout'
                     inMap.remove(data.getId());
                     inoutList.add(dataMapping);
                     // log.warn("removed:" + dataMapping.getId());
                  }
               }
               else
               { // is 'out'
                  outList.add(data);
               }

            }
         }

      }
      for (Iterator<DataMapping> iterator = inMap.values().iterator(); iterator.hasNext();)
      {
         DataMapping data = (DataMapping) iterator.next();
         addInField(data);
      }
      for (Iterator<DataMapping> iterator = inoutList.iterator(); iterator.hasNext();)
      {
         DataMapping data = (DataMapping) iterator.next();
         addInOutField(data);
         dataMappingsOut.add(data.getId());
      }
      for (Iterator<DataMapping> iterator = outList.iterator(); iterator.hasNext();)
      {
         DataMapping data = (DataMapping) iterator.next();
         addOutField(data);
         dataMappingsOut.add(data.getId());
      }

   }

   /**
    * @param data
    */
   private void addOutField(DataMapping data)
   {
      if (log.isDebugEnabled())
      {
         log.debug("OUT" + data.getName() + data.getMappedType().getName());
      }

      if (data.getMappedType().equals(String.class))
      {
         addStringElement(data, IceDirection.OUT);
      }
      if (data.getMappedType().equals(Integer.class))
      {
         addIntegerElement(data, IceDirection.OUT);
      }
      if (data.getMappedType().equals(Date.class))
      {
         addDateElement(data, IceDirection.OUT);
      }
      if (data.getMappedType().equals(Boolean.class))
      {
         addBooleanElement(data, IceDirection.OUT);
      }

   }

   /**
    * @param data
    */
   private void addInOutField(DataMapping data)
   {
      if (log.isDebugEnabled())
      {
         log.debug("INOUT" + data.getName() + data.getMappedType().getName());
      }

      if (data.getMappedType().equals(String.class))
      {
         addStringElement(data, IceDirection.IN_OUT);
      }
      if (data.getMappedType().equals(Integer.class))
      {
         addIntegerElement(data, IceDirection.IN_OUT);
      }
      if (data.getMappedType().equals(Date.class))
      {
         addDateElement(data, IceDirection.IN_OUT);
      }
      if (data.getMappedType().equals(Boolean.class))
      {
         addBooleanElement(data, IceDirection.IN_OUT);
      }
   }

   /**
    * @param data
    */
   private void addInField(DataMapping data)
   {
      if (log.isDebugEnabled())
      {
         log.debug("IN" + data.getName() + data.getMappedType().getName());
      }

      if (data.getMappedType().equals(String.class))
      {
         addStringElement(data, IceDirection.IN);
      }
      if (data.getMappedType().equals(Integer.class))
      {
         addIntegerElement(data, IceDirection.IN);
      }
      if (data.getMappedType().equals(Date.class))
      {
         addDateElement(data, IceDirection.IN);
      }
      if (data.getMappedType().equals(Boolean.class))
      {
         addBooleanElement(data, IceDirection.IN);
      }
   }

   /**
    * @param data
    * @param direction
    */
   private void addIntegerElement(DataMapping data, IceDirection direction)
   {
      String key = data.getId();

      // Label
      HtmlOutputText label = new HtmlOutputText();
      label.setValue(data.getName());
      label.setId("datMap_" + generateUniqueId());
      dataMappingsGrid.getChildren().add(label);
      // Data Value
      Serializable value = direction != IceDirection.OUT ? getInDataValue(data) : "";
      dataMappingValues.put(key, value);

      // Edit Element
      HtmlInputText inputText = new HtmlInputText();
      inputText.setId("datMapInp_" + generateUniqueId());
      if (direction == IceDirection.IN)
      {
         inputText.setReadonly(true);
         inputText.setStyle("background: #DDD");
      }
      // inputText.setValueExpression(null, new ValueExpression("#{}"));
      inputText.setValueBinding("value", FacesUtils.createValueBinding(BEAN_MAPPING + key
            + BEAN_MAPPING_CLOSE));
      dataMappingsGrid.getChildren().add(inputText);
   }

   /**
    * @param data
    * @param direction
    */
   private void addStringElement(DataMapping data, IceDirection direction)
   {
      String key = data.getId();
      // Label
      HtmlOutputText label = new HtmlOutputText();
      label.setValue(data.getName());
      label.setId("datMap_" + generateUniqueId());
      dataMappingsGrid.getChildren().add(label);

      // Data Value
      Serializable value = direction != IceDirection.OUT ? getInDataValue(data) : "";
      dataMappingValues.put(key, value);

      // Edit Element
      HtmlInputText inputText = new HtmlInputText();
      inputText.setId("datMapInp_" + generateUniqueId());
      if (direction == IceDirection.IN)
      {
         inputText.setReadonly(true);
         inputText.setStyle("background: #DDD");
      }
      inputText.setValueBinding("value", FacesUtils.createValueBinding(BEAN_MAPPING + key
            + BEAN_MAPPING_CLOSE));
      dataMappingsGrid.getChildren().add(inputText);
   }

   /**
    * @param data
    * @param direction
    */
   private void addDateElement(DataMapping data, IceDirection direction)
   {

      String key = data.getId();

      // Label
      HtmlOutputText label = new HtmlOutputText();
      label.setValue(data.getName());
      label.setId("datMap_" + generateUniqueId());
      dataMappingsGrid.getChildren().add(label);

      // Data Value
      Serializable value = direction != IceDirection.OUT ? getInDataValue(data) : null;
      dataMappingValues.put(key, value);

      // Edit Element
      SelectInputDate inputDate = new SelectInputDate();
      inputDate.setId("datMapInp_" + generateUniqueId());
      if (direction == IceDirection.IN)
      {
         inputDate.setReadonly(true);
         inputDate.setStyle("background: #DDD");
         inputDate.setDisabled(true);
      }
      inputDate.setConverter(new DateTimeConverter());
      inputDate.setPopupDateFormat("EEE, MMM d, yyyy");
      inputDate.setRenderAsPopup(true);
      inputDate.setValueBinding("value", FacesUtils.createValueBinding(BEAN_MAPPING + key
            + BEAN_MAPPING_CLOSE));
      dataMappingsGrid.getChildren().add(inputDate);
   }

   /**
    * @param data
    * @param direction
    */
   private void addBooleanElement(DataMapping data, IceDirection direction)
   {
      String key = data.getId();

      // Label
      HtmlOutputText label = new HtmlOutputText();
      label.setValue(data.getName());
      label.setId("datMap_" + generateUniqueId());
      dataMappingsGrid.getChildren().add(label);

      // Data Value
      Serializable value = direction != IceDirection.OUT
            ? getInDataValue(data)
            : Boolean.valueOf(false);
      dataMappingValues.put(key, value);

      // Edit Element
      HtmlSelectBooleanCheckbox checkbox = new HtmlSelectBooleanCheckbox();
      checkbox.setId("datMapInp_" + generateUniqueId());
      if (direction == IceDirection.IN)
      {
         checkbox.setReadonly(true);
         checkbox.setStyle("background: #DDD");
      }
      checkbox.setValueBinding("value", FacesUtils.createValueBinding(BEAN_MAPPING + key
            + BEAN_MAPPING_CLOSE));
      dataMappingsGrid.getChildren().add(checkbox);
   }

   private String generateUniqueId()
   {
      return dataMappingsGrid.getChildCount() + "_" + activityOID;

   }

   /**
    * @param data
    * @return
    */
   private Serializable getInDataValue(DataMapping data)
   {

      Serializable value =  ServiceFactoryUtils
            .getWorkflowService()
            .getInDataValue(activityOID, PredefinedConstants.DEFAULT_CONTEXT,
                  data.getId());

      return value;
   }

   public HtmlPanelGrid getDataMappingsGrid()
   {
      return dataMappingsGrid;
   }

   public Map<String, Serializable> getDataMappingValues()
   {
      return dataMappingValues;
   }

   public List<String> getDataMappingsOut()
   {
      return dataMappingsOut;
   }

}
