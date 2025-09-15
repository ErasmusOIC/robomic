import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import javax.imageio.*;

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
	int ibidiSize = 8;
//	String [] wells = {"w1", "w2", "w3", "w7", "w6", "w5"};  // well to process
	//String [] wells = {"w1", "w2", "w3", "w4", "w8", "w7", "w6", "w5"};  // well to process
	//String [] wells = {"w2", "w3", "w7", "w6"};  // well to process
	String [] wells = {"w1"};  // well to process
	String initWell = "w1";  // well currently positioned above objective
	int cells_per_well = 1;  // number of cells per well
	double mindist = 0.02;  // minimum distance between cells (*7500 for px)
	int max_tiles = 10;  // maximum number of tiles per well to collect cells (may result in less collected cells per well)
	int tileSweep = 1;  // sweep for tile scans
	int detailSweep = 2;  // sweep for detail scan
	int [] intensity_image = {0, 10, 0, 0};  // intensity for imaging (405, 488, 561, 640 nm)
	int [] intensity_monitor = {0, 3, 0, 0};  // intensity for frap monitoring (405, 488, 561, 640 nm)
	int [] intensity_bleach = {100, 100, 100, 0};  // intensity for frap bleaching (405, 488, 561, 640 nm)
	int tileDim = 768;  // width and height of tile scans
	int detailDim = 768;  // width and height of detail scans
	int npre = 30;  // number of frap pre scans
	int nbl = 2;  // number of frap bleach scans
	int npost = 200;  // number of frap post scans
	int interval = 500;  // interval for frap scans in ms
	boolean img_afterbl = true;  // toggle to make complete detail image of cell after bleach
	boolean img_exit = true;  // toggle to make complete detail image of cell after frap
	
	int frapOffsetX = 0;  // strip @ center
	int frapOffsetY = -16;
	int frapWidth = 256;
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
	
	int eval_min_dim = 80;
	int eval_max_dim = 640;
	double eval_min_score = 0.6;
	
	double max_int_fraction = 1.0;  // keep all cells
	
	
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


public class Program_MultiwellFrapGeneric extends Program_MultiwellFrap {
	Robomick robomick;
	
