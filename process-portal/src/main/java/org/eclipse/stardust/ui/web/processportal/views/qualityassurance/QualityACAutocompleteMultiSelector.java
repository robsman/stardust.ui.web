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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.model.QualityAssuranceCode;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.common.autocomplete.AutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.processportal.common.ResourcePaths;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;

import com.icesoft.faces.component.selectinputtext.SelectInputText;

/**
 * @author Yogesh.Manware
 * 
 */
public class QualityACAutocompleteMultiSelector extends AutocompleteMultiSelector<QualityAssuranceCodeEntry>
{
   private static final long serialVersionUID = 1L;

   public static final int MAX_ROWS = 5;
   public static final int MIN_CHARACTERS = 1;

   protected DataTable<QualityAssuranceCodeEntry> selectedQACodesTable;
   protected IAutocompleteMultiSelectorListener<QualityAssuranceCodeEntry> autocompleteMultiSelectorListener;
   protected boolean showAutocompletePanel;
   private ActivityInstance activityInstance;

   public QualityACAutocompleteMultiSelector(ActivityInstance instance)
   {
      super(MAX_ROWS, MIN_CHARACTERS);
      activityInstance = instance;
      this.setAutocompleteContentUrl(ResourcePaths.V_AUTOCOMPLETE_QUALITY_SELECTOR_TABLE);
      this.setSelectedDataContentUrl(ResourcePaths.V_AUTOCOMPLETE_QUALITY_SELECTED_QAC_TABLE);
      this.setDataProvider(new AutocompleteQualityACSelectorDataProvider());
      showAutocompletePanel = true;
      super.setAutocompleteSelectorListener(new AutocompleteQualityACSelectorListener());
      initializeSelectedQACodesTable();
   }

   @Override
   public void setAutocompleteSelectorListener(IAutocompleteSelectorListener autocompleteSelectorListener)
   {
      super.setAutocompleteSelectorListener(autocompleteSelectorListener);
   }

   /**
    * @param qualityAssuranceCode
    */
   public void removeSelectedQualityAssuranceCodes(QualityAssuranceCodeEntry qualityAssuranceCode)
   {
      List<QualityAssuranceCodeEntry> codes = selectedQACodesTable.getList();
      codes.remove(qualityAssuranceCode);
      setSelectedValues(codes);

      // Fire Event
      if (autocompleteMultiSelectorListener != null)
      {
         autocompleteMultiSelectorListener.dataRemoved(qualityAssuranceCode);
      }
   }

