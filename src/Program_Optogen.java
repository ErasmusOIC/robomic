import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.*;
import java.awt.Font;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import javax.imageio.*;
import java.nio.file.*;
import java.nio.file.StandardCopyOption.*;
import java.lang.reflect.*;


class Config {
	
	int ibidiSize = 18;  // size of ibidi
//	String [] wells = {"w7", "w1", "w2", "w8", "w9", "w3", "w4", "w5", "w6"};  // wells to process
//	String initWell = "w7";  // well currently positioned above objective
	String [] wells = {"w8"};  // wells to process
	String initWell = "w8";  // well currently positioned above objective
	int cells_per_well = 10;  // number of cells per well
	double mindist = 0.02;  // minimal distance between cells (*7500 to px)
	int max_tiles = 80;  // max nuber of tiles per well to collect cells (may result in less collected cells per well)
	int tileSweep = 1;  // sweep for tile scans
	int detailSweep = 2;  // sweep for detail scans
	int i488 = 6;  // laser intensity for activation in blue
	int i561 = 20;  // laser intensity for imaging in red
	int actPulses = 14;  // numner of activation pulses
	int actDim = 768;  // size of activated region
	
	
//	int nImgPost = 6;  // number of post activation images
	int nImgPost = 1;  // number of post activation images
	int imgPostIval = 10;  // interval of post activation images (s)
	
	
	
	int eval_min_iavg = 115;
	int eval_min_dim = 80;  // min dim of cell for selection
	int eval_max_dim = 220;  // max dim of cell for selection
	double eval_min_score = 0.6;  // minimal score of cell for selection
	double max_int_fraction = 0.2;  // 0.2 -> keep 20% most bright cells
	
	
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
	
