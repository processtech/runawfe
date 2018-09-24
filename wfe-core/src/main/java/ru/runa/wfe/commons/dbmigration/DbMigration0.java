//
// ATTENTION!!! This file is auto-generated by wfe-codegen-dbaware subproject. See README.txt there.
//
// UPD. Initial version was manually edited by dimgel:
//      - fixed long (PostgreSQL-generated) and random (Hibernate-generated) UK/FK names;
//      - replaced PK with UK on table ACTOR_PASSWORD (see tnms #5151-52).
//      Please keep these updates and don't regenerate from non-empty database; see wfe-codegen-dbaware/README.txt.
//
package ru.runa.wfe.commons.dbmigration;

import java.sql.Connection;
import java.sql.Statement;

public class DbMigration0 extends DbMigration {

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateSequence("seq_admin_script"),
                getDDLCreateSequence("seq_batch_presentation"),
                getDDLCreateSequence("seq_bot"),
                getDDLCreateSequence("seq_bot_station"),
                getDDLCreateSequence("seq_bot_task"),
                getDDLCreateSequence("seq_bpm_agglog_assignments"),
                getDDLCreateSequence("seq_bpm_agglog_process"),
                getDDLCreateSequence("seq_bpm_agglog_tasks"),
                getDDLCreateSequence("seq_bpm_job"),
                getDDLCreateSequence("seq_bpm_log"),
                getDDLCreateSequence("seq_bpm_process"),
                getDDLCreateSequence("seq_bpm_process_definition"),
                getDDLCreateSequence("seq_bpm_setting"),
                getDDLCreateSequence("seq_bpm_subprocess"),
                getDDLCreateSequence("seq_bpm_swimlane"),
                getDDLCreateSequence("seq_bpm_task"),
                getDDLCreateSequence("seq_bpm_token"),
                getDDLCreateSequence("seq_bpm_variable"),
                getDDLCreateSequence("seq_executor"),
                getDDLCreateSequence("seq_executor_group_member"),
                getDDLCreateSequence("seq_executor_relation"),
                getDDLCreateSequence("seq_localization"),
                getDDLCreateSequence("seq_permission_mapping"),
                getDDLCreateSequence("seq_priveleged_mapping"),
                getDDLCreateSequence("seq_profile"),
                getDDLCreateSequence("seq_relation_group"),
                getDDLCreateSequence("seq_report"),
                getDDLCreateSequence("seq_report_parameter"),
                getDDLCreateSequence("seq_substitution"),
                getDDLCreateSequence("seq_substitution_criteria"),
                getDDLCreateSequence("seq_system_log"),
                getDDLCreateSequence("seq_wfe_constants"),

