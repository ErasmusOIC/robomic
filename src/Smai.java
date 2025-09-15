import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.*;


public class Smai {
	private String host = "oic-toomuch.erasmusmc.nl";
	private int port = 58974;
	private Socket s = null;
	private boolean debug = true;
	
	public Smai () {
	}
	
	public void setDebug (boolean d) {
		debug = d;
	}
	
	public java.util.List<Cell> smaiTile (File tile, String trg, double offsetX, double offsetY, String id_prefix) {
		open ();
		
		DataInputStream dis = getDataInputStream ();
		DataOutputStream dos = getDataOutputStream ();
		
		// send job type
		sendMsg (dos, "JOB_TILE");
		
		// send uid
		// todo: move to Robomick constructor
		String uid = Tool.rndString ("0123456789abcdef", 8);
		sendMsg (dos, uid);
		
		// send image file
		sendFile (dos, tile.toString ());
		
		// recv text file
		recvFile (dis, trg);
		
		close ();
		
		
		// parse detect.txt
		java.util.List<Cell> detectedCells = parseDetect (trg, offsetX, offsetY, id_prefix);
		
		return detectedCells;
	}
	
	public Point smaiDetail (File tile, File mask, double offsetX, double offsetY, String id_prefix) {
		open ();
		
		DataInputStream dis = getDataInputStream ();
		DataOutputStream dos = getDataOutputStream ();
		
		// send job type
		sendMsg (dos, "JOB_DETAIL");
		
		// send uid
		// todo: move to Robomick constructor
		String uid = Tool.rndString ("0123456789abcdef", 8);
		sendMsg (dos, uid);
		
		// send image file
		sendFile (dos, tile.toString ());
		
		// recv detres
		String detres = recvMsg (dis);
		//System.out.println ("detres = " + detres);
		
		int x = -1;
		int y = -1;
		if (!detres.equals ("-1")) {
			// recv mask file
			recvFile (dis, mask.toString ());
			
			// recv centermost cell coord
			String coordstr = recvMsg (dis);
			String [] seg = coordstr.split (",");
			
			x = Integer.parseInt (seg [0]);
			y = Integer.parseInt (seg [1]);
		}
		close ();
		return new Point (x, y);
	}
	
	public void tileMarkDetections (File tilefile, java.util.List<Cell> cells, File markfile) {
		try {
			BufferedImage bi = ImageIO.read (tilefile);
			Graphics2D g2 = bi.createGraphics ();
			g2.setFont (new Font ("Serif", Font.PLAIN, 12));
			g2.setColor (Color.WHITE);
			
			for (Cell c : cells) {
				g2.drawRect (c.x, c.y, c.w, c.h);
				g2.drawString ("" + c.id, c.x + 2, c.y + 12);
			}
			g2.dispose ();
			
			ImageIO.write (bi, "tif", markfile);
		}
		catch (Exception x) {
			x.printStackTrace ();
		}
	}
	
	private java.util.List<Cell> parseDetect (String path, double offsetX, double offsetY, String id_prefix) {
		java.util.List<Cell> dc = new ArrayList<> ();
		try {
			BufferedReader br = new BufferedReader (new FileReader (path));
			String line;
			do {
				line = br.readLine ();
				if (line != null) {
					String [] seg = line.split ("\t");
					
					Cell c = new Cell ();
					c.id = id_prefix + seg [1];
					c.score = Double.parseDouble (seg [2]);
					c.x = Integer.parseInt (seg [3]);
					c.y = Integer.parseInt (seg [4]);
					c.w = Integer.parseInt (seg [5]) - c.x;
					c.h = Integer.parseInt (seg [6]) - c.y;
					c.rwx = offsetX - (c.x + c.w / 2 - 384) * 0.00013333;
					c.rwy = offsetY + (c.y + c.h / 2 - 384) * 0.00013333;
					dc.add (c);
				}
			} while (line != null);
			br.close ();
		}
		catch (Exception x) {
			x.printStackTrace ();
		}
		return dc;
	}
	
