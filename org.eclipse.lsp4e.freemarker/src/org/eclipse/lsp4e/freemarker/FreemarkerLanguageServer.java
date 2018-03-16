package org.eclipse.lsp4e.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.osgi.framework.Bundle;

public class FreemarkerLanguageServer extends ProcessStreamConnectionProvider {

	public FreemarkerLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add("java");
		commands.add("-jar");
		commands.add("D:\\_Personal\\Freemarker\\org.lsp4fm\\target\\freemarker-server-all.jar");
//		commands.add("-Declipse.application=org.eclipse.jdt.ls.core.id1");
//		commands.add("-Dosgi.bundles.defaultStartLevel=4");
//		commands.add("-Declipse.product=org.eclipse.jdt.ls.core.product");
//		commands.add("-Dlog.protocol=true");
//		commands.add("-Dlog.level=ALL");
//		commands.add("-noverify");
//		commands.add("-Xmx1G");
//		commands.add("-jar");
//		commands.add("./plugins/org.eclipse.equinox.launcher_1.4.0.v20161219-1356.jar");
//		commands.add("-configuration");
//		if (Platform.getOS().equals(Platform.OS_WIN32)) {
//			commands.add("./config_win");
//		}
//		if (Platform.getOS().equals(Platform.OS_LINUX)) {
//			commands.add("./config_linux");
//		}
//		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
//			commands.add("./config_mac");
//		}
//		commands.add("-data");
//		commands.add("./data");

		setCommands(commands);

		Bundle bundle = Activator.getDefault().getBundle();
		Path workingDir = Path.EMPTY;
		//try {
			workingDir = new Path("D:\\_Personal\\Freemarker\\org.lsp4fm\\target"); //new Path(FileLocator.toFileURL(FileLocator.find(bundle, new Path("server"), null)).getPath());
			setWorkingDirectory(workingDir.toOSString());
//		} catch (IOException e) {
//			LanguageServerPlugin.logError(e);
//		}
	}

	@Override
	public String toString() {
		return "Java Language Server" + super.toString();
	}
}
