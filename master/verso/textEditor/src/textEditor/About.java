package textEditor;

/**
 * @name        GEODES Text Editor
 * @package     textEditor
 * @file        About.java
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
 *
 * @Modifiedby SidaDan
 * @modemail Fschultz@sinf.de
 * Bug fixed. If JTextArea txt not empty and the user will
 * shutdown the Simple Java NotePad, then the Simple Java NotePad
 * is only hidden (still running). So I added a WindowListener
 * an call method dispose() for this frame.
 * Tested with java 8.
 * 
 * @Modifiedby Maxime Gallais-Jimenez
 * @modemail mgallaisj@gmail.com
 * Adaptation for the GEODES text Editor
 */

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class About {

    private final JFrame frame;
    private final JPanel panel;
    private String contentText;
    private final JLabel text;
    
    private static final long serialVersionUID = 1L;
    public final static String AUTHOR_EMAIL = "hi@ph7.me";
    public final static String NAME = "GEODES Text Editor";
    public final static String OLDNAME = "PHNotePad";
    public final static String EDITOR_EMAIL = "contact@achinthagunasekara.com";
    public final static double VERSION = 3.0;

    public About(TextEditor ui) {
        panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        frame = new JFrame();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
            }
        });


        frame.setVisible(true);
        frame.setSize(500,300);
        frame.setLocationRelativeTo(ui);
        text = new JLabel();
    }

    public void me() {
        frame.setTitle("About Me - " + NAME);

        contentText =
        "<html><body><p>" +
        "Author: Pierre-Henry Soria<br />" +
        "Contact me at: " +
        "<a href='mailto:" + AUTHOR_EMAIL + "?subject=About the NotePad PH Software'>" + AUTHOR_EMAIL + "</a>" +
        "<br /><br />" +
        "Modified By: Achintha Gunasekara<br />" +
        "Contact me at: <a href='mailto:" + EDITOR_EMAIL + "?subject=About the NotePad PH Software'>" + EDITOR_EMAIL + "</a>" +
        "<br /><br />" +
        "Modified and changed to become GEODES Text Editor By: Maxime Gallais-Jimenez<br />" +
        "Modified and added to Verso By: Maxime Gallais-Jimenez<br />" +
        "Contact me at: <a href='mailto:mgallaisj@gmail.com?subject=About the GEODES Text Editor Software in Verso'>mgallaisj@gmail.com</a>" +
        "</p></body></html>";

        text.setText(contentText);
        panel.add(text);
        frame.add(panel);
    }

    public void software() {
        frame.setTitle("About Software - " + NAME);

        contentText =
        "<html><body><p>" +
        "Name: " + NAME + "<br />" +
        "Version: " + VERSION +
        "</p></body></html>";

        text.setText(contentText);
        panel.add(text);
        frame.add(panel);
    }

}
