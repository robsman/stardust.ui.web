Changes the logging for the CXF web services stack to log4j.
This is needed because since cxf 2.6 logging defaults to SLF4J 1.6.0+ if SLF4J is found in the classpath and causes errors on jboss 5.1 which uses SLF4J 1.5.x
See https://issues.apache.org/jira/browse/CXF-4180.