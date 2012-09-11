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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.common.UnsavedModelsTracker;
import org.eclipse.stardust.ui.web.modeler.edit.CommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.edit.model.element.ModelChangeCommandHandler;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@Path("/modeler/{randomPostFix}/sessions")
public class ModelerSessionRestController
{
   @Context
   private ServletContext servletContext;

   @Context
   private UriInfo uriInfo;

   public URI toChangeUri(Modification change)
   {
      return URI.create(uriInfo.getAbsolutePath().toString() + "/changes/" + change.getId());
   }

   public JsonObject toJson(Modification change)
   {
      JsonObject result = new JsonObject();

      result.addProperty("id", change.getId());
      result.addProperty("timestamp", System.currentTimeMillis());

      if (change.getMetadata().containsKey("commandId"))
      {
          result.addProperty("commandId", change.getMetadata().get("commandId"));
      }
      if (change.getMetadata().containsKey("modelId"))
      {
          result.addProperty("modelId", change.getMetadata().get("modelId"));
      }
      if (change.getMetadata().containsKey("account"))
      {
          result.addProperty("account", change.getMetadata().get("account"));
      }

      JsonObject jsChanges = new JsonObject();
      result.add("changes", jsChanges);
      JsonArray jsModified = new JsonArray();
      for (EObject changedObject : change.changedObjects())
      {
         jsModified.add(modelService().modelElementMarshaller().toJson(changedObject));
      }
      jsChanges.add("modified", jsModified);

      JsonArray jsAdded = new JsonArray();
      for (EObject addedObject : change.addedObjects())
      {
         jsAdded.add(modelService().modelElementMarshaller().toJson(addedObject));
      }
      jsChanges.add("added", jsAdded);

      JsonArray jsRemoved = new JsonArray();
      for (EObject removedObject : change.removedObjects())
      {
         jsRemoved.add(modelService().modelElementMarshaller().toJson(removedObject));
      }
      jsChanges.add("removed", jsRemoved);

      return result;
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
         result = toJson(pendingUndo);

         result.addProperty("pendingUndo", uriInfo.getAbsolutePath().toString() + "/changes/" + pendingUndo.getId());
         if (editingSession.canRedo())
         {
            Modification pendingRedo = editingSession.getPendingRedo();
            result.addProperty("pendingRedo", uriInfo.getAbsolutePath().toString() + "/changes/" + pendingRedo.getId());
         }
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

            result = toJson(undoneChange);

            if (editingSession.canUndo())
            {
               Modification pendingUndo = editingSession.getPendingUndo();
               result.addProperty("pendingUndo", uriInfo.getAbsolutePath().toString() + "/changes/" + pendingUndo.getId());
            }
            if (editingSession.canRedo())
            {
               Modification pendingRedo = editingSession.getPendingRedo();
               result.addProperty("pendingRedo", uriInfo.getAbsolutePath().toString() + "/changes/" + pendingRedo.getId());
            }

            commandHandlerRegistry().broadcastChange(undoneChange.getSession(), result);

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

            result = toJson(redoneChange);

            if (editingSession.canUndo())
            {
               Modification pendingUndo = editingSession.getPendingUndo();
               result.addProperty("pendingUndo", uriInfo.getAbsolutePath().toString() + "/changes/" + pendingUndo.getId());
            }
            if (editingSession.canRedo())
            {
               Modification pendingRedo = editingSession.getPendingRedo();
               result.addProperty("pendingRedo", uriInfo.getAbsolutePath().toString() + "/changes/" + pendingRedo.getId());
            }

            commandHandlerRegistry().broadcastChange(redoneChange.getSession(), result);

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
         ModelType model = modelService().findModel(modelId);
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
         for (ChangeDescriptionJto cJson : changesJson) {
            if (null != cJson) {
               EObject targetElement = null;
               if (null != cJson.uuid) {
                  String uuid = cJson.uuid;
                  targetElement = modelService().uuidMapper().getEObject(uuid);
               }

               JsonElement changeJson = cJson.changes;
               ModelChangeCommandHandler handler = resolveSpringBean(ModelChangeCommandHandler.class, servletContext);
               JsonObject response = handler.handleCommand(commandId, targetElement, changeJson.getAsJsonObject());
               if (null != response) {
                  return Response.ok(response.toString(), APPLICATION_JSON_TYPE).build();
               }
            }
         }
      }

      return null;
   }


   private Response applyModelElementChange(String commandId, ModelType model, CommandJto commandJto)
   {
      List<Pair<EObject, JsonObject>> changeDescriptors = newArrayList();
      List<ChangeDescriptionJto> targetElementsJson = commandJto.changeDescriptions;
      for (ChangeDescriptionJto changeDescrJto : targetElementsJson)
      {
         EObject targetElement = null;

         if ((null != changeDescrJto.oid) || (null != changeDescrJto.uuid))
         {
            // existing target, identified by uuid
            if (null != changeDescrJto.uuid)
            {
               String uuid = changeDescrJto.uuid;
               targetElement = modelService().uuidMapper().getEObject(uuid);

               if (null == targetElement)
               {
                  return Response.status(Status.BAD_REQUEST) //
                        .entity("Unknown target element for element UUID " + uuid)
                        .build();
               }
            }
            // existing target, identified by oid
            else if (null != changeDescrJto.oid)
            {
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
                  return Response.status(Status.BAD_REQUEST) //
                        .entity(
                              "Unknown target element for element OID " + oid
                                    + " within model " + model.getId())
                        .build();
               }
            }

            JsonElement jsTargetElementChanges = changeDescrJto.changes;

            changeDescriptors.add(new Pair<EObject, JsonObject>(targetElement,
                  jsTargetElementChanges.getAsJsonObject()));
         }
         else
         {
            return Response.status(Status.BAD_REQUEST) //
                  .entity("Missing target element identifier: " + changeDescrJto)
                  .build();
         }
      }

      // dispatch to actual command handler
      Modification change = commandHandlerRegistry().handleCommand(
            modelService().currentSession().getSession(model), commandId, changeDescriptors);
      if (null != change)
      {
         change.getMetadata().put("commandId", commandId);
         change.getMetadata().put("modelId", model.getId());
         if (null != commandJto.account)
         {
            change.getMetadata().put("account", commandJto.account);
         }

         // Notify unsaved models tracker of the change to the model.
         UnsavedModelsTracker.getInstance().notifyModelModfied(model.getId());

         JsonObject changeJto = toJson(change);

         commandHandlerRegistry().broadcastChange(change.getSession(), changeJto);

         return Response.created(toChangeUri(change)) //
               .entity(jsonIo().writeJsonObject(changeJto))
               .type(MediaType.APPLICATION_JSON_TYPE)
               .build();
      }
      else
      {
         return Response.status(Status.BAD_REQUEST) //
               .entity("Unsupported change request: " + commandId //
                     + " [" + targetElementsJson + "]")
               .build();
      }
   }

   private ModelService modelService()
   {
      return resolveSpringBean(ModelService.class, servletContext);
   }

   private JsonMarshaller jsonIo()
   {
      return resolveSpringBean(JsonMarshaller.class, servletContext);
   }

   private CommandHandlingMediator commandHandlerRegistry()
   {
      return resolveSpringBean(CommandHandlingMediator.class, servletContext);
   }

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
