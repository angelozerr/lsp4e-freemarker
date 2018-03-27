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
import java.util.concurrent.Future;

import freemarker.ext.languageserver.FreemarkerLanguageServerLauncher;

/**
 * Provides a connection to the Freemarker Language Server, through which one can communicate with it through
 * the protocol specified by the Language Server Protocol Specification.
 */
public class FreemarkerLanguageServerStreamConnectionProvider extends LocalStreamConnectionProvider {

	@Override
	protected Future<?> launch(InputStream clientToServerStream, OutputStream serverToClientStream) throws IOException {
		return FreemarkerLanguageServerLauncher.launch(clientToServerStream, serverToClientStream);
	}

}
