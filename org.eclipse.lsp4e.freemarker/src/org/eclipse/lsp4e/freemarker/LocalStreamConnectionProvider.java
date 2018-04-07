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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Objects;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.StreamConnectionProvider;

/**
 * A {@link StreamConnectionProvider} that connects to a LSP server that runs in
 * the client's JVM, not as a separate process.
 */
// This meant to be independent of the Freemareker plugin project, so it shouldn't use local dependencies, like
// FreemarkerPlugin.log(...).
public abstract class LocalStreamConnectionProvider implements StreamConnectionProvider {

	private static final int PIPE_BUFFER_SIZE = 8192;

	// Certainly nobody wants to internationalize error messages like this...
	private static final String STOP_ERROR_MESSAGE = "Error while stopping the local LSP service."; //$NON-NLS-1$

	private PipedOutputStream clientToServerStream;
	private PipedInputStream serverToClientStream;
	private PipedInputStream clientToServerStreamReverse;
	private PipedOutputStream serverToClientStreamReverse;
	private LocalServer localServer;

	private final ILog log;
	private final String pluginId;

	/**
	 * @param log      The log used by the embedding plug-in. (It's assumed that the
	 *                 server is local because it's embedded into an Eclipse
	 *                 plug-in.)
	 * @param pluginId The ID of the embedding Eclipse plug-in. Used for
	 *                 {@link IStatus#getPlugin()} for example.
	 */
	protected LocalStreamConnectionProvider(ILog log, String pluginId) {
		this.log = log;
		this.pluginId = pluginId;
	}

	@Override
	public synchronized void start() throws IOException {
		clientToServerStream = new PipedOutputStream();
		serverToClientStream = new PipedInputStream(PIPE_BUFFER_SIZE);
		clientToServerStreamReverse = new PipedInputStream(clientToServerStream, PIPE_BUFFER_SIZE);
		serverToClientStreamReverse = new PipedOutputStream(serverToClientStream);
		localServer = launchServer(clientToServerStreamReverse, serverToClientStreamReverse);
	}

	protected abstract LocalServer launchServer(InputStream clientToServerStream, OutputStream serverToClientStream)
			throws IOException;

	@Override
	public InputStream getInputStream() {
		return serverToClientStream;
	}

	@Override
	public OutputStream getOutputStream() {
		return clientToServerStream;
	}

	@Override
	public InputStream getErrorStream() {
		return null;
	}

	@Override
	public synchronized void stop() {
		if (localServer == null) {
			return;
		}

		try {
			localServer.stop();
		} catch (Exception e) {
			logError(STOP_ERROR_MESSAGE, e);
		}
		localServer = null;

		try {
			clientToServerStream.close();
		} catch (IOException e) {
			logError(STOP_ERROR_MESSAGE, e);
		}
		clientToServerStream = null;

		try {
			clientToServerStreamReverse.close();
		} catch (IOException e) {
			logError(STOP_ERROR_MESSAGE, e);
		}
		clientToServerStreamReverse = null;

		try {
			serverToClientStreamReverse.close();
		} catch (IOException e) {
			logError(STOP_ERROR_MESSAGE, e);
		}
		serverToClientStreamReverse = null;

		try {
			serverToClientStream.close();
		} catch (IOException e) {
			logError(STOP_ERROR_MESSAGE, e);
		}
		serverToClientStream = null;
	}

	/**
	 * See similar {@link LocalStreamConnectionProvider} constructor parameter.
	 */
	protected ILog getLog() {
		return log;
	}

	/**
	 * See similar {@link LocalStreamConnectionProvider} constructor parameter.
	 */
	protected String getPluginId() {
		return pluginId;
	}

	/**
	 * Convenience method to log an error.
	 */
	protected void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, getPluginId(), message, e));
	}

	/**
	 * Convenience method to log an info message.
	 */
	protected void logInfo(String message) {
		getLog().log(new Status(IStatus.INFO, getPluginId(), message));
	}

	/**
	 * Represents a locally launched server, that can be stopped.
	 */
	public static abstract class LocalServer {

		private final Future<?> launcherFuture;

		/**
		 * @param launcherFuture The future returned by
		 *                       {@link org.eclipse.lsp4j.jsonrpc.Launcher#startListening()}
		 */
		public LocalServer(Future<?> launcherFuture) {
			Objects.requireNonNull(launcherFuture, "launcherFuture");
			this.launcherFuture = launcherFuture;
		}

		/**
		 * Override this if you have resource to release.
		 */
		public void stop() {
			// TODO I'm not sure if I can stop the language server like this... will have to
			// look into the org.eclipse.lsp4j.jsonrpc.Launcher#startListening()
			// implementation, as it has no JavaDoc.
			launcherFuture.cancel(true);
		}
	}

}
