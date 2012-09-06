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
package org.eclipse.stardust.ui.web.common.columnSelector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.ui.web.common.ICallbackHandler;
import org.eclipse.stardust.ui.web.common.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.configuration.PreferencesScopesHelper;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.PopupDialog;
import org.eclipse.stardust.ui.web.common.util.StringUtils;



/**
 * @author Ankita.Patel
 * 
 */
public class TableColumnSelectorPopup extends PopupDialog
{
   private static final long serialVersionUID = 1L;

   private IColumnModel columnModel;
   private List<ColumnPreference> columns;
   
   private PreferencesScopesHelper prefScopesHelper;
   private PreferenceScope selectedPreferenceScope;
   private Set<String> columnsPreviouslyVisible;

   private boolean updatePreferences = true;
   private ICallbackHandler callbackHandler;
   
   /**
    * @param treeTable
    * @param title
    * @param columnModel
    */
   public TableColumnSelectorPopup(String title, IColumnModel columnModel)
   {
      super(title);
      this.columnModel = columnModel;
      setFireViewEvents(false);

      prefScopesHelper = new PreferencesScopesHelper();
      selectedPreferenceScope = prefScopesHelper.getSelectedPreferenceScope();

      buildData(columnModel.getSelectableColumns());

      if(StringUtils.isEmpty(this.getTitle()))
      {
         MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
         setTitle( propsBean.getString("common.filterPopup.selectColumnsLabel") );
      }
   }

   /**
    * @param treeTable
    * @param columnModel
    */
   public TableColumnSelectorPopup(IColumnModel columnModel)
   {
      this("", columnModel);
   }

   /**
    * 
    */
   public void initialize()
   {
      buildData(columnModel.getSelectableColumns());
   }
   
   @Override
   public void apply()
   {
      ColumnPreference columnPreference;
      List<ColumnPreference> finalCols = new ArrayList<ColumnPreference>();
      for (ColumnPreference cp : columns)
      {
         columnPreference = columnModel.getColumn(cp.getColumnName());
         columnPreference.setVisible(cp.getVisible());
         columnPreference.setNewlyVisible(isColumnNewlyVisible(cp));
         finalCols.add(columnPreference);
      }
      columnModel.setSelectableColumns(finalCols);

      setVisible(false);

      if (updatePreferences)
      {
         prefScopesHelper.setSelectedPreferenceScope(selectedPreferenceScope);
         columnModel.saveSelectableColumns(prefScopesHelper.getSelectedPreferenceScope());
      }
      if (null != callbackHandler)
      {
         callbackHandler.handleEvent(EventType.APPLY);
      }
   }

   @Override
   public void reset()
   {
      setVisible(false);
      prefScopesHelper.setSelectedPreferenceScope(selectedPreferenceScope);
      columnModel.resetSelectableColumns(prefScopesHelper.getSelectedPreferenceScope());
   }

   @Override
   public void openPopup()
   {
      selectedPreferenceScope = prefScopesHelper.getSelectedPreferenceScope();
      List<ColumnPreference> cols = columnModel
            .getSelectableColumnsForPreferenceScope(selectedPreferenceScope);
      buildData(cols);
      super.openPopup();
   }

   @Override
   public void closePopup()
   {
      super.closePopup();
   }

   /**
    * @param event
    */
   public void preferenceScopeValueChanged(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      } 

      PreferenceScope scope = selectedPreferenceScope != null
            ? selectedPreferenceScope
            : prefScopesHelper.getSelectedPreferenceScope();
      
      List<ColumnPreference> cols = columnModel.getSelectableColumnsForPreferenceScope(scope);
      buildData(cols);
   }

   /**
    * 
    */
   private void buildData(List<ColumnPreference> cols)
   {
      columns = new ArrayList<ColumnPreference>();
      columnsPreviouslyVisible = new HashSet<String>();
      ColumnPreference columnPreference;
      for (ColumnPreference cp : cols)
      {
         columnPreference = new ColumnPreference(cp.getColumnName(), cp.getColumnTitle());
         columnPreference.setVisible(cp.getVisible());
         columns.add(columnPreference);
         if (cp.getVisible())
         {
            columnsPreviouslyVisible.add(cp.getColumnName());
         }
      }
   }
   
   public IColumnModel getColumnModel()
   {
      return columnModel;
   }

   public List<ColumnPreference> getColumns()
   {
      return columns;
   }
   
   public PreferencesScopesHelper getPrefScopesHelper()
   {
      return prefScopesHelper;
   }

   public PreferenceScope getSelectedPreferenceScope()
   {
      return selectedPreferenceScope;
   }

   public void setSelectedPreferenceScope(PreferenceScope selectedPreferenceScope)
   {
      this.selectedPreferenceScope = selectedPreferenceScope;
   }
   
   private boolean isColumnNewlyVisible(ColumnPreference cp)
   {
      return (cp.getVisible() && !columnsPreviouslyVisible.contains(cp.getColumnName()));
   }

   public void setUpdatePreferences(boolean updatePreferences)
   {
      this.updatePreferences = updatePreferences;
   }

   public void setCallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }
}
