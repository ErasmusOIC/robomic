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
	int ibidiSize = 8;
//	String [] wells = {"w7", "w8", "w9", "w10", "w11", "w12"};  // well to process
//	String initWell = "w7";  // well currently positioned above objective
//	String [] wells = {"w4", "w5", "w6", "w12", "w11", "w10", "w9", "w3", "w2", "w1", "w7", "w8", "w14"};  // well to process
//	String initWell = "w4";  // well currently positioned above objective
	String [] wells = {"w2"};  // well to process
	String initWell = "w2";  // well currently positioned above objective
	
	boolean actEnabled = true;  // activation just before bleach
	
	int cells_per_well = 10;  // number of cells per well
	double mindist = 0.1;  // minimum distance between cells (*7500 for px)
	int max_tiles = 100;  // maximum number of tiles per well to collect cells (may result in less collected cells per well)
	int tileSweep = 1;  // sweep for tile scans
	int detailSweep = 2;  // sweep for detail scan
	int [] intensity_image = {0, 0, 25, 0};  // intensity for imaging (405, 488, 561, 640 nm)
	int [] intensity_monitor = {0, 0, 3, 0};  // intensity for frap monitoring (405, 488, 561, 640 nm)
	int [] intensity_bleach = {0, 0, 100, 0};  // intensity for frap bleaching (405, 488, 561, 640 nm)
	int [] intensity_activate = {0, 5, 0, 0};  // intensity for activation (405, 488, 561, 640 nm)
	int [] intensity_monitor_act = {0, 3, 3, 0};  // intensity for frap monitoring with activation (405, 488, 561, 640 nm)
	int actPulses = 8;  // number of activation pulses
	int actDim = 256;  // size of activated region -> does activate
	
	int tileDim = 768;  // width and height of tile scans
	int detailDim = 768;  // width and height of detail scans
	int npre = 10;  // number of frap pre scans
	int nbl = 2;  // number of frap bleach scans
	int npost = 100;  // number of frap post scans
	int interval = 1000;  // interval for frap scans in ms
	boolean img_afterbl = true;  // toggle to make complete detail image of cell after bleach
	boolean img_exit = true;  // toggle to make complete detail image of cell after frap
	int n_img_exit = 1;  // number of exit images
	int interval_img_exit = 30000;  // interval for exit images
	
	int frapOffsetX = 0;  // strip @ center
	int frapOffsetY = 0;  // keep x and y offset @ 0
	int frapWidth = 200;  // width does have impact on snapimage duration, height does not
	int frapHeight = 8;
	
	
	// DO NOT USE OFFSETS!
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


public class Program_MultiwellFrapOptogen extends Program_MultiwellFrap {
	Robomick robomick;
	
	public Program_MultiwellFrapOptogen (Robomick r) {
		robomick = r;
	}
	
	
	public void run () throws Exception {
		Config config = new Config ();
		config.dump ();
		config.tofile (robomick.datapath + "config.txt");
		
		Ibidi ibidi = new Ibidi (config.ibidiSize);  // use Ibidi to convert well name to well position
		Smai smai = new Smai ();
		
		smai.setDebug (false);
		robomick.rcm ().setDebug (false);
		
		
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
			
			
			// do tile scans until enough cells collected
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
				
				double tileOverlap = -1.00; // fraction
				double um_move = um_per_img * (1.0 - tileOverlap);
				double offsetX = wellX + (tileX * um_move);  // tile center in real world um
				double offsetY = wellY + (tileY * um_move);
				
				Tool.log (-1, "info", "tile = " + colGreen + nTiles + colReset + "(" + tileX + ", " + tileY + ")" + " x, y = " + df.format (offsetX) + ", " + df.format (offsetY));
				robomick.rcm ().do_cmd ("MOVEXY " + offsetX + " " + offsetY);
				
				if (objLow) {
					robomick.rcm ().do_cmd ("MOVEZ " + (z));
					delay (2000);  // need this to re-stabalize zstage after long distance move
					objLow = false;
					Tool.log (-1, "info", "finding focus...");
					robomick.rcm ().do_cmd ("FIND_FOCUS");
				}
				
				// enable AF
				robomick.rcm ().do_cmd ("SET_AF ON");
				
				// snap tile
				String remote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.tileDim + " " + config.tileDim);
				String local = robomick.datapath + "" + well + "/" + well + "-t" + nTiles + ".tif";
				File f = robomick.rcm ().do_get_file (remote, local);
				
				
				// send image to smai and process detection
				String detlog = robomick.datapath + "" + well + "/" + well + "-t" + nTiles + "-detect.log";
				String id_prefix = "" + well + "-t" + nTiles + "-c";
				List<Cell> tileCells = smai.smaiTile (f, detlog, offsetX, offsetY, id_prefix);
				
				
				// make sure cell coords are within range of image (will cause troubles in cropCellFromTile)
				fixCoords (tileCells, config.tileDim);
				
				
				// convert 16-bit tile to 8-bit 
				String dir = new File (remote).getParent ();
				String nm = new File (remote).getName ();
				String remote8bit = dir + "/" + "8bit-" + nm;
				robomick.rcm ().do_cmd ("CONVERT_16_TO_8BIT " + remote + " " + remote8bit);
				String local8bit = robomick.datapath + "" + well + "/" + well + "-t" + nTiles + "-8bit.tif";
				File f8 = robomick.rcm ().do_get_file (remote8bit, local8bit);
				
				
				// make copy of 8-bit tile image to mark accepted cells
				Path csrc = new File (wellDir.toString () + "/" + well + "-t" + nTiles + "-8bit.tif").toPath ();
				Path ctrg = new File (wellDir.toString () + "/" + well + "-t" + nTiles + "-mark.tif").toPath ();
				Files.copy (csrc, ctrg);
				
				
				Tool.log (-1, "debug", "found " + colYellow + tileCells.size () + colReset + " cells in tile " + nTiles);
				
