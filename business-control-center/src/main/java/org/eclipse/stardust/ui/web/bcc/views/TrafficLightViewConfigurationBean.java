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
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ProcessDefinitionDetails;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.ConfigurationValidator;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.TrafficLightViewPropertyProvider;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.TrafficlightLocalizerKey;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.bcc.views.TrafficLightPropertyEntry.ProcessingThreshold;
import org.eclipse.stardust.ui.web.bcc.views.TrafficLightPropertyEntry.RowData;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;



/**
 * @author Giridhara.G
 * @version $Revision: $
 */

public class TrafficLightViewConfigurationBean extends UIComponentBean implements ConfirmationDialogHandler
{

   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(TrafficLightViewConfigurationBean.class);

   private static final String EMPTY = "";

   private SelectItem[] processDefinitionSelectItem;

   private String[] selectedProcessDefinition;

   private List<ProcessDefinition> selectedProcessDefinitions;

   private SelectItem[] processesSelectedItem;

   private String processId;

   private boolean validConfigValues = true;

   private ProcessDefinition selectedConfigProcess;

   private ProcessDefinition processDefinition;

   private Map propertyEntries = new HashMap();

   private boolean displayTotalRow = false;

   private String stateCalculator;

   private String descriptorFilter;

   private MessagesBCCBean propsBean;

   private List<RowData> rowDataList = null;

   private boolean displayRowData;

   private List<ProcessingThreshold> processingThresholdList = null;

   private boolean displayProcessingThreshold;

   private String warningMessage;

   private ConfirmationDialog trafficLightConfirmationDialog;
   
   public TrafficLightViewConfigurationBean()
   {
      try
      {
         propsBean = MessagesBCCBean.getInstance();
         initialize();
      }
      catch (Exception e)
      {
         trace.error(e);
      }

   }

   @Override
   public void initialize()
   {
      try
      {
         this.selectedProcessDefinitions = new ArrayList<ProcessDefinition>();
         preSelectedProcesses();
      }
      catch (Exception e)
      {
         trace.error(e);
      }
   }

