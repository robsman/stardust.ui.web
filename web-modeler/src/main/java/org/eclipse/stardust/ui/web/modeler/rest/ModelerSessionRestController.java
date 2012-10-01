package org.eclipse.stardust.ui.web.modeler.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.rest.RestControllerUtils.resolveSpringBean;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.BeansException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.common.UnsavedModelsTracker;
import org.eclipse.stardust.ui.web.modeler.edit.SimpleCommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.edit.model.element.ModelChangeCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.postprocessing.ChangesetPostprocessingService;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.eclipse.stardust.ui.web.modeler.spi.ModelNavigator;

@Path("/modeler/{randomPostFix}/sessions")
public class ModelerSessionRestController
{
   private static final Logger trace = LogManager.getLogger(ModelerSessionRestController.class);

   @Context
   private ServletContext servletContext;

   @Context
   private UriInfo uriInfo;

   public String toChangeUri(Modification change)
   {
      return uriInfo.getAbsolutePath().toString() + "/changes/" + change.getId();
   }

   public ChangeJto toJto(Modification change)
   {
      ChangeJto jto = new ChangeJto();

      jto.id= change.getId();
      jto.timestamp = System.currentTimeMillis();

      if (change.getMetadata().containsKey("commandId"))
      {
          jto.commandId = change.getMetadata().get("commandId");
      }
      if (change.getMetadata().containsKey("modelId"))
      {
          jto.modelId= change.getMetadata().get("modelId");
      }
      if (change.getMetadata().containsKey("account"))
      {
          jto.account = change.getMetadata().get("account");
      }

      ModelMarshaller marshaller;
      if ( !isEmpty(jto.modelId))
      {
         EObject model = modelService().currentSession()
               .modelRepository()
               .findModel(jto.modelId);
         marshaller = modelService().currentSession()
               .modelRepository()
               .getModelBinding(model)
               .getMarshaller();
      }
      else
      {
         marshaller = modelService().modelElementMarshaller();
      }

      for (EObject changedObject : change.getModifiedElements())
      {
         jto.changes.modified.add(marshaller.toJson(changedObject));
      }
      for (EObject addedObject : change.getAddedElements())
      {
         jto.changes.added.add(marshaller.toJson(addedObject));
      }
      for (EObject removedObject : change.getRemovedElements())
      {
         jto.changes.removed.add(marshaller.toJson(removedObject));
      }

      return jto;
   }

   public JsonObject toJson(ChangeJto changeJto)
   {
      return jsonIo().gson().toJsonTree(changeJto).getAsJsonObject();
   }

   @GET
   @Path("/changes/mostCurrent")
   @Produces(MediaType.APPLICATION_JSON)
   public String showCurrentChange()
   {
      JsonObject result = new JsonObject();
      EditingSession editingSession = modelService().currentSession().getSession();
      if (editingSession.canUndo())
      {
         Modification pendingUndo = editingSession.getPendingUndo();
         ChangeJto jto = toJto(pendingUndo);

         jto.pendingUndo = toChangeUri(pendingUndo);
         if (editingSession.canRedo())
         {
            Modification pendingRedo = editingSession.getPendingRedo();
            jto.pendingRedo = toChangeUri(pendingRedo);
         }
         result = toJson(toJto(pendingUndo));
      }

      return jsonIo().writeJsonObject(result);
   }

   @POST
   @Path("/changes/mostCurrent/navigation")
   public Response adjustMostCurrentChange(String action)
   {
      if ("undoMostCurrent".equals(action))
      {
         JsonObject result = new JsonObject();
         EditingSession editingSession = modelService().currentSession().getSession();
         if (editingSession.canUndo())
         {
            Modification undoneChange = editingSession.undoLast();
            postprocessChange(undoneChange);
            ChangeJto jto = toJto(undoneChange);

            if (editingSession.canUndo())
            {
               Modification pendingUndo = editingSession.getPendingUndo();
               postprocessChange(pendingUndo);
               jto.pendingUndoableChange = toJto(pendingUndo);
               jto.pendingUndo = toChangeUri(pendingUndo);
            }
            if (editingSession.canRedo())
            {
               Modification pendingRedo = editingSession.getPendingRedo();
               postprocessChange(pendingRedo);
               jto.pendingRedoableChange = toJto(pendingRedo);
               jto.pendingRedo = toChangeUri(pendingRedo);
            }

            jto.isUndo = true;
            result = toJson(jto);

            commandHandlingMediator().broadcastChange(undoneChange.getSession(), result);

            return Response.ok(jsonIo().writeJsonObject(result), MediaType.APPLICATION_JSON_TYPE).build();
         }
         else
         {
            return Response.status(Status.CONFLICT) //
                  .entity("Nothing to be undone")
                  .build();
         }
      }
      else if ("redoLastUndo".equals(action))
      {
         JsonObject result = new JsonObject();
         EditingSession editingSession = modelService().currentSession().getSession();
         if (editingSession.canRedo())
         {
            Modification redoneChange = editingSession.redoNext();
            postprocessChange(redoneChange);
            ChangeJto jto = toJto(redoneChange);

            if (editingSession.canUndo())
            {
               Modification pendingUndo = editingSession.getPendingUndo();
               postprocessChange(pendingUndo);
               jto.pendingUndoableChange = toJto(pendingUndo);
               jto.pendingUndo = toChangeUri(pendingUndo);
            }
            if (editingSession.canRedo())
            {
               Modification pendingRedo = editingSession.getPendingRedo();
               postprocessChange(pendingRedo);
               jto.pendingRedoableChange = toJto(pendingRedo);
               jto.pendingRedo = toChangeUri(pendingRedo);
            }

            jto.isRedo = true;
            result = toJson(jto);

            commandHandlingMediator().broadcastChange(redoneChange.getSession(), result);

            return Response.ok(jsonIo().writeJsonObject(result), MediaType.APPLICATION_JSON_TYPE).build();
         }
         else
         {
            return Response.status(Status.CONFLICT) //
                  .entity("Nothing to be redone")
                  .build();
         }
      }
      else
      {
         return Response.status(Status.BAD_REQUEST) //
               .entity("Invalid navigation action: " + action)
               .build();
      }
   }