				// add acceptable nuclei from tileCells to wellCells
				int nAccepted = 0;
				for (int ci = 0; ci < tileCells.size (); ci++) {
					// crop cell from tile
					String trg = cropDir.toString () + "/" + tileCells.get (ci).id + "-crop.tif";
					BufferedImage crop = cropCellFromTile (local, tileCells.get (ci), trg);
					
					// evaluate cropped cell (min/max dim and score)
					boolean cellOkay = evaluateCell (config, tileCells.get (ci), crop);
					
					if (tileCells.get (ci).iavg < config.eval_min_iavg) {
						cellOkay = false;
					}
					
					if (cellOkay) {
						Tool.log (-1, "debug", "accepted cell " + ci + "\t  cell: " + tileCells.get (ci));
						wellCells.add (tileCells.get (ci));
						nAccepted++;
					}
				}
				
				Tool.log (-1, "debug", "accepted " + colGreen + nAccepted + colReset + " cells in tile " + nTiles);
				
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
				
				// test distance between cells in wellCells
				// if distance < mindist then remove cell with lowest iavg
				int nRejected = 0;
				for (int i = wellCells.size () - 1; i >= 1; i--) {
					for (int j = i - 1; j >= 0; j--) {
						if (i >= wellCells.size ()) {
							// break out
							continue;
						}
						
						double dx2 = (wellCells.get (i).rwx - wellCells.get (j).rwx) * (wellCells.get (i).rwx - wellCells.get (j).rwx);
						double dy2 = (wellCells.get (i).rwy - wellCells.get (j).rwy) * (wellCells.get (i).rwy - wellCells.get (j).rwy);
						double dist = Math.sqrt (dx2 + dy2);
						if (dist < config.mindist) {
							Tool.log (-1, "info", "DISTANCE TOO SMALL! removing cell " + wellCells.get (i) + "  while keeping cell " + wellCells.get (j));
							wellCells.remove (i);
							nRejected++;
							continue;
						}
					}
				}
				
				Tool.log (-1, "debug", "rejected " + colRed + nRejected + colReset + " cells due to distance");
				
