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
package org.eclipse.stardust.ui.web.viewscommon.views.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteSelector.IAutocompleteSelectorListener;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IFilterHandler;
import org.eclipse.stardust.ui.web.common.table.ISearchHandler;
import org.eclipse.stardust.ui.web.common.table.ISortHandler;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.user.ParticipantAutocompleteSelector;
import org.eclipse.stardust.ui.web.viewscommon.user.ParticipantWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;


import com.icesoft.faces.component.selectinputtext.SelectInputText;

public class ParticipantHelper implements IUserObjectBuilder<ParticipantTableEntry>
{
   private static final String EMPTY_STRING = "";
   private static final int ALL_TYPES = 0;
   private static final int ROLE_TYPE = 1;
   private static final int ORGANIZATION_TYPE = 2;
   private static ParticipantComparator PARTICIPANT_COMPARATOR = new ParticipantComparator();
   private final List<SelectItem> typeFilters;
   private final MessagesViewsCommonBean viewsCommonMessageBean = MessagesViewsCommonBean.getInstance();
   private final List<ParticipantWrapper> selectedParticipantList = new ArrayList<ParticipantWrapper>();
   private PARTICIPANT_VIEW_TYPE viewType;
   private PaginatorDataTable<ParticipantTableEntry, ModelParticipant> participantTable;
   private final ParticipantAutocompleteSelector participantSelector;
   private String nameFilter = EMPTY_STRING;
   private int typeFilterAutoComplete;
   private int typeFilterList;

   /**
    * Constructor
    */
   public ParticipantHelper()
   {
      viewType = PARTICIPANT_VIEW_TYPE.LIST;
      typeFilters = CollectionUtils.newList();
      typeFilters.add(new SelectItem(ALL_TYPES, viewsCommonMessageBean.getString("delegation.allTypes")));
      typeFilters.add(new SelectItem(ROLE_TYPE, viewsCommonMessageBean.getString("delegation.roles")));
      typeFilters.add(new SelectItem(ORGANIZATION_TYPE, viewsCommonMessageBean.getString("delegation.orgs")));
      participantSelector = createParticipantSelector();
   }

   /**
    * method to create table
    */
   public void createTable()
   {
      List<ColumnPreference> participantFixedCols = new ArrayList<ColumnPreference>();

      ColumnPreference nameCol = new ColumnPreference("Participant", "name", ColumnDataType.STRING,
            viewsCommonMessageBean.getString("views.authorizationManagerView.participantTable.column.participant"),
            true, false);

      ColumnPreference typeCol = new ColumnPreference("Type", "type", ColumnDataType.STRING,
            viewsCommonMessageBean.getString("views.authorizationManagerView.participantTable.column.type"), true,
            false);

      participantFixedCols.add(nameCol);
      participantFixedCols.add(typeCol);

      IColumnModel participantColumnModel = new DefaultColumnModel(null, participantFixedCols, null, "admin",
            "participant");

      ISearchHandler searchHandler = new ParticipantSearchHandler();
      IFilterHandler filterHandler = null;
      ISortHandler iSortHandler = null;
      participantTable = new PaginatorDataTable<ParticipantTableEntry, ModelParticipant>(participantColumnModel,
            searchHandler, filterHandler, iSortHandler, this, new DataTableSortModel<ParticipantTableEntry>("name",
                  false));
      participantTable.setRowSelector(new DataTableRowSelector("selected", true));
      participantTable.setISearchHandler(searchHandler);
      participantTable.initialize();
   }

   /**
    *
    */
   public ParticipantTableEntry createUserObject(Object resultRow)
   {
      if (resultRow instanceof Participant)
      {
         return new ParticipantTableEntry((Participant) resultRow);
      }

      return null;
   }

