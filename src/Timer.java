public class Timer extends Thread {
    private Model model;
    private TransferFile transferFile;
    public Timer(TransferFile transferFile, Model model){
        this.transferFile = transferFile;
        this.model = model;
    }

    @Override
    public void run(){
        do{
            int time = model.getTime();
            if(time > 0){
                try {
                    Thread.sleep(time*1000);

                    System.out.println("\n");

                        System.out.println("GBN客户端等待ACK超时");
                        transferFile.timeOut();

                    model.setTime(0);

                } catch (InterruptedException e) {
                } catch (Exception e) {
                }
            }
        }while (true);
    }

}
