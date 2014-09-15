/*
 * Created on Oct 1, 2010 by pladd
 *
 */
package com.bottinifuel.ADD_PositivePay;

import net.sf.vfsjfilechooser.filechooser.AbstractVFSFileFilter;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;

/**
 * @author pladd
 *
 */
public class PrefixFileFilter extends AbstractVFSFileFilter
{
    public final String Prefix;
    
    public PrefixFileFilter(String prefix)
    {
        Prefix = prefix;
    }
    
    
    @Override
    public boolean accept(FileObject arg0)
    {
        try {
            if (arg0.getType() == FileType.FOLDER)
                return true;
        }
        catch (FileSystemException e)
        {
            return false;
        }
        if (arg0.getName().getBaseName().startsWith(Prefix))
            return true;
        else
            return false;
    }


    @Override
    public String getDescription()
    {
        return "Prefix: " + Prefix;
    }

}
