package org.eclipse.stardust.ui.web.modeler.service;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.eclipse.stardust.engine.extensions.jaxws.app.WSConstants;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContext;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

@org.springframework.stereotype.Service
public class WebServicesSupport
{
   @Resource
   private ModelService modelService;

   /**
    * Returns a JSON representation of the service structure underneath the
    * <code>wsdlUrl</code> provided with the input JSON.
    * <p>
    * <b>Members:</b>
    * <ul>
    * <li><code>WSConstants.WS_WSDL_URL_ATT</code> a string containing the URL from which
    * the WSDL document was loaded.</li>
    * <li><code>"services"</code> a JsonArray of JsonObjects each containing specification
    * of one service, including the dynamically bound meta service, having the structure:
    * <ul>
    * <li><code>"name"</code> a string containing the local name of the service (for
    * display purposes).</li>
    * <li><code>WSConstants.WS_SERVICE_NAME_ATT</code> a string containing the qualified
    * name of the service.</li>
    * <li><code>"ports"</code> a JsonArray of JsonObjects each containing specification of
    * one port, with the structure:
    * <ul>
    * <li><code>"name"</code> a string containing the local name of the port (for display
    * purposes).</li>
    * <li><code>WSConstants.WS_PORT_NAME_ATT</code> a string containing the qualified name
    * of the port.</li>
    * <li><code>"style"</code> a string containing the binding style, i.e. "document" (for
    * display purposes). This may be displayed if the operation does not provide a style</li>
    * <li><code>"operations"</code> a JsonArray of JsonObjects each containing
    * specification of one operation, with the structure:
    * <ul>
    * <li><code>"name"</code> a string containing the operation name (for display
    * purposes).</li>
    * <li><code>WSConstants.WS_OPERATION_NAME_ATT</code> a string containing the qualified
    * operation name.</li>
    * <li><code>"style"</code> a string containing the operation style, i.e. "document"
    * (for display purposes).</li>
    * <li><code>"use"</code> a string containing the operation use, i.e. "literal" (for
    * display purposes).</li>
    * <li><code>WSConstants.WS_OPERATION_INPUT_NAME_ATT</code> a string containing the
    * input name.</li>
    * <li><code>WSConstants.WS_OPERATION_OUTPUT_NAME_ATT</code> a string containing the
    * output name.</li>
    * <li><code>WSConstants.WS_SOAP_ACTION_URI_ATT</code> a string containing the SOAP
    * action URI.</li>
    * <li><code>WSConstants.WS_SOAP_PROTOCOL_ATT</code> a string containing the SOAP
    * protocol.</li>
    * <li><code>WSConstants.WS_INPUT_ORDER_ATT</code> a string containing the list of
    * parts composing the input message.</li>
    * <li><code>WSConstants.WS_OUTPUT_ORDER_ATT</code> a string containing the list of
    * parts composing the output message.</li>
    * </ul>
    * </li>
    * </ul>
    * </li>
    * </ul>
    * </li>
    * </ul>
    *
    * @param postedData
    *           a JsonObject that contains a primitive (String) member with the name
    *           "wsdlUrl" that specifies the URL from where the WSDL should be loaded.
    * @param springContext
    * @return the JsonObject containing the representation of the services.
    */
   public JsonObject getWebServiceStructure(JsonObject postedData, ApplicationContext springContext)
   {
      String wsdlUrl = postedData.get("wsdlUrl").getAsString();
      String modelID = postedData.get("modelID").getAsString();
      System.out.println("===> Get Web Service Structure for URL " + wsdlUrl);

      if (wsdlUrl != null && wsdlUrl.indexOf("${") > -1)
      {
         ModelType model = modelService.getModelManagementStrategy().getModels()
               .get(modelID);
         if (model != null)
         {
            VariableContext variableContext = new VariableContext();
            variableContext.initializeVariables(model);
            wsdlUrl = variableContext.replaceAllVariablesByDefaultValue(wsdlUrl);
         }
      }

      Definition definition = JaxWSResource.getDefinition(wsdlUrl);

      @SuppressWarnings("unchecked")
      Collection<Service> services = definition.getServices().values();
      @SuppressWarnings("unchecked")
      Collection<Binding> bindings = definition.getBindings().values();

      JsonObject webServiceJson = new JsonObject();
      webServiceJson.addProperty(WSConstants.WS_WSDL_URL_ATT, wsdlUrl);
      addServices(webServiceJson, services, bindings);
      return webServiceJson;
   }

