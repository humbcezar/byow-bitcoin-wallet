package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.events.BlockReceivedEvent;
import byow.bitcoinwallet.events.TransactionReceivedEvent;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import java.util.Set;

import static com.blockstream.libwally.Wally.*;
import static wf.bitcoin.krotjson.HexCoder.encode;

@Component
@Lazy
public class NodeMonitorTask {

    @Autowired
    private BitcoinJSONRPCClient bitcoindRpcClient;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private Socket subscriber;

    private NodeTask currentTask;

    public NodeTask getTask() {
        currentTask = new NodeTask();
        currentTask.setOnFailed(event -> {
            try {
                throw currentTask.getException();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return currentTask;
    }

    public void cancel() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel();
        }
    }

    class NodeTask extends Task<Void> {
        @Override
        protected Void call() {
            subscriber.connect("tcp://127.0.0.1:29000");

            while (!Thread.currentThread().isInterrupted()) {
                if (isCancelled()) {
                    break;
                }
                byte[] contents = null;
                String topic;
                synchronized (subscriber) {
                    topic = subscriber.recvStr(ZMQ.DONTWAIT);
                    if (topic == null || !Set.of("rawtx", "hashblock").contains(topic)) {
                        continue;
                    }
                    contents = subscriber.recv(ZMQ.DONTWAIT);
                }

                switch (topic) {
                    case "rawtx" -> {
                        applicationEventPublisher.publishEvent(
                            new TransactionReceivedEvent(this, tx_from_hex(encode(contents), WALLY_TX_FLAG_USE_WITNESS))
                        );
                    }
                    case "hashblock" -> {
                        applicationEventPublisher.publishEvent(new BlockReceivedEvent(this));
                    }
                }
            }
            return null;
        }
    }

    public void subscribe() {
        synchronized (subscriber) {
            subscriber.subscribe("rawtx".getBytes());
            subscriber.subscribe("hashblock".getBytes());
        }
    }

    public void close() {
        if (subscriber != null) {
            subscriber.close();
        }
        cancel();
    }
}
