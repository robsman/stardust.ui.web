package org.eclipse.stardust.ui.web.modeler.ui.xpdl;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.modeler.spi.ModelerUiExtensionsProvider;

@Component
@Scope("singleton")
public class XpdlModelingUiExtensions implements ModelerUiExtensionsProvider
{
   @Override
   public List<ViewManagerConfiguration> getViewManagerConfigs()
   {
      return singletonList(new ViewManagerConfiguration("jsfViewManager", "m_jsfViewManager"));
   }

   @Override
   public List<PropertyPageConfiguration> getPropertyPageConfigs()
   {
      // TODO Auto-generated method stub
      return asList( //
            new PropertyPageConfiguration("processPropertiesPanel", "basicPropertiesPage",
                  "m_processBasicPropertiesPage", "always"),
            new PropertyPageConfiguration("processPropertiesPanel", "processAnnotationsPropertiesPage",
                  "modelElementAnnotationsPropertiesPage.html", "m_modelElementAnnotationsPropertiesPage", "always"),
            new PropertyPageConfiguration("processPropertiesPanel", "processInterfacePropertiesPage",
                  "m_processProcessInterfacePropertiesPage", "always"),
            new PropertyPageConfiguration("processPropertiesPanel", "dataPathPropertiesPage",
                  "m_processDataPathPropertiesPage", "always"),
            new PropertyPageConfiguration("processPropertiesPanel", "displayPropertiesPage",
                  "m_processDisplayPropertiesPage", "always"),
            new PropertyPageConfiguration("processPropertiesPanel", "processAttachmentsPropertiesPage",
                  "processDefinitionProcessAttachmentsPropertiesPage.html", "m_processProcessAttachmentsPropertiesPage", "always"),

            new PropertyPageConfiguration("activityPropertiesPanel", "basicPropertiesPage",
                  "m_activityBasicPropertiesPage", "always"),
            new PropertyPageConfiguration("activityPropertiesPanel", "activityAnnotationsPropertiesPage",
                  "modelElementAnnotationsPropertiesPage.html", "m_modelElementAnnotationsPropertiesPage", "always"),
            new PropertyPageConfiguration("activityPropertiesPanel", "processingPropertiesPage",
                  "m_activityProcessingPropertiesPage", "preview"),
            new PropertyPageConfiguration("activityPropertiesPanel", "controllingPropertiesPage",
                  "activityControllingPropertiesPage.html", "m_activityControllingPropertiesPage", "always"),
            new PropertyPageConfiguration("activityPropertiesPanel", "qualityControlPropertiesPage",
                  "m_activityQualityControlPropertiesPage", "preview"),
            new PropertyPageConfiguration("activityPropertiesPanel", "serviceParametersPropertiesPage",
                  "activityServiceParametersPropertiesPage.html", "m_activityServiceParametersPropertiesPage", "always"),

            new PropertyPageConfiguration("eventPropertiesPanel", "basicPropertiesPage",
                  "m_eventBasicPropertiesPage", "always"),
            new PropertyPageConfiguration("eventPropertiesPanel", "eventAnnotationsPropertiesPage",
                  "modelElementAnnotationsPropertiesPage.html", "m_modelElementAnnotationsPropertiesPage", "always"),

            new PropertyPageConfiguration("gatewayPropertiesPanel", "basicPropertiesPage",
                  "m_gatewayBasicPropertiesPage", "always"),
            new PropertyPageConfiguration("gatewayPropertiesPanel", "gatewayAnnotationsPropertiesPage",
                  "modelElementAnnotationsPropertiesPage.html", "m_modelElementAnnotationsPropertiesPage", "always"),

            new PropertyPageConfiguration("swimlanePropertiesPanel", "basicPropertiesPage",
                  "m_swimlaneBasicPropertiesPage", "always"),
            new PropertyPageConfiguration("swimlanePropertiesPanel", "swimlaneAnnotationsPropertiesPage",
                  "modelElementAnnotationsPropertiesPage.html", "m_modelElementAnnotationsPropertiesPage", "always"),

            new PropertyPageConfiguration("dataPropertiesPanel", "basicPropertiesPage",
                  "m_dataBasicPropertiesPage", "always"),
            new PropertyPageConfiguration("dataPropertiesPanel", "dataAnnotationsPropertiesPage",
                  "modelElementAnnotationsPropertiesPage.html", "m_modelElementAnnotationsPropertiesPage", "always"),

            new PropertyPageConfiguration("controlFlowPropertiesPanel", "basicPropertiesPage",
                  "m_controlFlowBasicPropertiesPage", "always"),
            new PropertyPageConfiguration("controlFlowPropertiesPanel", "transactionPropertiesPage",
                  "controlFlowTransactionPropertiesPage.html", "m_controlFlowTransactionPropertiesPage", "always"),
            new PropertyPageConfiguration("controlFlowPropertiesPanel", "controlFlowAnnotationsPropertiesPage",
                  "modelElementAnnotationsPropertiesPage.html", "m_modelElementAnnotationsPropertiesPage", "always"),

            new PropertyPageConfiguration("dataFlowPropertiesPanel", "basicPropertiesPage",
                  "m_dataFlowBasicPropertiesPage", "always"),
            new PropertyPageConfiguration("dataFlowPropertiesPanel", "dataFlowgatewayAnnotationsPropertiesPage",
                  "modelElementAnnotationsPropertiesPage.html", "m_modelElementAnnotationsPropertiesPage", "always"),

            new PropertyPageConfiguration("annotationPropertiesPanel", "basicPropertiesPage",
                  "annotationBasicPropertiesPage.html", "m_annotationBasicPropertiesPage", "always")
      );
   }