                getDDLCreateTable("actor_password", list(
                        new BigintColumnDef("actor_id", false).setPrimaryKey(),
                        new BlobColumnDef("password", false)
                )),
                getDDLCreateTable("admin_script", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("name", 1024, true),
                        new BlobColumnDef("content", true)
                )),
                getDDLCreateTable("batch_presentation", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("category", 1024, false),
                        new BlobColumnDef("fields", true),
                        new IntColumnDef("range_size", true),
                        new TimestampColumnDef("create_date", false),
                        new BooleanColumnDef("shared", false),
                        new BigintColumnDef("version", true),
                        new BooleanColumnDef("is_active", true),
                        new VarcharColumnDef("name", 1024, false),
                        new VarcharColumnDef("class_type", 1024, true),
                        new BigintColumnDef("profile_id", true)
                )),
                getDDLCreateTable("bot", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new BooleanColumnDef("is_transactional", true),
                        new VarcharColumnDef("password", 1024, true),
                        new TimestampColumnDef("create_date", false),
                        new BooleanColumnDef("is_sequential", true),
                        new BigintColumnDef("transactional_timeout", true),
                        new TimestampColumnDef("bound_due_date", true),
                        new BigintColumnDef("bound_process_id", true),
                        new VarcharColumnDef("bound_subprocess_id", 255, true),
                        new VarcharColumnDef("username", 1024, true),
                        new BigintColumnDef("version", true),
                        new BigintColumnDef("bot_station_id", false)
                )),
                getDDLCreateTable("bot_station", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new BigintColumnDef("version", true),
                        new VarcharColumnDef("address", 1024, true),
                        new VarcharColumnDef("name", 1024, false)
                )),
                getDDLCreateTable("bot_task", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new BlobColumnDef("embedded_file", true),
                        new VarcharColumnDef("embedded_file_name", 1024, true),
                        new BooleanColumnDef("is_sequential", true),
                        new VarcharColumnDef("task_handler", 1024, true),
                        new BigintColumnDef("version", true),
                        new BlobColumnDef("configuration", true),
                        new VarcharColumnDef("name", 1024, true),
                        new BigintColumnDef("bot_id", false)
                )),
                getDDLCreateTable("bpm_agglog_assignments", list(
                        new CharColumnDef("discriminator", 1, false),
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("new_executor_name", 1024, true),
                        new VarcharColumnDef("old_executor_name", 1024, true),
                        new TimestampColumnDef("assignment_date", false),
                        new BigintColumnDef("assignment_object_id", true),
                        new IntColumnDef("idx", true)
                )),
                getDDLCreateTable("bpm_agglog_process", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new BigintColumnDef("process_id", false),
                        new BigintColumnDef("parent_process_id", true),
                        new VarcharColumnDef("cancel_actor_name", 1024, true),
                        new IntColumnDef("end_reason", false),
                        new VarcharColumnDef("start_actor_name", 1024, true),
                        new TimestampColumnDef("create_date", false),
                        new TimestampColumnDef("end_date", true)
                )),
                getDDLCreateTable("bpm_agglog_tasks", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("initial_actor_name", 1024, true),
                        new VarcharColumnDef("complete_actor_name", 1024, true),
                        new IntColumnDef("end_reason", false),
                        new VarcharColumnDef("swimlane_name", 1024, true),
                        new BigintColumnDef("token_id", false),
                        new VarcharColumnDef("task_name", 1024, false),
                        new BigintColumnDef("task_id", false),
                        new TimestampColumnDef("create_date", false),
                        new TimestampColumnDef("end_date", true),
                        new TimestampColumnDef("deadline_date", true),
                        new VarcharColumnDef("node_id", 1024, false),
                        new IntColumnDef("task_index", true),
                        new BigintColumnDef("process_id", false)
                )),
                getDDLCreateTable("bpm_job", list(
                        new CharColumnDef("discriminator", 1, false),
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("due_date", true),
                        new TimestampColumnDef("create_date", false),
                        new VarcharColumnDef("due_date_expression", 255, true),
                        new BigintColumnDef("version", true),
                        new VarcharColumnDef("name", 1024, true),
                        new VarcharColumnDef("repeat_duration", 1024, true),
                        new VarcharColumnDef("transition_name", 1024, true),
                        new BigintColumnDef("token_id", true),
                        new BigintColumnDef("process_id", false)
                )),
                getDDLCreateTable("bpm_log", list(
                        new CharColumnDef("discriminator", 1, false),
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("severity", 1024, false),
                        new BigintColumnDef("token_id", true),
                        new TimestampColumnDef("create_date", false),
                        new VarcharColumnDef("node_id", 1024, true),
                        new BigintColumnDef("process_id", false),
                        new BlobColumnDef("bytes", true),
                        new VarcharColumnDef("content", 4000, true)
                )),
                getDDLCreateTable("bpm_process", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("execution_status", 255, false),
                        new BigintColumnDef("parent_id", true),
                        new TimestampColumnDef("end_date", true),
                        new TimestampColumnDef("start_date", true),
                        new VarcharColumnDef("tree_path", 1024, true),
                        new BigintColumnDef("version", true),
                        new BigintColumnDef("definition_id", false),
                        new BigintColumnDef("root_token_id", false)
                )),
                getDDLCreateTable("bpm_process_definition", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("subprocess_binding_date", true),
                        new TimestampColumnDef("update_date", true),
                        new VarcharColumnDef("category", 1024, false),
                        new TimestampColumnDef("create_date", false),
                        new BigintColumnDef("version", false),
                        new VarcharColumnDef("description", 1024, true),
                        new VarcharColumnDef("name", 1024, false),
                        new VarcharColumnDef("language", 1024, false),
                        new BlobColumnDef("bytes", true),
                        new BigintColumnDef("update_user_id", true),
                        new BigintColumnDef("create_user_id", true)
                )),
                getDDLCreateTable("bpm_setting", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("name", 1024, false),
                        new VarcharColumnDef("value", 1024, true),
                        new VarcharColumnDef("file_name", 1024, false)
                )),
                getDDLCreateTable("bpm_subprocess", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new VarcharColumnDef("parent_node_id", 1024, true),
                        new IntColumnDef("subprocess_index", true),
                        new BigintColumnDef("parent_token_id", true),
                        new BigintColumnDef("parent_process_id", false),
                        new BigintColumnDef("process_id", false)
                )),
                getDDLCreateTable("bpm_swimlane", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new BigintColumnDef("version", true),
                        new VarcharColumnDef("name", 1024, true),
                        new BigintColumnDef("process_id", true),
                        new BigintColumnDef("executor_id", true)
                )),
                getDDLCreateTable("bpm_task", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new TimestampColumnDef("deadline_date", true),
                        new TimestampColumnDef("assign_date", true),
                        new VarcharColumnDef("node_id", 1024, true),
                        new VarcharColumnDef("deadline_date_expression", 255, true),
                        new IntColumnDef("task_index", true),
                        new BigintColumnDef("version", true),
                        new VarcharColumnDef("description", 1024, true),
                        new VarcharColumnDef("name", 1024, true),
                        new BigintColumnDef("process_id", true),
                        new BigintColumnDef("token_id", true),
                        new BigintColumnDef("executor_id", true),
                        new BigintColumnDef("swimlane_id", true)
                )),
                getDDLCreateTable("bpm_task_opened", list(
                        new BigintColumnDef("task_id", false),
                        new BigintColumnDef("executor_id", true)
                )),
                getDDLCreateTable("bpm_token", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("error_message", 1024, true),
                        new VarcharColumnDef("transition_id", 1024, true),
                        new VarcharColumnDef("execution_status", 255, false),
                        new VarcharColumnDef("message_selector", 1024, true),
                        new TimestampColumnDef("error_date", true),
                        new TimestampColumnDef("end_date", true),
                        new TimestampColumnDef("start_date", true),
                        new VarcharColumnDef("node_id", 1024, true),
                        new BooleanColumnDef("reactivate_parent", true),
                        new VarcharColumnDef("node_type", 1024, true),
                        new BigintColumnDef("version", true),
                        new VarcharColumnDef("name", 1024, true),
                        new BigintColumnDef("process_id", true),
                        new BigintColumnDef("parent_id", true)
                )),
                getDDLCreateTable("bpm_variable", list(
                        new CharColumnDef("discriminator", 1, false),
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new VarcharColumnDef("stringvalue", 1024, true),
                        new CharColumnDef("converter", 1, true),
                        new BigintColumnDef("version", true),
                        new VarcharColumnDef("name", 1024, true),
                        new TimestampColumnDef("datevalue", true),
                        new BlobColumnDef("bytes", true),
                        new BigintColumnDef("longvalue", true),
                        new DoubleColumnDef("doublevalue", true),
                        new BigintColumnDef("process_id", false)
                )),
                getDDLCreateTable("executor", list(
                        new VarcharColumnDef("discriminator", 1, false),
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new VarcharColumnDef("full_name", 1024, true),
                        new BigintColumnDef("version", true),
                        new VarcharColumnDef("description", 1024, true),
                        new VarcharColumnDef("name", 1024, false),
                        new VarcharColumnDef("e_mail", 255, true),
                        new BigintColumnDef("process_id", true),
                        new VarcharColumnDef("phone", 1024, true),
                        new VarcharColumnDef("department", 1024, true),
                        new VarcharColumnDef("title", 1024, true),
                        new BigintColumnDef("code", true),
                        new BooleanColumnDef("is_active", true),
                        new VarcharColumnDef("node_id", 1024, true),
                        new IntColumnDef("escalation_level", true),
                        new BigintColumnDef("escalation_executor_id", true)
                )),
                getDDLCreateTable("executor_group_member", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new BigintColumnDef("version", true),
                        new BigintColumnDef("group_id", false),
                        new BigintColumnDef("executor_id", false)
                )),
                getDDLCreateTable("executor_relation", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new VarcharColumnDef("description", 1024, true),
                        new VarcharColumnDef("name", 1024, true)
                )),
                getDDLCreateTable("executor_relation_pair", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new BigintColumnDef("executor_to", false),
                        new BigintColumnDef("executor_from", false),
                        new BigintColumnDef("relation_id", false)
                )),
                getDDLCreateTable("localization", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new VarcharColumnDef("name", 1024, true),
                        new VarcharColumnDef("value", 1024, true)
                )),
                getDDLCreateTable("permission_mapping", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new BigintColumnDef("object_id", false),
                        new VarcharColumnDef("object_type", 255, false),
                        new VarcharColumnDef("permission", 255, false),
                        new BigintColumnDef("executor_id", false)
                )),
                getDDLCreateTable("priveleged_mapping", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("type", 1024, false),
                        new BigintColumnDef("executor_id", false)
                )),
                getDDLCreateTable("profile", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new BigintColumnDef("version", true),
                        new BigintColumnDef("actor_id", false)
                )),
                getDDLCreateTable("report", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("category", 1024, true),
                        new BlobColumnDef("compiled_report", false),
                        new VarcharColumnDef("config_type", 1024, false),
                        new BigintColumnDef("version", false),
                        new VarcharColumnDef("description", 2048, true),
                        new VarcharColumnDef("name", 1024, false)
                )),
                getDDLCreateTable("report_parameter", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("inner_name", 1024, false),
                        new BooleanColumnDef("required", false),
                        new VarcharColumnDef("name", 1024, false),
                        new VarcharColumnDef("type", 1024, false),
                        new BigintColumnDef("report_id", false)
                )),
                getDDLCreateTable("substitution", list(
                        new VarcharColumnDef("discriminator", 1, false),
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new BigintColumnDef("actor_id", false),
                        new VarcharColumnDef("org_function", 1024, false),
                        new BooleanColumnDef("is_external", false),
                        new IntColumnDef("position_index", false),
                        new BigintColumnDef("version", true),
                        new BooleanColumnDef("enabled_flag", false),
                        new BigintColumnDef("criteria_id", true)
                )),
                getDDLCreateTable("substitution_criteria", list(
                        new VarcharColumnDef("discriminator", 31, false),
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new VarcharColumnDef("conf", 1024, true),
                        new VarcharColumnDef("name", 1024, false)
                )),
                getDDLCreateTable("system_log", list(
                        new VarcharColumnDef("discriminator", 31, false),
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new TimestampColumnDef("create_date", false),
                        new BigintColumnDef("actor_id", false),
                        new BigintColumnDef("process_definition_version", true),
                        new VarcharColumnDef("process_definition_name", 1024, true),
                        new BigintColumnDef("process_id", true)
                )),
                getDDLCreateTable("wfe_constants", list(
                        new BigintColumnDef("id", false).setPrimaryKey(),
                        new VarcharColumnDef("name", 1024, true),
                        new VarcharColumnDef("value", 1024, true)
                )),

                getDDLCreateIndex("batch_presentation", "ix_batch_presentation_profile", "profile_id"),
                getDDLCreateIndex("bot", "ix_bot_station", "bot_station_id"),
                getDDLCreateIndex("bot_task", "ix_bot_task_bot", "bot_id"),
                getDDLCreateIndex("bpm_agglog_assignments", "ix_agglog_assign_date", "assignment_date"),
                getDDLCreateIndex("bpm_agglog_assignments", "ix_agglog_assign_executor", "new_executor_name"),
                getDDLCreateIndex("bpm_agglog_process", "ix_agglog_process_create_date", "create_date"),
                getDDLCreateIndex("bpm_agglog_process", "ix_agglog_process_end_date", "end_date"),
                getDDLCreateIndex("bpm_agglog_process", "ix_agglog_process_instance", "process_id"),
                getDDLCreateIndex("bpm_agglog_tasks", "ix_agglog_tasks_create_date", "create_date"),
                getDDLCreateIndex("bpm_agglog_tasks", "ix_agglog_tasks_end_date", "end_date"),
                getDDLCreateIndex("bpm_agglog_tasks", "ix_agglog_tasks_process", "process_id"),
                getDDLCreateIndex("bpm_job", "ix_job_process", "process_id"),
                getDDLCreateIndex("bpm_log", "ix_log_process", "process_id"),
                getDDLCreateIndex("bpm_process", "ix_process_definition", "definition_id"),
                getDDLCreateIndex("bpm_process", "ix_process_root_token", "root_token_id"),
                getDDLCreateIndex("bpm_subprocess", "ix_subprocess_parent_process", "parent_process_id"),
                getDDLCreateIndex("bpm_subprocess", "ix_subprocess_process", "process_id"),
                getDDLCreateIndex("bpm_swimlane", "ix_swimlane_process", "process_id"),
                getDDLCreateIndex("bpm_task", "ix_task_executor", "executor_id"),
                getDDLCreateIndex("bpm_task", "ix_task_process", "process_id"),
                getDDLCreateIndex("bpm_token", "ix_message_selector", "message_selector"),
                getDDLCreateIndex("bpm_token", "ix_token_parent", "parent_id"),
                getDDLCreateIndex("bpm_token", "ix_token_process", "process_id"),
                getDDLCreateIndex("bpm_variable", "ix_variable_name", "name"),
                getDDLCreateIndex("bpm_variable", "ix_variable_process", "process_id"),
                getDDLCreateIndex("executor", "ix_executor_code", "code"),
                getDDLCreateIndex("executor_group_member", "ix_member_executor", "executor_id"),
                getDDLCreateIndex("executor_group_member", "ix_member_group", "group_id"),
                getDDLCreateIndex("executor_relation_pair", "ix_erp_executor_from", "executor_from"),
                getDDLCreateIndex("executor_relation_pair", "ix_erp_executor_to", "executor_to"),
                getDDLCreateIndex("executor_relation_pair", "ix_erp_relation", "relation_id"),
                getDDLCreateIndex("permission_mapping", "ix_permission_mapping_data", "executor_id", "object_type", "permission", "object_id"),
                getDDLCreateIndex("priveleged_mapping", "ix_privelege_type", "type"),
                getDDLCreateIndex("substitution", "ix_substitution_actor", "actor_id"),
                getDDLCreateIndex("substitution", "ix_substitution_criteria", "criteria_id"),

                getDDLCreateUniqueKey("bot_station", "uk_bot_station_name", "name"),
                getDDLCreateUniqueKey("bpm_variable", "uk_variable_2", "process_id", "name"),
                getDDLCreateUniqueKey("executor", "uk_executor_name", "name"),
                getDDLCreateUniqueKey("executor_group_member", "uk_executor_group_member_2", "executor_id", "group_id"),
                getDDLCreateUniqueKey("executor_relation", "uk_executor_relation_name", "name"),
                getDDLCreateUniqueKey("localization", "uk_localization_name", "name"),
                getDDLCreateUniqueKey("permission_mapping", "uk_permission_mapping_4", "object_id", "object_type", "permission", "executor_id"),
                getDDLCreateUniqueKey("profile", "uk_profile_actor_id", "actor_id"),
                getDDLCreateUniqueKey("report", "uk_report_name", "name"),
                getDDLCreateUniqueKey("substitution", "uk_substitution_2", "position_index", "actor_id"),
                getDDLCreateUniqueKey("wfe_constants", "uk_wfe_constants_name", "name"),

                getDDLCreateForeignKey("batch_presentation", "fk_batch_presentation_profile", "profile_id", "profile", "id"),
                getDDLCreateForeignKey("bot", "fk_bot_station", "bot_station_id", "bot_station", "id"),
                getDDLCreateForeignKey("bot_task", "fk_bot_task_bot", "bot_id", "bot", "id"),
                getDDLCreateForeignKey("bpm_agglog_assignments", "fk_agglog_assignments_1", "assignment_object_id", "bpm_agglog_tasks", "id"),
                getDDLCreateForeignKey("bpm_job", "fk_job_process", "process_id", "bpm_process", "id"),
                getDDLCreateForeignKey("bpm_job", "fk_job_token", "token_id", "bpm_token", "id"),
                getDDLCreateForeignKey("bpm_process", "fk_process_definition", "definition_id", "bpm_process_definition", "id"),
                getDDLCreateForeignKey("bpm_process", "fk_process_root_token", "root_token_id", "bpm_token", "id"),
                getDDLCreateForeignKey("bpm_process_definition", "fk_definition_create_user", "create_user_id", "executor", "id"),
                getDDLCreateForeignKey("bpm_process_definition", "fk_definition_update_user", "update_user_id", "executor", "id"),
                getDDLCreateForeignKey("bpm_subprocess", "fk_subprocess_parent_process", "parent_process_id", "bpm_process", "id"),
                getDDLCreateForeignKey("bpm_subprocess", "fk_subprocess_process", "process_id", "bpm_process", "id"),
                getDDLCreateForeignKey("bpm_subprocess", "fk_subprocess_token", "parent_token_id", "bpm_token", "id"),
                getDDLCreateForeignKey("bpm_swimlane", "fk_swimlane_executor", "executor_id", "executor", "id"),
                getDDLCreateForeignKey("bpm_swimlane", "fk_swimlane_process", "process_id", "bpm_process", "id"),
                getDDLCreateForeignKey("bpm_task", "fk_task_executor", "executor_id", "executor", "id"),
                getDDLCreateForeignKey("bpm_task", "fk_task_process", "process_id", "bpm_process", "id"),
                getDDLCreateForeignKey("bpm_task", "fk_task_swimlane", "swimlane_id", "bpm_swimlane", "id"),
                getDDLCreateForeignKey("bpm_task", "fk_task_token", "token_id", "bpm_token", "id"),
                getDDLCreateForeignKey("bpm_task_opened", "fk_task_opened_task", "task_id", "bpm_task", "id"),
                getDDLCreateForeignKey("bpm_token", "fk_token_parent", "parent_id", "bpm_token", "id"),
                getDDLCreateForeignKey("bpm_token", "fk_token_process", "process_id", "bpm_process", "id"),
                getDDLCreateForeignKey("bpm_variable", "fk_variable_process", "process_id", "bpm_process", "id"),
                getDDLCreateForeignKey("executor", "fk_group_escalation_executor", "escalation_executor_id", "executor", "id"),
                getDDLCreateForeignKey("executor_group_member", "fk_member_executor", "executor_id", "executor", "id"),
                getDDLCreateForeignKey("executor_group_member", "fk_member_group", "group_id", "executor", "id"),
                getDDLCreateForeignKey("executor_relation_pair", "fk_erp_executor_from", "executor_from", "executor", "id"),
                getDDLCreateForeignKey("executor_relation_pair", "fk_erp_executor_to", "executor_to", "executor", "id"),
                getDDLCreateForeignKey("executor_relation_pair", "fk_erp_relation", "relation_id", "executor_relation", "id"),
                getDDLCreateForeignKey("permission_mapping", "fk_permission_executor", "executor_id", "executor", "id"),
                getDDLCreateForeignKey("priveleged_mapping", "fk_pm_executor", "executor_id", "executor", "id"),
                getDDLCreateForeignKey("profile", "fk_profile_actor", "actor_id", "executor", "id"),
                getDDLCreateForeignKey("report_parameter", "fk_report_parameter_report", "report_id", "report", "id"),
                getDDLCreateForeignKey("substitution", "fk_substitution_criteria", "criteria_id", "substitution_criteria", "id")
        );
    }

    @Override
    public void executeDML(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("insert into wfe_constants (" + insertPkColumn() + "name, value) values (" +
                    insertPkNextVal("wfe_constants") + "'ru.runa.database_version', 59)");
        }
    }
}
