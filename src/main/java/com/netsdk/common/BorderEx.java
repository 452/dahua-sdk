package main.java.com.netsdk.common;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

/*
 * 边框设置
 */
public class BorderEx {
	public static void set(JComponent object, String title, int width) {
		Border innerBorder = BorderFactory.createTitledBorder(title);
	    Border outerBorder = BorderFactory.createEmptyBorder(width, width, width, width);
	    object.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));	 
	}

}
