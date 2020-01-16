CREATE OR REPLACE VIEW emf.vw_backup_restore_tables AS
 SELECT t.id || '-' ||  t.dataset_type as index, t.id,
    ((((('/backups/EMF/emissions_schema/'::text || t.dataset_type::text) || '/'::text) || ceiling(t.id::numeric / 1000.0)) || '/'::text) || t.id) || '.bck'::text AS backup_file,
    t.dataset_type,
    string_agg(t.table_name, ','::text ORDER BY t.table_name) AS table_names,
    t.last_modified
   FROM ( SELECT v.id,
            'emissions.'::text || v.table_name AS table_name,
            v.dataset_type,
            v.last_modified
           FROM ((
                         SELECT min(d.id) AS id,
                            "substring"(lower(i.table_name::text), 1, 63) AS table_name,
                            'ds'::character varying AS dataset_type,
                            max(GREATEST(d.created_date_time, d.modified_date_time)) AS last_modified
                           FROM emf.datasets d
                             JOIN emf.internal_sources i ON i.dataset_id = d.id
                             JOIN pg_class ON lower(pg_class.relname::text) = lower("substring"(i.table_name::text, 1, 63))
                          WHERE d.status::text <> 'Deleted'::text AND NOT (lower("substring"(i.table_name::text, 1, 63)) IN ( SELECT lower("substring"(tc.output_table::text, 1, 63)) AS lower
                                   FROM emf.table_consolidations tc))
                          GROUP BY ("substring"(lower(i.table_name::text), 1, 63))
                        UNION ALL
                         SELECT min(ad.id) AS id,
                            "substring"(lower(i.table_name::text), 1, 63) AS table_name,
                            'ds'::character varying AS dataset_type,
                            max(GREATEST(ad.created_date_time, ad.modified_date_time)) AS last_modified
                           FROM emf.datasets d
                             JOIN emf.internal_sources i ON i.dataset_id = d.id
                             JOIN pg_class ON lower(pg_class.relname::text) = lower("substring"(i.table_name::text, 1, 63))
                             JOIN emf.datasets ad ON ad.dataset_type = d.dataset_type
                             JOIN emf.internal_sources adi ON adi.dataset_id = ad.id AND lower("substring"(adi.table_name::text, 1, 63)) = lower("substring"(i.table_name::text, 1, 63))
                          WHERE d.status::text <> 'Deleted'::text AND ad.status::text <> 'Deleted'::text AND (lower("substring"(i.table_name::text, 1, 63)) IN ( SELECT lower("substring"(tc.output_table::text, 1, 63)) AS lower
                                   FROM emf.table_consolidations tc))
                          GROUP BY ("substring"(lower(i.table_name::text), 1, 63))
                ) UNION
                 SELECT q.id,
                    lower("substring"(q.output_table::text, 1, 63)) AS table_name,
                    'qa'::character varying AS dataset_type,
                    q.table_creation_date AS last_modified
                   FROM emf.qa_step_results q
                     JOIN pg_class ON lower(pg_class.relname::text) = lower("substring"(q.output_table::text, 1, 63))) v) t
  GROUP BY t.id, t.dataset_type, t.last_modified
  ORDER BY t.last_modified DESC;

ALTER TABLE emf.vw_backup_restore_tables
    OWNER TO postgres;



