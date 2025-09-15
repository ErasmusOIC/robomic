import java.util.*;

class DPoint {
	double x, y;
	public DPoint (double _x, double _y) {
		x = _x;
		y = _y;
	}
}

public class Ibidi {
	int size;
	Map<String, DPoint> wellpos = new HashMap<> ();
	
	public Ibidi (int _size) {
		size = _size;
		
		prepPos ();
	}
	
	private void prepPos () {
		if (size == 4) {  // for testing purposes only
			wellpos.put ("w1", new DPoint (0.0, 0.0));
			wellpos.put ("w2", new DPoint (-2.0, 0.0));
			wellpos.put ("w3", new DPoint (0.0, 2.0));
			wellpos.put ("w4", new DPoint (-2.0, 2.0));
		}
		else if (size == 8) {  // 8-well ibidi
			wellpos.put ("w1", new DPoint (0.0, 0.0));
			wellpos.put ("w2", new DPoint (-12.4, 0.0));
			wellpos.put ("w3", new DPoint (-25.4, 0.0));
			wellpos.put ("w4", new DPoint (-37.7, 0.0));
			wellpos.put ("w5", new DPoint (0.0, 10.6));
			wellpos.put ("w6", new DPoint (-12.4, 10.6));
			wellpos.put ("w7", new DPoint (-25.4, 10.6));
			wellpos.put ("w8", new DPoint (-37.7, 10.6));
		}
		else if (size == 18) {  // 18-well ibidi
			wellpos.put ("w1", new DPoint (0.0, 0.0));
			wellpos.put ("w2", new DPoint (-8.0, 0.0));
			wellpos.put ("w3", new DPoint (-16.0, 0.0));
			wellpos.put ("w4", new DPoint (-24.0, 0.0));
			wellpos.put ("w5", new DPoint (-32.0, 0.0));
			wellpos.put ("w6", new DPoint (-40.0, 0.0));
			wellpos.put ("w7", new DPoint (0.0, 6.0));
			wellpos.put ("w8", new DPoint (-8.0, 6.0));
			wellpos.put ("w9", new DPoint (-16.0, 6.0));
			wellpos.put ("w10", new DPoint (-24.0, 6.0));
			wellpos.put ("w11", new DPoint (-32.0, 6.0));
			wellpos.put ("w12", new DPoint (-40.0, 6.0));
			wellpos.put ("w13", new DPoint (0.0, 12.0));
			wellpos.put ("w14", new DPoint (-8.0, 12.0));
			wellpos.put ("w15", new DPoint (-16.0, 12.0));
			wellpos.put ("w16", new DPoint (-24.0, 12.0));
			wellpos.put ("w17", new DPoint (-32.0, 12.0));
			wellpos.put ("w18", new DPoint (-40.0, 12.0));
		}
		else {
			System.out.println ("Ibidi.prepPos :: unsupported size: " + size);
		}
	}
	
	public double getX (String well) {
		return wellpos.get (well).x;
	}
	
	public double getY (String well) {
		return wellpos.get (well).y;
	}
	
}







