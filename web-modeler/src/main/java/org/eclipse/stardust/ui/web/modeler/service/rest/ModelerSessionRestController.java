package org.eclipse.stardust.ui.web.modeler.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.service.rest.RestControllerUtils.resolveSpringBean;
import static org.eclipse.stardust.ui.web.modeler.service.rest.RestControllerUtils.resolveSpringBeans;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
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

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.common.ModelRepository;
import org.eclipse.stardust.ui.web.modeler.common.UnsavedModelsTracker;
import org.eclipse.stardust.ui.web.modeler.edit.SimpleCommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.edit.postprocessing.ChangesetPostprocessingService;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ModelCommandsHandler;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.eclipse.stardust.ui.web.modeler.spi.ModelNavigator;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;

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
   private ModelService modelService;

   private static CommandJto CommandJto;

   public static CommandJto getCommandJto()
   {
      return CommandJto;
   }

   public ModelerSessionRestController()
   {}

   public ModelerSessionRestController(UriInfo uriInfo)
   {
      this.uriInfo = uriInfo;
   }

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
         EObject model = modelService.currentSession()
               .modelRepository()
               .findModel(jto.modelId);
         marshaller = modelService.currentSession()
               .modelRepository()
               .getModelBinding(model)
               .getMarshaller();
      }
      else
      {
         marshaller = modelService.modelElementMarshaller();
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

      if (change.wasFailure())
      {
         ChangeJto.ProblemJto failureJto = new ChangeJto.ProblemJto();
         failureJto.severity = "error";
         failureJto.message = change.getFailure().getMessage();

         jto.problems = newArrayList();
         jto.problems.add(failureJto);
      }

      return jto;
   }

   public ChangeJto toJto(CommandJto command, ModelCommandsHandler.ModificationDescriptor changes)
   {
      ChangeJto jto = new ChangeJto();

      jto.id= changes.getId();
      jto.timestamp = System.currentTimeMillis();

      jto.commandId = command.commandId;
      jto.modelId= command.modelId;
      jto.account = command.account;

      jto.changes.modified.addAll(changes.modified);
      jto.changes.added.addAll(changes.added);
      jto.changes.removed.addAll(changes.removed);

      if (changes.wasFailure())
      {
         ChangeJto.ProblemJto failureJto = new ChangeJto.ProblemJto();
         failureJto.severity = "error";
         failureJto.message = changes.getFailure().getMessage();

         jto.problems = newArrayList();
         jto.problems.add(failureJto);
      }

      return jto;
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

   public enum ModelFormat
   {
      Native,
      Xpdl,
   }

   public StreamingOutput getCurrentModelState(String modelId, final ModelFormat modelFormat)
   {
      ModelRepository modelRepository = modelService.currentSession().modelRepository();

      final EObject model = modelRepository.findModel(modelId);
      if (null != model)
      {
         final ModelPersistenceHandler<EObject> persistenceHandler = modelRepository.getModelBinding(
               model)
               .getPersistenceHandler(model);
         if (null != persistenceHandler)
         {
            return new StreamingOutput()
            {
               @Override
               public void write(OutputStream output) throws IOException, WebApplicationException
               {
                  switch (modelFormat)
                  {
                  case Native:
                     persistenceHandler.saveModel(model, output);
                     break;

                  case Xpdl:
                     persistenceHandler.saveDeployableModel(model, output);
                     break;

                  default:
                     throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
                  }
                  output.flush();
               }
            };
         }
         else
         {
            // no suitable persistence handler
            throw new WebApplicationException(Status.BAD_REQUEST);
         }
      }
      else
      {
         // invalid model ID
         throw new WebApplicationException(Status.NOT_FOUND);
      }
   }

   @GET
   @Path("/changes/mostCurrent")
   @Produces(MediaType.APPLICATION_JSON)
   public String showCurrentChange()
   {
      JsonObject result = new JsonObject();
      EditingSession editingSession = modelService.currentSession().getSession();
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

      return jsonIo.writeJsonObject(result);
   }

   @POST
   @Path("/changes/mostCurrent/navigation")
   public Response adjustMostCurrentChange(String action)
   {
      if ("undoMostCurrent".equals(action))
      {
         JsonObject result = new JsonObject();
         EditingSession editingSession = modelService.currentSession().getSession();
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

            return Response.ok(jsonIo.writeJsonObject(result), MediaType.APPLICATION_JSON_TYPE).build();
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
         EditingSession editingSession = modelService.currentSession().getSession();
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

            return Response.ok(jsonIo.writeJsonObject(result), MediaType.APPLICATION_JSON_TYPE).build();
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
      System.out.println("postedData ==============> " + postedData);
      try
      {
         CommandJto commandJto = jsonIo.gson().fromJson(postedData, CommandJto.class);
         Response outcome = applyChange(commandJto);

         return outcome;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return Response.serverError().entity(e).build();
      }
   }

   public Response applyChange(CommandJto commandJto)
   {
      String commandId = commandJto.commandId;
      String modelId = commandJto.modelId;

      ModelRepository modelRepository = modelService.currentSession().modelRepository();
      EObject model = modelRepository.findModel(modelId);

      if (commandId.startsWith("model."))
      {
         return applyGlobalChange(commandId, model, commandJto);
      }
      else
      {
         // change to be interpreted in context of a model
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
   private Response applyGlobalChange(String commandId, EObject model, CommandJto commandJto)
   {
      List<ChangeDescriptionJto> changesJson = commandJto.changeDescriptions;

      for (ChangeDescriptionJto changeDescrJto : changesJson) {
         if (null != changeDescrJto) {
            EObject targetElement = null;
            if (null != changeDescrJto.uuid)
            {
               String uuid = changeDescrJto.uuid;
               targetElement = modelService.uuidMapper().getEObject(uuid);
            }
            else
            {
               targetElement = model;
            }

            JsonObject changeJson = changeDescrJto.changes;
            String modelFormat;
            if (null != model)
            {
               modelFormat = modelService.currentSession().modelRepository()
                     .getModelFormat(model);
            }
            else
            {
               modelFormat = extractString(changeJson, "modelFormat");
            }

            ModelCommandsHandler.ModificationDescriptor changes = null;
            for (ModelCommandsHandler handler : resolveSpringBeans(
                  ModelCommandsHandler.class, springContext))
            {
               // TODO make this a regular modification
               if (handler.handlesModel(modelFormat))
               {
                  changes = handler.handleCommand(commandId, targetElement, changeJson);
                  break;
               }
            }
            if (null != changes)
            {
               JsonObject changeJto = toJson(toJto(commandJto, changes));
               return Response.ok(jsonIo.writeJsonObject(changeJto))
                     .type(APPLICATION_JSON_TYPE).build();
            }
            else
            {
               return Response.status(Status.BAD_REQUEST) //
                     .entity("Unsupported modelFormat: " + modelFormat).build();
            }
         }
      }

      return null;
   }


   private Response applyModelElementChange(String commandId, EObject model, CommandJto commandJto)
   {
      List<CommandHandlingMediator.ChangeRequest> changeDescriptors = newArrayList();

      // pre-process change descriptions
      ModelBinding<EObject> modelBinding = modelService.currentSession().modelRepository().getModelBinding(model);
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

      ModelerSessionRestController.CommandJto = commandJto;

      // dispatch to actual command handler
      EditingSession editingSession = modelService.currentSession().getSession(model);
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

         ModelerSessionRestController.CommandJto = null;

         return Response.created(URI.create(toChangeUri(change))) //
               .entity(jsonIo.writeJsonObject(changeJto))
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
         ModelBinding<EObject> modelBinding = modelService.currentSession().modelRepository().getModelBinding(model);
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
         targetElement = modelService.uuidMapper().getEObject(uuid);

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
            ChangesetPostprocessingService.class, springContext);

      postprocessingService.postprocessChangeset(change);
   }

   private CommandHandlingMediator commandHandlingMediator()
   {
      try
      {
         CommandHandlingMediator twophaseMediator = resolveSpringBean(CommandHandlingMediator.class, springContext);
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

      final SimpleCommandHandlingMediator mediator = resolveSpringBean(SimpleCommandHandlingMediator.class, springContext);
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
            try
            {
               return mediator.handleCommand(editingSession, commandId, changes);
            }
            catch (Exception e)
            {
               trace.warn("Failed handling command '" + commandId + "'", e);

               return new Modification(editingSession, e);
            }
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

      public List<ProblemJto> problems = null;

      public String pendingUndo;
      public String pendingRedo;

      //TODO pendingUndoableChange / pendingRedoableChange is a temporary addition
      //will be replaced with something concrete once requirement is clear
      public ChangeJto pendingUndoableChange;
      public ChangeJto pendingRedoableChange;

      public Boolean isUndo;
      public Boolean isRedo;

      public static class ChangesJto
      {
         public JsonArray modified = new JsonArray();
         public JsonArray added = new JsonArray();
         public JsonArray removed = new JsonArray();
      };

      public static class ProblemJto
      {
         public String severity;
         public String message;
      }
   };

   public static class CommandJto
   {
      public String commandId;
      public String modelId;

      public String account;

      public List<ChangeDescriptionJto> changeDescriptions;
   }

   public static class ChangeDescriptionJto
   {
      public String uuid;
      public String oid;

      public JsonObject changes;
   }
}