	public Program_MultiwellFrapGeneric (Robomick r) {
		robomick = r;
	}
	
	
	public void run () throws Exception {
		Config config = new Config ();
		config.dump ();
		config.tofile (robomick.datapath + "config.txt");
		
		Ibidi ibidi = new Ibidi (config.ibidiSize);  // use Ibidi to convert well name to well position
		Smai smai = new Smai ();
		
		smai.setDebug (true);
		robomick.rcm ().setDebug (false);
		
		
		robomick.rcm ().do_cmd ("ZEROXY");
		
		
		boolean objLow = false;
		double zInit = Double.parseDouble (robomick.rcm ().do_cmd ("GET_Z"));
		
		for (String well : config.wells) {
			robomick.rcm ().do_cmd ("SET_CHURCHWINDOW " + config.i2chw (config.intensity_image));
			robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_image));
			robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_image));
			robomick.rcm ().do_cmd ("SET_AF OFF");
			
			double z = Double.parseDouble (robomick.rcm ().do_cmd ("GET_Z"));
			
			
			if (!well.equals (config.initWell)) {
				robomick.rcm ().do_cmd ("MOVEZ " + (z - 3000));
				delay (2000);  // need this to re-stabalize zstage after long distance move
				objLow = true;
			}
			
			
			double wellX = ibidi.getX (well) - ibidi.getX (config.initWell);  // well pos in real world um
			double wellY = ibidi.getY (well) - ibidi.getY (config.initWell);
			System.out.println ("well name = " + colGreen + well + colReset + "  x, y = " + df.format (wellX) + ", " + df.format (wellY));
			
			char [] arrDir = {'u', 'r', 'd', 'l'};
			int indexDir = 0;
			int stepPerDir = 1;
			int stepInDir = stepPerDir;
			int tileX = 0;  // tile pos in unit (center tile = [0, 0], tile below = [0, -1], etc)
			int tileY = 0;
			int nTiles = 0;
			
			
			// do tile scans until enough cells collected
			List<Cell> wellCells = new ArrayList<> ();
			do {
				// move in spiral around center tile until enough cells found
				//    4 5 6
				//    3 0 7
				//    2 1 8
				//        9
				
				robomick.rcm ().do_cmd ("SET_SWEEP " + config.tileSweep);
				
				// for sweep 1
				double um_per_img = config.tileDim * 0.00013333;
				if (config.tileSweep == 2) {
					um_per_img /= 2;
				}
				
				double tileOverlap = 0.25; // fraction
				double um_move = um_per_img * (1.0 - tileOverlap);
				double offsetX = wellX + (tileX * um_move);  // tile center in real world um
				double offsetY = wellY + (tileY * um_move);
				
				System.out.println ("tile = " + colGreen + nTiles + colReset + "(" + tileX + ", " + tileY + ")" + " x, y = " + df.format (offsetX) + ", " + df.format (offsetY));
				robomick.rcm ().do_cmd ("MOVEXY " + offsetX + " " + offsetY);
				
				if (objLow) {
					robomick.rcm ().do_cmd ("MOVEZ " + (z));
					delay (2000);  // need this to re-stabalize zstage after long distance move
					objLow = false;
					System.out.println ("finding focus...");
					robomick.rcm ().do_cmd ("FIND_FOCUS");
				}
				
				// enable AF
				robomick.rcm ().do_cmd ("SET_AF ON");
				
				// snap tile
				String remote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.tileDim + " " + config.tileDim);
				String local = robomick.datapath + "" + well + "-t" + nTiles + ".tif";
				File f = robomick.rcm ().do_get_file (remote, local);
				
				
				// send image to smai and process detection
				String detlog = robomick.datapath + "" + well + "-t" + nTiles + "-detect.log";
				String id_prefix = "" + well + "-t" + nTiles + "-c";
				List<Cell> tileCells = smai.smaiTile (f, detlog, offsetX, offsetY, id_prefix);
				
				
				// make sure cell coords are within range of image (will cause troubles in cropCellFromTile)
				fixCoords (tileCells, config.tileDim);
				
				
				// convert 16-bit tile to 8-bit 
				String dir = new File (remote).getParent ();
				String nm = new File (remote).getName ();
				String remote8bit = dir + "/" + "8bit-" + nm;
				robomick.rcm ().do_cmd ("CONVERT_16_TO_8BIT " + remote + " " + remote8bit);
				String local8bit = robomick.datapath + "" + well + "-t" + nTiles + "-8bit.tif";
				File f8 = robomick.rcm ().do_get_file (remote8bit, local8bit);
				
				
				// transfer detected and accepted cells to wellCells
				List<Cell> acceptedCells = new ArrayList<> ();
				System.out.println ("found " + colRed + tileCells.size () + colReset + " cell(s) in tile " + colGreen + nTiles + colReset + "(" + tileX + ", " + tileY + ") (required: " + config.cells_per_well + ")");
				
				writeLog (robomick.datapath + "log.txt", "found " + tileCells.size () + " cell(s) in tile " + nTiles + "(" + tileX + ", " + tileY + ") (required: " + config.cells_per_well + ")");
				
				for (int ci = 0; ci < tileCells.size (); ci++) {
					if (wellCells.size () < config.cells_per_well) {
						if (distOkay (tileCells.get (ci), wellCells, config.mindist)) {
							
							// crop cell from tile
							String trg = robomick.datapath + tileCells.get (ci).id + "-crop.tif";
							BufferedImage crop = cropCellFromTile (local, tileCells.get (ci), trg);
							
							
							// todo: make mask for trg
							// ... work in progress ...
/*

							// make mask for cropped cell
							File mask = new File (robomick.datapath + tileCells.get (ci).id + "-crop-mask.tif");
							Point smdet = smai.smaiDetail (new File (trg), mask, tileCells.get (ci).rwx, tileCells.get (ci).rwy, tileCells.get (ci).id);
							if (smdet.x == -1) {
								System.out.println ("failed to generate mask");
								writeLog (robomick.datapath + "log.txt", "failed to generate mask");
							}
							
							// apply mask to crop
							// ...
							

*/
							
							
							
							
							
							
							// evaluate cropped cell
							boolean cellOkay = evaluateCell (config, tileCells.get (ci), crop);
							if (cellOkay) {
								acceptedCells.add (tileCells.get (ci));
								wellCells.add (tileCells.get (ci));
							}
						}
						else {
							System.out.println ("  " + colRed + ":(" + colReset + " rejecting cell too close to other cell");
							System.out.println ("     cell: " + tileCells.get (ci));
							writeLog (robomick.datapath + "log.txt", "     :(" + " rejecting cell too close to other cell");
							writeLog (robomick.datapath + "log.txt", "     cell: " + tileCells.get (ci));
						}
					}
				}
				
				
				// generate mark image based on acceptedCells
				String markfile = robomick.datapath + "" + well + "-t" + nTiles + "-mark.tif";
				smai.tileMarkDetections (new File (local8bit), acceptedCells, new File (markfile));
				
				
				
				if (arrDir [indexDir] == 'u') {
					tileY++;
				}
				else if (arrDir [indexDir] == 'r') {
					tileX++;
				}
				else if (arrDir [indexDir] == 'd') {
					tileY--;
				}
				else if (arrDir [indexDir] == 'l') {
					tileX--;
				}
				
				stepInDir--;
				if (stepInDir == 0) {
					// inc stepPerDir after changing dir to r or l
					if (arrDir [indexDir] == 'r' || arrDir [indexDir] == 'l') {
						stepPerDir++;
					}
					
					// update direction
					indexDir = ++indexDir % arrDir.length;
					
					// reset stepInDir
					stepInDir = stepPerDir;
					
					
				}
				System.out.println ("#cells collected in well: " + colRed + wellCells.size () + colReset);
				nTiles++;
			} while ((wellCells.size () < config.cells_per_well) && (nTiles < config.max_tiles));
			
			
			// print collected cells in well
			System.out.println ();
			System.out.println ("wellCells:");
			
			writeLog (robomick.datapath + "log.txt", "wellCells:");
			
			if (wellCells.size () > 0) {
				for (int ci = 0; ci < wellCells.size (); ci++) {
					System.out.println (ci + "\t" + wellCells.get (ci));
					writeLog (robomick.datapath + "log.txt", ci + "\t" + wellCells.get (ci));
				}
				System.out.println ();
			}
			else {
				System.out.println ("No cells found in well " + colGreen + well + colReset);
				System.out.println ();
				
				writeLog (robomick.datapath + "log.txt", "No cells found in well " + well);
				
			}
			
			
			// make detail image of centered cell
			robomick.rcm ().do_cmd ("SET_SWEEP " + config.detailSweep);
			
			
			// process collected cells in well
			for (int ci = 0; ci < wellCells.size (); ci++) {
				Cell cell = wellCells.get (ci);
				
				// move to cell center
				robomick.rcm ().do_cmd ("MOVEXY " + cell.rwx + " " + cell.rwy);
				robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_image));
				robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_image));
				
				// pre frap image (required for making premask)
				String prefrapremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
				String prefraplocal = robomick.datapath + cell.id + "-INIT.tif";
				File cellpref = robomick.rcm ().do_get_file (prefrapremote, prefraplocal);
				
				// make premask and get cell center
				File premask = new File (robomick.datapath + cell.id + "-premask.tif");
				Point presmaidet = smai.smaiDetail (cellpref, premask, cell.rwx, cell.rwy, cell.id);
				if (presmaidet.x == -1) {
					System.out.println ("failed to generate mask");
					writeLog (robomick.datapath + "log.txt", "failed to generate mask");
				}
				
				// calc um per px
				double um_per_px = 0.00013333;
				if (config.detailSweep == 2) {
					um_per_px /= 2;
				}
				
				// finetune center cell based on pre frap image
				int center = config.detailDim / 2;
				int displaceX = center - presmaidet.x;
				int displaceY = center - presmaidet.y;
				cell.rwx = cell.rwx + displaceX * um_per_px;
				cell.rwy = cell.rwy - displaceY * um_per_px;
				
				// move to finetuned cell center
				robomick.rcm ().do_cmd ("MOVEXY " + cell.rwx + " " + cell.rwy);
				
				// make image after finetuning cell center
				String afterftremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
				String afterftlocal = robomick.datapath + cell.id + "-INITFT.tif";
				File cellafterft = robomick.rcm ().do_get_file (afterftremote, afterftlocal);
				
				double background = getBackground (cellafterft);
				
				
				// make mask after finetuning cell center
				File mask = new File (robomick.datapath + cell.id + "-mask.tif");
				Point smaidet = smai.smaiDetail (cellafterft, mask, cell.rwx, cell.rwy, cell.id);
				if (smaidet.x == -1) {
					System.out.println ("failed to generate mask");
					writeLog (robomick.datapath + "log.txt", "failed to generate mask");
				}
				
				// prep frap data storage
				List<FrapTimepoint> data = new ArrayList<> ();
				System.out.print ("[frap " + (ci + 1) + "/" + wellCells.size () + "] " + colYellow + cell.id + colReset);
				
				// to prevent SNAP_IMAGE in pre/bl/post to move twice (which is very time consuming)
				
				// move is done just once before pre and move back after post
				String [] xy = robomick.rcm ().do_cmd ("GET_XY").split (" ");
				double originalX = Double.parseDouble (xy [0]);
				double originalY = Double.parseDouble (xy [1]);
				double rwfrapoffsetx = config.frapOffsetX * um_per_px;
				double rwfrapoffsety = config.frapOffsetY * um_per_px;
				double moveX = originalX + rwfrapoffsetx;
				double moveY = originalY + rwfrapoffsety;
				robomick.rcm ().do_cmd ("MOVEXY " + moveX + " " + moveY);
				
				// pre bleach
				robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_monitor));
				robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_monitor));
				long pret0 = System.currentTimeMillis ();
				System.out.print ("  pre");
				for (int i = 0; i < config.npre; i++) {
					long tbegin = pret0 + i * config.interval;
					while (System.currentTimeMillis () < tbegin) {
						Thread.sleep (1);
					}
					System.out.print (" " + i);
					
					FrapTimepoint ft = new FrapTimepoint ();
					ft.timestamp = System.currentTimeMillis ();
					
					String preremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.frapWidth + " " + config.frapHeight);
					String prelocal = robomick.datapath + cell.id + "_pre" + i + ".tif";
					String prelocalmasked = robomick.datapath + cell.id + "_pre" + i + "_masked.tif";
					File ff = robomick.rcm ().do_get_file (preremote, prelocal);
					
					ft.value = getIntensity (ff, mask, prelocalmasked, config.frapOffsetX, config.frapOffsetY, config.frapWidth, config.frapHeight);
					ft.bg = background;
					ft.desc = "pre" + i;
					data.add (ft);
				}
				
				// bleach
				robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_bleach));
				robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_bleach));
				System.out.print (" bl ");
				for (int i = 0; i < config.nbl; i++) {
					System.out.print (" " + i);
					String blremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.frapWidth + " " + config.frapHeight);
					String bllocal = robomick.datapath + cell.id + "_bl" + i + ".tif";
					File ff = robomick.rcm ().do_get_file (blremote, bllocal);
				}
				
				// snap image after bleach when enabled
				if (config.img_afterbl) {
					
					// move back to original pre-prebleach position
					robomick.rcm ().do_cmd ("MOVEXY " + cell.rwx + " " + cell.rwy);
				
					// after bleach image
					robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_image));
					robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_image));
					String afterbleachremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
					String afterbleachlocal = robomick.datapath + cell.id + "-AFTERBL.tif";
					File cellafterbleach = robomick.rcm ().do_get_file (afterbleachremote, afterbleachlocal);
					
					// move to previous position
					robomick.rcm ().do_cmd ("MOVEXY " + moveX + " " + moveY);
				}
				
				// post bleach
				robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_monitor));
				robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_monitor));
				long postt0 = System.currentTimeMillis ();
				System.out.print (" post");
				for (int i = 0; i < config.npost; i++) {
					long tbegin = postt0 + i * config.interval;
					while (System.currentTimeMillis () < tbegin) {
						Thread.sleep (1);
					}
					System.out.print (" " + i);
					
					FrapTimepoint ft = new FrapTimepoint ();
					ft.timestamp = System.currentTimeMillis ();
					
					String postremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.frapWidth + " " + config.frapHeight);
					String postlocal = robomick.datapath + cell.id + "_post" + i + ".tif";
					String postlocalmasked = robomick.datapath + cell.id + "_post" + i + "_masked.tif";
					File ff = robomick.rcm ().do_get_file (postremote, postlocal);
					
					ft.value = getIntensity (ff, mask, postlocalmasked, config.frapOffsetX, config.frapOffsetY, config.frapWidth, config.frapHeight);
					ft.bg = background;
					ft.desc = "post" + i;
					data.add (ft);
				}
				System.out.println ();
				
				// move back to original pre-prebleach position
				robomick.rcm ().do_cmd ("MOVEXY " + originalX + " " + originalY);  // TODO: should move this line to next if statement
				
				// snap image at end of frap when enabled
				if (config.img_exit) {
					// post frap image
					robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_image));
					robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_image));
					String postfrapremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
					String postfraplocal = robomick.datapath + cell.id + "-EXIT.tif";
					File cellpostf = robomick.rcm ().do_get_file (postfrapremote, postfraplocal);
				}
				
				// add data to frap log
				BufferedWriter frapLog = null;
				try {
					frapLog = new BufferedWriter (new FileWriter (robomick.datapath + "frap.log", true));
				}
				catch (IOException x) {
					x.printStackTrace ();
				}
				frapLog.write (cell.id + "\r\n");
				frapLog.write ("step\ttime\tsignal\tbg\r\n");
				for (FrapTimepoint ft : data) {
					frapLog.write (ft.desc + "\t" + (ft.timestamp - data.get (0).timestamp) + "\t" + ft.value + "\t" + ft.bg + "\r\n");
				}
				frapLog.write ("\r\n");
				try {
					frapLog.flush ();
					frapLog.close ();
				}
				catch (IOException x) {
					x.printStackTrace ();
				}
			}
		}
		
		// return to starting position after finishing last well
		double wellX = ibidi.getX (config.wells [0]) - ibidi.getX (config.initWell);
		double wellY = ibidi.getY (config.wells [0]) - ibidi.getY (config.initWell);
		robomick.rcm ().do_cmd ("MOVEXY " + wellX + " " + wellY);
		robomick.rcm ().do_cmd ("MOVEZ " + zInit);
	}
}


