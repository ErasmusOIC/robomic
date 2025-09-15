import java.text.DecimalFormat;

public abstract class Program {
	public Program () {
		String program = this.getClass ().getName ();
		System.out.println ("[run program] " + program);
		
		
		
	}
	
	public abstract void run () throws Exception;
	
	protected static final DecimalFormat df = new DecimalFormat ("0.00000");
	protected static final String colRed = "\u001B[91m";
	protected static final String colGreen = "\u001B[92m";
	protected static final String colYellow = "\u001B[93m";
	protected static final String colReset = "\u001B[0m";
	
}




