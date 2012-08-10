package org.eclipse.stardust.ui.web.modeler.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractAsString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
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
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.common.UnsavedModelsTracker;
import org.eclipse.stardust.ui.web.modeler.edit.CommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSessionManager;
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
      result.addProperty("account", "sheldor"); // TODO Robert add!
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
         JsonObject json = jsonIo().readJsonObject(postedData);
         Response outcome = applyChange(json);

         return outcome;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return Response.serverError().entity(e).build();
      }
   }

   private Response applyChange(JsonObject commandJson)
   {
      String commandId = extractString(commandJson, "commandId");
      String modelId = extractString(commandJson, "modelId");

      if (isEmpty(modelId))
      {
         return applyGlobalChange(commandId, commandJson);
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

         return applyModelElementChange(commandId, model, commandJson);
      }
   }

   /**
    * @param commandId
    * @param commandJson
    * @return
    */
   private Response applyGlobalChange(String commandId, JsonObject commandJson)
   {
      // TODO global command (e.g. "model.create")
      if ("model.create".equalsIgnoreCase(commandId)
            || "model.delete".equalsIgnoreCase(commandId)
            || "model.update".equalsIgnoreCase(commandId))
      {
         JsonArray changesJson = commandJson.getAsJsonArray("changeDescriptions");
         for (JsonElement cJson : changesJson) {
            if (null != cJson) {
               EObject targetElement = null;
               if (null != cJson.getAsJsonObject().get(ModelerConstants.UUID_PROPERTY)) {
                  String uuid = cJson.getAsJsonObject().get(ModelerConstants.UUID_PROPERTY).getAsString();
                  targetElement = modelService().uuidMapper().getEObject(uuid);
               }

               JsonElement changeJson = cJson.getAsJsonObject().get("changes");
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


   private Response applyModelElementChange(String commandId, ModelType model, JsonObject commandJson)
   {
      List<Pair<EObject, JsonObject>> changeDescriptors = newArrayList();
      JsonArray targetElementsJson = commandJson.getAsJsonArray("changeDescriptions");
      for (JsonElement targetElementJson : targetElementsJson)
      {
         EObject targetElement = null;

         if (targetElementJson.isJsonObject()
               && (targetElementJson.getAsJsonObject().has("oid") || targetElementJson.getAsJsonObject()
                     .has("uuid")))
         {
            // existing target, identified by uuid
            if (targetElementJson.getAsJsonObject().has("uuid"))
            {
               String uuid = extractAsString(targetElementJson.getAsJsonObject(), "uuid");
               targetElement = modelService().uuidMapper().getEObject(uuid);

               if (null == targetElement)
               {
                  return Response.status(Status.BAD_REQUEST) //
                        .entity("Unknown target element for element UUID " + uuid)
                        .build();
               }
            }
            // existing target, identified by oid
            else if (targetElementJson.getAsJsonObject().has("oid"))
            {
               String oid = extractAsString(targetElementJson.getAsJsonObject(), "oid");
               if (model.getId().equals(oid))
               {
                  targetElement = model;
               }
               else
               {
                  // deep search for model element by OID
                  // TODO can lookup faster as oid is declared the XML index
                  // field?
                  for (Iterator<? > i = model.eAllContents(); i.hasNext();)
                  {
                     Object element = i.next();
                     if ((element instanceof IModelElement)
                           && ((((IModelElement) element).getElementOid() == Long.parseLong(oid))))
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

            JsonElement jsTargetElementChanges = targetElementJson.getAsJsonObject().get(
                  "changes");

            changeDescriptors.add(new Pair<EObject, JsonObject>(targetElement,
                  jsTargetElementChanges.getAsJsonObject()));
         }
         else
         {
            return Response.status(Status.BAD_REQUEST) //
                  .entity("Missing target element identifier: " + targetElementJson)
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
         if (commandJson.has("account"))
         {
            change.getMetadata().put("account", extractString(commandJson, "account"));
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

   private ModelingSessionManager editingSessionManager()
   {
      return resolveSpringBean(ModelingSessionManager.class, servletContext);
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
}
