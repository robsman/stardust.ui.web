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
package org.eclipse.stardust.ui.web.common.app;

import static org.eclipse.stardust.ui.web.common.util.StringUtils.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.UiElementMessage;
import org.eclipse.stardust.ui.web.common.spring.scope.TabScopeManager;
import org.eclipse.stardust.ui.web.common.uielement.AbstractUiElement;
import org.eclipse.stardust.ui.web.common.util.AbstractMessageBean;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;

import com.google.gson.Gson;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class View extends AbstractUiElement implements TabScopeManager
{
   private static final long serialVersionUID = 8000517199194284012L;

   public static final String PRE_DESCRIPTION = "description";
   
   private ViewState viewState;

   private View openerView;

   private final ViewDefinition definition;

   private final String url;

   private String viewKey;

   private final String id;

   private String icon = "/plugins/common/images/icons/tabIcon-generic.png" ;

   private String label;

   private String fullLabel;

   private String tooltip;

   private String description;

   private final Map<String, Object> viewStateMap = new HashMap<String, Object>();

   private final Map<String, Runnable> viewStateDestructionCallbacks = new HashMap<String, Runnable>();

   private boolean selected;

   protected final AbstractMessageBean msgBean;

   private Map<String, Object> viewParams = CollectionUtils.newTreeMap();

   private String identityUrl;
   
   private FrameworkViewInfo frameworkViewInfo = null;
   
   private static final Logger trace = LogManager.getLogger(PortalUiController.class);

   /**
    * @param definition
    * @param url
    */
   public View(ViewDefinition definition, String url)
   {
      this(definition, url, null);
   }

   /**
    * Package Scope Constrcutor
    *
    * @param definition
    * @param url
    * @param msgBean
    */
   public View(ViewDefinition definition, String url, AbstractMessageBean msgBean)
   {
      super("");

      this.url = url;
      this.msgBean = msgBean;

      this.id = createIdFromUrl(url);

      String[] strings = url.split("\\?");
      
      String params = strings.length > 1 ? strings[1] : "";

      this.definition = definition;

      super.setName(this.definition != null ? this.definition.getName() : "");

      // for backward-compatibility string parsed to map.
      this.viewParams = parseParams(params);

      resolveLabelAndDescription();
   }

   /**
    * @param definition
    * @param viewKey
    * @param viewParams
    * @param msgBean
    */
   public View(ViewDefinition definition, String viewKey, Map<String, Object> viewParams,
         AbstractMessageBean msgBean)
   {
      super(definition != null ? definition.getName() : "");

      this.definition = definition;
      this.viewKey = viewKey;
      this.url = createURL(definition, viewKey);
      this.msgBean = msgBean;
      // TODO remove old fields
      this.id = createIdFromUrl(url);

      if (viewParams != null)
      {
         this.viewParams = viewParams;
         // this.parsedParams = extractStringParams(viewParams);
      }
      else
      { // for backward-compatibility string parsed to map.
         this.viewParams = parseParams(viewKey);
      }

      resolveLabelAndDescription();
      
   }

   /**
    * 
    */
   private void generateHTML5FrameworkId()
   {
      frameworkViewInfo = FrameworkViewInfo.generateHTML5FrameworkViewInfo(this);
   }
   
   public static String createURL(ViewDefinition definition, String viewKey)
   {
      String appendix = isEmpty(viewKey) ? "" : "?" + viewKey;
      String url = definition.getInclude() + "/" + definition.getName() + appendix;
      return url;
   }

   @Override
   public UiElementMessage createMessages()
   {
      return new UiElementMessage(definition);
   }

   @Override
   public String toString()
   {
      return url;
   }

   public void destroy()
   {
      // destroy view scoped beans
      for (Map.Entry<String, Runnable> dtor : viewStateDestructionCallbacks.entrySet())
      {
         if (viewStateMap.containsKey(dtor.getKey()))
         {
            dtor.getValue().run();
         }
      }

      viewStateDestructionCallbacks.clear();
      viewStateMap.clear();
   }

   public ViewState getViewState()
   {
      return viewState;
   }

   void setViewState(ViewState viewState)
   {
      this.viewState = viewState;
   }

   private static String createIdFromUrl(String url)
   {
      if (url.startsWith("/"))
         url = url.substring(1);

      if (url.endsWith(".xhtml"))
         url = url.substring(0, url.indexOf(".xhtml"));
      String id = url.replace('/', '.');
      id = convertDashStringToCamelCase(id);
      return id;
   }

   /**
    * @param str
    * @return
    */
   private static String convertDashStringToCamelCase(String str)
   {
      StringBuffer ret = new StringBuffer();
      boolean dashFound = false;

      char[] chars = str.toCharArray();

      for (char c : chars)
      {
         if (c == '-')
         {
            dashFound = true;
         }
         else
         {
            if (dashFound)
            {
               ret.append(String.valueOf(c).toUpperCase());
               dashFound = false;
            }
            else
            {
               ret.append(c);
            }
         }
      }

      return ret.toString();
   }

   private String contertToParamString(Map<String, Object> viewParams2)
   {
      String ret = "?";

      if (viewParams2 != null)
      {
        for (Entry<String, Object> element : viewParams2.entrySet())
         {
            if (element.getValue() instanceof String)
            {
               try
               {
                  ret += element.getKey() + "="
                        + URLEncoder.encode((String) element.getValue(), "UTF-8") + "&";
               }
               catch (UnsupportedEncodingException e)
               {
                  ret += element.getKey() + "=" + element.getValue() + "&";
               }
            }
         }
      }

      if (ret.length() < 2)
         ret = "";
      else if (ret.length() >= 2)
         ret = ret.substring(0, ret.lastIndexOf('&'));
      return ret;
   }

   /**
    * @param viewParams2
    * @return
    */
   private String convertToJsonString(Map<String, Object> viewParams2)
   {
      String ret = "";

      if (viewParams2 != null)
      {
         try
         {
            Gson gson = new Gson();
            return gson.toJson(viewParams2);
         }
         catch (Exception e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug(("Exception occured while converting all view Params to String so doing it on discrete data types"));
            }
            
            for (Entry<String, Object> element : viewParams2.entrySet())
            {
               if (element.getValue() instanceof String)
               {
                  ret += element.getKey() + ": '" + StringUtils.replace((String) element.getValue(), "'", "\\'") + "', ";
               }
               else if (element.getValue() instanceof Number)
               {
                  ret += element.getKey() + ":" + element.getValue() + ",";
               }
               else if (element.getValue() instanceof Boolean)
               {
                  ret += element.getKey() + ":" + element.getValue() + ",";
               }
            }
         }
        
      }

      if (ret.length() == 0)
         ret = "{}";
      else
         ret = "{" + ret.substring(0, ret.length() - 2) + "}";
      return ret;
   }

   private static Map<String, String> extractStringParams(Map<String, Object> viewParams)
   {
      Map<String, String> ret = CollectionUtils.newTreeMap();
      for (Entry<String, Object> element : viewParams.entrySet())
      {
         if (element.getValue() instanceof String)
         {
            ret.put(element.getKey(), (String) element.getValue());
         }
      }
      return ret;
   }

   public static Map<String, Object> parseParams(String params)
   {
      Map<String, Object> viewParams = CollectionUtils.newTreeMap();

      if (StringUtils.isNotEmpty(params))
      {
         if (params.startsWith("?") && (params.length() > 1))
         {
            params = params.substring(params.indexOf("?") + 1);
         }
         // parse view parameters
         for (Iterator<String> i = split(params, "&"); i.hasNext();)
         {
            Iterator<String> parsedParam = split(i.next(), "=");
            String name = parsedParam.next();
            String value = parsedParam.hasNext() ? parsedParam.next() : null;

            if ( !isEmpty(name))
            {
               try
               {
                  if (StringUtils.isNotEmpty(value))
                  {
                     viewParams.put(name, URLDecoder.decode(value, "UTF-8"));
                  }
                  else
                  {
                     viewParams.put(name, value);
                  }
               }
               catch (Exception e)
               {
                  viewParams.put(name, value);
               }
            }
         }
      }
      return viewParams;
   }

   public void resolveLabelAndDescription()
   {
      String key = "label";
      this.label = getMessage(key);
      this.fullLabel = this.label;
      this.tooltip = label;

      if (hasMessage(key))
      {
         for (String paramName : viewParams.keySet())
         {
            if (paramName instanceof String)
            {
               if ( -1 != label.indexOf("${viewParams." + paramName + "}"))
               {
                  updateParamValue(paramName, (String) viewParams.get(paramName));
               }
            }
         }
      }

      String key2 = "description";
      this.description = getMessage(key2);
   }

   /**
    * @param viewDefinition
    * @param params
    * @return
    */
   public static String getViewIdentityParams(ViewDefinition viewDefinition, Map<String, Object> params)
   {
      if(null != viewDefinition && !CollectionUtils.isEmpty(params))
      {
         StringBuffer sb = new StringBuffer();

         for (String key : params.keySet())
         {
            if (viewDefinition.getIdentityParamsSet().contains(key))
            {
               if (0 != sb.length())
               {
                  sb.append("&");
               }

               sb.append(key).append("=").append(params.get(key));
            }
         }

         return sb.toString();
      }

      return null;
   }

   /**
    * @return
    */
   public String getIdentityParams()
   {
      String idParams = getViewIdentityParams(definition, viewParams);
      if (StringUtils.isEmpty(idParams))
      {
         idParams = viewKey; // Keep the backward compatibility
      }

      return idParams;
   }

   /**
    * @return
    */
   public String getIdentityUrl()
   {
      if (null == identityUrl)
      {
         identityUrl = createURL(definition, getIdentityParams());
      }
      return identityUrl;
   }

   /**
    * @param paramName
    * @param paramValue
    */
   private void updateParamValue(String paramName, String paramValue)
   {
      String strParamConstruct = "${viewParams." + paramName + "}[";
      int index = label.indexOf(strParamConstruct);
      if (-1 != index)
      {
         String paramTrunc = label.substring(index + strParamConstruct.length(), label.indexOf("]", index));
         try
         {
            int paramTruncNum = Integer.parseInt(paramTrunc);
            String truncatedValue = paramValue;
            if(paramValue.length() > paramTruncNum)
            {
               truncatedValue = paramValue.substring(0, paramTruncNum);
               truncatedValue += "...";
            }

            label = replace(label, strParamConstruct + paramTrunc + "]", truncatedValue);
            fullLabel = replace(fullLabel, strParamConstruct + paramTrunc + "]", paramValue);
            tooltip = replace(tooltip, strParamConstruct + paramTrunc + "]", paramValue);
         }
         catch(Exception e)
         {
            // Ignore
         }
      }
      else
      {
         label = replace(label, "${viewParams." + paramName + "}", paramValue);
         fullLabel = replace(fullLabel, "${viewParams." + paramName + "}", paramValue);
         tooltip = replace(tooltip, "${viewParams." + paramName + "}", paramValue);
      }
   }

   /**
    * @param key
    * @return
    */
   private boolean hasMessage(String key)
   {
      if (null != msgBean)
      {
         return msgBean.hasKey(getId() + "." + key);
      }
      else if(null != definition)
      {
         return definition.hasMessage(key, null);
      }
      else
      {
         return MessagePropertiesBean.getInstance().hasKey(getId() + "." + key);
      }
   }

   /**
    * @param key
    * @return
    */
   private String getMessage(String key)
   {
      if (msgBean != null)
      {
         return msgBean.getString(getId() + "." + key);
      }
      else if(definition != null)
      {
         return definition.getMessage(key, null);
      }
      else
      {
         return MessagePropertiesBean.getInstance().getString(getId() + "." + key);
      }
   }

   /**
    * @param name
    * @return
    */
   public String getParamValue(String name)
   {
      String ret = null;
      if ((viewParams != null) && (viewParams.get(name) instanceof String))
      {
         ret = (String) viewParams.get(name);
      }
      return ret;
   }

   public ViewDefinition getDefinition()
   {
      return definition;
   }

   public String getViewId()
   {
      String viewId = (definition == null) ? getId() : definition.getName();
      return viewId;
   }

   public String getUrl()
   {
      return url;
   }

   public String getPath()
   {
      return definition.getInclude();
   }

   public Map<String, Object> getCurrentTabScope()
   {
      return viewStateMap;
   }

   public Map<String, Runnable> getCurrentTabScopeDestructionCallbacks()
   {
      return viewStateDestructionCallbacks;
   }

   /**
    * @deprecated Replace by direct use of {@link TabScopeManager}
    */
   @Deprecated
   public Map<String, Object> getViewMap()
   {
      return getCurrentTabScope();
   }

   /**
    * @deprecated Replace by direct use of {@link TabScopeManager}
    */
   @Deprecated
   public Map<String, Runnable> getViewScopeDestructionCallbacks()
   {
      return getCurrentTabScopeDestructionCallbacks();
   }

   public boolean isSelected()
   {
      return selected;
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }

   public String getId()
   {
      return id;
   }

   public String getIcon()
   {
      return icon;
   }

   public void setIcon(String icon)
   {
      this.icon = icon;
   }

   public String getLabel()
   {
      return label;
   }

   public void setLabel(String label)
   {
      this.label = label;
   }

   public String getFullLabel()
   {
      return fullLabel;
   }

   public void setFullLabel(String fullLabel)
   {
      this.fullLabel = fullLabel;
   }

   public String getTooltip()
   {
      return tooltip;
   }

   public void setTooltip(String tooltip)
   {
      this.tooltip = tooltip;
   }

   public String getDescription()
   {
      return description;
   }

   public String getParams()
   {
      return contertToParamString(viewParams);
   }

   public String getParamsAsJson()
   {
      return convertToJsonString(viewParams);
   }

   public View getOpenerView()
   {
      return openerView;
   }

   public void setOpenerView(View openerView)
   {
      this.openerView = openerView;
   }

   public Map<String, Object> getViewParams()
   {
      return viewParams;
   }

   public void setViewParams(Map<String, Object> viewParams)
   {
      this.viewParams = viewParams;
   }

   public enum ViewState
   {
      CREATED,
      ACTIVE,
      INACTIVE,
      CLOSED
   }

   public void updateViewKey(String newViewKey)
   {
      this.viewKey = newViewKey;
      identityUrl = null;
   }

   public String getViewKey()
   {
      return viewKey;
   }

   public FrameworkViewInfo getHtml5FwViewInfo()
   {
      if (frameworkViewInfo == null)
      {
         generateHTML5FrameworkId();
      }
      return frameworkViewInfo;
   }

   public String getHtml5FwViewId()
   {
      if (frameworkViewInfo == null)
      {
         generateHTML5FrameworkId();
      }
      return frameworkViewInfo.getHtml5FwViewId();
   }
}
