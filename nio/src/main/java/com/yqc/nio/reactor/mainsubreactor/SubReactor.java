package com.yqc.nio.reactor.mainsubreactor;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * @author yangqc
 */
public class SubReactor implements Runnable {

    private final Selector selector;

    SubReactor(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select(1);
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    dispatch(key);
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        Object attachment = selectionKey.attachment();
        if (attachment != null) {
            ((Runnable) attachment).run();
        }
    }
}
