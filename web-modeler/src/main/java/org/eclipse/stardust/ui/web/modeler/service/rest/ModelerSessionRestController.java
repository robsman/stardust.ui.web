package org.eclipse.stardust.ui.web.modeler.service.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.common.BadRequestException;
import org.eclipse.stardust.ui.web.modeler.common.ConflictingRequestException;
import org.eclipse.stardust.ui.web.modeler.common.ItemNotFoundException;
import org.eclipse.stardust.ui.web.modeler.common.ModelingSessionLocator;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController.ChangeJto;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController.ContentProvider;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController.ModelFormat;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController.ModelLockJto;

@Path("/modeler/{randomPostFix}/sessions")
public class ModelerSessionRestController
{
   private static final Logger trace = LogManager.getLogger(ModelerSessionRestController.class);

   @Resource
   private ApplicationContext springContext;

   @Context
   private UriInfo uriInfo;

   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelingSessionLocator sessionLocator;

   @Resource
   private ModelerSessionController controller;

   private static final Logger logger = LogManager.getLogger(ModelerSessionRestController.class);

   public ModelerSessionRestController()
   {}

   public ModelerSessionRestController(UriInfo uriInfo)
   {
      this.uriInfo = uriInfo;
   }

   public String toChangeUri(String changeId)
   {
      return uriInfo.getAbsolutePath().toString() + "/changes/" + changeId;
   }

   public JsonObject toJson(ChangeJto changeJto)
   {
      return jsonIo.gson().toJsonTree(changeJto).getAsJsonObject();
   }

   @GET
   @Path("/modelState/{modelId}/current")
   @Produces(MediaType.APPLICATION_XML)
   public StreamingOutput getCurrentModelState(@PathParam("modelId") String modelId)
   {
      return getCurrentModelState(modelId, ModelFormat.Native);
   }

   @GET
   @Path("/modelState/{modelId}/deployable")
   @Produces(MediaType.APPLICATION_XML)
   public StreamingOutput getCurrentDeployableModelState(@PathParam("modelId") String modelId)
   {
      return getCurrentModelState(modelId, ModelFormat.Xpdl);
   }

   private StreamingOutput getCurrentModelState(String modelId, ModelFormat modelFormat)
   {
      // TODO exception transformation
      final ContentProvider contentProvider = controller.getCurrentModelState(modelId, ModelFormat.Xpdl);
      return new StreamingOutput()
      {
         @Override
         public void write(OutputStream output) throws IOException, WebApplicationException
         {
            contentProvider.writeContent(output);
         }
      };
   }

   @GET
   @Path("/changes/mostCurrent")
   @Produces(MediaType.APPLICATION_JSON)
   public String showCurrentChange()
   {
      JsonObject result = new JsonObject();
      EditingSession editingSession = currentSession().getSession();
      if (editingSession.canUndo())
      {
         Modification pendingUndo = editingSession.getPendingUndo();
         ChangeJto jto = controller.toJto(pendingUndo);

         jto.pendingUndo = toChangeUri(pendingUndo.getId());
         if (editingSession.canRedo())
         {
            Modification pendingRedo = editingSession.getPendingRedo();
            jto.pendingRedo = toChangeUri(pendingRedo.getId());
         }
         // TODO this ignores the previously prepared jto, is this wanted?
         result = toJson(controller.toJto(pendingUndo));
      }

      return jsonIo.writeJsonObject(result);
   }

   @POST
   @Path("/changes/mostCurrent/navigation")
   public Response adjustMostCurrentChange(String action)
   {
      ChangeJto jto;
      try
      {
         if ("undoMostCurrent".equals(action))
         {
            jto = controller.undoMostCurrentChange();
         }
         else if ("redoLastUndo".equals(action))
         {
            jto = controller.redoMostCurrentlyUndoneChange();
         }
         else
         {
            return Response //
                  .status(Status.BAD_REQUEST) //
                  .entity("Invalid navigation action: " + action) //
                  .build();
         }

         if (null != jto.pendingUndoableChange)
         {
            jto.pendingUndo = toChangeUri(jto.pendingUndoableChange.id);
         }

         if (null != jto.pendingRedoableChange)
         {
            jto.pendingRedo = toChangeUri(jto.pendingRedoableChange.id);
         }

         return Response //
               .ok(jsonIo.writeJsonObject(toJson(jto)), MediaType.APPLICATION_JSON_TYPE) //
               .build();
      }
      catch (ConflictingRequestException cre)
      {
         return Response //
               .status(Status.CONFLICT) //
               .entity(cre.getMessage()) //
               .build();
      }
   }

   @GET
   @Path("/editLock")
   @Produces(MediaType.APPLICATION_JSON)
   public String getEditLocksStatus()
   {
      List<ModelLockJto> jtos = controller.getEditLocksStatus();

      return jsonIo.gson().toJson(jtos);
   }

   @GET
   @Path("/editLock/{modelId}")
   @Produces(MediaType.APPLICATION_JSON)
   public String getEditLockStatus(@PathParam("modelId") final String modelId)
   {
      ModelLockJto jto = controller.getEditLockStatus(modelId);
      return jsonIo.gson().toJson(jto);
   }

   @DELETE
   @Path("/editLock/{modelId}")
   @Produces(MediaType.APPLICATION_JSON)
   public String breakEditLockForModel(@PathParam("modelId") final String modelId)
   {
      ModelLockJto jto = controller.breakEditLockForModel(modelId);
      return jsonIo.gson().toJson(jto);
   }

   @POST
   @Path("/changes")
   public Response applyChange(String postedData)
   {
      logger.debug("postedData ==============> " + postedData);
      try
      {
         CommandJto commandJto = jsonIo.gson().fromJson(postedData, CommandJto.class);
         ChangeJto outcome = controller.applyChange(commandJto);

         return Response.created(URI.create(toChangeUri(outcome.id))) //
               .entity(jsonIo.gson().toJson(outcome)) //
               .build();
      }
      catch (BadRequestException bre)
      {
         return Response.status(Status.BAD_REQUEST).entity(bre.getMessage()).build();
      }
      catch (ConflictingRequestException cre)
      {
         return Response.status(Status.CONFLICT).entity(cre.getMessage()).build();
      }
      catch (ItemNotFoundException infe)
      {
         return Response.status(Status.NOT_FOUND).entity(infe.getMessage()).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return Response.serverError().entity(e).build();
      }
   }

   private ModelingSession currentSession()
   {
      return sessionLocator.currentModelingSession();
   }
}