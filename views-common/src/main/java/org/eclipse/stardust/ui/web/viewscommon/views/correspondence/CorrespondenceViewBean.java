/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.views.correspondence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.mail.Message.RecipientType;
import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.core.EMailAddressValidator;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.services.ContextPortalServices;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryDocumentUserObject;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentEditor;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRVersionTracker;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentEditor.DocumentEditingPolicy;
import org.eclipse.stardust.ui.web.viewscommon.views.document.helper.CorrespondenceMetaData;
import org.eclipse.stardust.ui.web.viewscommon.views.document.pdf.PDFConverterHelper;
import org.eclipse.stardust.ui.web.viewscommon.views.document.pdf.PdfResource;
import org.eclipse.stardust.ui.web.viewscommon.views.printer.PrinterDialogPopup;


import com.icesoft.faces.component.dragdrop.DropEvent;
import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputrichtext.InputRichText;
import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Pulsate;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class CorrespondenceViewBean extends UIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final float EFFECT_DURATION = 2.0f;
   private static final Logger trace = LogManager.getLogger(CorrespondenceViewBean.class);


   // Input parameter
   private ProcessInstance processInstance;

   // Form fields
   private String mailSender;
   private String toMailAddress;
   private String ccMailAddress;
   private String bccMailAddress;
   private String faxMailAddress;
   private String mailSubject;
   private List<Attachment> attachments;
   private InputRichText emailRichText;

   // Enabler flags
   private boolean showCc = false;
   private boolean showBcc = false;
   private boolean showFax = false;

   // Spring dependency injected
   private IDocumentEditor editor;
   private MailService mailService;

   private Map<String, String> requestParamMap = new HashMap<String, String>();
   private String addressCategory;
   private Effect dropBoxEffect;

   private CorrespondenceAttachments correspondenceAttachments;
   private boolean printAvailable = false;
   private PDFConverterHelper pdfConverterHelper;
   /**
    * default constructor
    */
   public CorrespondenceViewBean()
   {
      super("correspondenceView");
      attachments = new ArrayList<Attachment>();
      Document document = (Document) PortalApplication.getInstance().getFocusView().getViewParams().get("attachment");
      if (null != document)
      {
         addAttachments(new Attachment(document));
      }
      String procInstOID = (String) PortalApplication.getInstance().getFocusView().getViewParams().get(
            "processInstanceOID");
      processInstance = ProcessInstanceUtils.getProcessInstance(Long.valueOf(procInstOID));
      requestParamMap.put("To", "toaddressess");
      requestParamMap.put("Cc", "ccaddressess");
      requestParamMap.put("Bcc", "bccaddressess");
      requestParamMap.put("Fax", "faxaddressess");
      dropBoxEffect = new Pulsate(EFFECT_DURATION);
      dropBoxEffect.setFired(true);

      // set sender's mail address
      String emailId = getMailSender();
      emailId = (emailId == null || emailId.equals("")) ? DocumentMgmtUtility.getUser().getEMail() : emailId;
      emailId = (emailId == null || emailId.equals(""))
            ? Parameters.instance().getString(EngineProperties.MAIL_SENDER)
            : emailId;
      setMailSender(emailId);

      pdfConverterHelper = new PDFConverterHelper(); 
      printAvailable = pdfConverterHelper.isPDFConverterAvailable();
      
      correspondenceAttachments = new CorrespondenceAttachments(new CorrespondenceAttachmentsHandler(){
         boolean templatesAdded = false;

         public void popupOpened()
         {
            templatesAdded = false;
         }

         public void popupClosed()
         {
            // Need this as content is somehow not getting refreshed with Added Templates
            if (templatesAdded)
            {
               FacesUtils.refreshPage();
            }
         }

         public boolean addAttachment(Document document)
         {
            return CorrespondenceViewBean.this.addAttachments(new Attachment(document));
         }

         public boolean addTemplate(Document document, AddPolicy addPolicy)
         {
            boolean ret = CorrespondenceViewBean.this.addTemplate(document, AddPolicy.AT_TOP == addPolicy);
            if (ret)
            {
               templatesAdded = true;
            }

            return ret;
         }

         public boolean isDocumentTemplate(Document document)
         {
            return CorrespondenceViewBean.this.isDocumentTemplate(document);
         }
      });
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public void initialize()
   {}

   /**
    * 
    */
   public void selectAttachments()
   {
      correspondenceAttachments.setShowProcessDocuments(true);
      correspondenceAttachments.setProcessInstance(processInstance);
      correspondenceAttachments.openPopup();
   }

   /**
    * 
    */
   public void selectTemplates()
   {
      correspondenceAttachments.setShowProcessDocuments(false);
      correspondenceAttachments.openPopup();
   }

   /**
    * Action hanlder file to attach file
    */
   public void attachFile()
   {
      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
      fileUploadDialog.initialize();
      fileUploadDialog.setViewComment(false);
      fileUploadDialog.setViewDescription(false);
      fileUploadDialog.setOpenDocument(false);
      fileUploadDialog.setEnableOpenDocument(false);
      fileUploadDialog.setViewDocumentType(false);
      
      fileUploadDialog.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType == EventType.APPLY)
            {
               try
               {
                  processUploadedFile();
               }
               catch (Exception e)
               {
               }
            }
         }
      });
      fileUploadDialog.openPopup();
   }

   /**
    * Action handler method to remove attachments
    */
   public void removeAttachment()
   {
      try
      {

         FacesContext context = FacesContext.getCurrentInstance();
         String documentKey = (String) context.getExternalContext().getRequestParameterMap().get("documentKey");

         for (Attachment attachment1 : attachments)
         {
            if (attachment1.getDocumentKey().equals(documentKey))
            {
               attachments.remove(attachment1);
               break;
            }
         }
      }
      catch (Exception exception)
      {
         ExceptionHandler.handleException(exception);
      }
   }

   /**
    * Action handler - gets invoked on send mail click button
    * 
    * @param actionEvent
    * @throws Exception
    */
   public void sendMessage(ActionEvent actionEvent) throws Exception
   {
      try
      {
         Map<RecipientType, String[]> recipientDetails = new HashMap<RecipientType, String[]>();
         List<String> errorMsg = validateMailAddresses(recipientDetails);

         if (errorMsg.size() == 0)
         {
            // Send mail
            boolean sendMailSuccess = mailService.sendMail(recipientDetails, mailSender, mailSubject, editor.getContent(), attachments);

            if (sendMailSuccess)
            {
               // Save document
               saveEmailDocument();
               PortalApplication.getInstance().closeFocusView();
            }
         }
         else
         {
            StringBuilder sb = new StringBuilder();
            for (String msg : errorMsg)
            {
               sb.append(msg);
               sb.append(",");
               sb.append("\n");
            }
            MessageDialog.addErrorMessage(sb.toString());
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * openAddressPickerDialog
    */
   public void openAddressPickerDialog()
   {
      CorrespondenceAddressPickerDialog correspondenceAddressPickerDialog = CorrespondenceAddressPickerDialog
            .getCurrentInstance();
      correspondenceAddressPickerDialog.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType == EventType.APPLY)
            {
               try
               {
                  populateAddress();
               }
               catch (Exception e)
               {
               }
            }
         }
      });
      FacesContext context = FacesContext.getCurrentInstance();
      addressCategory = ((String) context.getExternalContext().getRequestParameterMap().get("from")).trim();
      correspondenceAddressPickerDialog.setEnteredValues(getEnteredMailAddresses(addressCategory));
      correspondenceAddressPickerDialog.setDataPathContacts(generateDataPathContacts());
      correspondenceAddressPickerDialog.setLabel(getMessages().getString("details." + addressCategory.toLowerCase()));
      correspondenceAddressPickerDialog.openPopup();
   }

   /**
    * @param dropEvent
    */
   public void dropAttachment(DropEvent dropEvent)
   {
      //if (dropEvent.getEventType() == DropEvent.DROPPED)
      {
         try
         {
            DefaultMutableTreeNode valueNode = (DefaultMutableTreeNode) dropEvent.getTargetDragValue();
            RepositoryDocumentUserObject docUserObject = (RepositoryDocumentUserObject) valueNode.getUserObject();
            Document draggedFile = (Document) docUserObject.getResource();
            addAttachments(new Attachment(draggedFile));
         }
         catch (Exception e)
         {
         }
      }
   }

   /**
    * Template drag n drop
    * 
    * @param dropEvent
    */
   public void onTemplateDropped(DropEvent dropEvent)
   {
      try
      {
         if (DropEvent.HOVER_START == dropEvent.getEventType())
         {
            getDropBoxEffect().setFired(false);
         }
         if (DropEvent.DROPPED == dropEvent.getEventType())
         {
            if (dropEvent.getTargetDragValue() == null)
            {
               return;
            }
            DefaultMutableTreeNode valueNode = (DefaultMutableTreeNode) dropEvent.getTargetDragValue();
            RepositoryDocumentUserObject docUserObject = (RepositoryDocumentUserObject) valueNode.getUserObject();
            Document draggedFile = (Document) docUserObject.getResource();
            addTemplate(draggedFile, false);
         }
      }
      catch (Exception e)
      {
      }
   }

   /**
    * @param document
    * @param addAtTop
    * @return
    */
   private boolean addTemplate(Document document, boolean addAtTop)
   {
      if (isDocumentTemplate(document))
      {
         String tempContent = new String(DocumentMgmtUtility.getDocumentManagementService()
               .retrieveDocumentContent(document.getId()));
         
         tempContent = resolveExpressions(tempContent);
         
         editor.addContent(tempContent, addAtTop
               ? DocumentEditingPolicy.ADD_AT_TOP
               : DocumentEditingPolicy.ADD_AT_BOTTOM);
         
         return true;
      }
      else
      {
         MessageDialog.addErrorMessage(this.getMessages().getString("invalid.template.message") + " "
               + document.getContentType());
         return false;
      }
   }

   /**
    * @param document
    * @return
    */
   private boolean isDocumentTemplate(Document document)
   {
      if (document.getContentType().trim().equals(MimeTypesHelper.HTML.getType())
            || document.getContentType().trim().equals(MimeTypesHelper.TXT.getType()))
      {
         return true;
      }
      
      return false;
   }
   
   /**
    * print
    */
   public void print()
   {
      PrinterDialogPopup printerDialogPopup = PrinterDialogPopup.getCurrent();
      printerDialogPopup.setPdfResource(getPdfResource(printerDialogPopup));
      printerDialogPopup.openPopup();
   }

   /**
    * action hanlder
    */
   public void selectCc()
   {
      showCc = true;
   }

   /**
    * action hanlder
    */
   public void selectBcc()
   {
      showBcc = true;
   }

   /**
    * action hanlder
    */
   public void selectFax()
   {
      showFax = true;
   }

   
   /**
    * resolves the expression in template using in datapath
    * @param input
    * @return
    */
   private String resolveExpressions(String input)
   {
      try
      {
         String fragment = input;
         String expression = input;
         String result = "";
   
         for (int start = fragment.indexOf("#{"); start >-1; start = fragment.indexOf("#{"))
         {
            result += fragment.substring(0, start);
            fragment = fragment.substring(start + 2);
   
            int end = fragment.indexOf("}");
   
            expression = fragment.substring(0, end);
   
            try
            {
               result += getCurrentProcessInstanceDataValue(expression);
            }
            catch (Exception e)
            {
               trace.warn("Could not resolve expression for expression:" + expression);
               result += "<span style=\"background-color: rgb(255, 255, 153);\">#{"
                     + expression + "}</span>";
            }
   
            fragment = fragment.substring(end + 1);
         }
   
         result += fragment;
   
         return result;
      }
      catch(Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      
      return input;
   }
   
   private Object getCurrentProcessInstanceDataValue(String path)
   {
      return ContextPortalServices.getWorkflowService().getInDataPath(processInstance.getOID(),
            path);
   }
   
   /**
    * populate Address
    */
   private void populateAddress()
   {
      CorrespondenceAddressPickerDialog correspondenceAddressPickerDialog = CorrespondenceAddressPickerDialog
            .getCurrentInstance();
      StringBuffer mailAddress = new StringBuffer();
      for (DataPathValue selectedValue : correspondenceAddressPickerDialog.getEnteredValues())
      {
         mailAddress.append(selectedValue.getDataPathValue());
         mailAddress.append("; ");
      }
      if (addressCategory.equalsIgnoreCase("To"))
      {
         setToMailAddress(mailAddress.toString());
      }
      if (addressCategory.equalsIgnoreCase("Cc"))
      {
         setCcMailAddress(mailAddress.toString());
      }
      if (addressCategory.equalsIgnoreCase("Bcc"))
      {
         setBccMailAddress(mailAddress.toString());
      }
      if (addressCategory.equalsIgnoreCase("Fax"))
      {
         setFaxMailAddress(mailAddress.toString());
      }
   }

   /**
    * @return dataPathContacts
    */
   private List<DataPathValue> generateDataPathContacts()
   {
      List<DataPathValue> dataPathContacts = new ArrayList<DataPathValue>();
      ProcessDefinition processDefinition = null;
      try
      {
         processDefinition = ContextPortalServices.getQueryService().getProcessDefinition(
               processInstance.getProcessID());
      }
      catch (Exception e)
      {
         trace.error(e);
      }
      if (processDefinition != null)
      {
         List<DataPath> list = processDefinition.getAllDataPaths();

         for (int n = 0; n < list.size(); ++n)
         {
            DataPath dataPath = list.get(n);
            if (dataPath.getDirection().equals(Direction.IN) || dataPath.getDirection().equals(Direction.IN_OUT))
            {
               Object dataValue = null;
               try
               {
                  dataValue = ContextPortalServices.getWorkflowService().getInDataPath(processInstance.getOID(),
                        dataPath.getId());
               }
               catch (Exception e)
               {
                  trace.error(e);
               }
               if (dataValue != null && EMailAddressValidator.validateEmailAddress(dataValue.toString()))
               {
                  dataPathContacts.add(new DataPathValue(dataPath.getId(), dataPath.getName(), dataValue != null
                        ? dataValue.toString()
                        : ""));
               }
            }
         }
      }
      return dataPathContacts;
   }

   /**
    * @param addressCategory
    * @return entered mail address
    */
   private List<DataPathValue> getEnteredMailAddresses(String addressCategory)
   {
      List<DataPathValue> enteredValues = new ArrayList<DataPathValue>();
      FacesContext context = FacesContext.getCurrentInstance();
      String[] recipients = ((String) context.getExternalContext().getRequestParameterMap().get(
            requestParamMap.get(addressCategory))).split(";");
      for (int i = 0; i < recipients.length; i++)
      {
         if (EMailAddressValidator.validateEmailAddress(recipients[i]))
            enteredValues.add(new DataPathValue(recipients[i], recipients[i], recipients[i]));
      }
      return enteredValues;
   }

   /**
    * store uploaded file details
    */
   private void processUploadedFile()
   {
      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
      addAttachments(new Attachment(fileUploadDialog.getFileInfo()));
   }

   /**
    * @param attachment
    * @return true if attachment added to the list
    */
   private boolean addAttachments(Attachment attachment)
   {
      if (!attachments.contains(attachment))
      {
         attachments.add(attachment);
         return true;
      }
      return false;
   }

   /**
    * Save mail as a document
    * 
    * @throws DocumentManagementServiceException
    * @throws IOException
    */
   private void saveEmailDocument() throws DocumentManagementServiceException, IOException
   {
      Folder processAttachmentsFolder = RepositoryUtility.getProcessAttachmentsFolder(processInstance);
      StringBuilder attachmentInfo = new StringBuilder("");
      JCRVersionTracker vt = null;

      if (!CollectionUtils.isEmpty(attachments))
      {
         for (Attachment attachment : attachments)
         {
            attachmentInfo.append(attachment.getName());
            if (attachment.isContainsDocument())
            {
               vt = new JCRVersionTracker(attachment.getDocument());
               attachmentInfo.append("(").append(vt.getCurrentVersionNo()).append(")");
            }
            else
            {
               createDocumentFromAttachment(attachment, processAttachmentsFolder);

            }
            attachmentInfo.append(";");
         }
      }
      createDocumentForMail(processAttachmentsFolder, attachmentInfo.toString());
   }

   /**
    * Create document for sent mail information
    * 
    * @param processAttachmentsFolder
    * @param attachmentInfo
    */
   private void createDocumentForMail(Folder processAttachmentsFolder, String attachmentInfo)
   {
      String docName = RepositoryUtility.createDocumentName(processAttachmentsFolder, DocumentMgmtUtility
            .stripOffSpecialCharacters(mailSubject), 0);
      DocumentInfo docInfo = DmsUtils.createDocumentInfo(docName);
      docInfo.setContentType(MimeTypesHelper.HTML.getType());
      Document mailDocument = DocumentMgmtUtility.getDocumentManagementService().createDocument(
            processAttachmentsFolder.getId(), docInfo, editor.getContent().getBytes(), null);
      populateMetaData(mailDocument, attachmentInfo);
      mailDocument = DocumentMgmtUtility.getDocumentManagementService().updateDocument(mailDocument, false, "", false);
      DMSHelper.addAndSaveProcessAttachment(processInstance, mailDocument);
   }

   /**
    * create document for attached file
    * 
    * @param attachment
    * @param processAttachmentsFolder
    * @throws DocumentManagementServiceException
    * @throws IOException
    */
   private void createDocumentFromAttachment(Attachment attachment, Folder processAttachmentsFolder)
         throws DocumentManagementServiceException, IOException
   {
      FileInfo fileInfo = attachment.getFileInfo();
      String docName = RepositoryUtility.createDocumentName(processAttachmentsFolder, fileInfo.getFileName(), 0);

      // create document
      Document document = DocumentMgmtUtility.createDocument(processAttachmentsFolder.getId(), docName,
            DocumentMgmtUtility.getFileSystemDocumentContent(fileInfo.getPhysicalPath()), null,
            fileInfo.getContentType(), null, null, null, null);

      // update process attachment
      DMSHelper.addAndSaveProcessAttachment(processInstance, document);
   }

   /**
    * populate metadata for email document
    * 
    * @param document
    * @param attachmentInfo
    */
   private void populateMetaData(Document document, String attachmentInfo)
   {
      // Set base metadata
      document.setOwner(ContextPortalServices.getUser().getAccount());
      // set correspondence metadata
      Map properties = document.getProperties();
      if (CollectionUtils.isEmpty(properties))
      {
         properties = new HashMap<String, Object>();
         document.setProperties(properties);
      }
      Map correspondenceMD = (Map) properties.get(CommonProperties.FAX_EMAIL_MESSAGE_INFO);
      if (CollectionUtils.isEmpty(correspondenceMD))
      {
         properties.put(CommonProperties.FAX_EMAIL_MESSAGE_INFO, new HashMap<String, Object>());
      }
      CorrespondenceMetaData cMetaData = new CorrespondenceMetaData(properties);
      cMetaData.setRecipients(getToMailAddress());
      cMetaData.setBlindCarbonCopyRecipients(bccMailAddress);
      cMetaData.setCarbonCopyRecipients(ccMailAddress);
      cMetaData.setFaxNumber(getFaxMailAddress());
      cMetaData.setSendDate(Calendar.getInstance().getTime());
      cMetaData.setAttachments(attachmentInfo);
      cMetaData.setSender(mailSender);
      cMetaData.setSubject(getMailSubject());
      // by default fax and email checkbox should be unchecked.
      cMetaData.setFaxEnabled(false);
      cMetaData.setMailEnabled(false);
   }

   /**
    * Validates email address and returns validation error messages
    * 
    * @return
    */
   private List<String> validateMailAddresses(Map<RecipientType, String[]> recipientDetails)
   {
      ArrayList<String> errorMsg = new ArrayList<String>();

      // Validate From email address
      if (StringUtils.isEmpty(mailSender))
      {
         errorMsg.add(getMessages().get("fromAddressMessage"));
      }
      else if (!EMailAddressValidator.validateEmailAddress(mailSender))
      {
         errorMsg.add(getMessages().get("fromMessage"));
      }

      if (StringUtils.isEmpty(mailSubject))
      {
         errorMsg.add(getMessages().get("subjectMessage"));
      }

      if (StringUtils.isEmpty(toMailAddress) && StringUtils.isEmpty(ccMailAddress)
            && StringUtils.isEmpty(bccMailAddress))
      {
         errorMsg.add(getMessages().get("recipientAddressMessage"));
      }
      else
      {
         if (StringUtils.isNotEmpty(toMailAddress))
         {
            if (!CollectionUtils.isEmpty(EMailAddressValidator.validateEmailAddresses(toMailAddress)))
            {
               errorMsg.add(getMessages().get("toMessage"));
            }
            else
            {
               recipientDetails.put(RecipientType.TO, toMailAddress.trim().split(";"));
            }
         }
         if (StringUtils.isNotEmpty(ccMailAddress))
         {
            if (!CollectionUtils.isEmpty(EMailAddressValidator.validateEmailAddresses(ccMailAddress)))
            {
               errorMsg.add(getMessages().get("ccMessage"));
            }
            else
            {
               recipientDetails.put(RecipientType.CC, ccMailAddress.trim().split(";"));
            }
         }
         if (StringUtils.isNotEmpty(bccMailAddress))
         {
            if (!CollectionUtils.isEmpty(EMailAddressValidator.validateEmailAddresses(bccMailAddress)))
            {
               errorMsg.add(getMessages().get("bccMessage"));
            }
            else
            {
               recipientDetails.put(RecipientType.BCC, bccMailAddress.trim().split(";"));
            }
         }
      }
      return errorMsg;
   }

   /**
    * @param printerDialogPopup
    * @return
    */
   private PdfResource getPdfResource(PrinterDialogPopup printerDialogPopup)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ExternalContext ec = fc.getExternalContext();
      String name = "" + System.currentTimeMillis() + ".pdf";
      PdfResource resource = new PdfResource(ec, name, editor.getContent(), getAttachments(),
            printerDialogPopup.getPrintingPreferences(), pdfConverterHelper);
      return resource;
   }

   /**
    * @return
    */
   public Effect getDropBoxEffect()
   {
      return dropBoxEffect;
   }

   // Default setters-getters
   public List<Attachment> getAttachments()
   {
      return attachments;
   }

   public void setMailSender(String mailSender)
   {
      this.mailSender = mailSender;
   }

   public void setToMailAddress(String toMailAddress)
   {
      this.toMailAddress = toMailAddress;
   }

   public void setCcMailAddress(String ccMailAddress)
   {
      this.ccMailAddress = ccMailAddress;
   }

   public void setBccMailAddress(String bccMailAddress)
   {
      this.bccMailAddress = bccMailAddress;
   }

   public void setFaxMailAddress(String faxMailAddress)
   {
      this.faxMailAddress = faxMailAddress;
   }

   public void setMailSubject(String mailSubject)
   {
      this.mailSubject = mailSubject;
   }

   public String getMailSubject()
   {
      return this.mailSubject;
   }

   public IDocumentEditor getEditor()
   {
      return editor;
   }

   public void setEditor(IDocumentEditor editor)
   {
      this.editor = editor;
   }

   public boolean isShowCc()
   {
      return showCc;
   }

   public boolean isShowBcc()
   {
      return showBcc;
   }

   public boolean isShowFax()
   {
      return showFax;
   }

   public InputRichText getEmailRichText()
   {
      return emailRichText;
   }

   public void setEmailRichText(InputRichText emailRichText)
   {
      this.emailRichText = emailRichText;
   }

   public String getMailSender()
   {
      return mailSender;
   }

   public String getToMailAddress()
   {
      return toMailAddress;
   }

   public String getCcMailAddress()
   {
      return ccMailAddress;
   }

   public String getBccMailAddress()
   {
      return bccMailAddress;
   }

   public String getFaxMailAddress()
   {
      return faxMailAddress;
   }

   public void setMailService(MailService mailService)
   {
      this.mailService = mailService;
   }

   public CorrespondenceAttachments getCorrespondenceAttachments()
   {
      return correspondenceAttachments;
   }

   public boolean isPrintAvailable()
   {
      return printAvailable;
   }
}