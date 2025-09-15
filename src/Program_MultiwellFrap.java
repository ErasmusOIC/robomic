import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import javax.imageio.*;

class FrapTimepoint {
	public long timestamp;
	public double value;
	public double bg;
	public String desc;
	
	@Override public String toString () {
		return "{timestamp: " + timestamp + ", value: " + value + ", bg: " + bg + ", desc: " + desc + "}";
	}
}


public abstract class Program_MultiwellFrap extends Program {
	Robomick robomick;
	
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
		//System.out.println ("distOkay ;o)");
		return true;
	}
	
	protected double getBackground (File file) {
		// get background intensity of image by randomly chosing some small squares, calculate avg intenisty in each square and return minimum of all squares
		Random rnd = new Random ();
		
		BufferedImage image = null;
		try {
			image = ImageIO.read (file);
		}
		catch (Exception x) {
			x.printStackTrace ();
			return -1;
		}
		
		int nbgscan = 10;  // number of bg scan squares
		int scandim = 16;  // dimension of squares
		
		int minscandim = Math.min (image.getWidth (), image.getHeight ()) - 1;
		scandim = Math.min (scandim, minscandim);
		
		double min = Double.MAX_VALUE;
		for (int i = 0; i < nbgscan; i++) {
			// chose random square in image
			int x = rnd.nextInt (image.getWidth () - scandim);
			int y = rnd.nextInt (image.getHeight () - scandim);
			BufferedImage squarex = image.getSubimage (x, y, scandim, scandim);
			BufferedImage square = new BufferedImage (scandim, scandim, squarex.getType ());
			Graphics2D g2 = square.createGraphics ();
			g2.drawImage (squarex, 0, 0, null);
			g2.dispose ();
			
			
			//// tmp write square
			//try {
			//	ImageIO.write (square, "tif", new File (robomick.datapath + System.currentTimeMillis () + "-square-" + i + ".tif"));
			//}
			//catch (Exception x2) {
			//	x2.printStackTrace ();
			//}
			
			
			// calc avg intensity in square
			short [] pxi = ((DataBufferUShort) square.getRaster ().getDataBuffer ()).getData ();
			double sum = 0;
			for (int j = 0; j < pxi.length; j++) {
				sum += pxi [j];
			}
			double avg = sum / (double) pxi.length;
			min = Math.min (min, avg);
		}
		return min;
	}
	
	protected double getIntensity (File image, File mask, String maskedImageFilename, int frapOffsetX, int frapOffsetY, int frapWidth, int frapHeight) {
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
	
	protected void delay (int dur) {
		//System.out.println ("sleeping for " + dur + " ms...");
		try {
			Thread.sleep (dur);
		}
		catch (InterruptedException x) {
			x.printStackTrace ();
		}
	}
	
	
	public void fixCoords (List<Cell> tileCells, int tileDim) {
		// sometimes detection file contains values beyond edges of tile, which cause troubles lateron
		// so fix them
		for (int i = 0; i < tileCells.size (); i++) {
			if (tileCells.get (i).x < 0) {
				Tool.log (-1, "fixcoords", "fixing cell x: " + tileCells.get (i).x + " -> " + 0);
				tileCells.get (i).x = 0;
			}
			
			if (tileCells.get (i).x + tileCells.get (i).w >= tileDim) {
				Tool.log (-1, "fixCoords", "fixing cell w: " + tileCells.get (i).w + " -> " + (tileDim - tileCells.get (i).x));
				tileCells.get (i).w = tileDim - tileCells.get (i).x;
			}
			
			if (tileCells.get (i).y < 0) {
				Tool.log (-1, "fixCoords", "fixing cell y: " + tileCells.get (i).y + " -> " + 0);
				tileCells.get (i).y = 0;
			}
			
			if (tileCells.get (i).y + tileCells.get (i).h >= tileDim) {
				Tool.log (-1, "fixCoords", "fixing cell h: " + tileCells.get (i).h + " -> " + (tileDim - tileCells.get (i).y));
				tileCells.get (i).h = tileDim - tileCells.get (i).y;
			}
		}
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
		
		// do tests
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
	
}


