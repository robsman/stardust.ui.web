package org.eclipse.stardust.ui.web.rest.component.util;

import java.util.Date;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserGroupQuery;
import org.eclipse.stardust.engine.api.query.UserGroups;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.ui.web.rest.common.Options;
import org.springframework.stereotype.Component;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */
@Component
public class UserGroupUtils
{

   private static final String COL_USERGROUP_NAME = "name";

   private static final String COL_USERGROUP_OID = "oid";

   private static final String COL_USERGROUP_ID = "id";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   /**
    * @param options
    * @return
    */
   public UserGroups getAllUserGroups(Options options)
   {
      UserGroupQuery userGroupQuery = UserGroupQuery.findAll();

      addSortCriteria(userGroupQuery, options);

      SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip, true);
      userGroupQuery.setPolicy(subsetPolicy);
      return serviceFactoryUtils.getQueryService().getAllUserGroups(userGroupQuery);
   }

   /**
    * @param id
    * @return
    */
   public UserGroup getUserGroup(String id)
   {
      return serviceFactoryUtils.getUserService().getUserGroup(id);
   }

   /**
    * @param userGroup
    * @return
    */
   public UserGroup modifyUserGroup(UserGroup userGroup)
   {
      return serviceFactoryUtils.getUserService().modifyUserGroup(userGroup);
   }

   /**
    * @param id
    * @return
    */
   public UserGroup deleteUserGroup(String id)
   {
      return serviceFactoryUtils.getUserService().invalidateUserGroup(id);
   }

   /**
    * @param id
    * @param name
    * @param description
    * @param validFrom
    * @param validTo
    * @return
    */
   public UserGroup createUserGroup(String id, String name, String description, Long validFrom, Long validTo)
   {
      Date validFromDate = (validFrom != null) ? new Date(validFrom) : null;
      Date validToDate = (validTo != null) ? new Date(validTo) : null;
      UserGroup createUserGroup = null;

      createUserGroup = serviceFactoryUtils.getUserService().createUserGroup(id, name, description, validFromDate,
            validToDate);

      return createUserGroup;
   }

   /**
    * @return
    */
   public long getActiveUserGroupCount()
   {
      UserGroupQuery userGroupQueryActive = UserGroupQuery.findActive();
      return serviceFactoryUtils.getQueryService().getAllUserGroups(userGroupQueryActive).size();
   }

   /**
    * @param userGroupQuery
    * @param options
    */
   private void addSortCriteria(UserGroupQuery userGroupQuery, Options options)
   {
      if (COL_USERGROUP_NAME.equals(options.orderBy))
      {
         userGroupQuery.orderBy(UserGroupQuery.NAME, options.asc);
      }
      else if (COL_USERGROUP_OID.equals(options.orderBy))
      {
         userGroupQuery.orderBy(UserGroupQuery.OID, options.asc);
      }
      else if (COL_USERGROUP_ID.equals(options.orderBy))
      {
         userGroupQuery.orderBy(UserGroupQuery.ID, options.asc);
      }
   }

}
