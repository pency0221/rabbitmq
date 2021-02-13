package cn.pency.rpc;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *类说明：RPC调用客户端
 */
public class RpcProxy {

    public static<T> T getRmoteProxyObj(final Class<?> serviceInterface,
                                        final InetSocketAddress addr){
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                new Class<?>[] {serviceInterface},
                new Proxy_Depot(serviceInterface,addr));

    }

    private static class Proxy_Depot implements InvocationHandler{

        private final Class<?> serviceInterface;
        private final InetSocketAddress addr;

        public Proxy_Depot(Class<?> serviceInterface, InetSocketAddress addr) {
            this.serviceInterface = serviceInterface;
            this.addr = addr;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Socket socket =  null;
            ObjectOutputStream output = null;
            ObjectInputStream input = null;

            try {
                socket = new Socket();
                socket.connect(addr);

                output = new ObjectOutputStream(socket.getOutputStream());
                /*向服务器输出请求*/
                output.writeUTF(serviceInterface.getName());
                output.writeUTF(method.getName());
                output.writeObject(method.getParameterTypes());
                output.writeObject(args);
                //TODO 远程调用结果
                input = new ObjectInputStream(socket.getInputStream());
                String result = input.readUTF();
                if(result.equals(RpcConst.EXCEPTION)){ //TODO 异常了
                    throw new RuntimeException("远程服务器异常！");
                }
                //TODO 正常
                return true;
            } finally {
                socket.close();
                output.close();
                //input.close();
            }


        }
    }

}
