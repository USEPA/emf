<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch1_introduction.md) - [Home](README.md) - [Next Chapter >>](ch3_control_measure_manager.md)

<!-- END COMMENT -->


Title: Installing CoST Software and Data
Author: C. Seppanen, UNC
CSS: base.css

# Installing CoST Software and Data [installation_chapter] #

## Background on the CoST Client-Server System ##

Because CoST is fully integrated within the EMF, installing CoST is the same as installing the EMF. There are two parts of the CoST/EMF system: a client and a server. For this guide, it is assumed that you need to install both the client and the server.

In a client-server system, there is a client portion of the system that runs on your desktop computer. The CoST/EMF client is a Java program that accesses software running on the CoST/EMF server. Because it is written in Java, it requires that a recent version of Java be installed on each user's computer. The EMF server runs a PostgreSQL ([http://www.postgresql.org](http://www.postgresql.org)) database that stores information related to emissions modeling, including emissions inventory datasets and a database of control measures. When a control strategy is developed, new datasets and summaries of them are created within CoST, and controlled emissions inventories can optionally be generated. These emissions inventories can be exported from CoST and then used as inputs to the SMOKE modeling system, which prepares emissions data for use in the CMAQ model. A schematic of the CoST/EMF client-server system is shown in [Figure](#cost_emf_client_server_system).

![CoST/EMF Client-Server System][cost_emf_client_server_system]

[cost_emf_client_server_system]: images/CoST_EMF_Client_Server_System.png

## Downloading the Software Installation Package ##

The software installation package is a ZIP file (~300MB) that contains all the relevant supporting applications and software required to run the CoST system on a Windows-based machine. CoST requires Java Runtime Environment 7 or 8 (also known as JRE 1.7 or 1.8), Tomcat, and PostgreSQL.

The total space required for the software is 5GB. Around 1.2GB of space can be freed at the end of the installation process. Make sure you have enough storage space (~40-50 GB) available to allow for future usage with your own custom inventories and control measures in the CoST system.

The software package can be downloaded via UNC's Community Modeling and Analysis System (CMAS).