				Tool.log (-1, "debug", "number of cells in wellCells:  " + colYellow + wellCells.size () + colReset + " after " + (nTiles + 1) + " tiles (required cells: " + (int) (config.cells_per_well / config.max_int_fraction) + ", max_tiles: " + config.max_tiles + ")");
				
				
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
			} while ((wellCells.size () < config.cells_per_well / config.max_int_fraction) && (nTiles < config.max_tiles));
			
			
			// keep most bright cells only
			wellCells = wellCells.subList (0, Math.min (config.cells_per_well, wellCells.size ()));
			
			Tool.log (-1, "info", "final #cells collected in well (after removal of too dim cells) " + colGreen + well + colReset + ": " + wellCells.size ());
			
			// order cells for (kindof) minimal xy stage movement
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
			
			// mark accepted cells in tile
			for (Cell c : wellCells) {
				String [] seg = c.id.split ("-");
				String tilename = seg [1];
				File markfile = new File (wellDir.toString () + "/" + well + "-" + tilename + "-mark.tif");
				//System.out.println (">> cell id = " + c.id + "  markfile = " + markfile);
				
				try {
					BufferedImage bi = ImageIO.read (markfile);
					Graphics2D g2 = bi.createGraphics ();
					g2.setFont (new Font ("Serif", Font.PLAIN, 12));
					g2.setColor (Color.WHITE);
					g2.drawRect (c.x, c.y, c.w, c.h);
					g2.drawString ("" + c.id, c.x + 2, c.y + 12);
					g2.dispose ();
					
					ImageIO.write (bi, "tif", markfile);
					
				}
				catch (Exception x) {
					x.printStackTrace ();
				}
			}
			
			writeLog (robomick.datapath + "cell.log", "wellCells (" + well + "):");
			Tool.log (-1, "info", "wellCells:");
			
			if (wellCells.size () > 0) {
				for (int ci = 0; ci < wellCells.size (); ci++) {
					writeLog (robomick.datapath + "cell.log", ci + "\t" + wellCells.get (ci));
					Tool.log (-1, "info", colYellow + ci + "\t" + wellCells.get (ci) + colReset);
				}
				//System.out.println ();
			}
			else {
				writeLog (robomick.datapath + "cell.log", "No cells found in well " + well);
				Tool.log (-1, "info", "No cells found in well " + colGreen + well + colReset);
			}
			
			
			
			// make detail image of centered cell
			robomick.rcm ().do_cmd ("SET_SWEEP " + config.detailSweep);
			
			
			// calc um per px
			double um_per_px = 0.00013333;
			if (config.detailSweep == 2) {
				um_per_px /= 2;
			}
			
			
			
			// process collected cells in well
			for (int ci = 0; ci < wellCells.size (); ci++) {
				Cell cell = wellCells.get (ci);
				
				SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
				System.out.print (sdf.format (new Date (System.currentTimeMillis ())) + " [frap " + (ci + 1) + "/" + wellCells.size () + "] " + colYellow + cell.id + colReset);
				
				// move to cell center
				robomick.rcm ().do_cmd ("MOVEXY " + cell.rwx + " " + cell.rwy);
				robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_image));
				robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_image));
				
				// pre frap image (required for making premask)
				System.out.print ("  [img-init]");
				String prefrapremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
				String prefraplocal = robomick.datapath + well + "/" + cell.id + "-INIT.tif";
				File cellpref = robomick.rcm ().do_get_file (prefrapremote, prefraplocal);
				
				// make premask and get cell center
				File premask = new File (robomick.datapath + well + "/" + cell.id + "-premask.tif");
				Point presmaidet = smai.smaiDetail (cellpref, premask, cell.rwx, cell.rwy, cell.id);
				if (presmaidet.x == -1) {
					Tool.log (-1, "info", "failed to generate mask");
				}
				
				// finetune center cell based on pre frap image
				int center = config.detailDim / 2;
				int displaceX = center - presmaidet.x;
				int displaceY = center - presmaidet.y;
				cell.rwx = cell.rwx + displaceX * um_per_px;
				cell.rwy = cell.rwy - displaceY * um_per_px;
				
				// move to finetuned cell center
				robomick.rcm ().do_cmd ("MOVEXY " + cell.rwx + " " + cell.rwy);
				
				// Enable auto focus
				robomick.rcm ().do_cmd ("SET_AF ON");
				
				// make image after finetuning cell center
				System.out.print ("  [img-initft]");
				String afterftremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
				String afterftlocal = robomick.datapath + well + "/" + cell.id + "-INITFT.tif";
				File cellafterft = robomick.rcm ().do_get_file (afterftremote, afterftlocal);
				
				// make mask after finetuning cell center
				File mask = new File (robomick.datapath + well + "/" + cell.id + "-mask.tif");
				Point smaidet = smai.smaiDetail (cellafterft, mask, cell.rwx, cell.rwy, cell.id);
				if (smaidet.x == -1) {
					Tool.log (-1, "info", "failed to generate mask");
				}
				
				
				double background = getBackground (cellafterft);
				
				
				
				// prep frap data storage
				List<FrapTimepoint> data = new ArrayList<> ();
				
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
					String prelocal = robomick.datapath + well + "/" + cell.id + "_pre" + i + ".tif";
					String prelocalmasked = robomick.datapath + well + "/" + cell.id + "_pre" + i + "_masked.tif";
					File ff = robomick.rcm ().do_get_file (preremote, prelocal);
					
					ft.value = getIntensity (ff, mask, prelocalmasked, config.frapOffsetX, config.frapOffsetY, config.frapWidth, config.frapHeight);
					ft.bg = background;
					ft.desc = "pre" + i;
					data.add (ft);
				}
				
				
				// activate when enabled in config
				if (config.actEnabled) {
					//Tool.log (-1, "info", "activating " + cell.id);
					robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_activate));
					robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_activate));
					System.out.print ("  act");
					for (int i = 0; i < config.actPulses; i++) {
						System.out.print (" " + i);
						//int offset = 768 / 2 - config.actDim / 2;
						String actremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.actDim + " " + config.actDim);
					}
				}
				
				
				
				// bleach
				robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_bleach));
				robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_bleach));
				System.out.print ("  bl ");
				for (int i = 0; i < config.nbl; i++) {
					System.out.print (" " + i);
					String blremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.frapWidth + " " + config.frapHeight);
					String bllocal = robomick.datapath + well + "/" + cell.id + "_bl" + i + ".tif";
					File ff = robomick.rcm ().do_get_file (blremote, bllocal);
				}
				
				// snap image after bleach when enabled
				if (config.img_afterbl) {
					
					// move back to original pre-prebleach position
					robomick.rcm ().do_cmd ("MOVEXY " + cell.rwx + " " + cell.rwy);
				
					// after bleach image
					robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_image));
					robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_image));
					System.out.print ("  [img-afterbl]");
					String afterbleachremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
					String afterbleachlocal = robomick.datapath + well + "/" + cell.id + "-AFTERBL.tif";
					File cellafterbleach = robomick.rcm ().do_get_file (afterbleachremote, afterbleachlocal);
					
					// move to previous position
					robomick.rcm ().do_cmd ("MOVEXY " + moveX + " " + moveY);
				}
				
				// post bleach
				robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_monitor_act));
				robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_monitor_act));
				long postt0 = System.currentTimeMillis ();
				System.out.print ("  post");
				for (int i = 0; i < config.npost; i++) {
					long tbegin = postt0 + i * config.interval;
					while (System.currentTimeMillis () < tbegin) {
						Thread.sleep (1);
					}
					System.out.print (" " + i);
					
					FrapTimepoint ft = new FrapTimepoint ();
					ft.timestamp = System.currentTimeMillis ();
					
					String postremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.frapWidth + " " + config.frapHeight);
					String postlocal = robomick.datapath + well + "/" + cell.id + "_post" + i + ".tif";
					String postlocalmasked = robomick.datapath + well + "/" + cell.id + "_post" + i + "_masked.tif";
					File ff = robomick.rcm ().do_get_file (postremote, postlocal);
					
					ft.value = getIntensity (ff, mask, postlocalmasked, config.frapOffsetX, config.frapOffsetY, config.frapWidth, config.frapHeight);
					ft.bg = background;
					ft.desc = "post" + i;
					data.add (ft);
					
