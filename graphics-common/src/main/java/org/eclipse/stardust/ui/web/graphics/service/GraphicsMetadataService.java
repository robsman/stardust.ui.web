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
package org.eclipse.stardust.ui.web.graphics.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.Highlight;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.Note;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PageAnnotation;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PageOrientation;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotations;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotationsImpl;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.Stamp;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.TextStyle;
import org.eclipse.stardust.ui.web.graphics.service.annotation.types.AnnotationAttributes;
import org.eclipse.stardust.ui.web.graphics.service.annotation.types.AnnotationDimensions;
import org.eclipse.stardust.ui.web.graphics.service.annotation.types.AnnotationProperties;
import org.eclipse.stardust.ui.web.graphics.service.annotation.types.GraphicAnnotation;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.TIFFDocumentWrapper;
import org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.TIFFDocumentHolder;
import org.springframework.stereotype.Component;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


@Component
public class GraphicsMetadataService
{
   /**
    * 
    */
   private Map<String, List<GraphicAnnotation>> tempStorage;

   private Map<String, TIFFDocumentWrapper> docAnnotationsMap;
   
   private HttpServletRequest httpRequest;
   
   private static final String STYLE_NORMAL = "normal";
   private static final String STYLE_BOLD = "bold";
   private static final String STYLE_ITALIC = "italic";
   private static final String STYLE_UNDERLINE = "underline";

   // TODO these will need to be internationalized in conjunction with the
   // internationalization
   // changes for JS related stuff.
   private static final String ACTIION_RECREATE = "Recreate";
   private static final String USR_ACTION_CREATED = "Created";
   private static final String USR_ACTION_MODIFIED = "Modified";
   private static final String USR_ACTION_CREATE = "Create";
   private static final String USR_ACTION_MODIFY = "Modify";
   private static final String ANNOTATION_TYPE_NOTE = "StickyNote";
   private static final String ANNOTATION_TYPE_STAMP = "stamp";
   private static final String ANNOTATION_TYPE_HIGHLIGHTER = "highlighter";
   private static final String ROTATION_ID = "rotationId";
   private static final String ROTATE_ACTION = "rotate";
   private static final String DATE_FORMAT = "MM/dd/yy hh:mm a";
   private static Map<String, String> LANG_POST_FIX_MAP;

   public GraphicsMetadataService()
   {

   }

   public void init(HttpServletRequest httpRequest)
   {
      this.httpRequest = httpRequest;
      tempStorage = new HashMap<String, List<GraphicAnnotation>>();
      SessionSharedObjectsMap sessionSharedMap = (SessionSharedObjectsMap) httpRequest.getSession().getAttribute(
            "sessionSharedObjectsMap");
      docAnnotationsMap = (Map<String, TIFFDocumentWrapper>) sessionSharedMap.getObject("DOC_ID_VS_DOC_MAP");
      initLocaleMap();
   }

   /**
    * @param documentId
    * @param metaData
    */
   public void createAnnotation(String documentId, int pageNo, String metaData, HttpServletRequest httpRequest)
   {
      String key = documentId + ":" + pageNo;
      List<GraphicAnnotation> annotations = getAnnotationsFor(documentId, pageNo, httpRequest);
      if (null == annotations)
      {
         annotations = new ArrayList<GraphicAnnotation>();
      }
      Gson gson = getGSON();
      GraphicAnnotation annot = gson.fromJson(metaData, GraphicAnnotation.class);
      annotations.add(annot);
      tempStorage.put(key, annotations);
      if (annot.getType().equals(ANNOTATION_TYPE_HIGHLIGHTER))
      {
         ((PrintDocumentAnnotations) docAnnotationsMap.get(documentId).getDocInfo().getAnnotations())
               .addHighlight(getHighlightFrom(annot, pageNo));
      }
      else if (annot.getType().equals(ANNOTATION_TYPE_NOTE))
      {
         Note note = getStickyNoteFrom(annot, pageNo);
         ((PrintDocumentAnnotations) docAnnotationsMap.get(documentId).getDocInfo().getAnnotations()).addNote(note);
      }
      else if (annot.getType().equals(ANNOTATION_TYPE_STAMP))
      {
         Stamp stamp = getStampFrom(annot, pageNo);
         ((PrintDocumentAnnotations) docAnnotationsMap.get(documentId).getDocInfo().getAnnotations()).addStamp(stamp);
      }
      docAnnotationsMap.get(documentId).setAnnotationChanged(true);
   }