   /**
    * Adds the service definitions to the parent json object.
    *
    * @param webServiceJson
    *           the parent json object.
    * @param services
    *           the list of services declared in the wsdl document.
    * @param bindings
    *           the list of bindings declared in the wsdl document.
    */
   private void addServices(JsonObject webServiceJson, Collection<Service> services,
         Collection<Binding> bindings)
   {
      JsonObject servicesJson = new JsonObject();
      webServiceJson.add("services", servicesJson);

      for (Service service : services)
      {
         QName qname = service.getQName();

         @SuppressWarnings("unchecked")
         Collection<Port> ports = service.getPorts().values();

         JsonObject serviceJson = new JsonObject();

         serviceJson.addProperty("name", qname.getLocalPart());
         serviceJson.addProperty(WSConstants.WS_SERVICE_NAME_ATT, qname.toString());
         addPorts(serviceJson, ports);
         servicesJson.add(qname.getLocalPart(), serviceJson);
      }

      JsonObject serviceJson = new JsonObject();
      serviceJson.addProperty("name", DYNAMIC_BOUND_SERVICE_QNAME.getLocalPart());
      serviceJson.addProperty(WSConstants.WS_SERVICE_NAME_ATT,
            DYNAMIC_BOUND_SERVICE_QNAME.toString());
      addPorts(serviceJson, bindings);
      servicesJson.add(DYNAMIC_BOUND_SERVICE_QNAME.getLocalPart(), serviceJson);
   }

   /**
    * Adds port or binding definitions to the service json.
    *
    * @param serviceJson
    *           the json object representing the parent service.
    * @param ports
    *           the list of ports or bindings declared for the service.
    */
   private void addPorts(JsonObject serviceJson, Collection<? > ports)
   {
      JsonObject portsJson = new JsonObject();

      serviceJson.add("ports", portsJson);

      for (Object port : ports)
      {
         String name = port instanceof Port
               ? ((Port) port).getName()
               : ((Binding) port).getQName().getLocalPart();
         Binding binding = port instanceof Port
               ? ((Port) port).getBinding()
               : (Binding) port;

         @SuppressWarnings("unchecked")
         Collection<BindingOperation> operations = binding.getBindingOperations();

         JsonObject portJson = new JsonObject();
         portJson.addProperty("name", name);
         portJson.addProperty(WSConstants.WS_PORT_NAME_ATT, name);
         portJson.addProperty("style", JaxWSResource.getBindingStyle(binding));
         addOperations(portJson, operations);
         portsJson.add(name, portJson);
      }
   }

   /**
    * Duplicated from WSConstants in 7.1
    */
   public static final String WS_OPERATION_INPUT_NAME_ATT = "carnot:engine:wsOperationInputName";

   public static final String WS_OPERATION_OUTPUT_NAME_ATT = "carnot:engine:wsOperationOutputName";

   public static final String WS_SOAP_ACTION_URI_ATT = "carnot:engine:wsSoapActionUri";

   public static final String WS_SOAP_PROTOCOL_ATT = "carnot:engine:wsSoapProtocol";

   public static final String WS_INPUT_ORDER_ATT = "carnot:engine:wsInputOrder";

   public static final String WS_OUTPUT_ORDER_ATT = "carnot:engine:wsOutputOrder";

