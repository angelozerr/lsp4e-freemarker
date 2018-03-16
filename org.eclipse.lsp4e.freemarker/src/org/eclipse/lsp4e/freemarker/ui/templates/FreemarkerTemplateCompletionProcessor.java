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
package org.eclipse.lsp4e.freemarker.ui.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.lsp4e.freemarker.FreemarkerPlugin;
import org.eclipse.lsp4e.freemarker.ui.FreemarkerImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class FreemarkerTemplateCompletionProcessor extends TemplateCompletionProcessor {

	private static final class ProposalComparator implements Comparator<ICompletionProposal> {
		@Override
		public int compare(ICompletionProposal o1, ICompletionProposal o2) {
			int r1 = o1 instanceof TemplateProposal ? ((TemplateProposal) o1).getRelevance() : 0;
			int r2 = o2 instanceof TemplateProposal ? ((TemplateProposal) o2).getRelevance() : 0;
			return r2 - r1;
		}
	}

	private static final Comparator<ICompletionProposal> fgProposalComparator = new ProposalComparator();

	@Override
	protected Template[] getTemplates(String contextTypeId) {
		return FreemarkerPlugin.getDefault().getTemplateStore().getTemplates();
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		return FreemarkerPlugin.getDefault().getTemplateContextRegistry()
				.getContextType(FreemarkerTemplateContextType.CONTEXT_TYPE);
	}

	@Override
	protected Image getImage(Template template) {
		return FreemarkerImages.get(FreemarkerImages.OBJ_DESC_FTL_OBJ);
	}

	class AsynchComputeCompletionProposals implements Runnable {

		private ITextViewer viewer;
		private int offset;
		private FreemarkerTemplateCompletionProcessor processor;
		private ICompletionProposal[] proposals;

		public AsynchComputeCompletionProposals(ITextViewer viewer, int offset, FreemarkerTemplateCompletionProcessor processor) {
			this.viewer = viewer;
			this.offset = offset;
			this.processor = processor;
		}

		@Override
		public void run() {
			proposals = processor.doComputeCompletionProposals(viewer, offset);
		}

		public ICompletionProposal[] getProposals() {
			return proposals;
		}
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		Display display = viewer.getTextWidget().getDisplay();
		if (display.getThread() != Thread.currentThread()) {
			// GenericEditor uses async completion, which fails because of viewer.getSelectionProvider().getSelection() needs Thread UI
			AsynchComputeCompletionProposals a = new AsynchComputeCompletionProposals(viewer, offset, this);
			display.syncExec(a);
			return a.getProposals();
		}
		return doComputeCompletionProposals(viewer, offset);
	}

	public ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int offset) {

		ITextSelection selection= (ITextSelection) viewer.getSelectionProvider().getSelection();

		// adjust offset to end of normalized selection
		if (selection.getOffset() == offset)
			offset= selection.getOffset() + selection.getLength();

		String prefix= extractPrefix(viewer, offset);
		Region region= new Region(offset - prefix.length(), prefix.length());
		TemplateContext context= createContext(viewer, region);
		if (context == null)
			return new ICompletionProposal[0];

		context.setVariable("selection", selection.getText()); // name of the selection variables {line, word}_selection //$NON-NLS-1$

		Template[] templates= getTemplates(context.getContextType().getId());

		List<ICompletionProposal> matches= new ArrayList<>();
		for (Template template : templates) {
			try {
				context.getContextType().validate(template.getPattern());
			} catch (TemplateException e) {
				continue;
			}
			//if (template.matches(prefix, context.getContextType().getId()))
			if (isMatchingTemplate(template, prefix, context))
				matches.add(createProposal(template, context, (IRegion) region, getRelevance(template, prefix)));
		}

		Collections.sort(matches, fgProposalComparator);

		return matches.toArray(new ICompletionProposal[matches.size()]);
	}
	
	protected boolean isMatchingTemplate(Template template, String prefix,
            TemplateContext context) {
        return template.getName().startsWith(prefix)
                && template.matches(prefix, context.getContextType()
                        .getId());
    }
}
