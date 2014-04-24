package org.eclipse.stardust.ui.web.modeler.service.rest;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;
import org.eclipse.stardust.ui.web.modeler.edit.recording.ModelChangeRecording;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.portal.ModelChangeRecordingController;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerRecordingsRestController.ModelerRecordingJto.State;

@Path("/modeler/{randomPostFix}/recordings")
public class ModelerRecordingsRestController
{
   private static final Logger trace = LogManager
         .getLogger(ModelerRecordingsRestController.class);

   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ApplicationContext springContext;

   public ModelerRecordingJto toJto(ModelChangeRecording recording)
   {
      ModelerRecordingJto jto = new ModelerRecordingJto();

      // TODO
      jto.recordingId = recording.getId();
      jto.state = recording.isActive() ? State.active : State.saved;
      jto.affectedModels = newArrayList();
      for (ModelChangeRecording.Step step : recording.getSteps())
      {
         if ( !jto.affectedModels.contains(step.commandJto.modelId))
         {
            jto.affectedModels.add(step.commandJto.modelId);
         }
      }


      jto.steps = newArrayList();
      for (ModelChangeRecording.Step step : recording.getSteps())
      {
         RecordingStepJto stepJto = new RecordingStepJto();
         stepJto.kind = RecordingStepJto.KIND_CHANGE;
         stepJto.id = "c" + step.id;
         stepJto.commandId = step.commandJto.commandId;
         stepJto.command = step.commandJto;

         jto.steps.add(stepJto);
      }

      return jto;
   }

   public JsonObject toJson(ModelerRecordingJto recordingJto)
   {
      return jsonIo.gson().toJsonTree(recordingJto).getAsJsonObject();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public String listRecordings()
   {
      ModelChangeRecordingController controller = resolveRecordingsController();

      JsonArray recordings = new JsonArray();
      for (ModelChangeRecording recording : controller.listRecordings())
      {
         recordings.add(jsonIo.gson().toJsonTree(toJto(recording)));
      }

      return jsonIo.gson().toJson(recordings);
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   public String startNewRecording()
   {
      ModelChangeRecordingController controller = resolveRecordingsController();

      ModelChangeRecording recording = controller.startRecording();

      return jsonIo.gson().toJson(toJto(recording));
   }

   @POST
   @PUT // consolidate to one method
   @Path("/{id}/state")
   @Produces(MediaType.APPLICATION_JSON)
   public String stopRecording(@PathParam("id") String recordingId)
   {
      ModelChangeRecordingController controller = resolveRecordingsController();

      ModelChangeRecording recording = controller.stopRecording(recordingId);

      if (null == recording)
      {
         throw new WebApplicationException(Status.NOT_FOUND);
      }

      return jsonIo.gson().toJson(toJto(recording));
   }

   @GET
   @Path("/{id}")
   @Produces(MediaType.APPLICATION_JSON)
   public String retrieveRecording(@PathParam("id") String recordingId)
   {
      ModelChangeRecordingController controller = resolveRecordingsController();

      ModelChangeRecording recording = controller.findRecording(recordingId);

      if (null == recording)
      {
         throw new WebApplicationException(Status.NOT_FOUND);
      }

      return jsonIo.gson().toJson(toJto(recording));
   }

   @DELETE
   @Path("/{id}")
   @Produces(MediaType.APPLICATION_JSON)
   public String deleteRecording(@PathParam("id") String recordingId)
   {
      ModelChangeRecordingController controller = resolveRecordingsController();

      ModelChangeRecording recording = controller.deleteRecording(recordingId);

      if (null == recording)
      {
         throw new WebApplicationException(Status.NOT_FOUND);
      }

      return jsonIo.gson().toJson(toJto(recording));
   }


   private ModelChangeRecordingController resolveRecordingsController()
   {
      return springContext.getBean(ModelChangeRecordingController.class);
   }

   public static class ModelerRecordingJto
   {
      public static enum State {
         active, saved
      }

      public String recordingId;

      public State state;

      public List<String> affectedModels;

      public List<RecordingStepJto> steps;
   }

   public static class RecordingStepJto
   {
      public static final String KIND_CHANGE = "change";
      public static final String KIND_NOTE = "note";

      public String id;

      public String kind;

      public String commandId;

      public String note;

      public CommandJto command;
   }
}