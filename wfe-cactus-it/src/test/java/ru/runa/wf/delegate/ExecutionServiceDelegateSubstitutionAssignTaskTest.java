package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;

/**
 * This test class is to check substitution logic concerning "Assign task"
 * function.<br />
 * It does not take into account concurrent work of the several members of the
 * same group.
 * 
 * @see ExecutionServiceDelegateAssignTaskTest
 */
public class ExecutionServiceDelegateSubstitutionAssignTaskTest extends ServletTestCase {
    private static final String PROCESS_NAME = WfServiceTestHelper.SWIMLANE_SAME_GROUP_SEQ_PROCESS_NAME;

    private final static String nameActor1 = "actor1";
    private final static String nameActor2 = "actor2";
    private final static String nameGroup = "testGroup";
    private final static String nameSubstitute = "substitute";

    private final static String pwdActor1 = "123";
    private final static String pwdActor2 = "123";
    private final static String pwdSubstitute = "123";

    private WfServiceTestHelper h;
    private BatchPresentation batchPresentation;
    private SubstitutionCriteria substitutionCriteria_always;

    private Actor actor1;
    private Actor actor2;
    private Group group;
    private Actor substitute;

    private User actor1User = null;
    private User actor2SUser = null;
    private User substituteUser = null;

    @Override
    protected void setUp() {
        val prefix = getClass().getName();
        h = new WfServiceTestHelper(prefix);
        batchPresentation = h.getTaskBatchPresentation();
        substitutionCriteria_always = h.createSubstitutionCriteria(null);

        actor1 = h.createActorIfNotExist(nameActor1, prefix);
        h.getExecutorService().setPassword(h.getAdminUser(), actor1, pwdActor1);
        actor2 = h.createActorIfNotExist(nameActor2, prefix);
        h.getExecutorService().setPassword(h.getAdminUser(), actor2, pwdActor2);
        group = h.createGroupIfNotExist(nameGroup, "description");
        h.addExecutorToGroup(actor1, group);
        h.addExecutorToGroup(actor2, group);
        substitute = h.createActorIfNotExist(nameSubstitute, prefix);
        h.getExecutorService().setPassword(h.getAdminUser(), substitute, pwdSubstitute);

        {
            val pp = Lists.newArrayList(Permission.LOGIN);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), group.getId(), pp, SecuredSingleton.SYSTEM);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), actor1.getId(), pp, SecuredSingleton.SYSTEM);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), actor2.getId(), pp, SecuredSingleton.SYSTEM);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), substitute.getId(), pp, SecuredSingleton.SYSTEM);
        }
        {
            val pp = Lists.newArrayList(Permission.READ);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), actor1.getId(), pp, substitute);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), substitute.getId(), pp, actor1);
        }
        actor1User = h.getAuthenticationService().authenticateByLoginPassword(nameActor1, pwdActor1);
        actor2SUser = h.getAuthenticationService().authenticateByLoginPassword(nameActor2, pwdActor2);
        substituteUser = h.getAuthenticationService().authenticateByLoginPassword(nameSubstitute, pwdSubstitute);

        byte[] parBytes = WfServiceTestHelper.readBytesFromFile(PROCESS_NAME + ".par");
        h.getDefinitionService().deployProcessDefinition(h.getAdminUser(), parBytes, Lists.newArrayList("testProcess"));
        WfDefinition definition = h.getDefinitionService().getLatestProcessDefinition(h.getAdminUser(), PROCESS_NAME);
        h.getAuthorizationService().setPermissions(h.getAdminUser(), actor1.getId(), Lists.newArrayList(Permission.START_PROCESS), definition);
    }

    @Override
    protected void tearDown() {
        h.getDefinitionService().undeployProcessDefinition(h.getAdminUser(), PROCESS_NAME, null);
        h.releaseResources();
        h.removeSubstitutionCriteria(substitutionCriteria_always);
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>Substitute assigns a task</li>
     * <li>User 1 tries to assign the task</li>
     * <li>User 2 tries to assign the task</li>
     * </ul>
     */
    // rask:
    public void testAssignAssigned() {
        WfTask[] actor1Tasks;
        WfTask[] actor2Tasks;
        WfTask[] substituteTasks;

        Substitution substitution1 = h.createActorSubstitutor(actor1User, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitute + ")", substitutionCriteria_always, true);
        {
            actor1Tasks = checkTaskList(actor1User, 0);
            actor2Tasks = checkTaskList(actor2SUser, 0);
            substituteTasks = checkTaskList(substituteUser, 0);
        }
        h.getExecutionService().startProcess(actor1User, PROCESS_NAME, null);
        {
            checkTaskList(actor1User, 1);
            checkTaskList(actor2SUser, 1);
            checkTaskList(substituteUser, 0);
        }
        h.setActorStatus(actor1, false);
        h.setActorStatus(actor2, false);
        {
            actor1Tasks = checkTaskList(actor1User, 1);
            actor2Tasks = checkTaskList(actor2SUser, 1);
            substituteTasks = checkTaskList(substituteUser, 1);
        }
        Actor actor = substituteUser.getActor();
        h.getTaskService().assignTask(substituteUser, substituteTasks[0].getId(), substituteTasks[0].getOwner(), actor);
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2SUser, 0);
            substituteTasks = checkTaskList(substituteUser, 1);
        }
        assertExceptionThrownOnAssign(actor1User, actor1Tasks[0]);
        assertExceptionThrownOnAssign(actor2SUser, actor2Tasks[0]);
        h.getTaskService().completeTask(substituteUser, substituteTasks[0].getId(), null);
        h.removeCriteriaFromSubstitution(substitution1);
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>Substitute executes a task</li>
     * <li>User 1 tries to assign the task</li>
     * <li>User 2 tries to assign the task</li>
     * </ul>
     */
    public void testAssignMoved() {
        WfTask[] actor1Tasks;
        WfTask[] actor2Tasks;
        WfTask[] substituteTasks;

        Substitution substitution1 = h.createActorSubstitutor(actor1User, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitute + ")", substitutionCriteria_always, true);
        {
            actor1Tasks = checkTaskList(actor1User, 0);
            actor2Tasks = checkTaskList(actor2SUser, 0);
            substituteTasks = checkTaskList(substituteUser, 0);
        }
        h.getExecutionService().startProcess(actor1User, PROCESS_NAME, null);
        {
            checkTaskList(actor1User, 1);
            checkTaskList(actor2SUser, 1);
            checkTaskList(substituteUser, 0);
        }
        h.setActorStatus(actor1, false);
        h.setActorStatus(actor2, false);
        {
            actor1Tasks = checkTaskList(actor1User, 1);
            actor2Tasks = checkTaskList(actor2SUser, 1);
            substituteTasks = checkTaskList(substituteUser, 1);
        }
        h.getTaskService().completeTask(substituteUser, substituteTasks[0].getId(), null);
        {
            checkTaskList(actor1User, actor1Tasks[0]);
            checkTaskList(actor2SUser, actor2Tasks[0]);
            checkTaskList(substituteUser, substituteTasks[0]);
        }
        assertExceptionThrownOnAssign(actor1User, actor1Tasks[0]);
        assertExceptionThrownOnAssign(actor2SUser, actor2Tasks[0]);
        h.removeCriteriaFromSubstitution(substitution1);
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>Substitute assigns a task</li>
     * <li>User 1 tries to execute the task</li>
     * <li>User 2 tries to execute the task</li>
     * </ul>
     */
    public void testMoveAssigned() {
        WfTask[] actor1Tasks;
        WfTask[] actor2Tasks;
        WfTask[] substituteTasks;

        Substitution substitution1 = h.createActorSubstitutor(actor1User, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitute + ")", substitutionCriteria_always, true);
        {
            actor1Tasks = checkTaskList(actor1User, 0);
            actor2Tasks = checkTaskList(actor2SUser, 0);
            substituteTasks = checkTaskList(substituteUser, 0);
        }
        h.getExecutionService().startProcess(actor1User, PROCESS_NAME, null);
        {
            checkTaskList(actor1User, 1);
            checkTaskList(actor2SUser, 1);
            checkTaskList(substituteUser, 0);
        }
        h.setActorStatus(actor1, false);
        h.setActorStatus(actor2, false);
        {
            actor1Tasks = checkTaskList(actor1User, 1);
            actor2Tasks = checkTaskList(actor2SUser, 1);
            substituteTasks = checkTaskList(substituteUser, 1);
        }
        Actor actor = substituteUser.getActor();
        h.getTaskService().assignTask(substituteUser, substituteTasks[0].getId(), substituteTasks[0].getOwner(), actor);
        {
            checkTaskList(actor1User, 0);
            checkTaskList(actor2SUser, 0);
            substituteTasks = checkTaskList(substituteUser, 1);
        }
        assertExceptionThrownOnExecute(actor1User, actor1Tasks[0]);
        assertExceptionThrownOnExecute(actor2SUser, actor2Tasks[0]);
        h.removeCriteriaFromSubstitution(substitution1);
    }

    private void assertExceptionThrownOnExecute(User user, WfTask task) {
        try {
            h.getTaskService().completeTask(user, task.getId(), null);
            throw new InternalApplicationException("Exception not thrown. Actor shouldn't see assigned/executed task by another user...");
        } catch (AuthenticationException e) {
            throw new InternalApplicationException("Auth exception thrown");
        } catch (AuthorizationException e) {
            // task was already assigned/executed by another user
        } catch (TaskDoesNotExistException e) {
        } catch (ExecutorDoesNotExistException e) {
            throw new InternalApplicationException("ExecutorOutOfDateException exception thrown");
        } catch (ValidationException e) {
            throw new InternalApplicationException("ValidationException exception thrown");
        }
    }

    private void assertExceptionThrownOnAssign(User user, WfTask task) {
        try {
            Actor actor = user.getActor();
            h.getTaskService().assignTask(user, task.getId(), task.getOwner(), actor);
            throw new InternalApplicationException("Exception TaskAlreadyAcceptedException not thrown");
        } catch (TaskDoesNotExistException e) {
            // TODO this is unexpected, fix me!!!
        } catch (TaskAlreadyAcceptedException e) {
        } catch (AuthenticationException e) {
            throw new InternalApplicationException("Auth exception thrown");
        }
    }

    private List<WfTask> checkTaskList(User user, WfTask task) {
        boolean result = false;
        List<WfTask> tasks = h.getTaskService().getMyTasks(user, batchPresentation);
        for (WfTask taskStub : tasks) {
            if (taskStub.equals(task) && taskStub.getName().equals(task.getName())) {
                result = true;
                break;
            }
        }
        assertFalse("Executed task is still in the user's tasks list.", result);
        return tasks;
    }

    private WfTask[] checkTaskList(User user, int expectedLength) {
        List<WfTask> tasks = h.getTaskService().getMyTasks(user, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
        return tasks.toArray(new WfTask[tasks.size()]);
    }
}
