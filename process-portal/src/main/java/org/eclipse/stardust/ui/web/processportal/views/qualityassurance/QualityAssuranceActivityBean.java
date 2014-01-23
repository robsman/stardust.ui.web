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
package org.eclipse.stardust.ui.web.processportal.views.qualityassurance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributesImpl;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceResult;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceResultImpl;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceResult.ResultState;
import org.eclipse.stardust.engine.api.model.ContextData;
import org.eclipse.stardust.engine.api.model.QualityAssuranceCode;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.command.impl.QualityAssuranceCompleteCommand;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.processportal.view.ActivityDetailsBean;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ValidationMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * Assist to Pass / Fail QA Activity
 * 
 * @author Yogesh.Manware
 * 
 */
public class QualityAssuranceActivityBean extends PopupUIComponentBean
{

   private static final long serialVersionUID = 4429473712364906234L;

   public static enum QAAction {
      PASS, FAIL
   }

   public static enum SelectQACodesMode {
      AUTO_COMPLETE, MULTI_SELECT_TABLE
   }

   private QAAction qaAction = QAAction.PASS;
   private SelectQACodesMode selectQACodesMode = SelectQACodesMode.AUTO_COMPLETE;
   private ActivityInstance activityInstance;
   private View parentView;
   private SortableTable<QualityAssuranceCodeEntry> qualityAssuranceCodesTable;
   private String note;
   private boolean assignToLastPerformer = true;
   private String correctionMadeOption = "false";
   Map<String, ? > outData;
   private QualityACAutocompleteMultiSelector qualityACAutocompleteMultiSelector;
   private ValidationMessageBean validationMessageBean;

   public static QualityAssuranceActivityBean getInstance()
   {
      return (QualityAssuranceActivityBean) FacesUtils.getBeanFromContext("qualityAssuranceActivityBean");
   }

   /**
    * Opens the Quality Assurance Dialog to Pass / Fail QA Activity
    * 
    * @param qaAction
    * @param ai
    * @param parentView
    * @param outData
    */
   public static void openDialog(QAAction qaAction, ActivityInstance ai, View parentView, Map<String, ? > outData)
   {
      QualityAssuranceActivityBean qaBean = QualityAssuranceActivityBean.getInstance();
      qaBean.setQaAction(qaAction);
      qaBean.setActivityInstance(ai);
      qaBean.setParentView(parentView);
      qaBean.initializeSelectedQATableColumns();
      qaBean.setOutData(outData);
      qaBean.setQualityACAutocompleteMultiSelector(new QualityACAutocompleteMultiSelector(ai));
      //reset messages
      qaBean.validationMessageBean = new ValidationMessageBean();
      qaBean.openPopup();
   }

   /**
    * Invoked from user action to complete Pass / Fail QA Activity
    */
   public void completeAction()
   {
      // get selected QA codes
      Set<QualityAssuranceCode> selectedQACodes = getSelectedQualityAssuranceCodes();
 
      // validate QA codes
      validationMessageBean.reset();
      if (isShowQACodesSection() && CollectionUtils.isEmpty(selectedQACodes))
      {
         validationMessageBean.addError(
               MessagePropertiesBean.getInstance().get("views.qualityAssuranceActivityDialog.selectQACodesError"),
               "qaCodeId");
      }

      if (validationMessageBean.isContainErrorMessages())
      {
         return;
      }
      
      // Create Quality Assurance result
      QualityAssuranceResult result = new QualityAssuranceResultImpl();
      result.setAssignFailedInstanceToLastPerformer(isAssignToLastPerformer());
      result.setQualityAssuranceCodes(selectedQACodes);
      // set Quality Assurance State
      ResultState state = null;
      if (isPassQAActivity())
      {
         if (isCorrectionMade())
         {
            state = ResultState.PASS_WITH_CORRECTION;
         }
         else
         {
            state = ResultState.PASS_NO_CORRECTION;
         }
      }
      else
      {
         state = ResultState.FAILED;
      }
      result.setQualityAssuranceState(state);

      ActivityInstanceAttributes activityInstanceAttributes = new ActivityInstanceAttributesImpl(
            activityInstance.getOID());
      activityInstanceAttributes.setQualityAssuranceResult(result);
      activityInstanceAttributes.addNote(getNote());

      IActivityInteractionController interactionController = ActivityDetailsBean
            .getInteractionController(activityInstance.getActivity());
      ContextData context = new ContextData(interactionController.getContextId(activityInstance), outData);

      QualityAssuranceCompleteCommand qualityAssuranceCompleteCommand = new QualityAssuranceCompleteCommand(
            activityInstanceAttributes, context);
      qualityAssuranceCompleteCommand.execute(getServiceFactory());

      // Close activity panel
      if (null != parentView)
      {
         PortalApplication.getInstance().closeView(parentView, true);
      }

      closePopup();
   }

