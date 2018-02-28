<!-- BEGIN COMMENT -->

[Home](README.md) - [Next Chapter >>](ch2_installing_cost.md)

<!-- END COMMENT -->

# Introduction to the Control Strategy Tool (CoST)

This document is a user's guide for the Control Strategy Tool (CoST) software. CoST was developed in cooperation between the [University of North Carolina Institute for the Environment](http://ie.unc.edu/research/environmental-modeling/) and the United States Environmental Protection Agency (EPA), Office of Air Quality Planning and Standards, Health and Environmental Impacts Division (HEID). CoST estimates the air pollution emissions reductions and costs associated with future-year control scenarios, and generates emissions inventories with the control scenarios applied [Misenheimer, 2007; Eyth, 2008](./ch7_references.md). CoST includes a database of information about emissions control measures, their costs, and the types of emissions sources to which they apply. The purpose of CoST is to support national- and regional-scale multi-pollutant analyses. CoST helps to develop control strategies that match control measures to emission sources using algorithms such as "Maximum Emissions Reduction" (for both single- and multiple-target pollutants), "Least Cost", and "Apply Measures in Series." CoST includes a graphical user interface (GUI) for configuring CoST simulations and viewing the results.

Results from a CoST control strategy run include the estimated cost and emissions (tons) reduction achieved for each control measure-source combination. CoST is an engineering cost estimation tool for creating controlled inventories and is not currently intended to model emissions trading strategies, nor is it an economic impact assessment tool. Control strategy results can be exported to comma-separated-values (CSV) files, Google Earth-compatible (.kmz) files, or Shapefiles. The CoST results can be viewed in the GUI as graphical tables that support sorting, filtering, and plotting. The Strategy Detailed Results from a CoST strategy run can be input to the [Sparse Matrix Operator Kernel Emissions (SMOKE)](http://www.smoke-model.org) modeling system, which is used by the EPA to prepare emissions inputs for air quality modeling.

CoST is a component of the [Emissions Modeling Framework (EMF)](https://github.com/USEPA-OAQPS/emf), which is currently being used by the EPA to solve many of the long-standing complexities of emissions modeling [Houyoux, 2008](./ch7_references.md). Emissions modeling is the process by which emissions inventories and other related information are converted to hourly, gridded, chemically speciated emissions estimates suitable for input into a regional air quality model such as the [Community Multiscale Air Quality (CMAQ) model](http://www.epa.gov/cmaq). The EMF supports the management and quality assurance of emissions inventories and emissions modeling-related data, and also the running of SMOKE to develop CMAQ inputs. Providing CoST as a tool integrated within the EMF facilitates a level of collaboration between control strategy development and emissions inventory modeling that was not previously possible. The concepts that have been added to the EMF for CoST are "**control measures**" and "**control strategies**." Control measures store information about available control technologies and practices that reduce emissions, the source categories to which they apply, the expected control efficiencies, and their estimated costs. A control strategy is a set of control measures applied to emissions inventory sources (in addition to any controls that are already in place) to accomplish an emissions reduction goal. These concepts are discussed in more detail later in this document.

CoST is designed for multi-pollutant analyses and data transparency. It provides a wide array of options for developing emissions control strategies through the Control Measures Database (CMDB). The CoST GUI provides a graphical interface for accessing the CMDB, designing control strategies, and viewing the results from control strategy runs. CoST has been applied to develop strategies for criteria and hazardous air pollutants (HAPs). CoST has been used in some very limited analyses for greenhouse gases (GHGs). The main limiting factors in performing GHG analyses are the availability of (1) GHG emissions inventories with enough detail to support the application of control measures to individual sources or source groups, and (2) GHG control measures, with the associated technology implementation and costs.

The CoST algorithms for developing control strategies include:

* "Annotate Inventory"
* "Maximum Emissions Reduction"
* "Multi-Pollutant Maximum Emissions Reduction"
* "Least Cost"
* "Least Cost Curve"
* "Apply Measures in Series"

The first five algorithms are typically used for point and area sources; the last one is usually used for mobile sources, for which most control techniques are independent of one another.

This document provides information on how to use CoST to view and edit control measures and how to develop control strategies. This includes how to specify the input parameters to control strategies, how to run the strategies, and how to analyze the outputs from the strategies. This document was prepared using version 2.15 of CoST. For additional information on other aspects of CoST, please see the following independent documents:

* [Control Strategy Tool (CoST) Development Document](https://www3.epa.gov/ttn/ecas/docs/CoST_DevelopmentDoc_02-23-2016.pdf): describes the algorithms implemented in the software

* [Control Strategy Tool (CoST) Control Measures Database Documentation](https://www3.epa.gov/ttn/ecas/models/CoST_CMDB_Document_2010-06-09.pdf): describes the contents of the Control Measures Database

* [Control Strategy Tool (CoST) Cost Equations Documentation](https://www3.epa.gov/ttn/ecas/docs/CoST_Equations_Document_2016_03_15.pdf): describes how CoST uses control measure engineering cost equations

These documents, and additional information about CoST, can be found at the EPA website on [Cost Analysis Models/Tools for Air Pollution Regulations](https://www.epa.gov/economic-and-cost-analysis-air-pollution-regulations/cost-analysis-modelstools-air-pollution).

<!-- BEGIN COMMENT -->

[Home](README.md) - [Next Chapter >>](ch2_installing_cost.md)<br>

<!-- END COMMENT -->
