package com.game.module.artifact;

import org.springframework.beans.factory.annotation.Autowired;

import com.game.params.IntParam;
import com.server.anotation.Command;
import com.server.anotation.Extension;

/**  
 * 神器
 */
@Extension
public class ArtifactExtension {
	
	@Autowired
	private ArtifactService service;
	
	@Command(3601)
	public Object compose(int playerId,IntParam id){
		IntParam result = new IntParam();
		result.param = service.compose(playerId, id.param);
		return result;
	}
	
	@Command(3602)
	public Object decompose(int playerId,IntParam id){
		IntParam result = new IntParam();
		result.param = service.decompose(playerId, id.param);
		return result;
	}
}
