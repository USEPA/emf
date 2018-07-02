Title: Overview of the EMF
Author: C. Seppanen, UNC
CSS: base.css

# Overview of the EMF [overview_chapter] #

## Introduction ##

The Emissions Modeling Framework (EMF) is a software system designed to solve many long-standing difficulties of emissions modeling identified at EPA. The overall process of emissions modeling involves gathering measured or estimated emissions data into emissions inventories; applying growth and controls information to create future year and controlled emissions inventories; and converting emissions inventories into hourly, gridded, chemically speciated emissions estimates suitable for input into air quality models such as the Community Multiscale Air Quality (CMAQ) model. 

This User's Guide focuses on the data management and analysis capabilities of the EMF. The EMF also contains a [Control Strategy Tool (CoST)][] for developing future year and controlled emissions inventories and is capable of driving SMOKE to develop CMAQ inputs. 

[Control Strategy Tool (CoST)]: http://www.cmascenter.org/help/model_docs/emf_cost/2.5.1/CoST_UsersGuide_2012-08-01_Final.pdf target="_blank"

Many types of data are involved in the emissions modeling process including: 

* emissions inventories
* growth and control factors
* spatial surrogates to assign emissions to grid cells
* chemical speciation information
* temporal profiles for calculating hourly emissions from annual, seasonal, or daily data
* cross-references for assigning allocation factors to inventory sources
* various reference data such as geographic region codes and names, and Source Classification Code (SCC) descriptions

Quality assurance (QA) is an important component of emissions modeling. Emissions inventories and other modeling data must be analyzed and reviewed for any discrepancies or outlying data points. Data files need to be organized and tracked so changes can be monitored and updates made when new data is available. Running emissions modeling software such as the Sparse Matrix Operator Kernel Emissions (SMOKE) Modeling System requires many configuration options and input files that need to be maintained so that modeling output can be reproduced in the future. At all stages, coordinating tasks and sharing data between different groups of people can be difficult and specialized knowledge may be required to use various tools.

In your emissions modeling work, you may have found yourself asking questions like:

* I need to download a particular piece of data. Where can I find it?
* When was this data last updated?
* Where can I get summary information about a given inventory?
* How does this year's inventory compare to last year's?
* What will the inventory look like 20 years from now?
* How will regulation *X* affect the inventory?
* What controls would be most effective for reducing emissions *X* percent?
* What types of analysis have been done on this data and who did each task?
* If a problem is found, how is that information shared with other people?

The EMF helps with these issues by using a client-server system where emissions modeling information is centrally stored and can be accessed by multiple users. The EMF integrates quality control processes into its data management to help with development of high quality emissions results. The EMF also organizes emissions modeling data and tracks emissions modeling efforts to aid in reproducibility of emissions modeling results. Additionally, the EMF strives to allow non-experts to use emissions modeling capabilities such as future year projections, spatial allocation, chemical speciation, and temporal allocation.



## EMF Components ##

A typical installation of the EMF system is illustrated in [Figure](#client_server_simple). In this case, a group of users shares a single EMF server with multiple local machines running the client application. The EMF server consists of a database, file storage, and the server application which handles requests from the clients and communicates with the database. The client application runs on each user's computer and provides a graphical interface for interacting with the emissions modeling data stored on the server (see [Chapter](#client_chapter)). Each user has his or her own username and password for accessing the EMF server. Some users will have administrative privileges which allow them to access additional system data such as managing users or dataset types.

![Typical EMF client-server setup][client_server_simple]

[client_server_simple]: images/client_server_simple.png

For a simpler setup, all of the EMF components can be run on a single machine: database, server application, and client application. With this "all-in-one" setup, the emissions data would generally not be shared between multiple users.

## Basic Workflow ##

[Figure](#data_workflow) illustrates the basic workflow of data in the EMF system.

![Data workflow in EMF system][data_workflow]

[data_workflow]: images/data_workflow.png

Emissions modeling data files are imported into the EMF system where they are represented as **datasets** (see [Chapter](#datasets_chapter)). The EMF supports many different types of data files including emissions inventories, allocation factors, cross-reference files, and reference data. Each dataset matches a **dataset type** which defines the format of the data to be loaded from the file ([Section](#dataset_types_section)). In addition to the raw data values, the EMF stores various metadata about each dataset including the time period covered, geographic region, the history of the data, and data usage in model runs or QA analysis.

Once your data is stored as a dataset, you can review and edit the dataset's properties ([Section](#dataset_properties_section)) or the data itself ([Section](#viewing_data_section)) using the EMF client. You can also run **QA steps** on a dataset or set of datasets to extract summary information, compare datasets, or convert the data to a different format (see [Chapter](#qa_chapter)).

You can export your dataset to a file and download it to your local computer ([Section](#exporting_datasets_section)). You can also export reports that you create with QA steps for further analysis in a spreadsheet program or to create charts ([Section](#export_qa_results_section)).
