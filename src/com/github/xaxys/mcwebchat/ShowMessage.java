package com.github.xaxys.mcwebchat;

public class ShowMessage{
	
	private Long messageId = 0L;
	private Long delay = 1000L;
	public boolean Abort;
	
	public ShowMessage(Long Id, Long Delay) {
		this.messageId = Id;
		this.delay = Delay;
	}
	
	public void Start() {
		Main.PLUGIN.getServer().getScheduler().runTaskAsynchronously(Main.PLUGIN, () -> {
			Main.PLUGIN.getLogger().info("启动获取消息线程:" + Thread.currentThread().getName());
			while (!Abort) {
				if (Main.PLUGIN.DebugMode) {
					Main.PLUGIN.getLogger().info("获取消息ID>"+messageId);
				}
				messageId = DataBase.getDB().GetMessage(messageId);
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Main.PLUGIN.getLogger().info("终止获取消息线程:" + Thread.currentThread().getName());
		});
	}
}
