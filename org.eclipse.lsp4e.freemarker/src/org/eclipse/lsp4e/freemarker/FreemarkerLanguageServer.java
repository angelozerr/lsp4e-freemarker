package org.eclipse.lsp4e.freemarker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.osgi.framework.Bundle;

public class FreemarkerLanguageServer extends ProcessStreamConnectionProvider {

	public FreemarkerLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add("java");
		commands.add("-jar");
		commands.add("freemarker-languageserver-all.jar");

		setCommands(commands);

		Bundle bundle = Activator.getDefault().getBundle();
		Path workingDir = Path.EMPTY;
		try {
			workingDir = new Path(FileLocator.toFileURL(FileLocator.find(bundle, new Path("server"), null)).getPath());
			setWorkingDirectory(workingDir.toOSString());
		} catch (IOException e) {
			// LanguageServerPlugin.logError(e);
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "Java Language Server" + super.toString();
	}
}
