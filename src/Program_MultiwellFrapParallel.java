import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Font;
import java.awt.Color;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import javax.imageio.*;
import java.nio.file.*;

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
	//String [] wells = {"w3", "w7", "w6", "w2"};  // well to process
	//String [] wells = {"w2", "w3", "w7", "w6"};  // well to process
	String [] wells = {"w1", "w2", "w3", "w4"};  // well to process
	//String [] wells = {"w1", "w2", "w3", "w4"};  // well to process
	//String [] wells = {"w5", "w6", "w7", "w8", "w4", "w3", "w2", "w1"};  // well to process
	//String [] wells = {"w1"};  // well to process
	//String [] wells = {"w5"};  // well to process
	//String [] wells = {"w2", "w3", "w4", "w5", "w11", "w10", "w9", "w8", "w14", "w15", "w16", "w17"};  // well to process
	String initWell = "w1";  // well currently positioned above objective
	int cells_per_well = 40;  // number of cells per well
	int cells_in_parallel = 5;  // number of cells in parallel
	double mindist = 0.015;  // minimum distance between cells (*7500 for px)
	int max_tiles = 90;  // maximum number of tiles per well to collect cells (may result in less collected cells per well)
	int tileSweep = 1;  // sweep for tile scans
	int detailSweep = 2;  // sweep for detail scan
	int [] intensity_image = {0, 14, 0, 0};  // intensity for imaging (405, 488, 561, 640 nm)
	int [] intensity_monitor = {0, 5, 0, 0};  // intensity for frap monitoring (405, 488, 561, 640 nm)
	int [] intensity_bleach = {100, 100, 100, 100};  // intensity for frap bleaching (405, 488, 561, 640 nm)
	int tileDim = 768;  // width and height of tile scans
	int detailDim = 768;  // width and height of detail scans
	int npre = 4;  // number of frap pre scans
	int nbl = 1;  // number of frap bleach scans
	int npost = 60;  // number of frap post scans
	int interval = 10000;  // interval for frap scans in ms
	boolean img_afterbl = true;  // toggle to make complete detail image of cell after bleach
	boolean img_exit = true;  // toggle to make complete detail image of cell after frap
	
	int frapOffsetX = 0;  // small top
	int frapOffsetY = -74;
	int frapWidth = 276;
	int frapHeight = 148;
	
//	int frapOffsetX = 0;  // strip @ center
//	int frapOffsetY = -16;
//	int frapWidth = 256;
//	int frapHeight = 32;
	
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
	
	int eval_min_dim = 75;
	int eval_max_dim = 200;
	double eval_min_score = 0.6;
	
	
	double max_int_fraction = 0.2;  // 0.1 -> keep 10% most bright cells
	
	
	
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


public class Program_MultiwellFrapParallel extends Program_MultiwellFrap {
	Robomick robomick;
	
