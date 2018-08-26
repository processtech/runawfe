package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.Collections;
import java.util.List;
import org.hibernate.Session;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.dbpatch.DbPatch;

/**
 * See TNMS #5006.
 */
public class AddSubprocessRootIdColumn extends DbPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        return Collections.singletonList(
                // First, add new column as nullable.
                getDDLCreateColumn("bpm_subprocess", new BigintColumnDef("root_process_id", true))
        );
    }

    @Override
    public void executeDML(Session session) {
        DbType dbType = ApplicationContextFactory.getDBType();
        String sql;

        // Set ROOT_PROCESS_ID for all first-level subprocesses.
        switch (dbType) {
            default:
                // They say this is standard syntax: https://stackoverflow.com/a/31410053/4247442; however had to remove alias
                // from outer table because looks like MS SQL may not support it: https://stackoverflow.com/a/14618713/4247442.
                sql = "update bpm_subprocess " +
                        "set root_process_id = parent_process_id " +
                        "where NOT exists (" +
                        // Subquery returns non-empty rowset if bpm_subprocess.parent_process_id is someone else's child (i.e. non-root).
                        // I select same s2.process_id field which is used in where clause, so only index ix_subprocess_process is accessed
                        // (although good SQL server should perform this optimization anyway).
                        "    select s2.process_id " +
                        "    from bpm_subprocess s2 " +
                        "    where s2.process_id = bpm_subprocess.parent_process_id " +
                        ")";
                break;
        }
        session.createSQLQuery(sql).executeUpdate();

        // Set ROOT_PROCESS_ID in loop (each step -- next deepness level in all trees), until nothing more to do.
        do {
            switch (dbType) {
                default:
                    // Subquery selects parent subprocess which has ROOT_PROCESS_ID already set.
                    // It's used in "WHERE EXISTS" too, since otherwise it will be evaulated to NULL in "SET".
                    String subquery = "select s2.root_process_id " +
                            "from bpm_subprocess s2 " +
                            "where (s2.process_id = bpm_subprocess.parent_process_id) and (s2.root_process_id is not null)";
                    sql = "update bpm_subprocess " +
                            "set root_process_id = (" + subquery + ") " +
                            "where root_process_id is null and exists (" + subquery + ")";
                    break;
            }
        } while (session.createSQLQuery(sql).executeUpdate() != 0);
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        return Collections.singletonList(
                // Last, alter column to be not-null.
                getDDLModifyColumnNullability("bpm_subprocess", "root_process_id", dialect.getTypeName(Types.BIGINT), false)
        );
    }
}