   @POST
   @Path("/changes")
   public Response applyChange(String postedData)
   {
      try
      {
         CommandJto commandJto = jsonIo().gson().fromJson(postedData, CommandJto.class);
         Response outcome = applyChange(commandJto);

         return outcome;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return Response.serverError().entity(e).build();
      }
   }

   private Response applyChange(CommandJto commandJto)
   {
      String commandId = commandJto.commandId;
      String modelId = commandJto.modelId;

      if (isEmpty(modelId))
      {
         return applyGlobalChange(commandId, commandJto);
      }
      else
      {
         // change to be interpreted in context of a model
         EObject model = modelService().currentSession().modelRepository().findModel(commandJto.modelId);
         if (null == model)
         {
            return Response.status(Status.BAD_REQUEST) //
                  .entity("Unknown model: " + modelId)
                  .build();
         }

         return applyModelElementChange(commandId, model, commandJto);
      }
   }

   /**
    * @param commandId
    * @param commandJson
    * @return
    */
   private Response applyGlobalChange(String commandId, CommandJto commandJto)
   {
      // TODO global command (e.g. "model.create")
      if ("model.create".equalsIgnoreCase(commandId)
            || "model.delete".equalsIgnoreCase(commandId)
            || "model.update".equalsIgnoreCase(commandId))
      {
         List<ChangeDescriptionJto> changesJson = commandJto.changeDescriptions;
         for (ChangeDescriptionJto changeDescrJto : changesJson) {
            if (null != changeDescrJto) {
               EObject targetElement = null;
               if (null != changeDescrJto.uuid) {
                  String uuid = changeDescrJto.uuid;
                  targetElement = modelService().uuidMapper().getEObject(uuid);
               }

               JsonElement changeJson = changeDescrJto.changes;
               ModelChangeCommandHandler handler = resolveSpringBean(ModelChangeCommandHandler.class, servletContext);
               // TODO make this a regular modification
               JsonObject response = handler.handleCommand(commandId, targetElement, changeJson.getAsJsonObject());
               if (null != response) {
                  return Response.ok(response.toString(), APPLICATION_JSON_TYPE).build();
               }
            }
         }
      }

      return null;
   }


   private Response applyModelElementChange(String commandId, EObject model, CommandJto commandJto)
   {
      List<CommandHandlingMediator.ChangeRequest> changeDescriptors = newArrayList();

      // pre-process change descriptions
      ModelBinding<EObject> modelBinding = modelService().currentSession().modelRepository().getModelBinding(model);
      try
      {
         for (ChangeDescriptionJto changeDescrJto : commandJto.changeDescriptions)
         {
            EObject targetElement = findTargetElement(model, changeDescrJto);

            changeDescriptors.add(new CommandHandlingMediator.ChangeRequest(model,
                  targetElement, changeDescrJto.changes));
         }
      }
      catch (WebApplicationException wae)
      {
         return wae.getResponse();
      }

      // dispatch to actual command handler
      EditingSession editingSession = modelService().currentSession().getSession(model);
      Modification change = commandHandlingMediator().handleCommand(editingSession,
            commandId, changeDescriptors);
      if (null != change)
      {
         postprocessChange(change);

         change.getMetadata().put("commandId", commandId);
         change.getMetadata().put("modelId", modelBinding.getModelId(model));
         if (null != commandJto.account)
         {
            change.getMetadata().put("account", commandJto.account);
         }

         // Notify unsaved models tracker of the change to the model.
         UnsavedModelsTracker.getInstance().notifyModelModfied(modelBinding.getModelId(model));

         JsonObject changeJto = toJson(toJto(change));

         commandHandlingMediator().broadcastChange(change.getSession(), changeJto);

         return Response.created(URI.create(toChangeUri(change))) //
               .entity(jsonIo().writeJsonObject(changeJto))
               .type(MediaType.APPLICATION_JSON_TYPE)
               .build();
      }
      else
      {
         return Response.status(Status.BAD_REQUEST) //
               .entity("Unsupported change request: " + commandId //
                     + " [" + commandJto.changeDescriptions + "]")
               .build();
      }
   }

