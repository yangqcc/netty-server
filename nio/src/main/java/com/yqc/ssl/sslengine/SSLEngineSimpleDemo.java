package com.yqc.ssl.sslengine;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-12-26
 * @modified By yangqc
 */
public class SSLEngineSimpleDemo {

    /*
     * Enables logging of the SSLEngine operations.
     */
    private static boolean logging = true;

    /*
     * Enables the JSSE system debugging system property:
     *
     * -Djavax.net.debug=all
     *
     * This gives a lot of low-level information about operations underway,
     * including specific handshake messages, and might be best examined after
     * gaining some familiarity with this application.
     */
    private static boolean debug = true;

    private SSLContext sslc;

    // client Engine
    private SSLEngine clientEngine;
    // write side of clientEngine
    private ByteBuffer clientOut;
    // read side of clientEngine
    private ByteBuffer clientIn;

    // server Engine
    private SSLEngine serverEngine;
    // write side of serverEngine
    private ByteBuffer serverOut;
    // read side of serverEngine
    private ByteBuffer serverIn;

    /*
     * For data transport, this example uses local ByteBuffers. This isn't
     * really useful, but the purpose of this example is to show SSLEngine
     * concepts, not how to do network transport.
     */

    // "reliable" transport client->server
    private ByteBuffer cTOs;
    // "reliable" transport server->client
    private ByteBuffer sTOc;

    /*
     * The following is to set up the keystores.
     */
    private static String keyStoreFile = "C:\\Users\\yangqc\\server.keystore";
    private static String trustStoreFile = "C:\\Users\\yangqc\\server.keystore";

    /*
     * Main entry point for this demo.
     */
    public static void main(String args[]) throws Exception {
        if (debug) {
            System.setProperty("javax.net.debug", "all");
        }

        SSLEngineSimpleDemo demo = new SSLEngineSimpleDemo();
        demo.runDemo();

        System.out.println("Demo Completed.");
    }

