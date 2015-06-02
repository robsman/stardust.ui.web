package org.eclipse.stardust.ui.web.modeler.edit.batch;

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.ui.web.modeler.common.BadRequestException;
import org.eclipse.stardust.ui.web.modeler.common.exception.ModelerException;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.edit.jto.ChangeDescriptionJto;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController.ChangeJto;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;

@CommandHandler
public class CommandBatchHandler
{
   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelerSessionController sessionController;

   private final JsonPathEvaluator jsonPathEvaluator = new JsonPathEvaluator();

   @OnCommand(commandId = "batchEdit.run")
   public void onBatchEdit(EObject model, JsonObject changes)
   {
      BatchChangesJto batchChangesJto = jsonIo.gson().fromJson(changes, BatchChangesJto.class);

      ModelingSession currentSession = sessionController.currentSession();

      Map<String, JsonElement> variables = newHashMap();
      for (Map.Entry<String, JsonElement> variableBinding : batchChangesJto.variableBindings
            .entrySet())
      {
         variables.put(variableBinding.getKey(), variableBinding.getValue());
      }

      for (BatchStepJto batchStep : batchChangesJto.steps)
      {
         CommandJto subCommand = new CommandJto();
//         subCommand.account = commandJto.account;
         subCommand.commandId = batchStep.commandId;
         subCommand.modelId = currentSession.modelRepository().getModelId(model);
         subCommand.changeDescriptions = newArrayList();
         subCommand.changeDescriptions.add(injectVariables(batchStep, variables));

         List<CommandHandlingMediator.ChangeRequest> changeDescriptors = newArrayList();

         // pre-process change descriptions
         ModelBinding<EObject> modelBinding = currentSession.modelRepository().getModelBinding(model);
         for (ChangeDescriptionJto changeDescrJto : subCommand.changeDescriptions)
         {
            EObject targetElement = ModelerSessionController.findTargetElement(currentSession, model, changeDescrJto);

            changeDescriptors.add(new CommandHandlingMediator.ChangeRequest(model,
                  targetElement, changeDescrJto.changes));
         }

         // TODO this needs to be a session for the subCommand's actual target model
         EditingSession editingSession = currentSession.getEditSession(model);

         // start a local editing session to determine the change set from only the batch step
         EditingSession localSession = new EditingSession(editingSession.getId() + "-batchStep");
         for (EObject trackedModel : editingSession.getTrackedModels())
         {
            localSession.trackModel(trackedModel);
         }

         // dispatch to actual command handler
         Modification change = sessionController.commandHandlingMediator().handleCommand(localSession,
               subCommand.commandId, changeDescriptors);
         if (null != change)
         {
            if (change.getFailure() instanceof ModelerException)
            {
               throw (ModelerException) change.getFailure();
            }

            change.getMetadata().put("commandId", subCommand.commandId);
            change.getMetadata().put("modelId", modelBinding.getModelId(model));

            localSession.beginEdit();
            sessionController.postprocessChange(change);
            localSession.endEdit(false);

            if (null != subCommand.account)
            {
               change.getMetadata().put("account", subCommand.account);
            }

            ChangeJto jto = sessionController.toJto(change);
            JsonObject changeJson = sessionController.toJson(jto);

            if (!isEmpty(batchStep.variables))
            {
               // evaluate given expressions against current step's output
               variables.putAll(extractVariables(batchStep.variables, changeJson));
            }
         }
         else
         {
            throw new BadRequestException("Unsupported change request: " + subCommand.commandId //
                  + " [" + subCommand.changeDescriptions + "]");
         }
      }
   }

   private ChangeDescriptionJto injectVariables(BatchStepJto batchStep,
         Map<String, JsonElement> variables)
   {
      // expand variable references
      String serializedChangeJto = jsonIo.gson().toJson(batchStep);
      for (Map.Entry<String, JsonElement> varEntry : variables.entrySet())
      {
         // replace incl. surrounding quotes (enabling conversion from string typed
         // var reference to number, e.g.)
         String varReference = "\"${" + varEntry.getKey() + "}\"";
         while (serializedChangeJto.contains(varReference))
         {
            serializedChangeJto = serializedChangeJto.replace(varReference, varEntry.getValue().toString());
         }

         // afterwards, also replace embedded references (naturally targeting string typed attributes)
         String embeddedVarReference = "${" + varEntry.getKey() + "}";
         while (serializedChangeJto.contains(embeddedVarReference))
         {
            serializedChangeJto = serializedChangeJto.replace(embeddedVarReference,
                  varEntry.getValue().getAsString());
         }
      }
      return jsonIo.gson().fromJson(serializedChangeJto, ChangeDescriptionJto.class);
   }

   private Map<String, JsonElement> extractVariables(List<VarSpecJto> varSpecs, JsonObject changeJson)
   {
      Map<String, JsonElement> variables = newHashMap();

      for (VarSpecJto varSpec : varSpecs)
      {
         JsonElement variableValue = jsonPathEvaluator.resolveExpression(
               changeJson.get("changes"), varSpec.expression);
         if (null != variableValue)
         {
            variables.put(varSpec.name, variableValue);
         }
         else
         {
            // TODO handle more thoroughly
            throw new PublicException("Unresolved variable: " + varSpec);
         }
      }

      return variables;
   }
}