   /**
    * Toggles between AutoComplete and Multi-select from table
    */
   public void toggleAutocompleteOrSelectFromList()
   {
      if (isAutoCompleteMode())
      {
         selectQACodesMode = SelectQACodesMode.MULTI_SELECT_TABLE;
      }
      else
      {
         selectQACodesMode = SelectQACodesMode.AUTO_COMPLETE;
      }
   }

   /*
    * Cleanup activities (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.PopupUIComponentBean#closePopup()
    */
   public void closePopup()
   {
      selectQACodesMode = SelectQACodesMode.AUTO_COMPLETE;
      assignToLastPerformer = true;
      correctionMadeOption = "false";
      super.closePopup();
   }

   @Override
   public void initialize()
   {}

   /**
    * Initialize QA Table
    */
   private void initializeSelectedQATableColumns()
   {
      ColumnPreference codeCol = new ColumnPreference("code", "code", ColumnDataType.STRING, MessagePropertiesBean
            .getInstance().get("views.qualityAssuranceActivityDialog.QACodeTable.code"), true, false);
      codeCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference descCol = new ColumnPreference("description", "description", ColumnDataType.STRING,
            MessagePropertiesBean.getInstance().get("views.qualityAssuranceActivityDialog.QACodeTable.description"), true, false);
      descCol.setEscape(false);

      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      cols.add(codeCol);
      cols.add(descCol);

      IColumnModel columnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_WORKFLOW,
            UserPreferencesEntries.V_QA_CODES);
      qualityAssuranceCodesTable = new SortableTable<QualityAssuranceCodeEntry>(columnModel, null,
            new SortableTableComparator<QualityAssuranceCodeEntry>("code", true));

