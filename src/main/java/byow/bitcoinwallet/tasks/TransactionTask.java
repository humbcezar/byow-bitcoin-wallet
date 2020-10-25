package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.events.TransactionReceivedEvent;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
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

    private TxTask currentTask;

    public TxTask getTask() {
        currentTask = new TxTask();
        return currentTask;
    }

    class TxTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            ZMQ.Socket subscriber = zContext.createSocket(SocketType.SUB);
            subscriber.connect("tcp://127.0.0.1:29000");
            subscriber.subscribe("rawtx");

            while (!Thread.currentThread().isInterrupted()) {
                String topic = subscriber.recvStr();
                byte[] contents = subscriber.recv();

                switch (topic) {
                    case "rawtx" -> {
                        RawTransaction rawTransaction = bitcoindRpcClient.decodeRawTransaction(HexCoder.encode(contents));
                        applicationEventPublisher.publishEvent(new TransactionReceivedEvent(this, rawTransaction));
                    }
                }
            }
            return null;
        }
    }

    public TxTask getCurrentTask() {
        return currentTask;
    }

}
