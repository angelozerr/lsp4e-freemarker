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
package org.eclipse.lsp4e.freemarker.ui;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.lsp4e.freemarker.FreemarkerPlugin;
import org.eclipse.swt.graphics.Image;

/**
 * Freemarker images
 *
 */
public class FreemarkerImages {

	private static final String NAME_PREFIX = FreemarkerPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	private static ImageRegistry PLUGIN_REGISTRY;

	private final static String ICONS_PATH = "icons/"; //$NON-NLS-1$

	/**
	 * Set of predefined Image Descriptors.
	 */
	private static final String PATH_OBJ = ICONS_PATH + "obj16/"; //$NON-NLS-1$
	// private static final String PATH_LCL = ICONS_PATH + "elcl16/"; //$NON-NLS-1$
	// private static final String PATH_TOOL = ICONS_PATH + "etool16/";
	// //$NON-NLS-1$

	// Freemarker
	public static final ImageDescriptor DESC_FTL_OBJ = create(PATH_OBJ, "ftl.gif"); //$NON-NLS-1$
	public static final String OBJ_DESC_FTL_OBJ = NAME_PREFIX + "DESC_FTL_OBJ"; //$NON-NLS-1$

	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeImageURL(prefix, name));
	}

	private static URL makeImageURL(String prefix, String name) {
		String path = "$nl$/" + prefix + name; //$NON-NLS-1$
		return FileLocator.find(FreemarkerPlugin.getDefault().getBundle(), new Path(path), null);
	}

	public static Image get(String key) {
		if (PLUGIN_REGISTRY == null)
			initialize();
		return PLUGIN_REGISTRY.get(key);
	}

	/* package */
	private static final void initialize() {
		PLUGIN_REGISTRY = new ImageRegistry();
		manage(OBJ_DESC_FTL_OBJ, DESC_FTL_OBJ);
	}

	private static Image manage(String key, ImageDescriptor desc) {
		Image image = desc.createImage();
		PLUGIN_REGISTRY.put(key, image);
		return image;
	}
}
