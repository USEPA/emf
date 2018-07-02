Title: Desktop Client
Author: C. Seppanen, UNC
CSS: base.css

# Desktop Client [client_chapter] #

## Requirements ##

The EMF client is a graphical desktop application written in Java. While it is primarily developed and used in Windows, it will run under Mac OS X and Linux (although due to font differences the window layout may not be optimal). The EMF client can be run on Windows XP, Windows 7, or Windows 8.

### Checking Your Java Installation ###

The EMF requires Java 6 or greater. The following instructions will help you check if you have Java installed on your Windows machine and what version is installed. If you need more details, please visit [How to find Java version in Windows](http://www.java.com/en/download/help/version_manual.xml) [java.com].

The latest version(s) of Java on your system will be listed as Java 7 with an associated Update number (eg. Java 7 Update 21). Older versions may be listed as Java(TM), Java Runtime Environment, Java SE, J2SE or Java 2.

**Windows 8**

1. Right-click on the screen at bottom-left corner and choose the **Control Panel** from the pop-up menu.
2. When the Control Panel appears, select **Programs**
3. Click **Programs and Features**
4. The installed Java version(s) are listed.

**Windows 7 and Vista**

1. Click **Start**
2. Select **Control Panel**
3. Select **Programs**
4. Click **Programs and Features**
5. The installed Java version(s) are listed.

**Windows XP**

1. Click **Start**
2. Select **Control Panel**
3. Click the **Add/Remove Programs** control panel icon
4. The Add/Remove control panel displays a list of software on your system, including any Java versions that are on your computer.

[Figure](#programs_control_panel) shows the Programs and Features Control Panel on Windows 7 with Java installed. The installed version of Java is Version 7 Update 45; this version does not need to be updated to run the EMF client.

![Programs and Features Control Panel][programs_control_panel]

[programs_control_panel]: images/programs_control_panel.png

### Installing Java ###

If you need to install Java, please follow the instructions for [downloading and installing Java for a Windows computer] (http://www.java.com/en/download/help/windows_offline_download.xml) [java.com]. Note that you will need administrator privileges to install Java on Windows. During the installation, make a note of the directory where Java is installed on your computer. You will need this information to configure the EMF client.

### Updating Java ###

If Java is installed on your computer but is not version 6 or greater, you will need to update your Java installation. Start by opening the Java Control Panel from the Windows Control Panel. [Figure](#java_control_panel) shows the Java Control Panel.

![Java Control Panel][java_control_panel]

[java_control_panel]: images/java_control_panel.png

Clicking the **About** button will display the Java version dialog seen in [Figure](#java_version). In [Figure](#java_version), the installed version of Java is Version 7 Update 45. This version of Java does not need to be updated to run the EMF client.

![Java Version Dialog][java_version]

[java_version]: images/java_version.png

To update Java, click the tab labeled Update in the Java Control Panel (see [Figure](#java_control_panel_update)). Click the button labeled **Update Now** in the bottom right corner of the Java Control Panel to update your installation of Java.

![Java Control Panel: Update Tab][java_control_panel_update]

[java_control_panel_update]: images/java_control_panel_update.png

## Installing the EMF Client ##

The following instructions are specific to the MARAMA EMF installation.

To get started, please contact MARAMA to request the EMF client package. Click on the folder named EMF\_State. You should see a page similar to [Figure](#dropbox_folder).

![EMF Client Folder on Dropbox][dropbox_folder]

[dropbox_folder]: images/dropbox_folder.png

In the top right corner of the window, click the Download button and choose "Download as .zip". Once the file EMF\_State.zip has finished downloading, open the zip file and drag the folder EMF\_State to your C: drive. You want to end up with the directory C:\EMF\_State as shown in [Figure](#emf_state_folder). You may need administrator privileges to drag the folder to the C: drive.

![EMF Client Folder on Windows][emf_state_folder]

[emf_state_folder]: images/emf_state_folder.png

Next, determine the location where Java is installed on your computer. Depending on the version of your operating system and which version of Java you have, the location might be:

* C:\Program Files\Java\jre6\bin\java
* C:\Program Files\Java\jre7\bin\java
* C:\Program Files (x86)\Java\jre6\bin\java
* C:\Program Files (x86)\Java\jre7\bin\java

If the location of your Java executable is anything other than C:\Program Files\Java\jre6\bin\java, you will need to edit the EMFClient.bat file in the C:\EMF\_State directory. Right click on the EMFClient.bat file and select Edit to open the file in Notepad. You should see a file like [Figure](#edit_emfclient_bat).

![Editing EMFClient.bat File][edit_emfclient_bat]

[edit_emfclient_bat]: images/edit_emfclient_bat.png

Find the line 

`set JAVA_EXE=C:\Program Files\Java\jre6\bin\java`

and update the location to match your Java installation. Save the file and close Notepad.

To launch the EMF client, double-click the file named EMFClient.bat. You may see a security warning similar to [Figure](#security_warning). Uncheck the box labeled "Always ask before opening this file" to avoid the warning in the future.

![EMF Client Security Warning][security_warning]

[security_warning]: images/security_warning.png

## Register as a New User and Log In ##

When you start the EMF client application, you will initially see a login window like [Figure](#login_window).

![Login to the Emissions Modeling Framework Window][login_window]

[login_window]: images/login_window.png

If you are an existing EMF user, enter your EMF username and password in the login window and click the Log In button. If you forget your password, an EMF Administrator can reset it for you. **Note:** The Reset Password button is used to update your password when it expires; it can't be used if you've lost your password. See [Section](#password_expired_section) for more information on password expiration.

If you have never used the EMF before, click the **Register New User** button to bring up the Register New User window as shown in [Figure](#new_user).

![Register New User Window][new_user]

[new_user]: images/new_user.png

In the Register New User window, enter the following information:

* **Name**: Your full name.
* **Affiliation**: Your affiliation. This must be at least 3 characters long.
* **Phone**: Your phone number.
* **Email**: Your email address. Your email address must have the format xx@yy.zz.
* **Username**: Select a username. Your username must be at least three characters long. The EMF will automatically check that the username you choose is unique.
* **Password**: Select a password. Your password must be at least 8 characters long and must contain at least one digit.
* **Confirm Password**: Re-enter your selected password.

Click OK to create your account. If there are any problems with the information you entered, an error message will be displayed at the top of the window as shown in [Figure](#new_user_error).

![Error Registering New User][new_user_error]

[new_user_error]: images/new_user_error.png

Once you have corrected any errors, your account will be created and the EMF main window will be displayed ([Figure](#main_window)).

![EMF Main Window][main_window]

[main_window]: images/main_window.png

## Update Your Profile ##

If you need to update any of your profile information or change your password, click the **Manage** menu and select **My Profile** to bring up the Edit User window shown in [Figure](#edit_user).

![Edit User Profile][edit_user]

[edit_user]: images/edit_user.png

To change your password, enter your new password in the Password field and be sure to enter the same password in the Confirm Password field. Your password must be at least 8 characters long and must contain at least one digit.

Once you have entered any updated information, click the Save button to save your changes and close the Edit User window. You can close the window without saving changes by clicking the Close button. If you have unsaved changes, you will be asked to confirm that you want to discard your changes ([Figure](#discard_changes)).

![Discard Changes Confirmation][discard_changes]

[discard_changes]: images/discard_changes.png

## Password Expiration [password_expired_section] ##

Passwords in the EMF expire every 90 days. If you try to log in and your password has expired, you will see the message "Password has expired. Reset Password." as shown in [Figure](#password_expired).

![Password Expired][password_expired]

[password_expired]: images/password_expired.png

Click the **Reset Password** button to set a new password as shown in [Figure](#reset_password). After entering your new password and confirming it, click the **Save** button to save your new password and you will be logged in to the EMF. Make sure to use your new password next time you log in.

![Reset Expired Password][reset_password]

[reset_password]: images/reset_password.png

## Interface Concepts ##

As you become familiar with the EMF client application, you'll encounter various concepts that are reused through the interface. In this section, we'll briefly introduce these concepts. You'll see specific examples in the following chapters of this guide.

### Viewing vs. Editing ###

First, we'll discuss the difference between **viewing** an item and **editing** an item. Viewing something in the EMF means that you are just looking at it and can't change its information. Conversely, editing an item means that you have the ability to change something. Oftentimes, the interface for viewing vs. editing will look similar but when you're just viewing an item, various fields won't be editable. For example, [Figure](#view_dataset_ch2) shows the Dataset Properties View window while [Figure](#edit_dataset_ch2) shows the Dataset Properties Editor window for the same dataset.

![Viewing a dataset][view_dataset_ch2]

[view_dataset_ch2]: images/view_dataset_ch2.png

![Editing a dataset][edit_dataset_ch2]

[edit_dataset_ch2]: images/edit_dataset_ch2.png

In the edit window, you can make various changes to the dataset like editing the dataset name, selecting the temporal resolution, or changing the geographic region. Clicking the Save button will save your changes. In the viewing window, those same fields are not editable and there is no Save button. Notice in the lower left hand corner of [Figure](#view_dataset_ch2) the button labeled Edit Properties. Clicking this button will bring up the editing window shown in [Figure](#edit_dataset_ch2).

Similarly, [Figure](#view_qa_steps_ch2) shows the QA tab of the Dataset Properties View as compared to [Figure](#edit_qa_steps_ch2) showing the same QA tab but in the Dataset Properties Editor.

![Viewing QA tab][view_qa_steps_ch2]

[view_qa_steps_ch2]: images/view_qa_steps_ch2.png

![Editing QA tab][edit_qa_steps_ch2]

[edit_qa_steps_ch2]: images/edit_qa_steps_ch2.png

In the View window, the only option is to view each QA step whereas the Editor allows you to interact with the QA steps by adding, editing, copying, deleting, or running the steps. If you are having trouble finding an option you're looking for, check to see if you're viewing an item vs. editing it.

### Access Restrictions ###

Only one user can edit a given item at a time. Thus, if you are editing a dataset, you have a "lock" on it and no one else will be able to edit it at the same time. Other users will be able to view the dataset as you're editing it. If you try to edit a locked dataset, the EMF will display a message like [Figure](#dataset_locked). For some items in the EMF, you may only be able to edit the item if you created it or if your account has administrative privileges.

![Dataset Locked Message][dataset_locked]

[dataset_locked]: images/dataset_locked.png

### Unsaved Changes ###

Generally you will need to click the Save button to save changes that you make. If you have unsaved changes and click the Close button, you will be asked if you want to discard your changes as shown in [Figure](#discard_changes). This helps to prevent losing your work if you accidentally close a window.

### Refresh ###

The EMF client application loads data from the EMF server. As you and other users work, your information is saved to the server. In order to see the latest information from other users, the client application needs to refresh its information by contacting the server. The latest data will be loaded from the server when you open a new window. If you are working in an already open window, you may need to click on the Refresh button to load the newest data. [Figure](#highlight_refresh_button) highlights the Refresh button in the Dataset Manager window. Clicking Refresh will contact the server and load the latest list of datasets.

![Refresh button in the Dataset Manager window][highlight_refresh_button]

[highlight_refresh_button]: images/highlight_refresh_button.png

Various windows in the EMF client application have Refresh buttons, usually in either the top right corner as in [Figure](#highlight_refresh_button) or in the row of buttons on the bottom right like in [Figure](#edit_qa_steps_ch2).

You will also need to use the Refresh button if you have made changes and return to a previously opened window. For example, suppose you select a dataset in the Dataset Manager and edit the dataset's name as described in [Section](#dataset_properties_section). When you save your changes, the previously opened Dataset Manager window won't automatically display the updated name. If you close and re-open the Dataset Manager, the dataset's name will be refreshed; otherwise, you can click the Refresh button to update the display.

### Status Window ###

Many actions in the EMF are run on the server. For example, when you run a QA step, the client application on your computer sends a message to the server to start running the step. Depending on the type of QA step, this processing can take a while and so the client will allow you to do other work while it periodically checks with the server to find out the status of your request. These status checks are displayed in the Status Window shown in [Figure](#status_window).

![Status Window][status_window]

[status_window]: images/status_window.png

The status window will show you messages about tasks when they are started and completed. Also, error messages will be displayed if a task could not be completed. You can click the Refresh button in the Status Window to refresh the status. The Trash icon clears the Status Window.

### The Sort-Filter-Select Table [sfs_table_section] ###

Most lists of data within the EMF are displayed using the Sort-Filter-Select Table, a generic table that allows sorting, filtering, and selection (as the name suggests). [Figure](#sfs_table) shows the sort-filter-select table used in the Dataset Manager. (To follow along with the figures, select the main **Manage** menu and then select **Datasets**. In the window that appears, find the **Show Datasets of Type** pull-down menu near the top of the window and select All.)

![Sort-Filter-Select Table][sfs_table]

[sfs_table]: images/sfs_table.png

Row numbers are shown in the first column, while the first row displays column headers. The column labeled Select allows you to select individual rows by checking the box in the column. Selections are used for different activities depending on where the table is displayed. For example, in the Dataset Manager window you can select various datasets and then click the View button to view the dataset properties of each selected dataset. In other contexts, you may have options to change the status of all the selected items or copy the selected items. There are toolbar buttons to allow you to quickly select all items in a table ([Section](#sfs_selectall_section)) and to clear all selections ([Section](#sfs_clear_section)).

The horizontal scroll bar at the bottom indicates that there are more columns in the table than fit in the window. Scroll to the right in order to see all the columns as in [Figure](#sfs_scroll_right).

![Sort-Filter-Select Table with Scrolled Columns][sfs_scroll_right]

[sfs_scroll_right]: images/sfs_scroll_right.png

Notice the info line displayed at the bottom of the table. In [Figure](#sfs_scroll_right) the line reads **35 rows : 12 columns: 0 Selected [Filter: None, Sort: None]**. This line gives information about the total number of rows and columns in the table, the number of selected items, and any filtering or sorting applied.

Columns can be resized by clicking on the border between two column headers and dragging it right or left. Your mouse cursor will change to a horizontal double-headed arrow when resizing columns.

You can rearrange the order of the columns in the table by clicking a column header and dragging the column to a new position. [Figure](#sfs_reorder_columns) shows the sort-filter-select table with columns rearranged and resized.

![Sort-Filter-Select Table with Rearranged and Resized Columns][sfs_reorder_columns]

[sfs_reorder_columns]: images/sfs_reorder_columns.png

To sort the table using data from a given column, click on the column header such as Last Modified Date. [Figure](#sfs_simple_sort) shows the table sorted by Last Modified Date in descending order (latest dates first). The table info line now includes **Sort: Last Modified Date(-)**.

![Sort-Filter-Select Table with Column Sort][sfs_simple_sort]

[sfs_simple_sort]: images/sfs_simple_sort.png

If you click the Last Modified Date header again, the table will re-sort by Last Modified Date in ascending order (earliest dates first). The table info line also changes to **Sort: Last Modified Date(+)** as seen in [Figure](#sfs_simple_sort_reversed).

![Sort-Filter-Select Table with Reversed Column Sort][sfs_simple_sort_reversed]

[sfs_simple_sort_reversed]: images/sfs_simple_sort_reversed.png

The toolbar at the top of the table (as shown in [Figure](#sort_filter_select)) has buttons for the following actions (from left to right):

![Toolbar for Sort-Filter-Select Table][sort_filter_select]

[sort_filter_select]: images/sort_filter_select.png

1. Sort options
2. Filter rows
3. Show or hide columns
4. Format data in columns
5. Reset table's sorting, filtering, and column layout
6. Select all rows
7. Clear all selections

If you hover your mouse over any of the buttons, a tooltip will pop up to remind you of each button's function.

### Sort Options ###

![][sfs_toolbar_sort]

[sfs_toolbar_sort]: images/sfs_toolbar_sort.png

The Sort toolbar button brings up the Sort Columns dialog as shown in [Figure](#sort_columns). This dialog allows you to sort the table by multiple columns and also allows case sensitive sorting. (Quick sorting by clicking a column header uses case insensitive sorting.)

![Sort Columns Dialog][sort_columns]

[sort_columns]: images/sort_columns.png

In the Sort Columns Dialog, select the first column you would use to sort the data from the Sort By pull-down menu. You can also specify if the sort order should be ascending or descending and if the sort comparison should be case sensitive.

To add additional columns to sort by, click the Add button and then select the column in the new Then Sort By pull-down menu. When you have finished setting up your sort selections, click the OK button to close the dialog and re-sort the table. The info line beneath the table will show all the columns used for sorting like **Sort: Creator(+), Last Modified Date(-)**.

To remove your custom sorting, click the Clear button in the Sort Columns dialog and then click the OK button. You can also use the Reset toolbar button to reset all custom settings as described in [Section](#sfs_reset_section).

### Filter Rows [sfs_filter_section] ###

![][sfs_toolbar_filter]

[sfs_toolbar_filter]: images/sfs_toolbar_filter.png

The Filter Rows toolbar button brings up the Filter Rows dialog as shown in [Figure](#filter_rows). This dialog allows you to create filters to "whittle down" the rows of data shown in the table. You can filter the table's rows based on any column with several different value matching options.

![Filter Rows Dialog][filter_rows]

[filter_rows]: images/filter_rows.png

To add a filter criterion, click the Add Criteria button and a new row will appear in the dialog window. Clicking the cell directly under the Column Name header displays a pull-down menu to pick which column you would like use to filter the rows. The Operation column allows you to select how the filter should be applied; for example, you can filter for data that starts with the given value or does not contain the value. Finally, click the cell under the Value header and type in the value to use. Note that the filter values are case-sensitive. A filter value of "nonroad" would *not* match the dataset type "ORL Nonroad Inventory".

If you want to specify additional criteria, click Add Criteria again and follow the same process. To remove a filter criterion, click on the row you want to remove and then click the Delete Criteria button.

If the radio button labeled Match using: is set to ALL criteria, then only rows that match all the specified criteria will be shown in the filtered table. If Match using: is set to ANY criteria, then rows will be shown if they meet any of the criteria listed.

Once you are done specifying your filter options, click the OK button to close the dialog and return to the filtered table. The info line beneath the table will include your filter criteria like **Filter: Creator contains rhc, Temporal Resolution starts with Ann**.

To remove your custom filtering, you can delete the filter criteria from the Filter Rows dialog or uncheck the Apply Filter? checkbox to turn off the filtering without deleting your filter rules. You can also use the Reset toolbar button to reset all custom settings as described in [Section](#sfs_reset_section). Note that clicking the Reset button will delete your filter rules.

### Show or Hide Columns [sfs_showhide_section] ###

![][sfs_toolbar_showhide]

[sfs_toolbar_showhide]: images/sfs_toolbar_showhide.png

The Show/Hide Columns toolbar button brings up the Show/Hide Columns dialog as shown in [Figure](#show_hide_columns). This dialog allows you to customize which columns are displayed in the table.

![Show/Hide Columns Dialog][show_hide_columns]

[show_hide_columns]: images/show_hide_columns.png

To hide a column, uncheck the box next to the column name under the Show? column. Click the OK button to return to the table. The columns you unchecked will no longer be seen in the table. The info line beneath the table will also be updated with the current number of displayed columns.

To make a hidden column appear again, open the Show/Hide Columns dialog and check the Show? box next to the hidden column's name. Click OK to close the Show/Hide Columns dialog.

To select multiple columns to show or hide, click on the first column name of interest. Then hold down the Shift key and click a second column name to select it and the intervening columns. Once rows are selected, clicking the Show or Hide buttons in the middle of the dialog will check or uncheck all the Show? boxes for the selected rows. To select multiple rows that aren't next to each other, you can hold down the Control key while clicking each row. The Invert button will invert the selected rows. After checking/unchecking the Show? checkboxes, click OK to return to the table with the columns shown/hidden as desired.

The Show/Hide Columns dialog also supports filtering to find columns to show or hide. This is an infrequently used option most useful for locating columns to show or hide when there are many columns in the table. [Figure](#sfs_showhide_filter) shows an example where a filter has been set up to match column names that contain the value "Date". Clicking the Select button above the filtering options selects matching rows which can then be hidden by clicking the Hide button.

![Show/Hide Columns with Column Name Filter][sfs_showhide_filter]

[sfs_showhide_filter]: images/sfs_showhide_filter.png

### Format Data in Columns ###

![][sfs_toolbar_format]

[sfs_toolbar_format]: images/sfs_toolbar_format.png

The Format Columns toolbar button displays the Format Columns dialog show in [Figure](#format_columns). This dialog allows you to customize the formatting of columns. In practice, this dialog is not used very often but it can be helpful to format numeric data by changing the number of decimal places or the number of significant digits shown.

![Format Columns Dialog][format_columns]

[format_columns]: images/format_columns.png

To change the format of a column, first check the checkbox next to the column name in the Format? column. If you only select columns that contain numeric data, the Numeric Format Options section of the dialog will appear; otherwise, it will not be visible. The Format Columns dialog supports filtering by column name similar to the Show/Hide Columns dialog ([Section](#sfs_showhide_section)).

From the Format Columns dialog, you can change the font, the style of the font (e.g. bold, italic), the horizontal alignment for the column (e.g. left, center, right), the text color, and the column width. For numeric columns, you can specify the number of significant digits and decimal places.

### Reset Table [sfs_reset_section] ###

![][sfs_toolbar_reset]

[sfs_toolbar_reset]: images/sfs_toolbar_reset.png

The Reset toolbar button will remove all customizations from the table: sorting, filtering, hidden columns, and formatting. It will also reset the column order and set column widths back to the default.

### Select All Rows [sfs_selectall_section] ###

![][sfs_toolbar_selectall]

[sfs_toolbar_selectall]: images/sfs_toolbar_selectall.png

The Select All toolbar button selects all the rows in the table. After clicking the Select All button, you will see that the checkboxes in the Select column are now all checked. You can select or deselect an individual item by clicking its checkbox in the Select column.

### Clear All Selections [sfs_clear_section] ###

![][sfs_toolbar_clear]

[sfs_toolbar_clear]: images/sfs_toolbar_clear.png

The Clear All Selections toolbar button unselects all the rows in the table.
