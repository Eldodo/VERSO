package utils;

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
		
		String sourceClassName = record.getSourceClassName();
		if(sourceClassName.contains("."))
			sourceClassName = sourceClassName.substring(sourceClassName.lastIndexOf(".")+1);
		return "["+f3.format(d)+"]"+record.getLevel() + ": "
	            + sourceClassName + "."
	            + record.getSourceMethodName() + ": "
	            + record.getMessage() + "\n";
	}
	
}