   public List<Participant> getAddedParticipants()
   {
      List<Participant> selectedParticipants = new ArrayList<Participant>();

      if (PARTICIPANT_VIEW_TYPE.LIST.equals(viewType))
      {
         List<ParticipantTableEntry> list = getParticipantTable().getList();

         for (ParticipantTableEntry tableEntry : list)
         {
            if (tableEntry.isSelected())
            {
               selectedParticipants.add(tableEntry.getParticipant());
            }
         }
      }
      else
      {
         for (ParticipantWrapper paricipant : selectedParticipantList)
         {
            selectedParticipants.add((Participant) paricipant.getParticipantInfo());
         }
      }

      return selectedParticipants;
   }

   public String getNameFilter()
   {
      return nameFilter;
   }

   public ParticipantAutocompleteSelector getParticipantSelector()
   {
      return participantSelector;
   }

   public PaginatorDataTable<ParticipantTableEntry, ModelParticipant> getParticipantTable()
   {
      return participantTable;
   }

   public List<ParticipantWrapper> getSelectedParticipantList()
   {
      return selectedParticipantList;
   }

   public int getTypeFilterAutoComplete()
   {
      return typeFilterAutoComplete;
   }

   public int getTypeFilterList()
   {
      return typeFilterList;
   }

   public List<SelectItem> getTypeFilters()
   {
      return typeFilters;
   }

   public PARTICIPANT_VIEW_TYPE getViewType()
   {
      return viewType;
   }

   public void initialize()
   {
      viewType = PARTICIPANT_VIEW_TYPE.LIST;
      refresh();
   }

