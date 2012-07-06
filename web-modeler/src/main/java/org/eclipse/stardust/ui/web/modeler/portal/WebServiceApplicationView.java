package org.eclipse.stardust.ui.web.modeler.portal;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.extensions.jaxws.app.WSConstants;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

import com.google.gson.JsonObject;

import com.infinity.bpm.rt.integration.webservices.WSConstants50;

public class WebServiceApplicationView {
	private ModelService modelService;
	private JsonObject webServiceApplicationJson;
	private Definition definition;
	private Service service;
	private Port port;
	private BindingOperation operation;
	private Binding binding;
	private String wsdlUrl;
	private String style;
	private String use;
	private String protocol;
	private List<SelectItem> serviceItems = new ArrayList<SelectItem>();
	private List<SelectItem> portItems = new ArrayList<SelectItem>();
	private List<SelectItem> operationItems = new ArrayList<SelectItem>();
	private List<SelectItem> authenticationMechanisms = new ArrayList<SelectItem>();
	private List<SelectItem> authenticationVariants = new ArrayList<SelectItem>();
	private boolean includeWebServiceAddressing;
	private boolean authenticationRequired;
	private String authenticationMechanism;
	private String authenticationVariant;
	private int numberOfRetries;
	private int timeBetweenRetries;

	public WebServiceApplicationView() {
		super();

		wsdlUrl = "http://www.restfulwebservices.net/wcf/CurrencyService.svc?wsdl";
		authenticationMechanisms.add(new SelectItem("HTTPBasicAuthorization",
				"HTTP Basic Authorization"));
		authenticationMechanisms
				.add(new SelectItem("WSSecurity", "WS Security"));
		authenticationVariants.add(new SelectItem("UserNamePasswort",
				"User Name/Password"));
		authenticationVariants.add(new SelectItem("UserNamePasswortDigest",
				"User Name/Password Digest"));
		authenticationVariants.add(new SelectItem("XwssConfiguration",
				"XWSS Configuration"));
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public String getWsdlUrl() {
		return wsdlUrl;
	}

	public void setWsdlUrl(String wsdlUrl) {
		this.wsdlUrl = wsdlUrl;
	}

	/**
	 *
	 */
	public void loadWsdl() {
		serviceItems.clear();
		portItems.clear();
		operationItems.clear();

		definition = JaxWSResource
				.getDefinition("http://www.restfulwebservices.net/wcf/CurrencyService.svc?wsdl");

		for (Object object : definition.getServices().values()) {
			Service service = (Service) object;
			serviceItems.add(new SelectItem(service.getQName().toString(),
					service.getQName().toString()));

			if (serviceItems.size() == 1) {
				setService(service);
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public List<SelectItem> getServiceItems() {
		return serviceItems;
	}

	/**
	 *
	 * @param event
	 */
	public void serviceChanged(ValueChangeEvent event) {
		setService((Service) definition.getServices().get(event.getNewValue()));
	}

	/**
	 *
	 * @param service
	 */
	private void setService(Service service) {
		for (Object object : service.getPorts().values()) {
			Port port = (Port) object;

			portItems.add(new SelectItem(port.getName(), port.getName()));

			if (portItems.size() == 1) {
				setPort(port);
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public List<SelectItem> getPortItems() {
		return portItems;
	}

	/**
	 *
	 * @param event
	 */
	public void portChanged(ValueChangeEvent event) {
		setPort(service.getPort(event.getNewValue().toString()));
	}

	/**
	 *
	 * @param port
	 */
	public void setPort(Port port) {
		this.port = port;
		this.binding = this.port == null ? null : this.port.getBinding();

		setStyle(JaxWSResource.getBindingStyle(binding));

		for (Object object : binding.getBindingOperations()) {
			BindingOperation operation = (BindingOperation) object;

			operationItems.add(new SelectItem(operation.getName(), operation
					.getName()));

			if (operationItems.size() == 1) {
				setOperation(operation);
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public List<SelectItem> getOperationItems() {
		return operationItems;
	}

	/**
	 *
	 * @param event
	 */
	public void operationChanged(ValueChangeEvent event) {
		// setOperation((BindingOperation)binding.getBindingOperation("", "",
		// ""));
		setOperation((BindingOperation) binding.getBindingOperations().get(0));
	}

	/**
	 *
	 */
	public void setOperation(BindingOperation operation) {
		this.operation = operation;

		String style = JaxWSResource.getOperationStyle(operation);

		if (style == null) {
			style = JaxWSResource.getBindingStyle(binding);
		}

		setUse(JaxWSResource.getOperationUse(operation));
		setProtocol(JaxWSResource.getOperationProtocol(operation));
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public boolean isIncludeWebServiceAddressing() {
		return includeWebServiceAddressing;
	}

	public void setIncludeWebServiceAddressing(
			boolean includeWebServiceAddressing) {
		this.includeWebServiceAddressing = includeWebServiceAddressing;
	}

	public boolean isAuthenticationRequired() {
		return authenticationRequired;
	}

	public void setAuthenticationRequired(boolean authenticationRequired) {
		this.authenticationRequired = authenticationRequired;
	}

	/**
	 *
	 * @return
	 */
	public List<SelectItem> getAuthenticationMechanisms() {
		return authenticationMechanisms;
	}

	/**
	 *
	 * @param event
	 */
	public void authenticationMechanismChanged(ValueChangeEvent event) {
	}

	/**
	 *
	 * @return
	 */
	public List<SelectItem> getAuthenticationVariants() {
		return authenticationVariants;
	}

	/**
	 *
	 * @param event
	 */
	public void authenticationVariantChanged(ValueChangeEvent event) {
	}

	public JsonObject getWebServiceApplicationJson() {
		return webServiceApplicationJson;
	}

	public void setWebServiceApplicationJson(
			JsonObject webServiceApplicationJson) {
		this.webServiceApplicationJson = webServiceApplicationJson;

		// setRetry(getWebServiceApplicationJson().get("synchronous:retry:enable"));
		setWsdlUrl(extractString(getWebServiceApplicationJson(), "carnot:engine:wsdlUrl"));
		// setWebService(
		// getWebServiceApplicationJson()
		// .get("carnot:engine:wsServiceName"));
		// setPortName(getWebServiceApplicationJson().get("carnot:engine:wsPortName"));
	}

	/**
	 *
	 */
	public void save() {
		// <carnot:AccessPoint Oid="10049" Id="carnot:engine:endpointAddress"
		// Name="Endpoint Address" Direction="IN" Type="serializable">
		// <carnot:Attributes>
		// <carnot:Attribute Name="carnot:engine:className"
		// Value="java.lang.String"/>
		// <carnot:Attribute Name="carnot:engine:browsable" Value="true"
		// Type="boolean"/>
		// </carnot:Attributes>
		// </carnot:AccessPoint>
		// <carnot:AccessPoint Oid="10139" Id="parameters" Name="parameters"
		// Direction="IN" Type="plainXML">
		// <carnot:Attributes>
		// <carnot:Attribute Name="carnot:engine:browsable" Value="true"
		// Type="boolean"/>
		// </carnot:Attributes>
		// </carnot:AccessPoint>
		// <carnot:AccessPoint Oid="10140" Id="parameters" Name="parameters"
		// Direction="OUT" Type="plainXML"/>

		JsonObject attributes = new JsonObject();

		JsonObject accessPoints = new JsonObject();

		webServiceApplicationJson.add("AccessPoints", accessPoints);
		webServiceApplicationJson.add("Attributes", attributes);

		attributes.addProperty("carnot:engine:wsRuntime", "jaxws");
		attributes.addProperty("carnot:engine:visibility", "Public");
		attributes.addProperty("synchronous:retry:enable", "false");
		attributes
				.addProperty("carnot:engine:wsdlUrl",
						"http://www.restfulwebservices.net/wcf/CurrencyService.svc?wsdl");
		attributes
				.addProperty("carnot:engine:wsServiceName",
						"{http://www.restfulwebservices.net/ServiceContracts/2008/01}CurrencyService");
		attributes.addProperty("carnot:engine:wsPortName",
				"BasicHttpBinding_ICurrencyService");
		attributes
				.addProperty("carnot:engine:wsOperationName", "GetConversionRate");
		attributes
				.addProperty("carnot:engine:wsSoapActionUri", "GetConversionRate");
		attributes.addProperty("carnot:engine:wsSoapProtocol", "SOAP 1.1 Protocol");
		attributes.addProperty("carnot:engine:wsInputOrder", "parameters");
		attributes.addProperty("carnot:engine:wsOutputOrder", "parameters");

		getModelService().updateWebServiceApplication("",
				webServiceApplicationJson);
	}

	public int getNumberOfRetries() {
		return numberOfRetries;
	}

	public void setNumberOfRetries(int numberOfRetries) {
		this.numberOfRetries = numberOfRetries;
	}

	public int getTimeBetweenRetries() {
		return timeBetweenRetries;
	}

	public void setTimeBetweenRetries(int timeBetweenRetries) {
		this.timeBetweenRetries = timeBetweenRetries;
	}

	private void setServiceData(ApplicationType application) {
		if (null != getWsdlUrl()) {
			String serviceName = AttributeUtil.getAttributeValue(application,
					WSConstants.WS_SERVICE_NAME_ATT);
			if (null != serviceName) {
				QName serviceQName = QName.valueOf(serviceName);
				// setViewerSelection(serviceViewer, findService(serviceQName));
				// serviceChanged();
				if (null != service) {
					if (false/*service instanceof DynamicBoundService*/) {
						String uddiUrl = AttributeUtil.getAttributeValue(
								application, WSConstants50.WS_UDDI_URL_ATT);
						// uddiServerURL.setText(uddiUrl == null ? EMPTY_STRING
						// : uddiUrl);
						String binding = AttributeUtil.getAttributeValue(
								application,
								WSConstants50.WS_UDDI_BINDING_KEY_ATT);
						// bindingKey.setText(binding == null ? EMPTY_STRING :
						// binding);
						String access = AttributeUtil.getAttributeValue(
								application,
								WSConstants50.WS_UDDI_ACCESS_POINT_ATT);
						// accessPoint.setText(access == null ? EMPTY_STRING :
						// access);
					} else {
						// uddiServerURL.setText(EMPTY_STRING);
						// bindingKey.setText(EMPTY_STRING);
						// accessPoint.setText(EMPTY_STRING);
					}
				}

				String portName = AttributeUtil.getAttributeValue(application,
						WSConstants.WS_PORT_NAME_ATT);
				QName qName = portName == null ? null : QName.valueOf(portName);
				// setViewerSelection(portViewer, qName == null ? null :
				// service instanceof DynamicBoundService
				// ? ((DynamicBoundService) service).getPort(qName) :
				// service.getPort(portName));
				// portChanged();

				if (null != binding) {
					String operationName = AttributeUtil.getAttributeValue(
							application, WSConstants.WS_OPERATION_NAME_ATT);
					if (!StringUtils.isEmpty(operationName)) {
						String inputName = AttributeUtil.getAttributeValue(
								application,
								WSConstants50.WS_OPERATION_INPUT_NAME_ATT);
						String outputName = AttributeUtil.getAttributeValue(
								application,
								WSConstants50.WS_OPERATION_OUTPUT_NAME_ATT);
						List<BindingOperation> operations = binding
								.getBindingOperations();

						for (BindingOperation operation : operations) {
							if (operationName.equals(operation.getName())) {
								String opInputName = operation
										.getBindingInput() == null ? null
										: operation.getBindingInput().getName();
								String opOutputName = operation
										.getBindingOutput() == null ? null
										: operation.getBindingOutput()
												.getName();
								if (CompareHelper.areEqual(inputName,
										opInputName)
										&& CompareHelper.areEqual(outputName,
												opOutputName)) {
									setOperation(operation);
									break;
								}
							}
						}
					}
				}
			}
		}
	}
}
