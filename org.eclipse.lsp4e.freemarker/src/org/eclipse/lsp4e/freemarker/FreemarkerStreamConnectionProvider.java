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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * Starts the FreeMarker LSP server inside the current JVM, and connects to it.
 */
public class FreemarkerStreamConnectionProvider extends LocalStreamConnectionProvider {

	public FreemarkerStreamConnectionProvider() {
		super(FreemarkerPlugin.getDefault().getLog(), FreemarkerPlugin.getPluginId());
	}

	private static final String LANGUAGE_SERVER_JAR_ENTRY_NAME = "server/freemarker-languageserver-all.jar"; //$NON-NLS-1$
	private static final String LANGUAGE_SERVER_LAUNCHER_CLASS_NAME = "freemarker.ext.languageserver.FreemarkerServerLauncher"; //$NON-NLS-1$

	@Override
	protected LocalServer launchServer(InputStream clientToServerStream, OutputStream serverToClientStream)
			throws IOException {
		URL[] classPath = getFreemarkerLanguageServerClassPath();
		logInfo("Using class path: " + Arrays.toString(classPath));
		
		URLClassLoader dynamicJarClassLoader = new URLClassLoader(classPath);

		Method launcherMethod;
		try {
			Class<?> launcherClass = dynamicJarClassLoader.loadClass(LANGUAGE_SERVER_LAUNCHER_CLASS_NAME);
			launcherMethod = launcherClass.getMethod("launch", //$NON-NLS-1$
					new Class<?>[] { InputStream.class, OutputStream.class });
		} catch (Exception e) {
			throw new RuntimeException(
					"Couldn't get launcher class and method via Java reflection (using class path: "
					+ Arrays.toString(classPath) + "); see cause exception", e); //$NON-NLS-2$
		}
		Future<?> launchedFuture;
		try {
			launchedFuture = (Future<?>) launcherMethod.invoke(null, clientToServerStream, serverToClientStream);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Error when calling launcher method; see cause exception", e); //$NON-NLS-1$
		}

		return new LocalServer(launchedFuture) {
			@Override
			public void stop() {
				super.stop();
				try {
					dynamicJarClassLoader.close();
				} catch (IOException e) {
					logError("Error when closing the dynamic jar class-loader", e); //$NON-NLS-1$
				}
			}
		};
	}

	private URL[] getFreemarkerLanguageServerClassPath() {
		Bundle bundle = Platform.getBundle(FreemarkerPlugin.PLUGIN_ID);
		if (bundle == null) {
			throw new RuntimeException("Bundle " + FreemarkerPlugin.PLUGIN_ID + " not found"); //$NON-NLS-1$
		}

		URL languageServerJarURL = bundle.getEntry(LANGUAGE_SERVER_JAR_ENTRY_NAME);
		if (languageServerJarURL == null) {
			throw new RuntimeException(
					"Entity " + LANGUAGE_SERVER_JAR_ENTRY_NAME + " not found in bundle " + FreemarkerPlugin.PLUGIN_ID); //$NON-NLS-1$
		}

		// TODO: Add freemarker.jar from the user project here, if it's found and has
		// high enough version, otherwise add freemarker.jar from this plugin.
		// (Currently, freemarker.jar is bundled into the language server jar.)

		return new URL[] { languageServerJarURL };
	}

}