	private void open () {
		try {
			s = new Socket ();
			s.connect (new InetSocketAddress (host, port), 1000);
		}
		catch (Exception x) {
			System.out.println ("Smai.open :: can't connect server");
			System.out.println ("@oic-toomuch check: systemctl status smai-stardist");
			System.out.println ("@oic-toomuch updated python recently: python3 -m pip install stardist --break-system-packages");
			
			x.printStackTrace ();
			System.exit (1);
		}
	}
	
	private void close () {
		try {
			s.close ();
		}
		catch (Exception x) {
			x.printStackTrace ();
		}
	}
	
	private DataInputStream getDataInputStream () {
		try {
			return new DataInputStream (s.getInputStream ());
		}
		catch (IOException x) {
			x.printStackTrace ();
		}
		return null;
	}
	
	private DataOutputStream getDataOutputStream () {
		try {
			return new DataOutputStream (s.getOutputStream ());
		}
		catch (IOException x) {
			x.printStackTrace ();
		}
		return null;
	}
	
	private String recvMsg (DataInputStream dis) {
		try {
			byte [] len = new byte [1];
			dis.read (len, 0, 1);
			
			byte [] buffer = new byte [len [0]];
			dis.read (buffer, 0, len [0]);
			
			String msg = new String (buffer, StandardCharsets.UTF_8);
			if (debug) Tool.log (-1, "Smai::recvMsg", msg);
			return msg;
		}
		catch (IOException x) {
			x.printStackTrace ();
		}
		return null;
	}
	
	private void sendMsg (DataOutputStream dos, String msg) {
		byte [] bytes = msg.getBytes (StandardCharsets.UTF_8);
		
		if (bytes.length > 255) {
			System.out.println ("Smai.sendMsg :: msg > 255 chars");
			System.exit (1);
		}
		
		byte [] len = new byte [1];
		len [0] = (byte) bytes.length;
		
		try {
			if (debug) Tool.log (-1, "Smai::sendMsg ", msg);
			dos.write (len, 0, 1);
			dos.write (bytes, 0, len [0]);
		}
		catch (IOException x) {
			x.printStackTrace ();
		}
	}
	
	private void recvFile (DataInputStream dis, String localfilename) {
		String msg;
		
		// make sure dir exists
		File parent = new File (localfilename).getParentFile ();
		if (parent != null) {
			parent.mkdirs ();
		}
		
		// recv filename
		msg = recvMsg (dis);
		String remotefilename = msg;
		
		// recv file length
		msg = recvMsg (dis);
		int filelength = Integer.parseInt (msg);
		
		try {
			// recv file data
			FileOutputStream fos = new FileOutputStream (localfilename);
			BufferedOutputStream bos = new BufferedOutputStream (fos);
			
			if (debug) Tool.log (-1, "Smai::recvFile", remotefilename + " -> " + localfilename + " (length=" + filelength + ")");
			
			int sum = 0;
			int size = 1024;
			do {
				byte [] buffer = new byte [size];
				size = Math.min (size, filelength - sum);
				int bytes = dis.read (buffer, 0, size);
				sum += bytes;
				bos.write (buffer, 0, bytes);
				
			} while (sum < filelength);
			bos.close ();
		}
		catch (IOException x) {
			x.printStackTrace ();
		}
	}
	
	private void sendFile (DataOutputStream dos, String localfilename) {
		File f = new File (localfilename);
		long len = f.length ();
		String lenstr = "" + len;
		
		if (debug) Tool.log (-1, "Smai::sendFile", localfilename + " (length=" + len + ")");
		
		// send filename
		sendMsg (dos, localfilename);
		
		// send file length
		sendMsg (dos, lenstr);
		
		try {
			// send file data
			BufferedInputStream bis = new BufferedInputStream (new FileInputStream (f));
			int sum = 0;
			int size = 1024;
			do {
				byte [] buffer = new byte [size];
				int bytes = bis.read (buffer, 0, size);
				sum += bytes;
				dos.write (buffer, 0, bytes);
			} while (sum < len);
		}
		catch (IOException x) {
			x.printStackTrace ();
		}
	}
	
}







