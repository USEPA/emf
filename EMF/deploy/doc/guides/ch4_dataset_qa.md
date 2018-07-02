# Dataset Quality Assurance [qa_chapter] #

## Introduction ##

The EMF allows you to perform various types of analyses on a dataset or set of datasets. For example, you can summarize the data by different aspects such as geographic region like county or state, SCC code, pollutant, or plant ID. You can also compare or sum multiple datasets. Within the EMF, running an analysis like this is called a QA step.

A dataset can have many QA steps associated with it. To view a dataset's QA steps, first select the dataset in the Dataset Manager and click the Edit Properties button. Switch to the QA tab to see the list of QA steps as in [Figure](#dataset_qa_steps).

![QA Steps for a Dataset][dataset_qa_steps]

[dataset_qa_steps]: images/dataset_properties_qa_nonroad.png

At the bottom of the window you will see a row of buttons for interacting with the QA steps starting with Add from Template, Add Custom, Edit, etc. If you do not see these buttons, make sure that you are editing the dataset's properties and not just viewing them.

## Add QA Step From Template ##

Each dataset type can have predefined QA steps called QA Step Templates. QA step templates can be added to a dataset type and configured by EMF Administrators using the Dataset Type Manager (see [Section](#dataset_types_section)). QA step templates are easy to run for a dataset because they've already been configured.

To see a list of available QA step templates for your dataset, open your dataset's QA tab in the Dataset Properties Editor ([Figure](#dataset_qa_steps)). Click the Add from Template button to open the Add QA Steps dialog. [Figure](#add_qa_step_template_nonroad) shows the available QA step templates for an ORL Nonroad Inventory.

![Add QA Steps From Template][add_qa_step_template_nonroad]

[add_qa_step_template_nonroad]: images/add_qa_step_template_nonroad.png

The ORL Nonroad Inventory has various QA step templates for generating different summaries of the inventory.

* List Data Source Codes and U.S. State with Descriptions
* Summarize by County and Pollutant
* Summarize by Data Source Code, U.S. State and Pollutant with Descriptions
* Summarize by Missing PM CEFF
* Summarize by Pollutant
* Summarize by Pollutant with Descriptions
* Summarize by SCC and Pollutant
* Summarize by SCC and Pollutant with Descriptions
* Summarize by U.S. County and Pollutant with Descriptions
* Summarize by U.S. State and Pollutant
* Summarize by U.S. State and Pollutant with Descriptions
* Summarize by U.S. State, SCC and Pollutant with Descriptions

Summaries "with Descriptions" include more information than those without. For example, the results of the "Summarize by SCC and Pollutant with Descriptions" QA step will include the descriptions of the SCCs and pollutants. Because these summaries with descriptions need to retrieve data from additional tables, they are a bit slower to generate compared to summaries without descriptions.

Select a summary of interest (for example, Summarize by County and Pollutant) by clicking the QA step name. If your dataset has more than one version, you can choose which version to summarize using the Version pull-down menu at the top of the window. Click OK to add the QA step to the dataset.

The newly added QA step is now shown in the list of QA steps for the dataset ([Figure](#qa_steps_with_new_step)).

![QA Steps with New Step Added][qa_steps_with_new_step]

[qa_steps_with_new_step]: images/qa_steps_with_new_step.png

To see the details of the QA step, select the step and click the Edit button. This brings up the Edit QA Step window like [Figure](#edit_new_qa_step_from_template).

![Edit New QA Step from Template][edit_new_qa_step_from_template]

[edit_new_qa_step_from_template]: images/edit_new_qa_step_from_template.png

The QA step name is shown at the top of the window. This name was automatically set by the QA step template. You can edit this name if needed to distinguish this step from other QA steps.

The Version pull-down menu shows which version of the data this QA step will run on.

The pull-down menu to the right of the Version setting indicates what type of program will be used for this QA step. In this case, the program type is "SQL" indicating that the results of this QA step will be generated using a SQL query. Most of the summary QA steps are generated using SQL queries. The EMF allows other types of programs to be run as QA steps including Python scripts and various built-in analyses like converting average-day emissions to an annual inventory.

The Arguments textbox shows the arguments used by the QA step program. In this case, the QA step is a SQL query and the Arguments field shows the query that will be run. The special SQL syntax used for QA steps is discussed in [Section](#qa_step_sql_syntax_section).

Other items of interest in the Edit QA Step window include the description and comment textboxes where you can enter a description of your QA step and any comments you have about running the step.

The QA Status field shows the overall status of the QA step. Right now the step is listed as "Not Started" because it hasn't been run yet. Once the step has been run, the status will automatically change to "In Progress". After you've reviewed the results, you can mark the step as "Complete" for future reference.

The Edit QA Step window also includes options for exporting the results of a QA step to a file. This is described in [Section](#export_qa_results_section).

At this point, the next step is to actually run the QA step as described in [Section](#running_steps_section).

## Adding Custom QA Steps [add_custom_qa_section] ##

In addition to using QA steps from templates, you can define your own custom QA steps. From the QA tab of the Dataset Properties Editor ([Figure](#dataset_qa_steps)), click the Add Custom button to bring up the Add Custom QA Step dialog as shown in [Figure](#add_custom_qa_step).

![Add Custom QA Step Dialog][add_custom_qa_step]

[add_custom_qa_step]: images/add_custom_qa_step.png

In this dialog, you can configure your custom QA step by entering its name, the program to use, and the program's arguments.

Creating a custom QA step from scratch is an advanced feature. Oftentimes, you can start by copying an existing step and tweaking it through the Edit QA Step interface. 

[Section](#avg_day_to_ann_section) shows how to create a custom QA step that uses the built-in QA program "Average day to Annual Inventory" to calculate annual emissions from average-day emissions. [Section](#compare_datasets_section) demonstrates using the Compare Datasets QA program to compare two inventories. [Section](#create_custom_sql_qa_section) gives an example of creating a custom QA step based on a SQL query from an existing QA step.

## Running QA Steps [running_steps_section] ##

To run a QA step, open the QA tab of the Dataset Properties Editor and select the QA step you want to run as shown in [Figure](#select_qa_step_to_run).

![Select a QA Step to Run][select_qa_step_to_run]

[select_qa_step_to_run]: images/select_qa_step_to_run.png

Click the Run button at the bottom of the window to run the QA step. You can also run a QA step from the Edit QA Step window. The Status window will display messages when the QA step begins running and when it completes:

> Started running QA step 'Summarize by County and Pollutant' for Version 'Initial Version' of Dataset 'nonroad_caps_2005v2_jul_orl_nc.txt'

> Completed running QA step 'Summarize by County and Pollutant' for Version 'Initial Version' of Dataset 'nonroad_caps_2005v2_jul_orl_nc.txt'

In the QA tab, click the Refresh button to update the table of QA steps as shown in [Figure](#refreshed_qa_steps).

![Refreshed QA Steps][refreshed_qa_steps]

[refreshed_qa_steps]: images/refreshed_qa_steps.png

The overall QA step status (the QA Status column) has changed from "Not Started" to "In Progress" and the Run Status is now "Success". The list of QA steps also shows the time the QA step was run in the When column.

To view the results of the QA step, select the step in the QA tab and click the View Results button. A dialog like [Figure](#view_qa_results_records) will pop-up asking how many records of the results you would like to preview.

![View QA Results: Select Number of Records][view_qa_results_records]

[view_qa_results_records]: images/view_qa_results_records.png

Enter the number of records to view or click the View All button to see all records. The View QA Step Results window will display the results of the QA step as shown in [Figure](#view_qa_results).

![View QA Results][view_qa_results]

[view_qa_results]: images/view_qa_results.png

## Exporting QA Step Results [export_qa_results_section] ##

In addition to viewing the results of a QA step in the EMF client application, you can export the results as a comma-separated values (CSV) file. CSV files can be directly opened by Microsoft Excel or other spreadsheet programs to make charts or for further analysis.

To export the results of a QA step, select the QA step of interest in the QA tab of the Dataset Properties Editor. Then click the Edit button to bring up the Edit QA Step window as shown in [Figure](#export_qa_step_results).

![Export QA Step Results][export_qa_step_results]

[export_qa_step_results]: images/export_qa_step_results.png

Typically, you will want to check the **Download result file to local machine?** checkbox so the exported file will automatically be downloaded to your local machine. You can type in a name for the exported file in the Export Name field. Then click the Export button. If you did not enter an Export Name, the application will confirm that you want to use an auto-generated name with the dialog shown in [Figure](#no_export_name).

![Export Name Not Specified][no_export_name]

[no_export_name]: images/no_export_name.png

Next, you'll see the Export QA Step Results customization window ([Figure](#export_qa_step_results_2)).

![Export QA Step Results Customization Window][export_qa_step_results_2]

[export_qa_step_results_2]: images/export_qa_step_results_2.png

The Row Filter textbox allows you to limit which rows of the QA step results to include in the exported file. [Table](#row_filter_syntax_table) provides some examples of the syntax used by the row filter. Available Columns lists the column names from the results that could be used in a row filter. In [Figure](#export_qa_step_results_2), the columns `fips`, `poll`, and `ann_emis` are available. To export only the results for counties in North Carolina (state FIPS code = 37), the row filter would be `fips like '37%'`.

Click the Finish button to start the export. At the top of the Edit QA Step window, you'll see the message "Started Export. Please monitor the Status window to track your export request." like [Figure](#export_qa_step_results_started)

![Export QA Step Results Started][export_qa_step_results_started]

[export_qa_step_results_started]: images/export_qa_step_results_started.png

Once your export is complete, you will see a message in the Status window like

> Completed exporting QA step 'Summarize by SCC and Pollutant' for Version 'Initial Version' of Dataset 'nonpt\_pf4\_cap\_nopfc\_2017ct\_nc\_sc\_va' to \<server directory\>avg\_day\_scc\_poll\_summary.csv.  The file will start downloading momentarily, see the Download Manager for the download status.

You can bring up the Downloads window as shown in [Figure](#downloads_qa_step_results) by opening the **Window** menu at the top of the EMF main window and selecting **Downloads**.

![Downloads Window: QA Step Results][downloads_qa_step_results]

[downloads_qa_step_results]: images/downloads_qa_step_results.png

As your file is downloading, the progress bar on the right side of the window will update to show you the progress of the download. Once it reaches 100%, your download is complete. Right click on the filename in the Downloads window and select **Open Containing Folder** to open the folder where the file was downloaded.

If you have Microsoft Excel or another spreadsheet program installed, you can double-click the downloaded CSV file to open it.

## Exporting KMZ Files ##

QA step results that include latitude and longitude information can be mapped with geographic information systems (GIS), mapping tools, and Google Earth. Many summaries that have "with Descriptions" in their names include latitude and longitude values. For plant-level summaries, the latitude and longitude in the output are the average of all the values for the specific combination of FIPS and plant ID. For county- and state-level summaries, the latitude and longitude are the centroid values specified in the "fips" table of the EMF reference schema.

To export a KMZ file that can be loaded into Google Earth, you will first need to view the results of the QA step. You can view a QA step's results by either selecting the QA step in the QA tab of the Dataset Properties Editor (see [Figure](#dataset_qa_steps)) and then clicking the View Results button, or you can click View Results from the Edit QA Step window. [Figure](#view_qa_step_results_latlon) shows the View QA Step Results window for a summary by county and pollutant with descriptions. The summary includes latitude and longitude values for each county.

![View QA Step Results with Latitude and Longitude Values][view_qa_step_results_latlon]

[view_qa_step_results_latlon]: images/view_qa_step_results_latlon.png

From the **File** menu in the top left corner of the View QA Step Results window, select **Google Earth**. Make sure to look at the File menu for the View QA Step Results window, not the main EMF application. The Create Google Earth file window will be displayed as shown in [Figure](#create_google_earth_file).

![Create Google Earth File][create_google_earth_file]

[create_google_earth_file]: images/create_google_earth_file.png

In the Create Google Earth file window, the Label Column pull-down menu allows you to select which column will be used to label the points in the KMZ file. This label will appear when you mouse over a point in Google Earth. For a plant summary, this would typically be "plant_name"; county or state summaries would use "county" or "state_name" respectively.

If your summary has data for multiple pollutants, you will often want to specify a filter so that data for only one pollutant is included in the KMZ file. To do this, specify a Filter Column (e.g. "poll") and then type in a Filter Value (e.g. "EVP__VOC").

The Data Column pull-down menu specifies the column to use for the value displayed when you mouse over a point in Google Earth such as annual emissions ("ann_emis"). The mouse over information will have the form: \<value from Label Column\> : \<value from Data Column\>.

The Maximum Data Cutoff and Minimum Data Cutoff fields allow you to exclude data points above or below certain thresholds.

If you want to control the size of the points, you can adjust the value of the Icon Scale setting between 0 and 1. The default setting is 0.3; values smaller than 0.3 result in smaller circles and values larger than 0.3 will result in larger circles.

Tooltips are available for all of the settings in the Create Google Earth file window by mousing over each field.

Once you have specified your settings, click the Generate button to create the KMZ file. The location of the generated file is shown in the Output File field. If your computer has Google Earth installed, you can click the Open button to open the file in Google Earth.

If you find that you need to repeatedly create similar KMZ files, you can save your settings to a file by clicking the Save button. The next time you need to generate a Google Earth file, click the Load button next to the Properties File field to load your saved settings.

## Average Day to Annual Inventory QA Program [avg_day_to_ann_section] ##

In addition to analyzing individual datasets, the EMF can run QA steps that use multiple datasets. In this section, we'll show how to create a custom QA step that calculates an annual inventory from 12 month-specific average-day emissions inventories.

To get started, we'll need to select a dataset to associate the QA step with. As a best practice, add the QA step to the January-specific dataset in the set of 12 month-specific files. This isn't required by the EMF but it can make finding multi-file QA steps easier later on. If you have more than 12 month-specific files to use (e.g. 12 non-California inventories and 12 California inventories), add the QA step to the "main" January inventory file (e.g. the non-California dataset).

After determining which dataset to add the QA step to, create a new custom QA step as described in [Section](#add_custom_qa_section). [Figure](#custom_qa_step_avg_day) shows the Add Custom QA Step dialog. We've entered a name for the step and used the **Program** pull-down menu to select "Average day to Annual Inventory".

![Add Custom QA Step Using Average Day to Annual Inventory QA Program][custom_qa_step_avg_day]

[custom_qa_step_avg_day]: images/custom_qa_step_avg_day.png

"Average day to Annual Inventory" is a QA program built into the EMF that takes a set of average-day emissions inventories as input and outputs an annual inventory by calculating monthly total emissions and summing all months. Click the OK button in the Add Custom QA Step dialog to save the new QA step. We'll enter the QA program arguments in a minute. Back in the QA tab of the Dataset Properties Editor, select the newly created QA step and click Edit to open the Edit QA Step window shown in [Figure](#edit_custom_qa_step).

![Edit Custom QA Step][edit_custom_qa_step]

[edit_custom_qa_step]: images/edit_custom_qa_step.png

We need to define the arguments that will be sent to the QA program that this QA step will run. The QA program is "Average day to Annual Inventory" so the arguments will be a list of month-specific inventories. Click the **Set** button to the right of the Arguments box to open the Set Inventories dialog as shown in [Figure](#custom_qa_step_set_inventories).

![Set Inventories for Average Day to Annual Inventory QA Program][custom_qa_step_set_inventories]

[custom_qa_step_set_inventories]: images/custom_qa_step_set_inventories.png

The Set Inventories dialog is specific to the "Average day to Annual Inventory" QA program. Other QA programs have different dialogs for setting up their arguments. The January inventory that we added the QA step to is already listed. We need to add the other 11 month-specific inventory files. Click the Add button to open the Select Datasets dialog shown in [Figure](#custom_qa_step_choose_invs).

![Select Datasets for QA Program][custom_qa_step_choose_invs]

[custom_qa_step_choose_invs]: images/custom_qa_step_choose_invs.png

In the Select Datasets dialog, the dataset type is automatically set to ORL Nonroad Inventory (ARINV) matching our January inventory. The other ORL nonroad inventory datasets are shown in a list. We can use the **Dataset name contains:** field to enter a search term to narrow the list. We're using 2005 inventories so we'll enter `2005` as our search term to match only those datasets whose name contains "2005". Then we'll select all the inventories in the list as shown in [Figure](#custom_qa_step_choose_invs_2). 

Select inventories by clicking on the dataset name. You can select a range of datasets by clicking on the first dataset you want to select in the list. Then hold down the Shift key while clicking on the last dataset you want to select. All of the datasets in between will also be selected. If you hold down the Ctrl key while clicking on datasets, you can select multiple items from the list that aren't next to each other.

![Select Filtered Datasets for QA Program][custom_qa_step_choose_invs_2]

[custom_qa_step_choose_invs_2]: images/custom_qa_step_choose_invs_2.png

Click the OK button in the Select Datasets dialog to save the selected inventories and return to the Set Inventories dialog. As shown in [Figure](#custom_qa_step_invs_set), the list of emission inventories now contains all 12 month-specific datasets.

![Inventories for Average Day to Annual Inventory QA Program][custom_qa_step_invs_set]

[custom_qa_step_invs_set]: images/custom_qa_step_invs_set.png

Click the OK button in the Set Inventories dialog to return to the Edit QA Step window shown in [Figure](#custom_qa_step_arguments). The Arguments textbox now lists the 12 month-specific inventories and the flag (-inventories) needed for the "Average day to Annual Inventory" QA program.

![Custom QA Step with Arguments Set][custom_qa_step_arguments]

[custom_qa_step_arguments]: images/custom_qa_step_arguments.png

Click the Save button at the bottom of the Edit QA Step window to save the QA step. This QA step can now be run as described in [Section](#running_steps_section).

## Compare Datasets QA Program [compare_datasets_section] ##

The Compare Datasets QA program allows you to aggregate and compare datasets using a variety of grouping options. You can compare datasets with the same dataset type or different types. In this section, we'll set up a QA step to compare the average day emissions from two ORL nonroad inventories by SCC and pollutant.

First, we'll select a dataset to associate the QA step with. In this example, we'll be comparing January and February emissions using the January dataset as the base inventory. The EMF doesn't dictate which dataset should have the QA step associated with it so we'll choose the base dataset as a convention. From the Dataset Manager, select the January inventory (shown in [Figure](#qa_compare_dataset_manager)) and click the **Edit Properties** button.

![Select Dataset to Add QA Step][qa_compare_dataset_manager]

[qa_compare_dataset_manager]: images/qa_compare_dataset_manager.png

Open the QA tab (shown in [Figure](#qa_compare_qa_tab)) and click **Add Custom** to add a new QA step.

![Dataset Editor QA Tab for Selected Dataset][qa_compare_qa_tab]

[qa_compare_qa_tab]: images/qa_compare_qa_tab.png

In the Add Custom QA Step dialog shown in [Figure](#qa_compare_add_custom_step), enter a name for the new QA step like "Compare to February". Use the **Program** pull-down menu to select the QA program "Compare Datasets".

![Select QA Program for New QA Step][qa_compare_add_custom_step]

[qa_compare_add_custom_step]: images/qa_compare_add_custom_step.png

You can enter a description of the QA step as shown in [Figure](#qa_compare_add_custom_step_description). Then click **OK** to save the QA step. We'll be setting up the arguments to the Compare Datasets QA program in just a minute.

![Add Description to New QA Step][qa_compare_add_custom_step_description]

[qa_compare_add_custom_step_description]: images/qa_compare_add_custom_step_description.png

Back in the QA tab of the Dataset Properties Editor, select the newly created QA step and click the **Edit** button (see [Figure](#qa_compare_select_new_step)).

![Select New QA Step from QA Tab][qa_compare_select_new_step]

[qa_compare_select_new_step]: images/qa_compare_select_new_step.png

In the Edit QA Step window (shown in [Figure](#qa_compare_edit_step)), click the **Set** button to the right of the **Arguments** textbox.

![Edit New QA Step][qa_compare_edit_step]

[qa_compare_edit_step]: images/qa_compare_edit_step.png

A custom dialog is displayed ([Figure](#qa_compare_set_arguments)) to help you set up the arguments needed by the Compare Datasets QA program.

![Set Up Compare Datasets QA Step][qa_compare_set_arguments]

[qa_compare_set_arguments]: images/qa_compare_set_arguments.png

To get started, we'll set the base datasets. Click the **Add** button underneath the **Base Datasets** area to bring up the Select Datasets dialog shown in [Figure](#qa_compare_select_base_dataset).

![Select Base Datasets][qa_compare_select_base_dataset]

[qa_compare_select_base_dataset]: images/qa_compare_select_base_dataset.png

Select one or more datasets to use as the base datasets in the comparison. For this example, we'll select the January inventory by clicking on the dataset name. Then click **OK** to close the dialog and return to the setup dialog. The setup dialog now shows the selected base dataset as in [Figure](#qa_compare_base_dataset_set).

![Base Dataset Set for Compare Datasets][qa_compare_base_dataset_set]

[qa_compare_base_dataset_set]: images/qa_compare_base_dataset_set.png

Next, we'll add the dataset we want to compare against by clicking the **Add** button underneath the **Compare Datasets** area. The Select Datasets dialog is displayed like in [Figure](#qa_compare_select_compare_dataset). We'll select the February inventory and click the **OK** button.

![Select Compare Datasets][qa_compare_select_compare_dataset]

[qa_compare_select_compare_dataset]: images/qa_compare_select_compare_dataset.png

Returning to the setup dialog, the comparison dataset is now set as shown in [Figure](#qa_compare_compare_dataset_set).

![Compare Dataset Set for Compare Datasets][qa_compare_compare_dataset_set]

[qa_compare_compare_dataset_set]: images/qa_compare_compare_dataset_set.png

The list of base and comparison datasets includes which version of the data will be used in the QA step. For example, the base dataset **2007JanORLTotMARAMAv3.txt [0 (Initial Version)]** indicates that version 0 (named "Initial Version") will be used. When you select the base and comparison datasets, the EMF automatically uses each dataset's Default Version. If any of the datasets have a different version that you would like to use for the QA step, select the dataset name and then click the **Set Version** button underneath the selected dataset. The Set Version dialog shown in [Figure](#qa_compare_set_version) lets you pick which version of the dataset you would like to use.

![Set Dataset Version for Compare Datasets QA Program][qa_compare_set_version]

[qa_compare_set_version]: images/qa_compare_set_version.png

Next, we need to tell the Compare Datasets QA program how to compare the two datasets. We're going to sum the average-day emissions in each dataset by SCC and pollutant and then compare the results from January to February. In the ORL Nonroad Inventory dataset type, the SCCs are stored in a field called `scc`, the pollutant codes are stored in a column named `poll`, and the average-day emissions are stored in a field called `avd_emis`. In the **Group By Expressions** textbox, type `scc`, press **Enter**, and then type `poll`. In the **Aggregate Expressions** textbox, type `avd_emis`. [Figure](#qa_compare_arguments_set) shows the setup dialog with the arguments entered.

![Arguments Set for Compare Datasets][qa_compare_arguments_set]

[qa_compare_arguments_set]: images/qa_compare_arguments_set.png

In this example, we're comparing two datasets of the same type (ORL Nonroad Inventory). This means that the data field names will be consistent between the base and comparison datasets. When you compare datasets with different types, the field names might not match. The **Matching Expressions** textbox allows you to define how the fields from the base dataset should be matched to the comparison dataset. For this case, we don't need to enter anything in the **Matching Expressions** textbox or any of the remaining fields in the setup dialog. The Compare Datasets arguments are described in more detail in [Section](#compare_datasets_details_section).

In the setup dialog, click **OK** to save the arguments and return to the Edit QA Step window. The **Arguments** textbox now lists the arguments that we set up in the previous step (see [Figure](#qa_compare_step_ready)).

![QA Step with Arguments Set][qa_compare_step_ready]

[qa_compare_step_ready]: images/qa_compare_step_ready.png

The QA step is now ready to run. Click the **Run** button to start running the QA step. A message is displayed at the top of the window as shown in [Figure](#qa_compare_step_running).

![Started Running QA Step][qa_compare_step_running]

[qa_compare_step_running]: images/qa_compare_step_running.png

In the Status window, you'll see a message about starting to run the QA step followed by a completion message once the QA step has finished running. [Figure](#qa_compare_status_window) shows the two status messages.

![QA Step Running in Status Window][qa_compare_status_window]

[qa_compare_status_window]: images/qa_compare_status_window.png

Once the status message 

> Completed running QA step 'Compare to February' for Version 'Initial Version' of Dataset '2007JanORLTotMARAMAv3.txt'

is displayed, the QA step has finished running. In the Edit QA Step window, click the **Refresh** button to display the latest information about the QA step. The fields **Run Status** and **Run Date** will be populated with the latest run information as shown in [Figure](#qa_compare_step_run_complete).

![QA Step with Run Status][qa_compare_step_run_complete]

[qa_compare_step_run_complete]: images/qa_compare_step_run_complete.png

Now, we can view the QA step results or export the results. First, we'll view the results inside the EMF client. Click the **View Results** button to open the View QA Step Results window as shown in [Figure](#qa_compare_view_results).

![View Compare Datasets QA Step Results][qa_compare_view_results]

[qa_compare_view_results]: images/qa_compare_view_results.png

[Table](#qa_compare_columns_table) describes each column in the QA step results.

Column Name|Description
-|-
`poll`|Pollutant code
`scc`|SCC code
`avd_emis_b`|Summed average-day emissions from base dataset (January) for this pollutant and SCC
`avd_emis_c`|Summed average-day emissions from comparison dataset (February) for this pollutant and SCC
`avd_emis_diff`|`avd_emis_c - avd_emis_b`
`avd_emis_absdiff`|Absolute value of `avd_emis_diff`
`avd_emis_pctdiff`|`100 * (avd_emis_diff / avd_emis_b)`
`avd_emis_abspctdiff`|Absolute value of `avd_emis_pctdiff`
`count_b`|Number of records from base dataset included in this row's results
`count_c`|Number of records from comparison dataset included in this row's results
[QA Step Results Columns][qa_compare_columns_table]

To export the QA step results, return to the Edit QA Step window as shown in [Figure](#qa_compare_ready_export). Select the checkbox labeled **Download result file to local machine?**. In this example, we have entered an optional **Export Name** for the output file. If you don't enter an **Export Name**, the output file will use an auto-generated name. Click the **Export** button.

![Ready to Export QA Step Results][qa_compare_ready_export]

[qa_compare_ready_export]: images/qa_compare_ready_export.png

The Export QA Step Results dialog will be displayed as shown in [Figure](#qa_compare_export_options). For more information about the **Row Filter** option, see [Section](#export_qa_results_section). To export all the result records, click the **Finish** button.

![Export QA Step Results Options][qa_compare_export_options]

[qa_compare_export_options]: images/qa_compare_export_options.png

Back in the Edit QA Step window, a message is displayed at the top of the window indicating that the export has started. See [Figure](#qa_compare_export_started).

![Export Started for QA Step Results][qa_compare_export_started]

[qa_compare_export_started]: images/qa_compare_export_started.png

Check the Status window to see the status of the export as shown in [Figure](#qa_compare_export_status_window).

![Export Messages in Status Window][qa_compare_export_status_window]

[qa_compare_export_status_window]: images/qa_compare_export_status_window.png

Once the export is complete, the file will start downloading to your computer. Open the Downloads window to check the download status. Once the progress bar reaches 100%, the download is complete. Right click on the results file and select **Open Containing Folder** as shown in [Figure](#qa_compare_open_download).

![QA Step Results in Downloads Window][qa_compare_open_download]

[qa_compare_open_download]: images/qa_compare_open_download.png

[Figure](#qa_compare_containing_folder) shows the downloaded file in Windows Explorer. By default, files are downloaded to a temporary directory on your computer. Some disk cleanup programs can automatically delete files in temporary directories; you should move any downloads you want to keep to a more permanent location on your computer.

![Downloaded QA Step Results in Windows Explorer][qa_compare_containing_folder]

[qa_compare_containing_folder]: images/qa_compare_containing_folder.png

The downloaded file is a CSV (comma-separated values) file which can be opened in Microsoft Excel or other spreadsheet programs. Double-click the filename to open the file. [Figure](#qa_compare_excel) shows the QA step results in Microsoft Excel.

![Downloaded QA Step Results in Microsoft Excel][qa_compare_excel]

[qa_compare_excel]: images/qa_compare_excel.png

### Details of Compare Datasets Arguments [compare_datasets_details_section] ###

#### Group By Expressions ####

The Group By Expressions are a list of columns/expressions that are used to group the dataset records for aggregation. The expressions must contain valid columns from either the base or comparison datasets. If a column exists only in the base or compare dataset, then a Matching Expression must be specified in order for a proper mapping to happen during the comparison analysis. A group by expression can be aliased by adding the `AS <alias>` clause to the expression; this alias is used as the column name in the QA step results. A group by expression can also contain SQL functions such as `substring` or string concatenation using `||`.

**Sample Group By Expressions**

`scc AS scc_code`<br/>
`substring(fips, 1, 2) as fipsst`

or

`fipsst||fipscounty as fips`<br/>
`substring(scc, 1, 5) as scc_lv5`

#### Aggregate Expressions ####

The Aggregate Expressions are a list of columns/expressions that will be aggregated (summed) using the specified group by expressions. The expressions must contain valid columns from either the base or comparison datasets. If a column exists only in the base or compare dataset, then a Matching Expression must be specified in order for a proper mapping to happen during the comparison analysis.

**Sample Aggregate Expressions**

`ann_emis`<br/>
`avd_emis`

#### Matching Expressions ####

The Matching Expressions are a list of expressions used to match base dataset columns/expressions to comparison dataset columns/expressions. A matching expression consists of three parts: the base dataset expression, the equals sign, and the comparison dataset expression (i.e. `base_expression=comparison_expression`).

**Sample Matching Expressions**

    substring(fips, 1, 2)=substring(region_cd, 1, 2)
    scc=scc_code
    ann_emis=emis_ann
    avd_emis=emis_avd
    fips=fipsst||fipscounty

#### Join Type ####

The Join Type specifies which type of SQL join should be used when performing the comparison.

Join Type|Description
-|-
INNER JOIN|Only include rows that exist in both the base and compare datasets based on the group by expressions
LEFT OUTER JOIN|Include all rows from the base dataset, only include rows from the compare dataset that meet the group by expressions
RIGHT OUTER JOIN|Include all rows from the compare dataset, only include rows from the base dataset that meet the group by expressions
FULL OUTER JOIN|Include all rows from both the base and compare datasets

The default join type is FULL OUTER JOIN.

#### Where Filter ####

The Where Filter is a SQL WHERE clause that is used to filter both the base and comparison datasets. The expressions in the WHERE clause must contain valid columns from either the base or comparison datasets. If a column exists only in the base or compare dataset, then a Matching Expression must be specified in order for a proper mapping to happen during the comparison analysis.

**Sample Row Filter**

`substring(fips, 1, 2) = '37' and SCC_code in ('10100202', '10100203')`

or

`fips like '37%' and SCC_code like '101002%'`

#### Base Field Suffix ####

The Base Field Suffix is appended to the base aggregate expression name that is returned in the output. For example, an Aggregate Expression `ann_emis` with a Base Field Suffix `2005` will be returned as `ann_emis_2005` in the QA step results.

#### Compare Field Suffix ####

The Compare Field Suffix is appended to the comparison aggregate expression name that is returned in the output. For example, an Aggregate Expression `ann_emis` with a Compare Field Suffix `2008` will be returned as `ann_emis_2008` in the QA step results.

### More Examples ###

[Figure](#qa_compare_example_1) shows the setup dialog for the following example of the Compare Datasets QA program. We are setting up a plant level comparison of a set of two inventories (EGU and non-EGU) versus another set of two inventories (EGU and non-EGU). All four inventories are the same dataset type. The annual emissions will be grouped by FIPS code, plant ID, and pollutant. There is no mapping required because the dataset types are identical; the columns `fips`, `plantid`, `poll`, and `ann_emis` exist in both sets of datasets. This comparison is limited to the state of North Carolina via the Where Filter:

    substring(fips, 1, 2)='37'

The QA step results will have columns named `ann_emis_base`, `ann_emis_compare`, `count_base`, and `count_compare` using the Base Field Suffix and Compare Field Suffix.

![Compare Datasets Example 1][qa_compare_example_1]

[qa_compare_example_1]: images/qa_compare_example_1.png

[Figure](#qa_compare_example_2) shows the setup dialog for a second example of the Compare Datasets QA program. This example takes a set of ORL nonpoint datasets and compares it to a single FF10 nonpoint inventory. We are grouping by state (first two digits of the FIPS code) and pollutant. A mapping expression is needed between the ORL column `fips` and the FF10 column `region_cd`:

    substring(fips, 1, 2)=substring(region_cd, 1, 2)
    
Another mapping expression is needed between the columns `ann_emis` and `ann_value`:

    ann_emis=ann_value

No mapping is needed for pollutant because both dataset types use the same column name `poll`. This comparison is limited to three states and to sources that have annual emissions greater than 1000 tons. These constraints are specified via the Where Filter:

    substring(fips, 1, 2) in ('37','45','51') and ann_emis > 1000

In the QA step results, the base dataset column will be named `ann_emis_2002` and the compare dataset column will be named `ann_emis_2008`.

![Compare Datasets Example 2][qa_compare_example_2]

[qa_compare_example_2]: images/qa_compare_example_2.png

## Creating a Custom SQL QA Step [create_custom_sql_qa_section] ##

Suppose you have an ORL nonroad inventory that contains average-day emissions instead of annual emissions. The QA step templates that can generate inventory summaries report summed annual emissions. If you want to get a report of the average-day emissions, you can create a custom SQL QA step.

First, let's look at the structure of a SQL QA step created from a QA step template. [Figure](#custom_qa_step_intro) shows a QA step that generates a summary of the annual emissions by county and pollutant.

![QA Step Reference][custom_qa_step_intro]

[custom_qa_step_intro]: images/edit_new_qa_step_from_template.png

This QA step uses a custom SQL query shown in the **Arguments** textbox:

    select FIPS, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by FIPS, POLL order by FIPS, POLL

For the ORL nonroad inventory dataset type, the annual emission values are stored in a database column named `ann_emis` while the average-day emissions are in a column named `avd_emis`. For any dataset you can see the names of the underlying data columns by viewing the raw data as described in [Section](#viewing_data_section).

To create an average-day emissions report, we'll need to switch `ann_emis` in the above SQL query to `avd_emis`. In addition, the annual emissions report sums the emissions across the counties and pollutants. For average-day emissions, it might make more sense to compute the average emissions by county and pollutant. In the SQL query we can change `sum(ann_emis)` to `avg(avd_emis)` to call the SQL function which computes averages.

Our final revised SQL query is

    select FIPS, POLL, avg(avd_emis) as avd_emis from $TABLE[1] e group by FIPS, POLL order by FIPS, POLL

Once we know what SQL query to run, we'll create a custom QA step. [Section](#add_custom_qa_section) describes how to add a custom QA step to a dataset. [Figure](#custom_qa_step_sql) shows the new custom QA step with a name assigned and the Program pull-down menu set to SQL so that the custom QA step will run a SQL query. Our custom SQL query is pasted into the Arguments textbox.

![Custom SQL QA Step Setup][custom_qa_step_sql]

[custom_qa_step_sql]: images/custom_qa_step_sql.png

Click the **OK** button to save the QA step. The newly added QA step is now shown in the list of QA steps for the dataset ([Figure](#custom_qa_step_sql_done)).

![Custom SQL QA Step Ready][custom_qa_step_sql_done]

[custom_qa_step_sql_done]: images/custom_qa_step_sql_done.png

At this point, you can run the QA step as described in [Section](#running_steps_section) and view and export the QA step results ([Section](#export_qa_results_section)) just like any other QA step.

What if our custom SQL had a typo? Suppose we accidently entered the average-day emissions column name as `avg_emis` instead of `avd_emis`. When the QA step is run, it will fail to complete successfully. The Status window will display a message like

> Failed to run QA step Avg. Day by County and Pollutant for Version 'Initial Version' of Dataset \<dataset name\>. Check the query -ERROR: column "avg_emis" does not exist

Other types of SQL errors will be displayed in the Status window as well. If the SQL query uses an invalid function name like `average(avd_emis)` instead of `avg(avd_emis)`, the Status window message is

> Failed to run QA step Avg. Day by County and Pollutant for Version 'Initial Version' of Dataset \<dataset name\>. Check the query -ERROR: function average(double precision) does not exist

## Special SQL Syntax for QA Steps [qa_step_sql_syntax_section] ##

Each of the QA steps that create summaries use a customized SQL syntax that is very similar to standard SQL, except that it includes some EMF-specific concepts that allow the queries to be defined generally and then applied to specific datasets as needed. For example, the EMF syntax for the "Summarize by SCC and Pollutant" query is:

    select SCC, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by SCC, POLL order by SCC, POLL

The only difference between this and standard SQL is the use of the $TABLE[1] syntax. When this query is run, the $TABLE[1] portion of the query is replaced with the table name that contains the dataset's data in the EMF database. Most datasets have their own tables in the EMF schema, so you do not normally need to worry about selecting only the records for the specific dataset of interest. The customized syntax also has extensions to refer to another dataset and to refer to specific versions of other datasets using tokens other than $TABLE. For the purposes of this discussion, it is sufficient to note that these other extensions exist.

Some of the summaries are constructed using more complex queries that join information from other tables, such as the SCC and pollutant descriptions, and to account for any missing descriptions. For example, the syntax for the "Summarize by SCC and Pollutant with Descriptions" query is:

    select e.SCC, 
           coalesce(s.scc_description,'AN UNSPECIFIED DESCRIPTION')::character varying(248) as scc_description, 
           e.POLL, 
           coalesce(p.descrptn,'AN UNSPECIFIED DESCRIPTION')::character varying(11) as pollutant_code_desc, 
           coalesce(p.name,'AN UNSPECIFIED SMOKE NAME')::character varying(11) as smoke_name,
           p.factor, 
           p.voctog, 
           p.species, 
           coalesce(sum(ann_emis), 0) as ann_emis, 
           coalesce(sum(avd_emis), 0) as avd_emis 
    from $TABLE[1] e 
    left outer join reference.invtable p on e.POLL=p.cas 
    left outer join reference.scc s on e.SCC=s.scc 
    group by e.SCC,e.POLL,p.descrptn,s.scc_description,p.name,p.factor,p.voctog,p.species 
    order by e.SCC, p.name

This query is quite a bit more complex, but is still supported by the EMF QA step processing system.
