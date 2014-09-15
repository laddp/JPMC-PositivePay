/**
 * Change History
 * 
 * carlonc        Modified code to allow for a negative check amount    
 * Aug 22, 2013   Look for comment 082213                       
 */
package com.bottinifuel.ADD_PositivePay;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import com.bottinifuel.jpmc.PositivePay.Check;

/**
 * @author Administrator
 *
 */
public class ParsedCheckRegister {

	private Vector<Check> Checks = new Vector<Check>();
	private SimpleDateFormat InputDateFormat = new SimpleDateFormat("dd-MMM-yy hh:mm a");
	public final String Messages;
	
	public ParsedCheckRegister(InputStream is) throws Exception
	{
        String messages = "";
		try {
			LineNumberReader in = new LineNumberReader(new InputStreamReader(is));
			String line = in.readLine();
			
			String payeeName = null;
			int payeeLine = 0;
			
			BigDecimal runningTotal = new BigDecimal(0);

			boolean debug = false;

			Date checkDate = null;

			while (line != null)
			{
				if (line.length() == 0)
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " is blank");
				}
				else if (line.matches("\\f* \\(P062F2 - 060727\\)                                     CHECK REGISTER REPORT                                  PAGE:\\s*\\d*\\s*"))
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched page header #1"); 
				}
				else if (line.matches("                                                           BOTTINI FUELS                                      \\d{2}-\\w{3}-\\d{2}  \\d{2}:\\d{2} [A|P]M"))
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched page header #2");
					if (checkDate == null)
					{
						String dateStr = line.substring(110);
						checkDate = InputDateFormat.parse(dateStr);
					}
				}
				else if (line.startsWith("         COMPANY:   INVOICE DUE"))
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched parameter line"); 
				}
				else if (line.equals("VND# NAME                         P/T DSCNT"))
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched column header #1"); 
				}
				else if (line.equals("                          INV-DATE PAY-DATE INVOICE#          AMOUNT    DISCOUNT  ADJUSTMENT  NET-AMOUNT LEDGER#          CO  CHECK#"))
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched column header #2"); 
				}
				else if (line.equals("                                TEXT                                           GALLONS              ") )
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched column header #3"); 
				}
				else if (line.equals("          END OF REPORT"))
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched EOF line");
					if (payeeName != null)
						messages = messages + "Payee without matching check, line #" + payeeLine + '\n';
				}
				else if (line.startsWith("                                REF#:"))
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched reference number line"); 
				}
				else if (line.startsWith("          GRAND TOTALS"))
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched grand total line");
					if (payeeName != null)
						messages = messages + "Payee without matching check, line #" + payeeLine + '\n';
					BigDecimal grandTotal = new BigDecimal(line.substring(93).trim());
					if (!grandTotal.equals(runningTotal))
						throw new Exception("Grand total: $" + grandTotal + " does not equal running total: $" + runningTotal);
				}
				else if (line.startsWith("                                "))
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " comment line"); 
				}
				else if (line.matches("\\s*\\d{1,5}\\s.*"))
				{
                    if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched vendor line");
					if (payeeName != null)
						messages = messages + "Payee '" + payeeName + "' without matching check, line #" + payeeLine + '\n';
					payeeName = line.substring(6, 32).trim();
					payeeLine = in.getLineNumber();
				}
				else if (line.startsWith("           VENDOR TOTALS"))
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " Vendor total line");
					BigDecimal checkAmount = new BigDecimal(line.substring(93, 104).trim());
					if (checkAmount.compareTo(BigDecimal.ZERO) > 0)
					    runningTotal = runningTotal.add(checkAmount); 
					String checknum = line.substring(125).trim();
					if (checknum.length() != 0)
					{
						BigInteger checkNumber = new BigInteger(checknum);
						if (payeeName == null)
							throw new Exception("Parse error: no payee for check on line #" + in.getLineNumber());
						Checks.add(new Check(checkDate, payeeName, checkAmount, checkNumber));
					}
					else
					{
						if (checkAmount.compareTo(BigDecimal.ZERO) <= 0)
							messages += "Warning: no check number for zero or negative check to \"" + payeeName + "\" on line #" + in.getLineNumber() + "\n";
					}
					payeeName = null;
				}
				else if (line.startsWith("                         "))
				{
					if (line.endsWith("      "))
					{
					    if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched invoice line");
					}
					else
					{
						if (debug) System.out.println("Line #" + in.getLineNumber() + " Matched invoice / check line");
						BigDecimal checkAmount = new BigDecimal(line.substring(93, 104).trim());
					   	runningTotal = runningTotal.add(checkAmount);
						BigInteger checkNumber = new BigInteger(line.substring(125).trim());
						if (payeeName == null)
							throw new Exception("Parse error: no payee for check on line #" + in.getLineNumber());
						Checks.add(new Check(checkDate, payeeName, checkAmount, checkNumber));
						payeeName = null;
					}
				}
				else if (line.equals("\f"))
				{
					if (debug) System.out.println("Line #" + in.getLineNumber() + " end of file"); 
				}
				else
					throw new Exception("Line #" + in.getLineNumber() + " is unmatched...");
				line = in.readLine();
			}
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
		
		Messages = messages;
	}
	

	public Vector<Check> getChecks() {
		return Checks;
	}

	public static void main(String args[])
	{
		try {
			if (args.length < 1)
			{
				System.out.println("No file argument(s)");
				return;
			}
			for (String arg : args)
			{
				System.out.println(arg);
				ParsedCheckRegister p = new ParsedCheckRegister(new FileInputStream(arg));
				for (Check c : p.Checks)
					System.out.println("\"" + c.PayeeName + "\"," + c.Number + "," + c.Amount);
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e);
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
