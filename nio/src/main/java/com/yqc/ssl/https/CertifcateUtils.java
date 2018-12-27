package com.yqc.ssl.https;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-12-24
 * @modified By yangqc
 */
public class CertifcateUtils {

    /**
     * 读取证书
     *
     * @return
     * @throws Exception
     */
    public static byte[] readCertifacates() throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        InputStream inputStream = new FileInputStream("D:/https.crt");
        Certificate certificate = factory.generateCertificate(inputStream);
        return certificate.getEncoded();
    }

    /**
     * 读取私钥
     *
     * @return
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public static byte[] readPrivateKey() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        String userHome = System.getProperty("user.home");
        InputStream inputStream = new FileInputStream(userHome + "/server.keystore");
        keyStore.load(inputStream, "123456".toCharArray());
        PrivateKey privateKey = (PrivateKey) keyStore.getKey("serverkey", "654321".toCharArray());
        return privateKey.getEncoded();
    }

    /**
     * 获取私钥
     *
     * @return
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public static PrivateKey readPrivateKeys() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        String userHome = System.getProperty("user.home");
        InputStream inputStream = new FileInputStream(userHome + "/server.keystore");
        keyStore.load(inputStream, "123456".toCharArray());
        Key serverkey = keyStore.getKey("serverkey", "654321".toCharArray());
        return (PrivateKey) serverkey;
    }

    /**
     * 获取公钥
     *
     * @return
     * @throws CertificateException
     * @throws FileNotFoundException
     */
    public static PublicKey readPublicKeys() throws CertificateException, FileNotFoundException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        InputStream inputStream = new FileInputStream("d:/https.crt");
        Certificate certificate = factory.generateCertificate(inputStream);
        return certificate.getPublicKey();
    }

    /**
     * 字节流创建证书
     *
     * @param bytes
     * @return
     * @throws CertificateException
     */
    public static Certificate createCertiface(byte[] bytes) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        InputStream inputStream = new ByteArrayInputStream(bytes);
        Certificate certificate = factory.generateCertificate(inputStream);
        return certificate;
    }

    public static String byte2hex(byte[] bytes) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < bytes.length; n++) {
            stmp = Integer.toHexString(bytes[n] & 0XFF);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }
}
