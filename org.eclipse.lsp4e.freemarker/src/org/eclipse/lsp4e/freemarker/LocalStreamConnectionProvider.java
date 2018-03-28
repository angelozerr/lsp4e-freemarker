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
import java.util.concurrent.Future;

import org.eclipse.lsp4e.server.StreamConnectionProvider;

/**
 * A {@link StreamConnectionProvider} that connects to a LSP server that runs in the client's JVM, not as a separate process.  
 */
public abstract class LocalStreamConnectionProvider implements StreamConnectionProvider {
	
	private static final int PIPE_BUFFER_SIZE = 8192;
	
	private PipedOutputStream clientToServerStream;
	private PipedInputStream serverToClientStream;
	private PipedInputStream clientToServerStreamReverse;
	private PipedOutputStream serverToClientStreamReverse;
	private Future<?> launched; 
	
	public LocalStreamConnectionProvider() {
	}

	@Override
	public synchronized void start() throws IOException {
		clientToServerStream = new PipedOutputStream();
		serverToClientStream = new PipedInputStream(PIPE_BUFFER_SIZE);
		clientToServerStreamReverse = new PipedInputStream(clientToServerStream, PIPE_BUFFER_SIZE);
		serverToClientStreamReverse = new PipedOutputStream(serverToClientStream);
		launched = launch(clientToServerStreamReverse, serverToClientStreamReverse);
	}

	protected abstract Future<?> launch(InputStream clientToServerStream, OutputStream serverToClientStream) throws IOException;

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
		if (launched == null) {
			return;
		}
		
		// TODO Not sure if it works like this...
		launched.cancel(true);
		launched = null;
		
		try {
			clientToServerStream.close();
		} catch (IOException e) {
			// TODO Log
			e.printStackTrace();
		}
		clientToServerStream = null;

		try {
			clientToServerStreamReverse.close();
		} catch (IOException e) {
			// TODO Log
			e.printStackTrace();
		}
		clientToServerStreamReverse = null;
		
		try {
			serverToClientStreamReverse.close();
		} catch (IOException e) {
			// TODO Log
			e.printStackTrace();
		}
		serverToClientStreamReverse = null;
		
		try {
			serverToClientStream.close();
		} catch (IOException e) {
			// TODO Log
			e.printStackTrace();
		}
		serverToClientStream = null;
	}

}
