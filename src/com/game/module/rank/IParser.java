package com.game.module.rank;

import com.game.params.IProtocol;

public interface IParser<T extends IProtocol> {

	T parse(RankEntity entity);
}
