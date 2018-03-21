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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
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

	class GetErrorThread extends Thread {

		private final Process process;
		private String message = null;

		public GetErrorThread(Process process) {
			this.process = process;
		}

		@Override
		public void run() {
			try (final BufferedReader b = new BufferedReader(new InputStreamReader(getErrorStream()))) {
				String line;
				if ((line = b.readLine()) != null) {
					message = line;
					synchronized (FreemarkerLanguageServer.this) {
						FreemarkerLanguageServer.this.notifyAll();
					}
				}
			} catch (IOException e) {
				message = e.getMessage();
			}
		}

		public void check() throws IOException {
			if (message != null) {
				throw new IOException(message);
			}
			if (!process.isAlive()) {
				throw new IOException("Process is not alive"); //$NON-NLS-1$
			}
		}

	}

	@Override
	public void start() throws IOException {
		// Start the process
		super.start();
		// Get the process by Java reflection
		Process p = getProcess();
		if (p != null) {
			// Sart a thread which read error stream to check that the java command is
			// working.
			GetErrorThread t = new GetErrorThread(p);
			try {
				t.start();
				// wait a little to execute java command line...
				synchronized (FreemarkerLanguageServer.this) {
					try {
						this.wait(500);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				// check if there is an error or if process is not alived.
				try {
					t.check();
				} catch (IOException e) {
					throw new IOException("Unable to start language server: " + this.toString(), e); //$NON-NLS-1$
				}
			} finally {
				t.interrupt();
			}
		}
	}

	private Process getProcess() {
		try {
			Field f = ProcessStreamConnectionProvider.class.getDeclaredField("process");
			f.setAccessible(true);
			Process p = (Process) f.get(this);
			return p;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder builder = super.createProcessBuilder();
		// override redirect to PIPE to read error stream with GetErrorThread
		builder.redirectError(ProcessBuilder.Redirect.PIPE);
		return builder;
	}

	@Override
	public String toString() {
		return "FreeMarker (" + super.toString() + ")";
	}

}
