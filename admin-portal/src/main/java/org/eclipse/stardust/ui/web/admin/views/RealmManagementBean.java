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
package org.eclipse.stardust.ui.web.admin.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.UserRealm;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.admin.AdminLocalizerKey;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnRenderType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;



/**
 * @author ankita.patel
 * @version $Revision: $
 */
public class RealmManagementBean extends PopupUIComponentBean implements ViewEventHandler
{

   private static final long serialVersionUID = 1L;

   protected final static String GLOBAL_MESSAGE_ID = "globalMessage";

   private AdminMessagesPropertiesBean propsBean;

   private WorkflowFacade workflowFacade;

   private List<RealmMgmtTableEntry> userRealms;

   private SortableTable<RealmMgmtTableEntry> userRealmsTable;

   private String id;

   private String name;

   private String description;

   boolean fireCloseEvent = true;

   boolean modifyRealm = false;

   /**
    * 
    */
   public RealmManagementBean()
   {
      super(ResourcePaths.V_realmMgmt);
      workflowFacade = (WorkflowFacade) SessionContext.findSessionContext()
            .lookup(AdminportalConstants.WORKFLOW_FACADE);
      propsBean = AdminMessagesPropertiesBean.getInstance();

      List<ColumnPreference> realmFixedCols = new ArrayList<ColumnPreference>();
      ColumnPreference selectCol = new ColumnPreference("Select", "selectedRow",
            ColumnDataType.BOOLEAN, propsBean.getString("views.common.column.select"),
            true, false);
      selectCol.setColumnRenderType(ColumnRenderType.READ_WRITE);
      selectCol.setColumnAlignment(ColumnAlignment.CENTER);
      selectCol.setExportable(false);
      realmFixedCols.add(selectCol);

      ColumnPreference nameCol = new ColumnPreference("Name", "name", this.getMessages()
            .getString("userRealmsTable.column.name"), ResourcePaths.V_REALCOLUMNS_VIEW,
            true, true);

      ColumnPreference idCol = new ColumnPreference("Id", "id", ColumnDataType.STRING,
            this.getMessages().getString("userRealmsTable.column.id"), true, false);

      ColumnPreference descrCol = new ColumnPreference("Description", "description",
            ColumnDataType.STRING, this.getMessages().getString(
                  "userRealmsTable.column.descr"), true, false);

      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      cols.add(nameCol);
      cols.add(idCol);
      cols.add(descrCol);

      IColumnModel realmColumnModel = new DefaultColumnModel(cols, realmFixedCols, null,
            UserPreferencesEntries.M_ADMIN, UserPreferencesEntries.V_REALM);
      TableColumnSelectorPopup colSelecPopup = new TableColumnSelectorPopup(
            realmColumnModel);

      userRealmsTable = new SortableTable<RealmMgmtTableEntry>(null, colSelecPopup, null,
            new SortableTableComparator<RealmMgmtTableEntry>("name", true));
      userRealmsTable.initialize();

      initialize();
   }

   @Override
   public void initialize()
   {
      userRealms = createUserRealms();
      userRealmsTable.setList(userRealms);
      userRealmsTable.initialize();
   }

