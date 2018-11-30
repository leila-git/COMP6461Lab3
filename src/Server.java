public class Server {
    public static void main(String[] args) throws InterruptedException {
        Thread thrdCsp;
        Thread thrdRcv;
        Thread thrdSender;
        Object rcvMonitor = new Object();
        Object clientServiceProviderMonitor = new Object();


        args = new String[]{"8007", "3000", "8000", Constants.currentPath + "/test/server/in"};

        Receiver r = new Receiver(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], 1, rcvMonitor);
        thrdRcv = new Thread(r, "receiverThreadInServer");
        thrdRcv.start();


        while (true) {

            synchronized (rcvMonitor) {
                rcvMonitor.wait();
                ClientServiceProvider csp = new ClientServiceProvider(Constants.defaultFolderName, clientServiceProviderMonitor);
                thrdCsp = new Thread(csp, "ClientServiceProviderThread");
                thrdCsp.start();
            }

            synchronized (clientServiceProviderMonitor) {
                clientServiceProviderMonitor.wait();

                args = new String[]{"3000", "9007", "9000", Constants.currentPath + "/test/server/out/result.txt", "result.txt"};
                Sender s = new Sender(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], args[4]);
                thrdSender = new Thread(s, "senderThreadInserver");
                thrdSender.start();
            }
        }
    }
}
