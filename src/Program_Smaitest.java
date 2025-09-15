import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Program_Smaitest extends Program {
	Robomick robomick;
	
	public Program_Smaitest (Robomick r) {
		robomick = r;
	}
	
	public void run ()throws Exception {
		Smai smai = new Smai ();
		
		robomick.rcm ().do_cmd ("SET_SWEEP 1");
		robomick.rcm ().do_cmd ("SET_CHURCHWINDOW 405");
		robomick.rcm ().do_cmd ("SET_CHSELECT 1 0 0 0");
		robomick.rcm ().do_cmd ("SET_CHINTENSITY 9 0 0 0");
        	
		String tile = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 768 768");
		File f1 = robomick.rcm ().do_get_file (tile, robomick.datapath + "tile.tif");
		
		// generate 8-bit filename and convert
		File f8 = new File (tile);
		String dir = f8.getParent ();
		String nm = "8bit-" + f8.getName ();
		robomick.rcm ().do_cmd ("CONVERT_16_TO_8BIT " + tile + " " + dir + "/" + nm);
		
		File f82 = robomick.rcm ().do_get_file (dir + "/" + nm, robomick.datapath + nm);
		
		
		List<Cell> cells = smai.smaiTile (f1, robomick.datapath + "detect.log", 0.0, 0.0, "id_prefix");
		
		System.out.println (cells);
		
		smai.tileMarkDetections (new File (robomick.datapath + nm), cells, new File (robomick.datapath + "mark.tif"));
	}
	
}


