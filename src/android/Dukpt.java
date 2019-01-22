package android;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Dukpt {
    public static byte[] getKey(byte[] key, byte[] KSN, long EncCnt) {
        byte i, j;
        byte[] enc_out = null;
        byte[] din = new byte[16];
        long SR, SSR;
        boolean doAes;

        SR = 0x100000;
        SSR = 0;
        doAes = (EncCnt & SR) != 0;

        for (i = 0; i < 16; i++)
            din[i] = i;

        for (j = 0; j < 21; j++) {
            if (doAes) {
                SSR = SSR | SR;

                for (i = 0; i < 6; i++)
                    din[i] = (byte) 0xff;
                for (i = 6; i < 13; i++)
                    din[i] = KSN[i - 6];

                din[13] = (byte) ((KSN[7] & 0xe0) | ((SSR >> 16) & 0x1f));
                din[14] = (byte) ((SSR >> 8) & 0xff);
                din[15] = (byte) ((SSR >> 0) & 0xff);

                try {
                    enc_out = aesEncB(din, key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (i = 0; i < 16; i++) {
                    // (enc_out[i*4+0]<<24) | (enc_out[i*4+1]<<16) | (enc_out[i*4+2]<<8) | (enc_out[i*4+3]<<0) ;
                    key[i] = enc_out[i];
                }
            }
            SR = SR >> 1;
            doAes = (EncCnt & SR) != 0;
        }
        for (i = 0; i < 16; i++) {
            // (enc_out[i*4+0]<<24) | (enc_out[i*4+1]<<16) | (enc_out[i*4+2]<<8) | (enc_out[i*4+3]<<0) ;
            key[i] = enc_out[i];
        }
        return key;
    }

    private static byte[] aesEncB(byte[] data, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        Key keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(data);
        return encrypted;
    }
}
