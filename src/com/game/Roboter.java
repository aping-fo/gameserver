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

/**
 * Created by lucky on 2017/7/18.
 */
public class Roboter {
    private final String accName;
    private final String name;
    private final Socket socket;
    private final DataInputStream dis;

    public Roboter(String name, String host, int port) throws Exception {
        this.accName = name;
        this.name = name;
        socket = new Socket(host, port);

        getRoleList();
        createRole();
        login();

        Path path = Paths.get("robot.data");
        FileInputStream read = new FileInputStream(path.toFile());
        dis = new DataInputStream(read);
    }

    private void createRole() {
        CRegVo vo = new CRegVo();
        vo.accName = accName;
        vo.name = name;
        vo.sex = 1;
        vo.vocation = 1;
        Util.sendSocketData(socket, 1002, vo, 0, 0);
    }

    private void getRoleList() {
        StringParam param = new StringParam();
        param.param = accName;
        Util.sendSocketData(socket, 1001, param, 0, 0);
    }

    public void login() {
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

    public boolean sendRobotMsg() {
        try {
            if (dis.available() > 0) {
                int len = dis.readShort();
                byte[] dataBytes = new byte[len - 4];
                dis.read(dataBytes);
                System.out.println(name + " send data,data len = " + len);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeShort(len);
                dos.write(dataBytes);
                dos.writeInt(dis.readInt());
                dos.flush();
                Thread.sleep(500);
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public void readRobotMsg() throws Exception {
        InputStream in = socket.getInputStream();
    }
}
