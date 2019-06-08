package textEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import textEditor.StyledDocument;
import textEditor.JavaEditor.JavaStyledDocument;

/**
 * @author Maxime Gallais-Jimenez
 *
 * Class which represents the document to edit
 * 
 */
@SuppressWarnings("serial")
public class TextEditor extends JFrame {

	protected EditorPane editorPane;
	protected StyledDocument document;
	protected File file;

	protected JMenuBar menuBar;
	protected JComboBox<Integer> fontSize;
	protected JComboBox<String> fontType;
	protected JMenu menuFile, menuEdit, menuFind, menuAbout;
	protected JMenuItem newFile, openFile, saveFile, saveAs, close, cut, copy, paste, clearFile, selectAll, quickFind, aboutMe, aboutSoftware, wordWrap, undo, redo;
	protected JToolBar mainToolbar;
	JButton newButton, openButton, saveButton, clearButton, quickButton, aboutMeButton, aboutButton, closeButton, boldButton, italicButton;
	protected Action selectAllAction;

	protected final String iconFolder = "textEditor"+File.separator+"icons"+File.separator;
	// setup icons - Bold and Italic
	protected final ImageIcon boldIcon = new ImageIcon(iconFolder+"bold.png");
	protected final ImageIcon italicIcon = new ImageIcon(iconFolder+"italic.png");

	// setup icons - File Menu
	protected final ImageIcon newIcon = new ImageIcon(iconFolder+"new.png");
	protected final ImageIcon openIcon = new ImageIcon(iconFolder+"open.png");
	protected final ImageIcon saveIcon = new ImageIcon(iconFolder+"save.png");
	protected final ImageIcon saveAsIcon = new ImageIcon(iconFolder+"saveas.png");//Icon made by Freepik from www.flaticon.com
	protected final ImageIcon closeIcon = new ImageIcon(iconFolder+"close.png");

	// setup icons - Edit Menu
	protected final ImageIcon undoIcon = new ImageIcon(iconFolder+"undo.png");//Icon made by Google from www.flaticon.com 
	protected final ImageIcon redoIcon = new ImageIcon(iconFolder+"redo.png");//Icon made by SimpleIcon from www.flaticon.com 
	protected final ImageIcon clearIcon = new ImageIcon(iconFolder+"clear.png");
	protected final ImageIcon cutIcon = new ImageIcon(iconFolder+"cut.png");
	protected final ImageIcon copyIcon = new ImageIcon(iconFolder+"copy.png");
	protected final ImageIcon pasteIcon = new ImageIcon(iconFolder+"paste.png");
	protected final ImageIcon selectAllIcon = new ImageIcon(iconFolder+"selectall.png");
	protected final ImageIcon wordwrapIcon = new ImageIcon(iconFolder+"wordwrap.png");

	// setup icons - Search Menu
	protected final ImageIcon searchIcon = new ImageIcon(iconFolder+"search.png");

	// setup icons - Help Menu
	protected final ImageIcon aboutMeIcon = new ImageIcon(iconFolder+"about_me.png");
	protected final ImageIcon aboutIcon = new ImageIcon(iconFolder+"about.png");

	protected boolean edit = false;
	
	protected UndoAction undoAction;
	protected RedoAction redoAction;


	public TextEditor() {
		document = new StyledDocument();
		file = null;
		init();
	}

	public TextEditor(String path) {
		document = new StyledDocument();
		file = null;
		init();
		setFile(path);
	}

	public TextEditor(File file) {
		document = new StyledDocument();
		file = null;
		init();
		setFile(file);
	}

