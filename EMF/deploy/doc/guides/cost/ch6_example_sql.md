<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch5_control_strategy_exercises.md) - [Home](README.md) - [Next Chapter >>](ch7_references.md)

<!-- END COMMENT -->

# Example SQL Statements for Creating Row Filters {#Chapter6}

Table {@tbl:examples_of_row_filters_and_inventory_filters_table} provides some examples of row filters that can be applied to inventory filters to target specific sources for use during strategy analyses.

<a id=examples_of_row_filters_and_inventory_filters_table></a>

Filter Purpose|SQL Where Clause
-|-
Filter on a particular set of SCCs|`SCC LIKE '101%' OR SCC LIKE '102%'`
Filter on a particular set of pollutants|`POLL IN ('PM10-PRI','PM25-PRI')`<br/>`\\`{=latex}*or*<br/>`\\`{=latex}`POLL = 'PM10-PRI' OR POLL = 'PM25-PRI'`
Filter sources only in NC (State FIPS = 37), SC (45), and VA (51);<br/>`\\`{=latex}note that REGION\_CD column format is State + County FIPS code (e.g., 37001)|`SUBSTRING(REGION_CD,1,2) IN ('37', '45', '51')`
Filter sources only in CA (06) and include only NOx and VOC pollutants|`SUBSTRING(REGION_CD,1,2) = '06' AND POLL IN ('NOX', 'VOC')`<br/>`\\`{=latex}*or*<br/>`\\`{=latex}`REGION_CD LIKE '06%' AND (POLL = 'NOX' OR POLL = 'VOC')`

Table: Examples of Row Filters (Data Viewer window) and Inventory Filters (Inventories tab of the Edit Control Strategy window. {#tbl:examples_of_row_filters_and_inventory_filters_table}


<!-- BEGIN COMMENT -->

[<< Previous Chapter](ch5_control_strategy_exercises.md) - [Home](README.md) - [Next Chapter >>](ch7_references.md)<br>

<!-- END COMMENT -->
