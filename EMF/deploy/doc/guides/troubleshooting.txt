Title: Troubleshooting
Author: C. Seppanen, UNC
CSS: base.css

# Troubleshooting #

## Client won't start ##

**Problem:**

On startup, an error message is displayed like [Figure](#client_startup_error):

"The EMF client was not able to contact the server due to this error:

(504)Server doesn't respond at all."

or

(504)Server denies connection.

![Error Starting the EMF Client][client_startup_error]

[client_startup_error]: images/client_startup_error.png

**Solution:**

The EMF client application was not able to connect to the EMF server. This could be due to a problem on your computer, the EMF server, or somewhere in between.

If you are connecting to a remote EMF server, first check your computer's network connection by loading a page like [google.com](http://google.com) in your web browser. You must have a working network connection to use the EMF client.

Next, check the server location in the EMF client start up script C:\EMF_State\EMFClient.bat. Look for the line

`set TOMCAT_SERVER=http://<server location>:8080`

You can directly connect to the EMF server by loading 

`http://<server location>:8080/emf/services`

in your web browser. You should see a response similar to [Figure](#emf_server_response).

![EMF Server Response][emf_server_response]

[emf_server_response]: images/emf_server_response.png

If you can't connect to the EMF server or don't get a response, then the EMF server may not be running. Contact the EMF server administrator for further help.

## Can't load Dataset Manager ##

**Problem:**

When I click the Datasets item from the main Manage menu, nothing happens and I can't click on anything else.

**Solution:**

Clicking Datasets from the main Manage menu displays the Dataset Manager. In order to display this window, the EMF client needs to request a complete list of dataset types from the EMF server. If you are connecting to an EMF server over the Internet, fetching lists of data can take a while and the EMF client needs to wait for the data to be received. Try waiting to see if the Dataset Manager window appears.

## Can't load all datasets ##

**Problem:**

In the Dataset Manager, I selected Show Datasets of Type "All" and nothing happens and I can't click on anything else.

**Solution:**

When displaying datasets of the selected type, the EMF client needs to fetch the details of the datasets from the EMF server. If you are connecting to an EMF server over the Internet or if there are many datasets imported into the EMF, loading this data can take a long time. Try waiting to see if the list of datasets is displayed. Rather than displaying all datasets, you may want to pick a single dataset type or use the Advanced search to limit the list of datasets that need to be loaded from the EMF server.
