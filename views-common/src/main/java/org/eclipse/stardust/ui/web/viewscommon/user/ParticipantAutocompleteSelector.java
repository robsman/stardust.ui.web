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
package org.eclipse.stardust.ui.web.viewscommon.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.common.autocomplete.AutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;

import com.icesoft.faces.component.selectinputtext.SelectInputText;

/**
 * @author Shrikant.Gangal
 *
 */
public class ParticipantAutocompleteSelector extends AutocompleteMultiSelector<ParticipantWrapper>
{
   private static final long serialVersionUID = 1L;
   private static final int MAX_MATCHES_TO_DISPLAY = 15;
   private static final int MIN_CHARS_TO_INVOKE_SEARCH = 1;
   private boolean singleSelect;
   protected boolean showAutocompletePanel;

   protected boolean showSelectedList;
   protected IAutocompleteMultiSelectorListener<ParticipantWrapper> autocompleteMultiSelectorListener;
   
   protected boolean showOnlineIndicator;
   protected boolean showProfileImage;
   protected SortableTable<ParticipantWrapper> selectedParticipantsTable;

   protected List<SelectItem> visibleMatchingData;

   /**
    * @param provider
    * @param autoCompleteListner
    */
   public ParticipantAutocompleteSelector(final IAutocompleteDataProvider provider,
         final IAutocompleteSelectorListener autoCompleteListner)
   {
      super(MAX_MATCHES_TO_DISPLAY, MIN_CHARS_TO_INVOKE_SEARCH);
      setDataProvider(provider);
      autocompleteContentUrl = ResourcePaths.V_AUTOCOMPLETE_SINGLE_PARTICIPANT_SELECTOR;
      setAutocompleteSelectorListener(autoCompleteListner);
   }
   
   /**
    * @param provider
    * @param autoCompleteListner
    */
   public ParticipantAutocompleteSelector(final IAutocompleteDataProvider provider, boolean singleSelect)
   {
      super(MAX_MATCHES_TO_DISPLAY, MIN_CHARS_TO_INVOKE_SEARCH);
      setDataProvider(provider);
      //setAutocompleteSelectorListener(autoCompleteListner);
      this.singleSelect = singleSelect;
      setAutocompleteContentUrl(singleSelect ? ResourcePaths.V_AUTOCOMPLETE_SINGLE_PARTICIPANT_SELECTOR : ResourcePaths.V_AUTOCOMPLETE_MULTIPLE_PARTICIPANT_SELECTOR);
      
      if(!singleSelect)
      {
         showSelectedList = true;
         showAutocompletePanel = true;
         setSelectedDataContentUrl(ResourcePaths.V_AUTOCOMPLETE_PARTICIPANT_SELECTOR_TABLE);
         setAutocompleteSelectorListener(new IAutocompleteSelectorListener()
         {
            public void actionPerformed(SelectInputText autoComplete, SelectItem selectedItem)
            {
               if (selectedItem.getValue() instanceof ParticipantWrapper) // Safety check
               {
                  ParticipantWrapper participantWrapper = (ParticipantWrapper) selectedItem.getValue();
                  addSelectedParticipant(participantWrapper);
                  autoComplete.setValue(null);
               }
            }
         });
         initializeSelectedParticipantsTable();
      }
   }

   /**
    * To check if the selected participant is an individual user or a role/dept etc.
    * 
    * @return
    */
   public boolean isSelectedParticipantAUser()
   {
      if (selectedItem != null)
      {
         ParticipantWrapper pw = (ParticipantWrapper) selectedItem.getValue();
         if (pw != null)
         {
            return pw.isParticipantAUser();
         }
      }

      return false;
   }

   /**
    * Returns the User profile image URL for the user.
    * 
    * @return
    */
   public String getUserImageURL()
   {
      if (selectedItem != null)
      {
         ParticipantWrapper pw = (ParticipantWrapper) selectedItem.getValue();
         if (pw != null)
         {
            return pw.getUserImageURL();
         }
      }

      return null;
   }

