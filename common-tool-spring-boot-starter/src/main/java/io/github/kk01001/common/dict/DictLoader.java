package io.github.kk01001.common.dict;

import java.util.Map;

public interface DictLoader {
    
    /**
     * 加载字典数据
     *
     * @param type 字典类型
     * @return 字典数据(code -> text)
     */
    Map<String, String> loadDict(String type);
    
    /**
     * 加载所有字典数据
     *
     * @return 字典数据(type -> (code -> text))
     */
    Map<String, Map<String, String>> loadAllDict();
} 