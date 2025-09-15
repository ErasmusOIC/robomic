import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Program_Halffrap extends Program {
	Robomick robomick;
	
	public Program_Halffrap (Robomick r) {
		robomick = r;
	}
	public void run () throws Exception {
		robomick.rcm ().do_cmd ("SET_SWEEP 1");
		robomick.rcm ().do_cmd ("SET_CHURCHWINDOW 488");
		robomick.rcm ().do_cmd ("SET_CHSELECT 0 1 0 0");
		robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 8 0 0");
        	
		String pre = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 768 768");
		System.out.println ("pre = " + pre);
		File f1 = robomick.rcm ().do_get_file (pre, robomick.datapath + "pre.tif");
		
		robomick.rcm ().do_cmd ("SET_CHSELECT 1 1 1 1");
		robomick.rcm ().do_cmd ("SET_CHINTENSITY 100 100 100 100");
		
		
		for (int i = 0; i < 4; i++) {
			String bl = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 -192 768 384");
			System.out.println ("bl = " + bl);
			File f2 = robomick.rcm ().do_get_file (bl, robomick.datapath + "bl.tif");
		}
		
		
		
		robomick.rcm ().do_cmd ("SET_CHSELECT 0 1 0 0");
		robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 8 0 0");
        	
		String post = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 768 768");
		System.out.println ("post = " + post);
		File f3 = robomick.rcm ().do_get_file (post, robomick.datapath + "post.tif");
	}
}


