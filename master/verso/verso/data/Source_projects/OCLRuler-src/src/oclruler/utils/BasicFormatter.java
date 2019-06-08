package oclruler.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class BasicFormatter extends Formatter {
	
	@Override
	public String format(LogRecord record) {
		Date d = new Date(record.getMillis());
		SimpleDateFormat f3 = new SimpleDateFormat("H:m:s", Locale.CANADA_FRENCH);
		String dStr = f3.format(d);
		String sourceClassName = record.getSourceClassName();
		if (sourceClassName.contains("."))
			sourceClassName = sourceClassName.substring(sourceClassName.lastIndexOf(".") + 1);

		String msg = "[" + dStr;

		String lvl = "";
		String key = record.getLevel().getName();
		switch (key) {
		case "SEVERE":
			lvl = " !!! ";
			msg = "[" + dStr + "]" + lvl;
			break;
		case "WARNING":
			lvl = " (!) ";
			msg = "[" + dStr + "]" + lvl;
			break;
		case "INFO":
			msg = "[" + dStr + "]";
			break;
		case "CONFIG":
			lvl = "c";
			msg = "[" + dStr + "-" + lvl + "]";
			break;
		case "FINE":
			lvl = "F";
			msg = "[" + dStr + "-" + lvl + "]";
			break;
		case "FINER":
			lvl = "F1";
			msg = "[" + dStr + "-" + lvl + "]";
			break;
		case "FINEST":
			lvl = "F2";
			msg = "[" + dStr + "-" + lvl + "]";
			break;

		default:
			break;
		}

		String location = null;
		if (sourceClassName.equals("Evolutioner"))
			location = "Evolution";
		else
			location = sourceClassName + "." + record.getSourceMethodName();

		return msg + location + ": " + record.getMessage() + "\n";
	}
	
}
