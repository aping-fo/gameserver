package com.game.sdk.erating.domain;

import com.game.sdk.erating.NodeName;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by lucky on 2018/2/1.
 */
@NodeName(name = "")
public class Role extends Header{
    @NodeName(name = "id_o")
    private int id;
    @NodeName(name = "id_list",object = false)
    private List<Integer> id_list;

    public Role(int command_id, int game_id, int gateway_id) {
        super(command_id, game_id, gateway_id);

        id = 100;
        id_list = Lists.newArrayList(1,2,3,4);
    }
}
