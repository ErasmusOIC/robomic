import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.*;
import java.nio.charset.StandardCharsets;
import javax.imageio.*;

public class Program_TestAlignment extends Program {
	Robomick robomick;
	
	public Program_TestAlignment (Robomick r) {
		robomick = r;
	}
	
	public File snapToFile (String local) throws Exception {
		String remote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
		File f = robomick.rcm ().do_get_file (remote, local);
		return f;
	}
	
	public void run () throws Exception {
		
		robomick.rcm ().do_cmd ("SET_SWEEP 1");
		robomick.rcm ().do_cmd ("ZEROXY");
		robomick.rcm ().do_cmd ("SET_AF OFF");
		
		String remote;
		String local;
		File f;
		
		// get initial z position
		double z0 = Double.parseDouble (robomick.rcm ().do_cmd ("GET_Z"));
		double dd = 0.1;  // step size
		int n = 15;  // number of slices
		
		DecimalFormat df = new DecimalFormat ("#.000");
		
		for (int i = 0; i < n; i++) {
			double dz = (i - n / 2) * dd;
			double z = z0 + dz;
			String zstr = df.format (z);
			robomick.rcm ().do_cmd ("MOVEZ " + z + "");
			
			robomick.rcm ().do_cmd ("SET_CHURCHWINDOW 405");
			robomick.rcm ().do_cmd ("SET_CHSELECT 1 0 0 0");
			robomick.rcm ().do_cmd ("SET_CHINTENSITY 12 0 0 0");
			f = snapToFile (robomick.datapath + "405-" + i + "_" + zstr + ".tif");
			
			robomick.rcm ().do_cmd ("SET_CHURCHWINDOW 488");
			robomick.rcm ().do_cmd ("SET_CHSELECT 0 1 0 0");
			robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 10 0 0");
			f = snapToFile (robomick.datapath + "488-" + i + "_" + zstr + ".tif");
			robomick.rcm ().alignImage (f, new File (robomick.datapath + "488-" + i + "_" + zstr + "-tr.tif"), 488);
			
			robomick.rcm ().do_cmd ("SET_CHURCHWINDOW 561");
			robomick.rcm ().do_cmd ("SET_CHSELECT 0 0 1 0");
			robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 0 4 0");
			f = snapToFile (robomick.datapath + "561-" + i + "_" + zstr + ".tif");
			robomick.rcm ().alignImage (f, new File (robomick.datapath + "561-" + i + "_" + zstr + "-tr.tif"), 561);
		}
		
		robomick.rcm ().do_cmd ("MOVEZ " + z0 + "");
		robomick.rcm ().do_cmd ("SET_CHINTENSITY 12 10 4 0");
		
		
		
		
		
		
		
		
		
	}
}


