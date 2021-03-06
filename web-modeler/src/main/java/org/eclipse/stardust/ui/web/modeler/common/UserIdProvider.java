package org.eclipse.stardust.ui.web.modeler.common;

public interface UserIdProvider
{
   String getCurrentUserId();

   String getLoginName();

   String getFirstName();

   String getLastName();

   String getCurrentUserDisplayName();

   boolean isAdministrator();
}
