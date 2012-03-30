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
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.ui.web.bcc.PropertyEntry;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.ConfigurationValidator;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.PropertyProvider;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
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
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class GanttChartConfigurationBean extends UIComponentBean implements ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;

   private PropertyProvider propertyProvider;

   private List selectedProcessDefinitions = new ArrayList();

   private PropertyEntry propertyEntry = PropertyEntry.EMPTY_PROPERTY_ENTRY;

   private boolean validConfigValues = true;

   private SelectItem[] allProcessDefinitions;

   private String[] selectedProcesses;

   private List/* <SelectItem> */processDefinitionIds = new ArrayList();

   private SelectItem[] selectedConfigProcesses;

   private String selectedProcessQId;

   private Map<String, PropertyEntry> propertyEntries = new HashMap<String, PropertyEntry>();

   private String selectedDescriptorValue;

   private SelectItem[] descriptorValueList;

   private String plannedStartTime;

   private String plannedTerminationTime;

   private String estimatedDurationSeconds;

   private String thresholdPercentage;

   private SelectItem[] successorProcessDefinitionIds;

   private SelectItem[] predecessorProcessDefinitionIds;

   private String successor;

   private String predecessor;

   private SelectItem[] descriptors;

   private String descriptorKey;

   private String descriptorValues;

   private String warningMessage;

   private List<ProcessDefinition> processDefinitions;
   
   private ConfirmationDialog ganttChartConfirmationDialog;

   /**
    * 
    */
   public GanttChartConfigurationBean()
   {
      propertyProvider = (PropertyProvider) PropertyProvider.getInstance();
      initialize();
   }

   /**
    * method to get all ProcessDefination of active models
    * 
    * @return
    */
   private List<ProcessDefinition> getAllActiveProcessDefinations()
   {
      List<ProcessDefinition> defs = CollectionUtils.newArrayList();
      List<DeployedModel> models = ModelUtils.getActiveModels();
      for (DeployedModel model : models)
      {
         defs.addAll(model.getAllProcessDefinitions());
      }
      return defs;
   }

   @Override
   public void initialize()
   {
      boolean disableForAllProcesses = propertyProvider
            .getBooleanProperty(PropertyProvider.DISABLE_DIAGRAM_FOR_ALL_PROCESSES_PROPERTY);
      boolean enableForAllProcesses = propertyProvider
            .getBooleanProperty(PropertyProvider.ENABLE_DIAGRAM_FOR_ALL_PROCESSES_PROPERTY);
      enableForAllProcesses = !enableForAllProcesses && !disableForAllProcesses ? true : enableForAllProcesses;
      List<ProcessDefinition> defs = getAllActiveProcessDefinations();
      allProcessDefinitions = new SelectItem[defs.size()];
      processDefinitions = new ArrayList<ProcessDefinition>();
      int count = 0;
      for (ProcessDefinition pd : defs)
      {

         if (enableForAllProcesses
               && (!propertyProvider.hasConfigParam(pd.getQualifiedId(), PropertyProvider.ENABLE_DIAGRAM_PROPERTY) || propertyProvider
                     .getBooleanProperty(pd.getQualifiedId(), PropertyProvider.ENABLE_DIAGRAM_PROPERTY)))
         {
            selectedProcessDefinitions.add(pd);
         }
         else if (disableForAllProcesses
               && propertyProvider.getBooleanProperty(pd.getQualifiedId(), PropertyProvider.ENABLE_DIAGRAM_PROPERTY))
         {
            selectedProcessDefinitions.add(pd);

         }
         processDefinitions.add(pd);
         SelectItem selectItemID = new SelectItem(pd.getQualifiedId(), I18nUtils.getProcessName(pd));
         allProcessDefinitions[count] = new SelectItem(pd.getQualifiedId(), I18nUtils.getProcessName(pd));

         processDefinitionIds.add(selectItemID);
         count++;
      }
      count = 0;
      Collections.sort(processDefinitionIds, new SelectItemComparator());
      for (Object item : processDefinitionIds)
      {
         if (item instanceof SelectItem)
         {
            allProcessDefinitions[count] = (SelectItem) item;
            count++;
         }
      }

   }

   /**
    * Selects multiple processes
    * 
    * @param event
    */
   public void selectedAllProcessList(ValueChangeEvent event)
   {

      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }
      if (event.getNewValue() != null)
      {
         warningMessage = null;
         this.selectedProcessQId = null;
         selectedConfigProcesses = new SelectItem[1];
         String[] testList = (String[]) event.getNewValue();
         if (testList != null)
            selectedConfigProcesses = new SelectItem[testList.length + 1];
         else
            selectedConfigProcesses = new SelectItem[1];
         selectedConfigProcesses[0] = new SelectItem("", "");

         if (testList.length > 0)
         {
            ProcessDefinition processDefinition = null;
            for (int l = 0; l < testList.length; l++)
            {
               for (Iterator iterator = processDefinitions.iterator(); iterator.hasNext();)
               {
                  ProcessDefinition pd = (ProcessDefinition) iterator.next();
                  if (pd.getQualifiedId().equals(testList[l].toString()))
                  {
                     processDefinition = pd;
                  }
               }
               selectedConfigProcesses[l + 1] = new SelectItem(processDefinition.getQualifiedId(),
                     I18nUtils.getProcessName(processDefinition));

            }
         }
      }

   }

   /**
    * selects one process to configure
    * 
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

      warningMessage = null;
      if (StringUtils.isNotEmpty(selectedProcessQId) && !propertyEntries.containsKey(selectedProcessQId))
      {
         propertyEntries.put(selectedProcessQId, new PropertyEntry(getProcessDefinition(selectedProcessQId)));
      }
      propertyEntry = StringUtils.isNotEmpty(selectedProcessQId) ? (PropertyEntry) propertyEntries
            .get(selectedProcessQId) : PropertyEntry.EMPTY_PROPERTY_ENTRY;
      computeDescriptorList(propertyEntry);
      computeRestofValues(propertyEntry);
      computeSuccessorPIds(propertyEntry);
      computePredecessorPIds(propertyEntry);
      computeDescriptorKey(propertyEntry);

   }

   /**
    * populates process definition descriptor data
    * 
    * @param event
    */
   public void descriptorChangeListener(ValueChangeEvent event)
   {
     
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }
      propertyEntry = null;
      if (StringUtils.isNotEmpty(selectedDescriptorValue))
      {

         String key = selectedProcessQId + "." + selectedDescriptorValue;
         propertyEntry = propertyEntries.get(key);

         if (propertyEntry == null)
         {
            propertyEntry = new PropertyEntry(getProcessDefinition(selectedProcessQId), selectedDescriptorValue);
            propertyEntries.put(key, propertyEntry);

            SelectItem selectItemID = new SelectItem(key, key);
            processDefinitionIds.add(selectItemID);
         }
      }   
      
      if(propertyEntry==null)
      {
         propertyEntry = selectedProcessQId != null
         ? (PropertyEntry) propertyEntries.get(selectedProcessQId)
         : PropertyEntry.EMPTY_PROPERTY_ENTRY;
      }
      
      resetDescriptorProperties();
      if (propertyEntry != null)
      {
         computeRestofValues(propertyEntry);
         computeSuccessorPIds(propertyEntry);
         computePredecessorPIds(propertyEntry);
         computeDescriptorKey(propertyEntry);
      }
   }

   private ProcessDefinition getProcessDefinition(String fqID)
   {
      for (ProcessDefinition pd : processDefinitions)
      {
         if (pd.getQualifiedId().equals(fqID))
         {
            return pd;
         }
      }
      return null;
   }

   /**
    * saves the configuration
    */
   public void applyConfiguration()
   {
      warningMessage=null;
      MessagesBCCBean propsBean = MessagesBCCBean.getInstance();
      

      if (validConfigValues)
      {
         if (selectedProcessQId != null && selectedProcesses != null)
         {
            if (successor != null && predecessor != null && successor.equals(predecessor))
            {

               warningMessage = MessagesViewsCommonBean.getInstance().getString(
                     "views.ganttChartView.configure.error.successorEqualsPredecessor");
               return;

            }
            for (String processFQID : propertyEntries.keySet())
            {
               PropertyEntry propertyEntry = propertyEntries.get(processFQID);               
               propertyEntry.save();
            }
            propertyProvider.save();           
            MessageDialog.addInfoMessage(propsBean.getString("common.successConfigurationMsg"));
         }
         else
         {
            warningMessage = propsBean.getString("common.error.selectProcess");
         }

      }
      validConfigValues = true;
   }

   /**
    * resets values
    */
   public void cancelConfiguration()
   {
      reset();
   }

   /**
    * resets data for selected descriptor value
    */
   private void resetDescriptorProperties()
   {
      plannedStartTime = null;
      plannedTerminationTime = null;
      thresholdPercentage = null;
      estimatedDurationSeconds = null;
      successor = null;
      predecessor = null;
   }

   /**
    * computes descriptors values
    * 
    * @param propertyEntry2
    */
   private void computeDescriptorKey(PropertyEntry propertyEntry2)
   {
      if (propertyEntry2.getDescriptors() != null)
      {
         SelectItem[] descriList = propertyEntry2.getDescriptors();
         descriptors = new SelectItem[propertyEntry2.getDescriptors().length + 1];
         descriptors[0] = new SelectItem("");
         int count = 1;
         for (Object object : descriList)
         {
            SelectItem item = (SelectItem) object;
            descriptors[count] = item;
            count++;
         }
      }
      else
      {
         descriptors = new SelectItem[0];
      }

   }

   /**
    * computes prdecessor process definition ids
    * 
    * @param propertyEntry2
    */
   private void computePredecessorPIds(PropertyEntry propertyEntry2)
   {
      String successor = propertyEntry2.getSuccessor();
      if (processDefinitionIds != null && processDefinitionIds.size() > 0)
      {

         predecessorProcessDefinitionIds = new SelectItem[processDefinitionIds.size() + 1];
         predecessorProcessDefinitionIds[0] = new SelectItem("");
         // }

         int count = 1;
         for (Iterator<SelectItem> iterator = processDefinitionIds.iterator(); iterator.hasNext();)
         {
            SelectItem item = (SelectItem) iterator.next();
            if (successor == null || !successor.equals(item.getLabel()))
            {
               predecessorProcessDefinitionIds[count] = item;
               count++;
            }
         }
      }

   }

   /**
    * computes successor process definition ids
    * 
    * @param propertyEntry2
    */
   private void computeSuccessorPIds(PropertyEntry propertyEntry2)
   {
      String predecessor = propertyEntry2.getPredecessor();

      if (processDefinitionIds != null && processDefinitionIds.size() > 0)
      {
         successorProcessDefinitionIds = new SelectItem[processDefinitionIds.size() + 1];
         successorProcessDefinitionIds[0] = new SelectItem("");
         // }
         int count = 1;
         for (Iterator<SelectItem> iterator = processDefinitionIds.iterator(); iterator.hasNext();)
         {
            SelectItem item = (SelectItem) iterator.next();
            if (predecessor == null || !predecessor.equals(item.getLabel()))
            {
               successorProcessDefinitionIds[count] = item;
               count++;
            }
         }
      }
      else
      {
         successorProcessDefinitionIds = new SelectItem[0];
      }
   }

   /**
    * Computes other properties
    * 
    * @param propertyEntry2
    */
   private void computeRestofValues(PropertyEntry propertyEntry2)
   {
      this.plannedStartTime = propertyEntry2.getPlannedStartTime();
      this.plannedTerminationTime = propertyEntry2.getPlannedTerminationTime();
      this.thresholdPercentage = propertyEntry2.getThresholdPercentage();
      this.estimatedDurationSeconds = propertyEntry2.getEstimatedDurationSeconds();
      this.successor = propertyEntry2.getSuccessor();
      this.predecessor = propertyEntry2.getPredecessor();
      this.descriptorKey = propertyEntry2.getDescriptorKey();
      this.descriptorValues = propertyEntry2.getDescriptorValues();     
   }

   /**
    * computes descriptor list
    * 
    * @param propertyEntry2
    */
   private void computeDescriptorList(PropertyEntry propertyEntry2)
   {
      List descriptors = propertyEntry2.getDescriptorValueList();
      if (descriptors != null && descriptors.size() > 0)
      {
         descriptorValueList = new SelectItem[descriptors.size() + 1];
         descriptorValueList[0] = new SelectItem("");
         int count = 1;
         for (Object object : descriptors)
         {
            SelectItem item = (SelectItem) object;
            descriptorValueList[count] = item;
            count++;
         }
      }
      else
      {
         descriptorValueList = new SelectItem[0];
      }
   }

   /**
    * Resets all values
    */
   private void reset()
   {
      warningMessage=null;
      selectedProcessDefinitions = CollectionUtils.newArrayList();
      if (StringUtils.isNotEmpty(selectedProcessQId))
      {
         propertyEntry = propertyEntries.get(selectedProcessQId);
         propertyEntry.reset();
      }
      else
      {
         propertyEntry = PropertyEntry.EMPTY_PROPERTY_ENTRY;
      }

      plannedStartTime = null;
      plannedTerminationTime = null;
      thresholdPercentage = null;
      estimatedDurationSeconds = null;
      descriptorKey = null;
      descriptorValues = null;
      successor = null;
      predecessor = null;
      selectedDescriptorValue = null;

      FacesUtils.clearFacesTreeValues();
      MessageDialog.addInfoMessage(MessagesViewsCommonBean.getInstance().getString("views.common.config.reset"));

   }
   
   /**
    * Open confirmation dialog prior to reset value.
    */
   public void openConfirmationDialog()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      Iterator<String> facesMessageIds = facesContext.getClientIdsWithMessages();
      while (facesMessageIds.hasNext())
      {
         // Clears the Error messages on the Page
         FacesUtils.clearFacesComponentValues(FacesUtils.matchComponentInHierarchy(FacesContext.getCurrentInstance()
               .getViewRoot(), facesMessageIds.next()));
      }
      ganttChartConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
      ganttChartConfirmationDialog.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      ganttChartConfirmationDialog.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            MessagesViewsCommonBean.getInstance().getString("views.ganttChartView.labelTitle")));
      ganttChartConfirmationDialog.openPopup();
   }

   /**
    * 
    */
   public boolean accept()
   {
      cancelConfiguration();
      ganttChartConfirmationDialog = null;
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      ganttChartConfirmationDialog = null;
      return true;
   }
   
   // **************** Validator methods *********************************
   public void validatePlannedStartTime(FacesContext context, UIComponent component, Object input)
         throws ValidatorException
   {
      boolean validProcThres = ConfigurationValidator.validatePlannedStartTime(input);
      validConfigValues = validProcThres ? validConfigValues : validProcThres;
   }

   public void validatePlannedTerminationTime(FacesContext context, UIComponent component, Object input)
         throws ValidatorException
   {
      boolean validProcThres = ConfigurationValidator.validatePlannedTerminationTime(input);
      validConfigValues = validProcThres ? validConfigValues : validProcThres;
   }

   public void validateDurationSeconds(FacesContext context, UIComponent component, Object input)
         throws ValidatorException
   {
      boolean validProcThres = ConfigurationValidator.validateDurationSeconds(input);
      validConfigValues = validProcThres ? validConfigValues : validProcThres;
   }

   public void validateThresholdPercentage(FacesContext context, UIComponent component, Object input)
         throws ValidatorException
   {
      boolean validProcThres = ConfigurationValidator.validateThresholdPercentage(input);
      validConfigValues = validProcThres ? validConfigValues : validProcThres;
   }

   /**
    * @param viewName
    */
   public GanttChartConfigurationBean(String viewName)
   {
      super(viewName);
   }

   // *************************** Modiified setter methods ****************************
   public void setSelectedDescriptorValue(String descriptorValue)
   {
      this.selectedDescriptorValue = descriptorValue;

   }

   // ******************* Default getter & setter methods ******************

   public SelectItem[] getDescriptorValueList()
   {
      return descriptorValueList;
   }

   public String getSelectedDescriptorValue()
   {
      return selectedDescriptorValue;
   }

   public SelectItem[] getAllProcessDefinitions()
   {
      return allProcessDefinitions;
   }

   public void setAllProcessDefinitions(SelectItem[] allProcessDefinitions)
   {
      this.allProcessDefinitions = allProcessDefinitions;
   }

   public List getProcessDefinitionIds()
   {
      return processDefinitionIds;
   }

   public String[] getSelectedProcesses()
   {
      return selectedProcesses;
   }

   public void setSelectedProcesses(String[] selectedProcesses)
   {
      this.selectedProcesses = selectedProcesses;
   }

   public SelectItem[] getSelectedConfigProcesses()
   {
      return selectedConfigProcesses;
   }

   public void setSelectedConfigProcesses(SelectItem[] selectedConfigProcesses)
   {
      this.selectedConfigProcesses = selectedConfigProcesses;
   }

   // public ProcessDefinition getSelectedConfigProcess()
   // {
   // return selectedConfigProcess;
   // }
   //
   // public void setSelectedConfigProcess(ProcessDefinition selectedConfigProcess)
   // {
   // this.selectedConfigProcess = selectedConfigProcess;
   // }

   public PropertyEntry getPropertyEntry()
   {
      return propertyEntry;
   }

   public String getPlannedStartTime()
   {
      return plannedStartTime;
   }

   public void setPlannedStartTime(String plannedStartTime)
   {
      this.plannedStartTime = plannedStartTime;
      propertyEntry.setPlannedStartTime(plannedStartTime);
   }

   public String getPlannedTerminationTime()
   {
      return plannedTerminationTime;
   }

   public void setPlannedTerminationTime(String plannedTerminationTime)
   {     
      this.plannedTerminationTime = plannedTerminationTime;
      propertyEntry.setPlannedTerminationTime(plannedTerminationTime);
   }

   public String getThresholdPercentage()
   {
      return thresholdPercentage;
   }

   public void setThresholdPercentage(String thresholdPercentage)
   {
      this.thresholdPercentage = thresholdPercentage;
      propertyEntry.setThresholdPercentage(thresholdPercentage);
   }

   public void setPropertyEntry(PropertyEntry propertyEntry)
   {
      this.propertyEntry = propertyEntry;
   }

   public String getEstimatedDurationSeconds()
   {
      return estimatedDurationSeconds;
   }

   public void setEstimatedDurationSeconds(String estimatedDurationSeconds)
   {
      this.estimatedDurationSeconds = estimatedDurationSeconds;
      propertyEntry.setEstimatedDurationSeconds(estimatedDurationSeconds);
   }

   public SelectItem[] getSuccessorProcessDefinitionIds()
   {
      return successorProcessDefinitionIds;
   }

   public void setSuccessorProcessDefinitionIds(SelectItem[] successorProcessDefinitionIds)
   {
      this.successorProcessDefinitionIds = successorProcessDefinitionIds;
   }

   public SelectItem[] getPredecessorProcessDefinitionIds()
   {
      return predecessorProcessDefinitionIds;
   }

   public void setPredecessorProcessDefinitionIds(SelectItem[] predecessorProcessDefinitionIds)
   {
      this.predecessorProcessDefinitionIds = predecessorProcessDefinitionIds;
   }

   public String getSuccessor()
   {
      return successor;
   }

   public void setSuccessor(String successor)
   {
      this.successor = successor;
      propertyEntry.setSuccessor(successor);
   }

   public String getPredecessor()
   {
      return predecessor;
   }

   public void setPredecessor(String predecessor)
   {
      this.predecessor = predecessor;
      propertyEntry.setPredecessor(predecessor);
   }

   public SelectItem[] getDescriptors()
   {
      return descriptors;
   }

   public void setDescriptors(SelectItem[] descriptors)
   {
      this.descriptors = descriptors;
   }

   public String getDescriptorKey()
   {
      return descriptorKey;
   }

   public void setDescriptorKey(String descriptorKey)
   {
      this.descriptorKey = descriptorKey;
      propertyEntry.setDescriptorKey(descriptorKey);
      descriptorValues = propertyEntry.getDescriptorValues();
   }

   public String getDescriptorValues()
   {
      return descriptorValues;
   }

   public void setDescriptorValues(String descriptorValues)
   {
     
      if (null != propertyEntries && StringUtils.isNotEmpty(descriptorValues)
            && StringUtils.isEmpty(selectedDescriptorValue))
      {   
         
         if (propertyEntry != null)
         {
            this.descriptorValues = descriptorValues;
            propertyEntry.setDescriptorValues(descriptorValues);
            computeDescriptorList(propertyEntry);
         }
      }

   }

   public String getWarningMessage()
   {
      return warningMessage;
   }

   public void setWarningMessage(String warningMessage)
   {
      this.warningMessage = warningMessage;
   }

   public ConfirmationDialog getGanttChartConfirmationDialog()
   {
      return ganttChartConfirmationDialog;
   }


   /**
    * @author Ankita.Patel
    * @version $Revision: $
    */
   public class SelectItemComparator implements Comparator<SelectItem>
   {
      public int compare(SelectItem s1, SelectItem s2)
      {
         if (s1.getValue() instanceof ProcessDefinition && s2.getValue() instanceof ProcessDefinition)
         {
            ProcessDefinition pd1 = (ProcessDefinition) s1.getValue();
            ProcessDefinition pd2 = (ProcessDefinition) s1.getValue();
            return pd1.getQualifiedId().compareTo(pd2.getQualifiedId());
         }
         return s1.getLabel().compareTo(s2.getLabel());
      }
   }

   public String getSelectedProcessQId()
   {
      return selectedProcessQId;
   }

   public void setSelectedProcessQId(String selectedProcessQId)
   {
      this.selectedProcessQId = selectedProcessQId;
   }

}
