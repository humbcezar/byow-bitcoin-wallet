package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.events.BlockReceivedEvent;
import byow.bitcoinwallet.events.TransactionReceivedEvent;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ.Socket;

import static com.blockstream.libwally.Wally.WALLY_TX_FLAG_USE_WITNESS;
import static com.blockstream.libwally.Wally.tx_from_bytes;
import static java.lang.Thread.currentThread;
import static java.util.Set.of;
import static org.zeromq.ZMQ.DONTWAIT;

@Component
@Lazy
public class NodeMonitorTask {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Socket subscriber;

    private final String zmqUrl;

    private NodeTask currentTask;

    @Autowired
    public NodeMonitorTask(
        ApplicationEventPublisher applicationEventPublisher,
        Socket subscriber,
        @Value("${zmq.url}") String zmqUrl
    ) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.subscriber = subscriber;
        this.zmqUrl = zmqUrl;
    }

    public NodeTask buildTask() {
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

    private void cancel() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel();
        }
    }

    class NodeTask extends Task<Void> {
        @Override
        protected Void call() {
            subscriber.connect(zmqUrl);

            while (!currentThread().isInterrupted()) {
                if (isCancelled()) {
                    break;
                }
                byte[] contents;
                String topic;
                synchronized (subscriber) {
                    topic = subscriber.recvStr(DONTWAIT);
                    if (topic == null || !of("rawtx", "hashblock").contains(topic)) {
                        continue;
                    }
                    contents = subscriber.recv(DONTWAIT);
                }

                switch (topic) {
                    case "rawtx" -> applicationEventPublisher.publishEvent(
                        new TransactionReceivedEvent(this, tx_from_bytes(contents, WALLY_TX_FLAG_USE_WITNESS))
                    );
                    case "hashblock" -> applicationEventPublisher.publishEvent(new BlockReceivedEvent(this));
                }
            }
            return null;
        }
    }
}