	/**
	 * Open a window with an empty document
	 */
	protected void init() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE); //set the default close operation to not close verso for example
		setSize(800,1000);
		setLocationRelativeTo(null); //center the frame on the monitor
		setTitle("Untitled");

		// Set editorPane
		editorPane = new EditorPane(document);
		editorPane.setBackground(Color.white);
		editorPane.setEditable(true);
		editorPane.setFont(new Font("Century Gothic", Font.PLAIN, 12));
		editorPane.addKeyListener(new KeyListener() {			
			@Override
			public void keyTyped(KeyEvent e) {
				edit = true;
			}
			@Override
			public void keyReleased(KeyEvent e) {
				// Operation not supported		
			}
			@Override
			public void keyPressed(KeyEvent e) {
				// Operation not supported			
			}
		});

		// Set Undo/Redo listener on the document
		document.addUndoableEditListener(new EditorPaneUndoableEditListener());
		
		// This is why we didn't have to worry about the size of the editorPane!
		getContentPane().setLayout(new BorderLayout()); // the BorderLayout bit makes it fill it automatically
		getContentPane().add(editorPane);

		// Set the Menus
		menuFile = new JMenu("File");
		menuEdit = new JMenu("Edit");
		menuFind = new JMenu("Search");
		menuAbout = new JMenu("About");

		// Set the Items Menu
		newFile = new JMenuItem("New", newIcon);
		openFile = new JMenuItem("Open", openIcon);
		saveFile = new JMenuItem("Save", saveIcon);
		saveAs = new JMenuItem("Save as", saveAsIcon);
		close = new JMenuItem("Quit", closeIcon);
		clearFile = new JMenuItem("Clear", clearIcon);
		quickFind = new JMenuItem("Quick", searchIcon);
		aboutMe = new JMenuItem("About Me", aboutMeIcon);
		aboutSoftware = new JMenuItem("About Software", aboutIcon);

		menuBar = new JMenuBar();
		menuBar.add(menuFile);
		menuBar.add(menuEdit);
		menuBar.add(menuFind);

		menuBar.add(menuAbout);

		this.setJMenuBar(menuBar);

		// Set Actions:
		selectAllAction = new SelectAllAction("Select All", clearIcon, "Select all text", new Integer(KeyEvent.VK_A), editorPane);

		// New File
		newFile.addActionListener(new NewFileListener(this)); // Adding an action listener (so we know when it's been clicked).
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK)); // Set a keyboard shortcut
		menuFile.add(newFile); // Adding the file menu

		// Open File
		openFile.addActionListener(new OpenFileListener(this));
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		menuFile.add(openFile);

		// Save File
		saveFile.addActionListener(new SaveFileListener());
		saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		menuFile.add(saveFile);

		//Save As
		saveAs.addActionListener(new SaveNewFileListener());
		saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		menuFile.add(saveAs);

		// Close File
		/*
		 * Along with our "CTRL+Q" shortcut to close the window, we also have the
		 * default closer, as stated at the beginning of this tutorial. this means that
		 * we actually have TWO shortcuts to close: 1) the default close operation
		 * (example, Alt+F4 on Windows) 2) CTRL+Q, which we are about to define now:
		 * (this one will appear in the label).
		 */
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		close.addActionListener(new CloseListener(this));
		menuFile.add(close);

		//Undo
		undoAction = new UndoAction();
		undo = new JMenuItem(undoAction);
		undo.setText("Undo");
		undo.setIcon(undoIcon);
		undo.setToolTipText("Undo");
		undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		menuEdit.add(undo);
		
		//Redo
		redoAction = new RedoAction();
		redo = new JMenuItem(redoAction);
		redo.setText("Redo");
		redo.setIcon(redoIcon);
		redo.setToolTipText("Redo");
		redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		menuEdit.add(redo);
		
		// Select All Text
		selectAll = new JMenuItem(selectAllAction);
		selectAll.setText("Select All");
		selectAll.setIcon(selectAllIcon);
		selectAll.setToolTipText("Select All");
		selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		menuEdit.add(selectAll);

		// Clear File (Code)
		clearFile.addActionListener(new ClearFileListener(this));
		clearFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
		menuEdit.add(clearFile);

		// Cut Text
		cut = new JMenuItem(new DefaultEditorKit.CutAction());
		cut.setText("Cut");
		cut.setIcon(cutIcon);
		cut.setToolTipText("Cut");
		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		menuEdit.add(cut);

		// WordWrap
		wordWrap = new JMenuItem();
		wordWrap.setText("Word Wrap");
		wordWrap.setIcon(wordwrapIcon);
		wordWrap.setToolTipText("Word Wrap");

		// Short cut key or key stroke
		wordWrap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
		menuEdit.add(wordWrap);


		// Copy Text
		copy = new JMenuItem(new DefaultEditorKit.CopyAction());
		copy.setText("Copy");
		copy.setIcon(copyIcon);
		copy.setToolTipText("Copy");
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		menuEdit.add(copy);

		// Paste Text
		paste = new JMenuItem(new DefaultEditorKit.PasteAction());
		paste.setText("Paste");
		paste.setIcon(pasteIcon);
		paste.setToolTipText("Paste");
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		menuEdit.add(paste);

		// Find Word
		quickFind.addActionListener(new FindListener());
		quickFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		menuFind.add(quickFind);

		// About Me
		aboutMe.addActionListener(new AboutListener(this));
		aboutMe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		menuAbout.add(aboutMe);

		// About Software
		aboutSoftware.addActionListener(new AboutListener(this));
		aboutSoftware.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		menuAbout.add(aboutSoftware);

		mainToolbar = new JToolBar();
		this.add(mainToolbar, BorderLayout.NORTH);
		// used to create space between button groups
		newButton = new JButton(newIcon);
		newButton.setToolTipText("New");
		newButton.addActionListener(new NewFileListener(this));
		mainToolbar.add(newButton);
		mainToolbar.addSeparator();

		openButton = new JButton(openIcon);
		openButton.setToolTipText("Open");
		openButton.addActionListener(new OpenFileListener(this));
		mainToolbar.add(openButton);
		mainToolbar.addSeparator();

		saveButton = new JButton(saveIcon);
		saveButton.setToolTipText("Save");
		saveButton.addActionListener(new SaveFileListener());
		mainToolbar.add(saveButton);
		mainToolbar.addSeparator();

		clearButton = new JButton(clearIcon);
		clearButton.setToolTipText("Clear All");
		clearButton.addActionListener(new ClearFileListener(this));
		mainToolbar.add(clearButton);
		mainToolbar.addSeparator();

		quickButton = new JButton(searchIcon);
		quickButton.setToolTipText("Quick Search");
		quickButton.addActionListener(new FindListener());
		mainToolbar.add(quickButton);
		mainToolbar.addSeparator();

		aboutMeButton = new JButton(aboutMeIcon);
		aboutMeButton.setToolTipText("About Me");
		aboutMeButton.addActionListener(new AboutListener(this));
		mainToolbar.add(aboutMeButton);
		mainToolbar.addSeparator();

		aboutButton = new JButton(aboutIcon);
		aboutButton.setToolTipText("About GEODES Text Editor");
		aboutButton.addActionListener(new AboutListener(this));
		mainToolbar.add(aboutButton);
		mainToolbar.addSeparator();

		closeButton = new JButton(closeIcon);
		closeButton.setToolTipText("Quit");
		closeButton.addActionListener(new CloseListener(this));
		mainToolbar.add(closeButton);
		mainToolbar.addSeparator();

		boldButton = new JButton(boldIcon);
		boldButton.setToolTipText("Bold");
		boldButton.addActionListener(new BoldListener());
		mainToolbar.add(boldButton);
		mainToolbar.addSeparator();

		italicButton = new JButton(italicIcon);
		italicButton.setToolTipText("Italic");
		italicButton.addActionListener(new ItalicListener());
		mainToolbar.add(italicButton);
		mainToolbar.addSeparator();

		/**
		 * **************** FONT SETTINGS SECTION **********************
		 */
		// FONT FAMILY SETTINGS SECTION START
		fontType = new JComboBox<String>();

		// GETTING ALL AVAILABLE FONT FOMILY NAMES
		String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

		for (int i = 0; i < fonts.length; i++) {
			// Adding font family names to font[] array
			fontType.addItem(fonts[i]);
		}
		// Setting maximize size of the fontType ComboBox
		fontType.setMaximumSize(new Dimension(170, 30));
		fontType.setToolTipText("Font Type");
		fontType.setSelectedItem(editorPane.getFont().getFontName());
		mainToolbar.add(fontType);
		mainToolbar.addSeparator();

		// Adding Action Listener on fontType JComboBox
		fontType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				// Getting the selected fontType value from ComboBox
				String p = fontType.getSelectedItem().toString();
				// Getting size of the current font or text
				int s = editorPane.getFont().getSize();
				editorPane.setFont(new Font(p, Font.PLAIN, s));
			}
		});

		// FONT FAMILY SETTINGS SECTION END
		// FONT SIZE SETTINGS START
		fontSize = new JComboBox<Integer>();

		for (int i = 5; i <= 100; i++) {
			fontSize.addItem(i);
		}
		fontSize.setMaximumSize(new Dimension(70, 30));
		fontSize.setToolTipText("Font Size");
		fontSize.setSelectedIndex(editorPane.getFont().getSize()-5);
		mainToolbar.add(fontSize);

		fontSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				String sizeValue = fontSize.getSelectedItem().toString();
				int sizeOfFont = Integer.parseInt(sizeValue);
				String fontFamily = editorPane.getFont().getFamily();

				Font font1 = new Font(fontFamily, Font.PLAIN, sizeOfFont);
				editorPane.setFont(font1);
			}
		});
		// FONT SIZE SETTINGS SECTION END

		add(new JScrollPane(editorPane));
		setVisible(true);
	}

	/**
	 * Set the content of the window with the file content
	 * @param file File with the content to display
	 */
	public void setFile(File file) {
		if(!file.exists()) {
			System.err.println("Le fichier "+file.getAbsolutePath()+" n'existe pas.");
		}else if(!file.isFile()) {
			System.err.println("Le fichier "+file.getAbsolutePath()+" n'est pas un fichier.");
		}else {
			this.file = file;
			if(file.getName().endsWith(".java")) {
				document = new JavaStyledDocument();
				editorPane.setContentType("text/java");
				editorPane.setDocument(document);
			}else {
				document = new StyledDocument();
				editorPane.setContentType("text");
				editorPane.setDocument(document);
			}
			try {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line, content="";
				while((line = in.readLine()) != null) {
					content+=line+"\n";
				}
				in.close();
				editorPane.setText(content);
				setTitle(file.getAbsolutePath());
			}catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * Set the content of the window with the file content
	 * @param path Path of the file with the content to display
	 */
	public void setFile(String path) {
		setFile(new File(path));
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			if (edit) {
				Object[] options = { "Save and exit", "No Save and exit", "Return" };
				int n = JOptionPane.showOptionDialog(this, "Do you want to save the file ?", "Question",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				if (n == 0) {// save and exit
					saveFile();
					this.dispose();// dispose all resources and close the application
				} else if (n == 1) {// no save and exit
					this.dispose();// dispose all resources and close the application
				}
			} else {
				dispose();
			}
		}
	}

	/**
	 * Save a file.
	 * If it is a new it save it in a new file 
	 * Else it saves the file opened
	 */
	protected void saveFile() {
		if(file == null) {
			saveNewFile();
		}else {
			writeInFile(file);
		}
		edit = false;
	}

	/**
	 * Save the current content of the Editor in a new file
	 */
	protected void saveNewFile() {
		// Open a file chooser
		JFileChooser fileChoose = new JFileChooser();
		// Open the file, only this time we call
		int option = fileChoose.showSaveDialog(this);

		/*
		 * ShowSaveDialog instead of showOpenDialog if the user clicked OK (and not
		 * cancel)
		 */
		if (option == JFileChooser.APPROVE_OPTION) {
			try {
				File openFile = fileChoose.getSelectedFile();
				setTitle(openFile.getName());

				writeInFile(openFile);
			} catch (Exception ex) { // again, catch any exceptions and...
				// ...write to the debug console
				System.err.println(ex.getMessage());
			}
		}
	}

	/**
	 * Write the content of the editor in a file
	 * @param openFile File where the content is writte
	 */
	protected void writeInFile(File openFile) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(openFile.getPath()));
			out.write(editorPane.getText());
			out.close();

			//		enableAutoComplete(openFile);
			edit = false;
		}catch(IOException ioe) {
			System.err.println(ioe);
		}
	}
	
	/**
	 * Highlight keywords in the file
	 * @param keywords words to highlight
	 */
	public void highlight(HashSet<String> keywords) {
		new Highligter(keywords, editorPane);
	}

	/****************************************************************************************
	 * 							Listeners for all the buttons
	 ****************************************************************************************/
	
	/*
	 * Action to perform when the close button or shortcut is pressed
	 */
	class CloseListener implements ActionListener {
		TextEditor textEditor;

		public CloseListener(TextEditor te) {
			textEditor = te;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (edit) {
				Object[] options = { "Save and exit", "No Save and exit", "Return" };
				int n = JOptionPane.showOptionDialog(textEditor, "Do you want to save the file ?", "Question",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
				if (n == 0) {// save and exit
					saveFile();
					dispose();// dispose all resources and close the application
				} else if (n == 1) {// no save and exit
					dispose();// dispose all resources and close the application
				}
			} else {
				dispose();// dispose all resources and close the application
			}
		}			
	}

	/*
	 * Action to perform when the new File button or shortcut is pressed
	 */
	class NewFileListener implements ActionListener {
		TextEditor textEditor;

		public NewFileListener(TextEditor te) {
			textEditor = te;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (edit) {
				Object[] options = { "Save", "No Save", "Return" };
				int n = JOptionPane.showOptionDialog(textEditor, "Do you want to save the file at first ?", "Question",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
				if (n == 0) {// save
					saveFile();
					edit = false;
				} else if (n == 1) {
					edit = false;
					FEdit.clear(editorPane);
				}
			} else {
				FEdit.clear(editorPane);
			}
		}
	}

	/*
	 * Action to perform when the open file button or shortcut is pressed
	 */
	class OpenFileListener implements ActionListener {
		TextEditor textEditor;

		public OpenFileListener(TextEditor te) {
			textEditor = te;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser open = new JFileChooser(); // open up a file chooser (a dialog for the user to browse files to
			// open)
			int option = open.showOpenDialog(textEditor); // get the option that the user selected (approve or cancel)

			/*
			 * NOTE: because we are OPENing a file, we call showOpenDialog~ if the user
			 * clicked OK, we have "APPROVE_OPTION" so we want to open the file
			 */
			if (option == JFileChooser.APPROVE_OPTION) {
				FEdit.clear(editorPane); // clear the TextArea before applying the file contents
				try {
					File openFile = open.getSelectedFile();
					setTitle(openFile.getName());
					Scanner scan = new Scanner(new FileReader(openFile.getPath()));
					while (scan.hasNext()) {
						try {
							setFile(openFile);
							Document doc = editorPane.getDocument();
							doc.insertString(doc.getLength(), scan.nextLine() + "\n", null);
						} catch (BadLocationException ble) {
							ble.printStackTrace();
						}
					}
					scan.close();

					//enableAutoComplete(openFile);
				} catch (Exception ex) { // catch any exceptions, and...
					// ...write to the debug console
					System.err.println(ex.getMessage());
				}
			}

		}
	}
	
	/*
	 * Action to perform when the save button or shortcut is pressed
	 */
	class SaveFileListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			saveFile();
		}
	}

	/*
	 * Action to perform when the save as button or shortcut is pressed
	 */
	class SaveNewFileListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			saveNewFile();
		}
	}
	
	/*
	 * Action to perform when the bold button or shortcut is pressed
	 */
	class BoldListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (editorPane.getFont().getStyle() == Font.BOLD) {
				editorPane.setFont(editorPane.getFont().deriveFont(Font.PLAIN));
			} else {
				editorPane.setFont(editorPane.getFont().deriveFont(Font.BOLD));
			}
		}
	}
	
	/*
	 * Action to perform when the italic button or shortcut is pressed
	 */
	class ItalicListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (editorPane.getFont().getStyle() == Font.ITALIC) {
				editorPane.setFont(editorPane.getFont().deriveFont(Font.PLAIN));
			} else {
				editorPane.setFont(editorPane.getFont().deriveFont(Font.ITALIC));
			}
		}
	}
	
	/*
	 * Action to perform when the clear button or shortcut is pressed
	 */
	class ClearFileListener implements ActionListener {
		TextEditor textEditor;

		public ClearFileListener(TextEditor te) {
			textEditor = te;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object[] options = { "Yes", "No" };
			int n = JOptionPane.showOptionDialog(textEditor, "Are you sure to clear the text Area ?", "Question",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {// clear
				// FEdit.clear(textArea);
				FEdit.clear(editorPane);
			}
		}
	}
	
	/*
	 * Action to perform when the find button or shortcut is pressed
	 */
	class FindListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			new Find(editorPane);
		}
	}
	
	/*
	 * Action to perform when the about button or shortcut is pressed
	 */
	class AboutListener implements ActionListener {
		TextEditor textEditor;
		
		public AboutListener(TextEditor te) {
			textEditor = te;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == aboutMe || e.getSource() == aboutMeButton) {
				new About(textEditor).me();
			} // About Software
			else if (e.getSource() == aboutSoftware || e.getSource() == aboutButton) {
				new About(textEditor).software();
			}
		}
	}
	
	protected class EditorPaneUndoableEditListener implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			//Remember the edit and update the menus
			editorPane.addEdit(e.getEdit());
			undoAction.updateUndoState();
			redoAction.updateRedoState();
		}
	}
	
	class UndoAction extends AbstractAction {
		public UndoAction() {
            super("Undo");
            setEnabled(false);
        }
		
		public void actionPerformed(ActionEvent e) {
			try {
		        editorPane.undo();
		    } catch (CannotUndoException ex) {
		        System.out.println("Unable to undo: " + ex);
		        ex.printStackTrace();
		    }
		    updateUndoState();
		    redoAction.updateRedoState();
		}
		
		protected void updateUndoState() {
            if (editorPane.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, editorPane.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
	}
	
	class RedoAction extends AbstractAction {
		public RedoAction() {
            super("Redo");
            setEnabled(false);
        }
		
		public void actionPerformed(ActionEvent e) {
		    try {
		        editorPane.redo();
		    } catch (CannotRedoException ex) {
		        System.out.println("Unable to redo: " + ex);
		        ex.printStackTrace();
		    }
		    updateRedoState();
		    undoAction.updateUndoState();
		}
		
		protected void updateRedoState() {
            if (editorPane.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, editorPane.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
	}
	
	class SelectAllAction extends AbstractAction {

		/**
		 * Used for Select All function
		 */
		private static final long serialVersionUID = 1L;

		public SelectAllAction(String text, ImageIcon icon, String desc, Integer mnemonic,
				final JTextComponent editorPane) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			editorPane.selectAll();
		}
	}

}