   /**
    * @param documentId
    * @return
    */
   public String retrieve(String documentId, int pageNo, HttpServletRequest httpRequest)
   {
      List<GraphicAnnotation> annotations = getAnnotationsFor(documentId, pageNo, httpRequest);

      if (null != annotations)
      {
         Gson gson = getGSON();

         return gson.toJson(annotations, new TypeToken<List<GraphicAnnotation>>()
         {
         }.getType());
      }
      else
      {
         return "";
      }
   }

   /**
    * @param documentId
    * @param postedData
    */
   public void updateAnnotation(String documentId, int pageNo, String postedData, HttpServletRequest httpRequest)
   {
      String key = documentId + ":" + pageNo;
      Gson gson = getGSON();
      GraphicAnnotation annotation = gson.fromJson(postedData, GraphicAnnotation.class);
      List<GraphicAnnotation> annotations = getAnnotationsFor(documentId, pageNo, httpRequest);
      ListIterator<GraphicAnnotation> listIter = annotations.listIterator();
      while (listIter.hasNext())
      {
         GraphicAnnotation annot = listIter.next();
         if (annot.getId().equals(annotation.getId()))
         {
            listIter.set(annotation);
         }
      }

      if (annotation.getType().equals(ANNOTATION_TYPE_HIGHLIGHTER))
      {
         PrintDocumentAnnotations printAnnots = (PrintDocumentAnnotations) docAnnotationsMap.get(documentId)
               .getDocInfo().getAnnotations();
         printAnnots.removeHighlight(annotation.getId());
         printAnnots.addHighlight(getHighlightFrom(annotation, pageNo));
      }
      if (annotation.getType().equals(ANNOTATION_TYPE_NOTE))
      {
         PrintDocumentAnnotations printAnnots = (PrintDocumentAnnotations) docAnnotationsMap.get(documentId)
               .getDocInfo().getAnnotations();
         printAnnots.removeNote(annotation.getId());
         printAnnots.addNote(getStickyNoteFrom(annotation, pageNo));
      }
      if (annotation.getType().equals(ANNOTATION_TYPE_STAMP))
      {
         PrintDocumentAnnotations printAnnots = (PrintDocumentAnnotations) docAnnotationsMap.get(documentId)
               .getDocInfo().getAnnotations();
         printAnnots.removeStamp(annotation.getId());
         printAnnots.addStamp(getStampFrom(annotation, pageNo));
      }
      docAnnotationsMap.get(documentId).setAnnotationChanged(true);
   }

   /**
    * @param documentId
    * @param postedData
    */
   public void updateRotationFactor(String documentId, int pageNo, String postedData, HttpServletRequest httpRequest)
   {
      String key = documentId + ":" + pageNo;
      Gson gson = getGSON();
      GraphicAnnotation rotationFactor = gson.fromJson(postedData, GraphicAnnotation.class);
      List<GraphicAnnotation> annotations = getAnnotationsFor(documentId, pageNo, httpRequest);
      if (null == annotations)
      {
         annotations = new ArrayList<GraphicAnnotation>();
         tempStorage.put(key, annotations);
      }
      else
      {
         ListIterator<GraphicAnnotation> listIter = annotations.listIterator();
         while (listIter.hasNext())
         {
            GraphicAnnotation annotation = listIter.next();
            if (ROTATION_ID.equals(annotation.getId()))
            {
               listIter.remove();
            }
         }
      }
      annotations.add(rotationFactor);

      // ////////////////
      PrintDocumentAnnotations printAnnots = (PrintDocumentAnnotations) docAnnotationsMap.get(documentId).getDocInfo()
            .getAnnotations();
      printAnnots.removePageOrientation(pageNo);
      printAnnots.addPageOrientation(getPageOrientationFrom(rotationFactor, pageNo));
      docAnnotationsMap.get(documentId).setRotationChanged(true);
   }

