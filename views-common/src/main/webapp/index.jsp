<%@page import="org.eclipse.stardust.ui.web.common.util.StringUtils"%>
<%@page import="org.eclipse.stardust.ui.web.common.util.FacesUtils"%>
<%@page import="org.eclipse.stardust.ui.web.common.util.ReflectionUtils"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.eclipse.stardust.ui.web.plugin.support.ServiceLoaderUtils"%>
<%@page import="org.eclipse.stardust.ui.web.common.spi.user.AuthenticationProvider"%>

<%@ page session="true" contentType="text/html;charset=utf-8"%>
<%
   AuthenticationProvider.Factory authProviderFactory = null;

   String authenticationProviderFactory = pageContext.getServletContext().getInitParameter(AuthenticationProvider.Factory.PARAM_NAME);
   if (StringUtils.isNotEmpty(authenticationProviderFactory))
   {
      try
      {
         Object object = ReflectionUtils.createInstance(authenticationProviderFactory);
         if (object instanceof AuthenticationProvider.Factory)
         {
            authProviderFactory = (AuthenticationProvider.Factory) object;
         }
         else
         {
            out.println("<b>Init Param <i>" + AuthenticationProvider.Factory.PARAM_NAME + "=" + authenticationProviderFactory + "</i> is not an instanceof AuthenticationProvider.Factory.</b><br/><br/>");
         }
      }
      catch (Exception e)
      {
         out.println("<b>Error occurred in creating AuthenticationProvider Factory.</b>");
         out.println("<br/><br/>");
         out.println(FacesUtils.getStackTrace(e));
         out.println("<br/><br/>");
      }
   }
   else
   {
      Iterator<AuthenticationProvider.Factory> serviceProviders = ServiceLoaderUtils
            .searchProviders(AuthenticationProvider.Factory.class);

      if (null != serviceProviders && serviceProviders.hasNext())
      {
         authProviderFactory = serviceProviders.next();
      }
   }

	if (null != authProviderFactory)
   {
      AuthenticationProvider authProvider = authProviderFactory.getAuthenticationProvider();
      authProvider.initialize(pageContext.getServletContext(), request, response);
      authProvider.showPage();
   }
   else
   {
      out.println("<b>Authentication Provider Not configured. Cannot login... Please contact Administrator</b><br/><br/>");
      out.println("Authentication Provider can be configured in two ways -");
      out.println("<h4>web.xml init param</h4>");
      out.println("E.g.");
      out.print("<pre>");
      out.println("&lt;context-param&gt;");
      out.println("&nbsp;&nbsp;&lt;param-name&gt;authenticationProviderFactory&lt;/param-name&gt;");
      out.println("&nbsp;&nbsp;&lt;param-value&gt;org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppAuthenticationProvider$IppFactory&lt;/param-value&gt;");
      out.println("&lt;/context-param&gt;");
      out.print("</pre>");
      out.print("<br/>");
      out.println("<h4>Using Service Provider</h4>");
      out.println("Create a file called <i>org.eclipse.stardust.ui.web.common.spi.user.AuthenticationProvider$Factory</i> under <i>META-INF/services</i>");
      out.println("<br/>Provide the implementation class name over there. E.g. -");
      out.print("<pre>");
      out.println("org.eclipse.stardust.ui.web.common.spi.user.impl.IppAuthenticationProvider$IppFactory");
      out.print("</pre>");
   }
%>