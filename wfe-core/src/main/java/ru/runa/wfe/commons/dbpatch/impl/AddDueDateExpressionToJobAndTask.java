/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddDueDateExpressionToJobAndTask extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateColumn("BPM_JOB", new ColumnDef("DUE_DATE_EXPRESSION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true)));
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("DEADLINE_DATE_EXPRESSION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true)));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }
}
