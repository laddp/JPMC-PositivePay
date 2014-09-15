/**
 * 
 */
package com.bottinifuel.jpmc.PositivePay;

import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;

/**
 * @author Administrator
 *
 */
public class PositivePayWriter {

    private PrintStream out;
    private int        ItemCount = 0;
    private BigDecimal Total = new BigDecimal(0);
	private static SimpleDateFormat OutputDateFormat = new SimpleDateFormat("MMddyy");
	
	public PositivePayWriter(OutputStream os)
	{
	    out = new PrintStream(os);
	}
	
	public BigDecimal WriteChecks(BigDecimal acctNum, Collection<Check> checks)
	{
	    BigDecimal runTotal = new BigDecimal(0.0);
		
		for (Check c : checks)
		{
			out.print("I," + acctNum + "," +
			          c.Number + "," +
			          OutputDateFormat.format(c.CheckDate) + "," +
			          c.Amount + ",,");
			runTotal = runTotal.add(c.Amount);
			if (c.PayeeName.contains(","))
				out.println("\"" + c.PayeeName + "\",");
			else
				out.println(c.PayeeName + ",");
		}
		
		Total = Total.add(runTotal);
		ItemCount += checks.size();
		return runTotal;
	}

    public int getItemCount()
    {
        return ItemCount;
    }

    public BigDecimal getTotal()
    {
        return Total;
    }
}