   /**
    * @param qualityACode
    */
   public void addSelectedQACode(QualityAssuranceCodeEntry qualityACode)
   {
      List<QualityAssuranceCodeEntry> qualityACodes = selectedQACodesTable.getList();
      if (!qualityACodes.contains(qualityACode))
      {
         qualityACode.setCodesAutocompleteMultiSelector(this);
         qualityACodes.add(qualityACode);
         setSelectedValues(qualityACodes);

         // Fire Event
         if (autocompleteMultiSelectorListener != null)
         {
            autocompleteMultiSelectorListener.dataAdded(qualityACode);
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#getSelectedData
    * ()
    */
   public List<QualityAssuranceCodeEntry> getSelectedValues()
   {
      return selectedQACodesTable.getList();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#setSelectedData
    * (java.util.List)
    */
   public void setSelectedValues(List<QualityAssuranceCodeEntry> selectedValues)
   {
      selectedQACodesTable.setList(selectedValues);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#
    * getSelectedDataAsString()
    */
   public List<String> getSelectedValuesAsString()
   {
      List<String> stringList = new ArrayList<String>();

      List<QualityAssuranceCodeEntry> list = getSelectedValues();
      for (QualityAssuranceCodeEntry codeEntry : list)
      {
         stringList.add(codeEntry.getDescription());
      }
      return stringList;
   }

   /**
    * 
    */
   private void initializeSelectedQACodesTable()
   {
      ColumnPreference codeDesc = new ColumnPreference("codeDesc", "codeDesc", "",
            ResourcePaths.V_AUTOCOMPLETE_QUALITY_SELECTOR_TABLE_COLUMNS, true, false);

      ColumnPreference actions = new ColumnPreference("actions", "actions", "",
            ResourcePaths.V_AUTOCOMPLETE_QUALITY_SELECTOR_TABLE_COLUMNS, true, false);
      actions.setColumnAlignment(ColumnAlignment.CENTER);

      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      cols.add(codeDesc);
      cols.add(actions);

      IColumnModel columnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_WORKFLOW,
            UserPreferencesEntries.V_QA_CODES);

      selectedQACodesTable = new DataTable<QualityAssuranceCodeEntry>(columnModel, null);

      selectedQACodesTable.setList(new ArrayList<QualityAssuranceCodeEntry>());
      selectedQACodesTable.initialize();
   }

   public boolean isShowAutocompletePanel()
   {
      return showAutocompletePanel;
   }

   public void setShowAutocompletePanel(boolean showAutocompletePanel)
   {
      this.showAutocompletePanel = showAutocompletePanel;
   }

   public DataTable<QualityAssuranceCodeEntry> getSelectedQACodesTable()
   {
      return selectedQACodesTable;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#
    * setAutocompleteMultiSelectorListener
    * (org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector
    * .IAutocompleteMultiSelectorListener)
    */
   public void setAutocompleteMultiSelectorListener(
         IAutocompleteMultiSelectorListener<QualityAssuranceCodeEntry> autocompleteMultiSelectorListener)
   {
      this.autocompleteMultiSelectorListener = autocompleteMultiSelectorListener;
   }

   /**
    * @author Yogesh.Manware
    * 
    */
   public class AutocompleteQualityACSelectorDataProvider implements IAutocompleteDataProvider
   {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider#getMatchingData
       * (java.lang.String, int)
       */
      public List<SelectItem> getMatchingData(String searchValue, int maxMatches)
      {
         // manipulate search result
         long modelOID = activityInstance.getModelOID();
         Set<QualityAssuranceCode> allQACodes = activityInstance.getActivity().getAllQualityAssuranceCodes();
         Set<QualityAssuranceCodeEntry> searchResult = new HashSet<QualityAssuranceCodeEntry>();
         // prepare SelectItem list
         List<SelectItem> qaItems = new ArrayList<SelectItem>(searchResult.size());

         for (QualityAssuranceCode qualityAssuranceCode : allQACodes)
         {
            if (null != qualityAssuranceCode)
            {
               QualityAssuranceCodeEntry qualityACodeEntry = new QualityAssuranceCodeEntry(qualityAssuranceCode,
                     modelOID);

               if (qualityAssuranceCode.getCode().toLowerCase().contains(searchValue.toLowerCase())
                     || qualityACodeEntry.getDescription().toLowerCase().contains(searchValue.toLowerCase()))
               {
                  if (!searchResult.contains(qualityACodeEntry))
                  {
                     searchResult.add(qualityACodeEntry);
                     qaItems.add(new SelectItem(qualityACodeEntry, qualityACodeEntry.getDescription()));
                  }
               }
            }
         }
         // sort codes
         Comparator<SelectItem> comp = new Comparator<SelectItem>()
         {
            public int compare(SelectItem selectItem1, SelectItem selectItem2)
            {
               QualityAssuranceCodeEntry codeEntry1 = (QualityAssuranceCodeEntry) selectItem1.getValue();
               QualityAssuranceCodeEntry codeEntry2 = (QualityAssuranceCodeEntry) selectItem2.getValue();
               return codeEntry1.compareTo(codeEntry2);
            }
         };
         Collections.sort(qaItems, comp);

         return qaItems;
      }
   }

   /**
    * @author Yogesh.Manware
    * 
    */
   public class AutocompleteQualityACSelectorListener implements IAutocompleteSelectorListener
   {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteListener#actionPerformed
       * (javax.faces.model.SelectItem)
       */
      public void actionPerformed(SelectInputText autoComplete, SelectItem selectedItem)
      {
         if (selectedItem.getValue() instanceof QualityAssuranceCodeEntry)
         {
            QualityAssuranceCodeEntry qualityAssuranceCodeEntry = (QualityAssuranceCodeEntry) selectedItem.getValue();
            addSelectedQACode(qualityAssuranceCodeEntry);
            autoComplete.setValue(null);
            setSearchValue("");
         }
      }
   }
}