package io.github.kk01001.chain;


/**
 * @author linshiqiang
 * @since 2022-10-17 10:20
 * 责任链抽象类
 */
public abstract class AbstractChainHandler<T> {

    /**
     * 下一个链
     */
    protected AbstractChainHandler<T> nextChain;

    private void next(AbstractChainHandler<T> chainHandler) {
        nextChain = chainHandler;
    }

    protected Boolean isEnd() {

        return nextChain == null;
    }

    /**
     * 还存在下一个节点, 转到下一个节点
     *
     * @param context 上下文
     */
    protected void nextHandler(T context) {
        if (!isEnd()) {
            nextChain.doHandler(context);
        }
    }

    /**
     * 处理事件
     *
     * @param context 上下文
     */
    public abstract void doHandler(T context);

    public static class Builder<T> {

        private AbstractChainHandler<T> head;

        private AbstractChainHandler<T> tail;

        public void addHandler(AbstractChainHandler<T> abstractChainHandler) {
            if (head == null) {
                head = tail = abstractChainHandler;
                return;
            }

            tail.next(abstractChainHandler);
            tail = abstractChainHandler;
        }

        public AbstractChainHandler<T> build() {
            return head;
        }
    }
}
