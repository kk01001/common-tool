package io.github.kk01001.mysql;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.network.SSLMode;
import lombok.Data;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author linshiqiang
 * @date 2024-09-13 19:01:00
 * @description
 */
@Data
public class BinaryLogClientListener {

    private BinaryLogClient client;

    private final String schema;

    private final String hostname;

    private final Integer port;

    private final String username;

    private final String password;

    private List<String> tableList;

    private SSLMode sslMode;

    private Long serverId;

    public BinaryLogClientListener(String schema, String hostname, Integer port, String username, String password) {
        this.schema = schema;
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.client = new BinaryLogClient(this.hostname, this.port, this.schema, this.username, this.password);
    }

    public void setSSLMode(SSLMode sslMode) {
        if (sslMode == null) {
            throw new IllegalArgumentException("SSL mode cannot be NULL");
        }
        this.sslMode = sslMode;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public void start(final long timeout) throws IOException, TimeoutException {
        this.client.connect(timeout);
    }

    public void disconnect() throws IOException {
        this.client.disconnect();
    }

    public void registerEventListener() {
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
        );
        this.client.setEventDeserializer(eventDeserializer);
        this.client.registerEventListener(new BinaryLogClient.EventListener() {
            @Override
            public void onEvent(Event event) {
                EventHeader header = event.getHeader();
                EventType eventType = header.getEventType();
                switch (eventType) {
                    case WRITE_ROWS:
                        WriteRowsEventData writeRowsData = (WriteRowsEventData) event.getData();
                        // 处理插入数据
                        System.out.println("Insert: " + writeRowsData.toString());
                        break;

                    case UPDATE_ROWS:
                        UpdateRowsEventData updateRowsData = (UpdateRowsEventData) event.getData();
                        // 处理更新数据
                        System.out.println("Update: " + updateRowsData.toString());
                        break;

                    case DELETE_ROWS:
                        DeleteRowsEventData deleteRowsData = (DeleteRowsEventData) event.getData();
                        // 处理删除数据
                        System.out.println("Delete: " + deleteRowsData.toString());
                        break;

                    default:
                        EventData data = event.getData();
                        // 处理其他类型的事件
                        if (data != null) {
                            System.out.println("Other event: " + data.toString());
                        }
                        break;
                }
            }
        });
    }

}
