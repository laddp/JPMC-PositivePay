/**
 * 
 */
package com.bottinifuel.jpmc.PositivePay;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Check
{
    public final Date CheckDate;
	public final String PayeeName;
	public final BigDecimal Amount;
	public final BigInteger Number;
	
	public List<BigDecimal> Items = new LinkedList<BigDecimal>();
	
	public Check(Date date, String payee, BigDecimal amt, BigInteger num)
	{
	    CheckDate = date;
		PayeeName = payee;
		Amount = amt;
		Number = num;
	}
	
	public BigDecimal ItemTotal()
	{
		BigDecimal total = new BigDecimal(0);
		for (BigDecimal item : Items)
		{
			total = total.add(item);
		}
		return total;
	}
	
	public boolean VerifyItemTotals()
	{
		
		return ItemTotal().equals(Amount);
	}
}