   public static final QName DYNAMIC_BOUND_SERVICE_QNAME = new QName(
         "http://www.carnot.ag/ws", "Dynamically bound Service");

   /**
    * Computes a string containing a comma separated list of the parts composing the
    * message.
    *
    * @param message
    *           the Message
    * @return the computed list of parts
    */
   public static String getPartsOrder(Message message)
   {
      if (message == null)
      {
         return "";
      }

      @SuppressWarnings("unchecked")
      List<Part> parts = message.getOrderedParts(null);

      if (parts.isEmpty())
      {
         return "";
      }

      StringBuffer buffer = new StringBuffer();

      for (Part part : parts)
      {
         if (buffer.length() > 0)
         {
            buffer.append(',');
         }
         buffer.append(part.getName());
      }

      return buffer.toString();
   }

   /**
    * Adds operation definitions to the port json.
    *
    * @param portJson
    *           the json object representing the parent port.
    * @param operations
    *           the list of operations declared for the port.
    */
   private void addOperations(JsonObject portJson, Collection<BindingOperation> operations)
   {
      JsonObject operationsJson = new JsonObject();

      portJson.add("operations", operationsJson);

      for (BindingOperation operation : operations)
      {
         String name = getOperationName(operation);
         BindingInput bindingInput = operation.getBindingInput();
         String inputName = bindingInput == null ? null : bindingInput.getName();
         BindingOutput bindingOutput = operation.getBindingOutput();
         String outputName = bindingOutput == null ? null : bindingOutput.getName();
         Input input = operation.getOperation().getInput();
         Output output = operation.getOperation().getOutput();

         JsonObject operationJson = new JsonObject();

         operationJson.addProperty("name", name);
         operationJson.addProperty(WSConstants.WS_OPERATION_NAME_ATT, operation.getName());
         String style = JaxWSResource.getOperationStyle(operation);
         if (style == null)
         {
            if ( !(portJson.get("style") instanceof JsonNull))
            {
               style = portJson.get("style").getAsString();
            }
         }
         operationJson.addProperty("style", style);
         operationJson.addProperty("use", JaxWSResource.getOperationUse(operation));
         operationJson.addProperty(WS_OPERATION_INPUT_NAME_ATT, inputName);
         operationJson.addProperty(WS_OPERATION_OUTPUT_NAME_ATT, outputName);
         operationJson.addProperty(WS_SOAP_ACTION_URI_ATT,
               JaxWSResource.getSoapActionUri(operation));
         operationJson.addProperty(WS_SOAP_PROTOCOL_ATT,
               JaxWSResource.getOperationProtocol(operation));
         operationJson.addProperty(WS_INPUT_ORDER_ATT, getPartsOrder(input == null
               ? null
               : input.getMessage()));
         operationJson.addProperty(WS_OUTPUT_ORDER_ATT, getPartsOrder(output == null
               ? null
               : output.getMessage()));

         operationsJson.add(name, operationJson);
         if (portJson.get("style") instanceof JsonNull)
         {
            portJson.addProperty("style", style);
         }
      }
   }

   /**
    * Computes a unique label for the operation by appending the input and output names to
    * the operation name.
    *
    * @param operation
    *           the BindingOperation
    * @return the computed label
    */
   private String getOperationName(BindingOperation operation)
   {
      String name = operation.getName();
      BindingInput bindingInput = operation.getBindingInput();
      String inputName = bindingInput == null ? null : bindingInput.getName();
      BindingOutput bindingOutput = operation.getBindingOutput();
      String outputName = bindingOutput == null ? null : bindingOutput.getName();

      if (name != null)
      {
         if (inputName == null)
         {
            if (outputName == null)
            {
               return name;
            }
            else
            {
               return name + "(:none," + outputName + ")";
            }
         }
         else
         {
            if (outputName == null)
            {
               return name + "(" + inputName + ",:none)";
            }
            else
            {
               return name + "(" + inputName + "," + outputName + ")";
            }
         }
      }

      return "";
   }


}