   /**
    * @param documentId
    * @param postedData
    */
   public void deleteAnnotation(String documentId, int pageNo, String postedData, HttpServletRequest httpRequest)
   {
      Gson gson = getGSON();
      GraphicAnnotation annotation = gson.fromJson(postedData, GraphicAnnotation.class);
      List<GraphicAnnotation> annotations = getAnnotationsFor(documentId, pageNo, httpRequest);
      ListIterator<GraphicAnnotation> listIter = annotations.listIterator();
      while (listIter.hasNext())
      {
         GraphicAnnotation annot = listIter.next();
         if (annot.getId().equals(annotation.getId()))
         {
            listIter.remove();
         }
      }

      if (annotation.getType().equals(ANNOTATION_TYPE_HIGHLIGHTER))
      {
         ((PrintDocumentAnnotations) docAnnotationsMap.get(documentId).getDocInfo().getAnnotations())
               .removeHighlight(annotation.getId());
      }
      if (annotation.getType().equals(ANNOTATION_TYPE_NOTE))
      {
         ((PrintDocumentAnnotations) docAnnotationsMap.get(documentId).getDocInfo().getAnnotations()).removeNote(annotation
               .getId());
      }
      if (annotation.getType().equals(ANNOTATION_TYPE_STAMP))
      {
         ((PrintDocumentAnnotations) docAnnotationsMap.get(documentId).getDocInfo().getAnnotations()).removeStamp(annotation
               .getId());
      }
      docAnnotationsMap.get(documentId).setAnnotationChanged(true);
   }

   /**
    * 
    */
   public void cleanCache(String documentId, String metaData, HttpServletRequest httpRequest)
   {
      Set<String> keys = tempStorage.keySet();
      for (String key : keys)
      {
         if (key.startsWith(documentId))
         {
            tempStorage.remove(key);
         }
      }
   }
   
   /**
    * Updates the state of a document, that can be shared between popped out and popped in windows.
    * shared sate like the current page, current zoom level etc.
    * 
    * @param docId
    * @param stateJson
    */
   public void updateViewerState(String docId, String stateJson)
   {
      SessionSharedObjectsMap sessionSharedMap = (SessionSharedObjectsMap) httpRequest.getSession().getAttribute(
      "sessionSharedObjectsMap");
      synchronized(sessionSharedMap)
      {
         Map<String, String> viewerStateShareMap = (Map<String, String>) sessionSharedMap.getObject("VIEWER_STATE_SHARE_MAP");
         viewerStateShareMap.put(docId, stateJson);
      }
   }
   
   /**
    * @param docId
    * @return
    */
   public String getViewerState(String docId)
   {   
      String stateJson = null;
      SessionSharedObjectsMap sessionSharedMap = (SessionSharedObjectsMap) httpRequest.getSession().getAttribute(
      "sessionSharedObjectsMap");
      synchronized(sessionSharedMap)
      {
         Map<String, String> viewerStateShareMap = (Map<String, String>) sessionSharedMap.getObject("VIEWER_STATE_SHARE_MAP");
         stateJson = viewerStateShareMap.get(docId);
         if (null == stateJson)
         {
            stateJson = "{}";
         }
      }
      return stateJson;
   }   

   /**
    * @return
    */
   private Gson getGSON()
   {
      return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
   }

   /**
    * @param documentId
    * @param httpRequest
    * @return
    */
   private List<GraphicAnnotation> getAnnotationsFor(String documentId, int pageNo, HttpServletRequest httpRequest)
   {
      String key = documentId + ":" + pageNo;
      if (null == tempStorage.get(key))
      {
         IDocumentContentInfo docInfo = docAnnotationsMap.get(documentId).getDocInfo();
         PrintDocumentAnnotationsImpl docAnnots = (PrintDocumentAnnotationsImpl) docInfo.getAnnotations();
         if (null == docAnnots)
         {
            docInfo.setAnnotations(new PrintDocumentAnnotationsImpl());
            tempStorage.put(key, new ArrayList<GraphicAnnotation>());            
         }
         populateAnnotationsMaps(documentId, docAnnotationsMap.get(documentId));
      }

      return tempStorage.get(key);
   }

