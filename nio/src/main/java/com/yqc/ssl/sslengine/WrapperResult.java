package com.yqc.ssl.sslengine;

import javax.net.ssl.SSLEngineResult;
import java.nio.ByteBuffer;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-12-31
 * @modified By yangqc
 */
public class WrapperResult {

    SSLEngineResult result;

    /**
     * if passed in buffer was not big enough then the
     * a reallocated buffer is returned here
     */
    ByteBuffer buf;
}
