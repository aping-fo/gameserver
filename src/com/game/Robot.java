package com.game;

import com.game.params.StringParam;
import com.game.params.player.CRegVo;
import com.server.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Created by lucky on 2017/7/18.
 * 
 */
public class Robot {
    private final String accName;
    private final String name;

    public Robot(String name) {
        this.accName = name;
        this.name = name;
    }

    public void start(String host, int port) throws Exception {
        Socket socket = new Socket(host, port);
        //read(socket);
        //login(socket);
        read(socket);
    }


    public void read(Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getRoleList(socket);
                    createRole(socket);
                    InputStream in = socket.getInputStream();
                   out: while (true) {
                        Thread.sleep(10);
                        int avail = in.available();
                        if (avail < 2) {
                            continue;
                        }
                        in.mark(900000);
                        byte[] lenData = new byte[2];
                        in.read(lenData, 0, 2);
                        int len = Util.bytesToShort(lenData, 0);
                        if (in.available() < len) {
                            in.reset();
                            continue;
                        }

                        byte[] data = new byte[2];
                        in.read(data, 0, 2);
                        int cmd = Util.bytesToShort(data, 0);
                        System.out.println(System.currentTimeMillis() + " - " + name + ": Rec cmd:" + cmd);
                        if (cmd == 1006) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    robot(socket);
                                }
                            }).start();
                            break;
                        }
                        data = new byte[len - 2];
                        in.read(data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void createRole(Socket socket) {
        CRegVo vo = new CRegVo();
        vo.accName = accName;
        vo.name = name;
        vo.sex = 1;
        vo.vocation = 1;
        Util.sendSocketData(socket, 1002, vo, 0, 0);
    }

    private void getRoleList(Socket socket) {
        StringParam param = new StringParam();
        param.param = accName;
        Util.sendSocketData(socket, 1001, param, 0, 0);
    }

    public void login( Socket socket) {
        StringParam param = new StringParam();
        param.param = accName;
        Util.sendSocketData(socket, 1001, param, 0, 0);

        CRegVo vo = new CRegVo();
        vo.accName = accName;
        vo.name = name;
        vo.sex = 1;
        vo.vocation = 1;
        Util.sendSocketData(socket, 1002, vo, 0, 0);
    }

    public void robot(Socket socket) {
        try {
            login(socket);

            Path path = Paths.get("robot.data");
            FileInputStream read = new FileInputStream(path.toFile());
            DataInputStream dis = new DataInputStream(read);
            Random random = new Random();
            while (dis.available() > 0) {
                int len = dis.readShort();
                byte[] dataBytes = new byte[len - 4];
                dis.read(dataBytes);
                System.out.println(name +"send data,data len = "+ len);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeShort(len);
                dos.write(dataBytes);
                dos.writeInt(dis.readInt());
                dos.flush();
                Thread.sleep(1000);
                Thread.yield();
            }
        } catch (Exception e) {

        }
    }
}
