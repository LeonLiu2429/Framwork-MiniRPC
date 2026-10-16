
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

public class NacosTestDemo {

    public static void main(String[] args) throws NacosException {
        String serverAddress = "127.0.0.1:8848";
        String serviceName = "com.leon.demo.EchoService";
        String ip = "127.0.0.1";
        int port = 19000;

        NamingService naming =
                NamingFactory.createNamingService(serverAddress);

        try {
            naming.registerInstance(serviceName, ip, port);
            System.out.println("已注册实例");

            List<Instance> instances =
                    naming.getAllInstances(serviceName);

            for (Instance instance : instances) {
                System.out.println(
                        instance.getIp() + ":" + instance.getPort()
                );
            }
        } finally {
            naming.deregisterInstance(serviceName, ip, port);
            naming.shutDown();
        }
    }
}