   /**
    * Overridden - to first check if the given event is in INVOKE_APPLICATION phase - this
    * is needed as the participant data provider uses bean members and we must wait till
    * their values are set.
    * 
    */
   public void searchValueChanged(ValueChangeEvent event)
   {
      try
      {
         if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
         {
            event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
            event.queue();
            return;
         }
         else
         {
            if (event.getComponent() instanceof SelectInputText)
            {
               SelectInputText autoComplete = (SelectInputText) event.getComponent();
               selectedItem = autoComplete.getSelectedItem();
               if (selectedItem != null)
               {
                  setSelectedItem(autoComplete, selectedItem);
               }
               else
               {
                  String newWord = (String) event.getNewValue();
                  newWord = newWord.trim();

                  if (StringUtils.isNotEmpty(newWord) && selectedItem == null)
                  {
                     if (newWord.length() >= minCharacters)
                     {
                        matchingData = dataProvider.getMatchingData(newWord, maxRows);
                        
                        /* It's observed that if we enter some text in the search box that doesn't
                         * fetch any matches, then the autoComplete drop down continues to show the previous successful match
                         * (if any) on the UI, while the results in the backing bean are reset.
                         * To avoid problems if a user selects an item from such phantom list, we cache the previous successful matching
                         * result set and use it for selection. */
                        if (!CollectionUtils.isEmpty(matchingData))
                        {                        
                           visibleMatchingData = matchingData;
                        }
                     }
                     else
                     {
                        matchingData = null;
                     }
                     
                     /* In 5.3.x it's observed that the 'selectedItem' is not set as expected after user selects an item from the
                      * autoComplete drop down list (autoComplete.getSelectedItem() returns null).
                      * To get around this problem we match the search string (which would be the label of the selected item) with
                      * the list of matching items from previous successful search to identify the selected item and set it. */
                     if (!CollectionUtils.isEmpty(visibleMatchingData))
                     {
                        Iterator iter = visibleMatchingData.iterator();
                        while (iter.hasNext())
                        {
                           SelectItem item = (SelectItem) iter.next();
                           if (item.getLabel().equals(newWord))
                           {
                              selectedItem = item;
                              setSelectedItem(autoComplete, selectedItem);
                              break;
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
         ExceptionHandler.handleException(e);
      }
   }
   
   /**
    * @param participant
    */
   public void removeSelectedparticipant(ParticipantWrapper participant)
   {
      validateMultiSelectMode();

      List<ParticipantWrapper> participants = selectedParticipantsTable.getList();
      participants.remove(participant);
      setSelectedValues(participants);

      // Fire Event
      if(autocompleteMultiSelectorListener != null)
      {
         autocompleteMultiSelectorListener.dataRemoved(participant);
      }
   }
   
   /**
    * @param participant
    */
   public void addSelectedParticipant(ParticipantWrapper participant)
   {
      validateMultiSelectMode();

      List<ParticipantWrapper> participants = selectedParticipantsTable.getList();
      if(!participants.contains(participant))
      {
         participant.setAutocompleteParticipantSelector(this);
         participants.add(participant);
         setSelectedValues(participants);
         setSearchValue("");
   
         // Fire Event
         if(autocompleteMultiSelectorListener != null)
         {
            autocompleteMultiSelectorListener.dataAdded(participant);
         }
      }
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#getSelectedValues()
    */
   public List<ParticipantWrapper> getSelectedValues()
   {
      validateMultiSelectMode();
      
      return selectedParticipantsTable.getList();
   }


   /**
    * @return
    */
   public SortableTable<ParticipantWrapper> getSelectedParticipantsTable()
   {
      return selectedParticipantsTable;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#getSelectedValuesAsString()
    */
   public List<String> getSelectedValuesAsString()
   {
      validateMultiSelectMode();
      
      List<String> stringList = new ArrayList<String>();

      List<ParticipantWrapper> list = getSelectedValues();
      for (ParticipantWrapper participantWrapper : list)
      {
         stringList.add(participantWrapper.getText());
      }

      return stringList;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#setAutocompleteMultiSelectorListener(IAutocompleteMultiSelectorListener)
    */
   public void setAutocompleteMultiSelectorListener(IAutocompleteMultiSelectorListener<ParticipantWrapper> autocompleteMultiSelectorListener)
   {
      validateMultiSelectMode();
      this.autocompleteMultiSelectorListener = autocompleteMultiSelectorListener;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#setSelectedValues(java.util.List)
    */
   public void setSelectedValues(List<ParticipantWrapper> selectedValues)
   {
      validateMultiSelectMode();
      selectedParticipantsTable.setList(selectedValues);
   }
   
   /**
    * @return
    */
   public boolean isShowAutocompletePanel()
   {
      return showAutocompletePanel;
   }

   /**
    * @param showAutocompletePanel
    */
   public void setShowAutocompletePanel(boolean showAutocompletePanel)
   {
      this.showAutocompletePanel = showAutocompletePanel;
   }

   /**
    * @return
    */
   public boolean isShowOnlineIndicator()
   {
      return showOnlineIndicator;
   }

   /**
    * @param showOnlineIndicator
    */
   public void setShowOnlineIndicator(boolean showOnlineIndicator)
   {
      this.showOnlineIndicator = showOnlineIndicator;
   }

   /**
    * @return
    */
   public boolean isShowProfileImage()
   {
      return showProfileImage;
   }

   /**
    * @param showProfileImage
    */
   public void setShowProfileImage(boolean showProfileImage)
   {
      this.showProfileImage = showProfileImage;
   }

   
   /**
    * @return
    */
   public boolean isShowSelectedList()
   {
      return showSelectedList;
   }

   /**
    * @param showSelectedList
    */
   public void setShowSelectedList(boolean showSelectedList)
   {
      this.showSelectedList = showSelectedList;
   }

   /**
    * 
    */
   private void validateMultiSelectMode()
   {
      if(singleSelect)
      {
         throw new IllegalAccessError("Does not support Multi Selection");
      }
   }
   
   /**
    * @return
    */
   public boolean isSingleSelect()
   {
      return singleSelect;
   }

   /**
    * 
    */
   private void initializeSelectedParticipantsTable()
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      ColumnPreference colProfileIcon = new ColumnPreference("ProfileIcon", "", "",
            ResourcePaths.V_AUTOCOMPLETE_PARTICIPANT_SELECTOR_TABLE_COLUMNS, true, false);
      colProfileIcon.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colParticipant = new ColumnPreference("ParticipantName", "text", propsBean
            .getString("views.participantAutocomplete.selectedParticipant.table.column.name"),
            ResourcePaths.V_AUTOCOMPLETE_PARTICIPANT_SELECTOR_TABLE_COLUMNS, true, true);
      colParticipant.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colCloseIcon = new ColumnPreference("Actions", "", "",
            ResourcePaths.V_AUTOCOMPLETE_PARTICIPANT_SELECTOR_TABLE_COLUMNS, true, false);
      colCloseIcon.setColumnAlignment(ColumnAlignment.CENTER);

      cols.add(colProfileIcon);
      cols.add(colParticipant);
      cols.add(colCloseIcon);

      IColumnModel columnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_VIEWS_COMMON, UserPreferencesEntries.V_PARTICIPANT_AUTOCOMPLETE);

      selectedParticipantsTable = new SortableTable<ParticipantWrapper>(columnModel, null,
            new SortableTableComparator<ParticipantWrapper>("text", true));

      selectedParticipantsTable.setList(new ArrayList<ParticipantWrapper>());
      selectedParticipantsTable.initialize();
   }
   
   private void setSelectedItem(SelectInputText autoComplete, SelectItem selectedItem)
   {
      autoComplete.setValue(selectedItem.getLabel());
      if (autocompleteSelectorListener != null)
      {
         autocompleteSelectorListener.actionPerformed(autoComplete, selectedItem);
      }
   }
}