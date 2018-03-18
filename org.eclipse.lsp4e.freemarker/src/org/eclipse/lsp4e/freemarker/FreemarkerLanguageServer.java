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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.osgi.framework.Bundle;

/**
 * LSP4e Freemarker Language server.
 *
 */
public class FreemarkerLanguageServer extends ProcessStreamConnectionProvider {

	public FreemarkerLanguageServer() {
		super(computeCommands(), computeWorkingDir());
	}

	private static String computeWorkingDir() {
		return System.getProperty("user.dir");
	}

	private static List<String> computeCommands() {
		List<String> commands = new ArrayList<>();
		commands.add("java");
		commands.add("-jar");
		commands.add(computeFreemarkerLanguageServerJarPath());
		return commands;
	}

	private static String computeFreemarkerLanguageServerJarPath() {
		Bundle bundle = Platform.getBundle(FreemarkerPlugin.PLUGIN_ID);
		URL fileURL = bundle.getEntry("server/freemarker-languageserver-all.jar");
		try {
			URL resolvedFileURL = FileLocator.toFileURL(fileURL);

			// We need to use the 3-arg constructor of URI in order to properly escape file
			// system chars
			URI resolvedURI = new URI(resolvedFileURL.getProtocol(), resolvedFileURL.getPath(), null);
			File file = new File(resolvedURI);
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				return "\"" + file.getAbsolutePath() + "\"";
			} else {
				return file.getAbsolutePath();
			}
		} catch (URISyntaxException | IOException exception) {
			FreemarkerPlugin.log(new Status(IStatus.ERROR, FreemarkerPlugin.PLUGIN_ID,
					"Cannot get the FreeMarker LSP Server jar.", exception)); //$NON-NLS-1$
		}
		return "";
	}

	@Override
	public String toString() {
		return "FreeMarker Language Server" + super.toString();
	}

}