	public void toFile (String filename) {
		// write configuration to specified file
		try {
                        PrintWriter pw = new PrintWriter (filename);
                        pw.println (this.asString());
                        pw.close ();
                }
                catch (Exception x) {
                        x.printStackTrace ();
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
	
	
}


public class Program_Optogen extends Program {
	Robomick robomick;
	
	public Program_Optogen (Robomick r) {
		robomick = r;
	}
	
	public void run () throws Exception {
		Config config = new Config ();
		config.toFile (robomick.datapath + "config.txt");
		
		Ibidi ibidi = new Ibidi (config.ibidiSize);
		Smai smai = new Smai ();
		smai.setDebug (true);
		robomick.rcm ().setDebug (true);
		
		robomick.rcm ().do_cmd ("ZEROXY");
		
		boolean objLow = false;
		double zInit = Double.parseDouble (robomick.rcm ().do_cmd ("GET_Z"));
		
		for (String well : config.wells) {
			File wellDir = new File (robomick.datapath + well + "/");
			wellDir.mkdirs ();
			File cropDir = new File (robomick.datapath + well + "/");
			cropDir.mkdirs ();
			
			robomick.rcm ().do_cmd ("SET_SWEEP 1");
			robomick.rcm ().do_cmd ("SET_CHURCHWINDOW 561");
			robomick.rcm ().do_cmd ("SET_CHSELECT 0 0 1 0");
			robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 0 " + config.i561 + " 0");
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
                        
			robomick.rcm ().do_cmd ("SET_SWEEP " + config.tileSweep);
			
			
			
			// set um per image
			double um_per_img = 768 * 0.00013333;
			if (config.tileSweep == 2) {
				um_per_img /= 2;
			}
			
			do {
				// move in spiral around center tile until enough cells found
				//    4 5 6
                                //    3 0 7
                                //    2 1 8
                                //        9
				
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
					System.out.println ("finding focus...");
					robomick.rcm ().do_cmd ("FIND_FOCUS");
				}
				
				// snap tile
                                String remote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
                                String local = wellDir.toString () + "/" + well + "-t" + nTiles + ".tif";
                                File f = robomick.rcm ().do_get_file (remote, local);
				
				// send image to smai and process detection
                                String detlog = wellDir.toString () + "/" + well + "-t" + nTiles + "-detect.log";
                                String id_prefix = "" + well + "-t" + nTiles + "-c";
                                List<Cell> tileCells = smai.smaiTile (f, detlog, offsetX, offsetY, id_prefix);
				
				// make sure cell coords are within range of image (will cause troubles in cropCellFromTile)
                                fixCoords (tileCells, 768);
				
				
				
				
				
				// convert 16-bit tile to 8-bit 
                                String dir = new File (remote).getParent ();
                                String nm = new File (remote).getName ();
                                String remote8bit = dir + "/" + "8bit-" + nm;
                                robomick.rcm ().do_cmd ("CONVERT_16_TO_8BIT " + remote + " " + remote8bit);
                                String local8bit = wellDir.toString () + "/" + well + "-t" + nTiles + "-8bit.tif";
                                File f8 = robomick.rcm ().do_get_file (remote8bit, local8bit);
				
				
				
				
				// make copy of 8bit tile image to mark accepted cells
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
					
					
					// todo: make mask of cropped cell and use masked cell for evaluation
					
					
					// evaluate cropped cell
					boolean cellOkay = evaluateCell (config, tileCells.get (ci), crop);
					if (cellOkay) {
						Tool.log (-1, "debug", "accepting cell " + ci + "\t  cell: " + tileCells.get (ci));
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
							//System.out.println ("BREAK OUT");
							continue;
						}
						
						//System.out.println ("cmp dist between " + i + " (imax=" + wellCells.get (i).iavg + ") and " + j + " (imax=" + wellCells.get (j).iavg + ") ...");
						double dx2 = (wellCells.get (i).rwx - wellCells.get (j).rwx) * (wellCells.get (i).rwx - wellCells.get (j).rwx);
						double dy2 = (wellCells.get (i).rwy - wellCells.get (j).rwy) * (wellCells.get (i).rwy - wellCells.get (j).rwy);
						double dist = Math.sqrt (dx2 + dy2);
						//System.out.println ("dist = " + dist);
						if (dist < config.mindist) {
							System.out.println ("DISTANCE TOO SMALL!");
							System.out.println ("removing cell " + wellCells.get (i));
							System.out.println ("    while keeping cell " + wellCells.get (j));
							wellCells.remove (i);
							nRejected++;
							continue;
						}
					}
				}
				Tool.log (-1, "debug", "rejected " + colRed + nRejected + colReset + " cells due to distance");
				
				Tool.log (-1, "debug", "number of cells in wellCells: " + colYellow + wellCells.size () + colReset + " after " + (nTiles + 1) + " tiles (required: " + (int) (config.cells_per_well / config.max_int_fraction) + ")");
				
                        
                        
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
                        
                        
                        // keep most bright cells only
                        wellCells = wellCells.subList (0, Math.min (config.cells_per_well, wellCells.size ()));
                        
                        Tool.log (-1, "info", "final #cells collected in well (after removal of too dim cells) " + colGreen + well + colReset + ": " + wellCells.size ());
                        
                        
                        // workon: order cells for minimal xy stage movement
                        // traveling salesman problem on steroids
                        // easy solution: order by tile
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
                        	System.out.println (">> cell id = " + c.id + "  markfile = " + markfile);
                        	
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
                                System.out.println ();
                        }
                        else {
                                writeLog (robomick.datapath + "cell.log", "No cells found in well " + well);
                                Tool.log (-1, "info", "no cells found in well " + colGreen + well + colReset);
                        }
                        
                        
                        // set sweep to detail
                        robomick.rcm ().do_cmd ("SET_SWEEP " + config.detailSweep);
                        
                        // prep mask storage
                        //File [] mask = new File [wellCells.size ()];
                        
			double um_per_px = 0.00013333;
                        if (config.detailSweep == 2) {
                                um_per_px /= 2;
                        }
                       
                       
                        int ci = 0;
                        while (ci < wellCells.size ()) {
                        	Cell cell = wellCells.get (ci);
                        	File cellDir = new File (robomick.datapath + cell.id + "/");
				String [] seg = cell.id.split ("-");
				String seg_well = seg [0];
				String seg_tile = seg [1];
				String seg_cell = seg [2];
				String remote, local;
				File f;
                        	
                        	
                        	// move to cell center
				robomick.rcm ().do_cmd ("MOVEXY " + cell.rwx + " " + cell.rwy);
                        	
                        	
                        	
                        	// make one pre image with 561
				robomick.rcm ().do_cmd ("SET_CHURCHWINDOW 561");
				robomick.rcm ().do_cmd ("SET_CHSELECT 0 0 1 0");
				robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 0 " + config.i561 + " 0");
				remote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
				local = robomick.datapath + "" + seg_well + "-" + seg_tile + "-" + seg_cell + "-561-pre.tif";
				f = robomick.rcm ().do_get_file (remote, local);
				robomick.rcm ().alignImage (f, new File (robomick.datapath + "" + seg_well + "-" + seg_tile + "-" + seg_cell + "-561-pre-tr.tif"), 561);
				
				// activate with 488
				robomick.rcm ().do_cmd ("SET_CHURCHWINDOW 488");
				robomick.rcm ().do_cmd ("SET_CHSELECT 0 1 0 0");
				robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 " + config.i488 + " 0 0");
				for (int pi = 0; pi < config.actPulses; pi++) {
					
					int offset = 768 / 2 - config.actDim / 2;
					remote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE " + offset + " " + offset + " " + config.actDim + " " + config.actDim + "");
					local = robomick.datapath + "" + seg_well + "-" + seg_tile + "-" + seg_cell + "-488-" + pi + ".tif";
					f = robomick.rcm ().do_get_file (remote, local);
					robomick.rcm ().alignImage (f, new File (robomick.datapath + "" + seg_well + "-" + seg_tile + "-" + seg_cell + "-488-" + pi + "-tr.tif"), 488);
				}
				
				// make "nImgPost" post image(s) with 561
				robomick.rcm ().do_cmd ("SET_CHURCHWINDOW 561");
				robomick.rcm ().do_cmd ("SET_CHSELECT 0 0 1 0");
				robomick.rcm ().do_cmd ("SET_CHINTENSITY 0 0 " + config.i561 + " 0");
				long ts0 = System.currentTimeMillis ();
				for (int pi = 0; pi < config.nImgPost; pi++) {
					
					// sleep
					long tsb = ts0 + pi * config.imgPostIval * 1000;
					while (System.currentTimeMillis () < tsb) {
						delay (1);
					}
					
					// snap
					remote = robomick.rcm ().do_cmd ("SNAP_IMAGE_TO_FILE");
					local = robomick.datapath + "" + seg_well + "-" + seg_tile + "-" + seg_cell + "-561-post-" + pi + ".tif";
					f = robomick.rcm ().do_get_file (remote, local);
					robomick.rcm ().alignImage (f, new File (robomick.datapath + "" + seg_well + "-" + seg_tile + "-" + seg_cell + "-561-post-" +pi + "-tr.tif"), 561);
					
				}
                        	
                        	ci++;
                        }
                        
                        
                        
                        
                        
                        
                        
		}
		
		
		
