/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.bot.logic;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotAlreadyExistsException;
import ru.runa.wfe.bot.BotDoesNotExistException;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationAlreadyExistsException;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.BotTaskAlreadyExistsException;
import ru.runa.wfe.bot.BotTaskDoesNotExistException;
import ru.runa.wfe.bot.dao.BotDAO;
import ru.runa.wfe.bot.dao.BotStationDAO;
import ru.runa.wfe.bot.dao.BotTaskDAO;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class BotLogic extends CommonLogic {
    @Autowired
    private BotStationDAO botStationDAO;
    @Autowired
    private BotDAO botDAO;
    @Autowired
    private BotTaskDAO botTaskDAO;

    public List<BotStation> getBotStations() {
        return botStationDAO.getAll();
    }

    public BotStation createBotStation(User user, BotStation botStation) throws BotStationAlreadyExistsException {
        checkPermissionOnBotStation(user, Permission.BOT_STATION_CONFIGURE);
        if (botStationDAO.get(botStation.getName()) != null) {
            throw new BotStationAlreadyExistsException(botStation.getName());
        }
        return botStationDAO.create(botStation);
    }

    public void updateBotStation(User user, BotStation botStation) throws BotStationAlreadyExistsException {
        checkPermissionOnBotStation(user, Permission.BOT_STATION_CONFIGURE);
        BotStation botStationToCheck = getBotStation(botStation.getName());
        if (botStationToCheck != null && !Objects.equal(botStationToCheck.getId(), botStation.getId())) {
            throw new BotStationAlreadyExistsException(botStation.getName());
        }
        botStationDAO.update(botStation);
    }

    public BotStation getBotStationNotNull(Long id) throws BotStationDoesNotExistException {
        return botStationDAO.getNotNull(id);
    }

    public BotStation getBotStation(String name) {
        return botStationDAO.get(name);
    }

    public BotStation getBotStationNotNull(String name) throws BotStationDoesNotExistException {
        return botStationDAO.getNotNull(name);
    }

    public void removeBotStation(User user, Long id) throws BotStationDoesNotExistException {
        checkPermissionOnBotStation(user, Permission.BOT_STATION_CONFIGURE);
        List<Bot> bots = getBots(user, id);
        for (Bot bot : bots) {
            removeBot(user, bot.getId());
        }
        permissionDAO.deleteAllPermissions(getBotStationNotNull(id));
        botStationDAO.delete(id);
    }

    public Bot createBot(User user, Bot bot) throws BotAlreadyExistsException {
        checkPermissionOnBotStation(user, Permission.BOT_STATION_CONFIGURE);
        Preconditions.checkNotNull(bot.getBotStation());
        if (getBot(user, bot.getBotStation().getId(), bot.getUsername()) != null) {
            throw new BotAlreadyExistsException(bot.getUsername());
        }
        if (executorDAO.isExecutorExist(bot.getUsername()) && executorDAO.isExecutorExist(SystemProperties.getBotsGroupName())) {
            Actor botActor = executorDAO.getActor(bot.getUsername());
            Group botsGroup = executorDAO.getGroup(SystemProperties.getBotsGroupName());
            executorDAO.addExecutorToGroup(botActor, botsGroup);
        }
        bot = botDAO.create(bot);
        incrementBotStationVersion(bot);
        return bot;
    }

    public List<Bot> getBots(User user, Long botStationId) {
        checkReadPermissionOnBotStations(user);
        BotStation botStation = getBotStationNotNull(botStationId);
        return botDAO.getAll(botStation);
    }

    public Bot getBotNotNull(User user, Long id) {
        checkReadPermissionOnBotStations(user);
        return botDAO.getNotNull(id);
    }

    public Bot getBot(User user, Long botStationId, String name) {
        checkReadPermissionOnBotStations(user);
        BotStation botStation = getBotStationNotNull(botStationId);
        return botDAO.get(botStation, name);
    }

    public Bot getBotNotNull(User user, Long botStationId, String name) {
        checkReadPermissionOnBotStations(user);
        BotStation botStation = getBotStationNotNull(botStationId);
        return botDAO.getNotNull(botStation, name);
    }

    public void updateBot(User user, Bot bot, boolean incrementBotStationVersion) throws BotAlreadyExistsException {
            checkPermissionOnBotStation(user, Permission.BOT_STATION_CONFIGURE);
        Preconditions.checkNotNull(bot.getBotStation());
        Bot botToCheck = getBot(user, bot.getBotStation().getId(), bot.getUsername());
        if (botToCheck != null && !Objects.equal(botToCheck.getId(), bot.getId())) {
            throw new BotAlreadyExistsException(bot.getUsername());
        }
        bot = botDAO.update(bot);
        if (incrementBotStationVersion) {
            incrementBotStationVersion(bot);
        }
    }

    public void removeBot(User user, Long id) throws BotDoesNotExistException {
        checkPermissionOnBotStation(user, Permission.BOT_STATION_CONFIGURE);
        List<BotTask> tasks = getBotTasks(user, id);
        for (BotTask botTask : tasks) {
            removeBotTask(user, botTask.getId());
        }
        Bot bot = getBotNotNull(user, id);
        if (executorDAO.isExecutorExist(bot.getUsername()) && executorDAO.isExecutorExist(SystemProperties.getBotsGroupName())) {
            Actor botActor = executorDAO.getActor(bot.getUsername());
            Group botsGroup = executorDAO.getGroup(SystemProperties.getBotsGroupName());
            executorDAO.removeExecutorFromGroup(botActor, botsGroup);
        }
        botDAO.delete(id);
    }

    public BotTask createBotTask(User user, BotTask botTask) throws BotTaskAlreadyExistsException {
        checkPermissionOnBotStation(user, Permission.BOT_STATION_CONFIGURE);
        Preconditions.checkNotNull(botTask.getBot());
        if (getBotTask(user, botTask.getBot().getId(), botTask.getName()) != null) {
            throw new BotTaskAlreadyExistsException(botTask.getName());
        }
        botTask = botTaskDAO.create(botTask);
        incrementBotStationVersion(botTask);
        return botTask;
    }

    public List<BotTask> getBotTasks(User user, Long id) {
        checkReadPermissionOnBotStations(user);
        Bot bot = getBotNotNull(user, id);
        return botTaskDAO.getAll(bot);
    }

    public BotTask getBotTaskNotNull(User user, Long id) {
        checkReadPermissionOnBotStations(user);
        return botTaskDAO.getNotNull(id);
    }

    public BotTask getBotTask(User user, Long botId, String name) {
        checkReadPermissionOnBotStations(user);
        Bot bot = getBotNotNull(user, botId);
        return botTaskDAO.get(bot, name);
    }

    public BotTask getBotTaskNotNull(User user, Long botId, String name) {
        checkReadPermissionOnBotStations(user);
        Bot bot = getBotNotNull(user, botId);
        return botTaskDAO.getNotNull(bot, name);
    }

    public void updateBotTask(User user, BotTask botTask) throws BotTaskAlreadyExistsException {
        checkPermissionOnBotStation(user, Permission.BOT_STATION_CONFIGURE);
        Preconditions.checkNotNull(botTask.getBot());
        BotTask botTaskToCheck = getBotTask(user, botTask.getBot().getId(), botTask.getName());
        if (botTaskToCheck != null && !Objects.equal(botTaskToCheck.getId(), botTask.getId())) {
            throw new BotTaskAlreadyExistsException(botTask.getName());
        }
        if (botTask.getConfiguration() != null && botTask.getConfiguration().length == 0) {
            BotTask botTaskFromDB = getBotTaskNotNull(user, botTask.getId());
            botTask.setConfiguration(botTaskFromDB.getConfiguration());
        }
        botTask = botTaskDAO.update(botTask);
        incrementBotStationVersion(botTask);
    }

    public void removeBotTask(User user, Long id) throws BotTaskDoesNotExistException {
        checkPermissionOnBotStation(user, Permission.BOT_STATION_CONFIGURE);
        BotTask botTask = getBotTaskNotNull(user, id);
        botTaskDAO.delete(id);
        incrementBotStationVersion(botTask);
    }

    private void checkPermissionOnBotStation(User user, Permission permission) {
        checkPermissionAllowed(user, BotStation.INSTANCE, permission);
    }

    private void checkReadPermissionOnBotStations(User user) {
        // bot can read botstation
        if (botDAO.get(user.getName()) == null) {
            checkPermissionOnBotStation(user, Permission.READ);
        }
    }

    private void incrementBotStationVersion(Object entity) {
        BotStation botStation;
        if (entity instanceof BotStation) {
            botStation = (BotStation) entity;
        } else if (entity instanceof Bot) {
            botStation = ((Bot) entity).getBotStation();
        } else if (entity instanceof BotTask) {
            botStation = ((BotTask) entity).getBot().getBotStation();
        } else {
            throw new InternalApplicationException("Unexpected entity class " + entity);
        }
        botStation.setVersion(botStation.getVersion() + 1);
        botStationDAO.update(botStation);
    }

}