   private EObject findTargetElement(EObject model, ChangeDescriptionJto changeDescrJto)
         throws WebApplicationException
   {
      if (model instanceof ModelType)
      {
         return findTargetElement((ModelType) model, changeDescrJto);
      }
      else
      {
         ModelBinding<EObject> modelBinding = modelService().currentSession().modelRepository().getModelBinding(model);
         ModelNavigator<EObject> modelNavigator = modelBinding.getNavigator();
         if ( !isEmpty(changeDescrJto.uuid))
         {
            return modelNavigator.findElementByUuid(model, changeDescrJto.uuid);
         }
         else if ( !isEmpty(changeDescrJto.oid))
         {
            // HACK sometimes the modelId is passed in the oid field
            if (modelBinding.getModelId(model).equals(changeDescrJto.oid))
            {
               return model;
            }

            return modelNavigator.findElementByOid(model, Long.valueOf(changeDescrJto.oid));
         }
         else
         {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST) //
                  .entity("Missing context element identifier: " + changeDescrJto)
                  .build());
         }
      }
   }

   private EObject findTargetElement(ModelType model, ChangeDescriptionJto changeDescrJto)
         throws WebApplicationException
   {
      EObject targetElement = null;
      // existing target, identified by uuid
      if (null != changeDescrJto.uuid)
      {
         String uuid = changeDescrJto.uuid;
         targetElement = modelService().uuidMapper().getEObject(uuid);

         if (null == targetElement)
         {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST) //
                  .entity("Unknown target element for element UUID " + uuid)
                  .build());
         }
      }
      else if (null != changeDescrJto.oid)
      {
         // existing target, identified by oid
         String oid = changeDescrJto.oid;
         if (model.getId().equals(oid))
         {
            targetElement = model;
         }
         else
         {
            long parsedOid = Long.parseLong(oid);
            // deep search for model element by OID
            // TODO can lookup faster as oid is declared the XML index
            // field?
            for (Iterator<? > i = model.eAllContents(); i.hasNext();)
            {
               Object element = i.next();
               if ((element instanceof IModelElement)
                     && ((((IModelElement) element).getElementOid() == parsedOid)))
               {
                  targetElement = (IModelElement) element;
                  break;
               }
            }
         }

         if (null == targetElement)
         {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST) //
                  .entity(
                        "Unknown target element for element OID " + oid
                              + " within model " + model.getId())
                  .build());
         }
      }
      else
      {
         throw new WebApplicationException(Response.status(Status.BAD_REQUEST) //
               .entity("Missing target element identifier: " + changeDescrJto)
               .build());
      }

      return targetElement;
   }

   private void postprocessChange(Modification change)
   {
      ChangesetPostprocessingService postprocessingService = resolveSpringBean(
            ChangesetPostprocessingService.class, servletContext);

      postprocessingService.postprocessChangeset(change);
   }

   private ModelService modelService()
   {
      return resolveSpringBean(ModelService.class, servletContext);
   }

   private JsonMarshaller jsonIo()
   {
      return resolveSpringBean(JsonMarshaller.class, servletContext);
   }

   private CommandHandlingMediator commandHandlingMediator()
   {
      try
      {
         CommandHandlingMediator twophaseMediator = resolveSpringBean(CommandHandlingMediator.class, servletContext);
         if (null != twophaseMediator)
         {
            trace.info("Using two-phase command handling.");
            return twophaseMediator;
         }
      }
      catch (BeansException be)
      {
         // failed resolving twophase mediator, fall back to simple mediator
      }

      final SimpleCommandHandlingMediator mediator = resolveSpringBean(SimpleCommandHandlingMediator.class, servletContext);
      return new CommandHandlingMediator()
      {
         @Override
         public void broadcastChange(EditingSession session, JsonObject commndJson)
         {
            mediator.broadcastChange(session, commndJson);
         }

         @Override
         public Modification handleCommand(EditingSession editingSession,
               String commandId, List<ChangeRequest> changes)
         {
            return mediator.handleCommand(editingSession, commandId, changes);
         }
      };
   }

   public static class ChangeJto
   {
      public String id;
      public long timestamp;
      public String commandId;
      public String modelId;
      public String account;

      public ChangesJto changes = new ChangesJto();

      public String pendingUndo;
      public String pendingRedo;

      //TODO pendingUndoableChange / pendingRedoableChange is a temporary addition
      //will be replaced with something concrete once requirement is clear
      public ChangeJto pendingUndoableChange;
      public ChangeJto pendingRedoableChange;

      public Boolean isUndo;
      public Boolean isRedo;

      static class ChangesJto
      {
         public JsonArray modified = new JsonArray();
         public JsonArray added = new JsonArray();
         public JsonArray removed = new JsonArray();
      };
   };

   private static class CommandJto
   {
      public String commandId;
      public String modelId;

      public String account;

      public List<ChangeDescriptionJto> changeDescriptions;
   }

   private static class ChangeDescriptionJto
   {
      public String uuid;
      public String oid;

      public JsonObject changes;
   }
}
