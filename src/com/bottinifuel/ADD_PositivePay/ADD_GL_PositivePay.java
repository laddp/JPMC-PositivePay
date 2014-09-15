/*
 * Created on Sep 27, 2010 by pladd
 *
 */
package com.bottinifuel.ADD_PositivePay;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Vector;

import com.bottinifuel.jpmc.PositivePay.PositivePayWriter;

/**
 * @author pladd
 *
 */
public class ADD_GL_PositivePay
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
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
                ParsedCheckRegister p = new ParsedCheckRegister(new FileInputStream(arg));
                BigDecimal total = payWriter.WriteChecks(new BigDecimal("885782920"), p.getChecks());
                System.out.println("Processed register file: " + arg);
                System.out.println("\tTotal # Items:  " + p.getChecks().size());
                System.out.println("\tTotal Amount:  " + DecimalFormat.getCurrencyInstance().format(total));
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
