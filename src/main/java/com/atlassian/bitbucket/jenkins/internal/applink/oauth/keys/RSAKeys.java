package com.atlassian.bitbucket.jenkins.internal.applink.oauth.keys;

import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

public class RSAKeys {

    private static final String RSA = "RSA";

    /**
     * Returns a newly created RSA public/private {@code KeyPair}.
     *
     * @return newly created RSA public/private {@code KeyPair}
     * @throws NoSuchAlgorithmException thrown if there are no RSA providers available
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        final KeyPairGenerator gen = KeyPairGenerator.getInstance(RSA);
        return gen.generateKeyPair();
    }

    /**
     * Returns the RSA {@code PublicKey} that was decoded from the encoded string.  The encoded key may contain new-lines
     * and OpenSSL header/footer sections.  It must be a public key that has been encoded using the X.509 key spec and
     * base-64 algorithm.
     *
     * @param pemEncodedPublicKey A public key that has been X.509 and base-64 encoded
     * @return decoded RSA {@code PublicKey}
     * @throws NoSuchAlgorithmException thrown if there are no RSA providers available
     * @throws InvalidKeySpecException  thrown if {@code pemEncodedPublicKey} is not a validly encoded {@code PublicKey}
     */
    public static PublicKey fromPemEncodingToPublicKey(
            String pemEncodedPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory fac = KeyFactory.getInstance(RSA);
        EncodedKeySpec publicKeySpec =
                new X509EncodedKeySpec(decodeBase64(convertFromOpenSsl(pemEncodedPublicKey).getBytes()));
        return fac.generatePublic(publicKeySpec);
    }

    /**
     * Returns the RSA {@code PublicKey} that was decoded from the encoded certificate.
     *
     * @param encodedCertificate A public certificate
     * @return the RSA {@code PublicKey} that was decoded from the encoded certificate
     * @throws CertificateException throw if the {@code encodedCertificate} is not a validly encoded {@link X509Certificate}
     */
    public static PublicKey fromEncodedCertificateToPublicKey(String encodedCertificate) throws CertificateException {
        CertificateFactory certFac = CertificateFactory.getInstance("X509");
        ByteArrayInputStream in = new ByteArrayInputStream(encodedCertificate.getBytes());
        X509Certificate cert = (X509Certificate) certFac.generateCertificate(in);
        return cert.getPublicKey();
    }

    /**
     * Returns the RSA {@code PrivateKey} that was decoded from the encoded string.  The encoded key may contain new-lines
     * and OpenSSL header/footer sections.  It must be a private key that has been encoded using the PKCS8 key spec and
     * the base-64 algorithm.
     *
     * @param pemEncodedPrivateKey A private key that has been PKCS8 and base-64 encoded
     * @return decoded RSA {@code PrivateKey}
     * @throws NoSuchAlgorithmException thrown if there are no RSA providers available
     * @throws InvalidKeySpecException  thrown if {@code pemEncodedPrivateKey} is not a validly encoded {@code PrivateKey}
     */
    public static PrivateKey fromPemEncodingToPrivateKey(
            String pemEncodedPrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory fac = KeyFactory.getInstance(RSA);
        EncodedKeySpec privateKeySpec =
                new PKCS8EncodedKeySpec(decodeBase64(convertFromOpenSsl(pemEncodedPrivateKey).getBytes()));
        return fac.generatePrivate(privateKeySpec);
    }

    /**
     * Convert the key to the appropriate string encoding.  For public keys, this means converting to the X.509 key spec
     * and then encoding in base-64.  For private keys, this means converting to the PKCS8 key spec and then encoding
     * in base-64.
     *
     * @param key {@code Key} to be encoded
     * @return {@code Key} as an encoded string
     */
    public static String toPemEncoding(Key key) {
        return new String(encodeBase64(key.getEncoded()));
    }

    /**
     * Strips standard header/footer and any newlines.
     *
     * @param key key to convert from an OpenSSL format
     * @return key with standard OpenSSL headers and footers and all newlines removed
     */
    public static String convertFromOpenSsl(String key) {
        return key.replaceAll("-----[A-Z ]*-----", "").replace("\n", "");
    }
}
