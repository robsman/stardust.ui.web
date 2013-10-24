package org.eclipse.stardust.ui.web.modeler.common;

public interface UserIdProvider
{
   String getCurrentUserId();

   String getCurrentUserDisplayName();

   boolean isAdministrator();
}
