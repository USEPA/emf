<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch4_control_strategy_manager.md) - [Home](README.md) - [Next Chapter >>](ch6_example_sql.md)

<!-- END COMMENT -->

## Contents
[Exercise 1. Importing an Emissions Inventory (or County List File)](#importing_an_emissions_inventory)<br>
[Exercise 2. Running a Maximum Emissions Reduction Strategy](#running_max_emis_reduction)<br>
[Exercise 3. Running a Strategy with a Hypothetical Measure](#running_hypothetical)<br>
[Exercise 4. Examining Co-benefits of a Control Strategy](#cobenefits)<br>

# Advanced Control Strategy Exercises #

This chapter includes advanced exercises for learning how to use different features of CoST. Users should complete the exercises in [Chapter 3](./ch3_control_measure_manager.md) and [Chapter 4](ch4_control_strategy_manager.md) before attempting the exercises in this chapter.  

<a id=importing_an_emissions_inventory></a>

## Exercise 1. Importing an Emissions Inventory (or County List File)

1. Choose `Datasets` from the **Manage** menu of the main EMF window.

In many cases, it will be necessary to import an emissions inventory into the EMF for use with CoST. Before it can be imported, the inventory must be in one of these FF10 or ORL formats: Point, Onroad, Nonroad, or Nonpoint. For more information on these formats, see [https://www.cmascenter.org/smoke/documentation/4.5/html/ch08s02.html#sect_input_inventory_format](https://www.cmascenter.org/smoke/documentation/4.5/html/ch08s02.html#sect_input_inventory_format). Try importing an inventory using the following steps:

2. Select an inventory format from `Show Datasets of Type` menu to the type that represents the inventory to import (e.g., FF10 Nonpoint Inventory).

3. Click the `Import` button to show the **Import Datasets** window.

([Import Dataset Window Figure](#import_dataset_window)).

![Import Dataset Window][import_dataset_window]

[import_dataset_window]: images/Import_Datasets.png

4. Click the `Browse` button and browse to the location of the inventory to import on the EMF server computer (e.g., C:\Users\Public\EMF_Data\inventories).

([Browse Datasets Window Figure](#import_browse_window)).

![Import Browse Window][import_browse_window]

[import_browse_window]: images/EMF_Import_Dataset_Server_Local.png

5. Select the checkbox that corresponds to the inventory (e.g., 2017eh\_from\_nonpt\_2011NEIv2\_NONPOINT\_20141108\_09mar2015\_v0\_FIPS\_37.csv) and then click `OK`.

6. Specify a unique, descriptive name for the new dataset in the `Dataset Name` dialog box.

7. Click the `Import` button. Monitor the **Status** window for the status of the import.

8. Click `Done` on the Import Datasets window.

9. After the Status window messages indicates that the import has completed, click `Refresh` on the **Dataset Manager** and to see the newly imported inventory dataset. The dataset could now be used as an input to a control strategy.

10. To import a list of counties to limit the counties used for a strategy analysis, set the `Show Datasets of Type` menu to **List of Counties** and then import the file following steps 3 through 9 above. Be sure that the list of counties file has at least two columns, with one of them labeled 'FIPS'.

The Dataset Manager includes the following controls:

* `View` opens the Dataset Properties Viewer
* `Edit Properties` opens the Dataset Properties Editor
* `Edit Data` creates new versions of the dataset
* `Remove` removes the dataset
* `Import` imports new datasets
* `Export` exports the data to a file on the EMF server
* `Purge` purges datasets that were removed from the system
* `Close` closes the **Dataset Manager Window**

([Export Dataset Window Figure](#export_window)).

![Export Window][export_window]

[export_window]: images/EMF_Export_Window.png

<a id=running_max_emis_reduction></a>

## Exercise 2. Running a Maximum Emissions Reduction Strategy

For this exercise, an existing Least Cost Strategy will be modified to create a Maximum Emissions Reductions strategy.

1. From the Control Strategy Manager `Copy` the least cost strategy created in [Chapter 4](#ch4_control_strategy_manager.md) to a new strategy.

2. `Edit` the strategy and set the Type of Analysis to **Max Emissions Reduction**.

3. `Run` the new strategy and answer the following questions once it completes.


* Did the Maximum Emissions Reduction strategy run slower or faster than the Least Cost?

* How much more emissions reduction was achieved over the 50% level of reduction set in the Least Cost run in [Chapter 4](#ch4_control_strategy_manager.md)?

* How do the Average Cost per Ton and Total Cost differ between the 50% Least Cost reduction strategy and the Maximum Emissions Reduction available?

    *Hint: use the following columns of the **Control Strategy Manager** to answer this question.*

    **Least Cost Strategy Total Cost:**<br/>
    **Least Cost Strategy Average CPT:**

    **Max. Emissions Red Total Cost:**<br/>
    **Max. Emissions Red Average CPT:**

* What are some of the SCCs for sources that had control measures applied in the result, but had a control efficiency of less than 90%? [It is important to note these because they may provide opportunities for controls...] *Hint: Examine the Strategy Detailed Result and apply a filter for CONTROL_EFF<90 to find the applicable rows:*

    **SCCs with CE < 90%:**

<a id=running_hypothetical></a>

## Exercise 3. Running a Strategy with a Hypothetical Measure

For this exercise, create a new control measure and then see what impact it has on the strategy results.

1. From the **Control Strategy Manager** create a `New` control measure. Set the `Major Pollutant` to **NOX** and set the `Class` to **Hypothetical**.

2. For the new measure enter an `Efficiency` record for **NOX** with a control efficiency of **95%**, a `Cost per Ton` of **3000**, and a `Cost Year` of **2006**.

3. For your new measure, add all SCCs **starting with 102** (there should be about 78 of these). *Hint: you do not have to click 78 checkboxes to do this - remember to filter and Select All.*

4. `Copy` the Maximum Emissions Reduction strategy from the previous exercise to a new strategy.

5. Set the new strategy to include **Hypothetical** measures in addition to **Known**.

6. `Run` the new strategy.

Based on the results of this strategy, **how much additional emissions reduction** were realized over the previous maximum reduction result if there was a control measure with a 95% CE available for sources with SCCs starting with 102?

<a id=cobenefits></a>

## Exercise 4. Examining Co-benefits of a Control Strategy

For this exercise, run a control strategy that produces co-benefits for multiple pollutants.

1. Set up a new control strategy.

2. Set the `Target Year` to **2020** and the `Strategy Type` to **Maximum Emissions Reduction**.

3. Set the inventory to use to be the **Nonpoint** inventory imported in [Exercise 1](#importing_an_emissions_inventory_section).

4. Set the `Target Pollutant` to **PM2_5**, then `Run` the strategy.

5. Once the run completes, summarize the **Strategy Detailed Result** by **Control Technology and Pollutant**.

6. Once the summary has completed running, `View` the **Strategy Detailed Result**

* Does this result show more than one pollutant?

* What is the typical cost per ton for the strategy?

7. Try setting a **Maximum Cost per Ton** constraint less than the typical cost per ton in the result just generated and rerun the strategy. How does the constraint impact the results?

8. Use the Cost Control Summary Function in the Control Strategy Manager Window to create a local spreadsheet summarizing the selected control strategies, including name, strategy type, and constraints, emission reductions and strategy costs.

([Cobenefit Strategy Summary with Constraint Figure](#summary_spreadsheet)).

![Summary Spreadsheet][summary_spreadsheet]

[summary_spreadsheet]: images/cobenefit_strategy_summary.png

([Cobenefit Strategy Summary with No Constraints Figure](#summary_spreadsheet_no_constraints)).

![Summary Spreadsheet No Constraints][summary_spreadsheet_no_constraints]

[summary_spreadsheet_no_constraints]: images/cobenefit_strategy_summary_no_constraints.png

9. Note that the summary spreadsheet with constraints was for a cost per ton value of 3000. The result was that no controls were applied, as they all cost more than that.  For the summary spreadsheet with no constraints, the cost of the controls is listed after the status (Completed), for a total value of $3,679,355 and a total emission reduction of 331.2 tons of PM2.5. 

<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch4_control_strategy_manager.md) - [Home](README.md) - [Next Chapter >>](ch6_example_sql.md)<br>

<!-- END COMMENT -->