   /**
    * @param documentId
    * @param docAnnots
    */
   private void populateAnnotationsMaps(String documentId, TIFFDocumentWrapper doc)
   {
      PrintDocumentAnnotationsImpl docAnnots = (PrintDocumentAnnotationsImpl) doc.getDocInfo().getAnnotations();
      Set<Highlight> highLights = docAnnots.getHighlights();
      for (Highlight highlight : highLights)
      {
         GraphicAnnotation annot = getGraphicAnnotationFrom(highlight);
         String key = documentId + ":" + highlight.getPageNumber();
         List<GraphicAnnotation> pgAnnots = tempStorage.get(key);
         if (null == pgAnnots)
         {
            pgAnnots = new ArrayList<GraphicAnnotation>();
            tempStorage.put(key, pgAnnots);
         }
         pgAnnots.add(annot);
      }
      Set<Note> notes = docAnnots.getNotes();
      for (Note note : notes)
      {
         GraphicAnnotation annot = getGraphicAnnotationFrom(note);
         String key = documentId + ":" + note.getPageNumber();
         List<GraphicAnnotation> pgAnnots = tempStorage.get(key);
         if (null == pgAnnots)
         {
            pgAnnots = new ArrayList<GraphicAnnotation>();
            tempStorage.put(key, pgAnnots);
         }
         pgAnnots.add(annot);
      }
      Set<Stamp> stamps = docAnnots.getStamps();
      for (Stamp stamp : stamps)
      {
         GraphicAnnotation annot = getGraphicAnnotationFrom(stamp);
         String key = documentId + ":" + stamp.getPageNumber();
         List<GraphicAnnotation> pgAnnots = tempStorage.get(key);
         if (null == pgAnnots)
         {
            pgAnnots = new ArrayList<GraphicAnnotation>();
            tempStorage.put(key, pgAnnots);
         }
         pgAnnots.add(annot);
      }
      Set<PageOrientation> pageOrientations = docAnnots.getPageOrientations();
      for (PageOrientation pageOrientation : pageOrientations)
      {
         GraphicAnnotation annot = getGraphicAnnotationFrom(pageOrientation);
         String key = documentId + ":" + pageOrientation.getPageNumber();
         List<GraphicAnnotation> pgAnnots = tempStorage.get(key);
         if (null == pgAnnots)
         {
            pgAnnots = new ArrayList<GraphicAnnotation>();
            tempStorage.put(key, pgAnnots);
         }
         pgAnnots.add(annot);
      }
   }

   /**
    * @param highlight
    * @return
    */
   private GraphicAnnotation getGraphicAnnotationFrom(Highlight highlight)
   {
      GraphicAnnotation annot = new GraphicAnnotation();
      annot.setId(highlight.getId());
      annot.setAction(ACTIION_RECREATE);
      setUserActionAndDate(annot, highlight);
      annot.setType(ANNOTATION_TYPE_HIGHLIGHTER);
      AnnotationProperties annotProps = new AnnotationProperties();
      AnnotationDimensions dim = new AnnotationDimensions();
      dim.setHeight(highlight.getHeight().intValue());
      dim.setWidth(highlight.getWidth().intValue());
      dim.setX(highlight.getxCoordinate().intValue());
      dim.setY(highlight.getyCoordinate().intValue());
      annotProps.setDimensions(dim);
      annotProps.setOrientation(translateOrientationIntToString(highlight.getPageRelativeRotation()));
      AnnotationAttributes attrs = new AnnotationAttributes();
      attrs.setColour(highlight.getColor());
      attrs.setOpacity(0.5f);
      attrs.setRotationfactor(0);
      annotProps.setAttributes(attrs);
      annot.setProps(annotProps);
      return annot;
   }

