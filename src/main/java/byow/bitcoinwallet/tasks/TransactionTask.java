package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.events.TransactionReceivedEvent;
import byow.bitcoinwallet.services.RescanAborter;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;
import wf.bitcoin.krotjson.HexCoder;

@Component
public class TransactionTask {
    private Logger logger = LoggerFactory.getLogger(TransactionTask.class);

    @Autowired
    private BitcoinJSONRPCClient bitcoindRpcClient;

    @Autowired
    private ZContext zContext;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private RescanAborter rescanAborter;

    private Socket subscriber;

    private TxTask currentTask;

    public TxTask getTask() {
        TxTask txTask = new TxTask();
        txTask.setOnFailed(event -> txTask.getException().printStackTrace());
        return txTask;
    }

    class TxTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            subscriber = zContext.createSocket(SocketType.SUB);
            subscriber.connect("tcp://127.0.0.1:29000");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] contents = null;
                synchronized (subscriber) {
                    String topic = subscriber.recvStr(ZMQ.DONTWAIT);
                    if (topic == null || !topic.equals("rawtx")) {
//                    Thread.sleep(1000);
                        continue;
                    }
                    contents = subscriber.recv();
                }

                RawTransaction rawTransaction = bitcoindRpcClient.decodeRawTransaction(HexCoder.encode(contents));
                logger.info(rawTransaction.toString());
                applicationEventPublisher.publishEvent(new TransactionReceivedEvent(this, rawTransaction));
            }
            return null;
        }
    }

    public void unsubscribe() {
        synchronized (subscriber) {
            subscriber.unsubscribe("rawtx".getBytes());
        }
    }

    public void subscribe() {
        synchronized (subscriber) {
            subscriber.subscribe("rawtx".getBytes());
        }
    }

    public void close() {
        if (subscriber != null) {
            subscriber.close();
        }
        if (currentTask != null) {
            currentTask.cancel();
        }
    }
}