                // return to starting position
                double wellX = ibidi.getX (config.wells [0]) - ibidi.getX (config.initWell);
                double wellY = ibidi.getY (config.wells [0]) - ibidi.getY (config.initWell);
                robomick.rcm ().do_cmd ("MOVEXY " + wellX + " " + wellY);
                robomick.rcm ().do_cmd ("MOVEZ " + zInit);
	}
	
	public void fixCoords (List<Cell> tileCells, int tileDim) {
		// sometimes detection file contains values beyond edges of tile, which cause troubles lateron
		// so fix them
		for (int i = 0; i < tileCells.size (); i++) {
			if (tileCells.get (i).x < 0) {
				//Tool.log (-1, "fixcoords", "fixing cell x: " + tileCells.get (i).x + " -> " + 0);
				tileCells.get (i).x = 0;
			}
			
			if (tileCells.get (i).x + tileCells.get (i).w >= tileDim) {
				//Tool.log (-1, "fixCoords", "fixing cell w: " + tileCells.get (i).w + " -> " + (tileDim - tileCells.get (i).x));
				tileCells.get (i).w = tileDim - tileCells.get (i).x;
			}
			
			if (tileCells.get (i).y < 0) {
				//Tool.log (-1, "fixCoords", "fixing cell y: " + tileCells.get (i).y + " -> " + 0);
				tileCells.get (i).y = 0;
			}
			
			if (tileCells.get (i).y + tileCells.get (i).h >= tileDim) {
				//Tool.log (-1, "fixCoords", "fixing cell h: " + tileCells.get (i).h + " -> " + (tileDim - tileCells.get (i).y));
				tileCells.get (i).h = tileDim - tileCells.get (i).y;
			}
		}
	}

	protected boolean distOkay (Cell cell, List<Cell> list, double mindist) {
		if (list.size () == 0) {
			return true;
		}
		
		for (int i = 0; i < list.size (); i++) {
			double dx2 = (cell.rwx - list.get (i).rwx) * (cell.rwx - list.get (i).rwx);
			double dy2 = (cell.rwy - list.get (i).rwy) * (cell.rwy - list.get (i).rwy);
			double dist = Math.sqrt (dx2 + dy2);
			if (dist < mindist) {
				//System.out.println ("!distOkay ;( = " + dist);
				return false;
			}
		}
		return true;
	}
	
	public BufferedImage cropCellFromTile (String tileFile, Cell cell, String trg) {
		BufferedImage tile = null;
		try {
			tile = ImageIO.read (new File (tileFile));
		}
		catch (IOException x) {
			x.printStackTrace ();
		}
		BufferedImage crop = tile.getSubimage (cell.x, cell.y, cell.w, cell.h);
		
		
		// get rid of old databuffer
		BufferedImage crop2 = new BufferedImage (crop.getWidth (), crop.getHeight (), crop.getType ());
		Graphics2D g2 = crop2.createGraphics ();
		g2.drawImage (crop, 0, 0, null);
		g2.dispose ();
		
		
		try {
			ImageIO.write (crop2, "tif", new File (trg));
		}
		catch (Exception x2) {
			x2.printStackTrace ();
		}
		return crop2;
	}
	
	public boolean evaluateCell (Config config, Cell cell, BufferedImage crop) {
		
		short [] pxi = ((DataBufferUShort) crop.getRaster ().getDataBuffer ()).getData ();
		short imin = Short.MAX_VALUE;
		short imax = Short.MIN_VALUE;
		double sum = 0;
		for (int i = 0; i < pxi.length; i++) {
			imin = (short) Math.min (pxi [i], imin);
			imax = (short) Math.max (pxi [i], imax);
			sum += pxi [i];
		}
		
		short iavg = (short) (sum / pxi.length);
		
		
		
		cell.evalRes = "imin=" + imin + " imax=" + imax + " iavg=" + iavg;
		cell.iavg = iavg;
		
		long now = System.currentTimeMillis ();
		
		
		// do tests on iavg
		if (iavg < config.eval_min_iavg) {
			Tool.log (now, "evalcell:reject_iavg<" + config.eval_min_iavg, "  cell: " + cell);
			return false;
		}
		
		
		
		
		// do tests on dim
		if ((cell.w > config.eval_max_dim) || (cell.h > config.eval_max_dim)) {
			Tool.log (now, "evalcell:reject_max-w/h>" + config.eval_max_dim, "  cell: " + cell);
			return false;
		}
		if ((cell.w < config.eval_min_dim) || (cell.h < config.eval_min_dim)) {
			Tool.log (now, "evalcell:reject_min-w/h<" + config.eval_min_dim, "  cell: " + cell);
			return false;
		}
		if (cell.score < config.eval_min_score) {
			Tool.log (now, "evalcell:reject_score<" + config.eval_min_score, "  cell: " + cell);
			return false;
		}
		return true;
	}
	
	protected void writeLog (String logfilename, String msg) {
		BufferedWriter log = null;
		try {
			log = new BufferedWriter (new FileWriter (logfilename, true));
			log.write (msg + "\n");
			log.flush ();
			log.close ();
		}
		catch (IOException x) {
			x.printStackTrace ();
		}
	}
	
	public void delay (int dur) {
		//System.out.println ("sleeping for " + dur + " ms...");
		try {
			Thread.sleep (dur);
		}
		catch (InterruptedException x) {
			x.printStackTrace ();
		}
	}

	public double getIntensity (File image, File mask, String maskedImageFilename, int frapOffsetX, int frapOffsetY, int frapWidth, int frapHeight) {
		// get average intensiy of masked image
		if (!mask.exists ()) {
			System.err.println ("mask not found!");
			return -1;
		}
		
		try {
			// chop mask to dimensions of image
			BufferedImage bm = ImageIO.read (mask);
			int subX = (-frapWidth / 2) + (bm.getWidth () / 2) - frapOffsetX;
			int subY = (-frapHeight / 2) + (bm.getHeight () / 2) + frapOffsetY;
			BufferedImage bms = bm.getSubimage (subX, subY, frapWidth, frapHeight);
			//ImageIO.write (bms, "tif", new File (robomick.datapath + "mask_in_frap.tif"));
			
			
			// get rid of old databuffer
			BufferedImage bms2 = new BufferedImage (frapWidth, frapHeight, bms.getType ());
			Graphics2D g2 = bms2.createGraphics ();
			g2.drawImage (bms, 0, 0, null);
			g2.dispose ();
			
			// TODO: WERKT, MAAR KAN DIT EFFICIENTER???
			
			BufferedImage bi = ImageIO.read (image);
			if (bi.getType () != BufferedImage.TYPE_USHORT_GRAY) {
				System.out.println ("image type = " + bi.getType ());
				System.err.println ("getIntensity : UNSUPPORTED IMAGE TYPE");
			}
			if (bms2.getType () != BufferedImage.TYPE_BYTE_GRAY) {
				System.out.println ("mask  type = " + bms2.getType ());
				System.err.println ("getIntensity : UNSUPPORTED MASK TYPE");
			}
			
			short [] pxi = ((DataBufferUShort) bi.getRaster ().getDataBuffer ()).getData ();
			byte [] pxm = ((DataBufferByte) bms2.getRaster ().getDataBuffer ()).getData ();
			
			
			//  <tmp for debugging? ///
			BufferedImage maskedImage = new BufferedImage (frapWidth, frapHeight, bi.getType ());
			long sum = 0;
			long num = 0;
			for (int i = 0; i < pxi.length; i++) {
				if (pxm [i] != 0) {
					int x = i % frapWidth;
					int y = i / frapWidth;
					maskedImage.setRGB (x, y, bi.getRGB (x, y));  // copy pixel to maskedImage
					sum += pxi [i];
					num++;
				}
			}
			try {  // write maskedImage
				ImageIO.write (maskedImage, "tif", new File (maskedImageFilename));
			}
			catch (Exception x) {
				x.printStackTrace ();
			}
			//  </tmp for debuggoing? ///
			
			
			if (num == 0) {
				return 0;
			}
			
			return (sum / (double) num);
		}
		catch (Exception x) {
			x.printStackTrace ();
		}
		return -1;
	}
	
	
	
	
	
}


