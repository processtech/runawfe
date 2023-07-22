package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class ExpandChatColumnsPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLModifyColumn("CHAT_MESSAGE", new TimestampColumnDef("CREATE_DATE").notNull()));
    }
}
