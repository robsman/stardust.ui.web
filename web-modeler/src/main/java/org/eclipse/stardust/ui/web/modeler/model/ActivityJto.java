package org.eclipse.stardust.ui.web.modeler.model;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class ActivityJto extends ModelElementJto
{
   public ActivityJto()
   {
      this.type = ModelerConstants.ACTIVITY_KEY;
   }

   public String activityType;

   public String participantFullId;
   public String applicationFullId;
   public String subprocessFullId;
}