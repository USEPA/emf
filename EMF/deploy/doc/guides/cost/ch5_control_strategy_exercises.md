<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch4_control_strategy_manager.md) - [Home](README.md) - [Next Chapter >>](ch6_example_sql.md)

<!-- END COMMENT -->


Title: Control Strategy Exercises
Author: C. Seppanen, UNC
CSS: base.css

# Control Strategy Exercises [control_strategy_exercises_chapter] #

This chapter has some advanced training exercises for you to work through after you have completed the basic exercises above.

## Importing an Emissions Inventory (or County List File) [importing_an_emissions_inventory_section] ##

In many cases, it will be necessary to import an emissions inventory into the EMF for use with CoST. Before it can be imported, the inventory must be in one of these FF10 or ORL formats: Point, Onroad, Nonroad, or Nonpoint. For more information on these formats, see [https://www.cmascenter.org/smoke/documentation/4.5/html/ch08s02.html#sect_input_inventory_format](https://www.cmascenter.org/smoke/documentation/4.5/html/ch08s02.html#sect_input_inventory_format). Try importing an inventory using the following steps:

1. Choose **Datasets** from the **Manage** menu of the EMF main window.

2. Set the **Show Datasets of Type** menu to the type that represents your inventory (e.g., ORL Nonpoint Inventory).

3. Click the **Import** button to show the **Import Datasets** window.

4. Click the **Browse** button and browse to the location of your inventory on the EMF server computer (e.g., C:\EMF_temp\inventories\nonpoint).

5. Select the checkbox that corresponds to your inventory (e.g., arinv\_nonpt\_2020cc\_31may2007\_v0\_orl\_txt) and then click **OK**.

6. Specify a meaningful name for the new dataset in the **Dataset Name** type-in field that does not duplicate one of the existing dataset names.

7. Click the **Import** button. Monitor the **Status** window for the status of your import. If the inventory file was new, you may have data formatting issues to deal with.

8. Click **Done** on the Import Datasets window.

9. After you see a message that the Status window that indicates that the import has completed, click **Refresh** on the **Dataset Manager** and you should see the newly imported inventory dataset. The dataset could now be used as an input to a control strategy.

10. To import a list of counties to limit the counties used for a strategy analysis, set the **Show Datasets of Type** menu to **List of Counties** and then import the file following steps 3 through 9 above. Be sure that your list of counties file has at least two columns, with one of them labeled 'FIPS'.

From the Dataset Manager, you can:

* use the **View** button to open the Dataset Properties Viewer,
* use the **Edit** button to open the Dataset Properties Editor,
* use the **Edit Data** button to create new versions of the dataset,
* use the **Remove** button to remove the dataset,
* use the **Import** button to import new datasets,
* use the **Export** button to export the data to a file on the EMF server,
* use the **Purge** button to purge datasets that were removed from the system,
* use the **Close** button to close the **Dataset Manager Window**.

## Running a Maximum Emissions Reduction Strategy ##

For this exercise, we will modify the least cost strategy you created in [Chapter](#control_strategy_manager_chapter) and run it as a maximum emissions reductions strategy.

1. **Copy** the least cost strategy you created in [Chapter](#control_strategy_manager_chapter) to a new strategy.

2. **Edit** the strategy and set the Strategy Type to **Max Emissions Reduction**.

3. **Run** the new strategy.

4. Did the maximum emissions reduction strategy run slower or faster than the least cost?

5. How much more reduction did you get over the 50% level of reduction you specified in [Chapter](#control_strategy_manager_chapter)?

6. How does the Average Cost per Ton and Total Cost differ between the 50% reduction strategy you ran earlier and the maximum emissions reduction available?

    Hint: **use the columns of the Control Strategy Manager** to answer this question.

    **Least Cost Strategy Total Cost:**<br/>
    **Least Cost Strategy Average CPT:**

    **Max. Emissions Red Total Cost:**<br/>
    **Max. Emissions Red Average CPT:**

7. What are some of the SCCs for sources that had control measures applied in the result, but had a control efficiency of less than 90%? [It is important to note these because they may provide opportunities for controls...] **Hint**: Examine the Strategy Detailed Result and apply a filter for CONTROL_EFF<90 to find the applicable rows.

    **SCCs with CE < 90%:**

## Running a Strategy with a Hypothetical Measure ##

For this exercise, we will create a new control measure and then see what impact it has on the strategy results.

1. Create a **new** control measure. Set the **Major Pollutant** to NOX. Set the **Class** to **Hypothetical**.

2. On your new measure, enter an Efficiency record for **NOX** with a control efficiency of **95%**, a cost per ton of **3000**, and a cost year of **2006**.

3. For your new measure, add all SCCs **starting with 102** (there should be about 78 of these). Hint: you do not have to click 78 checkboxes to do this - remember to filter and Select All.

4. **Copy** your maximum emissions reduction strategy from the previous exercise to a new strategy.

5. Set your new strategy to include **Hypothetical** measures in addition to **Known**.

6. **Run** your new strategy.

7. Based on the results of this strategy, **how much additional emissions reduction** could you get over the previous maximum reduction result if there was a control measure with a 95% CE available for sources with SCCs starting with 102, such as the one you created?

## Examining Cobenefits ##

For this exercise, we will run a control strategy that will result in some control measures being applied that result in co-benefits.

1. Set up a new control strategy.

2. Set the Target year to **2020** and the Strategy Type to **Max Emissions Reduction**.

3. Set the inventory to use to be the **nonpoint** inventory you imported in [Section](#importing_an_emissions_inventory_section).

4. Set the inventory filter to: **`FIPS like '42%'`**.

5. Set the Target Pollutant to **PM10**, then **Run** the strategy.

6. Once the run completes, summarize the Strategy Detailed result by Control Technology and Pollutant.

7. Once the summary has completed running, **view the Detailed Result**.

    Do you see data for more than one pollutant?
    
    What is the typical cost per ton for the strategy?

8. Try setting a **Maximum Cost per Ton** constraint less than the typical cost per ton in the result you just generated and rerun the strategy.

    How does the constraint impact the results?

<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch4_control_strategy_manager.md) - [Home](README.md) - [Next Chapter >>](ch6_example_sql.md)<br>

<!-- END COMMENT -->

