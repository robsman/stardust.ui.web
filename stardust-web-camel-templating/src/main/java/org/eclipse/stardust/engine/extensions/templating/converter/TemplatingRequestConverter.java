package org.eclipse.stardust.engine.extensions.templating.converter;

import static org.eclipse.stardust.engine.extensions.camel.app.mail.TemplateConfigurationUtils.IsConvertToPDF;
import static org.eclipse.stardust.engine.extensions.camel.app.mail.TemplateConfigurationUtils.getOutgoingDocumentId;
import static org.eclipse.stardust.engine.extensions.camel.app.mail.TemplateConfigurationUtils.getTemplateId;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.CredentialProvider;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.templating.core.TemplatingRequest;

@Converter
public class TemplatingRequestConverter
{

   @Converter
   public static TemplatingRequest documentRequestToTemplatingRequest(
         Map<String, Object> documentRequestItem, Exchange exchange)
   {

	   DocumentManagementService dms = getDocumentManagementService();
	   
	      TemplatingRequest templatingRequest = new TemplatingRequest();
	      String format = "text";
	      String documentLocation = "";
	      if ((StringUtils.isNotEmpty(getTemplateId(documentRequestItem)))&&(StringUtils.isEmpty(getOutgoingDocumentId(documentRequestItem))))
	      {	
	    	  if((getTemplateId(documentRequestItem)!=null)&&(StringUtils.isNotEmpty(getOutgoingDocumentId(documentRequestItem)))&&(getTemplateId(documentRequestItem).contains("{urn:repositoryId:System}"))){
	    		  documentLocation= dms.getDocument(getTemplateId(documentRequestItem)).getPath();

		      }else{
		    	  documentLocation = getTemplateId(documentRequestItem);
		      }
	      }else{
	        if((getOutgoingDocumentId(documentRequestItem)!=null)&&(getOutgoingDocumentId(documentRequestItem).contains("{urn:repositoryId:System}"))){
	        	documentLocation= dms.getDocument(getOutgoingDocumentId(documentRequestItem)).getPath();
	          }else{
	        	  documentLocation = getOutgoingDocumentId(documentRequestItem);
	          }
	      }
  
      if (StringUtils.isNotEmpty(documentLocation) && documentLocation.endsWith("docx"))
      {
         format = "docx";
      }
      templatingRequest.setTemplateUri("repository://" + documentLocation);
      templatingRequest.setFormat(format);
      templatingRequest.setConvertToPdf(IsConvertToPDF(documentRequestItem));
      templatingRequest.setParameters(new HashMap<String, Object>());
      return templatingRequest;
   }

   private static ServiceFactory getServiceFactory()
   {
      ServiceFactory sf = ClientEnvironment.getCurrentServiceFactory();
      if (sf == null)
      {
         sf = ServiceFactoryLocator.get(CredentialProvider.CURRENT_TX);
      }
      return sf;
   }

   private static DocumentManagementService getDocumentManagementService()
   {
      return getServiceFactory().getDocumentManagementService();
   }
   
}
