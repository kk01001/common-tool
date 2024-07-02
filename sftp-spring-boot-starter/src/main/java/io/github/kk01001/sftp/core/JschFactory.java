package io.github.kk01001.sftp.core;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author kk01001
 * date 2023-03-29 20:38:00
 */
@Slf4j
public class JschFactory implements PooledObjectFactory<ChannelSftp> {

    private final String host;

    private final int port;

    private final String username;

    private final String password;

    public JschFactory(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * @param pooledObject
     * @throws Exception
     */
    @Override
    public void activateObject(PooledObject<ChannelSftp> pooledObject) throws Exception {
        // 当连接从池中获取时，需要激活一下连接，
        // 保证它是处于打开状态的
        ChannelSftp channelSftp = pooledObject.getObject();
        if (!channelSftp.isConnected()) {
            channelSftp.connect();
        }
    }

    /**
     * 销毁连接对象
     *
     * @param pooledObject
     * @throws Exception
     */
    @Override
    public void destroyObject(PooledObject<ChannelSftp> pooledObject) throws Exception {
        pooledObject.getObject().disconnect();
    }

    /**
     * 销毁连接对象
     *
     * @param pooledObject
     * @param destroyMode
     * @throws Exception
     */
    @Override
    public void destroyObject(PooledObject<ChannelSftp> pooledObject, DestroyMode destroyMode) throws Exception {
        pooledObject.getObject().disconnect();
    }

    /**
     * 创建连接对象
     *
     * @return
     * @throws Exception
     */
    @Override
    public PooledObject<ChannelSftp> makeObject() throws Exception {
        Session session = JschUtil.createSession(host, port, username, password);
        return new DefaultPooledObject<>(JschUtil.openSftp(session));
    }

    /**
     * 钝化
     *
     * @param pooledObject
     * @throws Exception
     */
    @Override
    public void passivateObject(PooledObject<ChannelSftp> pooledObject) throws Exception {
    }

    /**
     * 验证连接对象
     *
     * @param pooledObject
     * @return
     */
    @Override
    public boolean validateObject(PooledObject<ChannelSftp> pooledObject) {
        return pooledObject.getObject().isConnected();
    }

}