   /**
    * Deletes selected User realm
    * 
    * @param ae
    */
   public void deleteUserRealm()
   {

      List<RealmMgmtTableEntry> userRealms = userRealmsTable.getList();
      try
      {
         for (Iterator<RealmMgmtTableEntry> iterator = userRealms.iterator(); iterator.hasNext();)
         {
            RealmMgmtTableEntry realmMgmtTableEntry = (RealmMgmtTableEntry) iterator.next();
            if (realmMgmtTableEntry.isSelectedRow())
            {
               UserRealm realm = realmMgmtTableEntry.getUserRealm();
               String id = realm != null ? realm.getId() : null;
               if (id != null)
               {
                  workflowFacade.getServiceFactory().getUserService().dropUserRealm(id);
               }
            }
         }
         initialize();
      }
      catch (AccessForbiddenException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }

   /**
    * on apply, Creates user realm
    */
   public void onApply()
   {
      try
      {
         ServiceFactory serviceFactory = SessionContext.findSessionContext().getServiceFactory();
         UserService service = serviceFactory != null ? serviceFactory.getUserService() : null;

         if (!modifyRealm)
         {
            UserRealm userRealm = service != null ? service.createUserRealm(id, name, description) : null;
            if (userRealm != null)
            {
               initialize();
               closePopup();
            }
            else
            {
               closePopup();
               FacesMessage fm = new javax.faces.application.FacesMessage(
                     javax.faces.application.FacesMessage.SEVERITY_WARN, Localizer.getString(
                           AdminLocalizerKey.KEY_CANNOT_CREATE_REALM, "REALMID", id), "");
               MessageDialog.addErrorMessage(this.getMessages().getString("cannotCreateRealm" + " '" + id + " '"));
               FacesContext.getCurrentInstance().addMessage(
                     GLOBAL_MESSAGE_ID,
                     new javax.faces.application.FacesMessage(javax.faces.application.FacesMessage.SEVERITY_WARN,
                           Localizer.getString(AdminLocalizerKey.KEY_CANNOT_CREATE_REALM, "REALMID", id), ""));
            }
         }
         else
         {
            closePopup();

         }

      }
      catch (AccessForbiddenException e)
      {
         MessageDialog.addErrorMessage(this.getMessages().getString("accessForbiddenException"));
      }
      catch (Exception e)
      {
         closePopup();
         ExceptionHandler.handleException(e);
      }

   }

   /**
    * Returns selected items count
    * 
    * @return
    */
   public int getSelectedItemCount()
   {
      int count = 0;
      if (userRealmsTable != null)
      {
         List<RealmMgmtTableEntry> userRealms = userRealmsTable.getList();

         for (RealmMgmtTableEntry realmMgmtTableEntry : userRealms)
         {
            if (realmMgmtTableEntry.isSelectedRow())
               count++;

         }
      }

      return count;

   }

   /**
    * close popup
    */
   public void closePopup()
   {
      modifyRealm = false;
      this.id = "";
      this.name = "";
      this.description = "";
      fireCloseEvent = true;
      FacesUtils.refreshPage();
      super.closePopup();
   }

   /**
    * Creates list of UserRealms
    * 
    * @return userRealms
    */
   private List<RealmMgmtTableEntry> createUserRealms()
   {
      userRealms = new ArrayList<RealmMgmtTableEntry>();
      List<UserRealm> realms = workflowFacade.getServiceFactory().getUserService().getUserRealms();
      for (Iterator<UserRealm> iterator = realms.iterator(); iterator.hasNext();)
      {
         Object object = (Object) iterator.next();
         if (object instanceof UserRealm)
         {
            UserRealm ur = (UserRealm) object;
            userRealms.add(new RealmMgmtTableEntry(ur, ur.getId(), ur.getName(), ur.getDescription(), false));
         }
      }
      return userRealms;
   }

   // ********************* Default Getter & Setter Methods *********************
   public SortableTable<RealmMgmtTableEntry> getUserRealmsTable()
   {
      return userRealmsTable;
   }

   public void setUserRealmsTable(SortableTable<RealmMgmtTableEntry> userRealmsTable)
   {
      this.userRealmsTable = userRealmsTable;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public boolean isModifyRealm()
   {
      return modifyRealm;
   }

   public void setModifyRealm(boolean modifyRealm)
   {
      this.modifyRealm = modifyRealm;
   }

   /**    
    * 
    */
   private void createRealmTable()
   {
      List<ColumnPreference> realmFixedCols = new ArrayList<ColumnPreference>();
      ColumnPreference nameCol = new ColumnPreference("Name", "name", this.getMessages().getString(
            "userRealmsTable.column.name"), ResourcePaths.V_REALCOLUMNS_VIEW, true, true);

      ColumnPreference idCol = new ColumnPreference("Id", "id", ColumnDataType.STRING, this.getMessages().getString(
            "userRealmsTable.column.id"), true, false);

      ColumnPreference descrCol = new ColumnPreference("Description", "description", ColumnDataType.STRING, this
            .getMessages().getString("userRealmsTable.column.descr"), true, false);

      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      cols.add(nameCol);
      cols.add(idCol);
      cols.add(descrCol);

      IColumnModel realmColumnModel = new DefaultColumnModel(cols, realmFixedCols, null,
            UserPreferencesEntries.M_ADMIN, UserPreferencesEntries.V_REALM);
      TableColumnSelectorPopup colSelecPopup = new TableColumnSelectorPopup(realmColumnModel);

      userRealmsTable = new SortableTable<RealmMgmtTableEntry>(null, colSelecPopup, null,
            new SortableTableComparator<RealmMgmtTableEntry>("name", true));
      userRealmsTable.setRowSelector(new DataTableRowSelector("selectedRow", true));
      userRealmsTable.initialize();

   }

   /**
 * 
 */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         createRealmTable();

      }
      else if (ViewEventType.TO_BE_ACTIVATED == event.getType())
      {

         initialize();
      }

   }

}