   /**
    * @param note
    * @return
    */
   private GraphicAnnotation getGraphicAnnotationFrom(Note note)
   {
      GraphicAnnotation annot = new GraphicAnnotation();
      annot.setId(note.getId());
      annot.setAction(ACTIION_RECREATE);
      setUserActionAndDate(annot, note);
      annot.setType(ANNOTATION_TYPE_NOTE);
      AnnotationProperties annotProps = new AnnotationProperties();
      AnnotationDimensions dim = new AnnotationDimensions();
      dim.setHeight(note.getHeight().intValue());
      dim.setWidth(note.getWidth().intValue());
      dim.setX(note.getxCoordinate().intValue());
      dim.setY(note.getyCoordinate().intValue());
      annotProps.setDimensions(dim);
      annotProps.setOrientation(translateOrientationIntToString(note.getPageRelativeRotation()));
      annotProps.setCompletetext(note.getText());
      annotProps.setText(note.getText());
      AnnotationAttributes attrs = new AnnotationAttributes();
      attrs.setColour(note.getColor());
      attrs.setOpacity(1f);
      attrs.setRotationfactor(0);
      attrs.setFontsize(note.getFontSize());
      setFontStyleForGraphicAnnotation(attrs, note);
      annotProps.setAttributes(attrs);
      annot.setProps(annotProps);

      return annot;
   }
   
   private void setFontStyleForGraphicAnnotation(AnnotationAttributes attrs, Note note)
   {
      attrs.setFontweight(STYLE_NORMAL);
      attrs.setFontstyle(STYLE_NORMAL);
      attrs.setTextdecoration(STYLE_NORMAL);
      
      Set<TextStyle> textStyle = note.getTextStyle();
      Iterator<TextStyle> iter = textStyle.iterator();
      while(iter.hasNext())
      {
         TextStyle style = iter.next();
         if(style == TextStyle.BOLD)
         {
            attrs.setFontweight(STYLE_BOLD);
         }
         if(style == TextStyle.ITALIC)
         {
            attrs.setFontstyle(STYLE_ITALIC);
         }
         if(style == TextStyle.UNDERLINED)
         {
            attrs.setTextdecoration(STYLE_UNDERLINE);
         }
      }
   }
   
   private void setFontStyleForNote(Note note, GraphicAnnotation annot)
   {
      Set<TextStyle> style = new HashSet<TextStyle>();
      if (STYLE_ITALIC.equals(annot.getProps().getAttributes().getFontstyle()))
      {
         style.add(TextStyle.ITALIC);
      }
      else
      {
         style.remove(TextStyle.ITALIC);
      }
      if (STYLE_UNDERLINE.equals(annot.getProps().getAttributes().getTextdecoration()))
      {
         style.add(TextStyle.UNDERLINED);
      }
      else
      {
         style.remove(TextStyle.UNDERLINED);
      }
      if (STYLE_BOLD.equals(annot.getProps().getAttributes().getFontweight()))
      {
         style.add(TextStyle.BOLD);
      }
      else
      {
         style.remove(TextStyle.BOLD);
      }
      
      note.setTextStyle(style);
   }  

   /**
    * @param stamp
    * @return
    */
   private GraphicAnnotation getGraphicAnnotationFrom(Stamp stamp)
   {
      GraphicAnnotation annot = new GraphicAnnotation();
      annot.setId(stamp.getId());
      annot.setAction(ACTIION_RECREATE);
      setUserActionAndDate(annot, stamp);
      annot.setType(ANNOTATION_TYPE_STAMP);
      AnnotationProperties annotProps = new AnnotationProperties();
      AnnotationDimensions dim = new AnnotationDimensions();
      dim.setHeight(stamp.getHeight().intValue());
      dim.setWidth(stamp.getWidth().intValue());
      dim.setX(stamp.getxCoordinate().intValue());
      dim.setY(stamp.getyCoordinate().intValue());
      annotProps.setDimensions(dim);
      annotProps.setOrientation(translateOrientationIntToString(stamp.getPageRelativeRotation()));
      annotProps.setDocumentId(stamp.getStampDocumentId());
      AnnotationAttributes attrs = new AnnotationAttributes();
      attrs.setColour(stamp.getColor());
      attrs.setOpacity(1f);
      attrs.setRotationfactor(0);
      annotProps.setAttributes(attrs);
      annot.setProps(annotProps);

      return annot;
   }