    /*
     * Create an initialized SSLContext to use for this demo.
     */
    public SSLEngineSimpleDemo() throws Exception {

        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");

        //password
        char[] passphrase = "654321".toCharArray();

        File file = new File(keyStoreFile);
        System.out.println("put keystore in this path:" + file.getAbsolutePath());

        ks.load(new FileInputStream(keyStoreFile), passphrase);
        ts.load(new FileInputStream(trustStoreFile), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        SSLContext sslCtx = SSLContext.getInstance("TLS");

        sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        sslc = sslCtx;
    }

    /*
     * Run the demo.
     *
     * Sit in a tight loop, both engines calling wrap/unwrap regardless of
     * whether data is available or not. We do this until both engines report
     * back they are closed.
     *
     * The main loop handles all of the I/O phases of the SSLEngine's lifetime:
     *
     * initial handshaking application data transfer engine closing
     *
     * One could easily separate these phases into separate sections of code.
     */
    private void runDemo() throws Exception {
        boolean dataDone = false;

        createSSLEngines();
        createBuffers();

        SSLEngineResult clientResult; // results from client's last operation
        SSLEngineResult serverResult; // results from server's last operation

        /*
         * Examining the SSLEngineResults could be much more involved, and may
         * alter the overall flow of the application.
         *
         * For example, if we received a BUFFER_OVERFLOW when trying to write to
         * the output pipe, we could reallocate a larger pipe, but instead we
         * wait for the peer to drain it.
         */
        while (!isEngineClosed(clientEngine) || !isEngineClosed(serverEngine)) {

            log("================");

            clientResult = clientEngine.wrap(clientOut, cTOs);
            log("client wrap: ", clientResult);
            runDelegatedTasks(clientResult, clientEngine);

            serverResult = serverEngine.wrap(serverOut, sTOc);
            log("server wrap: ", serverResult);
            runDelegatedTasks(serverResult, serverEngine);

            cTOs.flip();
            sTOc.flip();

            log("----");

            clientResult = clientEngine.unwrap(sTOc, clientIn);
            log("client unwrap: ", clientResult);
            runDelegatedTasks(clientResult, clientEngine);

            serverResult = serverEngine.unwrap(cTOs, serverIn);
            log("server unwrap: ", serverResult);
            runDelegatedTasks(serverResult, serverEngine);

            cTOs.compact();
            sTOc.compact();

            /*
             * After we've transfered all application data between the client
             * and server, we close the clientEngine's outbound stream. This
             * generates a close_notify handshake message, which the server
             * engine receives and responds by closing itself.
             *
             * In normal operation, each SSLEngine should call closeOutbound().
             * To protect against truncation attacks, SSLEngine.closeInbound()
             * should be called whenever it has determined that no more input
             * data will ever be available (say a closed input stream).
             */
            if (!dataDone && (clientOut.limit() == serverIn.position())
                    && (serverOut.limit() == clientIn.position())) {

                /*
                 * A sanity check to ensure we got what was sent.
                 */
                checkTransfer(serverOut, clientIn);
                checkTransfer(clientOut, serverIn);

                log("\tClosing clientEngine's *OUTBOUND*...");
                clientEngine.closeOutbound();
                // serverEngine.closeOutbound();
                dataDone = true;
            }
        }
    }

    /*
     * Using the SSLContext created during object creation, create/configure the
     * SSLEngines we'll use for this demo.
     */
    private void createSSLEngines() throws Exception {
        /*
         * Configure the serverEngine to act as a server in the SSL/TLS
         * handshake. Also, require SSL client authentication.
         */
        serverEngine = sslc.createSSLEngine();
        serverEngine.setUseClientMode(false);
        serverEngine.setNeedClientAuth(true);

        /*
         * Similar to above, but using client mode instead.
         */
        clientEngine = sslc.createSSLEngine("client", 80);
        clientEngine.setUseClientMode(true);
    }

    /*
     * Create and size the buffers appropriately.
     */
    private void createBuffers() {

        /*
         * We'll assume the buffer sizes are the same between client and server.
         */
        SSLSession session = clientEngine.getSession();
        int appBufferMax = session.getApplicationBufferSize();
        int netBufferMax = session.getPacketBufferSize();

        /*
         * We'll make the input buffers a bit bigger than the max needed size,
         * so that unwrap()s following a successful data transfer won't generate
         * BUFFER_OVERFLOWS.
         *
         * We'll use a mix of direct and indirect ByteBuffers for tutorial
         * purposes only. In reality, only use direct ByteBuffers when they give
         * a clear performance enhancement.
         */
        clientIn = ByteBuffer.allocate(appBufferMax + 50);
        serverIn = ByteBuffer.allocate(appBufferMax + 50);

        cTOs = ByteBuffer.allocateDirect(netBufferMax);
        sTOc = ByteBuffer.allocateDirect(netBufferMax);

        clientOut = ByteBuffer.wrap("Hi Server, I'm Client".getBytes());
        serverOut = ByteBuffer.wrap("Hello Client, I'm Server".getBytes());
    }

    /*
     * If the result indicates that we have outstanding tasks to do, go ahead
     * and run them in this thread.
     */
    private static void runDelegatedTasks(SSLEngineResult result,
                                          SSLEngine engine) throws Exception {

        if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            Runnable runnable;
            while ((runnable = engine.getDelegatedTask()) != null) {
                log("\trunning delegated task...");
                runnable.run();
            }
            SSLEngineResult.HandshakeStatus hsStatus = engine.getHandshakeStatus();
            if (hsStatus == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                throw new Exception("handshake shouldn't need additional tasks");
            }
            log("\tnew HandshakeStatus: " + hsStatus);
        }
    }

    private static boolean isEngineClosed(SSLEngine engine) {
        return (engine.isOutboundDone() && engine.isInboundDone());
    }

    /*
     * Simple check to make sure everything came across as expected.
     */
    private static void checkTransfer(ByteBuffer a, ByteBuffer b)
            throws Exception {
        a.flip();
        b.flip();

        if (!a.equals(b)) {
            throw new Exception("Data didn't transfer cleanly");
        } else {
            log("\tData transferred cleanly");
        }

        a.position(a.limit());
        b.position(b.limit());
        a.limit(a.capacity());
        b.limit(b.capacity());
    }

    /*
     * Logging code
     */
    private static boolean resultOnce = true;

    private static void log(String str, SSLEngineResult result) {
        if (!logging) {
            return;
        }
        if (resultOnce) {
            resultOnce = false;
            System.out.println("The format of the SSLEngineResult is: \n"
                    + "\t\"getStatus() / getHandshakeStatus()\" +\n"
                    + "\t\"bytesConsumed() / bytesProduced()\"\n");
        }
        SSLEngineResult.HandshakeStatus hsStatus = result.getHandshakeStatus();
        log(str + result.getStatus() + "/" + hsStatus + ", "
                + result.bytesConsumed() + "/" + result.bytesProduced()
                + " bytes");
        if (hsStatus == SSLEngineResult.HandshakeStatus.FINISHED) {
            log("\t...ready for application data");
        }
    }

    private static void log(String str) {
        if (logging) {
            System.out.println(str);
        }
    }
}
