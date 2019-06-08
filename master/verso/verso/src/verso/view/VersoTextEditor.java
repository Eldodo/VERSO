package verso.view;


import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextOperationAction;

import verso.builder.IncrementalBuilder;
import verso.builder.VersoModificationListener;
import verso.model.Element;
import verso.model.Line;
import verso.model.SystemDef;
import verso.model.SystemManager;
import verso.representation.Lines.visitor.LineMapping;
import verso.util.TypeFinder;

public class VersoTextEditor extends TextEditor implements VersoModificationListener, Runnable {
	LineMapping currentMapping = null;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		System.out.println("Init is fun as hell");
		this.setDocumentProvider(new TextFileDocumentProvider());
		super.init(site, input);
		System.out.println(input.getName());
		IncrementalBuilder.addListener(this);
		currentMapping = new LineMapping();
		currentMapping.setMetricName("LineDepth");
		currentMapping.setGraphicalValue("ColorGradation");
	}

	protected void editorSaved() {

		super.editorSaved();

	}

	protected void configureSourceViewerDecorationSupport() {
		System.out.println("VersoTextEditor.configureSourceViewerDecorationSupport()");
		SourceViewerDecorationSupport svds = this.getSourceViewerDecorationSupport(this.getSourceViewer());
		svds.uninstall();
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		this.performSave(true, null);
	}

	protected void createActions() {
		super.createActions();

		IAction a = new TextOperationAction(new MyResources(), null, this, 0); // $NON-NLS-1$
		a.setActionDefinitionId("ChangeColor");
		setAction("ChangeColor", a);
		System.out.println("allo");
		SourceViewerDecorationSupport svds = this.getSourceViewerDecorationSupport(this.getSourceViewer());
		svds.dispose();
	}

	public void changeColor() {

		SystemDef sys = SystemManager.getSystem("Test");
		IEditorInput itr = this.getEditorInput();
		IFile f = ((FileEditorInput) itr).getFile();
		ICompilationUnit comp = TypeFinder.findCompilationUnitFromFile(f);

		String name = "";
		if (sys != null) {
			try {
				IType[] types = comp.getAllTypes();
				name = types[0].getFullyQualifiedName();
			} catch (Exception e) {
				System.out.println(e);
			}
			Element e = sys.getElement(name);
			if (e != null) {
				ISourceViewer isv = this.getSourceViewer();
				if (isv != null) {
					applyMapping(this.currentMapping, e, isv);

				}
			}
		}
	}

	public void listen(SystemDef sys) {
		this.getEditorSite().getShell().getDisplay().syncExec(this);
	}

	public void run() {
		changeColor();
	}

	public void setMapping(LineMapping map) {
		this.currentMapping = map;
	}

	private void applyMapping(LineMapping map, Element e, ISourceViewer isv) {
		if (map == null)
			return;
		TextPresentation tp = new TextPresentation();
		for (Line l : e.getLines()) {
			double value = l.getMetric(map.getMetricName()).getNormalizedValue();
			if (map.getGraphicalValue().equals("BlueColor")) {
				tp.addStyleRange(new StyleRange(l.getLineStart(), l.getLenght(),
						new Color(Display.findDisplay(Thread.currentThread()), 0, 0, 0),
						new Color(Display.findDisplay(Thread.currentThread()),
								(int) (255 - value * 255), (int) (255 - value * 255), 255)));
			}
			if (map.getGraphicalValue().equals("BlueToRed")) {
				tp.addStyleRange(new StyleRange(l.getLineStart(), l.getLenght(),
						new Color(Display.findDisplay(Thread.currentThread()), 0, 0, 0),
						new Color(Display.findDisplay(Thread.currentThread()),
								(int) (value * 100) + 155, 155, (int) (100 - value * 100) + 155)));
			}
			if (map.getGraphicalValue().equals("ColorGradation")) {
				Color c;
				if (value < 0.25) {
					c = new Color(Display.findDisplay(Thread.currentThread()), 0, (int) value * 255, 255);
				} else if (value < 0.50) {
					c = new Color(Display.findDisplay(Thread.currentThread()),0, 255, (int) (255 - (value - 0.25) * 4 * 255));
				} else if (value < 0.75) {
					c = new Color(Display.findDisplay(Thread.currentThread()),(int) ((value - 0.50) * 4 * 255), 255, 0);
				} else {
					c = new Color(Display.findDisplay(Thread.currentThread()),255, (int) (255 - (value - 0.75) * 4 * 255), 0);
				}
				Color c2 = new Color(Display.findDisplay(Thread.currentThread()),Math.max(0, c.getRed() - 150), Math.max(0, c.getGreen() - 150),
						Math.max(0, c.getBlue() - 150));
				c = new Color(Display.findDisplay(Thread.currentThread()),c2.getRed() + 150, c2.getGreen() + 150, c2.getBlue() + 150);
				tp.addStyleRange(new StyleRange(l.getLineStart(), l.getLenght(),
						new Color(Display.findDisplay(Thread.currentThread()), 0, 0, 0),
						new Color(Display.findDisplay(Thread.currentThread()), c.getRed(),
								c.getGreen(), c.getBlue())));
			}
		}
		if (!tp.isEmpty())
			isv.changeTextPresentation(tp, true);
		tp.clear();

	}

}
