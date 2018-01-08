package com.game;

import com.game.params.IntParam;
import com.server.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * Created by lucky on 2017/12/28.
 */
public class GameReload {
    private Socket socket;
    private String host;
    private int port;


    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("config/sys.properties")));
        int port = Integer.valueOf(properties.getProperty("port"));
        new GameReload("localhost", port).stop();
        System.out.println("reload config ...");
    }

    public GameReload(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void stop() throws Exception {
        this.socket = new Socket(host, port);
        IntParam param = new IntParam();
        param.param = 1024 * 4 + 9;
        Util.sendSocketData(socket, 9904, param, 0, 0);
        Thread.sleep(3000);
    }
}
