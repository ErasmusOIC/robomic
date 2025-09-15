import java.text.DecimalFormat;

public class Cell {
	String id;
	double score;
	int x, y;
	int w, h;
	double rwx, rwy;
	String evalRes = "";
	int iavg = -1;
	
	@Override
	public String toString () {
		DecimalFormat df = new DecimalFormat ("0.00000");
		String rwxf = df.format (rwx);
		String rwyf = df.format (rwy);
		return "{Cell :: id=" + id + " score=" + score + " x=" + x + " y=" + y + " w=" + w + " h=" + h + " rwx=" + rwxf + " rwy=" + rwyf + " evalRes=" + evalRes + " iavg=" + iavg + "}";
	}
}