   /**
    * Value change listener for nameFilter.
    * 
    * @param event
    */
   public void nameFilterValueChangeListener(final ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();

         return;
      }
      handleFilterCriteriaChangeEvent(event);
   }

   public void refresh()
   {
      participantTable.initialize();
      participantTable.refresh();
      reset();
   }

   public void reset()
   {     
      selectedParticipantList.clear();
      typeFilterAutoComplete = ALL_TYPES;
      typeFilterList = ALL_TYPES;
      participantTable.refresh();
   }

   /**
    * 
    * @param evt
    */
   public void removeRow(ActionEvent evt)
   {
      ParticipantWrapper row = (ParticipantWrapper) evt.getComponent().getAttributes().get("participant");
      selectedParticipantList.remove(row);
   }

   public void setNameFilter(String nameFilter)
   {
      this.nameFilter = nameFilter;
   }

   public void setParticipantTable(PaginatorDataTable<ParticipantTableEntry, ModelParticipant> participantTable)
   {
      this.participantTable = participantTable;
   }

   public void setTypeFilterAutoComplete(int typeFilterAutoComplete)
   {
      this.typeFilterAutoComplete = typeFilterAutoComplete;
   }

   public void setTypeFilterList(int typeFilterList)
   {
      this.typeFilterList = typeFilterList;
   }

   public void setViewType(PARTICIPANT_VIEW_TYPE viewType)
   {
      this.viewType = viewType;
   }

   /**
    * Toggles between search and select dialog. Resets the view being switched to so that
    * a fresh search can be made.
    */
   public void toggleView()
   {
      viewType = PARTICIPANT_VIEW_TYPE.LIST.equals(viewType)
            ? PARTICIPANT_VIEW_TYPE.AUTOCOMPLETE
            : PARTICIPANT_VIEW_TYPE.LIST;
      reset();
   }

   /**
    * Value change listener for typeFilter.
    * 
    * @param event
    */
   public void typeFilterValueChangeListener(final ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();

         return;
      }
      handleFilterCriteriaChangeEvent(event);
   }

   /**
    * 
    * @return
    */
   private ParticipantAutocompleteSelector createParticipantSelector()
   {
      final ParticipantAutocompleteSelector autoCompleteSelector = new ParticipantAutocompleteSelector(
            new ParticipantsDataProvider(), true);
      autoCompleteSelector.setMaxRows(5);
      autoCompleteSelector.setSearchValue(EMPTY_STRING);
      autoCompleteSelector.setAutocompleteSelectorListener(new IAutocompleteSelectorListener()
      {
         public void actionPerformed(SelectInputText autoComplete, SelectItem selectedItem)
         {
            if (selectedItem.getValue() instanceof ParticipantWrapper) // Safety check
            {
               autoComplete.setValue(EMPTY_STRING);
               if (!selectedParticipantList.contains(selectedItem.getValue()))
               {
                  selectedParticipantList.add((ParticipantWrapper) selectedItem.getValue());
               }

            }
         }
      });

      return autoCompleteSelector;
   }

   /**
    * 
    * @return
    */
   private List<Participant> findParticipants(String filterValue, int type)
   {
      List<Participant> typeFilteredList = findParticipantsByType(type);

      // String filterValue = getNameFilter();
      String regex = (!StringUtils.isEmpty(filterValue)) ? (filterValue.replaceAll("\\*", ".*") + ".*") : "";

      // Compile the pattern first as we are using this multiple times below in for
      // loop
      Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

      if (!StringUtils.isEmpty(regex))
      {
         List<Participant> nameFilteredList = CollectionUtils.newArrayList();

         for (Participant participant : typeFilteredList)
         {
            if (pattern.matcher(I18nUtils.getParticipantName(participant)).matches())
            {
               nameFilteredList.add(participant);
            }
         }

         Collections.sort(nameFilteredList, PARTICIPANT_COMPARATOR);

         return nameFilteredList;
      }
      else
      {
         Collections.sort(typeFilteredList, PARTICIPANT_COMPARATOR);

         return typeFilteredList;
      }
   }

   /**
    * 
    * @param participantList
    * @return
    */
   private List<Participant> findParticipantsByType(int typeFilter)
   {
      List<Participant> participantList = ParticipantUtils.getAllUnScopedModelParticipant(true);

      List<Participant> filteredList = CollectionUtils.newArrayList();

      switch (typeFilter)
      {
      case ROLE_TYPE:

         for (Participant participant : participantList)
         {
            if (participant instanceof Role)
            {
               filteredList.add(participant);
            }
         }

         break;

      case ORGANIZATION_TYPE:

         for (Participant participant : participantList)
         {
            if (participant instanceof Organization)
            {
               filteredList.add(participant);
            }
         }

         break;

      default:
         filteredList = participantList;

         break;
      }

      return filteredList;
   }

   /**
    * Checks of the given event is in 'Invoke application' phase. If not, sets the phase
    * Id to invoke application. If present, then invokes the retrieveFilteredData
    * function.
    * 
    * @param event
    *           - given event
    */
   private void handleFilterCriteriaChangeEvent(final FacesEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();

         return;
      }

      participantTable.refresh();
   }

   /**
    *
    */
   public class ParticipantSearchHandler extends IppSearchHandler<Participant>
   {
      private static final long serialVersionUID = 6208755392414522634L;

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.jsf.common.ISearchHandler#createQuery()
       */
      public Query createQuery()
      {
         return UserQuery.findAll();
      }

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.jsf.common.ISearchHandler#performSearch(org.eclipse.stardust.engine.api.query
       * .Query)
       */
      public QueryResult<Participant> performSearch(Query query)
      {
         List<Participant> participants = findParticipants(getNameFilter(), typeFilterList);

         return new RawQueryResult<Participant>(participants, null, false, new Long(participants.size()));
      }
   }

   /**
    * 
    * @author Vikas.Mishra
    * @version $Revision: $
    */
   class ParticipantsDataProvider implements IAutocompleteDataProvider
   {
      public List<SelectItem> getMatchingData(String searchValue, int maxMatches)
      {
         List<SelectItem> selectItems = new ArrayList<SelectItem>();
         Collection<Participant> participantList = findParticipants(searchValue, getTypeFilterAutoComplete());

         for (Participant participant : participantList)
         {
            selectItems.add(new SelectItem(new ParticipantWrapper(participant), I18nUtils
                  .getParticipantName(participant)));
         }

         return selectItems;
      }
   }
}

enum PARTICIPANT_VIEW_TYPE {
   AUTOCOMPLETE, LIST, NONE;
}

/**
 * Participant Comparator to compare by names
 */
class ParticipantComparator implements Comparator<Participant>
{
   public int compare(Participant part1, Participant part2)
   {
      return part1.getName().compareTo(part2.getName());
   }
}
