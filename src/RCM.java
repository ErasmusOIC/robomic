import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import javax.imageio.*;

public class RCM {
	public RCM () {
	}
	
	public void setDebug (boolean d) {
		debug = d;
	}
	
        private Socket open () throws Exception {
                try {
                        Socket s = new Socket ();
                        s.connect (new InetSocketAddress (host, port), 1000);
                        return s;
                }
                catch (Exception x) {
                        System.err.println ("Client.open :: failed to connect server");
                        
                        try {
                        	System.out.println ("waiting 3 seconds and try again...");
                        	Thread.sleep (3000);
                        	return open ();
                        }
                        catch (InterruptedException ix) {
                        	ix.printStackTrace ();
                        }
                        
                        //x.printStackTrace ();
                }
                return null;
        }
        
        private void close (Socket s) throws Exception {
                s.close ();
        }
        
        private DataInputStream getDataInputStream (Socket s) throws IOException {
                return new DataInputStream (s.getInputStream ());
        }
        
        private DataOutputStream getDataOutputStream (Socket s) throws IOException {
                return new DataOutputStream (s.getOutputStream ());
        }
        
        private String recvMsg (DataInputStream dis) throws IOException {
                byte [] len = new byte [1];
                dis.read (len, 0, 1);
                
                byte [] buffer = new byte [len [0]];
                dis.read (buffer, 0, len [0]);
                
                String str = new String (buffer, StandardCharsets.UTF_8);
                if (debug) Tool.log (-1, "RCM::recvMsg", str);
                return str;
        }
        
        private void sendMsg (DataOutputStream dos, String msg) throws IOException {
                byte [] bytes = msg.getBytes (StandardCharsets.UTF_8);
                
                if (bytes.length > 255) {
                        System.err.println ("sendMsg error :: length of msg > 255");
                        System.exit (1);
                }
                
                byte [] len = new byte [1];
                len [0] = (byte) bytes.length;
                
                if (debug) Tool.log (-1, "RCM::sendMsg", msg);
                dos.write (len, 0, 1);
                dos.write (bytes, 0, len [0]);
        }
        
        private void recvFile (DataInputStream dis, String localfilename) throws IOException {
                String msg;
                
                // receive file name
                msg = recvMsg (dis);
                String remotefilename = msg;
                
                // receive file length
                msg = recvMsg (dis);
                int filelength = Integer.parseInt (msg);
                
                // receive file data
                FileOutputStream fos = new FileOutputStream (localfilename);
                BufferedOutputStream bos = new BufferedOutputStream (fos);
                
                if (debug) Tool.log (-1, "RCM::recvFile", remotefilename + " -> " + localfilename + " (length=" + filelength + ")");
                
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
        
        private void sendFile (DataOutputStream dos, String localfilename) throws IOException {
                File f = new File (localfilename);
                long len = f.length ();
                String lenstr = "" + len;
                
                if (debug) Tool.log (-1, "RCM::sendFile", localfilename + " (length=" + len + ")");
                
                // send file name
                sendMsg (dos, localfilename);
                
                // send file length
                sendMsg (dos, lenstr);
                
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
                //bis.close ();
        }
        
        public String do_cmd (String cmd) throws Exception {
                Socket s = open ();
                DataInputStream dis = getDataInputStream (s);
                DataOutputStream dos = getDataOutputStream (s);
                sendMsg (dos, cmd);
                String reply = recvMsg (dis);
                close (s);
                return reply;
        }
        
        public File do_get_file (String remotefilename) throws Exception {
        	String localfilename = "recv-" + System.currentTimeMillis ();
        	return do_get_file (remotefilename, localfilename);
        }
        
        public File do_get_file (String remotefilename, String localfilename) throws Exception {
		// funny fact: returned value is same as localfilename argument, but as File
                String cmd = "GET_FILE " + remotefilename;
                Socket s = open ();
                DataInputStream dis = getDataInputStream (s);
                DataOutputStream dos = getDataOutputStream (s);
                sendMsg (dos, cmd);
                //String localfilename = "recv-" + System.currentTimeMillis ();
                recvFile (dis, localfilename);
                close (s);
                return new File (localfilename);
        }
        
	private BufferedImage transform (BufferedImage bi, double [][] mx) {
		BufferedImage tr = new BufferedImage (bi.getWidth (), bi.getHeight (), bi.getType ());
		for (int y = 0; y < bi.getHeight (); y++) {
			for (int x = 0; x < bi.getWidth (); x++) {
				double srcx = x * mx[0][0] + y * mx[0][1] + mx [0][2];
				double srcy = x * mx[1][0] + y * mx[1][1] + mx [1][2];
				double srcz = x * mx[2][0] + y * mx[2][1] + mx [2][2];
				int d = 1;
				int x2d = (int)(srcx * d / srcz);
				int y2d = (int)(srcy * d / srcz);
				if (x2d >= 0 && x2d < tr.getWidth () && y2d >= 0 && y2d < tr.getHeight ()) {
					tr.setRGB (x, y, bi.getRGB (x2d, y2d));
				}
			}
		}
		return tr;
	}
	
	private BufferedImage readImage (File f) {
		try {
			BufferedImage bi = ImageIO.read (f);
			return bi;
		}
		catch (IOException x) {
			x.printStackTrace ();
		}
		return null;
	}
	
	private void writeImage (BufferedImage bi, String type, File f) {
		try {
			ImageIO.write (bi, type, f);
		}
		catch (IOException x) {
			x.printStackTrace ();
		}
	}
	
        public void alignImage (File f_in, File f_out, int ch) {
        	if (ch == 405) {
        		System.out.println ("RCM.alignImage :: nothing to do for ch " + ch);
        		return;
        	}
        	else {
        		if (ch == 488) {
        			double [][] mx = {
        				{1.00005, 0, -0.50002},
        				{0 ,1, -9},
        				{0, 0, 1}
        			};
        			BufferedImage tr = transform (readImage (f_in), mx);
        			writeImage (tr, "tif", f_out);
        		}
        		else if (ch == 561) {
        			double [][] mx = {
        				{0.98039, 0, 1.96078},
        				{0 ,1, 8},
        				{0, 0, 1}
        			};
        			BufferedImage tr = transform (readImage (f_in), mx);
        			writeImage (tr, "tif", f_out);
        		}
        		else if (ch == 640) {
        			double [][] mx = {
        				{1, 0, 0},  // todo: when laser is fixed
        				{0 ,1, 0},
        				{0, 0, 1}
        			};
        			BufferedImage tr = transform (readImage (f_in), mx);
        			
        			System.out.println ("RCM.alignImage :: no transformation matrix for 640 since laser is broken");
        			return;
        		}
        		else {
        			System.out.println ("RCM.alignImage :: invalid ch: " + ch);
        			return;
        		}
        	}
        	
        }
        
        
        
	private final static String host = "oic-rcm-1454.erasmusmc.nl";
	private final static int port = 55557;
	private boolean debug = true;
}

