package cn.pency.rpc;

import cn.pency.vo.GoodTransferVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *类说明：RPC调用服务端
 */
@Service
public class DepotRpcServer implements ApplicationContextAware {

    private static Logger logger
            = LoggerFactory.getLogger(DepotRpcServer.class);
    private static final int PORT = 10002;

    private ApplicationContext appContext;

    //线程池
    private static ExecutorService executorService =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors());

    @Override
    public void setApplicationContext(
            ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    private static class SocketService implements Runnable{

        private ServerSocket serverSocket ;
        private ApplicationContext appContext;

        public SocketService(ApplicationContext appContext) {
            this.appContext = appContext;
        }


        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress(PORT));
                while(true){
                    Socket acceptClient = serverSocket.accept();
                    executorService.execute(
                            new ServerTask(acceptClient, this.appContext));
                    logger.info("--------------Rpc server is running " +
                            "on port" + PORT+"------------------");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ServerTask implements Runnable{

        private Socket acceptClient = null;
        private ApplicationContext appContext;

        public ServerTask(Socket acceptClient,
                          ApplicationContext appContext) {
            this.acceptClient = acceptClient;
            this.appContext = appContext;
        }

        @Override
        public void run() {

            try(ObjectInputStream inputStream =
                        new ObjectInputStream(
                                acceptClient.getInputStream());
                ObjectOutputStream outputStream =
                        new ObjectOutputStream(
                                acceptClient.getOutputStream())){

                try {
                    //TODO 客户端有请求过来，拿到服务名、方法名调用服务
                    String serviceName = inputStream.readUTF();
                    String methodName = inputStream.readUTF();
                    Class<?>[] parmTypes =  (Class<?>[])
                            inputStream.readObject();
                    Object[] args = (Object[])inputStream.readObject();
                    //TODO 存库服务处理
                    DepotServiceImpl depotServiceImpl
                            = appContext.getBean(DepotServiceImpl.class);
                    GoodTransferVo goodTransferVo
                            = (GoodTransferVo)args[0];
                    depotServiceImpl.changeDepot(goodTransferVo);
                    //TODO 存库服务发生异常了
                    throw new RuntimeException("库存系统异常了！！！！！");
                    //outputStream.writeUTF(RpcConst.SUCCESS);

                } catch (Exception e) {
                    e.printStackTrace();
                    outputStream.writeUTF(RpcConst.EXCEPTION);
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                try {
                    acceptClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @PostConstruct
    public void startService() throws Exception{
        Thread threadSocket = new Thread(
                new SocketService(this.appContext));
        threadSocket.start();
    }



}
