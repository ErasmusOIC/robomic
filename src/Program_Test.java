import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.*;
import java.awt.Font;
import java.awt.Color;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.charset.StandardCharsets;
import javax.imageio.*;
import java.nio.file.*;
import java.nio.file.StandardCopyOption.*;


class Config {
	/*
	
	* 8w:
	1	2	3	4
	5	6	7	8
	
	* 18w:
	1	2	3	4	5	6
	7	8	9	10	11	12
	13	14	15	16	17	18
	
	*/
	int ibidiSize = 18;
//	String [] wells = {"w7", "w8", "w9", "w10", "w11", "w12"};  // well to process
//	String initWell = "w7";  // well currently positioned above objective
//	String [] wells = {"w4", "w5", "w6", "w12", "w11", "w10", "w9", "w3", "w2", "w1", "w7", "w8", "w14"};  // well to process
//	String initWell = "w4";  // well currently positioned above objective
	String [] wells = {"w7"};  // well to process
	String initWell = "w7";  // well currently positioned above objective
	
	boolean actEnabled = false;
	
	int cells_per_well = 1;  // number of cells per well
	double mindist = 0.1;  // minimum distance between cells (*7500 for px)
	int max_tiles = 60;  // maximum number of tiles per well to collect cells (may result in less collected cells per well)
	int tileSweep = 1;  // sweep for tile scans
	int detailSweep = 2;  // sweep for detail scan
	int [] intensity_image = {0, 0, 40, 0};  // intensity for imaging (405, 488, 561, 640 nm)
	int [] intensity_monitor = {0, 0, 3, 0};  // intensity for frap monitoring (405, 488, 561, 640 nm)
	int [] intensity_bleach = {0, 0, 100, 0};  // intensity for frap bleaching (405, 488, 561, 640 nm)
	int [] intensity_activate = {0, 4, 0, 0};  // intensity for activation (405, 488, 561, 640 nm)
	int actPulses = 7;  // number of activation pulses
	//int actDim = 512;  // size of activated region
	//int actDim = 386;  // size of activated region -> does activate
	int actDim = 256;  // size of activated region -> does activate
	
	int tileDim = 768;  // width and height of tile scans
	int detailDim = 768;  // width and height of detail scans
	int npre = 10;  // number of frap pre scans
	int nbl = 2;  // number of frap bleach scans
//	int npost = 100;  // number of frap post scans
	int npost = 10;  // number of frap post scans
	int interval = 2000;  // interval for frap scans in ms
	boolean img_afterbl = true;  // toggle to make complete detail image of cell after bleach
	boolean img_exit = true;  // toggle to make complete detail image of cell after frap
	int n_img_exit = 1;  // number of exit images
	int interval_img_exit = 30000;  // interval for exit images
	
	int frapOffsetX = 0;  // strip @ center
	int frapOffsetY = -16;
	int frapWidth = 384;
	int frapHeight = 32;
	
//	int frapOffsetX = 0;  // small top
//	int frapOffsetY = -64;
//	int frapWidth = 256;
//	int frapHeight = 128;
	
//	int frapOffsetX = 0;  // top
//	int frapOffsetY = -192;
//	int frapWidth = 768;
//	int frapHeight = 384;
	
//	int frapOffsetX = +192;  // left
//	int frapOffsetY = 0;
//	int frapWidth = 384;
//	int frapHeight = 768;
	
//	int frapOffsetX = 0;  // bottom
//	int frapOffsetY = 192;
//	int frapWidth = 768;
//	int frapHeight = 384;
	
//	int frapOffsetX = -192;  // right
//	int frapOffsetY = 0;
//	int frapWidth = 384;
//	int frapHeight = 768;
	
//	int frapOffsetX = 0;  // center horizontal
//	int frapOffsetY = 0;
//	int frapWidth = 768;
//	int frapHeight = 384;
	
//	int frapOffsetX = 0;  // center vertical
//	int frapOffsetY = 0;
//	int frapWidth = 384;
//	int frapHeight = 768;
	
//	int frapOffsetX = 0;  // center
//	int frapOffsetY = 0;
//	int frapWidth = 384;
//	int frapHeight = 384;
	
//	int frapOffsetX = 0;  // center top
//	int frapOffsetY = -192;
//	int frapWidth = 384;
//	int frapHeight = 384;
	
//	int frapOffsetX = 192;  // top left
//	int frapOffsetY = -192;
//	int frapWidth = 384;
//	int frapHeight = 384;
	
//	int frapOffsetX = 192;  // center left
//	int frapOffsetY = 0;
//	int frapWidth = 384;
//	int frapHeight = 384;
	
//	int frapOffsetX = -192;  // center right
//	int frapOffsetY = 0;
//	int frapWidth = 384;
//	int frapHeight = 384;
	
