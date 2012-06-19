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
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.views.PortalConfiguration;
import org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;


/**
 * @author Shrikant.Gangal
 *
 */
public class ImageViewerConfigurationBean implements PortalConfigurationListener, UserPreferencesEntries, ConfirmationDialogHandler
{
   private static final String BEAN_NAME = "imageViewerConfigurationBean";
   
   private static final String DEFAULT_NOTE_FONT_SIZE = "24";

   private List<SelectItem> displayZoomLevelOptions;

   private List<SelectItem> noteFontSizeList;

   private List<SelectItem> highlightColourList;
   
   private List<SelectItem> docPriorVersionActionList;
   
   private List<SelectItem> highlightOpacityList;

   private boolean showSidePanel = true;

   private boolean invertImage = false;

   private boolean showAnnotations = true;
   
   private boolean pageDeletionEnabled = true;

   private boolean highlightDataFieldsEnabled = true;

   private boolean datanameInTargetIncluded = true;

   private boolean magnifyFields = true;
   
   private boolean boldSelected = false;
   
   private boolean italicSelected = false;
   
   private boolean underlineSelected = false;

   private String selectedDisplayZoomLevel;
   
   private Map<String, String> zoomLevelMap;
   
   private String selectedNoteFontSize;

   private MessagesViewsCommonBean messageBean;
   
   private String stickyNoteColour;
   
   private String highlighterColour;
   
   private String docPriorVersionAction;

   private String dataFieldHighlightColour;
   
   private String dataFieldHighlightOpacity;
   
   private ConfirmationDialog imageViewerConfirmationDialog;
   
   /**
    * 
    */
   public ImageViewerConfigurationBean()
   {
      PortalConfiguration.getInstance().addListener(this);
      initialize();
   }

   /**
    * 
    */
   public void initialize()
   {
      messageBean = MessagesViewsCommonBean.getInstance();
      intializeDisplayZoomLevelOptions();
      initializeHighlightColourList();
      initializeDocPriorVerionAcionList();
      initializeDataHighlightOpacityList();
      initializeNoteFontSizeList();
      setUsersPreferences();
   }

   /**
    * @return
    */
   public static ImageViewerConfigurationBean getCurrent()
   {
      return (ImageViewerConfigurationBean) org.eclipse.stardust.ui.web.common.util.FacesUtils
            .getBeanFromContext(BEAN_NAME);
   }

   /**
    * @return
    */
   public List<SelectItem> getDisplayZoomLevelOptions()
   {
      return displayZoomLevelOptions;
   }

   /**
    * @return
    */
   public boolean isShowSidePanel()
   {
      return showSidePanel;
   }

   /**
    * @param showSidePanel
    */
   public void setShowSidePanel(boolean showSidePanel)
   {
      this.showSidePanel = showSidePanel;
   }

   /**
    * @return
    */
   public boolean isInvertImage()
   {
      return invertImage;
   }

   /**
    * @param invertImage
    */
   public void setInvertImage(boolean invertImage)
   {
      this.invertImage = invertImage;
   }

   /**
    * @return
    */
   public boolean isShowAnnotations()
   {
      return showAnnotations;
   }

   /**
    * @param showAnnotations
    */
   public void setShowAnnotations(boolean showAnnotations)
   {
      this.showAnnotations = showAnnotations;
   }

   /**
    * @return
    */
   public boolean isPageDeletionEnabled()
   {
      return pageDeletionEnabled;
   }

   /**
    * @param pageDeletionEnabled
    */
   public void setPageDeletionEnabled(boolean pageDeletionEnabled)
   {
      this.pageDeletionEnabled = pageDeletionEnabled;
   }

   /**
    * @return
    */
   public boolean isHighlightDataFieldsEnabled()
   {
      return highlightDataFieldsEnabled;
   }

