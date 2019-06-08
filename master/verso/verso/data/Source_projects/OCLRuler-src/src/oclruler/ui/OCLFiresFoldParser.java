package oclruler.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldType;

public class OCLFiresFoldParser implements FoldParser {

	
//	private static final char[] MARKUP_CLOSING_TAG_START = { '<', '/' };
//	private static final char[] MARKUP_SHORT_TAG_END = { '/', '>' };
//	private static final char[] MLC_END = { '-', '-', '>' };
//
//	private static final char[] CONTEXT_END = { ':' };
//	private static final String END_CONSTRAINT = "--fin";
//	private static final String START_CONSTRAINT = "--Cst";
	
	/**
	 * Unstable ! <br/>
	 * Implementation on work. 
	 */
	@SuppressWarnings("unused")
	@Override
	public List<Fold> getFolds(RSyntaxTextArea textArea) {
		String[] lines = textArea.getText().split("\n");
		
		List<Fold> folds = new ArrayList<Fold>();
		Fold currentFold = null;
		int lineCount = textArea.getLineCount();
		boolean inFire = false;
		int fireStart = 0;
		for (int line = 0; line < lineCount; line++) {
			Token t = textArea.getTokenListForLine(line);
			try {
				if(t.startsWith("----#".toCharArray())){
					inFire = true;
					fireStart = t.getOffset();
				} else if(t.startsWith("----fin".toCharArray())){
					inFire = false;
					int fireEnd = t.getEndOffset();
					try {
						currentFold = new Fold(FoldType.COMMENT, textArea, fireStart);
						currentFold.setEndOffset(fireEnd);
						folds.add(currentFold);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					currentFold = null;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}

//		for (int line = 0; line < lineCount; line++) {
////			if (line < lines.length)
////				System.out.println(lines[line]);
//			try {
//				Token t = textArea.getTokenListForLine(line);
//				if (t != null) {
//					if (!inFire) {
//						t = t.getNextToken();
//
//						if (t != null && t.isSingleChar(Token.SEPARATOR, ':')) {
//							// START
//							System.out.println("Start. (getFolds) : " + t + ": " + t.getTextOffset());
//							fireStart = t.getOffset();
//							inFire = true;
//						}
//					} else { // inFire
//						t = t.getNextToken();
//
//						if (t != null && t.isSingleChar(Token.IDENTIFIER, '-')) {
//							// Keep going
//							int fireEnd = t.getEndOffset();
//							if (currentFold == null) {
//								currentFold = new Fold(FoldType.COMMENT, textArea, fireStart);
//								currentFold.setEndOffset(fireEnd);
//								folds.add(currentFold);
//								currentFold = null;
//							} else {
//								currentFold = currentFold.createChild(FoldType.COMMENT, fireStart);
//								currentFold.setEndOffset(fireEnd);
//								currentFold = currentFold.getParent();
//							}
//
//						} else {
//							// FIN
//							inFire = false;
//							System.out.println("FIN. (getFolds) : " + t + ": " + t.getTextOffset());
//						}
//
//					}
//
//				}
//			} catch (BadLocationException ble) {
//				ble.printStackTrace();
//			}
//		}
		
		
		
		
		
//		for (int line = 0; line < lineCount; line++) {
//			
//			try {
//				Token t = textArea.getTokenListForLine(line);
//				if (line < lines.length)
//				System.out.println(line+":"+lines[line] +" : "+t.getEndOffset());
//				
//				
//				while (t!=null && t.isPaintable()) {
//				
//					if (t != null && t.getLexeme() != null) {
//						if (!inCst) {
//							if (t != null && t.length() > START_CONSTRAINT.length() && t.startsWith(START_CONSTRAINT.toCharArray())) {
//								// START
//								System.out.println("Start. (getFolds) : " + t + ": " + t.getEndOffset());
//								cstStart = t.getOffset();
//								inCst = true;
//							}
//						} else { // inFire
//
//							if (t != null && t.length() > END_CONSTRAINT.length() && t.startsWith(END_CONSTRAINT.toCharArray())) {
//								// FIN
//								inCst = false;
//								int cstEnd = t.getEndOffset();
//								if (currentFold == null) {
//									currentFold = new Fold(FoldType.CODE, textArea, cstStart);
//									currentFold.setEndOffset(cstEnd);
//									folds.add(currentFold);
//									currentFold = null;
//								} else {
//									currentFold = currentFold.createChild(FoldType.CODE, cstStart);
//									currentFold.setEndOffset(cstEnd);
//									currentFold = currentFold.getParent();
//								}
//								System.out.println("FIN. (getFolds) : " + t + ": " + t.getEndOffset());
//							} else {
//								// Keep going
//								int cstEnd = t.getOffset();
//								if (currentFold == null) {
//									currentFold = new Fold(FoldType.CODE, textArea, cstStart);
//									currentFold.setEndOffset(cstEnd);
//									folds.add(currentFold);
//								} else {
//									currentFold = currentFold.createChild(FoldType.CODE, cstStart);
//								}
//							}
//						}
//					}
//					t = t.getNextToken();
//
//				}
//
//				// if(t != null){
//				// System.out.println("t:\""+t+"\"");
//				// if(t.endsWith(CONTEXT_END))
//				// System.out.println("Bingo");
//				// }
//			} catch (BadLocationException ble) {
//				ble.printStackTrace();
//			}
//		} 
		return folds;
			
		}

}
