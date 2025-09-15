import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Robomick {
	public static String datapath;
	
	public Robomick () {
		String nowdate = new SimpleDateFormat ("yyyyMMdd").format (new Date ());
		String nowtime = new SimpleDateFormat ("kkmmss").format (new Date ());
		datapath = "~/robomic/" + nowdate + "/" + nowtime + "/";
		
		File datadir = new File (datapath);
		System.out.println ("[datapath] " + datapath);
		if (!datadir.exists ()) {
			datadir.mkdirs ();
		}
		
		// write pid to file for kill-robomick
		long pid = ProcessHandle.current ().pid ();
		try {
			PrintWriter pidfile = new PrintWriter ("robomick.pid");
			pidfile.println (pid);
			pidfile.close ();
		}
		catch (FileNotFoundException x) {
			x.printStackTrace ();
		}
	}
	
	public RCM rcm () {
		return rcm;
	}
	
	
	// todo: move to RCM
	private void do_findfocus () throws Exception {
		double focusz = Double.parseDouble (rcm.do_cmd ("FIND_FOCUS"));
		System.out.println (focusz);
	}
	
	// todo: move to RCM
	private void do_QUIT_SERVER () throws Exception {
		String reply = rcm.do_cmd ("QUIT_SERVER");
		System.out.println ("reply = " + reply);
        }
        
	public void run () throws Exception {
		//Program p = new Program_Zstack (this);
		//Program p = new Program_Smaitest (this);
		//Program p = new Program_Tilescan (this);
		//Program p = new Program_Optogen (this);
		//Program p = new Program_Halffrap (this);
		//Program p = new Program_MultiwellFrapGeneric (this);
		//Program p = new Program_MultiwellFrapParallel (this);
		//Program p = new Program_TestAlignment (this);
		Program p = new Program_MultiwellFrapOptogen (this);
//		Program p = new Program_Test (this);
		
		try {
			PrintWriter pidfile = new PrintWriter (datapath + "program.txt");
			pidfile.println (p.getClass ().getName ());
			pidfile.close ();
		}
		catch (FileNotFoundException x) {
			x.printStackTrace ();
		}
		
		p.run ();
		
		// replace by: rcm.do_findfocus (); and rcm.do_QUIT_SERVER ();
		//do_findfocus ();
//		do_QUIT_SERVER ();
		
	}
	
	public static void main (String [] args) throws Exception {
		new Robomick ().run ();
	}
	
	private RCM rcm = new RCM ();
}