      qualityAssuranceCodesTable.setRowSelector(new DataTableRowSelector("selectedRow", true));
      codeCol.setSortable(true);
      descCol.setSortable(true);
      qualityAssuranceCodesTable.setList(getQualitControlCodeEntries());
      qualityAssuranceCodesTable.initialize();
   }

   /**
    * @return Quality Assurance code table entries
    */
   private List<QualityAssuranceCodeEntry> getQualitControlCodeEntries()
   {
      Set<QualityAssuranceCode> modelQACodes = null;
      modelQACodes = this.getActivityInstance().getActivity().getAllQualityAssuranceCodes();
      List<QualityAssuranceCodeEntry> qaCodeEntries = new ArrayList<QualityAssuranceCodeEntry>();
      long modelOID = this.getActivityInstance().getModelOID();
      if (CollectionUtils.isNotEmpty(modelQACodes))
      {
         for (QualityAssuranceCode code : modelQACodes)
         {
            // sometimes if we create and later delete QA codes from
            // model, code may be null
            if (null != code)
            {
               qaCodeEntries.add(new QualityAssuranceCodeEntry(code, modelOID));
            }
         }
      }
      return qaCodeEntries;
   }

   /**
    * returns selected QA codes while performing Pass/Fail the QA Activity
    * 
    * @return
    */
   private Set<QualityAssuranceCode> getSelectedQualityAssuranceCodes()
   {
      Set<QualityAssuranceCode> selectedQACodes = new HashSet<QualityAssuranceCode>();
      List<QualityAssuranceCodeEntry> qaCodeEntries;
      // auto-complete
      if (isAutoCompleteMode())
      {
         qaCodeEntries = qualityACAutocompleteMultiSelector.getSelectedValues();
      }
      else
      {// pick up from the list
         qaCodeEntries = qualityAssuranceCodesTable.getList();
      }

      if (CollectionUtils.isNotEmpty(qaCodeEntries))
      {
         for (QualityAssuranceCodeEntry qualityAssuranceCode : qaCodeEntries)
         {
            if (isAutoCompleteMode() || qualityAssuranceCode.isSelectedRow())
            {
               selectedQACodes.add(qualityAssuranceCode.getQualityAssuranceCode());
            }
         }
      }
      return selectedQACodes;
   }

   /**
    * @return
    */
   private static ServiceFactory getServiceFactory()
   {
      SessionContext sessionContext = ServiceFactoryUtils.getSessionContext();
      return (sessionContext != null) ? sessionContext.getServiceFactory() : null;
   }

   public String getHeader()
   {
      if (QAAction.PASS.equals(qaAction))
      {
         return MessagePropertiesBean.getInstance().get("views.qualityAssuranceActivityDialog.header.pass");
      }
      else
      {
         return MessagePropertiesBean.getInstance().get("views.qualityAssuranceActivityDialog.header.fail");
      }
   }
   
   public boolean isShowQACodesSection()
   {
      if (isQACodesAvailable() && (isFailQAActivity() || (isPassQAActivity() && isCorrectionMade())))
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   private boolean isQACodesAvailable()
   {
      if (CollectionUtils.isNotEmpty(qualityAssuranceCodesTable.getList()))
      {
         return true;
      }
      return false;
   }
      
   public boolean isAutoCompleteMode()
   {
      if (SelectQACodesMode.AUTO_COMPLETE.equals(selectQACodesMode))
      {
         return true;
      }
      return false;
   }

   public boolean isListMode()
   {
      if (SelectQACodesMode.MULTI_SELECT_TABLE.equals(selectQACodesMode))
      {
         return true;
      }
      return false;
   }

   public boolean isPassQAActivity()
   {
      if (QAAction.PASS.equals(qaAction))
      {
         return true;
      }
      return false;
   }

   public boolean isFailQAActivity()
   {
      if (QAAction.FAIL.equals(qaAction))
      {
         return true;
      }
      return false;
   }

   public boolean isCorrectionMade()
   {
      if ("true".equals(correctionMadeOption))
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   public String getNote()
   {
      return note;
   }

   public void setNote(String note)
   {
      this.note = note;
   }

   public QAAction getQaAction()
   {
      return qaAction;
   }

   public void setQaAction(QAAction qaAction)
   {
      this.qaAction = qaAction;
   }

   public ActivityInstance getActivityInstance()
   {
      return activityInstance;
   }

   public void setActivityInstance(ActivityInstance activityInstance)
   {
      this.activityInstance = activityInstance;
   }

   public View getParentView()
   {
      return parentView;
   }

   public void setParentView(View parentView)
   {
      this.parentView = parentView;
   }

   public SortableTable<QualityAssuranceCodeEntry> getQualityAssuranceCodesList()
   {
      return qualityAssuranceCodesTable;
   }

   public boolean isAssignToLastPerformer()
   {
      return assignToLastPerformer;
   }

   public void setAssignToLastPerformer(boolean assignToLastPerformer)
   {
      this.assignToLastPerformer = assignToLastPerformer;
   }

   public String getCorrectionMadeOption()
   {
      return correctionMadeOption;
   }

   public void setCorrectionMadeOption(String correctionMadeOption)
   {
      this.correctionMadeOption = correctionMadeOption;
   }

   public Map<String, ? > getOutData()
   {
      return outData;
   }

   public void setOutData(Map<String, ? > outData)
   {
      this.outData = outData;
   }

   public QualityACAutocompleteMultiSelector getQualityACAutocompleteMultiSelector()
   {
      return qualityACAutocompleteMultiSelector;
   }

   public void setQualityACAutocompleteMultiSelector(
         QualityACAutocompleteMultiSelector qualityACAutocompleteMultiSelector)
   {
      this.qualityACAutocompleteMultiSelector = qualityACAutocompleteMultiSelector;
   }

   public ValidationMessageBean getValidationMessageBean()
   {
      return validationMessageBean;
   }
}