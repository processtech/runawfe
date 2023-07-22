package ru.runa.wf.web.tag;

import org.apache.ecs.Entities;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.BR;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.WebResources;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.action.ImportDataFileAction;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.datafile.builder.DataFileBuilder;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "importDataFile")
public class ImportDataFileTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isVisible() {
        return WebResources.isImportExportEnabled();
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        getForm().setEncType(Form.ENC_UPLOAD);
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        if (SystemProperties.getAdministratorName().equals(getUser().getName())) {
            createAddDataRow(table);
        }
        doNotChangeInternalStoragePath(table);
        clearPasswordRow(table);
        clearPasswordForDataSourcesRow(table);
        Input fileInput = HTMLUtils.createInput(Input.FILE, FileForm.FILE_INPUT_NAME, "", true, true, DataFileBuilder.FILE_EXT);
        table.addElement(HTMLUtils.createRow(MessagesOther.TITLE_DATAFILE.message(pageContext), fileInput));
        tdFormElement.addElement(table);
    }

    private void createAddDataRow(Table table) {
        TD td = new TD();
        Input uploadInput = new Input(Input.RADIO, ImportDataFileAction.UPLOAD_PARAM, ImportDataFileAction.UPLOAD_ONLY);
        uploadInput.setID(ImportDataFileAction.UPLOAD_ONLY);
        uploadInput.setChecked(true);
        td.addElement(uploadInput);
        Label label = new Label(ImportDataFileAction.UPLOAD_ONLY);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_UPLOADONLY.message(pageContext)));
        td.addElement(label);
        td.addElement(Entities.NBSP);
        Input uploadAndClearInput = new Input(Input.RADIO, ImportDataFileAction.UPLOAD_PARAM, ImportDataFileAction.CLEAR_BEFORE_UPLOAD);
        uploadAndClearInput.setID(ImportDataFileAction.CLEAR_BEFORE_UPLOAD);
        td.addElement(uploadAndClearInput);
        label = new Label(ImportDataFileAction.CLEAR_BEFORE_UPLOAD);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_CLEARBEFOREUPLOAD.message(pageContext)));
        td.addElement(label);
        table.addElement(HTMLUtils.createRow(MessagesOther.TITLE_DATAFILE_ACTION.message(pageContext), td));
    }

    private void doNotChangeInternalStoragePath(Table table) {
        TD td = new TD();
        Input doNotChangeInternalStoragePath = new Input(Input.RADIO, ImportDataFileAction.CHANGE_INTERNAL_STORAGE_PATH_PARAM, ImportDataFileAction.DO_NOT_CHANGE_INTERNAL_STORAGE_PATH);
        doNotChangeInternalStoragePath.setID(ImportDataFileAction.DO_NOT_CHANGE_INTERNAL_STORAGE_PATH);
        doNotChangeInternalStoragePath.setChecked(true);
        td.addElement(doNotChangeInternalStoragePath);
        Label label = new Label(ImportDataFileAction.DO_NOT_CHANGE_INTERNAL_STORAGE_PATH);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_DO_NOT_CHANGE_INTERNAL_STORAGE_PATH.message(pageContext)));
        td.addElement(label);
        td.addElement(Entities.NBSP);

        Input changeInternalStoragePath = new Input(Input.RADIO, ImportDataFileAction.CHANGE_INTERNAL_STORAGE_PATH_PARAM, ImportDataFileAction.CHANGE_INTERNAL_STORAGE_PATH);
        changeInternalStoragePath.setID(ImportDataFileAction.CHANGE_INTERNAL_STORAGE_PATH);
        td.addElement(changeInternalStoragePath);
        label = new Label(ImportDataFileAction.CHANGE_INTERNAL_STORAGE_PATH);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_CHANGE_INTERNAL_STORAGE_PATH.message(pageContext)));
        td.addElement(label);
        table.addElement(HTMLUtils.createRow(MessagesOther.CHANGE_INTERNAL_STORAGE_PATH_ACTION.message(pageContext), td));
    }

    private void clearPasswordRow(Table table) {
        TD td = new TD();
        Input setPasswordInput = new Input(Input.RADIO, ImportDataFileAction.PASSWORD_PARAM, ImportDataFileAction.SET_PASSWORD);
        setPasswordInput.setID(ImportDataFileAction.SET_PASSWORD);
        setPasswordInput.setChecked(true);
        td.addElement(setPasswordInput);
        Label label = new Label(ImportDataFileAction.SET_PASSWORD);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_SET_PASSWORD.message(pageContext)));
        td.addElement(label);
        td.addElement(new Span(":").setClass(Resources.CLASS_SYMBOL));
        td.addElement(Entities.NBSP);
        Input passwordText = new Input(Input.TEXT, ImportDataFileAction.PASSWORD_VALUE_PARAM, "123");
        passwordText.setID(ImportDataFileAction.PASSWORD_VALUE_PARAM);
        passwordText.setStyle("width: 300px;");
        td.addElement(passwordText);
        td.addElement(new BR());
        Input clearPasswordInput = new Input(Input.RADIO, ImportDataFileAction.PASSWORD_PARAM, ImportDataFileAction.CLEAR_PASSWORD);
        clearPasswordInput.setID(ImportDataFileAction.CLEAR_PASSWORD);
        td.addElement(clearPasswordInput);
        label = new Label(ImportDataFileAction.CLEAR_PASSWORD);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_CLEAR_PASSWORD.message(pageContext)));
        td.addElement(label);
        table.addElement(HTMLUtils.createRow(MessagesOther.TITLE_DATAFILE_ACTION_PASSWORD.message(pageContext), td));
    }

    private void clearPasswordForDataSourcesRow(Table table) {
        TD td = new TD();
        Input setPasswordInput = new Input(Input.RADIO, ImportDataFileAction.PASSWORD_DATA_SOURCE_PARAM, ImportDataFileAction.SET_PASSWORD);
        setPasswordInput.setID(ImportDataFileAction.SET_PASSWORD_DATA_SOURCE);
        setPasswordInput.setChecked(true);
        td.addElement(setPasswordInput);
        Label label = new Label(ImportDataFileAction.SET_PASSWORD_DATA_SOURCE);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_SET_PASSWORD_DATA_SOURCE.message(pageContext)));
        td.addElement(label);
        td.addElement(new Span(":").setClass(Resources.CLASS_SYMBOL));
        td.addElement(Entities.NBSP);
        Input passwordText = new Input(Input.TEXT, ImportDataFileAction.PASSWORD_VALUE_DATA_SOURCE_PARAM, "321");
        passwordText.setID(ImportDataFileAction.PASSWORD_VALUE_DATA_SOURCE_PARAM);
        passwordText.setStyle("width: 300px;");
        td.addElement(passwordText);
        td.addElement(new BR());
        Input clearPasswordInput = new Input(Input.RADIO, ImportDataFileAction.PASSWORD_DATA_SOURCE_PARAM, ImportDataFileAction.CLEAR_PASSWORD);
        clearPasswordInput.setID(ImportDataFileAction.CLEAR_PASSWORD_DATA_SOURCE);
        td.addElement(clearPasswordInput);
        label = new Label(ImportDataFileAction.CLEAR_PASSWORD_DATA_SOURCE);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_CLEAR_PASSWORD_DATA_SOURCE.message(pageContext)));
        td.addElement(label);
        table.addElement(HTMLUtils.createRow(MessagesOther.TITLE_DATAFILE_ACTION_PASSWORD_DATA_SOURCE.message(pageContext), td));
    }

    @Override
    protected String getTitle() {
        return MessagesOther.TITLE_IMPORT_DATAFILE.message(pageContext);
    }

    @Override
    public String getAction() {
        return ImportDataFileAction.ACTION_PATH;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesOther.TITLE_IMPORT_DATAFILE.message(pageContext);
    }
}
