package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newApplicationActivity;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newManualTrigger;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractBoolean;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.NameIdUtilsExtension;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.xpdl2.FormalParameterType;
import org.eclipse.stardust.model.xpdl.xpdl2.ModeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@CommandHandler
public class WizardsCommandHandler
{
   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelService modelService;

   @OnCommand(commandId = "serviceWrapperProcess.create")
   public void onCreateServiceWrapperProcess(ModelType model, JsonObject request)
   {
      createWrapperProcess(model, request);
   }

   @OnCommand(commandId = "processInterfaceTestWrapperProcess.create")
   public void onCreateProcessInterfaceTestWrapperProcess(ModelType model, JsonObject request)
   {
      createProcessInterfaceTestWrapperProcess(model, request);
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return modelService.getModelBuilderFacade();
   }

   public void createWrapperProcess(ModelType model, JsonObject json)
   {
      // Create process definition

      ProcessDefinitionType processDefinition = getModelBuilderFacade().createProcess(
            model, null, extractString(json, "processDefinitionName"), "Default",
            "Default");
      modelService.uuidMapper().map(processDefinition);

      // TODO Correct flags

      if (extractBoolean(json, "createWebService"))
      {
         if (extractBoolean(json, "createRestService"))
         {
            ModelBuilderFacade.setAttribute(processDefinition,
                  "carnot:engine:externalInvocationType",
                  PredefinedConstants.PROCESSINTERFACE_INVOCATION_BOTH);
         }
         else
         {
            ModelBuilderFacade.setAttribute(processDefinition,
                  "carnot:engine:externalInvocationType",
                  PredefinedConstants.PROCESSINTERFACE_INVOCATION_SOAP);
         }
      }
      else
      {
         if (extractBoolean(json, "createRestService"))
         {
            ModelBuilderFacade.setAttribute(processDefinition,
                  "carnot:engine:externalInvocationType",
                  PredefinedConstants.PROCESSINTERFACE_INVOCATION_REST);
         }
      }

      if (extractBoolean(json, "transientProcess"))
      {
         ModelBuilderFacade.setAttribute(processDefinition,
               "carnot:engine:auditTrailPersistence", "TRANSIENT"); // Values are
                                                                    // TRANSIENT|DEFERRED|IMMEDIATE
      }

      LaneSymbol parentLaneSymbol = getModelBuilderFacade().findLaneInProcess(
            processDefinition, ModelerConstants.DEF_LANE_ID);

      int yOffset = 50;
      int xDataOffset = 10;
      int xActivityOffset = 150;
      int xEventOffset = 200;

      // Create Start Event

      StartEventSymbol startEventSymbol = AbstractElementBuilder.F_CWM.createStartEventSymbol();

      startEventSymbol.setXPos(xEventOffset);
      startEventSymbol.setYPos(yOffset);

      yOffset += 100;

      processDefinition.getDiagram().get(0).getStartEventSymbols().add(startEventSymbol);
      parentLaneSymbol.getStartEventSymbols().add(startEventSymbol);

      // Request data

      String dataId = NameIdUtilsExtension.createIdFromName(extractString(json, "requestDataName"));

      // TODO Weird programming because Model Builder Facade throws
      // ObjectNotFoundException

      DataType data = null;

      try
      {
         data = getModelBuilderFacade().findData(model, dataId);
      }
      catch (Exception x)
      {
      }
      finally
      {
         if (data == null)
         {
            data = getModelBuilderFacade().createStructuredData(model, dataId,
                  extractString(json, "requestDataName"),
                  extractString(json, "requestDataTypeFullId"));
         }
      }

      getModelBuilderFacade().createStructuredParameter(processDefinition, data,
            NameIdUtilsExtension.createIdFromName(extractString(json, "requestDataName")),
            extractString(json, "requestDataName"),
            extractString(json, "requestDataTypeFullId"), ModeType.IN);

      DataSymbolType dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();
      dataSymbol.setData(data);
      processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
      data.getDataSymbols().add(dataSymbol);
      dataSymbol.setXPos(xDataOffset);
      dataSymbol.setYPos(yOffset);

      parentLaneSymbol.getDataSymbol().add(dataSymbol);

      yOffset += 100;

      ActivityType activity;
      ActivitySymbolType activitySymbol;
      ActivitySymbolType previousActivitySymbol = null;

      if (extractString(json, "preprocessingApplicationFullId") != null)
      {
         activity = newApplicationActivity(processDefinition).withIdAndName(null,
               "Preprocessing App").build();

         activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);

         getModelBuilderFacade().setApplication(activity,
               extractString(json, "preprocessingApplicationFullId"));

         activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

         activitySymbol.setXPos(xActivityOffset);
         activitySymbol.setYPos(yOffset);
         activitySymbol.setWidth(180);
         activitySymbol.setHeight(50);
         activitySymbol.setActivity(activity);
         activity.getActivitySymbols().add(activitySymbol);

         processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
         parentLaneSymbol.getActivitySymbol().add(activitySymbol);

         AccessPointType inAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
               activity.getApplication(), DirectionType.IN_LITERAL);
         AccessPointType outAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
               activity.getApplication(), DirectionType.OUT_LITERAL);

         getModelBuilderFacade().createDataFlowConnection(processDefinition,
               activitySymbol, dataSymbol, PredefinedConstants.APPLICATION_CONTEXT,
               inAccessPoint != null ? inAccessPoint.getId() : null, PredefinedConstants.APPLICATION_CONTEXT,
               outAccessPoint != null ? outAccessPoint.getId() : null, "left", "right");

         previousActivitySymbol = activitySymbol;
         yOffset += 100;
      }

      // Create Application Activity

      activity = newApplicationActivity(processDefinition).withIdAndName(null,
            extractString(json, "serviceInvocationActivityName")).build();

      activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);
      getModelBuilderFacade().setApplication(activity,
            extractString(json, "applicationFullId"));

      activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

      activitySymbol.setXPos(xActivityOffset);
      activitySymbol.setYPos(yOffset);
      activitySymbol.setWidth(180);
      activitySymbol.setHeight(50);
      activitySymbol.setActivity(activity);
      activity.getActivitySymbols().add(activitySymbol);

      processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
      parentLaneSymbol.getActivitySymbol().add(activitySymbol);

      // Create connection from Start Event

      // TODO Host activity for start event?

      if (previousActivitySymbol != null)
      {
         getModelBuilderFacade().createTransitionSymbol(processDefinition,
               startEventSymbol, previousActivitySymbol, null, "bottom", "top");

         getModelBuilderFacade().createControlFlowConnection(processDefinition,
               previousActivitySymbol, activitySymbol, "Transition1", "", "", false,
               "true", "bottom", "top");
      }
      else
      {
         getModelBuilderFacade().createTransitionSymbol(processDefinition,
               startEventSymbol, activitySymbol, null, "bottom", "top");
      }

      AccessPointType inAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
            activity.getApplication(), DirectionType.IN_LITERAL);

      getModelBuilderFacade().createDataFlowConnection(processDefinition, activitySymbol,
            dataSymbol, PredefinedConstants.APPLICATION_CONTEXT,
            inAccessPoint != null ? inAccessPoint.getId() : null, null, null, "left",
            "right");

      previousActivitySymbol = activitySymbol;
      yOffset += 100;

      // Create Response Data

      dataId = NameIdUtilsExtension.createIdFromName(extractString(json, "responseDataName"));

      // TODO Weird programming because Model Builder Facade throws
      // ObjectNotFoundException

      data = null;

      try
      {
         data = getModelBuilderFacade().findData(model, dataId);
      }
      catch (Exception x)
      {
      }
      finally
      {
         if (data == null)
         {
            data = getModelBuilderFacade().createStructuredData(model, dataId,
                  extractString(json, "responseDataName"),
                  extractString(json, "responseDataTypeFullId"));
         }
      }

      getModelBuilderFacade().createStructuredParameter(processDefinition, data,
            NameIdUtilsExtension.createIdFromName(extractString(json, "responseDataName")),
            extractString(json, "responseDataName"),
            extractString(json, "responseDataTypeFullId"), ModeType.OUT);

      dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();

      dataSymbol.setData(data);
      processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
      data.getDataSymbols().add(dataSymbol);
      dataSymbol.setXPos(xDataOffset);
      dataSymbol.setYPos(yOffset);

      parentLaneSymbol.getDataSymbol().add(dataSymbol);

      AccessPointType outAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
            activity.getApplication(), DirectionType.OUT_LITERAL);

      getModelBuilderFacade().createDataFlowConnection(processDefinition, activitySymbol,
            dataSymbol, null, null, PredefinedConstants.APPLICATION_CONTEXT,
            outAccessPoint != null ? outAccessPoint.getId() : null, "left", "right");

      yOffset += 100;

      if (extractString(json, "postprocessingApplicationFullId") != null)
      {
         activity = newApplicationActivity(processDefinition).withIdAndName(null,
               "Postprocessing App").build();

         activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);
         getModelBuilderFacade().setApplication(activity,
               extractString(json, "postprocessingApplicationFullId"));

         activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

         activitySymbol.setXPos(xActivityOffset);
         activitySymbol.setYPos(yOffset);
         activitySymbol.setWidth(180);
         activitySymbol.setHeight(50);
         activitySymbol.setActivity(activity);
         activity.getActivitySymbols().add(activitySymbol);

         processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
         parentLaneSymbol.getActivitySymbol().add(activitySymbol);

         getModelBuilderFacade().createControlFlowConnection(processDefinition,
               previousActivitySymbol, activitySymbol, "Transition2", "", "", false,
               "true", "bottom", "top");

         inAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
               activity.getApplication(), DirectionType.IN_LITERAL);
         outAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
               activity.getApplication(), DirectionType.OUT_LITERAL);

         getModelBuilderFacade().createDataFlowConnection(processDefinition,
               activitySymbol, dataSymbol, PredefinedConstants.APPLICATION_CONTEXT,
               inAccessPoint != null ? inAccessPoint.getId() : null, PredefinedConstants.APPLICATION_CONTEXT,
               outAccessPoint != null ? outAccessPoint.getId() : null, "left", "right");

         yOffset += 100;
      }

      // Create End Symbol

      EndEventSymbol endEventSymbol = AbstractElementBuilder.F_CWM.createEndEventSymbol();

      endEventSymbol.setXPos(xEventOffset);
      endEventSymbol.setYPos(yOffset);

      processDefinition.getDiagram().get(0).getEndEventSymbols().add(endEventSymbol);

      parentLaneSymbol.getEndEventSymbols().add(endEventSymbol);

      // Create connection to End Event

      // TODO Host activity for end event?

      getModelBuilderFacade().createTransitionSymbol(processDefinition, activitySymbol,
            endEventSymbol, null, "bottom", "top");

      if (extractBoolean(json, "generateTestWrapper"))
      {
         JsonObject wrapperJson = new JsonObject();

         wrapperJson.addProperty("processDefinitionName", processDefinition.getName()
               + " Test");
         wrapperJson.addProperty("processFullId",
               getModelBuilderFacade().createFullId(model, processDefinition));
         wrapperJson.addProperty(
               "participantFullId",
               getModelBuilderFacade().createFullId(model,
                     getModelBuilderFacade().findParticipant(model, "Administrator")));
         wrapperJson.addProperty("dataInputActivityName", "Enter Data");
         wrapperJson.addProperty("subprocessActivityName", processDefinition.getName());
         wrapperJson.addProperty("dataOutputActivityName", "Retrieve Data");

         createProcessInterfaceTestWrapperProcess(model, wrapperJson);
      }
   }

   /**
   *
   */
   public void createProcessInterfaceTestWrapperProcess(ModelType model, JsonObject json)
   {
      ProcessDefinitionType processDefinition = getModelBuilderFacade().createProcess(
            model, null, extractString(json, "processDefinitionName"), "Default",
            "Default");
      modelService.uuidMapper().map(processDefinition);

      ModelBuilderFacade.setBooleanAttribute(processDefinition, PredefinedConstants.PROCESS_IS_AUXILIARY_ATT, true);

      ProcessDefinitionType processInterface = getModelBuilderFacade().findProcessDefinition(
            extractString(json, "processFullId"));

      LaneSymbol parentLaneSymbol = getModelBuilderFacade().findLaneInProcess(
            processDefinition, ModelerConstants.DEF_LANE_ID);

      parentLaneSymbol.setParticipant(getModelBuilderFacade().findParticipant(
            extractString(json, "participantFullId")));

      int activityWidth = 180;
      int activityHeight = 50;
      int eventWidth = 20;
      int xActivityOffset = 100;
      int xDataOffset = 10;

      // Create Start Event

      StartEventSymbol startEventSymbol = AbstractElementBuilder.F_CWM.createStartEventSymbol();

      startEventSymbol.setXPos(xActivityOffset + activityWidth / 2 - eventWidth / 2);
      startEventSymbol.setYPos(50);

      processDefinition.getDiagram().get(0).getStartEventSymbols().add(startEventSymbol);
      parentLaneSymbol.getStartEventSymbols().add(startEventSymbol);

      TriggerType manualTrigger = newManualTrigger(processDefinition).accessibleTo(
            getModelBuilderFacade().findParticipant(
                  extractString(json, "participantFullId"))).build();
      manualTrigger.setId("StartTest");
      manualTrigger.setName("Start Test");
      startEventSymbol.setTrigger(manualTrigger);

      // Create Enter Data Activity

      ActivityType activity = newApplicationActivity(processDefinition).withIdAndName(
            null, extractString(json, "dataInputActivityName")).build();

      activity.setImplementation(ActivityImplementationType.MANUAL_LITERAL);

      ActivitySymbolType activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

      activitySymbol.setXPos(xActivityOffset);
      activitySymbol.setYPos(100);
      activitySymbol.setWidth(activityWidth);
      activitySymbol.setHeight(activityHeight);
      activitySymbol.setActivity(activity);
      activity.getActivitySymbols().add(activitySymbol);

      processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
      parentLaneSymbol.getActivitySymbol().add(activitySymbol);

      // Create connection from Start Event

      // TODO Host activity for start event?

      getModelBuilderFacade().createTransitionSymbol(processDefinition, startEventSymbol,
            activitySymbol, null, "bottom", "top");

      ActivitySymbolType previousActivitySymbol = activitySymbol;

      // Create Subprocess Activity

      activity = newApplicationActivity(processDefinition).withIdAndName(null,
            extractString(json, "subprocessActivityName")).build();

      activity.setImplementation(ActivityImplementationType.SUBPROCESS_LITERAL);
      getModelBuilderFacade().setSubProcess(activity,
            extractString(json, "processFullId"));

      activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

      activitySymbol.setXPos(xActivityOffset);
      activitySymbol.setYPos(300);
      activitySymbol.setWidth(activityWidth);
      activitySymbol.setHeight(activityHeight);
      activitySymbol.setActivity(activity);
      activity.getActivitySymbols().add(activitySymbol);

      processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
      parentLaneSymbol.getActivitySymbol().add(activitySymbol);

      getModelBuilderFacade().createControlFlowConnection(processDefinition,
            previousActivitySymbol, activitySymbol, "Transition1", "Transition1", "", true, "",
            "bottom", "top");

      // Create Input Data

      int yDataOffset = 200;

      if (processInterface.getFormalParameters() != null)
      {
         for (FormalParameterType formalParameter : processInterface.getFormalParameters()
               .getFormalParameter())
         {
            if (formalParameter.getMode() == ModeType.IN
                  || formalParameter.getMode() == ModeType.INOUT)
            {
               String typeDeclarationId = formalParameter.getDataType()
                     .getDeclaredType()
                     .getId();
               TypeDeclarationType typeDeclaration = model.getTypeDeclarations()
                     .getTypeDeclaration(typeDeclarationId);
               String structuredDataTypeFullId = getModelBuilderFacade().createFullId(
                     model, typeDeclaration);

               // TODO Weird programming because Model Builder Facade throws
               // ObjectNotFoundException

               DataType data = null;

               try
               {
                  data = getModelBuilderFacade().findData(model, formalParameter.getId());
               }
               catch (Exception x)
               {
               }
               finally
               {
                  if (data == null)
                  {
                     data = getModelBuilderFacade().createStructuredData(model,
                           formalParameter.getId(), formalParameter.getName(),
                           structuredDataTypeFullId);
                  }
               }

               DataSymbolType dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();
               dataSymbol.setData(data);
               processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
               data.getDataSymbols().add(dataSymbol);
               dataSymbol.setXPos(xDataOffset);
               dataSymbol.setYPos(yDataOffset);

               parentLaneSymbol.getDataSymbol().add(dataSymbol);

               yDataOffset += 50;

               getModelBuilderFacade().createDataFlowConnection(processDefinition,
                     previousActivitySymbol, dataSymbol, DirectionType.OUT_LITERAL,
                     "left", "right", PredefinedConstants.DEFAULT_CONTEXT, null);
               getModelBuilderFacade().createDataFlowConnection(processDefinition,
                     activitySymbol, dataSymbol, DirectionType.IN_LITERAL, "right",
                     "left", PredefinedConstants.PROCESSINTERFACE_CONTEXT, formalParameter.getId());
            }
         }
      }

      previousActivitySymbol = activitySymbol;

      // Create Retrieve Data Activity

      activity = newApplicationActivity(processDefinition).withIdAndName(null,
            extractString(json, "dataOutputActivityName")).build();

      activity.setImplementation(ActivityImplementationType.MANUAL_LITERAL);

      activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

      activitySymbol.setXPos(xActivityOffset);
      activitySymbol.setYPos(500);
      activitySymbol.setWidth(activityWidth);
      activitySymbol.setHeight(activityHeight);
      activitySymbol.setActivity(activity);
      activity.getActivitySymbols().add(activitySymbol);

      processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
      parentLaneSymbol.getActivitySymbol().add(activitySymbol);

      getModelBuilderFacade().createControlFlowConnection(processDefinition,
            previousActivitySymbol, activitySymbol, "Transition2", "Transition2", "", true, "",
            "bottom", "top");

      // Create Output Data

      yDataOffset = 400;

      if (processInterface.getFormalParameters() != null)
      {
         for (FormalParameterType formalParameter : processInterface.getFormalParameters()
               .getFormalParameter())
         {
            if (formalParameter.getMode() == ModeType.OUT
                  || formalParameter.getMode() == ModeType.INOUT)
            {
               String typeDeclarationId = formalParameter.getDataType()
                     .getDeclaredType()
                     .getId();
               TypeDeclarationType typeDeclaration = model.getTypeDeclarations()
                     .getTypeDeclaration(typeDeclarationId);
               String structuredDataTypeFullId = getModelBuilderFacade().createFullId(
                     model, typeDeclaration);

               // TODO Weird programming because Model Builder Facade throws
               // ObjectNotFoundException

               DataType data = null;

               try
               {
                  data = getModelBuilderFacade().findData(model, formalParameter.getId());
               }
               catch (Exception x)
               {
               }
               finally
               {
                  if (data == null)
                  {
                     data = getModelBuilderFacade().createStructuredData(model,
                           formalParameter.getId(), formalParameter.getName(),
                           structuredDataTypeFullId);
                  }
               }

               DataSymbolType dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();

               dataSymbol.setData(data);
               processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
               data.getDataSymbols().add(dataSymbol);
               dataSymbol.setXPos(xDataOffset);
               dataSymbol.setYPos(yDataOffset);

               parentLaneSymbol.getDataSymbol().add(dataSymbol);

               yDataOffset += 50;

               getModelBuilderFacade().createDataFlowConnection(processDefinition,
                     previousActivitySymbol, dataSymbol, DirectionType.OUT_LITERAL,
                     "left", "right", PredefinedConstants.PROCESSINTERFACE_CONTEXT, formalParameter.getId());
               getModelBuilderFacade().createDataFlowConnection(processDefinition,
                     activitySymbol, dataSymbol, DirectionType.IN_LITERAL, "right",
                     "left", PredefinedConstants.DEFAULT_CONTEXT, null);
            }
         }
      }

      // Create End Symbol

      EndEventSymbol endEventSymbol = AbstractElementBuilder.F_CWM.createEndEventSymbol();

      endEventSymbol.setXPos(xActivityOffset + activityWidth / 2 - eventWidth / 2);
      endEventSymbol.setYPos(600);

      processDefinition.getDiagram().get(0).getEndEventSymbols().add(endEventSymbol);

      parentLaneSymbol.getEndEventSymbols().add(endEventSymbol);

      // Create connection to End Event

      getModelBuilderFacade().createTransitionSymbol(processDefinition, activitySymbol,
            endEventSymbol, null, "bottom", "top");
   }
}
