package io.github.kk01001.examples.mysql;

import io.github.kk01001.mysql.BinaryLogClientListener;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author linshiqiang
 * @date 2024-09-13 19:28:00
 * @description
 */
public class BinlogListener {

    public static void main(String[] args) throws IOException, TimeoutException {
        BinaryLogClientListener listener = new BinaryLogClientListener("psd", "127.0.0.1", 3306, "root", "123456");
        listener.registerEventListener();
        listener.start(10000);
    }
}