	int eval_min_dim = 70;
	int eval_max_dim = 220;
	int eval_min_iavg = 110;
	double eval_min_score = 0.6;
	
	double max_int_fraction = 0.5;  // fraction of cells to keep (most bright)
	
	
	public String i2chw (int [] intensity) {
		// convert intensity array to correct church window
		int max = 0;
		int imax = 0;
		int nzero = 0;
		for (int i = 0; i < intensity.length; i++) {
			if (intensity [i] > max) {
				max = intensity [i];
				imax = i;
			}
			if (intensity [i] == 0) {
				nzero++;
			}
		}
		
		if (nzero < 3) {
			System.out.println ("Config.i2chw: WARNING: selecting church window on intensity array with < 3 zero values...");
		}
		
		if (imax == 0) {
			return "405";
		}
		else if (imax == 1) {
			return "488";
		}
		else if (imax == 2) {
			return "561";
		}
		else {
			return "640";
		}
	}
	
	public String ia2s (int [] intensity) {
		// convert intensity array to string
		StringBuilder sb = new StringBuilder ();
		for (int i : intensity) {
			sb.append (i);
			sb.append (" ");
		}
		return sb.toString ().trim ();
	}
	
	public String gen_chsel (int [] intensity) {
		// generate chselect string from intensity array
		StringBuilder sb = new StringBuilder ();
		for (int i : intensity) {
			if (i > 0) {
				sb.append ("1");
			}
			else {
				sb.append ("0");
			}
			sb.append (" ");
		}
		return sb.toString ().trim ();
	}
	
	public String asString () {
		// convert configuration to string
		StringBuilder sb = new StringBuilder ();
		try {
			Field [] fields = Config.class.getDeclaredFields ();
			for (Field field : fields) {
				Object val = field.get (this);
				if (field.getType ().isArray ()) {
					sb.append (field.getName () + " = [ ");
					if (val instanceof String []) {
						String [] aval = (String []) val;
						for (String v : aval) {
							sb.append (v + " ");
						}
					}
					else if (val instanceof int []) {
						int [] aval = (int []) val;
						for (int v : aval) {
							sb.append (v + " ");
						}
					}
					else if (val instanceof double []) {
						double [] aval = (double []) val;
						for (double v : aval) {
							sb.append (v + " ");
						}
					}
					else {
						sb.append ("unsupported array type...");
					}
					sb.append ("]\n");
				}
				else {
					sb.append (field.getName () + " = " + val + "\n");
				}
			}
		}
		catch (Exception x) {
			x.printStackTrace ();
		}
		return sb.toString ().trim ();
	}
	
	public void dump () {
		// dump configuration
		System.out.println (this.asString () + "\n\n");
	}
	
	public void tofile (String configfile) {
		// write configuration to file
		try {
			PrintWriter pw = new PrintWriter (configfile);
			pw.println (this.asString());
			pw.close ();
		}
		catch (Exception x) {
			x.printStackTrace ();
		}
	}
}


public class Program_Test extends Program_MultiwellFrap {
	Robomick robomick;
	
	public Program_Test(Robomick r) {
		robomick = r;
	}
	
	
	public void run () throws Exception {
		Config config = new Config ();
		config.dump ();
		config.tofile (robomick.datapath + "config.txt");
		
		Ibidi ibidi = new Ibidi (config.ibidiSize);
		Smai smai = new Smai ();
		
		smai.setDebug (false);
		robomick.rcm ().setDebug (false);
		
		
		
		String remote, local;
		File f;
		
		String wavelength = "561";
		
		robomick.rcm ().do_cmd ("SET_SWEEP 1");
		
		if (wavelength.equals ("405")) {
			robomick.rcm ().do_cmd ("SET_CHSELECT 1 0 0 0");
			robomick.rcm ().do_cmd ("SET_CHINTENSITY 10 0 0 0");
		}
		else if (wavelength.equals ("488")) {
			robomick.rcm ().do_cmd ("SET_CHSELECT 0 1 0 0");
			robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 8 0 0");
		}
		else if (wavelength.equals ("561")) {
			robomick.rcm ().do_cmd ("SET_CHSELECT 0 0 1 0");
			robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 0 4 0");
		}
		robomick.rcm ().do_cmd ("SET_CHURCHWINDOW " + wavelength);
		
		// center strip-64-wide
		remote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE_NEW 0 0 768 64");
		local = robomick.datapath + wavelength + "_0_0_768_64.tif";
		f = robomick.rcm ().do_get_file (remote, local);
		
	}
}


