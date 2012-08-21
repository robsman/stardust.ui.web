package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.ui.web.modeler.service.streaming.JointModellingSessionsController.lookupModelChangeBroadcaster;

import org.atmosphere.cpr.Broadcaster;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;

@Component
@Scope("singleton")
public class ModelChangeBroadcaster implements IChangeListener
{
   private static final Logger trace = LogManager.getLogger(ModelChangeBroadcaster.class);

   @Override
   public void onCommand(EditingSession session, JsonObject commandJson)
   {
      Broadcaster b = lookupModelChangeBroadcaster(session.getId());
      if (null != b)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("About to broadcast model change: " + commandJson);
         }
         b.broadcast(commandJson);
      }
      else
      {
         trace.info("Skipping broadcast of model change (no subscribers):");
      }
   }

}
