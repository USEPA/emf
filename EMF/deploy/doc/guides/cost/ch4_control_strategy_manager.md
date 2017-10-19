<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch3_control_measure_manager.md) - [Home](README.md) - [Next Chapter >>](ch5_control_strategy_exercises.md)

<!-- END COMMENT -->

# Control Strategy Manager

<!-- BEGIN COMMENT -->

## Contents
[Chapter 4 Introduction](#Ch4Intro4)<br>
[1. Introduction to Control Strategies](#Intro4)<br>
[2. CoST Control Strategy Algorithms](#Algorithms4)<br>
[3. Managing Control Strategies](#Managing4)<br>
[4. Inputs to Control Strategies](#Inputs4)<br>
[5. Running a Strategy and Accessing Its Outputs](#Running4)<br>
[6. Outputs of Control Strategies](#Outputs4)<br>
[7. Summaries of Strategy Inputs and Outputs](#Summaries4)<br>

<!-- END COMMENT -->

<a id=Ch4Intro4></a>

## Introduction

This chapter demonstrates the features of the Control Strategy Manager. The Control Strategy Manager allows control strategies to be created, edited, copied, and removed. **A control strategy is a set of control measures applied to emissions inventory sources (in addition to any controls that are already in place) to accomplish an emissions reduction goal.** Such goals are usually set to improve air quality and/or to reduce risks to human health. In this chapter, you will learn how to:

* View, sort, and filter a list of control strategies from the *Control Strategy Manager* window
* Create control strategies
* Edit control strategies to specify their inputs and parameters
* Run control strategies
* Copy control strategies
* Remove control strategies
* Analyze and summarize outputs from control strategies

This chapter is presented as a series of steps so that it may be used as part of a training class or as a tutorial on how to use CoST. The numbered steps are the ones you are expected to perform, while other material is provided for documentation purposes.

<a id=Intro4></a>

### Introduction to Control Strategies

CoST automates the key steps for preparing control strategies. The purpose of developing control strategies is to answer questions about which sources can be controlled and how much the application of those controls might cost. For example, if the goal is to reduce NO<sub>x</sub> emissions for the Southeast U.S. in 2030 by 100,000 tons per year, CoST can help answer questions related to this goal, such as:

* What is the **maximum emissions reduction** achievable for NO<sub>x</sub> (i.e., is my reduction goal less than the maximum possible reduction?), and what set of controls will achieve this reduction?
* What set of controls can achieve the goal at the **least cost**?
* What does the **cost curve** look like for other levels of reduction?
* What **emissions reductions** for the target pollutant would be achieved?
* What are the emission reductions or increases for **other pollutants** of interest?
* What are the **engineering costs** of applying the controls for a specific strategy?
* What **control measures** are available for specific source categories and pollutants, how much reduction does each one provide, and for what cost?

A future goal for CoST is to be able to answer this question: What is the optimum method for achieving simultaneous targeted reductions for **multiple pollutants**?

CoST can help answer the above questions when users set up and run one or more control strategies. A diagram of the basic steps for running a control strategy is shown in [Figure 4-1](#basic_steps_for_running_a_control_strategy). As illustrated in that figure, the inputs to a control strategy consist of:

* a set of parameters that control how the strategy is run
* one or more emissions inventory datasets (that have already been loaded into the EMF)
* filters to limit the sources included from those datasets
* filters to limit which control measures are to be included in the strategy analysis
* constraints that limit the application of measures to specific sources based on the resulting costs or emissions reduction achieved

<a id=basic_steps_for_running_a_control_strategy></a>

![Basic Steps for Running a Control Strategy][basic_steps_for_running_a_control_strategy]

[basic_steps_for_running_a_control_strategy]: images/Basic_Steps_for_Running_a_Control_Strategy.png
**Figure 4-1. Control Strategy Diagram.**

After a control strategy run is complete, several outputs are associated with the strategy. The main CoST output for each control strategy is a table called the "**Strategy Detailed Result**". This table consists of emission source-control measure pairings, each of which contains information about the cost and emission reduction that would be achieved if the measure were to be applied to the source. If multiple inventories were processed by the strategy, then there will be one Strategy Result for *each* input inventory, unless the inventories were merged for a least cost run (as indicated in the 'Multiple Inventories' column [Table 4-1](#summary_of_strategy_algorithms_table)). Also, there will be at least one Strategy Detailed Result for each of the least cost iterations performed as part of a Least Cost Curve run. In addition to the Strategy Detailed Result, two other outputs are produced for each strategy run: the Strategy County Summary (which includes uncontrolled and controlled emissions), and the Strategy Measure Summary (which summarizes how control measures were applied for each sector-county-SCC-Pollutant combination). These three outputs are referred to in [Table 4-1](#summary_of_strategy_algorithms_table) as the 'Standard' outputs.

The Strategy Detailed Result table itself can be summarized on-demand in many ways using predefined summary queries (e.g., by state, by county, by control technology). Users familiar with SQL can also define their own custom queries. The Strategy Detailed Result table can also be merged with the original input inventory, in an automated manner, to produce a *controlled emissions inventory* that reflects implementation of the strategy. The controlled emissions inventory includes information about the measures that have been applied to the controlled sources and can be directly input to the SMOKE modeling system to prepare air quality model-ready emissions data. Comments are placed at the top of the inventory file to indicate the strategy that produced it and the settings of the high-level parameters that were used to run the strategy.

Detailed information on the types of control strategy algorithms is provided in [Section 2](#Algorithms4). [Section 3](#Managing4) describes how to set up and manage control strategies. [Section 4](#Input4) details the inputs to control strategies, including adding inventories, control measures, and constraints to a strategy. Running a strategy and accessing its outputs are discussed in [Section 5](#Running4). Documentation for the various types of strategy outputs is given in [Section 6](#Outputs4) and information about summaries of strategy inputs and outputs is given in [Section 7](#Summaries4).

<a id=Algorithms4></a>

## CoST Control Strategy Algorithms

Once the inputs have been defined, the strategy can be run on the EMF server. The method by which the measures are associated with the strategies depends on the algorithm that has been selected for the strategy. At this time, six algorithms are available to determine how measures are assigned to sources:

* **Annotate Inventory**: assigns control measures to the inventory based on the control efficiency specified for each source, and can be used to fill in control measure information for inventory sources that are missing these details but have a control efficiency assigned.
* **Apply Measures in Series**: assigns all control measures that can be used for a source in the specified order; this is often used for mobile sources, for which the control measures are typically independent of one another.
* **Least Cost**: each source can be assigned only a single measure to achieve a specified percent or absolute reduction for the sources included in the strategy run, with the minimum possible annualized cost.
* **Least Cost Curve**: performs least-cost runs iteratively at multiple percent reductions so that a cost curve can be developed that shows how the annualized cost increases as the level of desired reduction increases.
* **Maximum Emissions Reduction**: assigns to each source the single measure (if a measure is available for the source) that provides the maximum reduction to the target pollutant, regardless of cost.
* **Multi-Pollutant Maximum Emissions Reduction**: assigns all control measures that can be used for a source based on a specific target pollutant order (e.g., NO<sub>x</sub> first, PM<sub>10</sub> second, VOC third, and SO<sub>2</sub> last). Each source target pollutant can be assigned only a single measure, and it must be the one that provides the maximum reduction, regardless of cost. If a source's target pollutant was already controlled via a co-impact from a measure applied during a previous target pollutant iteration, then no additional control will be chosen for that specific source's target pollutant (e.g., if a NO<sub>x</sub> measure also controlled VOC as a co-impact, during the VOC iteration no measure would be attempted for this source, since it was already controlled via the co-impact).

Some of the key aspects of each of the strategy types are summarized in [Table 4-1](#summary_of_strategy_algorithms_table), and some additional information on each strategy type is provided in the following subsections.

<a id=summary_of_strategy_algorithms_table></a>
**Table 4-1. Control Strategy Algorithms**

Strategy Type|Multiple Inventories|Typical Sectors|Measure Assignment|Outputs
----------------------------|-----------------------|---------------------------------------------------|--------------------------|----------------
Annotate Inventory|Processed independently|Area, nonpoint|One per source|Standard
Apply Measures in Series|Processed independently|Mobile: Onroad, nonroad|Multiple per source|Standard
Least Cost|Will be merged|Area, nonpoint|One per source|Standard, Least Cost Control Measure Worksheet
Least Cost Curve|Will be merged|Area, nonpoint|One per source|Standard, Least Cost Control Measure Worksheet, Least Cost Curve Summary
Maximum Emissions Reduction|Processed independently|Area, nonpoint|One per source, to achieve maximum reduction of target pollutant|Standard
Multi-Pollutant Maximum Emissions Reduction|Processed independently|Area, nonpoint|One per source target pollutant; based on specified target pollutant order; could be multiple per source|Standard

### Maximum Emissions Reduction Control Strategy

The Maximum Emissions Reduction control strategy is the simplest of the strategy algorithms. It assigns to each source the single control measure that provides the maximum reduction to the target pollutant, regardless of cost. The strategy produces the three standard types of strategy outputs, including a Strategy Detailed Result for each input inventory.

### Apply Measures in Series Control Strategy

The Apply Measures in Series control strategy applies all relevant controls to a source, as opposed to the Maximum Emission Reduction strategy, where only the most relevant measure (with the best possible reduction for lowest cost) is applied to the source. The Apply Measures in Series strategy is typically used for mobile sources, for which the input inventories are often average day inventories specific to a given month, and for which there are often multiple independent controls available for each source. Therefore, the measures are applied to the source in series, one after the other. The order of application is based on the user-specified "apply order," but the system also considers the lowest cost and greatest control efficiency. Thus, the cost factor in terms of dollars per ton of pollutant reduced is used to calculate the annualized cost of the control measure when applied to a specific source.

The three standard types of outputs are generated after a successful strategy run: a Strategy Detailed Result ***for each input inventory***, a single Strategy Measure Summary, and a single Strategy County Summary. Note that when input inventories contain average-day emissions data for a month, the corresponding Strategy Detailed Result datasets will specify total monthly emissions as opposed to average day emissions for each source; otherwise they will specify annual emissions. The total monthly emissions are calculated by multiplying the average day emissions by the number of days in the month. Regarding the two types of summary outputs, if the input datasets have data for each of the 12 months, the summaries will provide annual emissions.

### Least Cost Control Strategy

The Least Cost strategy type assigns measures to emissions sources to achieve a specified percent reduction or absolute reduction of a target pollutant for sources in a specified geographic region while incurring the minimum possible annualized cost. This algorithm is similar to the maximum emissions reduction strategy in that only a single measure is applied to each source. For example, one measure might be selected for a source when trying to reduce the target pollutant by 20%. However, if you were trying to obtain a 40% reduction of the target pollutant, another more expensive measure that achieves a higher level of control might be selected for the same source to meet the targeted level of reduction. If multiple inventories are specified as inputs to a Least Cost strategy, they are automatically merged into one EMF dataset as an ORL Merged dataset type. This allows the multiple inventory sectors to be considered simultaneously during a single Least Cost run. Note that the merged inventory dataset will be truncated and repopulated at the start of each strategy run, to ensure that the most up to date inventory data is included in the run.

The Least Cost strategy automatically creates the same three standard output datasets, but it also creates an additional output dataset called the Least Cost Control Measure Worksheet. This output is a table of all possible emission source-control measure pairings (for sources and measures that meet the respective filters specified for the strategy), each of which contains information about the cost and emission reduction achieved if the measure was to be applied to the source. Examples of these tables are given in [Section 6](#Outputs4). This dataset will be used to help generate ***a single*** Strategy Detailed Result (no matter how many input inventories were processed) once the optimization process has been performed to achieve the desired reduction. This dataset has the all of the same columns as the Strategy Detailed Result (see [Table 4-9](#columns_in_the_strategy_detailed_result_table)), in addition to the following columns:

* **marginal**: This column stores the marginal cost (dollars are given based on the specified cost year) for the source-measure record. This is calculated according to the following equation:

```
marginal cost = annual cost (for specified cost year) / emission reduction (tons)
```

Note that cost equations are used to compute the annual cost, when applicable and all required input data is available. For target pollutant source-control pair records, the annual cost will be the total of the annual costs for the target pollutant and any costs associated with co-benefit pollutants.

* **status**: This column contains a flag that helps determine which source-control records should be actively considered during the strategy run.
* **cum\_annual\_cost**: This column contains the cumulative annual cost for the source and all preceding sources that have been included in the strategy (i.e., for which status is null). This is only specified for target pollutant sources, but it also includes costs associated with co-benefit pollutants.
* **cum\_emis\_reduction**: This column contains the cumulative emission reduction for the source and all preceding sources that have been included in the strategy (i.e., for which status is null). This is only calculated for target pollutant sources. The emission reduction is cumulated by following the apply\_order in an ascending order.

If multiple input inventories are used for the least cost strategy run and the user requests to create controlled inventories, there will be one controlled inventory created for each of the input inventories.

### Least Cost Curve Control Strategy

The purpose of the Least Cost Curve strategy type is to iteratively run Least Cost strategies so that a cost curve of can be generated. Typically, a cost curve has the total cost of emissions reduction and the cost per ton of emissions reduction increases as the desired level of reduction increases. The input inventories are treated in the same way as the least cost run in that the data from the inventories will be put together into an ORL Merged inventory prior to performing any of the runs. The inventory filters and measure filters work in the same way as they do for the other strategy types, as do the constraints that apply to all strategy types. The main difference between the Least Cost and Least Cost Curve strategy types is in the specification of constraints. Instead of specifying a single percent reduction or absolute emissions reduction, three new constraints are used to control the run:

* **Domain-wide Percent Reduction Start (%)**: Specifies a percent reduction to be used for the first Least Cost strategy to be run.
* **Domain-wide Percent Reduction End (%)**: Specifies a percent reduction to be used for the last Least Cost strategy to be run.
* **Domain-wide Percent Reduction Increment (%)**: Specifies an increment on percentages to use between the first and last runs (e.g., if 25% is specified, runs will be performed for 25, 50, 75, and 100% reduction).

Additional runs can be added to a least cost curve strategy if you do not delete the previous results when you rerun the strategy. Suppose that you generate a coarse cost curve (default increment is 25%) and you find an area of interest that bears further examination. You can then go back and specify different start, end, and increment to obtain more information (e.g., start=80%, end=90%, increment=2%) about that portion of the curve.

The types of outputs for a Least Cost Curve strategy are the following:

* **Strategy Detailed Result** datasets for each targeted percent reduction. Note that several results could have the same actual percent reduction if the targeted reduction exceeds the maximum available reduction. As with a Least Cost strategy, the actual percent reduction may not exactly match the targeted reduction due to the discrete nature of applying specific controls to specific sources. CoST will ensure that each actual reduction is equal to or greater than the corresponding targeted reduction.
* **Least Cost Control Measure Worksheet**: this output is the same as the worksheet produced for a regular Least Cost strategy run. Note that the same worksheet is used for all targeted percent reductions and only the status column is updated to specify when measure-source combinations are included in the current strategy.
* **Least Cost Curve Summary**: this output dataset contains a row with cost and emissions reduction information for each of the runs that was performed for the strategy. Rows are added to this output if additional strategy runs are performed (e.g., to examine different sections of the curve). The columns of this summary are: Poll, Uncontroll\_Emis (tons), Total\_Emis\_Reduction (tons), Target\_Percent\_Reduction, Actual\_Percent\_Reduction, Total\_Annual\_Cost, Average\_Ann\_Cost\_per\_Ton, Total\_Annual\_Oper\_Maint\_Cost, Total\_Annualized\_Capital\_Cost, Total\_Capital\_Cost. Here, the Uncontroll\_Emis column contains the emissions from the original input inventory with all existing controls backed out so that it represents the uncontrolled emissions. The columns starting with Total are computed by summing all of the values of the corresponding column in the Strategy Detailed Result for the pollutant specified in the Poll column. Examples of Least Cost Curve Summaries are given in ([Figure 4-27](#analyzing_a_least_cost_curve_output)) and Table 19 (TODO: fix).
* **Controlled Inventories**: these output datasets may optionally be created based on any of the Strategy Detailed Results that are available for the strategy. Thus, results corresponding to any of the targeted reductions may be processed by SMOKE and the resulting data used as an input to an air quality model. Note that for each targeted reduction, individual controlled inventories will be created for each of the input inventories.

### Annotate Inventory Control Strategy

The purpose of the Annotate Inventory strategy type is to specify what measures are likely to have been used to achieve specified percent reductions in input inventories. The input inventories are treated in the same way as the Maximum Emissions Reduction runs in that each inventory is processed separately and separate results are created for each one. The inventory filters and measure filters work in the same way as they do for the other strategy types. Note that the selected target pollutant is important because only records for that pollutant will be annotated, but CoST may be changed in the future to consider all inventory pollutants. Constraints are applicable in that if the controlled source does not satisfy the specified constraints, it will not be included in the result and another measure that does satisfy the constraints will be sought.

The outputs from the Annotate Inventory strategy type are an Annotated Inventory for each of the input inventories. The annotated inventories have the same dataset types as the input inventories. All of the source records for the specified target pollutant with nonzero control efficiencies in the input inventory will appear in the annotated inventory and the control measures column will be filled in for sources for which a matching measure has been found. Note that the originally specified control efficiency fields and the emissions in the inventory are not changed, even if the inventory efficiency differed from the efficiency specified for the control measure. Once an annotated inventory has been created, a controlled inventory can be created from the annotated inventory. Unlike the annotated inventory, the controlled inventory will have all records found in the input inventory and can therefore be used as an input to SMOKE.

One of the goals for CoST that has not yet been met is for the tool to be able to intelligently make use of control measures that can be applied in addition to other controls (also known as 'add-on' controls). In order for the software to meet this goal, it is important for it to first be able to determine whether there are any existing control measures on the emissions source and the type of control device(s) used by the existing measures. Currently, a data gap exists in this area for both the base year emissions inventories and the future year emission inventories which may be used as inputs to a control strategy run. The NEI contains data fields to store this information, and there is a limited amount of existing control efficiency and control device code data in the base year NEI. However, these fields are not very well populated in the base year inventory for sectors other than the EGU point sources, and the fields are even less well populated in the future year modeling inventories. Generally, the control efficiency field is much better populated than the control device fields. In addition, the control device codes that are stored in the NEI are a lot less specific than the control measure abbreviations that CoST uses. Therefore, even if the control device codes were well populated, these codes would need to be translated into the CoST control measure abbreviations for CoST to really have the information it needs to properly apply add-on controls.

To address the issue of unspecified control measures in inventories that can be input to CoST, several steps have been taken. First, when CoST creates a controlled inventory, in addition to filling in the information in the CEFF, REFF, and RPEN columns, CoST populates the Control Measures, Pct Reduction, Current Cost, and Total Cost columns in the FF10 or ORL inventory to specify information about measures that it has applied. In this way, the controlled inventories created by CoST always specify the relevant information about the measures that have been applied as a result of a CoST control strategy. The Annotate Inventory strategy type is a second step that has been taken to provide more information about existing control measures.

When an Annotate Inventory strategy is run, CoST looks at the percent reduction specified by the CEFF, REFF, and RPEN columns and uses the available control measures in the database to try to determine what control measure has the closest percent reduction to the one specified in the inventory. It then fills in the control measures column with the measure that was found. Note that the originally specified control efficiency fields and the emissions in the inventory are not changed, even if the inventory efficiency differed from the efficiency specified for the control measure. If no measure was found, it leaves the control measure field blank. Once the strategy has been run, a summary report can be generated with using the "Compare CoST to NEI measures" query that shows the sources with non-zero CEFF values and the difference between the inventory specified percent reduction and the percent reduction that the control measure that CoST "guessed" had been applied to the source. **It is important for the user to then examine the results of this report to find cases where the specified control efficiency matches were not even close and those for which no match was found.** Both of these situations can indicate that there is missing or incorrect data in the control measures database, or that the information in the inventory was erroneous. The eventual goal of the Annotate Inventory strategy is to develop a base year inventory with more complete existing control measure information.

### Multi-Pollutant Maximum Emissions Reduction Control Strategy

The Multi-Pollutant Maximum Emissions Reduction control strategy assigns to each source the single measure for each target pollutant that provides the maximum reduction, regardless of cost. This process is performed for each target pollutant in an order specified by the user (e.g., NO<sub>x</sub> first, PM<sub>10</sub> second, VOC third, and SO<sub>2</sub> last). If a measure would control a pollutant that was already controlled as a co-impact from a previous target pollutant analysis iteration, that measure will be excluded from consideration during the source-measure matching process. For example, if measure ABC controlled NO<sub>x</sub> (the first analyzed target pollutant) and VOC, and during the next pollutant iteration (for VOC) measure DEF also controls NO<sub>x</sub> and VOC, this measure will not be considered because VOC control was a co-impact from applying the ABC measure.

The inventory filter and county filter work differently for this control strategy than they do for the other strategy types. The inventory filter and county filter can be specified separately for each target pollutant, whereas for the other strategy types they are defined at the strategy level.

The Multi-Pollutant Maximum Emissions Reduction strategy produces the three standard types of strategy outputs, including a Strategy Detailed Result for each input inventory.

<a id=Managing4></a>

## Managing Control Strategies

The control strategies currently available within CoST are shown in the Control Strategy Manager. The Control Strategy Manager allows you to see information about control strategies, to create new control strategies, and also to edit, remove, and copy control strategies.

### Opening the Control Strategy Manager

**Step 3-1: Open Control Strategy Manager.** To open the Control Strategy Manager, choose `Control Strategies` from the `Manage` menu on the EMF main window ([Figure 4-2](#manage_menu_of_emf_main_window_2)) and the Control Strategy Manager will appear ([Figure 4-3](#control_strategy_manager_window)).

<a id=manage_menu_of_emf_main_window_2></a>

![Manage Menu of EMF Main Window][manage_menu_of_emf_main_window_2]

[manage_menu_of_emf_main_window_2]: images/Manage_Menu_of_EMF_Main_Window_2.png
**Figure 4-2. EMF Manage Menu.**

<a id=control_strategy_manager_window></a>

![Control Strategy Manager Window][control_strategy_manager_window]

[control_strategy_manager_window]: images/Control_Strategy_Manager_Window.png
**Figure 4-3. Control Strategy Manager.**

The Control Strategy Manager shows all of the control strategies currently available within the CoST/EMF system in a sortable, filterable window. The columns shown in the window are Select, Name, Last Modified, Is Final, Run Status, Region, Target Pollutant, Total Cost, Reduction (tons), Average Cost Per Ton, Project, Strategy Type, Cost Year, Inventory Year, and Creator. Descriptions of some of the columns are given in [Table 4-2](#key_columns_of_the_control_strategy_manager_table). The remaining fields are described in detail in [Section 4](#Inputs4).

<a id=key_columns_of_the_control_strategy_manager_table></a>
**Table 4-2. Control Strategy Manager Table**

Column|Description
--------------------|---------------------------------------------------------------------------------------------------------------------------------
Name|shows the name of the control strategy.
Last Modified|shows the date and time on which the strategy was last changed.
Run Status|gives information about the strategy run. Possible options are:<br/>Not started - the strategy run has never been started;<br/>Waiting - a run has been requested, but it is waiting because other strategies are running;<br/>Running - the strategy is currently running;<br/>Finished - the strategy run completed successfully;<br/>Failed - the strategy run started, but failed due to a problem.
Inv Year|shows the year of the emissions inventory that the strategy will process.

### Sorting and Filtering Control Strategies

By default, the strategies are shown using a descending sort on the last modified date and time, so that the most recently modified strategies appear at the top of the list.

**Step 3-2: Sort Control Strategies.** To sort the control strategies on the total cost of the strategy, click on the heading of the **Total Cost** column and the rows will re-sort so that the most expensive strategies will be shown at the top. Click on the **Total Cost** column header again to reverse the sort.

**Step 3-3: Filter Control Strategies.** To see only strategies that were run with a specific target pollutant, click the `Filter` button (<img src="images/Filter_Button.png"/>) on the Control Strategy Manager toolbar. When the **Filter Rows** window appears enter a criterion for the filter by clicking `Add Criteria`. Click in the cell under **Column Name** and choose **Target Pollutant**. Change the operation by clicking in the cell under **Operation**. For this example select **contains** as the desired **Operation**. Enter the pollutant of interest in the **Value** cell. Enter **PM2_5** in the **Value** cell for this example.

*Note that the filter values are case-sensitive* (e.g., "NOx" will not match a filter value of "NOX"). After applying the filter criterion above, the **Filter Rows** window will look like [Figure 4-4](#filter_rows_to_show_only_strategies_targeting_nox).

<a id=filter_rows_to_show_only_strategies_targeting_nox></a>

![Filter Rows to Show Only Strategies Targeting NO~x][filter_rows_to_show_only_strategies_targeting_nox]

[filter_rows_to_show_only_strategies_targeting_nox]: images/Filter_Rows_to_Show_Only_Strategies_Targeting_NOx.png
**Figure 4-4. Filter Rows Window.**

Click `OK` and the Control Strategy Manager will show only strategies that targeted PM2\_5.

After reviewing the information available on the Control Strategy Manager for PM2_5 measures, click the `Reset` button (<img src="images/Reset_Button.png"/>) to remove the filter and sort criteria.

For more information on performing sorting, filtering, formatting, and other operations on the table that shows the control strategies used for the Control Strategy Manager, refer to the [Introduction to the Control Measure Manager ](./ch3_control_measure_manager.md#Intro3).

### Copying Control Strategies

Existing strategies can be copied to create new control strategies, regardless of whether they have been run. If you copy a strategy and then edit the newly created strategy, you will not be changing any settings for the original strategy, so this is a safe way to start working with your own strategies. When a strategy is copied, it retains all of the settings from the original strategy except for the information on the Outputs tab, and the output summary information that is shown on the Summary tab.

**Step 3-4: Copy Control Strategies.** Click the **Select** checkbox next to one of the strategies (e.g., "Least Cost Example) and then click `Copy`. A new strategy will be added to the Control Strategy Manager with the name "Copy of *original strategy name*".

Note that multiple strategies can be copied at once by selecting as many strategies as needed before clicking `Copy`.

### Removing Control Strategies

A control strategy creator and the EMF Administrator can remove control strategies from CoST. Strategies should be removed with caution, because there is no 'undo' for this operation.

**Step 3-5: Remove Control Strategies.**  Click the **Select** checkbox next to one of the strategies and then click `Remove`. As an example, select the strategy you just copied in the previous subsection. When prompted to confirm removal of the control strategy, a pop-up menu  will appear [Figure 4-5](#confirm_strategy_deletion) where the user can select whether to remove output datasets associated with the strategy that they would like to remove. The first check box is to remove the following output data: Strategy results, messages, and summaries assocated with the control strategy that is being deleted.  The second check box is to remove the Controlled inventories associated with the control strategy that is being deleted.  Check both check boxes, so that all datasets created by the control strategy are cleaned up when the strategy is deleted. The selected strategy and output datasets will be removed from the table of strategies in the Control Strategy Manager.

<a id=confirm_strategy_deletion></a>

![Confirm_Strategy_Deletion][confirm_strategy_deletion]

[confirm_strategy_deletion]: images/Confirm_Strategy_Deletion.png
**Figure 4-5. Confirm Strategy Deletion Window.**


*Note that if you select more than one control strategy before clicking `Remove`, all of the selected strategies will be removed.*

### Creating a New Control Strategy

**Step 3-6: Create New Control Strategies.**  To create a new control strategy, click the `New` button in the Control Strategy Manager. The **Create New Control Strategy** window will appear with a text box to name the new strategy. Enter a name that is different from any of the existing control strategies (e.g., **Least Cost 2017 NOx Example**) and then click `OK`.

An **Edit Control Strategy** window for your newly created strategy will appear ([Figure 4-6](#edit_control_strategy_window)). The window has five tabs: Summary, Inventories, Measures, Constraints, and Outputs. This window and how to fill in the information on these tabs is discussed in more detail in [Section](#inputs_to_control_strategies_section).

<a id=edit_control_strategy_window></a>

![Edit Control Tab Summary Window][edit_control_strategy_window]

[edit_control_strategy_window]: images/Least_Cost_2017_NOx_for_Training.png

**Figure 4-6. Edit Control Strategy Window.**

For this example, edit the following fields in the **Edit Control Strategy** window:

* Set `Target Year` to **2017**
* Set `Type of Analysis` to **Least Cost**
* Set `Target Pollutant` to **NOX**

Click `Save` to save the new strategy and click `Close` to close the **Edit Control Strategy** window.

If the new strategy does not appear in the Control Strategy Manager, first click the `Reset` button on the toolbar to remove any filters that have been applied to the strategies. If the strategy still does not appear, click `Refresh` at the top right of the Control Strategy Manager to obtain new data from the server.

### Editing Control Strategies

**Step 3-7: Edit Control Strategies.** Click the **Select** checkbox next to the new strategy (i.e., the strategy created in Step 2-6) and then click `Edit`. If you have permission to edit the strategy (i.e., you are its creator or an Administrator), the **Edit Control Strategy** window will appear with the **Summary** tab visible ([Figure 4-7](#summary_tab_of_edit_control_strategy_window)). Note that if you had selected multiple control strategies before clicking `Edit`, they each would have opened in their own window. The tabs on the Edit Control Strategy window are listed in [Table 4-3](#tabs_of_the_edit_control_strategy_window_table). The contents of these tabs are described in detail in [Section 4](#Inputs4).

<a id=tabs_of_the_edit_control_strategy_window_table></a>
**Table 4-3. Control Strategy Manager Summary Tab**

Tab|Description
--------------------|-------------------------------------------------------------------------------------------------------------
Summary|Shows you high-level information about the strategy, such as its Name and the Target Pollutant.
Inventories|From which you can specify the emission inventories to use as input to the strategy and filters for those inventories.
Measures|Allows you to specify the classes of measures to include in the strategy, or select specific measures to include.
Constraints|Allows you to specify constraints for the strategy, such as a maximum cost per ton.
Outputs|Shows the results from the strategy after it has been run.

<a id=summary_tab_of_edit_control_strategy_window></a>

![Summary Tab of Edit Control Strategy Window][summary_tab_of_edit_control_strategy_window]

[summary_tab_of_edit_control_strategy_window]: images/Summary_Tab_of_Edit_Control_Strategy_Window.png
**Figure 4-7. Edit Control Strategy Summary Tab.**

<a id=Inputs4></a>

## Inputs to Control Strategies

Control strategies are defined by a series of fields that must be set prior to running the strategy. These fields are described in this section.

<a id=SummaryInputs4></a>

### Inputs on the Summary Tab

Along with the name and description of the strategy, the Control Strategy **Summary** tab defines the type of analysis to use for the strategy, spatial-temporal parameters, the target pollutant, and the discount rate.  To set the fields for the strategy on the **Summary** tab (see [Figure 4-5](#summary_tab_of_edit_control_strategy_window)), follow the steps below. Note that the fields on the Summary tab missing from this list are automatically set by CoST, and are discussed in [Fields Automatically Set by CoST Section](#fields_automatically_set_by_cost_section). *Fields that are contained within boxes with either white backgrounds or that are pull-down menus are editable; fields that are not contained within boxes are set by the software and cannot be changed by the user.*

**Step 4-1: Set the Control Strategy Name and Description.** If one has not already been specified, enter a unique and descriptive `Name` for the control strategy. For this example set `Name` to **Least Cost 2017 NOx Example**. Enter a `Description` of the purpose for the control strategy, and any other information relevant and useful to describe the strategy.

Optionally, use the `Project` pull-down menu to select a project for which this strategy run was performed (e.g., Ozone NAAQS). Projects are set in the EMF and are used as a means of grouping related strategies that were performed in support of a common goal.

**Step 4-2: Set Strategy Type.** Use the `Type of Analysis` pull-down menu to set the type of algorithm used to match the control measures with sources (e.g., Maximum Emissions Reduction, Least Cost). For this example, select **Least Cost** as the analysis type.

Optionally specify whether the strategy `Is Final` to finalize and archive the strategy. Once marked as final, a strategy may not be rerun. For this exercise, do not check the `Is Final` checkbox.

**Step 4-3: Set the Cost Year.** Use the `Cost Year` pull-down menu to set the year to use for estimating the costs of a strategy. For this exercise, set `Cost Year` to **2013**. All cost data specified for the control measures will be converted to this year using the Gross Domestic Product (GDP): Implicit Price Deflator (IPD), issued by the U.S. Department of Commerce, Bureau of Economic Analysis. Details of the computation used are given in the [Control Strategy Tool (CoST) Development Document](Add link). As there is a 1- to 2-year lag between the current year and the latest available cost year data, the `Cost Year` cannot be set to the current year.

**Step 4-4: Set the Target Year.** The target year should correspond to the input inventory or inventories (e.g., 2020, 2030). For control measure efficiency records to be considered for a strategy, the specified effective date for the record must be equal to or earlier than the target year. Note that any control measure efficiency records that come into effect *after* the target year will not be considered for use in the strategy. For this example set `Target Year` to **2017**.

Optionally set the name of the geographic `Region` to which the strategy is to be applied.  This setting is informational only and does not impact the strategy results. `Region` is different from the concept of "locale" used in the control measure efficiency records to indicate the state or county code to which the record applies. For this example, set `Region` to **NC**.

**Step 4-5: Set the Target Pollutant.** The target pollutant is the pollutant of primary interest for emissions reduction for the control strategy. The Least Cost and Maximum Emissions Reduction algorithms will apply reductions to the target pollutant. For the Multi-Pollutant Maximum Emissions Reduction strategy, one or more pollutants must be selected from the `Target Pollutant` pull-down menu.

Note that reductions of pollutants other than the selected target pollutant (e.g., PM<sub>10</sub>, PM<sub>2.5</sub>, elemental carbon [EC], organic carbon [OC]) will be included in strategy results if those pollutants both appear in the inventories input to the strategy and they are affected by measures applied as part of the strategy. These pollutants are sometimes referred to as "co-impact pollutants", because the impact on the emissions could be either a reduction (i.e., a benefit) or an increase (i.e., a disbenefit).

If **Multi-Pollutant Maximum Emissions Reduction** is selected from the `Type of Analysis` pull-down menu, set the `Target Pollutant` using the `Set` button next to this field to select from a list of pollutants to use for the strategy.  Select the pollutants to include in the strategy from the pollutant list using Control-Click.

For this Least Cost example, use the pull-down menu to set `Target Pollutant` to VOC.

**Step 4-6: Set the Discount Rate.** The Discount Rate** (i.e., interest rate) is used in the calculation of the annualized capital cost for control measures when appropriate data are available. For this example, set the `Discount Rate` to **7.0**. Note that the discount rate typically does not affect strategies for area or mobile sources.

**Step 4-7: Use Cost Equations.** When the `Use Cost Equations` checkbox is checked, cost equations will be included in the strategy run; otherwise only cost per ton (CPT) related cost estimates will be used. For this exercise, leave `Use Cost Equations` checked to use the Least Cost equations selected as the `Type of Analysis` in Step 2-9.

**Step 4-8: Apply CAP measures on HAP Pollutants.** When the `Apply CAP measures on HAP Pollutants` checkbox is checked, a CAP-to-HAP pollutant mapping is used to apply CAP reductions to the corresponding HAP pollutants at the same emission reduction percentage. For this exercise, leave `Apply CAP measures on HAP Pollutants` unchecked so that HAP pollutant controls will not be used.

**Step 4-9: Include Measures with No Cost Data.** When the `Include Measures with No Cost Data` checkbox is checked, measures with control efficiencies but without cost data are included in the strategy run; otherwise they are not included. These are typically measures with no cost data specified or measures that use a cost equation to compute cost, but for which there is not enough data for the source in the inventory to fill in the equations variables. For this exercise, leave `Include Measures with No Cost Data` checked.

**Step 4-10: Specify whether Major Pollutant must match Target.** When the checkbox is checked, then the major pollutant must be the same as the target. For this exercise, leave `Major Pollutant must match Target` checked.

<a id=fields_automatically_set_by_cost_section></a>

### Fields Automatically Set by CoST


Some fields of a control strategy that appear on the **Summary** tab are set automatically by the CoST software and are not specified by the user. Note that some of these summarize the results of the strategy analysis, so information for them is not available until after the strategy has been run. The automatically set fields are described in [Table 4-4](#fields_on_the_control_strategy_summary_tab_automatically_set_by_cost_table).

<a id=fields_on_the_control_strategy_summary_tab_automatically_set_by_cost_table></a>
**Table 4-4. Fields Automatically Set by CoST**

Field|Description
----------------------------------|------------------------------------------------------------------------------------------------------
Creator|The name of the person who created the strategy.
Last Modified Date|The date and time when the strategy was last modified.
Copied From|The name of the strategy that this strategy was copied from, if any.
Start Date|The date and time on which the strategy run was most recently started, or "Not started" if the strategy has never been run.
Completion Date|The date and time on which the strategy run was most recently completed. If the run has not completed, this field shows the run status of either "Not started", "Running", "Waiting", "Completed", or "Failed".
Running User|The name of the user who most recently ran the strategy.
Total Annualized Cost|The total annualized cost of applying the strategy.
Target Poll. Reduction (tons)|The absolute emissions reduction achieved for the target pollutant, in tons.

<a id=inputs_on_the_inventories_tab_section></a>

### Inputs on the Inventories Tab

This section describes how to set the inventory inputs for a Control Strategy on the Edit Control Strategy window **Inventories** tab ([Figure 4-8](#inventories_tab_of_edit_control_strategy_window)). Click on the **Inventories** tab.  The `Inventories to Process` table near the top of the tab lists the emissions inventories for which the control strategy will be run. A control strategy can have one or more emissions inventories as input. Before inventories can be selected for use in the strategy, they must already have been imported into the EMF using either the `Import` item on the `File` menu of the EMF Main Window or through the Dataset Manager. The CoST application comes preloaded with several example inventories for training purposes.

CoST/EMF supports both one-record-per-line (ORL) and flat file (FF10) inventory formats. Point inventories have information about emissions sources with specific locations, which are specified using latitude and longitude. Nonpoint, nonroad, and onroad inventories contain data aggregated to the county level. *Note that IDA inventories are not supported by CoST and need to be converted to ORL or FF10 prior to use with CoST.* The EMF database stores the data for the emissions inventories along with metadata about the inventories in its PostgreSQL ([http://www.postgresql.org](http://www.postgresql.org)) database.

<a id=inventories_tab_of_edit_control_strategy_window></a>

![Inventories Tab of Edit Control Strategy Window][inventories_tab_of_edit_control_strategy_window]

[inventories_tab_of_edit_control_strategy_window]: images/Inventories_Tab_of_Edit_Control_Strategy_Window.png
**Figure 4-8. Edit Control Strategy Inventories Tab.**

**Step 4-11: Add an Inventory to Control Strategy.** To add one or more inventories to the `Inventories to Process` table for the control strategy, click the `Add` button on the **Inventories** tab. From the **Select Datasets** window that appears, use the `Choose a dataset type` pull-down menu to select the type of inventory to add (e.g., Flat File 2010 Nonpoint). The file browser will display the inventories of the specified type ([Figure 4-9](#selecting_inventory_datasets_for_a_control_strategy)). If there are many inventories in the list, narrow down the list (e.g., to find inventories for 2017) by entering a search string in the `Dataset name contains` field and pressing the `Enter` key on your keyboard.

To select an inventory to use for the strategy from the **Select Datasets** window, click on the name of the inventory in the table. For this exercise, after choosing **Flat File 2010 Nonpoint** from the `Choose a dataset type` pull-down menu, select **2017eh\_from\_nonpoint\_2011NEIv2\_NONPOINT\_20141108\_09mar2015\_v0\_FIPS\_37** and then click `OK`. The inventory file will then be displyed in the `Inventories to Process` window of the **Inventories** tab.

<a id=selecting_inventory_datasets_for_a_control_strategy></a>

![Selecting Inventory Datasets for a Control Strategy][selecting_inventory_datasets_for_a_control_strategy]

[selecting_inventory_datasets_for_a_control_strategy]: images/Selecting_Inventory_Datasets_for_a_Control_Strategy.png
**Figure 4-9. Selecting Inventories for a Control Strategy.**

**Step 4-12: Add a Second Inventory to Control Strategy.** Click the `Add` button to add a second inventory to the control strategy. Select **Flat File 2010 Point** from the `Choose a dataset type` pull-down menu and add **2017eh\_from\_ptnonipm\_2011NEIv2\_POINT\_20140913\_revised\_20150115\_10mar2015\_v0\_FIPS\_37**.

*Note: To select multiple inventories, hold down the control key while clicking the additional inventories.*

**Step 4-13: Remove an Inventory from a Control Strategy.** To remove inventories from a control strategy, click the **Select** checkbox next to the inventory to remove and click the `Remove` button below the `Inventories to Process` table ([Figure 4-8](#inventories_tab_of_edit_control_strategy_window)). Click `Yes` to confirm deletion of the selected inventories. There is no need to remove either of the inventories for this example strategy.

**Step 4-14: Set Dataset Versions.** Note that multiple versions of the inventories may be available within the EMF. The EMF supports dataset versioning to facilitate reproducibility of historical runs. To specify which version of an inventory to use, check the **Select** checkbox next to a single inventory file in the `Inventories to Process` table and then click the `Set Version` button. A window will appear with a pull-down menu that lists the versions available for the selected inventory. Choose the desired version from the menu and then click the `OK` button to set the version to use for the strategy. You will then see the version number in the **Version** column of the `Inventories to Process` table. Note that the initial version of a dataset is always version **0**. There is no need to change the inventory versions for the example exercises.

**Step 4-15: View Dataset Properties.** To see the properties (i.e., metadata) for an inventory dataset, click the checkbox in the **Select** column of the `Inventories to Process` table and then click `View`. The **Dataset Properties View** window will appear ([Figure 4-10](#dataset_properties_view_window_for_an_emissions_inventory)). The **Dataset Properties View**  window provides information about the selected inventory dataset and has multiple tabs as described in [Table 4-5](#tabs_of_the_dataset_properties_view_and_edit_windows_table).

<a id=tabs_of_the_dataset_properties_view_and_edit_windows_table></a>
**Table 4-5. Dataset Properties View Window Tabs**

Tab|Description
-------------|------------------------------------------------------------------------------------------------------------
Summary|Shows high-level properties of the dataset
Data|Provides access to the actual inventory data so that you can view the data that will be used in the control strategy
Keywords|Shows additional types of metadata not found on the Summary tab
Notes|Shows comments that users have made about the dataset and questions they may have
Revisions|Shows the revisions that have been made to the dataset
History|Shows how the dataset has been used in the past
Sources|Shows where the data came from and where it is stored in the database, if applicable
QA|Shows QA summaries that have been made of the dataset (e.g., state summaries, county summaries)

In addition to the different tabs, there are buttons at the bottom of the Data Properties window. The `Edit Properties` button displays the **Dataset Properties Editor** for *changing* (as opposed to viewing) the properties of the Dataset. The `Edit Data` button displays the **Dataset Versions Editor** for editing the actual data of the dataset by adding new versions. The `Refresh` button updates the data on the **Dataset Properties View** window with the latest information available from the server. The `Export` button exports the dataset data to a file. The `Close` button closes the **Dataset Properties View** window.

<a id=dataset_properties_view_window_for_an_emissions_inventory></a>

![Dataset Properties View Window for an Emissions Inventory][dataset_properties_view_window_for_an_emissions_inventory]

[dataset_properties_view_window_for_an_emissions_inventory]: images/Dataset_Properties_View_Window_for_an_Emissions_Inventory.png
**Figure 4-10. Dataset Properties View Window.**

Close the **Dataset Properties View** window by clicking `Close`.

**Step 4-16: View Inventory Data.** To view the inventory data itself (as opposed to just the metadata), check the **Select** checkbox next to an inventory file in the `Inventories to Process` table in the **Inventories** tab of the **Edit Control Strategy** window and click the `View Data` button. A Data Viewer window will appear that shows the actual data for the selected inventory ([Figure 4-11](#data_viewer_for_an_emissions_inventory)). The data shown here are different from the metadata in the Dataset Properties View window. *Note that the View Data button is a shortcut. The Data Viewer can also be brought up from the Data tab of the Dataset Properties Viewer.*

<a id=data_viewer_for_an_emissions_inventory></a>

![Data Viewer for an Emissions Inventory][data_viewer_for_an_emissions_inventory]

[data_viewer_for_an_emissions_inventory]: images/Data_Viewer_for_an_Emissions_Inventory.png
**Figure 4-11. Data Viewer Display of an Emissions Inventory.**

As inventories can contain a lot of data, only the first 300 rows of an inventory are transfered from the server to the Data Viewer by default. The fields in the upper right corner of the window in the area labeled "**Current**" provide information about how many rows the inventory has, and which rows are currently visible. The **Data Viewer** works similarly to a web search engine that shows the results in pages, and the pagination arrows near the upper right corner of the window facilitate moving between pages of data. Use the pagination arrows in the upper right corner of the window to see how they work. Go to first record, go to previous page, give a specific record, go to next page, and go to last record.

The data sorting can be controlled by entering a comma-separated list of columns in the `Sort Order` field and then clicking `Apply`. Note that a descending sort order can be specified by following the column name with "desc" (e.g., "ANN\_VALUE desc, REGION_CD" will sort by decreasing annual emissions and then by county).

If you enter a `Row Filter`, and then click `Apply`, the **Data Viewer** will find rows that meet the specified criteria. Examples of the syntax for row filters are given in [Table 4-6](#examples_of_row_filters_table). See [Chapter 6](./ch6_example_sql.md) for additional row filter examples.

<a id=examples_of_row_filters_table></a>
**Table 4-6. Row Filter Examples**

Filter Purpose|SQL Where Clause
--------------------------------------------------------------------------------|--------------------------------------------------------------------
Filter on a particular set of SCCs|`scc like '246%' or scc like '261%'`
Filter on a particular set of pollutants|`poll in ('PM10', 'PM2\_5')`<br/>*or*<br/>POLL = 'PM10-PRI' or POLL = 'PM25-PRI'`
Filter sources only in NC (REGION\_CD = 37) ;<br/>note that REGION\_CD column format is State + County FIPS code (e.g., 37017)|`substring(REGION\_CD,1,2) in ('37')` or REGION\_CD like '371' for only sources in NC with a county FIPS code that starts with 1.
Filter sources only in NC (37) and include only NO~x and VOC pollutants|`substring(REGION\_CD,1,2) = '37' and poll in ('NOX', 'VOC')`<br/>*or*<br/>`REGION\_CD like '37%' and (poll = 'NOX' or poll = 'VOC')`


Click `Close` to close the **Data Viewer** window.


The next few paragraphs provide information on how the options at the lower portion of the **Inventories** tab work.

Some control strategy algorithms (e.g., Apply Measures in Series) are designed to process the inventories iteratively and produce results for each inventory. However, the "Least Cost" and "Least Cost Curve" strategy types can merge the input inventories from multiple sectors together prior to processing them, thereby facilitating cross-sector analyses. The `Merge Inventories` checkbox in the lower right corner of the `Inventories to Process` section of the **Inventories** tab controls whether multiple inventories will be merged together prior to applying the strategy algorithm, such as for Least Cost or Least Cost Curve runs. Otherwise, each inventory will be processed independently to create separate, independent results.

The fields in the `Filters` section of the **Inventories** tab of control whether the entire inventory is processed in the strategy or just a portion of it. The `Inventory Filter` field sets a general filter that can be entered using the same syntax as a Structured Query Language (SQL) "where clause". Any of the columns in the inventory can be used in the expression. Examples include: "\`SCC like '212%'\`" to limit the analysis to apply only to inventory records for which the SCC code starts with 212, and "\`FIPS like '06%' or FIPS like '07%'\`" to limit the strategy analysis to apply only to inventory records with Federal Information Processing Standards (FIPS) numeric state-county codes starting with 06 or 07. Additional examples of filters are shown in [Table 4-6](#examples_of_row_filters_table).

Note: for the Multi-Pollutant Max Emission Reduction strategy type the `Inventory Filter` has been moved from the **Inventories** tab to the **Constraints** tab. (you are currently viewing the Least Cost Curve Strategy at this point in the exercise, so the Inventory Filter is on the **Inventories** tab) For the Multi-Pollutant Max Emission Reduction strategy it is possible to specify the `Inventory Filter` differently for each target pollutant instead at the strategy level. Note that within the inventory filter, either upper or lower case may be used to refer to the column names and for the SQL keywords; the specified values within single quotes, however, are case sensitive (e.g., 'NOx' is different from 'NOX'). Adding an inventory filter will not have an effect on the strategy until the strategy is run.

If an inventory filter includes only specific pollutants, then pollutants not specified by the filter will not be considered for the computation of co-impacts from a strategy. In addition, pollutants like EC and OC that are not traditionally included in input inventories will not be included in the results unless the inventory has been preprocessed to include EC and OC.

The `County Dataset` filter allows another way to filter the inventory. This field sets an EMF dataset with a list of counties to which control measures will apply during the strategy run. Control measures will be applied only to counties that are included in this list. The County Dataset pull-down menu will show the names of the available CSV datasets in the EMF that have the dataset type 'List of Counties (CSV)'. Note that CSV files from which these county datasets are created must have at least two columns of data. Also, the first row of the file must be the column names, and one of the columns must have a name that starts with "FIPS". CoST will assume that this column has the list of FIPS codes that should be controlled. Make sure that leading zeros are present for FIPS codes less than 10000. Note that for the Multi-Pollutant Max Emission Reduction strategy type the `County Dataset` filter has been moved from the **Inventories** tab to the **Constraints** tab. For the Multi-Pollutant Max Emission Reduction strategy it's possible to specify different `County Dataset` filters for each target pollutant instead of at the strategy level.

*Note that only the records of the input inventories that pass both the inventory and county filters will be considered for control measure application.*

**Step 4-17: Set Inventory Filters.** For this exercise, set the following on the **Inventories** tab:  `Inventory Filter`: **\`REGION_CD in ('37077', '37017', '37095')\`**. Note that specifying a list of counties using the `Inventory Filter` is an alternative to specifying a county dataset that has a list of counties to consider controlling in the strategy (as shown in the next bullet). To control only a few counties, it is straightforward to use the `Inventory Filter`; to control more than a few counties, the county dataset method is recommended. In addition, many types of Inventory Filters can be specified using other fields of the inventory depending on the specifications of the control strategy (e.g., `SCC like '231%'`, or `ANN_VALUE>5`).

**Step 4-18: View a County Dataset.** Examine the available county datasets by pulling down the `County Dataset` menu and selecting one of the datasets. After selecting a county dataset, examine the dataset properties by clicking `View`. Display the the actual county dataset data by clicking `View Data`. Select a version of the dataset using the `County Dataset Version` field, which shows the available versions of the selected dataset. This selection is required because the EMF can store multiple versions of each dataset.

For this example, set the `County Dataset` pull-down menu to **Not selected** before proceeding.

<a id=inputs_on_the_measures_tab_section></a>

### Inputs on the Measures Tab

The **Measures** tab appears on the Edit Control Strategy window for all types of strategies . The **Measures** tab sets the control measures to use in a strategy run. There are two mutually exclusive ways to select control measures for inclusion in the control strategy run. The default is to include measures according to their class (see the top half of [Figure 4-12](#measures_tab_of_edit_control_strategy_window)). Currently available classes are **Known** (i.e., already in use), **Emerging** (i.e., realistic, but in an experimental phase), **Hypothetical** (i.e., the specified data are hypothetical), **Obsolete** (i.e., no longer in use), and **Temporary** (controls that are used during the analysis only if the user was the creator of the control measure, therefore other users' temporary measures won't be considered during an analysis). By default, only **Known** measures will be included in the strategy run. The second way to specify measures for inclusion in a strategy run is to select a list of specific measures to consider using for the run. The use of these two methods is described in this section. To select additional classes of measures other than the default 'Known', **hold down the Ctrl key while clicking the desired classes of measures**. To start over with selecting classes, just click on a single class of measure without holding down the Ctrl key. Note that only the measures with the classes selected by the user will be included in the strategy run.

<a id=measures_tab_of_edit_control_strategy_window></a>

![Measures Tab of Edit Control Strategy Window][measures_tab_of_edit_control_strategy_window]

[measures_tab_of_edit_control_strategy_window]: images/Measures_Tab_of_Edit_Control_Strategy_Window.png

**Figure 4-12. Edit Control Strategy Measures Tab.**

**Step 4-19: Select Control Measures by Class.** Click on one of the classes in the `Classes to Include` table and click `Save` to include all measures of a given class in a control strategy. To select multiple classes of measures, hold down the Ctrl key while clicking the desired classes of measures. To start over with selecting classes, just click on a single class of measure without holding down the Ctrl key. Note that only the measures with the classes selected by the user will be included in the strategy run.

**Step 4-20: Select Specific Control Measures.** To select specific measures for inclusion in the strategy, click the `Add` button under the `Measures to Include` table to show the **Select Control Measures** window ([Figure 4-13](#dialog_to_add_specific_control_measures_to_a_strategy)). In this window, set a filter to find all measures with the same control technology. Use the checkboxes in the **Select** column to all of the measures to include in a control strategy. To select all of the measures shown in the **Select Control Measures** window, click the `Select All` button in the toolbar. Uncheck measures to exclude from a strategy. For this example, use the `Filter` to select all sources matching the following criteria: **Abbrev contains SNCR**.

<a id=dialog_to_add_specific_control_measures_to_a_strategy></a>

![Dialog to Add Specific Control Measures to a Strategy][dialog_to_add_specific_control_measures_to_a_strategy]

[dialog_to_add_specific_control_measures_to_a_strategy]: images/Dialog_to_Add_Specific_Control_Measures_to_a_Strategy.png
**Figure 4-13. Select Control Measures Window.**

* Specify an **Order** to control the order in which this group of measures is applied as compared to other groups of measures you have selected. This order of application is particularly relevant to the "Apply Measures in Series" strategy type. When running this type of strategy, since multiple measures can be applied to the sources, they will be applied in increasing numerical order (i.e., measures with order set to 1 will be applied before those with order set to 2).

* `Set Order`: Controls the order in which the current group of measures is applied as compared to other groups of measures selected for the control strategy. This order of application is particularly relevant to the "Apply Measures in Series" strategy type. When running this type of strategy, since multiple measures can be applied to the sources, they will be applied in increasing numerical order (i.e., measures with order set to 1 will be applied before those with order set to 2).
* `Set RE%`: Overrides the values of Rule Effectiveness specified in the measure efficiency records.
* `Set RP%`: Overrides the values of Rule Penetration specified in the measure efficiency records. These two settings are useful for assessing the level of emissions reductions achieved assuming different levels of effectiveness and penetration for the measures. For example, setting the rule penetration to 75% assumes that 75% of the sources are applying the measure and would therefore result in 75% of the emissions reductions it would if it was 100%.
* `Regions`: Sets a county dataset from the `Dataset` pull-down menu and a version of that dataset from the `Version` menu. By setting a `Region` the selected measures will only be applied to counties listed in the selected county dataset.

After selecting some the specific measures and overrides using the **Select Control Measures** window, click `OK` to add the measures to the control strategy. The selected measures will appear on the **Measures** tab. The tab will now look similar to that shown in [Figure 4-14](#measures_tab_showing_specific_measures_to_include). Note that only the table of specific measures and their properties is shown, and the Classes to Include list is no longer shown. If desired, you may repeat the process of selecting specific measures to add new sets of measures to the list of measures to be used for the strategy. Each new group of measures selected can have different settings for the order, RE, RP, and Region.


<a id=measures_tab_showing_specific_measures_to_include></a>

![Measures Tab Showing Specific Measures to Include][measures_tab_showing_specific_measures_to_include]

[measures_tab_showing_specific_measures_to_include]: images/Measures_Tab_Showing_Specific_Measures_to_Include.png
**Figure 4-14. Measures Tab with Selected Control Measures.**

**Step 4-21: Editing Control Measures List.** Specific measures included in a control strategy can be changed by selecting the measure(s) to edit using the checkboxes in the **Select** column of the `Measures to Include` table in the **Measures** tab of the **Edit Control Strategy** window. Select the measures to change and click the `Edit` button. An **Editing Measures** window will appear that supports changes to the measure properties as they apply to the control strategy. Click `OK` to accept the edits or `Cancel` to reject the edits.

**Step 4-22: Removing Control Measures from a Strategy.** To remove specific measures from the list of measures to be included in a strategy run, check the corresponding **Select** checkboxes and then click `Remove`. Click `Yes` to confirm the removal of the selected measures from the control strategy.

For this example, remove all of the individually selected measures by clicking the `Select All` button on the **Measures** tab toolbar and then clicking `Remove` and `Yes` when prompted. Make sure that **Emerging** and **Known** are both selected in the `Classes to Include` list. The Measures tab will again look like [Figure 4-12](#measures_tab_of_edit_control_strategy_window).

<a id=input_on_constraints_tab_section></a>

### Input on Constraints Tab

The **Constraints** tab ([Figure 4-15](#constraints_tab_of_edit_control_strategy_window)) of the **Edit Control Strategy** window can be used to specify constraints for a control strategy to limit how control measures are assigned during the strategy run. For example, a strategy could be set up to not use any measures that cost more than $5,000 per ton (in 2013 dollars) for the target pollutant. Alternatively, a strategy could be defined to only use measures that reduce at least 1 ton of the target pollutant for the source. CoST evaluates the constraints while the source is being matched with the control measures. For example, the emission reduction achieved by applying a measure to a source is not known until the measure and its control efficiency have been selected. Thus, constraint calculations are dependent on both the inventory source and the measure being considered for application to the source. Note that the term "source" here refers to a single row of the emissions inventory, which for point sources is uniquely determined by FIPS, plant, point, stack, segment, and SCC, and for nonpoint sources is uniquely determined by FIPS and SCC. Sources should not be confused with "plants", each of which can contain many sources.

[Table 4-7](#constraints_common_to_multiple_control_strategy_types_table) defines the constraints that are applicable to all strategy types. If the constraint values are not satisfied for a particular control measure and source combination, the measure under consideration will not be applied to the source, and CoST will look for another measure that satisfies all of the constraints.

<a id=constraints_common_to_multiple_control_strategy_types_table></a>

**Table 4-7. Constraints Elements Applicable to All Strategy Types**

Constraint Name|Constraint Description
-------------------------------------|---------------------------------------------------------------------------------------------------------------------
Minimum Emissions Reduction (tons)|If specified, requires each control measure to reduce the target pollutant by at least the specified minimum tonnage for a particular source (down to the plant+point+stack+segment level of specification); if the minimum tonnage reduction is not attainable, the measure will not be applied.
Minimum Control Efficiency (%)|If specified, requires each control measure used in the strategy to have a control efficiency greater than or equal to the specified control efficiency for a particular source and target pollutant.
Maximum 2013 Cost per Ton (\$/ton)|If specified, each control measure must have an annualized cost per ton less than or equal to the specified maximum annualized cost per ton for the target pollutant for each source. This cost is based on 2013 dollars.
Maximum 2013 Annualized Cost (\$/yr)|If specified, each control measure must have an annualized cost less than or equal to the specified annualized cost for each source and target pollutant. This cost is based on 2013 dollars.
Minimum Percent Reduction Difference for Replacement Control (%)|If specified, each control measure must have a percent reduction in emissions with the new measure that is greater than or equal to the specified difference in order for the old control measure to be "replaced by" the new control measure. Incremental controls that add an additional device onto a previously controlled source are not yet supported by CoST except for the Apply Measures in Series strategy type, for which all controls are assumed to be independently applicable. In the event that a combination of two control devices is listed as a control measure (e.g., LNB+FGR) and the combined control efficiency provides an ample increase in the control efficiency over the original efficiency, that combination of the devices can still serve as a replacement control if the source already has a measure applied (e.g., LNB). In the future, instead of requiring an increase in the percent reduction, it may be more useful to specify a minimum additional percent reduction in remaining emissions (e.g., such as one might see when going from a 99% control measure to a 99.5% control measure).

The constraints in [Table 4-7](#constraints_common_to_multiple_control_strategy_types_table) are available in the `All Strategy Types` section of the **Constraints** tab. The `Least Cost` section of the **Constraints** tab is used to specify constraints that are specific to the Least Cost strategy type. These constraints vary based on the type of algorithm selected. [Figure 4-15](#constraints_tab_of_edit_control_strategy_window) shows the constraints specific to the Least Cost strategy algorithm.


Details of algorithm-specific constraints for the Least Cost, Least Cost Curve, and Multi-Pollutant Maximum Emissions Reduction follow.

### Least Cost Algorithm Constraints

Constraints for the Least Cost algorithm, include *either* a `Domain Wide Emissions Reduction` for the target pollutant in tons or a `Domain Wide Percent Reduction` in emissions of the target pollutant. When the strategy is run, CoST will attempt to satisfy the specified reduction using controls selected with the minimum cost. Note that after the strategy run is complete, CoST will fill in the value for the least cost constraint that was not originally specified.

**Step 4-23: Set a Least Cost Constraint.**  Enter a `Domain Wide Percent Reduction(%)` of **15.0** for the `Least Cost` Constraint. Click `Save` to save the changes made to the strategy. Do not close the Edit Control Strategy window yet.

Note that if you were to click `Close` without saving the changes, a prompt will ask if you wish to close the window without saving the changes.

<a id=constraints_tab_of_edit_control_strategy_window></a>

![Constraints Tab of Edit Control Strategy Window][constraints_tab_of_edit_control_strategy_window]

[constraints_tab_of_edit_control_strategy_window]: images/Constraints_Tab_of_Edit_Control_Strategy_Window.png

**Figure 4-15. Edit Control Strategy Constraints Tab.**

### Least Cost Curve Algorithm Constraints

When **Least Cost Curve** is selected from the `Type of Analysis` pull-down menu on the **Summary** tab three constraints settings are available in the `Least Cost Curve` section of the **Constraints** tab:
* `Domain-wide Percent Reduction Increment (%)`: increment for subsequent least cost runs to iterate on until the end percent reduction is reached.
* `Domain-wide Percent Reduction Start (%)`: target emissions reduction for the first least cost run.
* `Domain-wide Percent Reduction End (%)`: final emissions reduction target of the emissions reductions for the incremental least cost runs.

The Least Cost Curve strategy uses all three constraints in an iterative control run. First it will run the least cost strategy with the percent reduction specified as the value of **Domain-wide Percent Reduction Start (%)**, it will then add the **Domain-wide Percent Reduction Increment (%)** to the starting percent value and will run the least cost strategy at that value (i.e., starting value + increment). It will continue running strategies for each increment until it reaches the value of **Domain-wide Percent Reduction End**. Note that it may not be possible to achieve some of the selected percent reductions, in which case CoST will generate the same result for that increment as the Maximum Emissions Reduction would generate.

### Multi-Pollutant Maximum Emissions Reduction Algorithm Constraints

The Multi-Pollutant Maximum Emissions Reduction strategy type presents a different **Constraints** Tab than the other strategy types. Since this strategy type is running goals on numerous target pollutants (e.g. PM<sub>2.5</sub>, NO<sub>x</sub>, SO<sub>2</sub>), the constraints presented in [Table 4-7](#constraints_common_to_multiple_control_strategy_types_table) are combined with an inventory filtering capability (see the [Section on the Inputs to the Inventory Tab](#inputs_on_the_inventories_tab_section)) to allow for pollutant-specific constraints. The Multi-Pollutant strategy **Constraints** tab interface is shown in [Figure 4-16](#constraints_tab_for_multi_pollutant_maximum_emission_reduction_strategy).

<a id=constraints_tab_for_multi_pollutant_maximum_emission_reduction_strategy></a>

![Constraints Tab (for Multi-Pollutant Maximum Emission Reduction strategy type) of Edit Control Strategy Window][constraints_tab_for_multi_pollutant_maximum_emission_reduction_strategy]

[constraints_tab_for_multi_pollutant_maximum_emission_reduction_strategy]: images/Constraints_Tab_for_Multi_Pollutant_Maximum_Emission_Reduction_Strategy.png
**Figure 4-16. Multi-Pollutant Strategy Constraints Tab.**

The first step in configuring a Multi-Pollutant Maximum Emissions Reduction strategy is to select the target pollutants from the `Target Pollutant` list on the **Summary** tab.  The pollutants selected from this list will appear in the table on the **Constraints** tab. Note that the order that the pollutants are added to the `Target Pollutant` list is important because it sets the order by which controls are applied in the multi-pollutant strategy.

To set the constraints and filters for each pollutant, click the checkbox in the **Select** column next to a pollutant on the **Constraints** tab and click `Edit`. The **Edit Control Strategy Target Pollutant** window will appear for setting the constraints and filters to use as the control strategy for the selected pollutant. [Figure 4-17](#edit_target_pollutant_dialog_of_edit_control_strategy_window) shows an example **Edit Control Strategy Target Pollutant** window. The fields in the Edit Control Strategy Window include:

* `Minimum Emissions Reduction(tons)`: sets the minimum emissions reduction tonnage for a particular source (down to the plant+point+stack+segment level of specification); only control measures that meet this threshold will be considered for the strategy
* `Minimum Control Efficiency(%)`: requires that control measures used in the strategy have a control efficiency greater than or equal to the specified percentage for a particular source and target pollutant
* `Maximum Cost per Ton($/ton)`: requires that control measures used in the strategy have an annualized cost per ton less than or equal to the specified maximum annualized cost per ton for the target pollutant for each source. This cost is based on 2013 dollars
* `Maximum Annualized Cost($/yr)`: requires that control measures used in the strategy have an annualized cost less than or equal to the specified annualized cost for each source and target pollutant. This cost is based on 2013 dollars
* `Minimum Percent Reduction Difference for Replacement Control(%)`: requires that control measures used in the strategy have a percent reduction of emissions that is greater than or equal to the specified difference in order for an old control measure to be "replaced by" the new control measure
* `Inventory Filter`: sets a general filter that can be entered using the same syntax as a Structured Query Language (SQL) "where clause". Any of the columns in the inventory can be used in the expression. Examples include: "\`SCC like '212%'\`" to limit the analysis to apply only to inventory records for which the SCC code starts with 212, and "\`FIPS like '06%' or FIPS like '07%'\`" to limit the strategy analysis to apply only to inventory records with Federal Information Processing Standards (FIPS) numeric state-county codes starting with 06 or 07.
* `County Dataset/Version`: sets an EMF dataset and version with a list of counties to which control measures will apply during the strategy run. Control measures will be applied only to counties that are included in this list. The County Dataset pull-down menu will show the names of the available CSV datasets in the EMF that have the dataset type 'List of Counties (CSV)'

See [Table 4-7](constraints_common_to_multiple_control_strategy_types_table) for an additional description of how these fields are applied in a control strategy.

<a id=edit_target_pollutant_dialog_of_edit_control_strategy_window></a>

![Edit Target Pollutant Dialog of Edit Control Strategy Window][edit_target_pollutant_dialog_of_edit_control_strategy_window]

[edit_target_pollutant_dialog_of_edit_control_strategy_window]: images/Edit_Target_Pollutant_Dialog_of_Edit_Control_Strategy_Window.png
**Figure 4-17. Multi-Pollutant Target Pollutant Window.**

<a id=Running4></a>

## Running a Strategy and Accessing Its Outputs

### Running a Strategy

**Step 5-1: Run Least Cost Example Strategy.** After setting all of the example strategy inputs and constraints, as described in [Section 4](#Inputs4), click the `Run` button in the **Edit Control Strategy** window to start running the strategy. If the strategy was not saved before clicking `Run`, the changes will automatically be saved to the database. Note if the **Edit Control Strategy** window for the "Least Cost 2017 NOx Example" strategy is closed, it can be reopened by selecting the strategy from the list of strategies in the **Control Strategy Manager** and clicking the `Edit` button.

**Step 5-2: Monitor a Control Strategy Run Status.**  After starting a control strategy run, check the **Status** window near the bottom of the EMF graphical user interface (GUI) to view messages about the run. If all inputs have been properly specified, the status message should show **"Started running control strategy: *your strategy name*"**. Click the `Refresh` button on the **Status** window to see immediate status updates; the **Status** window will autoupdate every 1-2 minutes.

If the strategy runs successfully, one message will be displayed for each inventory that completes, such as **"Completed processing control strategy input dataset: *dataset\_name*"**. When the entire run has finished, the following message will be displayed: **"Completed running control strategy: *your strategy name*"** in the **Status** window. Otherwise, message will display stating that the strategy failed with information about why it failed.

### List Strategy Outputs

**Step 5-3: List Control Strategy Run Outputs.** Once the strategy run completes, click on the **Outputs** tab in the **Edit Control Strategy** window and then click `Refresh` at the bottom of the window to see the outputs from the run listed in the `Output Datasets` table ([Figure 4-18](#outputs_tab_of_edit_control_strategy_window_for_least_cost_strategy) and [Figure 4-19](#sample_outputs_tab_for_a_least_cost_curve_strategy)).

CoST automatically generates three main outputs for successful strategy runs: **Strategy Detailed Result**, **Strategy Measure Summary**, and **Strategy County Summary**. Some strategy types also generate a **Strategy Messages** output. Least Cost and Least Cost Curve strategies generate a **Least Cost Control Measure Worksheet** that lists all of the available control measure options for each source in the inventories.

For all types of strategies, CoST can generate a **Controlled Inventory** on-demand for any of the Strategy Detailed Result datasets. The types of outputs are discussed in more detail in [Section 6](#Outputs4). Note that the output datasets are given unique names that include a timestamp indicating when the strategy was run, including the year, month, day, hour, and minute of the run. A description of how to rename output datasets is provided below.

For additional details on the algorithms that are applied to assign measures to sources as part of a strategy run (other than the descriptions in [Section 1](#Intro4)), please see the [Control Strategy Tool (CoST) Equations Document](https://www3.epa.gov/ttn/ecas/docs/CoST_Equations_Document_2016_03_15.pdf).

<a id=outputs_tab_of_edit_control_strategy_window_for_least_cost_strategy></a>

![Outputs Tab of Edit Control Strategy Window for Least Cost Strategy][outputs_tab_of_edit_control_strategy_window_for_least_cost_strategy]

[outputs_tab_of_edit_control_strategy_window_for_least_cost_strategy]: images/Outputs_Tab_of_Edit_Control_Strategy_Window_for_Least_Cost_Strategy.png
**Figure 4-18. Edit Control Strategy Outputs Window.**

<a id=sample_outputs_tab_for_a_least_cost_curve_strategy></a>

![Sample Outputs Tab for a Least Cost Curve Strategy][sample_outputs_tab_for_a_least_cost_curve_strategy]

[sample_outputs_tab_for_a_least_cost_curve_strategy]: images/Dataset_Properties_Editor_Strategy.png
**Figure 4-19. Dataset Properties Editor for Strategy Detailed Result.**

### Viewing and Editing Properties of the Strategy Outputs

It is possible to perform a number of operations on the strategy outputs. These operations are described in this and the following subsections.

**Step 5-4: View Control Strategy Run Outputs.** The most basic operation is to view the data of the output dataset using the **Data Viewer**. To do this, select one of the outputs on the **Outputs** tab, such as the **Strategy Detailed Result**, and then click `View Data`. (Note that the Strategy Detailed Result is the main output on which the Strategy County Summary and Strategy Measure Summary are based.) This will bring up the Data Viewer showing the contents of the Strategy Detailed Result ([Figure 4-20](#view_data_for_strategy_detailed_result)).

The **Strategy Detailed Result** shows the abbreviation of the measure matched to each of the sources for all of the controlled sources, along with columns that identify each controlled source, information about the cost of applying the measures to the sources and the emissions reductions that resulted. The information computed includes the cost of application and the emissions reduced as a result. Enter a sort order (e.g., `annual_cost desc`) to have the rows sorted in a particular way.

<a id=view_data_for_strategy_detailed_result></a>

![View Data for Strategy Detailed Result][view_data_for_strategy_detailed_result]

[view_data_for_strategy_detailed_result]: images/View_Data_for_Strategy_Detailed_Result.png
**Figure 4-20. Strategy Detailed Result Outputs.**

Clear the entries in the `Sort Order` and `Row Filter` fields on the **Data Viewer** and click `Apply`, all of the data records will be presented in the order in which they appear in the database. More information about the columns included in the detailed result is given in [Table 4-9](#columns_in_the_strategy_detailed_result_table), which is discussed later in the [Strategy Detailed Result Section](#strategy_detailed_result_section).

Click `Close` to exit from the **Data Viewer** when you are finished reviewing the **Strategy Detailed Result**.

**Step 5-5: View Control Strategy Run Output Properties.** From the View/Edit Control Strategy **Outputs** tab, access the properties (metadata) of an output dataset (as opposed to the actual data contained in the output), by selecting an output (for this exercise, select the **Strategy Detailed Result**) on the **Outputs** tab of the **Edit Control Strategy** window and clicking the `Edit` button. This will bring up the**Dataset Properties Editor** for the output dataset ([Figure 4-21](#summary_tab_of_dataset_properties_editor)).

<a id=summary_tab_of_dataset_properties_editor></a>


![Summary Tab of Dataset Properties Editor][summary_tab_of_dataset_properties_editor]

[summary_tab_of_dataset_properties_editor]: images/Summary_Tab_of_Dataset_Properties_Editor.png
**Figure 4-21. Output Dataset Properties Window.**

Notice that the tabs on the **Dataset Properties Editor** are the same as those on the **Dataset Properties Viewer** shown in [Figure 4-8](#dataset_properties_view_window_for_an_emissions_inventory). Editor mode allows many of the fields to be changed, where they could not be changed directly from in the Viewer mode. For example, change the name of the output by replacing the automatically generated name with a more meaningful one (e.g., **Least Cost 2017 VOC for Training Result**) and then click `Save`. Notice that unsaved edites are denoted with an asterisk in the title bar of the window.

Examine the other tabs of the **Dataset Properties Editor** for the **Strategy Detailed Result** output. In particular see the **Keywords** tab, an example of which is shown in [Figure 4-22](#keywords_tab_of_dataset_properties_editor). For the **Strategy Detailed Result**, there are a number of keywords set in the `Keywords Specific to Dataset` section (in the lower part of window). These keywords correspond to the major parameters of the control strategy, such as the COST\_YEAR and the STRATEGY\_TYPE (these specific keywords are available in the list of keywords but are above the portion of the window shown in [Figure 4-22](#keywords_tab_of_dataset_properties_editor)). There are also keywords for the UNCONTROLLED\_EMISSIONS, the TOTAL\_EMISSION\_REDUCTION, and the ACTUAL\_PERCENT\_REDUCTION.

<a id=keywords_tab_of_dataset_properties_editor></a>

![Keywords Tab of Dataset Properties Editor][keywords_tab_of_dataset_properties_editor]

[keywords_tab_of_dataset_properties_editor]: images/Keywords_Tab_of_Dataset_Properties_Editor.png
**Figure 4-22. Output Dataset Properties Keywords Tab.**

The keywords in the `Keywords Specific to Dataset Type` section (the upper part of window in [Figure 4-22](#keywords_tab_of_dataset_properties_editor)) typically contain directives on how to export the data or other data values that are the same for all datasets of the same type. Typically FF10 or ORL inventories will have some of these keywords.

*Note that when the Dataset Properties Editor is open for a dataset no other users can edit that dataset.* Similarly, if a control strategy or control measure is open for *editing*, no other users can edit those items. Users will be able to view these items of the access permissions are set appropriately (see `Intended Use` setting on the **Summary** tab).

After examining the other Dataset Properties tabs, close the **Dataset Properties Editor** by clicking `Save` to save the changes, or `Close` to close without saving changes. For the example exercise, click `Close` and do not save any changes.

<a id=summarizing_the_strategy_outputs_section></a>

## Summarizing the Strategy Outputs

Strategy outputs, particularly **Strategy Detailed Results**, but also the input emissions inventories, can be summarized in many different ways. The ability to prepare summaries is helpful because in many cases there could be thousands of records in a single **Strategy Detailed Result** or emissions inventory. Thus, when the results of a strategy are analyzed or presented to others, it is useful to show the impact of the strategy in a summarized fashion. Frequently, it is helpful to summarize a strategy for each county, state, SCC, and/or control technology. The summaries are prepared using the EMF subsystem that was originally designed to support quality assurance (QA) of emissions inventories and related datasets, for which summaries are also needed. Thus, each summary is stored as the result of a "QA Step" that is created by having CoST run a SQL query. There are many predefined queries stored in the EMF as 'templates', circumventing the need for a user of the system to know SQL to create a summary. Summaries can be added from the QA tab of the **Dataset Properties Editor**, although there is a shortcut available on the **Outputs** tab. Summaries are discussed in more detail in [Section 7](#Summaries4).

**Step 5-6: Selecting Control Strategy Summaries.** Select **Strategy Detailed Result** on the **Outputs** tab of the **Edit Control Strategy** window and then click `Summarize` to open the **QA** tab of the **Dataset Properties Editor**.

To add a new summary from the list of predefined summary templates, click the `Add from Template` button to see a list of predefined QA Steps ([Figure 4-23](#summarizing_a_strategy_detailed_result)). To create summaries of interest, click the mouse button on the summaries to create. For this example, select **Summarize by Control Technology and Pollutant**, **Summarize by County and Pollutant**, **Summarize by Pollutant**. Select multiple summaries (as is illustrated in the figure) using Control-Click.  Click `OK` after selecting the summaries and the selected QA templates will be added to the table on the QA tab ([Figure 4-24](#available_qa_summaries_for_a_strategy_detailed_result)).

<a id=summarizing_a_strategy_detailed_result></a>

![Summarizing a Strategy Detailed Result][summarizing_a_strategy_detailed_result]

[summarizing_a_strategy_detailed_result]: images/Summarizing_a_Strategy_Detailed_Result.png
**Figure 4-23. Add Predefined QA Steps Window.**

<a id=available_qa_summaries_for_a_strategy_detailed_result></a>

![Available QA Summaries for a Strategy Detailed Result][available_qa_summaries_for_a_strategy_detailed_result]

[available_qa_summaries_for_a_strategy_detailed_result]: images/Available_QA_Summaries_for_a_Strategy_Detailed_Result.png
**Figure 4-24. Selected QA Summaries.**

**Step 5-7: Running Control Strategy Summaries.** To run the QA summaries that are listed on the QA tab, first select the summaries of interest and then click `Edit`. The **Edit QA Step** window will appear ([Figure 4-25](#edit_qa_step_window_to_create_a_summary)). Do not edit anything in this window, just view the properties of the QA summary. Click `Run` at the bottom of the window to start the QA summary processing.

Monitor the progress of the QA step in the **Status** window at the bottom of the EMF main window. Once the run is complete, click the `Refresh` button to populate the `Output Name`, `Run Status`, and `Run Date` fields in the **Edit QA Step** window. *Note: as an alternative to clicking Run on several different windows, you can instead select a few summaries and click Run on the QA tab.*

<a id=edit_qa_step_window_to_create_a_summary></a>

![Edit QA Step Window to Create a Summary][edit_qa_step_window_to_create_a_summary]

[edit_qa_step_window_to_create_a_summary]: images/Edit_QA_Step_Window_to_Create_a_Summary.png
**Figure 4-25. Edit QA Step Window.**

**Step 5-8: Viewing Control Strategy Summaries.** To see summarized control strategy output, select a checkbox next to the summary of interest on the **QA** tab and click `View Results` to bring up the **View QA Step Results** window ([Figure 4-26](#view_qa_step_results_window)). Sort and filter the results in this window in the same way as the **Control Measure Manager** and **Control Strategy Manager**. For example, click on the **avg\_cost\_per\_ton** column header to sort on the cost per ton. You can also show the Top n or Bottom n rows using the second and third from the left toolbar buttons. The colorful toolbar buttons on the right support computing statistics, creating plots (if you have the R software package installed on your client machine), and saving the table and plot configurations.

<a id=view_qa_step_results_window></a>

![View QA Step Results Window][view_qa_step_results_window]

[view_qa_step_results_window]: images/View_QA_Step_Results_Window.png
**Figure 4-26. View QA Step Results Window.**

**Step 5-9: Export Control Strategy Summary to Google Earth (KMZ).** If the summary has longitude and latitude information (e.g., a plant, state, or county summary), the EMF has an interface to create Google Earth-compatible Keyhole Markup Language Zipped (.kmz) files by choosing **Google Earth** from the `File` menu of the **View QA Step Results** window. The interface to create these files is shown in [Figure 4-27](#kmz_file_generator). Note that the following detailed result summaries have longitude and latitude:

* Summarize by U.S. County and Pollutant
* Summarize by U.S. State and Pollutant
* Summarize by Plant and Pollutant

<a id=kmz_file_generator></a>

![KMZ File Generator][kmz_file_generator]

[kmz_file_generator]: images/KMZ_File_Generator.png
**Figure 4-27. Google Earth KMZ file Generator.**

In the **Create Google Earth file** window, select a `Label Column` that will be used to label the points in the .kmz file. This label will appear when hovering over a point in the Goggle Earth map. For a plant summary this would typically be plant\_name, for a county summary this would be county, for a state summary, this would be state\_name.

Select a `Data Column` to show the value of a point in the .kmz file.
This value will appear when hovering over a point in the Goggle Earth map. (e.g., total\_emissions\_reduction, total\_annual\_cost, or avg\_cost\_per\_ton). The mouse over information will have the form:

*value from Label column : value from Data Column*.

For a summary that includes multiple pollutants, specify a filter so that data for only one pollutant is included in the .kmz file. Set the `Filter Column` to poll (e.g., pollutant), and then specify a `Filter Value` for a specific inventory pollutant (e.g., NOX).

Use `Minimum Data Cutoff` to limit the points shown in the .kmz file to include only those that reach a certain size threshold (e.g., do not show small sources or sources with a small amount of reduction). Points will only be created for rows in the summary for which the value in the data column exceeds the value given set by `Minimum Data Cutoff`. For example, if the **Data Column** is set to total\_emissions\_reduction, you might enter *1.0* as the `Minimum Data Cutoff` to show only plants with at least 1 ton of reduction.

Use **Icon Scale** to control the size of the points displayed in the Google Earth map. The setting of **Icon Scale** is a real number between 0 and 1. Smaller values produce smaller circles and larger values produce larger circles.

Mouse over the fields in this window to see tooltips with information about the field. After specifying the settings (aside from the Properties file) click `Generate` to create the .kmz file using default file name. The name and location of the output file are set by the `Output File` field.

Click `Save` to save the settings of the Google Earth file `Properties` to a file. Load a saved set of properties using the `Load` button in the **Create Google Earth file** window.

Close the **Create Google Earth file** window by clicking the `X` at the top right corner.

**Step 5-10: Export Control Strategy Summaries to CSV.** From the **Edit QA Step** window ([Figure 4-25](#edit_qa_step_window_to_create_a_summary)) select a folder to export the results to by either typing a directory path in the `Export Folder` field, or by using the `Browse` button to select an output directory. Click the `Export` button and the **Export QA Step Result** will be written to the selected folder on the EMF server.

### Exporting the Strategy Outputs

Return to the **Outputs** tab of the **Edit Control Strategy window** ([Figure 4-18](#outputs_tab_of_edit_control_strategy_window_for_least_cost_strategy)).

**Step 5-11: Export Control Strategy Outputs.** To export the strategy output datasets to the EMF server, enter a folder/directory name into the `Server Export Folder` field on the **Outputs** tab. Use the checkboxes to select one or more results to export from the `Output Datasets` table and click `Export`. The files will be written as ASCII files to the `Server Export Folder`. The resulting CSV files can be imported into a spreadsheet or other database software for analysis. *Note that the dataset must be exported to a location where the EMF application user has read/write access to the folder (e.g., the same directory as specified in the environment variable, `EMF_DATA_DIRECTORY`, in the EMF installation batch file).*

### Analyzing the Strategy Outputs

It is possible to view the strategy results directly from the **Outputs** tab in a sortable, filterable table, similar to the table used by the View QA Step Results window ([Figure 4-26](#view_qa_step_results_window)).

**Step 5-12: Analyze Control Strategy Outputs in CoST.** Use the checkboxes to select one or more results to analyze from the `Output Datasets` table and click **Analyze** to show the **Analyze Control Strategy** window for the selected outputs. An example of a Strategy County Summary analysis is shown in [Figure 4-28](#analyze_control_strategy_window). The results in this window can be sorted, filtered, plotted, and summarized with statistics. An example of a Least Cost Curve Summary is shown in [Figure 4-29](#analyzing_a_least_cost_curve_output). Exit from the **Analyze Control Strategy** window using the `X` at the top of the window.

<a id=analyze_control_strategy_window></a>

![Analyze Control Strategy Window][analyze_control_strategy_window]

[analyze_control_strategy_window]: images/Analyze_Control_Strategy_Window.png
**Figure 4-28. Control Strategy Analysis Window.**

<a id=analyzing_a_least_cost_curve_output></a>

![Analyzing a Least Cost Curve Output][analyzing_a_least_cost_curve_output]

[analyzing_a_least_cost_curve_output]: images/Analyzing_a_Least_Cost_Curve_Output.png
**Figure 4-29. Least Cost Curve Strategy Summary.**

<a id=creating_a_controlled_emissions_inventory_section></a>

## Creating a Controlled Emissions Inventory

CoST can create a controlled emissions inventory that reflects the effects of the strategy by merging the detailed result with the original emissions inventory. Details on controlled inventories are discussed further in the [Controlled Emissions Inventory Section](#controlled_emissions_inventory_section).

**Step 5-13: Creating a Controlled Inventory.** To create a controlled inventory, click the `Controlled Inventory` radio button on the **Outputs** tab of the **Edit Control Strategy** window ([Figure 4-18](#outputs_tab_of_edit_control_strategy_window_for_least_cost_strategy)) and select the **Strategy Detailed Result** in the `Output Datasets` table to enable the `Create` button. *Note that only creators of a strategy or the Administrator can create inventories from a strategy result.*

Click `Create` to receive a prompt to **Enter a name prefix for the controlled inventories**; for this exercise, enter **training** and click the `OK` button. Cancelling this prompt will result in a controlled inventory with no name prefix. The status of the inventory creation will be shown in the Status window. Once the controlled inventory has been successfully created, for all but the Least Cost strategy types the name of the inventory will appear at the far right of the `Output Datasets` table in the row corresponding to the Strategy Detailed Result. For Least Cost strategy types, the controlled inventory will show up as rows in the `Output Datasets` table a "Controlled Inventory" **Result Type**. [Figure 4-30](#controlled_inventory_for_maximum_emissions_reduction_example) shows an example of where the controlled inventory name is located for a Maximum Emissions Reduction example strategy. [Figure 4-31](#controlled_inventory_for_least_cost_curve_example) shows an example of where the controlled inventories are located for a Least Cost example strategy.

<a id=controlled_inventory_for_maximum_emissions_reduction_example></a>

![Controlled Inventory for Maximum Emissions Reduction Example][controlled_inventory_for_maximum_emissions_reduction_example]

[controlled_inventory_for_maximum_emissions_reduction_example]: images/Controlled_Inventory_for_Maximum_Emissions_Reduction_Example.png
**Figure 4-30. Maximum Emissions Reduction Controlled Inventory Entry.**

<a id=controlled_inventory_for_least_cost_curve_example></a>

![Controlled Inventory for Least Cost Curve Example][controlled_inventory_for_least_cost_curve_example]

[controlled_inventory_for_least_cost_curve_example]: images/Controlled_Inventory_for_Least_Cost_Curve_Example.png
**Figure 4-31. Least Cost Curve Controlled Inventory Entry.**

**Step 5-14: Viewing a Controlled Inventory.** To view the data for a controlled inventory generated from all but the Least Cost strategy types, select the `Controlled Inventory` radio button and click `View Data` ([Figure 4-30](#controlled_inventory_for_maximum_emissions_reduction_example)). For Least Cost strategy types use the checkbox in the **Select** column of the `Output Datasets` table to select a controlled inventory, select the `Result` radio button, and click `View Data` ([Figure 4-31](#controlled_inventory_for_least_cost_curve_example)). The data for the controlled inventory dataset will appear in the **Data Viewer**.

To view the data for the input inventory that was merged with the **Strategy Detailed Result** to create the controlled inventory, select the `Input Inventory` radio button and click `View Data`.

### Creating Custom Strategy Outputs

The `Customize` button on the **Outputs** tab is not frequently used, but can generate special types of outputs related to analyses with a Response Surface Model (RSM). These custom outputs are not discussed here.

The remainder of this chapter provides reference documentation on the outputs of strategies and summaries that can be created with CoST. Additional advanced exercises and examples are available in [Chapter 5](./ch5_control_strategy_exercises.md).

<a id=Outputs4></a>

## Outputs of Control Strategies

This section provides details on the contents of each type of CoST output.

<a id=strategy_detailed_result_section></a>

### Strategy Detailed Result

As noted earlier, the Strategy Detailed Result is the primary output from running a control strategy. It is a table of emission source-control measure pairings, each of which contains information about the costs and emission reduction achieved for measures after they are applied to the sources. The contents of this table are described later in this subsection. When generating the Strategy Detailed Result table, some data are needed for CoST to calculate the values of some columns related to costs, such as:

* `Stack Flow Rate (cfs)`: from the emissions inventory
* `Capital Annual Ratio`: from the control measure efficiency record
* `Discount Rate (%)`: from the control measure efficiency record
* `Equipment Life (yrs)`: from the control measure efficiency record
* `Boiler Capacity (MW)`: from the design capacity column of the inventory; units are obtained from the design\_capacity\_unit\_numerator and design\_capacity\_unit\_denominator columns from the inventory. Note that boiler capacity is often blank in inventories, so special steps may need to be taken to fill in this information.

The stack flow rate provides information on the volume of effluent that requires treatment by the control device. The capital annual ratio is used to calculate the capital costs of a control device from an available O&M cost estimate for that device. The capital costs are the one-time costs to purchase and install the device, while the operating and maintenance (O&M) costs are those required to operate and maintain the device for each year. The discount rate and equipment life are used to compute the annualized capital costs for the device. The discount rate can be considered an annual interest rate used to calculate the cost of borrowing money to purchase and install the control device. The annualized capital cost is computed based on the discount rate, and the costs are spread over the life of the equipment. The algorithms to compute these cost breakdowns vary based on whether the input data required to utilize a cost equation are available. This topic is described in further detail in [Table 4-9](#columns_in_the_strategy_detailed_result_table), which is given after an introductory discussion of cost concepts, below. The columns of the Strategy Detailed Result are also given in [Table 4-9](#columns_in_the_strategy_detailed_result_table).

When cost data are provided for the control measures, the resulting costs are also specified in terms of a particular year. To compute the cost results for a control strategy, it is necessary to escalate or de-escalate the costs to the same year in order to adjust for inflation and to allow for consistency in comparing control strategy results. This is done with the following formula:

```
Cost (\$) for a year of interest = (Cost for original cost year x GDP IPD for year of interest) / GDP IPD for original cost year

where the GDP IPD is the Gross Domestic Product.
```
Implicit Price Deflator (IPD) available from the United States Department of Commerce Bureau of Economic Analysis [Table 1.1.9. Implicit Price Deflators for Gross Domestic Product](http://bea.gov/iTable/iTable.cfm?reqid=9&step=3&isuri=1&903=13#reqid=9&step=3&isuri=1&904=2013&903=13&906=a&905=2015&910=x&911=1). The current version used is dated November 24, 2015. An excerpt of this version is shown in [Table 4-8](#excerpt_from_the_gdplev_table_table).


<a id=excerpt_from_the_gdplev_table_table></a>
**Table 4-8. Excerpt from the gdplev Table Used to Convert Data between Cost Years**

Year|GDP IPD| |Year|GDP IPD| |Year|GDP IPD| |Year|GDP IPD
---------|-----------|----|-------|---------|---|-----------|------|-|-------------|-----------
1980|44.377| |1990|66.773| |2000|81.887| |2010|101.221
1981|48.520| |1991|68.996| |2001|83.754| |2011|103.311
1982|51.530| |1992|70.569| |2002|85.039| |2012|105.214
1983|53.565| |1993|72.248| |2003|86.735| |2013|106.929
1984|55.466| |1994|73.785| |2004|89.120| |2014|108.686
1985|57.240| |1995|75.324| |2005|91.988|
1986|58.395| |1996|76.699| |2006|94.814|
1987|59.885| |1997|78.012| |2007|97.337|
1988|61.982| |1998|78.859| |2008|99.246|
1989|64.392| |1999|80.065| |2009|100|

To facilitate the comparison of the costs of control measures with one another, a normalized version of the control measure cost per ton is stored within the control measures database. These costs have all been converted to a consistent "reference year" using the above formula, so that the cost of any measure can be compared with any other even if their cost years differ. Currently, the reference year is 2013. In addition, during the course of the strategy run, the costs are converted (using the above formula) from the reference year to the cost year that was specified as an input to the strategy. The results of the strategy are therefore presented in terms of the specified cost year.

As indicated above, [Table 4-9](#columns_in_the_strategy_detailed_result_table) provides details on the columns of the Strategy Detailed Result.

<a id=columns_in_the_strategy_detailed_result_table></a>
**Table 4-9. Columns in the Strategy Detailed Result**

Column|Description
-------------|--------------------------------------------------------------------------------------------------------
DISABLE|A true-false value that determines whether to disable the control represented on this line during the creation of a controlled inventory.
CM\_ABBREV|The abbreviation for the control measure that was applied to the source.
POLL|The pollutant for the source, found in the inventory.
SCC|The SCC for the source, found in the inventory.
FIPS|The state and county FIPS code for the source, found in the inventory.
PLANTID|For point sources, the plant ID for the source from the inventory.
POINTID|For point sources, the point ID for the source from the inventory.
STACKID|For point sources, the stack ID for the source from the inventory.
SEGMENT|For point sources, the segment for the source from the inventory.
ANNUAL\_COST (\$)|The total annual cost (including both capital and O&M) required to keep the measure on the source for a year.<br/>*a. Default Approach (used when there is no cost equation, or when inputs to cost equation are not available):*<br/>Annual Cost = Emission Reduction (tons) x Reference Yr Cost Per Ton (\$/tons in 2013 Dollars) x Cost Yr GDP IPD / Reference Yr GDP IPD<br/>*b. Approach when Using Type 8 Cost Equation:*<br/>If Stack Flow Rate >= 5.0 cfm Then<br/>Annual Cost = (Annualized Capital Cost + 0.04 x Capital Cost + O&M Cost)<br/>Else<br/>Annual Cost = Default Annualized Cost Per Ton Factor x Emission Reduction (tons) x Cost Yr GDP IPD / Reference Yr GDP IPD
ANN\_COST\_PER\_TON (\$/ton)|The annual cost (both capital and O&M) to reduce one ton of the pollutant.<br/>Ann\_Cost\_Per\_Ton = Annual Cost (\$) / Emis Reduction (tons)
ANNUAL\_OPER\_MAINT\_COST (\$)|The annual cost to operate and maintain the measure once it has been installed on the source.<br/>*a. Default Approach (used when there is no cost equation, or inputs to cost equation are not available):*<br/>= (Annual Cost - Annualized Capital Cost)<br/>Note: if the capital recovery factor was not specified for the measure, it would not be possible to compute Annualized Capital Cost or Annual O&M Costs<br/>*b. Approach when Using Type 8 Cost Equation:*<br/>If Stack Flow Rate >= 5.0 cfm Then<br/>= O&M Control Cost Factor x Stack Flow Rate (cfm) x Cost Yr GDP IPD / Reference Yr GDP IPD<br/>Else<br/>= Default O&M Cost Per Ton Factor x Emission Reduction (tons) x Cost Yr GDP IPD / Reference Yr GDP IPD
ANNUAL\_VARIABLE\_OPER\_MAINT\_COST (\$)|The annual variable cost to operate and maintain the measure once it has been installed on the source.<br/>*a. Default Approach (used when there is no cost equation, or inputs to cost equation are not available):*<br/>= blank (not calculated, no default approach available)<br/>*b. Approach when Using Type 10 Cost Equation:*<br/>= variable\_operation\_maintenance\_cost\_multiplier x design\_capacity x 0.85 x annual\_avg\_hours\_per\_year x Cost Yr GDP IPD / Reference Yr GDP IPD
ANNUAL\_FIXED\_OPER\_MAINT\_COST (\$)|The annual fixed cost to operate and maintain the measure once it has been installed on the source.<br/>*a. Default Approach (used when there is no cost equation, or inputs to cost equation are not available):*<br/>= blank (not calculated, no default approach available)<br/>*b. Approach when Using Type 10 Cost Equation:*<br/>= design\_capacity x 1000 x fixed\_operation\_maintenance\_cost\_multiplier x (250 / design\_capacity) ^ fixed\_operation\_maintenance\_cost\_exponent x Cost Yr GDP IPD / Reference Yr GDP IPD
ANNUALIZED\_CAPITAL\_COST (\$)|The annualized cost of installing the measure on the source assuming a particular discount rate and equipment life.<br/>Annualized\_Capital\_Cost = Total Capital Cost x Capital Recovery Factor (CRF)<br/>Note: if the CRF is not available for the measure, it is not possible to compute the ACC or the breakdown of costs between capital and O&M costs.<br/>CRF = (Discount Rate x (1 + Discount Rate)^Equipment Life) / ((Discount Rate + 1) ^Equipment Life - 1)
TOTAL\_CAPITAL\_COST (\$)|The total cost to install a measure on a source.<br/>*a. Default Approach (used when there is no cost equation or cost equation inputs are not available):*<br/>TCC = Emission Reduction (tons) x Reference Yr Cost Per Ton (\$/tons in 2013 Dollars) x Capital Annualized Ratio x Cost Yr GDP IPD / Reference Yr GDP IPD<br/>*b. Approach when Using Type 8 Cost Equation:*<br/>If Stack Flow Rate >= 5.0 cfm Then<br/>TCC = Capital Control Cost Factor x Stack Flow Rate (cfm) x Cost Yr GDP IPD / Reference Yr GDP IPD<br/>Else<br/>TCC = Default Capital Cost Per Ton Factor x Emission Reduction (tons) x Cost Yr GDP IPD / Reference Yr GDP IPD
CONTROL\_EFF (%)|The control efficiency of the measure being applied, stored in the measure efficiency record.
RULE\_PEN (%)|The rule penetration of the measure being applied, stored in the measure efficiency record, but could be overridden as a strategy setting (see the [Section on the Inputs to the Meaures Tab](#inputs_on_the_measures_tab_section)).
RULE\_EFF (%)|The rule effectiveness of the measure being applied, stored in the measure efficiency record, but could be overridden as a strategy setting (see the [Section on the Inputs to the Meaures Tab](#inputs_on_the_measures_tab_section)).
PERCENT\_REDUCTION (%)|The percent by which the emissions from the source are reduced after the control measure has been applied.<br/>Percent reduction = Control Efficiency (%) x Rule Penetration (%) / 100 x Rule Effectiveness (%) / 100
ADJ\_FACTOR|The factor that was applied by a control program to adjust the emissions to the target year.
INV\_CTRL\_EFF (%)|The control efficiency for the existing measure on the source, found in the inventory.
INV\_RULE\_PEN (%)|The rule penetration for the existing measure on the source, found in the inventory.
INV\_RULE\_EFF (%)|The rule effectiveness for the existing measure on the source, found in the inventory.
FINAL\_EMISSIONS (tons)|The final emission that results from the source being controlled.<br/>= Annual Emission (tons) - Emission Reduction (tons)
EMIS\_REDUCTION (tons)|The emissions reduced (in tons) as a result of applying the control measure to the source.<br/>Emissions reduction = Annual Emission (tons) x Percent Reduction (%) / 100
INV\_EMISSIONS (tons)|The annual emissions, found in the inventory. Note that if the starting inventory had average-day emissions, the average-day value is annualized and the resulting value is shown here. This is necessary to properly compute the costs of the measure.
APPLY\_ORDER|If multiple measures are applied to the same source, this is a numeric value noting the order of application for this specific control measure. The first control to be applied will have a value of 1 for this field, the second will have a value of 2, and so on.
INPUT\_EMIS (tons)|The emissions that still exist for the source after prior control measures have been applied. Usually this is the same as INV\_EMISSIONS (see above), but for the "Apply Measures In Series" strategy type, in which multiple measures are applied to the same source, this is the emissions that are still available for the source after all prior control measures have been applied.
OUTPUT\_EMIS (tons)|The emissions that still exist for the source after the current and all prior control measures have been applied. Usually this is the same as FINAL\_EMISSIONS (see above), but for the "Apply Measures In Series" strategy type, in which multiple measures are applied to the same source, this is the emissions that are still available for the source after the current and all prior control measures have been applied.
FIPSST|The two-digit FIPS state code.
FIPSCTY|The three-digit FIPS county code.
SIC|The SIC code for the source from the inventory.
NAICS|The NAICS code for the source from the inventory.
SOURCE\_ID|The record number from the input inventory for this source.
INPUT\_DS\_ID|The numeric ID of the input inventory dataset (for bookkeeping purposes). If multiple inventories were merged to create the inventory (as can be done for Least Cost strategies), this ID is that of the merged inventory.
CS\_ID|The numeric ID of the control strategy.
CM\_ID|The numeric ID of the control measure.
EQUATION TYPE|The control measure equation that was used during the cost calculations.
ORIGINAL\_DATASET\_ID|The numeric ID of the original input inventory dataset, even if a merged inventory was used for the computation of the strategy, as can be done for Least Cost strategies.
SECTOR|The emissions sector specified for the input inventory (text, not an ID number; e.g., ptnonipm for the point non-IPM sector)
CONTROL\_PROGRAM|The control program that was applied to produce this record.
XLOC|The longitude for the source, found in the inventory for point sources; for nonpoint inventories the county centroid is used. This is useful for mapping purposes.
YLOC|The latitude for the source, found in the inventory for point sources; for nonpoint inventories the county centroid is used. This is useful for mapping purposes.
PLANT|The plant name from the inventory (or county name for nonpoint sources).
REPLACEMENT\_ADDON|Indicates whether the measure was a replacement or an add-on control.<br/>A = Add-On Control<br/>R = Replacement Control
EXISTING\_MEASURE\_ABBREVIATION|This column is used when an Add-On Control was applied to a source; it indicates the existing control measure abbreviation that was on the source.
EXISTING\_PRIMARY\_DEVICE\_TYPE\_CODE|This column is used when an Add-On Control was applied to a source; it indicates the existing control measure primary device type code that was on the source.
STRATEGY\_NAME|The name of the control strategy that produced the detailed result.
CONTROL\_TECHNOLOGY|Indicates the control technology of the control measure.
SOURCE\_GROUP|Indicates the source group of the control measure.
COMMENT|Information about this record and how it was produced; this information is either created automatically by the system or entered by the user.
RECORD\_ID<br/>VERSION<br/>DELETE\_VERSIONS|System specific columns used for tracking primary key and versioning of data

### Strategy Measure Summary

The Strategy Measure Summary output dataset is a table of emission reduction and cost values aggregated by the emissions sector (i.e., an EMF Sector), state/county FIPS code, SCC, pollutant, and control measure. This table contains information only for sources that were controlled during the strategy run. It is generated by running a SQL statement that aggregates the data from the Strategy Detailed Result according to the five categories just listed. The annual cost and emission reduction are calculated by summing all costs and emission reductions for the specified grouping (sector, FIPS, SCC, pollutant, and control measure). The average annual cost per ton is calculated by dividing the total annual costs by the total emission reduction for each measure. The columns contained in this summary and the formulas used to compute their values are shown in [Table 4-10](#columns_in_the_strategy_measure_summary_table). An example Strategy Measure Summary is shown in [Table 4-14](#example_of_strategy_measure_summary_data_table).

<a id=columns_in_the_strategy_measure_summary_table></a>

**Table 4-10. Columns in the Strategy Measure Summary**

Column|Description
--------------------|-----------------------------------------------------------------------------------------------------------------------
SECTOR|The sector for the source (e.g., ptnonipm for the point non-IPM sector)
FIPS|The state and county FIPS code for the source
SCC|The SCC for the source
POLL|The pollutant for the source
CONTROL\_MEASURE\_ABBREV|The control measure abbreviation
CONTROL\_MEASURE|The control measure name
CONTROL\_TECHNOLOGY|The control technology that is used for the measure (e.g., Low NO~x burner, Onroad Retrofit).
SOURCE\_GROUP|The group of sources to which the measure applies (e.g., Fabricated Metal Products - Welding).
ANNUAL\_COST|The total annual cost for all sources that use this measure. This is calculated by summing all source annual costs that use this measure<br/>= sum(annual\_cost)
AVG\_ANN\_COST\_PER\_TON|The average annual cost per ton (\$/ton). This is calculated by dividing the total annual cost by the total emission reduction for all sources for this measure<br/>= sum(annual\_cost) / sum(emis\_reduction)
INPUT\_EMIS|The total of emissions from all sources entering the control measure. This is calculated by summing the input emissions for all sources that were controlled by this measure<br/>= sum(input\_emis)
EMIS\_REDUCTION|The total reduction in emission in tons for all sources for this control measure
PCT\_RED|The percent reduction (%) for all sources controlled by this measure. This is calculated by dividing the total emissions reduction by the total input emissions.<br/>= [sum(emis\_reduction) / sum(input\_emis)] x 100
RECORD\_ID<br/>VERSION<br/>DELETE\_VERSIONS|System specific columns used for tracking primary key and versioning of data


### Strategy County Summary

The Strategy County Summary output dataset is a table of emission reduction and cost values aggregated by emissions sector, county, and pollutant. This dataset includes all of the inventory sources regardless of whether they were controlled. If there is more than one inventory included in the strategy inputs, then all inventories and their associated Strategy Detailed Results are merged and aggregated in this summary. The columns that compose this summary are shown in [Table 4-11](#columns_in_the_strategy_county_summary_table). An example Strategy County Summary is shown in [Table 4-15](#example_of_strategy_county_summary_data_table).

<a id=columns_in_the_strategy_county_summary_table></a>
**Table 4-11. Columns in the Strategy County Summary**

Column|Description
------------------------------------------|------------------------------------------------------------------------------------------------------------------
SECTOR|The emissions sector for the source (i.e., ptnonipm for the point non-IPM emissions sector)
FIPS|The state and county FIPS code for the source
POLL|The pollutant for the source
UNCONTROLLED\_EMIS|The original inventory emission for the county (in tons)
EMIS\_REDUCTION|The total emission reduction for the county (in tons)
REMAINING\_EMIS|The remaining emissions after being controlled (in tons)
PCT\_RED|The percent reduction for the pollutant
ANNUAL\_COST|The total annual cost for the county. This is calculated by summing the annual costs for the county<br/>= sum(annual\_cost)
ANNUAL\_OPER\_MAINT\_COST|The total annual O&M costs for the county. This is calculated by summing the annual O&M costs for the county<br/>= sum(annual\_oper\_maint\_cost)
ANNUALIZED\_CAPITAL\_COST|The total annualized capital costs for the county. This is calculated by summing the annualized capital costs for the county<br/>= sum(annualized\_capital\_cost)
TOTAL\_CAPITAL\_COST|The total capital costs for the county. This is calculated by summing the total capital costs for the county<br/>= sum(total\_capital\_cost)
AVG\_ANN\_COST\_PER\_TON|The average annual cost per ton (\$/ton). This is calculated by dividing the total annual cost by the total emission reduction for the county.<br/>= sum(annual\_cost) / sum(emis\_reduction)
RECORD\_ID<br/>VERSION<br/>DELETE\_VERSIONS|System specific columns used for tracking primary key and versioning of data

<a id=controlled_emissions_inventory_section></a>

### Controlled Emissions Inventory

Another output that can be created is a controlled emissions inventory (introduced earlier in the [Creating a Controlled Emissions Inventory Section](#creating_a_controlled_emissions_inventory_section)). This dataset is not automatically created during a strategy run; instead, a user can choose to create it after the strategy run has completed successfully. When CoST creates a controlled inventory, comments are placed at the top of the inventory file that indicate the strategy that produced it and the high-level settings for that strategy. For the sources that were controlled, CoST fills in the CEFF (control efficiency), REFF (rule effectiveness), and RPEN (rule penetration) columns based on the control measures applied to the sources. It also populates several additional columns toward the end of the ORL inventory rows that specify information about measures that it has applied. These columns are:

* `CONTROL MEASURES`: An ampersand (&)-separated list of control measure abbreviations that correspond to the control measures that have been applied to the given source.
* `PCT REDUCTION`: An ampersand-separated list of percent reductions that have been applied to the source, where percent reduction = CEFF x REFF x RPEN.
* `CURRENT COST`: The annualized cost for that source for the most recent control strategy that was applied to the source.
* `TOTAL COST`: The total cost for the source across all measures that have been applied to the source.

In this way, the controlled inventories created by CoST always specify the relevant information about the measures that have been applied as a result of a CoST control strategy.

### Strategy Messages ###

The Strategy Messages output provides information gathered while the strategy is running that is helpful to the user. The Strategy Messages output is currently created by the following strategy types:

* Project Future Year Inventory
* Max Emission Reduction
* Least Cost (but not Least Cost Curve)

The columns of the Strategy Messages output are described in [Table 4-12](#columns_in_the_strategy_messages_output_table). An example Strategy Messages output is shown in [Table 4-13](#example_of_strategy_messages_output_table).

<a id=columns_in_the_strategy_messages_output_table></a>
**Table 4-12. Columns in the Strategy Messages Output**

Column|Description
------------------------------------------|--------------------------------------------------------------------------------------------------------------
FIPS|The state and county FIPS code for the source, found in the inventory
SCC|The SCC code for the source, found in the inventory
PLANTID|For point sources, the plant ID for the source from the inventory.
POINTID|For point sources, the point ID for the source from the inventory.
STACKID|For point sources, the stack ID for the source from the inventory.
SEGMENT|For point sources, the segment for the source from the inventory.
POLL|The pollutant for the source, found in the inventory
STATUS|The status type of the message. The possible values:<br/><span style="text-decoration: underline">Warning</span> - a possible issue has been detected, but processing did not stop.<br/><span style="text-decoration: underline">Error</span> - a problem occurred that caused the processing to stop.<br/><span style="text-decoration: underline">Informational</span> - it was desirable to communicate information to the user.
CONTROL\_PROGRAM|The control program for the strategy run; this is populated only when using the "Project Future Year Inventory" strategy type.
MESSAGE|Text describing the strategy issue.
MESSAGE\_TYPE<br/>INVENTORY PACKET\_FIPS<br/>PACKET\_SCC<br/>PACKET\_PLANTID<br/>PACKET\_POINTID<br/>PACKET\_STACKID<br/>PACKET\_SEGMENT<br/>PACKET\_POLL<br/>PACKET\_SIC<br/>PACKET\_MACT<br/>PACKET\_NAICS<br/>PACKET\_COMPLIANCE\_DATE|Reserved columns used for another strategy type that was not part of this training exercise.
RECORD\_ID<br/>VERSION<br/>DELETE\_VERSIONS|System specific columns used for tracking primary key and versioning of data

<a id=example_of_strategy_messages_output_table></a>
**Table 4-13. Example of Strategy Messages Output**

fips|scc|plantid|pointid|stackid|segment|poll|status|control_program|message
------|----------|------------|-----|------|---|----------|------------|---|-------------------------------------------
42049|30900201|420490009|942|S942|1|PM2\_5|Warning| |Negative emission reduction (-1693.9)


<a id=example_of_strategy_measure_summary_data_table></a>
**Table 4-14. Example of Strategy Measure Summary Data**

SECTOR|FIPS|SCC|POLL|CONTROL\_MEASURE\_ABBREV|CONTROL\_MEASURE|CONTROL\_TECHNOLOGY|SOURCE\_GROUP|ANNUAL\_COST|AVG\_ANN\_COST\_PER\_TON|EMIS\_REDUCTION
---------|------|-----------|------|-------------|----------------------------------------------|-------------------------|--------------|---------|--------|-----------
ptnonipm|37001|10200906|PM10|PFFPJIBWD|Fabric Filter (Pulse Jet Type);(PM10) Industrial Boilers - Wood|Fabric Filter (Pulse Jet Type)|Industrial Boilers - Wood|$419,294|$12,862|32.6007
ptnonipm|37001|10200906|PM2\_5|PFFPJIBWD|Fabric Filter (Pulse Jet Type);(PM10) Industrial Boilers - Wood|Fabric Filter (Pulse Jet Type)|Industrial Boilers - Wood| | |19.5426
ptnonipm|37001|30500311|PM10|PFFPJMIOR|Fabric Filter (Pulse Jet Type);(PM10) Mineral Products - Other|Fabric Filter (Pulse Jet Type)|Mineral Products - Other|$446,026|$83,379|5.3494
ptnonipm|37001|30500311|PM2\_5|PFFPJMIOR|Fabric Filter (Pulse Jet Type);(PM10) Mineral Products - Other|Fabric Filter (Pulse Jet Type)|Mineral Products - Other| | |2.0939
ptnonipm|37001|30501110|PM10|PFFPJMIOR|Fabric Filter (Pulse Jet Type);(PM10) Mineral Products - Other|Fabric Filter (Pulse Jet Type)|Mineral Products - Other|$110|$147|0.7498
ptnonipm|37001|30501110|PM2\_5|PFFPJMIOR|Fabric Filter (Pulse Jet Type);(PM10) Mineral Products - Other|Fabric Filter (Pulse Jet Type)|Mineral Products - Other| | |0.2605


<a id=example_of_strategy_county_summary_data_table></a>
**Table 4-15. Example of Strategy County Summary Data**

SECTOR|FIPS|POLL|INPUT\_EMIS|EMIS\_REDUCTION|REMAINING\_EMIS|PCT\_RED|ANNUAL\_COST|ANNUAL\_OPER\_MAINT\_COST|ANNUALIZED\_CAPITAL\_COST|TOTAL\_CAPITAL\_COST|AVG\_ANN\_COST\_PER\_TON
------------|-------|------|-----------|-----|--------|---------|-----------|-----------|----------|----------|----------
ptnonipm|37001|VOC|313.8724| |313.8724| | | | | | |
ptnonipm|37001|PM2\_5|33.4717|33.2505|0.2212|99.3391| | | | | |
ptnonipm|37001|NH3|6.9128| |6.9128| | | | | | |
ptnonipm|37001|NOX|146.2904| |146.2904| | | | | | |
ptnonipm|37001|PM10|51.0928|50.7019|0.3909|99.2349|$865,430|$746,831|$83,300|$882,489|$22,363
ptnonipm|37001|SO2|54.3864| |54.3864| | | | | | |


<a id=Summaries4></a>

## Summaries of Strategy Inputs and Outputs

The EMF/CoST system can prepare summaries of the datasets that are loaded into the system, including both the emissions inventory datasets and the Strategy Detailed Result outputs. The ability to prepare summaries is helpful because in many cases there could be thousands of records in a single Strategy Detailed Result. Thus, when the results of a strategy are analyzed or presented to others, it is useful to show the impact of the strategy in a summarized fashion. Frequently, it is helpful to summarize a strategy for each county, state, SCC, or control technology. The power of the PostgreSQL relational database that contains the system data is used to develop these summaries. Currently, they are prepared using the EMF subsystem that was designed to support quality assurance (QA) of emissions inventories and related datasets. Recall that the creation of summaries for strategy outputs was discussed in the [Summarizing Strategy Outputs Section](#summarizing_the_strategy_outputs_section).

Each summary is stored as the result of a "QA Step" that is created by asking CoST to run a SQL query. Summaries can be added to inventory or Strategy Detailed Result datasets by editing the dataset properties, going to the QA tab, and using the available buttons to add and edit QA steps. For more details on how to create summaries, see [Summarizing Strategy Outputs Section](#summarizing_the_strategy_outputs_section). Examples of the types of summary templates available for Point Inventories (the type with the most templates due to the larger number of columns in that inventory type) are:

* "Summarize by Pollutant with Descriptions"
* "Summarize by Pollutant"
* "Summarize by SCC and Pollutant with Descriptions"
* "Summarize by SCC and Pollutant"
* "Summarize by U.S. State and Pollutant with Descriptions"
* "Summarize by U.S. State and Pollutant"
* "Summarize by U.S. County and Pollutant with Descriptions"
* "Summarize by MACT Code, U.S. State and Pollutant with Descriptions"
* "Summarize by Data Source Code, U.S. State and Pollutant with Descriptions"
* "Summarize by U.S. State, SCC and Pollutant with Descriptions"
* "Compare CoST to NEI measures"
* "Roll Up CoST and NEI measures"
* "Summarize by Plant and Pollutant"

Note that the summaries "with Descriptions" have more information than the ones without. For example, the "Summarize by SCC and Pollutant with Descriptions" summary includes the SCC description in addition to the pollutant description. The disadvantage to include the descriptions is that they are a bit slower to generate because information has to be brought in from additional tables than the table being summarized.

Each of the summaries is created using a customized SQL syntax that is very similar to standard SQL, except that it includes some EMF-specific concepts that allow the queries to be defined generally and then applied to specific datasets as needed. An example of the customized syntax for the "Summarize by SCC and Pollutant" query is:

```SQL
select SCC, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by SCC, POLL order by SCC, POLL
```

Notice that the only difference between this and standard SQL is the use of the $TABLE[1] syntax. When this query is run, the $TABLE[1] portion of the query is replaced with the table name used to contain the data in the EMF. Note that most datasets have their own tables in the EMF schema, so you do not normally need to worry about selecting only the records for the specific dataset of interest. The customized syntax also has extensions to refer to another dataset and to refer to specific versions of other datasets using tokens other than $TABLE. For the purposes of this discussion, it is sufficient to note that these other extensions exist.

Some of the summaries are constructed using more complex queries that join information from other tables, such as the SCC descriptions, the pollutant descriptions (which are particularly useful for HAPs), and to account for any missing descriptions. For example, the syntax for the "Summarize by SCC and Pollutant with Descriptions" query is:

```SQL
select e.SCC, coalesce(s.scc_description,'AN UNSPECIFIED DESCRIPTION')::character varying(248) as scc_description, e.POLL, coalesce(p.descrptn,'AN UNSPECIFIED DESCRIPTION')::character varying(11) as pollutant_code_desc, coalesce(p.name,'AN UNSPECIFIED SMOKE NAME')::character varying(11) as smoke_name,p.factor, p.voctog, p.species, coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis from $TABLE[1] e left outer join reference.invtable p on e.POLL=p.cas left outer join reference.scc s on e.SCC=s.scc group by e.SCC,e.POLL,p.descrptn,s.scc_description, p.name, p.factor,p.voctog, p.species order by e.SCC, p.name
```

This query is quite a bit more complex, but is still supported by the EMF QA step processing system. In addition to summaries of the inventories, there are many summaries available for the Strategy Detailed Results output by control strategy runs and for some of the other CoST-related dataset types. Some of the summaries available for Strategy Detailed Results are as follows:

* "Summarize by Pollutant"
* "Summarize by County and Pollutant"
* "Summarize by SCC and Pollutant"
* "Summarize by Control Technology and Pollutant"
* "Summarize by Control Measure and Pollutant"
* "Summarize by Source Group and Pollutant"
* "Summarize by U.S. State and Pollutant"
* "Summarize by State, SCC, and Control Technology"
* "Summarize by Control Technology, FIPS, and SCC"
* "Summarize by Control Program, U.S. State and Pollutant"
* "Summarize by Plant and Pollutant"
* "Summarize all Control Measures"
* "Summarize by Sector and Pollutant with Descriptions"
* "Summarize by Sector, U.S. State, and Pollutant"
* "Summarize by U.S. State and SMOKE Pollutant Name"
* "Cost Curve"

A plot created based the output of a Summarize by Control Technology and Pollutant summary is shown in [Figure 4-32](#control_technologies_used_within_a_least_cost_analysis).

<a id=control_technologies_used_within_a_least_cost_analysis></a>

![Control Technologies used within a Least Cost Analysis][control_technologies_used_within_a_least_cost_analysis]

[control_technologies_used_within_a_least_cost_analysis]: images/Control_Technologies_used_within_a_Least_Cost_Analysis.png
**Figure 4-32. Control Technologies used within a Least Cost Analysis.**

When multiple datasets need to be considered in a summary (e.g., to compare two inventories), the EMF "QA Program" mechanism is used. The QA programs each have customized user interfaces that allow users to select the datasets to be used in the query. Some of the following QA programs may prove useful to CoST users:

* `Multi-inventory sum`: takes multiple inventories as input and reports the sum of emissions from all inventories
* `Multi-inventory column report`: takes multiple inventories as input and shows the emissions from each inventory in separate columns
* `Multi-inventory difference report`: takes two sets of inventories as input, sums each inventory, and then computes the difference between the two sums
* `Compare Control Strategies`: compares the data available in the Strategy Detailed Result datasets output from two control strategies

Summaries can be mapped with geographic information systems (GIS), mapping tools, and Google Earth. To facilitate this mapping, many of the summaries that have "with Descriptions" in their names include latitude and longitude information. For plant-level summaries, the latitude and longitude provided are the average of all the values given for the specific combination of FIPS and PLANT\_ID. For county- and state-level summaries, the latitude and longitude are the centroid of the county or state specified in the "fips" table of the EMF reference schema.

It is useful to note that after the summaries have been created, they can be exported to CSV files. By clicking `View Results`, the summary results can be viewed in a table called the Analysis Engine that does sorting, filtering, and plotting. From the `File` menu of the Analysis Engine window, a compressed .kmz file can be created and subsequently loaded into Google Earth. Note that each KMZ file is currently provided with a single latitude and longitude coordinate representing its centroid, even for geographic shapes like counties.

Recall that in addition to the datasets output for control strategies, many types of summaries of these datasets can be created in CoST. [Figure 4-30](#control_technologies_used_within_a_least_cost_analysis) shows a plot summarizing a Least Cost Strategy Detailed Result using the "Summarize by Control Technology and Pollutant" query. Some of the technologies used in this run were Low NOx burners (LNB), Low NOx burners with Flue Gas Recovery (LNB + FGD), Non-Selective Catalytic Reduction (NSCR), and Selective Non-catalytic Reduction (SNCR). Note that [Figure 4-30](#control_technologies_used_within_a_least_cost_analysis) was generated by plotting data output from CoST with spreadsheet software, and not by CoST itself. CoST does have some plotting capabilities, but they are not discussed in this document.

<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch3_control_measure_manager.md) - [Home](README.md) - [Next Chapter >>](ch5_control_strategy_exercises.md)<br>

<!-- END COMMENT -->
