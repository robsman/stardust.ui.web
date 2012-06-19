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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.IQueryExtender;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.table.IQuery;
import org.eclipse.stardust.ui.web.common.table.IQueryResult;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler;
import org.eclipse.stardust.ui.web.common.table.export.ExportType;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.ParticipantDepartmentPair;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppFilterHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppQueryResult;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class RoleAssignmentBean extends UIComponentBean
      implements ResourcePaths, ViewEventHandler, IUserObjectBuilder<RoleAssignmentTableEntry>
{
   private static final Logger trace = LogManager.getLogger(RoleAssignmentBean.class);

   private static final long serialVersionUID = 1L;
   private final static String QUERY_EXTENDER = "carnotBcRoleAssignment/queryExtender";
   private final static String COL_TEAM_MEMBER = "TeamMember";

   private static final int COLUMN_SIZE = 5;

   private SessionContext sessionCtx;

   private IQueryExtender queryExtender;

   private WorkflowFacade facade;

   private List<RoleAssignmentTableEntry> roleEntries;

   private List<GrantsAssignmentTableEntry> grantsEntries;

   private String filterNamePattern;

   private PaginatorDataTable<RoleAssignmentTableEntry, RoleAssignmentTableEntry> roleAssignmentTable;

   /**
    * 
    */
   public RoleAssignmentBean()
   {
      super(V_roleAssignment);

   }

   @Override
   public void initialize()
   {
      createTable();
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         sessionCtx = SessionContext.findSessionContext();
         queryExtender = getQueryExtender();
         facade = WorkflowFacade.getWorkflowFacade();
         initialize();

      }
   }

   /**
    * Refresh the table
    */
   public void update()
   {
      initialize();
   }

   /**
    * @param grant
    * @return
    */
   private QualifiedModelParticipantInfo getGrantParticipant(Grant grant)
   {
      ModelParticipantInfo modelParticipantInfo = null;

      DeployedModel deployedModel = ModelCache.findModelCache().getActiveModel(grant);
      // Organization grant
      if (grant.isOrganization())
      {
         if (grant.getDepartment() != null)
         {
            Organization organization = deployedModel.getOrganization(grant.getId());
            modelParticipantInfo = grant.getDepartment().getScopedParticipant(organization);
         }
         else
         {
            modelParticipantInfo = deployedModel.getOrganization(grant.getId());
         }
      }
      // Role grant
      else
      {
         if (grant.getDepartment() != null)
         {
            Role role = deployedModel.getRole(grant.getId());
            modelParticipantInfo = grant.getDepartment().getScopedParticipant(role);
         }
         else
         {
            modelParticipantInfo = deployedModel.getRole(grant.getId());
         }
      }

      return (QualifiedModelParticipantInfo) modelParticipantInfo;
   }

   /**
    * Creates the query to get User Details
    * 
    * @return query
    */
   public Query createQuery()
   {
      UserQuery query = facade.getTeamQuery(true);
      query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Full));
      if (queryExtender != null)
      {
         queryExtender.extendQuery(query);
      }
      return query;
   }

   public void attachQueryExtender(IQueryExtender queryExtender)
   {
      this.queryExtender = queryExtender;
      sessionCtx.bind(QUERY_EXTENDER, queryExtender);
   }

   // ********** Modified Getter Method**********
   public IQueryExtender getQueryExtender()
   {
      if (queryExtender == null)
      {
         return (IQueryExtender) sessionCtx.lookup(QUERY_EXTENDER);
      }
      return queryExtender;
   }

   /**
    * @author Subodh.Godbole
    * 
    */
   private class RoleAssignmentDataTableExportHandler implements DataTableExportHandler<RoleAssignmentTableEntry>
   {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleCellExport
       * (org.eclipse.stardust.ui.web.common.table.export.ExportType,
       * org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.Object,
       * java.lang.Object)
       */
      public Object handleCellExport(ExportType exportType, ColumnPreference column, RoleAssignmentTableEntry row,
            Object value)
      {
         if (COL_TEAM_MEMBER.equals(column.getColumnName()))
         {
            return row.getName();
         }
         else
         {
            try
            {
               Boolean object = (Boolean) ColumnModel.resolvePropertyAndInvokeGetter(row, column.getColumnProperty());
               MessagePropertiesBean props = MessagePropertiesBean.getInstance();
               return object ? props.getString("common.yes") : props.getString("common.no");
            }
            catch (Exception e)
            {
               trace.error("Unable to determin export value for Column = " + column.getColumnName(), e);
               return value;
            }
         }
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#
       * handleHeaderCellExport(org.eclipse.stardust.ui.web.common.table.export.ExportType,
       * org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.String)
       */
      public String handleHeaderCellExport(ExportType exportType, ColumnPreference column, String text)
      {
         return text;
      }
   }

   public class RoleAssignmentFilterHandler extends IppFilterHandler
   {
      private static final long serialVersionUID = -2173022668039757090L;

      @Override
      public void applyFiltering(Query query, List<ITableDataFilter> filters)
      {
         filterNamePattern = null;
         for (ITableDataFilter tableDataFilter : filters)
         {
            String filterName = tableDataFilter.getName();
            if (COL_TEAM_MEMBER.equals(filterName))
            {
               filterNamePattern = ((TableDataFilterSearch) tableDataFilter).getValue();
            }
         }
      }
   }

   public class RoleAssignmentSearchHandler extends IppSearchHandler<RoleAssignmentTableEntry>
   {
      private static final long serialVersionUID = -3543070769771871255L;

      @Override
      public Query createQuery()
      {
         return null;// No query for engine call
      }

      @Override
      public QueryResult<RoleAssignmentTableEntry> performSearch(Query query)
      {
         return new RawQueryResult<RoleAssignmentTableEntry>(roleEntries, null, false, Long.valueOf(roleEntries.size()));
      }

      @Override
      public IQueryResult<RoleAssignmentTableEntry> performSearch(IQuery iQuery, int startRow, int pageSize)
      {
         List<RoleAssignmentTableEntry> resultList = StringUtils.isEmpty(filterNamePattern)
               ? roleEntries
               : filterResult(roleEntries, filterNamePattern);

         List<RoleAssignmentTableEntry> result = getPaginatedSubList(resultList, startRow, pageSize);

         RawQueryResult<RoleAssignmentTableEntry> queryResult = new RawQueryResult<RoleAssignmentTableEntry>(result,
               null, false, Long.valueOf(resultList.size()));

         return new IppQueryResult<RoleAssignmentTableEntry>(queryResult);
      }

   }

   /**
    * 
    * @param list
    * @param filterNamePattern
    * @return
    */
   private List<RoleAssignmentTableEntry> filterResult(List<RoleAssignmentTableEntry> list, String filterNamePattern)
   {
      List<RoleAssignmentTableEntry> filteredList = CollectionUtils.newArrayList();
      String regex = filterNamePattern.replaceAll("\\*", ".*") + ".*";
      Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

      for (RoleAssignmentTableEntry row : list)
      {
         if (pattern.matcher(row.getName()).matches())
         {
            filteredList.add(row);
         }
      }
      return filteredList;
   }

   /**
    * 
    * @param list
    * @param startRow
    * @param pageSize
    * @return
    */
   private List<RoleAssignmentTableEntry> getPaginatedSubList(List<RoleAssignmentTableEntry> list, int startIndex,
         int pageSize)
   {
      int listSize = list.size();
      int toIndex = listSize > (startIndex + pageSize) ? startIndex + pageSize : listSize;
      List<RoleAssignmentTableEntry> result = list.subList(startIndex, toIndex);
      return result;
   }

   private void createTable()
   {
      Query query = createQuery();
      Users users = facade.getAllUsers((UserQuery) query);
      Map<ParticipantDepartmentPair, String> roleNameMap = CollectionUtils.newHashMap();
      roleEntries = CollectionUtils.newArrayList();

      Map<User, List<ParticipantDepartmentPair>> userPairMap = CollectionUtils.newHashMap();

      for (User user : users)
      {
         List<Grant> grantsTempList = user.getAllGrants();
         QualifiedModelParticipantInfo modelParticipantInfo = null;
         List<ParticipantDepartmentPair> pairList = CollectionUtils.newArrayList();

         // iterate to create ParticipantDepartmentPair
         for (Grant grant : grantsTempList)
         {
            modelParticipantInfo = getGrantParticipant(grant);
            ParticipantDepartmentPair participantDepartmentPair = ParticipantDepartmentPair
                  .getParticipantDepartmentPair(modelParticipantInfo);
            if (participantDepartmentPair != null)
            {
               // add role to pair list
               pairList.add(participantDepartmentPair);

               // add unique role to roleNameMap
               if (!roleNameMap.containsKey(participantDepartmentPair))
               {
                  roleNameMap.put(participantDepartmentPair, ModelHelper.getParticipantName(modelParticipantInfo));
               }
            }
         }
         // add pairList to userPair Map
         userPairMap.put(user, pairList);
      }
      // Now iterate users
      for (User user:users)
      {         
         List<ParticipantDepartmentPair> paitList = userPairMap.get(user);
         
         grantsEntries = CollectionUtils.newArrayList();
         Set<ParticipantDepartmentPair> roles = roleNameMap.keySet();
         for (ParticipantDepartmentPair participantDepartmentPair : roles)
         {
            boolean found = false;
            for (ParticipantDepartmentPair key : paitList)
            {
               if (participantDepartmentPair.equals(key))
               {
                  found = true;
                  break;
               }
            }

            grantsEntries.add(new GrantsAssignmentTableEntry(participantDepartmentPair, found));
         }
         roleEntries.add(new RoleAssignmentTableEntry(user, grantsEntries));
      }

      List<ColumnPreference> fixedCols = CollectionUtils.newArrayList();
      ColumnPreference nameCol = new ColumnPreference(COL_TEAM_MEMBER, "name", this.getMessages().getString(
            "column.teamMember"), V_roleAssignmentViewColumns, new TableDataFilterPopup(new TableDataFilterSearch()),
            true, true);
      fixedCols.add(nameCol);

      List<ColumnPreference> selCols = CollectionUtils.newArrayList();

      if (CollectionUtils.isNotEmpty(roleEntries))
      {
         List<GrantsAssignmentTableEntry> roleList = roleEntries.get(0).getGrants();
         int i = 0;
         boolean visible = true;
         String propertyMapping = "";
         ColumnPreference roleName;
         for (GrantsAssignmentTableEntry re : roleList)
         {
            visible = i >= COLUMN_SIZE ? false : true;
            propertyMapping = "grants[" + i + "].userInRole";
            roleName = new ColumnPreference(re.getParticipantDepartmentPair().getFirst() + "_"
                  + re.getParticipantDepartmentPair().getSecond(), propertyMapping, roleNameMap.get(re
                  .getParticipantDepartmentPair()), V_roleAssignmentViewColumns, visible, false);

            roleName.setColumnAlignment(ColumnAlignment.CENTER);
            selCols.add(roleName);
            i++;
         }
      }
      IColumnModel columnModel = new DefaultColumnModel(selCols, fixedCols, null, UserPreferencesEntries.M_BCC,
            UserPreferencesEntries.V_ROLE_ASSIGNMENT);
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      IppSearchHandler<RoleAssignmentTableEntry> searchHandler = new RoleAssignmentSearchHandler();
      IppFilterHandler filterHandler = new RoleAssignmentFilterHandler();

      roleAssignmentTable = new PaginatorDataTable<RoleAssignmentTableEntry, RoleAssignmentTableEntry>(colSelecpopup,
            searchHandler, null, this);
      roleAssignmentTable.setISearchHandler(searchHandler);
      roleAssignmentTable.setIFilterHandler(filterHandler);
      roleAssignmentTable.setDataTableExportHandler(new RoleAssignmentDataTableExportHandler());
      roleAssignmentTable.initialize();
      roleAssignmentTable.refresh(true);

      userPairMap = null;
      users = null;
   }

   public RoleAssignmentTableEntry createUserObject(Object resultRow)
   {
      return (RoleAssignmentTableEntry) resultRow;
   }

   public PaginatorDataTable<RoleAssignmentTableEntry, RoleAssignmentTableEntry> getRoleAssignmentTable()
   {
      return roleAssignmentTable;
   }

}
