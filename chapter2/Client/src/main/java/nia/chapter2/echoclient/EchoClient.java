package nia.chapter2.echoclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * 代码清单 2-4 客户端的主类
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class EchoClient {
    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start(final String message) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //创建 Bootstrap
            Bootstrap b = new Bootstrap();
            //指定 EventLoopGroup 以处理客户端事件；需要适用于 NIO 的实现
            b.group(group)
                //适用于 NIO 传输的Channel 类型
                .channel(NioSocketChannel.class)
                //设置服务器的InetSocketAddress
                .remoteAddress(new InetSocketAddress(host, port))
                //在创建Channel时，向 ChannelPipeline中添加一个 EchoClientHandler实例
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)
                        throws Exception {
                        ch.pipeline().addLast(
                             new EchoClientHandler(message));
                    }
                });
            //连接到远程节点，阻塞等待直到连接完成
            ChannelFuture f = b.connect().sync();
            //阻塞，直到Channel 关闭
            f.channel().closeFuture().sync();
        } finally {
            //关闭线程池并且释放所有的资源
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args)
            throws Exception {
//        if (args.length != 2) {
//            System.err.println("Usage: " + EchoClient.class.getSimpleName() +
//                    " <host> <port>"
//            );
//            return;
//        }
//
//        final String host = args[0];
//        final int port = Integer.parseInt(args[1]);
//        new EchoClient(host, port).start();


        final String host = "127.0.0.1";
        final int port = 1093;
// System.in代表标准输入，就是键盘输入
        Scanner sc = new Scanner(System.in);
        // 增加下面一行将只把回车作为分隔符
        // sc.useDelimiter("\n");

        // 判断是否还有下一个输入项
        String content = null;
        ThreadFactory threadFactory = new ThreadFactory() {       //自定义ThreadFactory
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                System.out.println("生成线程 " + t);
                return t;
            }
        };
        ExecutorService executor = new ThreadPoolExecutor(5,200,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024),threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
        while(sc.hasNext())  {
            content = sc.next();
            // 输出输入项
            final String finalContent = content;
            Runnable writer = new Runnable() {
                @Override
                public void run() {
                    try {
                        new EchoClient(host, port).start(finalContent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            executor.execute(writer);
        }
    }
}

