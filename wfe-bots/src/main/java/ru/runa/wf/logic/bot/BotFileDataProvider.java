package ru.runa.wf.logic.bot;

import ru.runa.wfe.definition.par.FileDataProviderImpl;

public class BotFileDataProvider extends FileDataProviderImpl {
	byte[] embeddedFile;
		
	public BotFileDataProvider(byte[] embeddedFile) {
		this.embeddedFile = embeddedFile;
	}

	@Override
	public byte[] getFileData(String fileName) {
		return embeddedFile;
	}
}
