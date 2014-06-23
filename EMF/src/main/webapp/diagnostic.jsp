<%@ page import="java.lang.management.*" %>
<%@ page import="java.util.*" %>
<html>
<head>
 	<title>JVM Memory Monitor</title>
</head>
<body>
	<h1>JVM Memory Monitor</h1>
	<div><b>Heap Memory Usage</b>: <%=ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()%></div>
	<div><b>Non-Heap Memory Usage</b>: <%=ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage()%></div>
	<br/>

	<%
	List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
	for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
	%>
	
		<div><b><%=memoryPoolMXBean.getName()%> (<%=memoryPoolMXBean.getType()%>)</b>:</div>
		<div style='margin-left: 10px'>
			<br/>
			
			<%
			if (memoryPoolMXBean.isUsageThresholdSupported()) {
			%>
				<div><b>Usage</b>: <%=memoryPoolMXBean.getUsage()%></div>
				<div><b>Peak Usage</b>: <%=memoryPoolMXBean.getPeakUsage()%></div>
				<div><b>Usage Threshold</b>: <%=memoryPoolMXBean.getUsageThreshold()%></div>
				<div><b>Usage Threshold Exceeded</b>: <%=memoryPoolMXBean.isUsageThresholdExceeded()%></div>
			<%
		    } 
		    else {
			%>
				<div><b>Usage Threshold</b>: Not Supported</div>
			<%
		    } 
			%>
	
			<br/>

			<%
			if (memoryPoolMXBean.isCollectionUsageThresholdSupported()) {
			%>
				<div><b>Collection Usage</b>: <%=memoryPoolMXBean.getCollectionUsage()%></div>
				<div><b>Collection Usage Threshold</b>: <%=memoryPoolMXBean.getCollectionUsageThreshold()%></div>
				<div><b>Collection Usage Threshold Exceeded</b>: <%=memoryPoolMXBean.isCollectionUsageThresholdExceeded()%></div>
			<%
		    } 
		    else {
			%>
				<div><b>Collection Usage Threshold</b>: Not Supported</div>
			<%
		    } 
			%>
		</div>	
		<br/>
	
	<%
    }
	%>
 
</body>
</html>