package DeepLearning;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

	public static String getStackTrace(Throwable throwable)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		return sw.toString(); // stack trace as a string
	}
}
