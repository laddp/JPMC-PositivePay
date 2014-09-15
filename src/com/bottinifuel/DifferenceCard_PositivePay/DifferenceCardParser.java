/*
 * Created on Sep 27, 2010 by pladd
 *
 */
package com.bottinifuel.DifferenceCard_PositivePay;

import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import com.bottinifuel.jpmc.PositivePay.Check;

/**
 * @author pladd
 *
 */
public class DifferenceCardParser
{
    private Map<BigInteger, Check> Checks = new LinkedHashMap<BigInteger, Check>();
    private static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private static DecimalFormat numberFormat = (DecimalFormat)DecimalFormat.getNumberInstance();

    public final String MismatchedChecksMessages;
    
    public DifferenceCardParser(InputStream is, boolean skipMismatchedChecks) throws Exception
    {
        dateFormat.setLenient(false);
        numberFormat.setParseBigDecimal(true);

        PDDocument document = PDDocument.load(is);
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        String text = stripper.getText(document);
        LineNumberReader r = new LineNumberReader(new StringReader(text));
        
        List<String> tempChecks = new LinkedList<String>();
        String dateLine = null;
        BigDecimal fileTotal = new BigDecimal(0);
        
        String line = r.readLine();
        while (line != null)
        {
            line = line.trim();
            if (line.startsWith("Input") && !line.equals("Input Approved Pended Check Check/Trace Claim Reimbursement"))
                throw new Exception("Input format changed, line #" + r.getLineNumber());
            if (line.startsWith("Employee") && !line.equals("Employee ID Enrollee Name Date Amount Amount Amount Number Number Method"))
                throw new Exception("Input format changed, line #" + r.getLineNumber());
            if (line.startsWith("XXX-XX-"))
            {
                tempChecks.add(line);
            }
            else if (line.startsWith("Process Date:"))
            {
                dateLine = line;
            }
            else if (line.startsWith("Total"))
            {
                String [] contents = line.split("\\s");
                if (contents.length == 2)
                {
                    // Process section
                    if (dateLine == null)
                        throw new Exception("No Process Date line for Total line #" + r.getLineNumber());
                    if (tempChecks.size() == 0)
                        throw new Exception("No Check lines for Total line #" + r.getLineNumber());

                    BigDecimal sectionTotal = (BigDecimal)numberFormat.parse(contents[1]);
                    BigDecimal runningTotal = new BigDecimal(0);

                    String dateStr = dateLine.substring(14).split("\\s")[0];
                    Date checkDate = dateFormat.parse(dateStr);

                    for (String checkStr : tempChecks)
                    {
                        checkStr = checkStr.substring(12); // strip off EIN
                        
                        // strip off notaion prefix(es) 
                        while (checkStr.charAt(0) == 'e' ||
                               checkStr.charAt(0) == 'r' ||
                               checkStr.charAt(0) == 'd')
                            checkStr = checkStr.substring(1);
                        
                        // figure out payee name
                        String nameStr = checkStr.split("\\d")[0].trim();
                        int commaIndex = nameStr.indexOf(',');
                        String lastName = nameStr.substring(0,commaIndex).trim();
                        String firstName = nameStr.substring(commaIndex + 1).trim();
                        
                        String dataStr = checkStr.substring(nameStr.length()).trim();
                        String [] data = dataStr.split("\\s");
                        
                        int checkPos    = 6;
                        int checkNumPos = 4;
                        int amountPos   = 3;
                        int itemPos     = 1;
                        
                        if (data.length == 9)
                        {
                        	checkPos    += 2;
                        	checkNumPos += 1;
                        	amountPos   += 1;
                        }
                        else if (data.length != 7)
                            throw new Exception("Parse error - incorrect number of check items, line #" + r.getLineNumber());
                        
                        if (data[checkPos].equals("Check"))
                        {
                            BigInteger checkNum = new BigInteger(data[checkNumPos]);
                            BigDecimal amount = (BigDecimal)numberFormat.parse(data[amountPos]);
                            BigDecimal item   = (BigDecimal)numberFormat.parse(data[itemPos]);
                            runningTotal = runningTotal.add(item);
                            if (!Checks.containsKey(checkNum))
                            {
                                Check check = new Check(checkDate, firstName + " " + lastName, amount, checkNum);
                                check.Items.add(item);
                                Checks.put(checkNum, check);
                            }
                            else
                            {
                            	Checks.get(checkNum).Items.add(item);
                            }
                        }
                    }
                    
                    if (runningTotal.compareTo(sectionTotal) != 0)
                        throw new Exception("Section total mismatch, line #" + r.getLineNumber() + "\n" +
                                            "\tExpecting: " + sectionTotal + "\n" +
                                            "\tGot:       " + runningTotal);
                    else
                        fileTotal = fileTotal.add(sectionTotal);

                    dateLine = null;
                    tempChecks.clear();
                }
                else if (contents.length == 3)
                {
                    // File Total
                    BigDecimal total = (BigDecimal)numberFormat.parse(contents[2]);
                    if (fileTotal.compareTo(total) != 0)
                        throw new Exception("File total mismatch, line #" + r.getLineNumber() + "\n" +
                                            "\tExpecting: " + total + "\n" +
                                            "\tGot:       " + fileTotal);
                }
            }
            line = r.readLine();
        }
        document.close();
        
        String mismatchMessages = "";
        
        for (Check ck : Checks.values())
        {
        	if (!ck.VerifyItemTotals())
        	{
        		String message = "Check #" + ck.Number + " item totals mismatch.\n" +
        				"\tExpecting: " + ck.Amount + "\n" +
        				"\tGot:       " + ck.ItemTotal();
        		if (skipMismatchedChecks)
        			mismatchMessages += message;
        		else
        			throw new Exception(message);
        	}
        }
        
        MismatchedChecksMessages = mismatchMessages;
    }

    public Collection<Check> getChecks()
    {
        return Checks.values();
    }
}
