package org.eclipse.stardust.ui.web.modeler.utils.test;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import org.eclipse.stardust.ui.web.modeler.common.UserIdProvider;

public class TestUserIdProvider implements UserIdProvider
{
   private final String userId;

   private final String displayName;

   private UserIdProvider override;

   public static final TestUserIdProvider testUser(String userId)
   {
      return testUser(userId, null);
   }

   public static final TestUserIdProvider testUser(String userId, String displayName)
   {
      return new TestUserIdProvider(userId, displayName);
   }

   public TestUserIdProvider(String userId)
   {
      this(userId, null);
   }

   public TestUserIdProvider(String userId, String displayName)
   {
      this.userId = userId;
      this.displayName = displayName;
   }

   public void setOverride(UserIdProvider override)
   {
      this.override = override;
   }

   @Override
   public String getCurrentUserId()
   {
      return (null != override) ? override.getCurrentUserId() : userId;
   }

   @Override
   public String getLoginName()
   {
      return (null != override) ? override.getLoginName() : userId;
   }

   @Override
   public String getFirstName()
   {
      return (null != override) ? override.getFirstName() : "";
   }

   @Override
   public String getLastName()
   {
      return (null != override) ? override.getLastName() : isEmpty(displayName)
            ? userId
            : displayName;
   }

   @Override
   public String getCurrentUserDisplayName()
   {
      return (null != override)
            ? override.getCurrentUserDisplayName()
            : isEmpty(displayName) ? userId : displayName;
   }

   @Override
   public boolean isAdministrator()
   {
      return (null != override) ? override.isAdministrator() : "admin".equals(userId);
   }
}
