package ru.runa.wfe.datafile.builder;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.logic.BotLogic;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.user.User;

@Component
public class BotDataFileBuilder implements DataFileBuilder {

    @Autowired
    private BotLogic botLogic;

    @Override
    public void build(ZipOutputStream zos, Document script, User user) throws IOException {
        List<BotStation> botStations = botLogic.getBotStations();
        for (BotStation botStation : botStations) {
            populateBotStation(script, botStation);
            List<Bot> bots = botLogic.getBots(user, botStation.getId());
            for (Bot bot : bots) {
                populateBot(script, bot, botStation.getName());
                List<BotTask> botTasks = botLogic.getBotTasks(user, bot.getId());
                for (BotTask botTask : botTasks) {
                    populateBotTask(script, botTask, botStation.getName(), bot.getUsername());
                    byte[] conf = botTask.getConfiguration();
                    if (conf == null || conf.length == 0) {
                        continue;
                    }                    
                    zos.putNextEntry(new ZipEntry(PATH_TO_BOTTASK + getConfigurationName(botTask) + ".conf"));                                       
                    zos.write(conf);
                    byte[] embeddedFile = botTask.getEmbeddedFile();
                    if (embeddedFile != null && StringUtils.isNotEmpty(botTask.getEmbeddedFileName())) {
                        zos.putNextEntry(new ZipEntry(PATH_TO_BOTTASK + botTask.getEmbeddedFileName()));
                        zos.write(embeddedFile);
                    }
                }
            }
        }
    }

    private String getConfigurationName(BotTask botTask) {
       	return botTask.getBot().getUsername() + "_" + botTask.getName();
    }
    
    private void populateBotStation(Document script, BotStation botStation) {
        Element element = script.getRootElement().addElement("createBotStation", XmlUtils.RUNA_NAMESPACE);
        if (StringUtils.isNotEmpty(botStation.getName())) {
            element.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, botStation.getName());
        }
    }

    private void populateBot(Document script, Bot bot, String botStationName) {
        Element element = script.getRootElement().addElement("createBot", XmlUtils.RUNA_NAMESPACE);
        element.addAttribute(AdminScriptConstants.BOTSTATION_ATTRIBUTE_NAME, botStationName);
        if (StringUtils.isNotEmpty(bot.getUsername())) {
            element.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, bot.getUsername());
        }
        element.addAttribute(AdminScriptConstants.PASSWORD_ATTRIBUTE_NAME, "");
        element.addAttribute(AdminScriptConstants.SEQUENTIAL_EXECUTION_ATTRIBUTE_NAME, "" + bot.isSequentialExecution());
    }

    private void populateBotTask(Document script, BotTask botTask, String botStationName, String botName) {
        Element element = script.getRootElement().addElement("addConfigurationsToBot", XmlUtils.RUNA_NAMESPACE);
        element.addAttribute(AdminScriptConstants.BOTSTATION_ATTRIBUTE_NAME, botStationName);
        element.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, botName);
        Element subElement = element.addElement("botConfiguration", XmlUtils.RUNA_NAMESPACE);
        subElement.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, botTask.getName());
        if (StringUtils.isNotEmpty(botTask.getTaskHandlerClassName())) {
            subElement.addAttribute(AdminScriptConstants.HANDLER_ATTRIBUTE_NAME, botTask.getTaskHandlerClassName());
        }
        if (StringUtils.isNotEmpty(botTask.getName())) {
            subElement.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, botTask.getName());
        }
        if (StringUtils.isNotEmpty(botTask.getEmbeddedFileName())) {
            subElement.addAttribute(AdminScriptConstants.EMBEDDED_FILE_ATTRIBUTE_NAME, PATH_TO_BOTTASK + botTask.getEmbeddedFileName());
        }
        if (botTask.getConfiguration() != null && botTask.getConfiguration().length > 0) {           
            subElement.addAttribute(AdminScriptConstants.CONFIGURATION_STRING_ATTRIBUTE_NAME, getConfigurationName(botTask) + ".conf");
        }
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
