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
package org.eclipse.lsp4e.freemarker.ui.preferences;

import org.eclipse.lsp4e.freemarker.FreemarkerPlugin;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

/**
 * Freemarker templates preference page
 *
 */
public class FreemarkerTemplatesPreferencePage extends TemplatePreferencePage implements IWorkbenchPreferencePage {

	public FreemarkerTemplatesPreferencePage() {
		setPreferenceStore(FreemarkerPlugin.getDefault().getPreferenceStore());
		setTemplateStore(FreemarkerPlugin.getDefault().getTemplateStore());
		setContextTypeRegistry(FreemarkerPlugin.getDefault().getTemplateContextRegistry());
	}

	protected boolean isShowFormatterSetting() {
		return false;
	}

	public boolean performOk() {
		boolean result = super.performOk();
		FreemarkerPlugin.flushInstanceScope();
		return result;
	}

}
