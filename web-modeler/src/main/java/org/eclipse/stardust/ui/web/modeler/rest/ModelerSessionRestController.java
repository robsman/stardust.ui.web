package org.eclipse.stardust.ui.web.modeler.rest;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
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
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.common.UnsavedModelsTracker;
import org.eclipse.stardust.ui.web.modeler.edit.CommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.edit.EditingSessionManager;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

      JsonObject jsChanges = new JsonObject();
      result.add("changes", jsChanges);
      JsonArray jsModified = new JsonArray();
      for (EObject changedObject : change.changedObjects())
      {
         jsModified.add(ModelElementMarshaller.toJson((IModelElement) changedObject));
      }
      jsChanges.add("modified", jsModified);

      JsonArray jsAdded = new JsonArray();
      for (EObject addedObject : change.addedObjects())
      {
         jsAdded.add(ModelElementMarshaller.toJson((IModelElement) addedObject));
      }
      jsChanges.add("added", jsAdded);

      JsonArray jsRemoved = new JsonArray();
      for (EObject removedObject : change.removedObjects())
      {
         jsRemoved.add(ModelElementMarshaller.toJson((IModelElement) removedObject));
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
      EditingSession editingSession = editingSessionManager().getSession();
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
         EditingSession editingSession = editingSessionManager().getSession();
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
         EditingSession editingSession = editingSessionManager().getSession();
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
         return Response.serverError().entity(e).build();
      }
   }

   private Response applyChange(JsonObject commandJson)
   {
	   System.out.println("applyChange: " + commandJson);
      String commandId = extractString(commandJson, "commandId");
      String modelId = extractString(commandJson, "context", "modelId");

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

   private Response applyGlobalChange(String commandId, JsonObject commandJson)
   {
      // TODO global command (e.g. "model.create")

      return null;
   }


   private Response applyModelElementChange(String commandId, ModelType model, JsonObject commandJson)
   {
      List<Pair<EObject, JsonObject>> changeDescriptors = newArrayList();
      JsonArray targetElementsJson = commandJson.getAsJsonArray("changeDescriptions");
      for (JsonElement targetElementJson : targetElementsJson)
      {
         EObject targetElement = null;

         if (targetElementJson.isJsonObject() && targetElementJson.getAsJsonObject().has("oid"))
         {
            // existing target, identified by oid
            long oid = extractLong(targetElementJson.getAsJsonObject(), "oid");

            // deep search for model element by OID
            // TODO can lookup faster as oid is declared the XML index field?
            for (Iterator<? > i = model.eAllContents(); i.hasNext();)
            {
               Object element = i.next();
               if ((element instanceof IModelElement)
                     && ((((IModelElement) element).getElementOid() == oid)))
               {
                  targetElement = (IModelElement) element;
                  break;
               }
            }

            if (null != targetElement)
            {
               JsonElement jsTargetElementChanges = targetElementJson.getAsJsonObject().get("changes");

               changeDescriptors.add(new Pair<EObject, JsonObject>(targetElement,
                     jsTargetElementChanges.getAsJsonObject()));
            }
            else
            {
               return Response.status(Status.BAD_REQUEST) //
                     .entity(
                           "Unknown target element for element OID " + oid
                                 + " within model " + model.getId())
                     .build();
            }
         }
         else
         {
            return Response.status(Status.BAD_REQUEST) //
                  .entity("Missing target element identifier: " + targetElementJson)
                  .build();
         }
      }

      // dispatch to actual command handler
      Modification change = commandHandlerRegistry().handleCommand(model, commandId, changeDescriptors);
      if (null != change)
      {
         //Notify unsaved models tracker of the change to the model.
         UnsavedModelsTracker.getInstance().notifyModelModfied(model.getId());
         return Response.created(toChangeUri(change)) //
               .entity(jsonIo().writeJsonObject(toJson(change)))
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

   private EditingSessionManager editingSessionManager()
   {
      return resolveSpringBean(EditingSessionManager.class, servletContext);
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
