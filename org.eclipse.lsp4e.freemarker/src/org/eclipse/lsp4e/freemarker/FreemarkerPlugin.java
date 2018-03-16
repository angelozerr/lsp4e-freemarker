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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.lsp4e.freemarker.ui.templates.FreemarkerTemplateContextType;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The activator class controls the plug-in life cycle
 */
public class FreemarkerPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.lsp4e.freemarker"; //$NON-NLS-1$

	/**
	 * The key to store customized templates.
	 */
	private static final String TEMPLATES_KEY = getPluginId() + "custom_templates"; //$NON-NLS-1$

	// The shared instance
	private static FreemarkerPlugin plugin;

	private TemplateStore templateStore;

	private ContributionContextTypeRegistry contextTypeRegistry;

	/**
	 * The constructor
	 */
	public FreemarkerPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static FreemarkerPlugin getDefault() {
		return plugin;
	}

	public static String getPluginId() {
		return FreemarkerPlugin.PLUGIN_ID;
	}

	/**
	 * Returns the template store for the Freemarker editor templates.
	 *
	 * @return the template store for the Freemarker editor templates
	 */
	public TemplateStore getTemplateStore() {
		if (templateStore == null) {
			final IPreferenceStore store = getPreferenceStore();
			templateStore = new ContributionTemplateStore(getTemplateContextRegistry(), store, TEMPLATES_KEY);
			try {
				templateStore.load();
			} catch (IOException e) {
				log(e);
			}
			templateStore.startListeningForPreferenceChanges();
		}
		return templateStore;
	}

	/**
	 * Returns the template context type registry for the Freemarker plug-in.
	 *
	 * @return the template context type registry for the Freemarker plug-in
	 * 
	 */
	public synchronized ContextTypeRegistry getTemplateContextRegistry() {
		if (contextTypeRegistry == null) {
			ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry();
			registry.addContextType(FreemarkerTemplateContextType.CONTEXT_TYPE);
			contextTypeRegistry = registry;
		}
		return contextTypeRegistry;
	}

	/**
	 * Flushes the instance scope of this plug-in.
	 */
	public static void flushInstanceScope() {
		try {
			InstanceScope.INSTANCE.getNode(getPluginId()).flush();
		} catch (BackingStoreException e) {
			log(e);
		}
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), FreemarkerMessages.FreemarkerPlugin_internal_error, e));
	}
}
