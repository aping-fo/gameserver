package com.game.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.server.util.MyTheadFactory;
import com.server.util.ServerLogger;

public abstract class DelayUpdater {

	private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(5, new MyTheadFactory("delay updater"));
	private final AtomicBoolean isDirty = new AtomicBoolean(false);
	private final int period;//更新周期(秒)
	
	public static void stop(){
		try{
			executor.shutdown();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public DelayUpdater(int period){
		this.period = period;
	}
	
	public abstract boolean update();
	
	public final void submit(){
		if(isDirty.compareAndSet(false, true)){			
			executor.schedule(periodCallable, period, TimeUnit.SECONDS);
		}
	}
	
	public boolean dirty(){
		return isDirty.get();
	}
	
	public final void flush(){
		executor.schedule(flushCallable, 0, TimeUnit.SECONDS);
	}
	
	private Callable<Void> periodCallable = new Callable<Void>() {
		@Override
		public Void call() throws Exception {
			try{
				if(isDirty.compareAndSet(true, false) && !update()){
					submit();
				}
			}catch(Exception ex){
				ServerLogger.err(ex, "delay updater period update fail!");
			}
			return null;
		}
	};
	
	private Callable<Void> flushCallable = new Callable<Void>() {
		
		@Override
		public Void call() throws Exception {
			try{
				if(!update() && isDirty.getAndSet(false)){
					submit();
				}
			}catch(Exception ex){
				ServerLogger.err(ex, "delay updater period update fail!");
			}
			return null;
		}
	};
}