   /**
    * @param pageOrientation
    * @return
    */
   private GraphicAnnotation getGraphicAnnotationFrom(PageOrientation pageOrientation)
   {
      GraphicAnnotation annot = new GraphicAnnotation();
      annot.setId(ROTATION_ID);
      annot.setType(ROTATE_ACTION);
      AnnotationProperties annotProps = new AnnotationProperties();
      AnnotationAttributes attrs = new AnnotationAttributes();
      attrs.setRotationfactor(pageOrientation.getRotation());
      annotProps.setAttributes(attrs);
      annot.setProps(annotProps);

      return annot;
   }

   /**
    * @param annot
    * @param pageNo
    * @return
    */
   private Highlight getHighlightFrom(GraphicAnnotation annot, int pageNo)
   {
      Highlight highlight = new Highlight();
      highlight.setId(annot.getId());
      setUserActionAnddate(highlight, annot);
      highlight.setPageNumber(pageNo);
      highlight.setWidth(annot.getProps().getDimensions().getWidth());
      highlight.setHeight(annot.getProps().getDimensions().getHeight());
      highlight.setxCoordinate(annot.getProps().getDimensions().getX());
      highlight.setyCoordinate(annot.getProps().getDimensions().getY());
      highlight.setColor(annot.getProps().getAttributes().getColour());
      highlight.setPageRelativeRotation(translateOrientationStringToInt(annot.getProps().getOrientation()));
      return highlight;
   }

   /**
    * @param annot
    * @param pageNo
    * @return
    */
   private Note getStickyNoteFrom(GraphicAnnotation annot, int pageNo)
   {
      Note note = new Note();
      note.setId(annot.getId());
      setUserActionAnddate(note, annot);
      note.setPageNumber(pageNo);
      note.setWidth(annot.getProps().getDimensions().getWidth());
      note.setHeight(annot.getProps().getDimensions().getHeight());
      note.setxCoordinate(annot.getProps().getDimensions().getX());
      note.setyCoordinate(annot.getProps().getDimensions().getY());
      note.setColor(annot.getProps().getAttributes().getColour());
      note.setText(annot.getProps().getCompletetext());
      note.setFontSize(annot.getProps().getAttributes().getFontsize());
      note.setPageRelativeRotation(translateOrientationStringToInt(annot.getProps().getOrientation()));
      setFontStyleForNote(note, annot);

      return note;
   }

   /**
    * @param annot
    * @param pageNo
    * @return
    */
   private Stamp getStampFrom(GraphicAnnotation annot, int pageNo)
   {
      Stamp stamp = new Stamp();
      stamp.setId(annot.getId());
      setUserActionAnddate(stamp, annot);
      stamp.setPageNumber(pageNo);
      stamp.setWidth(annot.getProps().getDimensions().getWidth());
      stamp.setHeight(annot.getProps().getDimensions().getHeight());
      stamp.setxCoordinate(annot.getProps().getDimensions().getX());
      stamp.setyCoordinate(annot.getProps().getDimensions().getY());
      stamp.setStampDocumentId(annot.getProps().getDocumentid());
      stamp.setPageRelativeRotation(translateOrientationStringToInt(annot.getProps().getOrientation()));
      return stamp;
   }

   /**
    * @param rotationFactor
    * @param pageNo
    * @return
    */
   private PageOrientation getPageOrientationFrom(GraphicAnnotation rotationFactor, int pageNo)
   {
      PageOrientation pageOrientation = new PageOrientation();
      pageOrientation.setPageNumber(pageNo);
      pageOrientation.setRotation(rotationFactor.getProps().getAttributes().getRotationfactor());
      return pageOrientation;
   }

