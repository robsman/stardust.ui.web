package org.eclipse.stardust.ui.web.modeler.edit.recording;

import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.ui.web.modeler.edit.IChangeListener;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;

@Service
@Scope("session")
public class ModelChangeRecorder implements IChangeListener
{
   private CopyOnWriteArrayList<ModelChangeRecording> currentRecordings = new CopyOnWriteArrayList<ModelChangeRecording>();

   public ModelChangeRecording startRecording()
   {
      ModelChangeRecording recording = new ModelChangeRecording(this);

      currentRecordings.add(recording);

      return recording;
   }

   @Override
   public void onCommand(EditingSession session, CommandJto commandJto, JsonObject changeJson)
   {
      for (ModelChangeRecording recording : currentRecordings)
      {
         recording.addChange(commandJto);
      }
   }

   void stopRecording(ModelChangeRecording recording)
   {
      currentRecordings.remove(recording);
   }
}
