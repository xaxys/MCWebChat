package com.github.xaxys.mcwebchat;

public class ShowMessage implements Runnable {
	
	private static ShowMessage THIS;
	private Long messageId = 0L;
	private Long delay = Main.Conf.getLong("Delay", 1L);
	
	public ShowMessage(Long Id) {
		this.messageId = Id;
		THIS = this;
	}
	
	@Override
	public void run() {
		Main.PLUGIN.getLogger().info("启动获取消息线程:" + Thread.currentThread().getName());
		while (!Main.PLUGIN.Abort) {
			messageId = DataBase.getDB().GetMessage(messageId);
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Long getMessageId() {
		return messageId;
	}
	
	public static ShowMessage getShowMessage() {
		return THIS;
	}
	
}
