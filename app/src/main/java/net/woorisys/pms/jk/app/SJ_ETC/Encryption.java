package net.woorisys.pms.jk.app.SJ_ETC;

import android.util.Base64;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encryption {

    public byte[] UTF16LE(String Password)
    {
        byte[] UTF16Password;

        try
        {
            UTF16Password=(new String(Password.getBytes(), "utf-8")).getBytes("utf-16le");
        }
        catch (IOException e)
        {
            e.getMessage();
            UTF16Password=null;
        }

        return UTF16Password;
    }

    public byte[] SHA256PASSWORD(byte[] Password)
    {
        byte[] SHPPassword=null;

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Password);
            SHPPassword=digest.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.getMessage();
        }

        return  SHPPassword;
    }

    public String BASE64PASSWORD(byte[] Password)
    {
        String Base64Password=null;

        Base64Password= Base64.encodeToString(Password,Base64.DEFAULT);

        return Base64Password;
    }
}