   @Override
   public List<ToolbarSectionConfiguration> getToolbarSectionConfigs()
   {
      return asList(
            new ToolbarSectionConfiguration("drawingPalette", "Drawing", "always"),
            new ToolbarSectionConfiguration("activityPalette", "Activities and Gateways", "always"),
            new ToolbarSectionConfiguration("eventPalette", "Events", "always"),
            new ToolbarSectionConfiguration("dataPalette", "Data", null),
            new ToolbarSectionConfiguration("lanePalette", "Pools and Lanes", "always"),
            new ToolbarSectionConfiguration("connectorPalette", "Data and Sequence Flow", "always"),
            new ToolbarSectionConfiguration("annotationPalette", "Annotations", "always"),
            new ToolbarSectionConfiguration("decorationPalette", "Decoration", "decorationPalette.html", "m_decorationPalette", "preview")
      );
   }

   @Override
   public List<ToolbarEntryConfiguration> getToolbarEntryConfigs()
   {
      return asList(
            new ToolbarEntryConfiguration("selectModeButton", "drawingPalette", "Select Mode",
                  "../../images/icons/select.png", "m_defaultPaletteHandler", "setSelectMode", "always"),
            new ToolbarEntryConfiguration("separatorModeButton", "drawingPalette", "Separator Mode",
                  "../../images/icons/separator.png", "m_defaultPaletteHandler", "setSeparatorMode", "always"),
            new ToolbarEntryConfiguration("zoomInButton", "drawingPalette", "Zoom In",
                  "../../images/icons/zoom-in.png", "m_defaultPaletteHandler", "zoomIn", "always"),
            new ToolbarEntryConfiguration("zoomOutButton", "drawingPalette", "Zoom Out",
                  "../../images/icons/zoom-out.png", "m_defaultPaletteHandler", "zoomOut", "always"),
            new ToolbarEntryConfiguration("flipOrientationButton", "drawingPalette", "Flip Orientation",
                  "../../images/icons/horizontal-flip.png", "m_defaultPaletteHandler", "flipOrientation", "always"),
            new ToolbarEntryConfiguration("printButton", "drawingPalette", "Print",
                  "../../images/icons/print.gif", "m_defaultPaletteHandler", "print", "always"),

            new ToolbarEntryConfiguration("activityButton", "activityPalette", "Create Activity",
                  "../../images/icons/activity.png", "m_defaultPaletteHandler", "createActivity", "always"),
            new ToolbarEntryConfiguration("gatewayButton", "activityPalette", "Create Gateway",
                  "../../images/icons/gateway.png", "m_defaultPaletteHandler", "createGateway", "always"),

            new ToolbarEntryConfiguration("startEventButton", "eventPalette", "Create Start Event",
                  "../../images/icons/start_event_with_border.png", "m_defaultPaletteHandler", "createStartEvent", "always"),
            new ToolbarEntryConfiguration("endEventButton", "eventPalette", "Create End Event",
                  "../../images/icons/end_event_with_border.png", "m_defaultPaletteHandler", "createEndEvent", "always"),

            new ToolbarEntryConfiguration("dataButton", "dataPalette", "Create Primitive Data",
                  "../../images/icons/data.png", "m_defaultPaletteHandler", "createData", "always"),

            new ToolbarEntryConfiguration("swimlaneButton", "lanePalette", "Create Swimlane",
                  "../../images/icons/lane.png", "m_defaultPaletteHandler", "createSwimlane", "always"),

            new ToolbarEntryConfiguration("connectorButton", "connectorPalette", "Create Connector",
                  "../../images/icons/connect.png", "m_defaultPaletteHandler", "createConnector", "always"),

            new ToolbarEntryConfiguration("annotationButton", "annotationPalette", "Create Annotation",
                  "../../images/icons/annotation.png", "m_defaultPaletteHandler", "createAnnotation", "always")
      );
   }