/*
					// additional activation step each 5 post bleach sycles
					if (config.actAdditionalEnabled && (i % 5 == 0)) {
						System.out.print ("  act+ ");
						robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_activate));
						robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_activate));
						
						String actremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.actDim + " " + config.actDim);
						
						robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_monitor));
						robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_monitor));
					}
*/
					
				}
				
				// move back to original pre-prebleach position
				robomick.rcm ().do_cmd ("MOVEXY " + originalX + " " + originalY);
				
				// snap image at end of frap when enabled
				if (config.img_exit) {
					long iet0 = System.currentTimeMillis ();
					for (int iie = 0; iie < config.n_img_exit; iie++) {
						// delay...
						long tbegin = iet0 + iie * config.interval_img_exit;
						while (System.currentTimeMillis () < tbegin) {
							Thread.sleep (1);
						}
						
						// post frap image
						robomick.rcm ().do_cmd ("SET_CHSELECT " + config.gen_chsel (config.intensity_image));
						robomick.rcm ().do_cmd ("SET_CHINTENSITY " + config.ia2s (config.intensity_image));
						System.out.print ("  [img-exit-" + iie + "]");
						String postfrapremote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE 0 0 " + config.detailDim + " " + config.detailDim);
						String postfraplocal = robomick.datapath + well + "/" + cell.id + "-EXIT-" + iie + ".tif";
						File cellpostf = robomick.rcm ().do_get_file (postfrapremote, postfraplocal);
					}
				}
				System.out.println ();
				
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
		
		double totOdo = Double.parseDouble (robomick.rcm ().do_cmd ("GET_ODO"));
		Tool.log (-1, "info", "total distance xy-stage: " + totOdo + " mm");
	}
}


