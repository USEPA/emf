<%@ page import="gov.epa.emissions.framework.client.transport.RemoteServiceLocator" %>
<%@ page import="gov.epa.emissions.framework.client.transport.ServiceLocator" %>
<%@ page contentType="text/xml; charset=utf-8" trimDirectiveWhitespaces="true" %>
<%@ include file="i18nLib.jsp" %>
<%
  String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    ServiceLocator serviceLocator = new RemoteServiceLocator(DEFAULT_URL);

%>
<EMFVersion><%out.print(serviceLocator.userService().getEmfVersion());%></EMFVersion>