   @Override
   public List<ApplicationTypeConfiguration> getApplicationTypeConfigs()
   {
      return asList(
            new ApplicationTypeConfiguration("interactive", "UI Mashup",
                  "../images/icons/application-c-ext-web.png", "uiMashupApplicationView"),
            new ApplicationTypeConfiguration("webservice", "Web Service",
                  "../images/icons/application-web-service.png", "webServiceApplicationView"),
            new ApplicationTypeConfiguration("messageTransformationBean", "Message Transformation Application",
                  "../images/icons/application-message-trans.png", "messageTransformationApplicationView"),
            new ApplicationTypeConfiguration("camelSpringProducerApplication", "Camel Application",
                  "../images/icons/application-camel.png", "camelApplicationView"),
            new ApplicationTypeConfiguration("plainJava", "Plain Java Application",
                  "../images/icons/application-plain-java.png", "genericApplicationView"),
            new ApplicationTypeConfiguration("rulesEngineBean", "Business Rules Application",
                  "../images/icons/application-drools.png", "genericApplicationView"),
            new ApplicationTypeConfiguration("dmsOperation", "Document Management Application",
                  "../images/icons/application-plain-java.png", "genericApplicationView"),
            new ApplicationTypeConfiguration("jms", "JMS Application",
                  "../images/icons/application-plain-java.png", "genericApplicationView"),
            new ApplicationTypeConfiguration("mailBean", "Business Rules Application",
                  "../images/icons/application-plain-java.png", "genericApplicationView"),
            new ApplicationTypeConfiguration("messageParsingBean", "Message Parsing Application",
                  "../images/icons/application-plain-java.png", "genericApplicationView"),
            new ApplicationTypeConfiguration("messageSerializationBean", "Message Serialization Application",
                  "../images/icons/application-plain-java.png", "genericApplicationView"),
            new ApplicationTypeConfiguration("sessionBean", "EJB Session Bean Application",
                  "../images/icons/application-plain-java.png", "genericApplicationView"),
            new ApplicationTypeConfiguration("springBean", "Spring Bean Application",
                  "../images/icons/application-drools.png", "genericApplicationView"),
            new ApplicationTypeConfiguration("xslMessageTransformationBean", "XSL Message Transformation Application",
                  "../images/icons/application-drools.png", "genericApplicationView")
      );
   }

   @Override
   public List<DataTypeConfiguration> getDataTypeConfigs()
   {
      return asList(
            new DataTypeConfiguration("serializable", "Serializable Java Class",
                  "../images/icons/application-c-ext-web.png"),
            new DataTypeConfiguration("entity", "Entity Bean",
                  "../images/icons/application-web-service.png"),
            new DataTypeConfiguration("dmsDocumentList", "Document List",
                  "../images/icons/application-web-service.png")
      );
   }

}
