import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Program_Zstack extends Program {
	Robomick robomick;
	
	public Program_Zstack (Robomick r) {
		robomick = r;
	}
	
        public void run () throws Exception {
		System.out.println ("Robomick_Zstack :: run");
		//rcm.do_cmd ("FIND_FOCUS");
		robomick.rcm ().do_cmd ("SET_SWEEP 1");
		robomick.rcm ().do_cmd ("SET_CHURCHWINDOW 488");
		robomick.rcm ().do_cmd ("SET_CHSELECT 0 1 0 0");
		robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 4 0 0");
		robomick.rcm ().do_cmd ("SET_AF OFF");
        	
		String currz = robomick.rcm ().do_cmd ("GET_Z");
		double z0 = Double.parseDouble (currz);
        	
        	double dd = 0.25;  // step size
        	int n = 3;  // number of slices
        	
        	for (int i = 0; i < n; i++) {
        		double dz = (i - n / 2) * dd;
        		double z = z0 + dz;
        		robomick.rcm ().do_cmd ("MOVEZ " + z + "");
			String remote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 768 768");
			String localname = robomick.datapath + i + ".tif";
			File f = robomick.rcm ().do_get_file (remote, localname);
        	}
        	robomick.rcm ().do_cmd ("MOVEZ " + z0 + "");
/*
*/
	}
}


