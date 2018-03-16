/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
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

		Bundle bundle = FreemarkerPlugin.getDefault().getBundle();
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