1\. Download the installation package at the CMAS software download site: [http://www.cmascenter.org/download/software.cfm](http://www.cmascenter.org/download/software.cfm)

2\. Unzip the emf_state_install_20170404.zip file into a known folder location.

[Figure](#installation_package_zip_file_folder_and_file_structure) lists the batch file and the folders that are located in the install zip file; these are described below the figure.

![Installation Package Zip File Folder and File Structure][installation_package_zip_file_folder_and_file_structure]

[installation_package_zip_file_folder_and_file_structure]: images/Installation_Package_Zip_File_Folder_and_File_Structure.png

* Install\_EMF.bat - bat file to install the EMF Client and Server
* \control\_measures - contains measures in the database (note: These are already installed)
* \database\_backup - contains a backup of the database
* \EMF\_Client - includes client installation package (all the Java libraries etc.)
* \EMF\_Server - includes the emf.war file that will be pushed to the tomcat server
* \inventories - contains inventories
* \java\_jre - includes the Java JRE installation package
* \postgresql - includes the PostgreSQL installation package and postgresql jdbc driver
* \tomcat - includes the Tomcat installation package

## Installing Java ##

3\. Go to the java\_jre directory and double click the executable file, **jre-8u51-windows-i586.exe**.

Follow the installation steps as illustrated in the following figures.

![][java_setup_welcome]

[java_setup_welcome]: images/Java_Setup_Welcome.png

Click **Install** to accept the license agreement and start the installation process.

![][java_setup_progress]

[java_setup_progress]: images/Java_Setup_Progress.png

![][java_setup_complete]

[java_setup_complete]: images/Java_Setup_Complete.png

Click Close to finalize the installation process.

## Installing PostgreSQL Database ##

4\. Go to the postgresql directory and double click the executable file, **postgresql-9.3.9-3-windows.exe**.

During the installation process, you'll be prompted to enter a database superuser password. For this step, try to use the password **postgres**. Note that your network administrator might enforce password security restrictions. If this is the case, then use a password that meets these restrictions. Remember this password for a later step during the installation.

Follow the installation steps as illustrated in the following figures.

![][postgres_welcome]

[postgres_welcome]: images/Postgres_Welcome.png

Click **Next** to begin the installation process.

![][postgres_installation_directory]

[postgres_installation_directory]: images/Postgres_Installation_Directory.png

The default location is sufficient, click **Next** to continue to the next step. Remember this directory for later use in Step 2-6.

![][postgres_data_directory]

[postgres_data_directory]: images/Postgres_Data_Directory.png

The default location is sufficient, click **Next** to continue to the next step.

![][postgres_password]

[postgres_password]: images/Postgres_Password.png

For this step, make sure you use the password **postgres**. This password is expected during a later step when installing the CoST database. Note that your network administrator could have password security restrictions. If this is the case, then use a password that meets these restrictions. Remember this password for a later step during the installation.

![][postgres_port]

[postgres_port]: images/Postgres_Port.png

The default **Port** is sufficient, click **Next** to continue to the next step.

![][postgres_locale]

[postgres_locale]: images/Postgres_Locale.png

The default **Locale** is sufficient, click **Next** to continue to the next step.

![][postgres_ready_to_install]

[postgres_ready_to_install]: images/Postgres_Ready_to_Install.png

Click **Next** to install the PostgreSQL database server.

![][postgres_installing]

[postgres_installing]: images/Postgres_Installing.png

Click **Next** to finalize the PostgreSQL installation.

![][postgres_complete]

[postgres_complete]: images/Postgres_Complete.png

When you reach the end, uncheck the **Launch Stack Builder** option and click **Finish**.

The PostgreSQL database is now installed and ready for the CoST system database. This database will be installed in a later step.

## Installing Tomcat Web/Application Server ##

5\. Go to the tomcat directory and find the executable file, **apache-tomcat-7.0.63.exe**. Double click the file to install Tomcat. Follow the installation steps as illustrated in the following figures.

![][tomcat_welcome]

[tomcat_welcome]: images/Tomcat_Welcome.png

Click **Next** to begin the installation process.

![][tomcat_license]

[tomcat_license]: images/Tomcat_License.png

Click **I Agree** to continue to the next step.

![][tomcat_components]

[tomcat_components]: images/Tomcat_Components.png

Expand the **Tomcat** option and check the **Service Startup** and **Native** components and then click Next. Note by checking **Service Startup**, this important step will make sure the application server is available on startup when the machine is rebooted.

![][tomcat_options]

[tomcat_options]: images/Tomcat_Options.png

The default settings are sufficient, click **Next** to continue to the next step.

![][tomcat_jvm]

[tomcat_jvm]: images/Tomcat_JVM.png

The default location is sufficient, click **Next** to continue to the next step.

![][tomcat_install_location]

[tomcat_install_location]: images/Tomcat_Install_Location.png

The default location is sufficient, click **Install** to install the Tomcat web server. Remember this folder for later use in Step 2-6.

![][tomcat_installing]

[tomcat_installing]: images/Tomcat_Installing.png

Once the program files have been installed click Next to finalize installation process.

![][tomcat_complete]

[tomcat_complete]: images/Tomcat_Complete.png

When you reach the end, click **Finish**. The Tomcat application server is now installed and ready for the CoST system application. This CoST application will be installed in the next step.

## Installing CoST Application ##

6\. Go to the root directory where the zip file was installed and find the Install_EMF.bat executable file. Edit the bat file and change the following variables to match your computer's settings:

> `SET EMF_CLIENT_DIRECTORY=C:\Users\Public\EMF`

> `SET EMF_DATA_DIRECTORY=C:\Users\Public\EMF_Data`

> `SET POSTGRESDIR=C:\Program Files\PostgreSQL\9.3`

> `SET TOMCAT_DIR=C:\Program Files\Apache Software Foundation\Tomcat 7.0`

The `EMF_CLIENT_DIRECTORY` variable contains the location where the EMF client application will be installed. This is the location where you will find the actual program to run CoST.

The `EMF_DATA_DIRECTORY` variable contains the location where the EMF data files (e.g., inventories and control measure import files) will be installed.

The `POSTGRESDIR` variable contains the location where the PostgreSQL application was installed.

The `TOMCAT_DIR` variable contains the location where the Tomcat application was installed.

7\. Next, double-click the file **Install_EMF.bat** to start the installation.

**Note: This installation process can take around 30-40 minutes to finish.** During the installation process, you will be prompted once (see Figure below) to enter the PostgreSQL superuser password, **postgres**.

![][cost_installation]

[cost_installation]: images/CoST_Installation.png

11\. Next, go to the directory containing the EMF client application; this was specified in the batch file via the `EMF_CLIENT_DIRECTORY` variable. Edit the EMFClient.bat batch file to match your computer's settings:

> `set EMF_HOME=`Location of EMF client application (e.g., `C:\Users\Public\EMF` - see `EMF_CLIENT_DIRECTORY` environment from Step 6)

> `set JAVA_EXE=`Location of Java runtime application (e.g., 'C:\Program Files\Java\jre1.8.0\_51\bin\java`, note that the directory is C:\Program Files\Java\jre1.8.0\_51\bin and java is the Java runtime application)

12\. Run the client by double clicking on the bat file EMFClient.bat. Instead of the using the default system login, we recommend creating a new user by clicking the Register New User button as shown in [Figure](#login_to_the_emissions_modeling_framework_window).

## (Optional) Removing CoST Installation Package ##

13\. Go to the root directory where the zip file was installed (e.g., C:\temp\state_install). Remove all files and sub folders from this directory. The original zip package contains a compressed version of the installation package and can be kept for reference purposes. Removing these files and directories will free up around 1.2GB of space.

## Logging into the EMF ##

The CoST application can now be run by going to the EMF client directory and locating the EMFClient.bat file. Double click this file, and you will then be prompted to log in to the system. If the configuration was specified properly and the server is running, you should see a window like [Figure](#login_to_the_emissions_modeling_framework_window).

![Login to the Emissions Modeling Framework Window][login_to_the_emissions_modeling_framework_window]

[login_to_the_emissions_modeling_framework_window]: images/Login_to_the_Emissions_Modeling_Framework_Window.png

If you have never used the EMF before, click the **Register New User** button. You will then see a window that looks like [Figure](#register_new_user_window).

![Register New User Window][register_new_user_window]

[register_new_user_window]: images/Register_New_User_Window.png

In the Register New User window, fill in your full name, affiliation, phone number, and email address. You may then select a username with at least three characters and enter a password with at least 8 characters and at least one digit and then click OK. Once your account has been created, the EMF main window should appear ([Figure](#emf_main_window)).

If instead you are an existing EMF user, enter your EMF username and password in the "Login to the Emissions Modeling Framework" window and click **Log In**. The EMF main window should appear ([Figure](#emf_main_window)).

**Note:** The administrator EMF login name is **admin**, with a password **admin12345**.

![EMF Main Window][emf_main_window]

[emf_main_window]: images/EMF_Main_Window.png

<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch1_introduction.md) - [Home](README.md) - [Next Chapter >>](ch3_control_measure_manager.md)<br>

<!-- END COMMENT -->

