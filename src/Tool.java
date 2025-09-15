import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Tool {
	private Tool () {
	}
	
	public static String rndString (String pool, int length) {
		Random rnd = new Random ();
		StringBuilder salt = new StringBuilder ();
		while (salt.length () < length) {
			int index = rnd.nextInt (pool.length ());
			salt.append (pool.charAt (index));
		}
		return salt.toString ();
	}
	
	public static void log (long timestamp, String category, String message) {
		String logfilename = Robomick.datapath + "general.log";
		
		if (timestamp == -1) {
			timestamp = System.currentTimeMillis ();
		}
		
		Date date = new Date (timestamp);
		SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
		
		System.out.println (sdf.format (date) + "\t" + "[" + category + "]" + "\t" + message);
		
		String msg_no_ansi = message.replaceAll ("\u001B\\[[;\\d]*m", "");
		
		try {
			BufferedWriter bw = new BufferedWriter (new FileWriter (logfilename, true));
			bw.write (sdf.format (date) + "\t" + "[" + category + "]" + "\t" + msg_no_ansi + "\n");
			bw.flush ();
			bw.close ();
		}
		catch (IOException x) {
			x.printStackTrace ();
		}
	}
	
	public static String format_duration (long dur) {
		System.out.println ("dur = " + dur);
		
		long rst = dur;
		
		long d = rst / (60 * 60 * 24);
		rst = rst % (60 * 60 * 24);
		
		long h = rst / (60 * 60);
		rst = rst % (60 * 60);
		
		long m = rst / (60);
		rst = rst % (60);
		
		long s = rst;
		
		
		return d + "d " + h + "h " + m + "m " + s + "s";
	}
	
}

