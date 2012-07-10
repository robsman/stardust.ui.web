package org.eclipse.stardust.ui.web.modeler.rest;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.common.UnsavedModelsTracker;
import org.eclipse.stardust.ui.web.modeler.edit.CommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.edit.EditingSessionManager;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Path("/modeler/{randomPostFix}/sessions/{modelId}/{processId}")
public class ModelerSessionRestController
{
   @Context
   private ServletContext servletContext;

   @Context
   private UriInfo uriInfo;

   @PathParam("modelId")
   private String modelId;

   @PathParam("processId")
   private String processId;

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
   public String showCurrentChange()
   {
      ModelType model = modelService().findModel(modelId);
      ProcessDefinitionType processDefinition = modelService().findProcessDefinition(model, processId);

      JsonObject result = new JsonObject();
      EditingSession editingSession = editingSessionManager().getSession(processDefinition);
      if (editingSession.canUndo())
      {
         Modification pendingUndo = editingSession.getPendingUndo();
         result = toJson(pendingUndo);

         if (editingSession.canRedo())
         {
            Modification pendingRedo = editingSession.getPendingRedo();
            result.addProperty("nextChange", uriInfo.getAbsolutePath().toString() + "/changes/" + pendingRedo.getId());
         }
      }

      return jsonIo().writeJsonObject(result);
   }

   @POST
   @Path("/changes")
   public Response applyChange(String postedData)
   {
      try
      {
         JsonObject json = jsonIo().readJsonObject(postedData);
         Response outcome = applyChange(modelId, processId, json);

         return outcome;
      }
      catch (Exception e)
      {
         return Response.serverError().entity(e).build();
      }
   }

   private Response applyChange(String modelId, String processId, JsonObject commandJson)
   {
      String commandId = extractString(commandJson, "commandId");

      ModelType model = modelService().findModel(modelId);
      ProcessDefinitionType processDefinition = modelService().findProcessDefinition(model, processId);

      List<Pair<IModelElement, JsonObject>> changeDescriptors = newArrayList();
      JsonArray targetElementsJson = commandJson.getAsJsonArray("changeDescriptions");
      for (JsonElement targetElementJson : targetElementsJson)
      {
         IModelElement targetElement = null;

         if (targetElementJson.isJsonObject() && targetElementJson.getAsJsonObject().has("oid"))
         {
            long oid = extractLong(targetElementJson.getAsJsonObject(), "oid");

            // deep search for model element by OID
            for (Iterator<? > i = processDefinition.eAllContents(); i.hasNext();)
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

               changeDescriptors.add(new Pair<IModelElement, JsonObject>(targetElement,
                     jsTargetElementChanges.getAsJsonObject()));
            }
            else
            {
               return Response.status(Status.BAD_REQUEST) //
                     .entity("Unknown target element for element OID " + oid)
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
      Modification change = commandHandlerRegistry().handleCommand(processDefinition, commandId, changeDescriptors);
      if (null != change)
      {
         //Notify unsaved models tracker of the change to the model. 
         UnsavedModelsTracker.getInstance().notifyModelModfied(modelId);
         return Response.created(toChangeUri(change)) //
               .entity(jsonIo().writeJsonObject(toJson(change)))
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
