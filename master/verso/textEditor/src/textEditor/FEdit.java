package textEditor;
/**
 * @name        Simple Java NotePad
 * @package     ph.notepad
 * @file        FEdit.java
 * @author      SORIA Pierre-Henry
 * @email       pierrehs@hotmail.com
 * @link        http://github.com/pH-7
 * @copyright   Copyright Pierre-Henry SORIA, All Rights Reserved.
 * @license     Apache (http://www.apache.org/licenses/LICENSE-2.0)
 * @create      2012-05-04
 * @update      2015-09-4
 *
 *
 * @modifiedby  Achintha Gunasekara
 * @modweb      http://www.achinthagunasekara.com
 * @modemail    contact@achinthagunasekara.com
 */

import javax.swing.text.JTextComponent;

public class FEdit {

    public static void clear(JTextComponent comp) {
        comp.setText("");
    }

}