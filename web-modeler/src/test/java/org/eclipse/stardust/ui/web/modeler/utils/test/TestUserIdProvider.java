package org.eclipse.stardust.ui.web.modeler.utils.test;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import org.eclipse.stardust.ui.web.modeler.common.UserIdProvider;

public class TestUserIdProvider implements UserIdProvider
{
   private final String userId;

   private final String displayName;

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

   @Override
   public String getCurrentUserId()
   {
      return userId;
   }

   @Override
   public String getLoginName()
   {
      return userId;
   }

   @Override
   public String getFirstName()
   {
      return "";
   }

   @Override
   public String getLastName()
   {
      return isEmpty(displayName) ? userId : displayName;
   }

   @Override
   public String getCurrentUserDisplayName()
   {
      return isEmpty(displayName) ? userId : displayName;
   }

   @Override
   public boolean isAdministrator()
   {
      return "admin".equals(userId);
   }
}
