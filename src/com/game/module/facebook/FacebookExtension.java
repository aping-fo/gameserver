package com.game.module.facebook;

import com.game.params.*;
import com.server.anotation.Command;
import com.server.anotation.Extension;
import com.sun.org.apache.xerces.internal.xs.StringList;
import org.springframework.beans.factory.annotation.Autowired;

@Extension
public class FacebookExtension {

    @Autowired
    private FacebookService facebookService;

    @Command(10701)
    public IntParam facebookShareFinish(int playerId, Object param) {
        return facebookService.setFacebookShareFinish(playerId);
    }

    @Command(10702)
    public IntParam facebookInvite(int playerId, StringParam ids) {
        return facebookService.setFacebookInvite(playerId, ids);
    }

    @Command(10704)
    public IntParam facebookInputInvitor(int playerId, IntParam id) {
        return facebookService.facebookInputInvitor(playerId, id);
    }

    @Command(10705)
    public ListParam<FacebookInvitedPlayerVO> facebookGetInvitedPlayer(int playerId, Object param)
    {
        return facebookService.facebookGetInvitedPlayer(playerId);
    }
}