	public Program_MultiwellFrapParallel (Robomick r) {
		robomick = r;
	}
	
	
	public void run () throws Exception {
		
		
		Config config = new Config ();
		config.dump ();
		config.tofile (robomick.datapath + "config.txt");
		
		
		long t_begin = System.currentTimeMillis ();
		
		Tool.log (t_begin, "core", "start");
		
		
		Ibidi ibidi = new Ibidi (config.ibidiSize);  // use Ibidi to convert well name to well position
		Smai smai = new Smai ();
		
		smai.setDebug (true);
		robomick.rcm ().setDebug (true);
		
		robomick.rcm ().do_cmd ("ZEROXY");
		
		boolean objLow = false;
		double zInit = Double.parseDouble (robomick.rcm ().do_cmd ("GET_Z"));
		
		
		for (String well : config.wells) {
			
			File wellDir = new File (robomick.datapath + well + "/");
			wellDir.mkdirs ();
			File cropDir = new File (robomick.datapath + well + "/crop/");
			cropDir.mkdirs ();
			
			
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
			Tool.log (-1, "info", "well name = " + colGreen + well + colReset + "  x, y = " + df.format (wellX) + ", " + df.format (wellY));
			
			char [] arrDir = {'u', 'r', 'd', 'l'};
			int indexDir = 0;
			int stepPerDir = 1;
			int stepInDir = stepPerDir;
			int tileX = 0;  // tile pos in unit (center tile = [0, 0], tile below = [0, -1], etc)
			int tileY = 0;
			int nTiles = 0;
			
			
			List<Cell> wellCells = new ArrayList<> ();
			do {
				// move in spiral around center tile until enough cells found
				//    4 5 6
				//    3 0 7
				//    2 1 8
				//        9
				
				robomick.rcm ().do_cmd ("SET_SWEEP " + config.tileSweep);
				//System.out.println ("scan tile x=" + tileX + " y=" + tileY);
				
				// for sweep 1
				double um_per_img = config.tileDim * 0.00013333;
				if (config.tileSweep == 2) {
					um_per_img /= 2;
				}
				
				double tileOverlap = 0.25; // fraction
				double um_move = um_per_img * (1.0 - tileOverlap);
				double offsetX = wellX + (tileX * um_move);  // tile center in real world um
				double offsetY = wellY + (tileY * um_move);
				
				Tool.log (-1, "info", "tile = " + colGreen + nTiles + colReset + "(" + tileX + ", " + tileY + ")" + " x, y = " + df.format (offsetX) + ", " + df.format (offsetY));
				robomick.rcm ().do_cmd ("MOVEXY " + offsetX + " " + offsetY);
				
				if (objLow) {
					robomick.rcm ().do_cmd ("MOVEZ " + (z));
					delay (2000);  // need this to re-stabalize zstage after long distance move
					objLow = false;
					System.out.println ("finding focus...");
					robomick.rcm ().do_cmd ("FIND_FOCUS");
				}
				
				
				// snap tile
				String remote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.tileDim + " " + config.tileDim);
				String local = wellDir.toString () + "/" + well + "-t" + nTiles + ".tif";
				File f = robomick.rcm ().do_get_file (remote, local);
				
				
				// send image to smai and process detection
				String detlog = wellDir.toString () + "/" + well + "-t" + nTiles + "-detect.log";
				String id_prefix = "" + well + "-t" + nTiles + "-c";
				List<Cell> tileCells = smai.smaiTile (f, detlog, offsetX, offsetY, id_prefix);
				
				
				// make sure cell coords are within range of image (will cause troubles in cropCellFromTile)
				fixCoords (tileCells, config.tileDim);
				
				
				// convert 16-bit tile to 8-bit 
				String dir = new File (remote).getParent ();
				String nm = new File (remote).getName ();
				String remote8bit = dir + "/" + "8bit-" + nm;
				robomick.rcm ().do_cmd ("CONVERT_16_TO_8BIT " + remote + " " + remote8bit);
				String local8bit = wellDir.toString () + "/" + well + "-t" + nTiles + "-8bit.tif";
				File f8 = robomick.rcm ().do_get_file (remote8bit, local8bit);
				
				
				// transfer detected and accepted cells to wellCells
				List<Cell> acceptedCells = new ArrayList<> ();
				
				Tool.log (-1, "info", "found " + colRed + tileCells.size () + colReset + " cell(s) in tile " + colGreen + nTiles + colReset + " (" + tileX + ", " + tileY + ")");
				
				int nbefore = wellCells.size ();
				for (int ci = 0; ci < tileCells.size (); ci++) {
					if (wellCells.size () < (config.cells_per_well / config.max_int_fraction)) {
						if (distOkay (tileCells.get (ci), wellCells, config.mindist)) {
							
							// crop cell from tile
							String trg = cropDir.toString () + "/" + tileCells.get (ci).id + "-crop.tif";
							BufferedImage crop = cropCellFromTile (local, tileCells.get (ci), trg);
							
							// evaluate cropped cell
							boolean cellOkay = evaluateCell (config, tileCells.get (ci), crop);
							if (cellOkay) {
								acceptedCells.add (tileCells.get (ci));
								wellCells.add (tileCells.get (ci));
							}
						}
						else {
							Tool.log (-1, "too_close:reject", "rejecting cell too close to other cell");
						}
					}
				}
				int colfromtile = wellCells.size () - nbefore;
				Tool.log (-1, "info", "after rejection collected " + colRed + colfromtile + colReset + " cell(s) in tile " + colGreen + nTiles + colReset + " (" + tileX + ", " + tileY + ")");
				Tool.log (-1, "info", "subtotal #cells collected in well " + colGreen + well + colReset + ": " + colRed + wellCells.size () + colReset + " (required: " + ((int) (config.cells_per_well / config.max_int_fraction)) + ")");
				
				
				// generate mark image based on acceptedCells
				String markfile = wellDir.toString () + "/" + well + "-t" + nTiles + "-mark.tif";
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
				
				nTiles++;
			} while ((wellCells.size () < (config.cells_per_well / config.max_int_fraction)) && (nTiles < config.max_tiles));
			
			
			Tool.log (-1, "info", "total #cells collected in well (before removal of too dim cells) " + colGreen + well + colReset + ": " + colRed + wellCells.size () + colReset);
			
			// reverse order cells by brightness
			wellCells.sort (new Comparator<Cell> () {
				@Override
				public int compare (Cell c1, Cell c2) {
					if (c1.iavg == c2.iavg) {
						return 0;
					}
					return c1.iavg > c2.iavg ? -1 : 1;
				}
			});
			
			
			
			// keep most bright cells only
			wellCells = wellCells.subList (0, Math.min (config.cells_per_well, wellCells.size ()));
			
			Tool.log (-1, "info", "final #cells collected in well (after removal of too dim cells) " + colGreen + well + colReset + ": " + wellCells.size ());
			
			
			
			// kind of order cells for minimal xy stage movement
			wellCells.sort (new Comparator<Cell> () {
				@Override
				public int compare (Cell c1, Cell c2) {
					int tile1 = Integer.parseInt ((c1.id.split ("-") [1]).substring (1));
					int tile2 = Integer.parseInt ((c2.id.split ("-") [1]).substring (1));
					if (tile1 == tile2) {
						return 0;
					}
					return tile1 < tile2 ? -1 : 1;
				}
			});
			
			
			writeLog (robomick.datapath + "cell.log", "wellCells:");
			
			Tool.log (-1, "info", "wellCells:");
			
			if (wellCells.size () > 0) {
				for (int ci = 0; ci < wellCells.size (); ci++) {
					writeLog (robomick.datapath + "cell.log", ci + "\t" + wellCells.get (ci));
					Tool.log (-1, "info", colYellow + ci + "\t" + wellCells.get (ci) + colReset);
				}
				System.out.println ();
			}
			else {
				writeLog (robomick.datapath + "cell.log", "No cells found in well " + well);
				Tool.log (-1, "info", "no cells found in well " + colGreen + well + colReset);
			}
			
			
			
			// make image of tile with selected cells only
			for (Cell c : wellCells) {
				// get well, tile and cell from cellid
				String [] seg = c.id.split ("-");
				String seg_well = seg [0];
				String seg_tile = seg [1];
				String seg_cell = seg [2];
				
				// create wX-tY-mark2.tif if not existing
				String str_mark2 = wellDir.toString () + "/" + seg_well + "-" + seg_tile + "-mark2.tif";
				File mark2 = new File (str_mark2);
				if (!mark2.exists ()) {
					String str_base = wellDir.toString () + "/" + seg_well + "-" + seg_tile + "-8bit.tif";
					File base = new File (str_base);
					if (!base.exists ()) {
						System.out.println ("base = " + base);
						System.out.println ("base not exiting...");
						System.exit (1);
					}
					
					try {
						Path fromFile = Paths.get (str_base);
						Path toFile = Paths.get (str_mark2);
						Files.copy (fromFile, toFile);
					}
					catch (Exception x) {
						x.printStackTrace ();
					}
				}
				
				// add cell coordinates to mark2 file
				try {
					BufferedImage bi = ImageIO.read (mark2);
					Graphics2D g2 = bi.createGraphics ();
					g2.setFont (new Font ("serif", Font.PLAIN, 12));
					g2.setColor (Color.WHITE);
					g2.drawRect (c.x, c.y, c.w, c.h);
					g2.drawString ("" + c.id, c.x + 2, c.y + 12);
					g2.dispose ();
					ImageIO.write (bi, "tif", mark2);
				}
				catch (Exception x) {
					x.printStackTrace ();
				}
			}
			
			
			// make detail image of centered cell
			robomick.rcm ().do_cmd ("SET_SWEEP " + config.detailSweep);
			
			
			// prep frap data storage
			FrapTimepoint data [][] = new FrapTimepoint [wellCells.size ()][config.npre + config.nbl + config.npost];
			
			
			// prep mask storage
			File [] mask = new File [wellCells.size ()];
			
			
			
			double um_per_px = 0.00013333;
			if (config.detailSweep == 2) {
				um_per_px /= 2;
			}
			
			
			
			
			int ci = 0;
			while (ci < wellCells.size ()) {
				
				int iFrom = ci;  // i from (incl)
				int iTo = Math.min (ci + config.cells_in_parallel, wellCells.size ());  // i to (excl)
				
				
				for (int i = iFrom; i < iTo; i++) {
					Cell cell = wellCells.get (i);
					File cellDir = new File (robomick.datapath + cell.id + "/");
					cellDir.mkdirs ();
				}
				
				robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_image));
				robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_image));
				
				Tool.log (-1, "phase", "INIT parallel session :: phase 1 (finetune position + make prefrap image and mask)");
				
				for (int i = iFrom; i < iTo; i++) {
					Cell cell = wellCells.get (i);
					Tool.log (-1, "frap", colYellow + "prep" + colReset + " cell " + colGreen + cell.id + colReset);
					
					
					File cellDir = new File (robomick.datapath + cell.id + "/");
					
					
					// move to cell center
					robomick.rcm ().do_cmd ("MOVEXY " + cell.rwx + " " + cell.rwy);
					
					// make pre frap image (required for making premask)
					String prefrapremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
					String prefraplocal = cellDir.toString () + "/" + cell.id + "-INIT.tif";
					File cellpref = robomick.rcm ().do_get_file (prefrapremote, prefraplocal);
					
					// make mask of prefrap image and get cell center
					File premask = new File (cellDir.toString () + "/" + cell.id + "-premask.tif");
					Point presmaidet = smai.smaiDetail (cellpref, premask, cell.rwx, cell.rwy, cell.id);
					//System.out.println ("presmaidet = " + presmaidet);
					if (presmaidet.x == -1) {
						System.out.println ("failed to generate mask");
						writeLog (robomick.datapath + "log.txt", "failed to generate mask");
					}
					
					
					// finetune cell center based on prefrap image
					int center = config.detailDim / 2;
					int displaceX = center - presmaidet.x;
					int displaceY = center - presmaidet.y;
					cell.rwx = cell.rwx + displaceX * um_per_px;
					cell.rwy = cell.rwy - displaceY * um_per_px;
					
					// move to finetuned cell center
					robomick.rcm ().do_cmd ("MOVEXY " + cell.rwx + " " + cell.rwy);
					
					// make image after finetuning cell center
					String afterftremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
					String afterftlocal = cellDir.toString () + "/" + cell.id + "-INITFT.tif";
					File cellafterft = robomick.rcm ().do_get_file (afterftremote, afterftlocal);
					
					double background = getBackground (cellafterft);
					
					// make mask of finetuned prefrap image
					// make mask after finetuning cell center
					mask [i] = new File (cellDir.toString () + "/" + cell.id + "-mask.tif");
					Point smaidet = smai.smaiDetail (cellafterft, mask [i], cell.rwx, cell.rwy, cell.id);
					//System.out.println ("smaidet = " + smaidet);
					if (smaidet.x == -1) {
						System.out.println ("failed to generate mask");
						writeLog (robomick.datapath + "log.txt", "failed to generate mask");
					}
				}
				Tool.log (-1, "phase", "EXIT parallel session :: phase 1");
				
				
				Tool.log (-1, "phase", "INIT parallel session :: phase 2 (prebleach)");
				robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_monitor));
				robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_monitor));
				long pret0 = System.currentTimeMillis ();
				for (int r = 0; r < config.npre; r++) {
					long tbegin = pret0 + r * config.interval;
					Tool.log (-1, "frap", "waiting " + (tbegin - System.currentTimeMillis ()) + " ms for next interval");
					while (System.currentTimeMillis () < tbegin) {
						Thread.sleep (1);
					}
					for (int i = iFrom; i < iTo; i++) {
						Cell cell = wellCells.get (i);
						File cellDir = new File (robomick.datapath + cell.id + "/");
						Tool.log (-1, "frap", colYellow + "pre " + (r + 1) + colReset + " of " + colYellow + (config.npre) + colReset + " for cell " + colGreen + cell.id + colReset);
						
						double rwfrapoffsetx = config.frapOffsetX * um_per_px;
						double rwfrapoffsety = config.frapOffsetY * um_per_px;
						double moveX = cell.rwx + rwfrapoffsetx;
						double moveY = cell.rwy + rwfrapoffsety;
						robomick.rcm ().do_cmd ("MOVEXY " + moveX + " " + moveY);
						
						
						FrapTimepoint ft = new FrapTimepoint ();
						ft.timestamp = System.currentTimeMillis ();
						
						String preremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.frapWidth + " " + config.frapHeight);
						
						String prelocal = cellDir.toString () + "/" + cell.id + "_pre" + r + ".tif";
						String prelocalmasked = cellDir.toString () + "/" + cell.id + "_pre" + r + "_masked.tif";
						File ff = robomick.rcm ().do_get_file (preremote, prelocal);
						
						ft.value = getIntensity (ff, mask [i], prelocalmasked, config.frapOffsetX, config.frapOffsetY, config.frapWidth, config.frapHeight);
						ft.bg = background;
						ft.desc = "pre" + r;
						data [i][r] = ft;
						
					}
				}
				Tool.log (-1, "phase", "EXIT parallel session :: phase 2");
				
				
				Tool.log (-1, "phase", "INIT parallel session :: phase 3 (bleach)");
				robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_bleach));
				robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_bleach));
				for (int r = 0; r < config.nbl; r++) {
					for (int i = iFrom; i < iTo; i++) {
						Cell cell = wellCells.get (i);
						File cellDir = new File (robomick.datapath + cell.id + "/");
						Tool.log (-1, "frap", colYellow + "bl " + (r + 1) + colReset + " of " + colYellow + (config.nbl) + colReset + " for cell " + colGreen + cell.id + colReset);
						
						double rwfrapoffsetx = config.frapOffsetX * um_per_px;
						double rwfrapoffsety = config.frapOffsetY * um_per_px;
						double moveX = cell.rwx + rwfrapoffsetx;
						double moveY = cell.rwy + rwfrapoffsety;
						robomick.rcm ().do_cmd ("MOVEXY " + moveX + " " + moveY);
						
						String blremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.frapWidth + " " + config.frapHeight);
						String bllocal = cellDir.toString () + "/" + cell.id + "_bl" + i + ".tif";
						File ff = robomick.rcm ().do_get_file (blremote, bllocal);
						
						
						if (config.img_afterbl) {
							// move back to original pre-prebleach position
							robomick.rcm ().do_cmd ("MOVEXY " + cell.rwx + " " + cell.rwy);
						
							// after bleach image
							robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_image));
							robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_image));
							String afterbleachremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
							String afterbleachlocal = cellDir.toString () + "/" + cell.id + "-AFTERBL.tif";
							File cellafterbleach = robomick.rcm ().do_get_file (afterbleachremote, afterbleachlocal);
							
							robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_bleach));
							robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_bleach));
						}
						
					}
				}
				Tool.log (-1, "phase", "EXIT parallel session :: phase 3");
				
				
				Tool.log (-1, "phase", "INIT parallel session :: phase 4 (postbleach)");
				robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_monitor));
				robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_monitor));
				long postt0 = System.currentTimeMillis ();
				for (int r = 0; r < config.npost; r++) {
					long tbegin = postt0 + r * config.interval;
					Tool.log (-1, "frap", "waiting " + (tbegin - System.currentTimeMillis ()) + " for interval");
					while (System.currentTimeMillis () < tbegin) {
						Thread.sleep (1);
					}
					for (int i = iFrom; i < iTo; i++) {
						Cell cell = wellCells.get (i);
						File cellDir = new File (robomick.datapath + cell.id + "/");
						Tool.log (-1, "frap", colYellow + "post " + (r + 1) + colReset + " of " + colYellow + (config.npost) + colReset + " for cell " + colGreen + cell.id + colReset);
						
						double rwfrapoffsetx = config.frapOffsetX * um_per_px;
						double rwfrapoffsety = config.frapOffsetY * um_per_px;
						double moveX = cell.rwx + rwfrapoffsetx;
						double moveY = cell.rwy + rwfrapoffsety;
						robomick.rcm ().do_cmd ("MOVEXY " + moveX + " " + moveY);
						
						
						FrapTimepoint ft = new FrapTimepoint ();
						ft.timestamp = System.currentTimeMillis ();
						
						String postremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.frapWidth + " " + config.frapHeight);
						
						String postlocal = cellDir.toString () + "/" + cell.id + "_post" + r + ".tif";
						String postlocalmasked = cellDir.toString () + "/" + cell.id + "_post" + r + "_masked.tif";
						File ff = robomick.rcm ().do_get_file (postremote, postlocal);
						
						ft.value = getIntensity (ff, mask [i], postlocalmasked, config.frapOffsetX, config.frapOffsetY, config.frapWidth, config.frapHeight);
						ft.bg = background;
						ft.desc = "post" + r;
						data [i][config.npre + config.nbl + r] = ft;
						
						
						
						if ((r > 0) && (r % 6 == 0)) {
							robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_image));
							robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_image));
							
							Tool.log (-1, "frap", colYellow + "IMGPOST-" + r + colReset + " cell " + colGreen + cell.id + colReset);
							String imgremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
							String imglocal = cellDir.toString () + "/" + cell.id + "-POST" + r + ".tif";
							File imgf = robomick.rcm ().do_get_file (imgremote, imglocal);
							
							robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_monitor));
							robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_monitor));
						}
						
						
					}
				}
				Tool.log (-1, "phase", "EXIT parallel session :: phase 4");
				
				
				Tool.log (-1, "phase", "INIT parallel session :: phase 5 (postfrap image)");
				if (config.img_exit) {
					
					robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_image));
					robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_image));
					
					for (int i = iFrom; i < iTo; i++) {
						Cell cell = wellCells.get (i);
						File cellDir = new File (robomick.datapath + cell.id + "/");
						Tool.log (-1, "frap", colYellow + "postp" + colReset + " cell " + colGreen + cell.id + colReset);
						
						// move to cell center
						robomick.rcm ().do_cmd ("MOVEXY " + cell.rwx + " " + cell.rwy);
						
						// make post frap image
						String postfrapremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
						String postfraplocal = cellDir.toString () + "/" + cell.id + "-EXIT.tif";
						File cellpostf = robomick.rcm ().do_get_file (postfrapremote, postfraplocal);
					}
				}
				else {
					Tool.log (-1, "phase", "  skipping...");
				}
				Tool.log (-1, "phase", "EXIT parallel session :: phase 5");
				
				Tool.log (-1, "phase", "INIT parallel session :: phase 6 (post proc)");
				
				
				// add data to frap log
				BufferedWriter frapLog = null;
				try {
					frapLog = new BufferedWriter (new FileWriter (robomick.datapath + "frap.log", true));
				}
				catch (IOException x) {
					x.printStackTrace ();
				}
				
				for (int x = iFrom; x < iTo; x++) {
					Cell cell = wellCells.get (x);
					
					
					frapLog.write (cell.id + "\r\n");
					frapLog.write ("step\ttime\tsignal\tbg\r\n");
					
					long t0 = data [x][0].timestamp;
					for (int r = 0; r < data [x].length; r++) {
						if (data [x][r] != null) {  // skip bleach pulses, which have no data
							FrapTimepoint ft = data [x][r];
							frapLog.write (ft.desc + "\t" + (ft.timestamp - t0) + "\t" + ft.value + "\t" + ft.bg + "\r\n");
						}
					}
					
					frapLog.write ("\r\n");
					
					
				}
				
				try {
					frapLog.flush ();
					frapLog.close ();
				}
				catch (IOException x) {
					x.printStackTrace ();
				}
			
				
				Tool.log (-1, "phase", "EXIT parallel session :: phase 6");
				
				
				
				ci += config.cells_in_parallel;
			}

		}
		
		
		// return to starting position
		double wellX = ibidi.getX (config.wells [0]) - ibidi.getX (config.initWell);
		double wellY = ibidi.getY (config.wells [0]) - ibidi.getY (config.initWell);
		robomick.rcm ().do_cmd ("MOVEXY " + wellX + " " + wellY);
		robomick.rcm ().do_cmd ("MOVEZ " + zInit);
		
		
		// calc ETA
		long t_end = System.currentTimeMillis ();
		
		Tool.log (t_end, "core", "finish");
		Tool.log (t_end, "core", "actual duration:   " + Tool.format_duration (Math.round ((t_end - t_begin) / 1000)));
		
		double totOdo = Double.parseDouble (robomick.rcm ().do_cmd ("GET_ODO"));
		Tool.log (t_end, "core", "tot odo = " + totOdo + " mm");
		
		
	}
}


