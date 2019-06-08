package verso.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import verso.activator.Activator;

public class TypeFilterDialog extends FilteredItemsSelectionDialog{

	 private static final String DIALOG_SETTINGS = "FilteredResourcesSelectionDialogExampleSettings";
	List<String> typeNames = new ArrayList<String>();
	IJavaProject scope = null;
	public TypeFilterDialog(Shell shell, IJavaProject ijp) {
		super(shell);
		this.scope = ijp;
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		return null;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new ItemsFilter() {
	         public boolean matchItem(Object item) {
	            return matches(item.toString());
	         }
	         public boolean isConsistentItem(Object item) {
	            return true;
	         }
	      };

	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider,
			ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
			throws CoreException {
		/*
		SearchEngine engine = new SearchEngine();
		engine.searchAllTypeNames(null, null, this.scope, new NameAcceptor(), IJavaSearchConstants.FORCE_IMMEDIATE_SEARCH , null);
		System.out.println("search finished");
		*/
		for (IPackageFragment ipf : scope.getPackageFragments())
		{
			if (ipf.getKind() == IPackageFragmentRoot.K_SOURCE)
			{
				for (ICompilationUnit icu : ipf.getCompilationUnits())
				{
					for (IType type : icu.getAllTypes())
					{
						contentProvider.add(type.getElementName(), itemsFilter);
					}
				}
			}
		}
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings()
		.getSection(DIALOG_SETTINGS);
		if (settings == null) {
			settings = Activator.getDefault().getDialogSettings()
			.addNewSection(DIALOG_SETTINGS);
			}
		return settings;

	}

	@Override
	public String getElementName(Object item) {
		return item.toString();

	}

	@Override
	protected Comparator getItemsComparator() {
		 return new Comparator() {
	         public int compare(Object arg0, Object arg1) {
	            return arg0.toString().compareTo(arg1.toString());
	         }
	      };

	}

	@Override
	protected IStatus validateItem(Object item) {
		// TODO Auto-generated method stub
		return Status.OK_STATUS;

	}
	/*
	private class NameAcceptor extends TypeNameRequestor
	{
		public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path)
		{
			if (enclosingTypeNames.length > 0)
				return;
			typeNames.add("" + new String(packageName) + "." + new String(simpleTypeName));
		}
	}
	*/
}