   /**
	 * 
	 */
   public void preSelectedProcesses()
   {
      try
      {
         List<String> tempPreSelectedProcesses = TrafficLightViewPropertyProvider.getInstance()
               .getAllPreSelectedProcesses();

         selectedProcessDefinition = tempPreSelectedProcesses.toArray(new String[0]);
         for (String process : tempPreSelectedProcesses)
         {
            processDefinition = (ProcessDefinition) getProcessDefinition(process);
            this.selectedProcessDefinitions.add(processDefinition);
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }
   }

   /**
    * @return
    */
   public SelectItem[] getAllProcessDefinition()
   {
      try
      {
         ArrayList<SelectItem> sortedArray = new ArrayList<SelectItem>();
         List<DeployedModel> activeModels = ModelCache.findModelCache().getActiveModels();
         for (DeployedModel activeModel : activeModels)
         {
            Collection<ProcessDefinitionDetails> processes = activeModel.getAllProcessDefinitions();

            for (ProcessDefinitionDetails processDefinitionDetails : processes)
            {
               setSelectedConfigProcess(processDefinitionDetails);
               sortedArray.add(new SelectItem(processDefinitionDetails.getQualifiedId(), I18nUtils
                     .getProcessName(processDefinitionDetails)));
            }
         }
         Collections.sort(sortedArray, new SelectItemComparator());
         processDefinitionSelectItem = sortedArray.toArray(new SelectItem[0]);

      }
      catch (Exception e)
      {
         trace.error(e);
      }
      return processDefinitionSelectItem;
   }

   /**
    * @param event
    */
   public void effectChangeListener(ValueChangeEvent event)
   {
      try
      {
         if (event.getNewValue() != null)
         {
            this.processId = null;
            String[] testList = (String[]) event.getNewValue();
            this.selectedProcessDefinitions.clear();
            if (testList.length > 0)
            {
               for (int l = 0; l < testList.length; l++)
               {
                  processDefinition = (ProcessDefinition) getProcessDefinition(testList[l].toString());
                  this.selectedProcessDefinitions.add(processDefinition);
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }

   }

   /**
    * @param processId
    * @return
    */
   private ProcessDefinition getProcessDefinition(String processId)
   {
      try
      {
         String modelId = ModelUtils.extractModelId(processId);
         Model model = ModelUtils.getActiveModel(modelId);
         processDefinition = model.getProcessDefinition(processId);

      }
      catch (Exception e)
      {
         trace.error(e);
      }
      return processDefinition;
   }

   /**
    * @return
    */
   public SelectItem[] getAllSelectedProcesses()
   {
      try
      {
         String[] testList = null;
         if (selectedProcessDefinition != null)
         {
            processesSelectedItem = new SelectItem[selectedProcessDefinition.length + 1];
         }
         else
         {
            processesSelectedItem = new SelectItem[1];
         }
         processesSelectedItem[0] = new SelectItem("", "");

         if (selectedProcessDefinition != null)
         {

            testList = selectedProcessDefinition;
            for (int l = 0; l < testList.length; l++)
            {
               processDefinition = getProcessDefinition(testList[l]);
               processesSelectedItem[l + 1] = new SelectItem(processDefinition.getQualifiedId(),
                     I18nUtils.getProcessName(processDefinition));
            }

         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }
      return processesSelectedItem;
   }

   /**
    * @param event
    */
   public void processChangeListener(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }

      try
      {
         if (event.getNewValue() != null)
         {
            warningMessage = null;
            this.processId = event.getNewValue().toString().trim();
            processDefinition = getProcessDefinition(event.getNewValue().toString());
            if (processDefinition != null)
            {
               TrafficLightPropertyEntry tempPropertyEntry = new TrafficLightPropertyEntry(processDefinition);
               if (tempPropertyEntry != null)
               {
                  displayTotalRow = tempPropertyEntry.isDisplayTotalRow();
                  if (tempPropertyEntry.getStateCalculator() != null)
                  {
                     this.stateCalculator = tempPropertyEntry.getStateCalculator();
                  }
                  else
                  {
                     this.stateCalculator = EMPTY;
                  }
                  if (tempPropertyEntry.getDescriptorFilter() != null)
                  {
                     this.descriptorFilter = tempPropertyEntry.getDescriptorFilter();
                  }
                  else
                  {
                     this.descriptorFilter = EMPTY;
                  }
               }
               getRowDataTableValue();
               getProcessingThresholdTableValue();
            }
         }
         else
         {
            this.processId = null;
            warningMessage = null;
            displayTotalRow = false;
            stateCalculator = null;
            descriptorFilter = null;
         }

        // FacesUtils.clearFacesTreeValues();
      }
      catch (Exception e)
      {
         trace.error(e);
      }
   }

   /**
    * @param event
    */
   public void displayTotalChange(ValueChangeEvent event)
   {
      try
      {
         if (event.getNewValue() != null)
         {
            displayTotalRow = (Boolean) event.getNewValue();
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }

   }

   /**
    * @param event
    */
   public void stateCalculatorChange(ValueChangeEvent event)
   {
      try
      {
         if (event.getNewValue() != null)
         {
            stateCalculator = (String) event.getNewValue();
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }

   }

   /**
    * @param event
    */
   public void descriptorFilterChange(ValueChangeEvent event)
   {
      try
      {
         if (event.getNewValue() != null)
         {
            descriptorFilter = (String) event.getNewValue();
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }

   }

   /**
	 * 
	 */
   public void applyConfiguration()
   {
      try
      {
         if (validConfigValues)
         {
            if (processId != null)
            {
               TrafficLightPropertyEntry propertyEntry = (TrafficLightPropertyEntry) propertyEntries.get(processId);

               if (CollectionUtils.isNotEmpty(getProcessingThresholdList()))
               {
                  propertyEntry.setDisplayTotalRow(isDisplayTotalRow());
                  propertyEntry.setStateCalculator(getStateCalculator());
                  propertyEntry.setDescriptorFilter(getDescriptorFilter());
                  propertyEntry.save();
               }
               else
               {
                  warningMessage = propsBean.getString("common.error.selectProcess");
               }

               List<ProcessDefinition> tempProcessDefinitions = CollectionUtils.newArrayList();
               TrafficLightViewPropertyProvider provider = TrafficLightViewPropertyProvider.getInstance();
               if (this.selectedProcessDefinitions != null)
               {
                  provider.setAllPreSelectedProcesses(selectedProcessDefinitions);
                  
                  TrafficLightPropertyEntry tempPropertyEntry = null;
                  for (int i = 0; i < this.selectedProcessDefinitions.size(); i++)
                  {
                     ProcessDefinition processDefinition = (ProcessDefinition) this.selectedProcessDefinitions.get(i);
                     tempPropertyEntry = (TrafficLightPropertyEntry) propertyEntries.get(processDefinition
                           .getQualifiedId());
                     if (tempPropertyEntry.getProcessingThresholds() != null)
                     {
                        for (ProcessingThreshold processingThreshold : tempPropertyEntry.getProcessingThresholds())
                        {
                           if (processingThreshold != null && StringUtils.isNotEmpty(processingThreshold.getValue()))
                           {
                              tempProcessDefinitions.add(processDefinition);
                           }
                        }
                        
                        // TODO - check if a better way exist to save all selected PD
                        // config values
                        // Selected ProcessId is already saved, so skiping the same
                        if (!processDefinition.getQualifiedId().equals(processId))
                        {
                           tempPropertyEntry.save();
                        }
                     }
                  }
               }
               if (tempProcessDefinitions != null)
               {
                  provider.setAllProcessDefinitionIds(tempProcessDefinitions);
                  provider.save();                 
                  MessageDialog.addInfoMessage(propsBean.getString("common.successConfigurationMsg"));
               }
            }
            else
            {
               warningMessage = propsBean.getString("common.error.selectProcess");
            }
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }
      validConfigValues = true;
    
   }

   /**
    * @param selectedConfigProcess
    */
   public void setSelectedConfigProcess(ProcessDefinition selectedConfigProcess)
   {
      try
      {
         if (selectedConfigProcess != null)
         {
            if (!propertyEntries.containsKey(selectedConfigProcess.getQualifiedId()))
            {
               propertyEntries.put(selectedConfigProcess.getQualifiedId(), new TrafficLightPropertyEntry(
                     selectedConfigProcess));
            }
         }
         this.selectedConfigProcess = selectedConfigProcess;
      }
      catch (Exception e)
      {
         trace.error(e);
      }

   }

   /**
	 * 
	 */
   public void getRowDataTableValue()
   {
      try
      {
         processDefinition = getProcessDefinition(this.processId);
         rowDataList = new ArrayList<RowData>();
         if (processDefinition != null)
         {
            TrafficLightPropertyEntry tempPropertyEntry = null;
            if (propertyEntries.containsKey(processDefinition.getQualifiedId()))
            {
               tempPropertyEntry = (TrafficLightPropertyEntry) propertyEntries.get(processDefinition.getQualifiedId());
            }
            else
            {
               tempPropertyEntry = new TrafficLightPropertyEntry(processDefinition);
            }

            if (tempPropertyEntry.getRowData() != null)
            {                      
               for (RowData rowData:tempPropertyEntry.getRowData())
               {                
                  rowDataList.add(rowData);
               }
            }
         }
         Collections.sort(rowDataList);
      }
      catch (Exception e)
      {
         trace.error(e);
      }

   }

   /**
	 * 
	 */
   public void getProcessingThresholdTableValue()
   {
      try
      {
         processDefinition = getProcessDefinition(this.processId);
         processingThresholdList = new ArrayList<ProcessingThreshold>();
         if (processDefinition != null)
         {
            TrafficLightPropertyEntry tempPropertyEntry = null;
            if (propertyEntries.containsKey(processDefinition.getQualifiedId()))
            {
               tempPropertyEntry = (TrafficLightPropertyEntry) propertyEntries.get(processDefinition.getQualifiedId());
            }
            else
            {
               tempPropertyEntry = new TrafficLightPropertyEntry(processDefinition);
            }
            if (tempPropertyEntry.getProcessingThresholds() != null)
            {
               for (ProcessingThreshold processingThreshold : tempPropertyEntry.getProcessingThresholds())
               {
                  processingThresholdList.add(processingThreshold);
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }
   }

   /**
    * @param event
    */
   public void rowDataChangeListener(ValueChangeEvent event)
   {
      try
      {
         String rowDataId = (String) event.getComponent().getAttributes().get("rowDataId");
         if (event.getNewValue() != null)
         {
            if (rowDataId != null)
            {
               setRowDataConfigChange(rowDataId, event.getNewValue().toString());
               getRowDataTableValue();
            }
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }

   }

   /**
    * @param event
    */
   public void processingThresholdChangeListener(ValueChangeEvent event)
   {
      try
      {
         String activityId = (String) event.getComponent().getAttributes().get("activityId");

         if (event.getNewValue() != null)
         {
            if (activityId != null)
            {
               setprocessingThresholdConfigChange(activityId, event.getNewValue().toString());
               getProcessingThresholdTableValue();
            }
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }

   }

   /**
    * @param rowDataId
    * @param newDataPathValue
    */
   public void setRowDataConfigChange(String rowDataId, String newDataPathValue)
   {
      try
      {
         processDefinition = getProcessDefinition(this.processId);
         if (processDefinition != null)
         {
            if (propertyEntries.containsKey(processDefinition.getQualifiedId()))
            {
               TrafficLightPropertyEntry tempPropertyEntry = (TrafficLightPropertyEntry) propertyEntries
                     .get(processDefinition.getQualifiedId());
               for (int i = 0; i < tempPropertyEntry.getRowData().size(); i++)
               {
                  if (((RowData) tempPropertyEntry.getRowData().get(i)).getId().equals(rowDataId))
                     ((RowData) tempPropertyEntry.getRowData().get(i)).setValues(newDataPathValue);
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }
   }

   /**
    * @param activityId
    * @param newThresholdValue
    */
   public void setprocessingThresholdConfigChange(String activityId, String newThresholdValue)
   {
      try
      {
         processDefinition = getProcessDefinition(this.processId);
         if (processDefinition != null)
         {
            if (propertyEntries.containsKey(processDefinition.getQualifiedId()))
            {
               TrafficLightPropertyEntry tempPropertyEntry = (TrafficLightPropertyEntry) propertyEntries
                     .get(processDefinition.getQualifiedId());
               for (int i = 0; i < tempPropertyEntry.getProcessingThresholds().size(); i++)
               {
                  if (((ProcessingThreshold) tempPropertyEntry.getProcessingThresholds().get(i)).getId().equals(
                        activityId))
                     ((ProcessingThreshold) tempPropertyEntry.getProcessingThresholds().get(i))
                           .setValue(newThresholdValue);
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }
   }

   public void validateProcessingThreshold(FacesContext context, UIComponent component, Object input)
         throws ValidatorException
   {
      boolean validProcThres = ConfigurationValidator.validateProcessingThreshold(input);
      validConfigValues = validProcThres ? validConfigValues : validProcThres;
   }

   public void validateStateCalculator(FacesContext context, UIComponent component, Object input)
         throws ValidatorException
   {
      boolean classExists = ConfigurationValidator.checkClassExists(input,
            TrafficlightLocalizerKey.STATE_CALC_CLASS_NOT_FOUND);
      if (classExists)
      {
         stateCalculator = (String) input;
      }
      else
      {
         validConfigValues = classExists;
      }
   }

   public void validateDescFilter(FacesContext context, UIComponent component, Object input) throws ValidatorException
   {
      boolean classExists = ConfigurationValidator.checkClassExists(input,
            TrafficlightLocalizerKey.DESC_FILTER_CLASS_NOT_FOUND);
      if (classExists)
      {
         descriptorFilter = (String) input;
      }
      else
      {
         validConfigValues = classExists;
      }
   }

   /**
	 * 
	 */
   public void reset()
   {
      try
      {
         
         FacesUtils.clearFacesTreeValues();
         warningMessage = null;
         displayTotalRow = false;
         if (rowDataList != null)
         {
            for (RowData rowData : getRowDataList())
            {
               rowData.setValues(EMPTY);
            }
         }
         
         if (propertyEntries.containsKey(processId))
         {
            TrafficLightPropertyEntry tempPropertyEntry = (TrafficLightPropertyEntry) propertyEntries
                  .get(processId);
            tempPropertyEntry.setDescriptorFilter(EMPTY);
            tempPropertyEntry.setStateCalculator(EMPTY);
            tempPropertyEntry.setDisplayTotalRow(false);
            tempPropertyEntry.setProcessingThresholds(CollectionUtils.newHashMap());            
         }
         if (getProcessingThresholdList() != null)
         {
            for (ProcessingThreshold processingThreshold : getProcessingThresholdList())
            {
               processingThreshold.setValue(EMPTY);
            }
         }
                 
         MessageDialog.addInfoMessage(MessagesViewsCommonBean.getInstance().getString("views.common.config.reset"));

      }
      catch (Exception e)
      {
         trace.error(e);
      }
   }

   /**
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      Iterator<String> facesMessageIds = facesContext.getClientIdsWithMessages();
      while (facesMessageIds.hasNext())
      {
         // Clears the Error message on the Page
         FacesUtils.clearFacesComponentValues(FacesUtils.matchComponentInHierarchy(FacesContext.getCurrentInstance()
               .getViewRoot(), facesMessageIds.next()));
      }
      trafficLightConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
      trafficLightConfirmationDialog.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      trafficLightConfirmationDialog.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            MessagesBCCBean.getInstance().getString("views.trafficLightView.labelTitle")));
      trafficLightConfirmationDialog.openPopup();
   }
   
   /*
    * 
    */
   public boolean accept()
   {
      reset();
      trafficLightConfirmationDialog = null;
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      trafficLightConfirmationDialog = null;
      return true;
   }
   
   public SelectItem[] getProcessDefinitionSelectItem()
   {
      return processDefinitionSelectItem;
   }

   public void setProcessDefinitionSelectItem(SelectItem[] processDefinitionSelectItem)
   {
      this.processDefinitionSelectItem = processDefinitionSelectItem;
   }

   public String[] getSelectedProcessDefinition()
   {
      return selectedProcessDefinition;
   }

   public void setSelectedProcessDefinition(String[] selectedProcessDefinition)
   {
      this.selectedProcessDefinition = selectedProcessDefinition;
   }

   public SelectItem[] getProcessesSelectedItem()
   {
      return processesSelectedItem;
   }

   public void setProcessesSelectedItem(SelectItem[] processesSelectedItem)
   {
      this.processesSelectedItem = processesSelectedItem;
   }

   public String getProcessId()
   {
      return processId;
   }

   public void setProcessId(String processId)
   {
      this.processId = processId;
   }

   public ProcessDefinition getSelectedConfigProcess()
   {
      return selectedConfigProcess;
   }

   public boolean isDisplayTotalRow()
   {
      return displayTotalRow;
   }

   public void setDisplayTotalRow(boolean displayTotalRow)
   {
      // this.displayTotalRow = displayTotalRow;
   }

   public String getStateCalculator()
   {
      return stateCalculator;
   }

   public void setStateCalculator(String stateCalculator)
   {
      // this.stateCalculator = stateCalculator;
   }

   public String getDescriptorFilter()
   {
      return descriptorFilter;
   }

   public void setDescriptorFilter(String descriptorFilter)
   {
      // this.descriptorFilter = descriptorFilter;
   }

   public List<RowData> getRowDataList()
   {
      return rowDataList;
   }

   public List<ProcessingThreshold> getProcessingThresholdList()
   {
      return processingThresholdList;
   }

   public boolean isDisplayRowData()
   {
      if (CollectionUtils.isNotEmpty(rowDataList))
      {
         displayRowData = true;
      }
      return displayRowData;
   }

   public boolean isDisplayProcessingThreshold()
   {
      if (CollectionUtils.isNotEmpty(processingThresholdList))
      {
         displayProcessingThreshold = true;
      }
      return displayProcessingThreshold;
   }

   public String getWarningMessage()
   {
      return warningMessage;
   }

   public void setWarningMessage(String warningMessage)
   {
      this.warningMessage = warningMessage;
   }

   /**
    * @author Ankita.Patel
    * @version $Revision: $
    */
   public class SelectItemComparator implements Comparator<SelectItem>
   {
      public int compare(SelectItem s1, SelectItem s2)
      {
         return s1.getLabel().compareTo(s2.getLabel());
      }
   }

   public ConfirmationDialog getTrafficLightConfirmationDialog()
   {
      return trafficLightConfirmationDialog;
   }

   
}