   /**
    * @param httpRequest
    * @param doc
    */
   private int getOriginalPageIndex(HttpServletRequest httpRequest, String documentId, int pageNo)
   {
      SessionSharedObjectsMap sessionSharedMap = (SessionSharedObjectsMap) httpRequest.getSession().getAttribute(
            "sessionSharedObjectsMap");
      TIFFDocumentHolder dms = (TIFFDocumentHolder) sessionSharedMap.getObject(documentId);
      return dms.getOriginalPageIndex(pageNo);
   }

   /**
    * @param pgAnnot
    * @param grAnnot
    */
   private void setUserActionAnddate(PageAnnotation pgAnnot, GraphicAnnotation grAnnot)
   {
      if (null != grAnnot.getLastuseraction() && grAnnot.getAction().equals(USR_ACTION_CREATE))
      {
         pgAnnot.setCreatedByAuthor(grAnnot.getUser());
         pgAnnot.setCreateDate(getDate(grAnnot.getLastactiontimestamp(), getLocale()));
      }
      else if (null != grAnnot.getLastuseraction() && grAnnot.getAction().equals(USR_ACTION_MODIFY))
      {
         pgAnnot.setModifiedByAuthor(grAnnot.getUser());
         pgAnnot.setModificationDate(getDate(grAnnot.getLastactiontimestamp(), getLocale()));
      }
   }

   /**
    * @param grAnnot
    * @param pgAnnot
    */
   private void setUserActionAndDate(GraphicAnnotation grAnnot, PageAnnotation pgAnnot)
   {
      if (null != pgAnnot.getCreatedByAuthor())
      {
         grAnnot.setUser(pgAnnot.getCreatedByAuthor());
         grAnnot.setLastuseraction(USR_ACTION_CREATED);
         grAnnot.setLastactiontimestamp(formatDate(pgAnnot.getCreateDate(), getLocale()));
      }
      if (null != pgAnnot.getModifiedByAuthor())
      {
         grAnnot.setUser(pgAnnot.getModifiedByAuthor());
         grAnnot.setLastuseraction(USR_ACTION_MODIFIED);
         grAnnot.setLastactiontimestamp(formatDate(pgAnnot.getModificationDate(), getLocale()));
      }
   }

   /**
    * @param dateStr
    * @return
    */
   private Date getDate(String dateStr, Locale locale)
   {
      try
      {
         SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, locale);
         return formatter.parse(dateStr);
      }
      catch (ParseException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   /**
    * @param date
    * @return
    */
   private String formatDate(Date date, Locale locale)
   {
      SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, locale);
      return formatter.format(date);
   }   
   
   public String getLocale(String langHeaderString)
   {
      String langPostFix = LANG_POST_FIX_MAP.get(langHeaderString.substring(0, 2));
      if (null == langPostFix)
      {
         langPostFix = langHeaderString.substring(0, 2);
      }
      
      return langPostFix;
   }
   
   private void initLocaleMap()
   {
      if (null == LANG_POST_FIX_MAP)
      {
         LANG_POST_FIX_MAP = new HashMap<String, String>();
         LANG_POST_FIX_MAP.put("zh", "zh_CN");
      }
   }
   
   /**
    * @return
    */
   public Locale getLocale()
   {
      StringTokenizer tok = new StringTokenizer(httpRequest.getHeader("Accept-language"), ",");
      if (tok.hasMoreTokens())
      {
         return new Locale(tok.nextToken().substring(0, 2));
      }      
      return new Locale("en");
   }
   
   /**
    * @param orientation
    * @return
    */
   private int translateOrientationStringToInt(String orientation)
   {
      if (orientation.equals("N"))
      {
         return 0;
      }
      else if (orientation.equals("E"))
      {
         return -90;
      }
      else if (orientation.equals("W"))
      {
         return 90;
      }
      else if (orientation.equals("S"))
      {
         return 180;
      }

      return 0;
   }
   
   private String translateOrientationIntToString(int orietntation)
   {
      int intOr = ((orietntation / 90) % 4);
      if (intOr < 0)
      {
         intOr = 4 + intOr;
      }
      switch(intOr)
      {
          case 0:
              return "N";
          case 1:
              return "W";
          case 2:
              return "S";
          case 3:
              return "E";
          default:
              return "N";
      }
   }
}
