/*
 * Created on Sep 27, 2010 by pladd
 *
 */
package com.bottinifuel.DifferenceCard_PositivePay;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Vector;

import com.bottinifuel.jpmc.PositivePay.Check;
import com.bottinifuel.jpmc.PositivePay.PositivePayWriter;

/**
 * @author pladd
 *
 */
public class DifferenceCard_PositivePay
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy"); 
        if (args.length < 2)
        {
            System.out.println("No file argument(s)");
            return;
        }
        try {
            PositivePayWriter payWriter = new PositivePayWriter(new FileOutputStream(args[0], false));

            Vector<String> inputFiles = new Vector<String>(Arrays.asList(args));
            inputFiles.removeElementAt(0);
            
            for (String arg : inputFiles)
            {
                DifferenceCardParser p = new DifferenceCardParser(new FileInputStream(arg), false);
                BigDecimal total = payWriter.WriteChecks(new BigDecimal("885782102"), p.getChecks());
                System.out.println("Processed register file: " + arg);
                System.out.println("\tTotal # Items:  " + p.getChecks().size());
                System.out.println("\tTotal Amount:  " + DecimalFormat.getCurrencyInstance().format(total));
                
                System.out.println("\nItem Summary:");
                System.out.println("===========================================================");
                System.out.println("Date        Num\tAmount\tPayee");
                System.out.println("-----------------------------------------------------------");
                for (Check check : p.getChecks())
                {
                    System.out.println(df.format(check.CheckDate) + "  " +
                                       check.Number + "\t" +
                                       "$" + check.Amount + "\t" +
                                       check.PayeeName);
                }
                System.out.println("===========================================================");
            }

            System.out.println("\n\nOutput file: " + args[0]);
            System.out.println("\tTotal # Items:  " + payWriter.getItemCount());
            System.out.println("\tTotal Amount:  " + DecimalFormat.getCurrencyInstance().format(payWriter.getTotal()));
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