   /**
    * @param highlightDataFieldsEnabled
    */
   public void setHighlightDataFieldsEnabled(boolean highlightDataFieldsEnabled)
   {
      this.highlightDataFieldsEnabled = highlightDataFieldsEnabled;
   }

   /**
    * @return
    */
   public boolean isDatanameInTargetIncluded()
   {
      return datanameInTargetIncluded;
   }

   /**
    * @param datanameInTargetIncluded
    */
   public void setDatanameInTargetIncluded(boolean datanameInTargetIncluded)
   {
      this.datanameInTargetIncluded = datanameInTargetIncluded;
   }

   /**
    * @return
    */
   public boolean isMagnifyFields()
   {
      return magnifyFields;
   }

   /**
    * @param magnifyFields
    */
   public void setMagnifyFields(boolean magnifyFields)
   {
      this.magnifyFields = magnifyFields;
   }
   
   /**
    * 
    */
   public void save()
   {  
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SHOW_SIDE_PANEL, String.valueOf(showSidePanel));
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_INVERT_IMAGE, String.valueOf(invertImage));
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SHOW_ANNOTATIONS, String.valueOf(showAnnotations));
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_ENABLE_PAGE_DELETE, String.valueOf(pageDeletionEnabled));
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_HIGHLIGHT_DATA_FIELDS_ENABLED, String.valueOf(highlightDataFieldsEnabled));
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_DATANAME_IN_TARGET_INCLUDED, String.valueOf(datanameInTargetIncluded));
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_MAGNIFY_FIELDS, String.valueOf(magnifyFields));
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_BOLD_SELECTED, String.valueOf(boldSelected));
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_ITALIC_SELECTED, String.valueOf(italicSelected));
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_UNDERLINE_SELECTED, String.valueOf(underlineSelected));
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_NOTE_FONT_SIZE, selectedNoteFontSize);
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_STICKY_NOTE_COLOUR, stickyNoteColour);
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_HIGHLIGHTER_COLOUR, highlighterColour);
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DATA_FIELD_HIGHLIGHTER_COLOUR, dataFieldHighlightColour);
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DOC_PRIOR_VERSION_ACTION, docPriorVersionAction);
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DATA_FIELD_HIGHLIGHTER_OPACITY, dataFieldHighlightOpacity);
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DISPLAY_ZOOM_LIVEL, selectedDisplayZoomLevel);
      userPrefsHelper.setString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_STAMP, ImageViewerStampsBean.getCurrent().getSelectedStampId());
      MessageDialog.addInfoMessage(messageBean.getString("views.imageViewerConfig.save.successMessage"));
   }
   
   /**
    * 
    */
   public void reset()
   {
      resetUsersPreferences();
      initialize();
      FacesUtils.clearFacesTreeValues();
      MessageDialog.addInfoMessage(MessagesViewsCommonBean.getInstance().getString("views.common.config.reset"));
   }
   
   /**
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      imageViewerConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
      imageViewerConfirmationDialog.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      imageViewerConfirmationDialog.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            MessagesViewsCommonBean.getInstance().getString("views.imageViewerConfig.labelTitle")));
      imageViewerConfirmationDialog.openPopup();
   }

   public boolean accept()
   {
      reset();
      imageViewerConfirmationDialog = null;
      return true;
   }

   public boolean cancel()
   {
      imageViewerConfirmationDialog = null;
      return true;
   }
   
   /**
    * @param event
    */
   public void openStampsDialog(ActionEvent event)
   {
      ImageViewerStampsBean.getCurrent().openPopup();
   }
   
   /**
    * @param event
    */
   public void closeStampsDialog(ActionEvent event)
   {
      ImageViewerStampsBean.getCurrent().closePopup();
   }
   
   /**
    * @param event
    */
   public void selectDefaultStamp(ActionEvent event)
   {
      
   }
   
   /**
    * @param event
    */
   public void toggleBoldSelection(ActionEvent event)
   {
      boldSelected = !boldSelected;
   }
   
   /**
    * @param event
    */
   public void toggleItalicSelection(ActionEvent event)
   {
      italicSelected = !italicSelected;
   }
   
   /**
    * @param event
    */
   public void toggleUnderlineSelection(ActionEvent event)
   {
      underlineSelected = !underlineSelected;
   }

   /**
    * @return
    */
   public String getSelectedDisplayZoomLevel()
   {
      return selectedDisplayZoomLevel;
   }
   
   /**
    * @return
    */
   public String getSelectedDisplayZoomLevelEnum()
   {
      return zoomLevelMap.get(selectedDisplayZoomLevel);
   }

   public void setSelectedDisplayZoomLevel(String selectedDisplayZoomLevel)
   {
      this.selectedDisplayZoomLevel = selectedDisplayZoomLevel;
   }

   public String getHighlighterColour()
   {
      return highlighterColour;
   }
   
   public void changeHighlighterColour(ActionEvent event)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      highlighterColour = (String) context.getExternalContext().getRequestParameterMap().get("colour");
   }

   public String getDocPriorVersionAction()
   {
      return docPriorVersionAction;
   }

   public void setDocPriorVersionAction(String docPriorVersionAction)
   {
      this.docPriorVersionAction = docPriorVersionAction;
   }

   public String getDataFieldHighlightColour()
   {
      return dataFieldHighlightColour;
   }

   public void changeDataFieldHighlightColour(ActionEvent event)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      dataFieldHighlightColour = (String) context.getExternalContext().getRequestParameterMap().get("colour");
   }

   public String getSelectedStamp()
   {
      return ImageViewerStampsBean.getCurrent().getSelectedStampId();
   }

   public List<SelectItem> getHighlightColourList()
   {
      return highlightColourList;
   }

   public void setHighlightColourList(List<SelectItem> highlightColourList)
   {
      this.highlightColourList = highlightColourList;
   }

   public List<SelectItem> getDocPriorVersionActionList()
   {
      return docPriorVersionActionList;
   }

   public void setDocPriorVersionActionList(List<SelectItem> docPriorVersionActionList)
   {
      this.docPriorVersionActionList = docPriorVersionActionList;
   }

   public List<SelectItem> getHighlightOpacityList()
   {
      return highlightOpacityList;
   }

   public void setHighlightOpacityList(List<SelectItem> highlightOpacityList)
   {
      this.highlightOpacityList = highlightOpacityList;
   }

   public List<SelectItem> getNoteFontSizeList()
   {
      return noteFontSizeList;
   }

   public String getSelectedNoteFontSize()
   {
      return selectedNoteFontSize;
   }
   
   public void setSelectedNoteFontSize(String selectedNoteFontSize)
   {
      this.selectedNoteFontSize = selectedNoteFontSize;
   }
   
   public boolean isBoldSelected()
   {
      return boldSelected;
   }

   public void setBoldSelected(boolean boldSelected)
   {
      this.boldSelected = boldSelected;
   }

   public boolean isItalicSelected()
   {
      return italicSelected;
   }

   public void setItalicSelected(boolean italicSelected)
   {
      this.italicSelected = italicSelected;
   }

   public boolean isUnderlineSelected()
   {
      return underlineSelected;
   }

   public void setUnderlineSelected(boolean underlineSelected)
   {
      this.underlineSelected = underlineSelected;
   }

   public String getStickyNoteColour()
   {
      return stickyNoteColour;
   }

   public void changeStickyNoteColour(ActionEvent event)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      stickyNoteColour = (String) context.getExternalContext().getRequestParameterMap().get("colour");
   }

   public String getDataFieldHighlightOpacity()
   {
      return dataFieldHighlightOpacity;
   }

   public void setDataFieldHighlightOpacity(String dataFieldHighlightOpacity)
   {
      this.dataFieldHighlightOpacity = dataFieldHighlightOpacity;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener#preferencesScopeChanging(org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope)
    */
   public boolean preferencesScopeChanging(PreferenceScope pScope)
   {
      return true;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener#preferencesScopeChanged(org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope)
    */
   public void preferencesScopeChanged(PreferenceScope pScope)
   {
      initialize();
   }

   public ConfirmationDialog getImageViewerConfirmationDialog()
   {
      return imageViewerConfirmationDialog;
   }

   /**
    * 
    */
   private void intializeDisplayZoomLevelOptions()
   {
      zoomLevelMap = new HashMap<String, String>();
      zoomLevelMap.put(messageBean.getString("views.imageViewerConfig.displayOptions.actual"), "0");
      zoomLevelMap.put(messageBean.getString("views.imageViewerConfig.displayOptions.fitToWindow"), "1");
      zoomLevelMap.put(messageBean.getString("views.imageViewerConfig.displayOptions.fitToHeight"), "2");
      zoomLevelMap.put(messageBean.getString("views.imageViewerConfig.displayOptions.fitToWidth"), "3");
      
      displayZoomLevelOptions = new ArrayList<SelectItem>();
      displayZoomLevelOptions.add(new SelectItem(messageBean.getString("views.imageViewerConfig.displayOptions.actual")));
      displayZoomLevelOptions.add(new SelectItem(messageBean.getString("views.imageViewerConfig.displayOptions.fitToWindow")));
      displayZoomLevelOptions.add(new SelectItem(messageBean.getString("views.imageViewerConfig.displayOptions.fitToHeight")));
      displayZoomLevelOptions.add(new SelectItem(messageBean.getString("views.imageViewerConfig.displayOptions.fitToWidth")));
   }

   /**
    * 
    */
   private void initializeHighlightColourList()
   {
      highlightColourList = new ArrayList<SelectItem>();
      highlightColourList.add(new SelectItem("#FFFF00"));
      highlightColourList.add(new SelectItem("#00FF00"));
      highlightColourList.add(new SelectItem("#00FFFF"));
      highlightColourList.add(new SelectItem("#FF0000"));
   }
   
   /**
    * 
    */
   private void initializeDocPriorVerionAcionList(){
      docPriorVersionActionList=new ArrayList<SelectItem>();
      docPriorVersionActionList.add(new SelectItem("0",messageBean.getString("views.imageViewerConfig.extractPages.DocPriorVerionAction.Retain")));
      docPriorVersionActionList.add(new SelectItem("1",messageBean.getString("views.imageViewerConfig.extractPages.DocPriorVerionAction.Delete")));
   }
   /**
    * 
    */
   private void initializeDataHighlightOpacityList()
   {
      highlightOpacityList = new ArrayList<SelectItem>();
      highlightOpacityList.add(new SelectItem("0.2"));
      highlightOpacityList.add(new SelectItem("0.4"));
      highlightOpacityList.add(new SelectItem("0.5"));
      highlightOpacityList.add(new SelectItem("0.6"));
      highlightOpacityList.add(new SelectItem("0.8"));
   }
   
   /**
    * 
    */
   private void initializeNoteFontSizeList()
   {
      noteFontSizeList = new ArrayList<SelectItem>();
      noteFontSizeList.add(new SelectItem("8"));
      noteFontSizeList.add(new SelectItem("9"));
      noteFontSizeList.add(new SelectItem("10"));
      noteFontSizeList.add(new SelectItem("11"));
      noteFontSizeList.add(new SelectItem("12"));
      noteFontSizeList.add(new SelectItem("14"));
      noteFontSizeList.add(new SelectItem("16"));
      noteFontSizeList.add(new SelectItem("20"));
      noteFontSizeList.add(new SelectItem("24"));
      noteFontSizeList.add(new SelectItem("28"));
      noteFontSizeList.add(new SelectItem("32"));
      noteFontSizeList.add(new SelectItem("40"));
      noteFontSizeList.add(new SelectItem("60"));
      noteFontSizeList.add(new SelectItem("72"));
   }
   
   /**
    * 
    */
   private void setUsersPreferences()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();
      showSidePanel = userPrefsHelper.getBoolean(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SHOW_SIDE_PANEL, true);
      invertImage = userPrefsHelper.getBoolean(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_INVERT_IMAGE, false);
      showAnnotations = userPrefsHelper.getBoolean(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SHOW_ANNOTATIONS, true);
      pageDeletionEnabled=userPrefsHelper.getBoolean(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_ENABLE_PAGE_DELETE, true);
      highlightDataFieldsEnabled = userPrefsHelper.getBoolean(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_HIGHLIGHT_DATA_FIELDS_ENABLED, true);
      datanameInTargetIncluded = userPrefsHelper.getBoolean(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_DATANAME_IN_TARGET_INCLUDED, true);
      magnifyFields = userPrefsHelper.getBoolean(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_MAGNIFY_FIELDS, false);
      boldSelected = userPrefsHelper.getBoolean(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_BOLD_SELECTED, false);
      italicSelected = userPrefsHelper.getBoolean(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_ITALIC_SELECTED, false);
      underlineSelected = userPrefsHelper.getBoolean(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_UNDERLINE_SELECTED, false);
      selectedNoteFontSize = userPrefsHelper.getSingleString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_NOTE_FONT_SIZE, DEFAULT_NOTE_FONT_SIZE);
      stickyNoteColour = userPrefsHelper.getSingleString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_STICKY_NOTE_COLOUR, "yellow");
      highlighterColour = userPrefsHelper.getSingleString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_HIGHLIGHTER_COLOUR, "#FF0000");
      dataFieldHighlightColour = userPrefsHelper.getSingleString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DATA_FIELD_HIGHLIGHTER_COLOUR, "#FF0000");
      docPriorVersionAction = userPrefsHelper.getSingleString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DOC_PRIOR_VERSION_ACTION, "RETAIN_PRIOR_DOCUMENT");
      dataFieldHighlightOpacity = userPrefsHelper.getSingleString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DATA_FIELD_HIGHLIGHTER_OPACITY, "0.5");
      selectedDisplayZoomLevel = userPrefsHelper.getSingleString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DISPLAY_ZOOM_LIVEL, "FIT_TO_WINDOW");
      ImageViewerStampsBean.getCurrent().setSelectedStampId(userPrefsHelper.getSingleString(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_STAMP, ""));
   }
   
   /**
    * 
    */
   private void resetUsersPreferences()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SHOW_SIDE_PANEL);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_INVERT_IMAGE);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SHOW_ANNOTATIONS);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_ENABLE_PAGE_DELETE);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_HIGHLIGHT_DATA_FIELDS_ENABLED);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_DATANAME_IN_TARGET_INCLUDED);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_MAGNIFY_FIELDS);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_BOLD_SELECTED);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_ITALIC_SELECTED);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_UNDERLINE_SELECTED);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_NOTE_FONT_SIZE);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_STICKY_NOTE_COLOUR);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_HIGHLIGHTER_COLOUR);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DATA_FIELD_HIGHLIGHTER_COLOUR);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DATA_FIELD_HIGHLIGHTER_OPACITY);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DISPLAY_ZOOM_LIVEL);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_DOC_PRIOR_VERSION_ACTION);
      userPrefsHelper.resetValue(V_IMAGE_VIEWER_CONFIG, F_IMAGE_VIEWER_SELECTED_STAMP);
   }

   /**
    * @return
    */
   private static UserPreferencesHelper getUserPrefenceHelper()
   {
      return UserPreferencesHelper.getInstance(M_VIEWS_COMMON, PortalConfiguration.getInstance()
            .getPrefScopesHelper().getSelectedPreferenceScope());
   }
  
}
