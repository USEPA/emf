# Server Administration [server_chapter] #

## Components ##

The EMF server consists of a database, file storage, and the server application which handles requests from the clients and communicates with the database.

The database server is PostgreSQL version 9.2 or later. For ShapeFile export, you will need the PostGIS module installed.

The server application is a Java executable that runs in the Apache Tomcat servlet container. You will need Apache Tomcat 6.0 or later.

The server components can run on Windows, Linux, or Mac OS X.

## Network Access ##

The EMF client application communicates with the server on port 8080. For the client application, the EMFClient.bat launch script specifies the server location and port via the setting

`set TOMCAT_SERVER=http://<server address>:8080`

In order to import data into the EMF, the files must be locally accessible by the server. Depending on your setup, you may want to mount a network drive on the server or allow SFTP connections for users to upload files.

## EMF Administrator ##

Inside the EMF client, users with administrative privileges have access to additional management options.

### User Management ###

EMF administrators can reset users passwords. Administrators can also create new users.

### Dataset Type Management ###

Administrators can create and edit dataset types. Administrators can also add QA step templates to dataset types.
