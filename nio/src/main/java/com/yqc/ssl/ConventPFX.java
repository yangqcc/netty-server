package com.yqc.ssl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

/**
 * @author yangqc
 */
public class ConventPFX {

    public static final String PKCS12 = "PKCS12";
    public static final String JKS = "JKS";
    /**
     * pfx֤��λ��
     */
    public static final String PFX_KEYSTORE_FILE = "C:\\Users\\hanke\\Desktop\\81231006051077490.pfx";

    /**
     * keystore����
     */
    public static final String KEYSTORE_PASSWORD = "vpos123";
    /**
     * jksλ��
     */
    public static final String JKS_KEYSTORE_FILE = "d:\\81231006051077490.jks";

    public static void coverToKeyStore() {
        try {
            KeyStore inputKeyStore = KeyStore.getInstance("PKCS12");
            //��pfx֤����ؽ���
            FileInputStream fis = new FileInputStream(PFX_KEYSTORE_FILE);
            char[] nPassword = KEYSTORE_PASSWORD.toCharArray();

            //keystore������,�������ڽ���keystore
            inputKeyStore.load(fis, nPassword);
            fis.close();
            KeyStore outputKeyStore = KeyStore.getInstance("JKS");
            //�����һ������Ϊ��,��ô�½�һ���յ�keystore
            outputKeyStore.load(null, KEYSTORE_PASSWORD.toCharArray());
            //�г�����keystore���б���
            Enumeration enums = inputKeyStore.aliases();

            while (enums.hasMoreElements()) {
                String keyAlias = (String) enums.nextElement();
                System.out.println("alias=[" + keyAlias + "]");
                if (inputKeyStore.isKeyEntry(keyAlias)) {
                    Key key = inputKeyStore.getKey(keyAlias, nPassword);
                    Certificate[] certChain = inputKeyStore.getCertificateChain(keyAlias);

                    outputKeyStore.setKeyEntry(keyAlias, key, KEYSTORE_PASSWORD.toCharArray(), certChain);
                }
            }

            //�½�����ļ�
            FileOutputStream out = new FileOutputStream(JKS_KEYSTORE_FILE);
            //���keystore
            outputKeyStore.store(out, nPassword);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        coverToKeyStore();
    }

}
