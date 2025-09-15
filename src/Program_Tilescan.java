import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Program_Tilescan extends Program {
	Robomick robomick;
	
	public Program_Tilescan (Robomick r) {
		robomick = r;
	}
	
	public void run () throws Exception {
		/*
			+,-  0,-  -,-
			+,0  0,0  -,0
			+,+  0,+  -,+
			
			8 7 6
			1 0 5
			2 3 4
			
			optimal step values:
			60x, sweep1, 25% overlap: step = 0.08
			60x, sweep2, 25% overlap: step = 0.04
		*/
		double step = 0.08;
		
		robomick.rcm ().do_cmd ("SET_SWEEP 1");
		robomick.rcm ().do_cmd ("SET_CHURCHWINDOW 488");
		robomick.rcm ().do_cmd ("SET_CHSELECT 0 1 0 0");
		robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 3 0 0");
		robomick.rcm ().do_cmd ("ZEROXY");
		
		String reply;
		
		robomick.rcm ().do_cmd ("MOVEXY 0.0 0.0");
		String remote0 = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
		String localname0 = robomick.datapath + "0.tif";
		File f0 = robomick.rcm ().do_get_file (remote0, localname0);
		
		robomick.rcm ().do_cmd ("MOVEXY " + step + " 0.0");
		String remote1 = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
		String localname1 = robomick.datapath + "1.tif";
		File f1 = robomick.rcm ().do_get_file (remote1, localname1);
		
		robomick.rcm ().do_cmd ("MOVEXY " + step + " " + step + "");
		String remote2 = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
		String localname2 = robomick.datapath + "2.tif";
		File f2 = robomick.rcm ().do_get_file (remote2, localname2);
		
		robomick.rcm ().do_cmd ("MOVEXY 0.0 " + step + "");
		String remote3 = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
		String localname3 = robomick.datapath + "3.tif";
		File f3 = robomick.rcm ().do_get_file (remote3, localname3);
		
		robomick.rcm ().do_cmd ("MOVEXY -" + step + " " + step + "");
		String remote4 = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
		String localname4 = robomick.datapath + "4.tif";
		File f4 = robomick.rcm ().do_get_file (remote4, localname4);
		
		robomick.rcm ().do_cmd ("MOVEXY -" + step + " 0.0");
		String remote5 = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
		String localname5 = robomick.datapath + "5.tif";
		File f5 = robomick.rcm ().do_get_file (remote5, localname5);
		
		robomick.rcm ().do_cmd ("MOVEXY -" + step + " -" + step + "");
		String remote6 = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
		String localname6 = robomick.datapath + "6.tif";
		File f6 = robomick.rcm ().do_get_file (remote6, localname6);
		
		robomick.rcm ().do_cmd ("MOVEXY 0.0 -" + step + "");
		String remote7 = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
		String localname7 = robomick.datapath + "7.tif";
		File f7 = robomick.rcm ().do_get_file (remote7, localname7);
		
		robomick.rcm ().do_cmd ("MOVEXY " + step + " -" + step + "");
		String remote8 = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
		String localname8 = robomick.datapath + "8.tif";
		File f8 = robomick.rcm ().do_get_file (remote8, localname8);
		
		robomick.rcm ().do_cmd ("MOVEXY 0.0 0.0");
	}
}


