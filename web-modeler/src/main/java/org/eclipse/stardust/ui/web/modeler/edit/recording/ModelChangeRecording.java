package org.eclipse.stardust.ui.web.modeler.edit.recording;

import static java.util.Collections.unmodifiableList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;
import java.util.UUID;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;

public class ModelChangeRecording
{
   private static final Logger trace = LogManager.getLogger(ModelChangeRecording.class);

   private final String id = UUID.randomUUID().toString();

   private ModelChangeRecorder recorder;

   private final List<Step> changes = newArrayList();

   public ModelChangeRecording(ModelChangeRecorder modelChangeRecorder)
   {
      this.recorder = modelChangeRecorder;
   }

   public String getId()
   {
      return id;
   }

   public boolean isActive()
   {
      return null != recorder;
   }

   public List<Step> getSteps()
   {
      return unmodifiableList(changes);
   }

   public List<Step> stopRecording()
   {
      recorder.stopRecording(this);

      this.recorder = null;

      if (changes.isEmpty())
      {
         trace.warn("Generating test data ...");
         long nSteps = 2 + Math.round(8 * Math.random());
         for (int i = 0; i < nSteps; i++)
         {
            CommandJto jto = new CommandJto();
            jto.commandId = "modelElement.update";
            jto.modelId = "TestModel";

            changes.add(new Step(changes.size() + 1, jto));
         }
      }
      return getSteps();
   }

   void addChange(CommandJto commandJto)
   {
      changes.add(new Step(changes.size() + 1, commandJto));

      // TODO extract affected model's IDs
   }

   public static class Step
   {
      public final int id;

      public final CommandJto commandJto;

      public Step(int id, CommandJto commandJto)
      {
         this.id = id;
         this.commandJto = commandJto;
      }
   }
}
