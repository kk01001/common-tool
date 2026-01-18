package io.github.kk01001.sensitive.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2026-01-18 13:02:31
 * @description DFA 算法节点
 */
public class DfaNode {
    
    /**
     * 子节点
     */
    private final Map<Character, DfaNode> children = new HashMap<>();
    
    /**
     * 是否是敏感词结尾
     */
    private boolean end = false;
    
    /**
     * 敏感词分类
     */
    private String category;
    
    /**
     * 完整的敏感词（仅在结束节点存储）
     */
    private String word;
    
    public Map<Character, DfaNode> getChildren() {
        return children;
    }
    
    public DfaNode getChild(char c) {
        return children.get(c);
    }
    
    public void addChild(char c, DfaNode node) {
        children.put(c, node);
    }
    
    public boolean hasChild(char c) {
        return children.containsKey(c);
    }
    
    public boolean isEnd() {
        return end;
    }
    
    public void setEnd(boolean end) {
        this.end = end;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getWord() {
        return word;
    }
    
    public void setWord(String word) {
        this.word = word;
    }
}
