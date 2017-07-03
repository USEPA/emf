Title: Introduction
Author: C. Seppanen, UNC
CSS: base.css

# Introduction [introduction_chapter] #

This document provides a training manual and user's guide for the Control Strategy Tool (CoST) software developed by EPA's Health and Environmental Impacts Division (HEID). CoST allows users to estimate the emission reductions and costs associated with future-year control scenarios, and then to generate emission inventories with the control scenarios applied [Misenheimer, 2007; Eyth, 2008]. CoST tracks information about control measures, their costs, and the types of emissions sources to which they apply. The purpose of CoST is to support national- and regional-scale multipollutant analyses. CoST helps to develop control strategies that match control measures to emission sources using algorithms such as "Maximum Emissions Reduction" (for both single- and multiple-target pollutants), "Least Cost", and "Apply Measures in Series".

The result of a control strategy run contains information that specifies the estimated cost and emissions reduction achieved for each control measure-source combination. CoST is an engineering cost estimation tool for creating controlled inventories and is not currently intended to model emissions trading strategies, nor is it an economic impact tool. Control strategy results can be exported to comma-separated-values (CSV) files, Google Earth-compatible (.kmz) files, or Shapefiles. The results can also be viewed in a graphical table that supports sorting, filtering, and plotting. The Strategy Detailed Results from a strategy can also be merged with the original inventory to create controlled emissions inventories datasets that can be exported to files that can be input to the Sparse Matrix Operator Kernel Emissions (SMOKE) modeling system, which is used by EPA to prepare emissions inputs for air quality modeling.

CoST is a component of the Emissions Modeling Framework (EMF), which is currently being used by EPA to solve many of the long-standing complexities of emissions modeling [Houyoux, 2008]. Emissions modeling is the process by which emissions inventories and other related information is converted to hourly, gridded, chemically speciated emissions estimates suitable for input to an air quality model such as the Community Multiscale Air Quality (CMAQ) model. The EMF supports the management and quality assurance of emissions inventories and emissions modeling-related data, and also the running of SMOKE to develop CMAQ inputs. Providing CoST as a tool integrated within the EMF facilitates a level of collaboration between control strategy development and emissions inventory modeling that was not previously possible. The concepts that have been added to the EMF for CoST are "**control measures**" and "**control strategies**". Control measures store information about available control technologies and practices that reduce emissions, the source categories to which they apply, the expected control efficiencies, and their estimated costs. A control strategy is a set of control measures applied to emissions inventory sources (in addition to any controls that are already in place) to accomplish an emissions reduction goal. These concepts are discussed in more detail later in this document.

CoST supports multipollutant analyses and data transparency, and provides a wide array of options for developing control strategies. CoST uses a Control Measures Database to develop control strategies, and provides a user interface to that database. CoST has been developed to replace the older AirControlNET software. It has been applied to develop strategies for criteria and hazardous air pollutants (HAPs). CoST has been used in some very limited analyses for greenhouse gases (GHGs). The main limiting factors in performing GHG analyses is the availability of (1) GHG emissions inventories at an appropriate level of detail, and (2) control measures for GHGs.

CoST is an extensible software system that provides several types of algorithms for developing control strategies:

* "Maximum Emissions Reduction"
* "Multi-Pollutant Maximum Emissions Reduction"
* "Least Cost"
* "Least Cost Curve"
* "Apply Measures in Series"

The first four algorithms are typically used for point and area sources; the last one is usually used for mobile sources, for which most control techniques are independent of one another.

This document provides information on how to use CoST to view and edit control measures and how to develop control strategies. This includes how to specify the input parameters to control strategies, how to run the strategies, and how to analyze the outputs from the strategies. For additional information on other aspects of CoST, please see the following independent documents:

* Control Strategy Tool (CoST) Development Document: describes the algorithms implemented in the software

* Control Strategy Tool (CoST) Control Measures Database Documentation: describes the contents of the Control Measures Database

* Control Strategy Tool (CoST) Cost Equations Documentation: describes how CoST uses control measure engineering cost equations

These documents, and additional information about CoST, can be found at: [https://www.epa.gov/economic-and-cost-analysis-air-pollution-regulations/cost-analysis-modelstools-air-pollution](https://www.epa.gov/economic-and-cost-analysis-air-pollution-regulations/cost-analysis-modelstools-air-pollution). A glossary of terms is included as an appendix to this document.
