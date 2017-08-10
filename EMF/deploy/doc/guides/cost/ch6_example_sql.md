<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch5_control_strategy_exercises.md) - [Home](README.md) - [Next Chapter >>](ch7_references.md)

<!-- END COMMENT -->

# Example SQL Statements for Creating Row Filters

[Table 6-1](#examples_of_row_filters_and_inventory_filters_table) provides some examples of row filters that can be applied to inventory filters to target specific sources for use during strategy analyses.

<a id=examples_of_row_filters_and_inventory_filters_table></a>
**Table 6-1. Examples of Row Filters (Data Viewer window) and Inventory Filters (Inventories tab of the Edit Control Strategy window**

Filter Purpose|SQL Where Clause
-|-
Filter on a particular set of SCCs|`scc like '101%' or scc like '102%'`
Filter on a particular set of pollutants|`poll in ('PM10', 'PM2\_5')`<br/>*or*<br/>`POLL = 'PM10' or POLL = 'PM2\_5'`
Filter sources only in NC (State FIPS = 37), SC (45), and VA (51);<br/>note that FIPS column format is State + County FIPS code (e.g., 37001)|`substring(FIPS,1,2) in ('37', '45', '51')`
Filter sources only in CA (06) and include only NO~x and VOC pollutants|`substring(fips,1,2) = '06' and poll in ('NOX', 'VOC')`<br/>*or*<br/>`fips like '06%' and (poll = 'NOX' or poll = 'VOC')`


<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch5_control_strategy_exercises.md) - [Home](README.md) - [Next Chapter >>](ch7_references.md)<br>

<!-- END COMMENT -->
