package com.myc.client.core;

import java.util.HashSet;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import com.myc.client.zk.ZookeeperFactory;

import io.netty.channel.ChannelFuture;

public class ServerWathcer implements CuratorWatcher {

	@Override
	public void process(WatchedEvent event) throws Exception {
		//服务器监听
		CuratorFramework client = ZookeeperFactory.create();
		String path = event.getPath();
		client.getChildren().usingWatcher(this).forPath(path);
		//这个serverPaths就是发生变化后的路径
		List<String> serverPaths = client.getChildren().forPath(path);
		ChannelManager.realServerPath.clear();
		for (String serverPath : serverPaths) {
			String[] str = serverPath.split("#");
			int weight = Integer.valueOf(str[2]);
			if(weight > 0) {
				for(int w = 0;w <= weight; w++) {
					ChannelManager.realServerPath.add(str[0]+"#"+str[1]);//保存了host和port
				}
			}
		    ChannelManager.realServerPath.add(str[0]+"#"+str[1]);
		}
		
		ChannelManager.clear();
		for(String realServer : ChannelManager.realServerPath) {
			String[] str = realServer.split("#");
			try {
				int weight = Integer.valueOf(str[2]);
				if(weight > 0) {
					for(int w = 0;w <= weight; w++) {
						ChannelFuture channelFuture = TcpClient.b.connect(str[0],Integer.valueOf(str[1]));
						ChannelManager.add(channelFuture);
					}
				}
		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}

}
