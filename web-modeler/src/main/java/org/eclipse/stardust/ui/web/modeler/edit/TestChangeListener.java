package org.eclipse.stardust.ui.web.modeler.edit;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;

@Component
@Scope("singleton")
public class TestChangeListener implements IChangeListener
{
   private static final Logger logger = LogManager.getLogger(TestChangeListener.class);

   @Override
   public void onCommand(EditingSession session, JsonObject commandJson)
   {
      logger.debug("[session: " + session.getId() + "] - command: " + commandJson);
   }

}
