package com.myc.client.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.springframework.context.support.StaticApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.myc.client.constant.Constants;
import com.myc.client.handler.SimpleClientHandler;
import com.myc.client.param.ClientRequest;
import com.myc.client.param.Response;
import com.myc.client.zk.ZookeeperFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class TcpClient {

//	static Set<String> realServerPath = new HashSet<String>();
	static final Bootstrap b = new Bootstrap();// (1)
	static ChannelFuture f = null;
	static {
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		b.group(workerGroup); // (2)
		b.channel(NioSocketChannel.class); // (3)
		b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
				ch.pipeline().addLast(new StringDecoder());
				ch.pipeline().addLast(new SimpleClientHandler());
				ch.pipeline().addLast(new StringEncoder());
			}
		});

		CuratorFramework client = ZookeeperFactory.create();
		String host = "localhost";
		int port = 8080;
		// 获取zookeeper子节点
		try {
			//加上zk监听服务器,监听客户端路径下子路径的变化(服务器变化)
			CuratorWatcher watcher = new ServerWathcer();
			client.getChildren().usingWatcher(watcher).forPath(Constants.SERVER_PATH);
		    //获取服务器列表
			List<String> serverPaths = client.getChildren().forPath(Constants.SERVER_PATH);
			
			for (String serverPath : serverPaths) {
				// 对子节点去重
				String[] str = serverPath.split("#");
				int weight = Integer.valueOf(str[2]);
				if(weight > 0) {
					for(int w = 0;w <= weight; w++) {
						ChannelManager.realServerPath.add(str[0]+"#"+str[1]);//保存了host和port
						ChannelFuture channelFuture = TcpClient.b.connect(str[0],Integer.valueOf(str[1]));
						ChannelManager.add(channelFuture);
					}
				}
				
							
			}
			
	        //服务器启动的时候进行管理
			if (ChannelManager.realServerPath.size() > 0) {
				String[] hostAndPort = ChannelManager.realServerPath.toArray()[0].toString().split("#");
				host = hostAndPort[0];
				port = Integer.valueOf(hostAndPort[1]);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

//		try {
//			f = b.connect(host, port).sync();// (5)
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
}

		
		
	// 注意：1.每一个请求都是同一个连接，并发问题
	// 发送数据
	public static Response send(ClientRequest request) {
		f = ChannelManager.get(ChannelManager.position);
		f.channel().writeAndFlush(JSONObject.toJSONString(request));
		f.channel().writeAndFlush("\r\n");
		DefaultFuture df = new DefaultFuture(request);
		return df.get();
	}
}

