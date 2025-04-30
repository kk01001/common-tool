package io.github.kk01001.nlp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 基于字典的中文分词器，实现正向最大匹配、逆向最大匹配和双向最大匹配算法
 */
@Slf4j
@Component
public class DictionarySegmenter {

    /**
     * 词典集合
     */
    private static final Set<String> dictionary = new HashSet<>();

    /**
     * 最大词长
     */
    private static final int MAX_WORD_LENGTH = 4;

    public static void main(String[] args) {
        addWord("完善");
        addWord("NLP");
        for (int i = 0; i < 1; i++) {
            long start = System.currentTimeMillis();
            String text = "面向生产环境的多语种自然语言处理工具包，" +
                    "基于PyTorch和TensorFlow 2.x双引擎，目标是普及落地最前沿的NLP技术。" +
                    "HanLP具备功能完善、精度准确、性能高效、语料时新、架构清晰、可自定义的特点。";
            System.out.println(forwardMaximumMatching(text));
            System.out.println(backwardMaximumMatching(text));
            System.out.println(bidirectionalMaximumMatching(text));
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        }
    }


    /**
     * 初始化词典
     *
     * @param words 词语列表
     */
    public static void initDictionary(Collection<String> words) {
        dictionary.clear();
        dictionary.addAll(words);
        log.info("Dictionary initialized with {} words", words.size());
    }

    /**
     * 添加词语到词典
     *
     * @param word 词语
     */
    public static void addWord(String word) {
        dictionary.add(word);
    }

    /**
     * 正向最大匹配算法(FMM)
     *
     * @param text 待分词文本
     * @return 分词结果列表
     */
    public static List<String> forwardMaximumMatching(String text) {
        List<String> results = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return results;
        }

        int textLength = text.length();
        int start = 0;

        while (start < textLength) {
            int maxLength = Math.min(MAX_WORD_LENGTH, textLength - start);
            String word = null;

            for (int end = maxLength; end >= 1; end--) {
                String candidate = text.substring(start, start + end);
                if (dictionary.contains(candidate)) {
                    word = candidate;
                    break;
                }
            }

            if (word == null) {
                word = text.substring(start, start + 1);
            }

            results.add(word);
            start += word.length();
        }

        return results;
    }

    /**
     * 逆向最大匹配算法(BMM)
     *
     * @param text 待分词文本
     * @return 分词结果列表
     */
    public static List<String> backwardMaximumMatching(String text) {
        List<String> results = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return results;
        }

        int textLength = text.length();
        int end = textLength;

        while (end > 0) {
            int maxLength = Math.min(MAX_WORD_LENGTH, end);
            String word = null;

            for (int start = maxLength; start >= 1; start--) {
                String candidate = text.substring(end - start, end);
                if (dictionary.contains(candidate)) {
                    word = candidate;
                    break;
                }
            }

            if (word == null) {
                word = text.substring(end - 1, end);
            }

            results.add(0, word);
            end -= word.length();
        }

        return results;
    }

    /**
     * 双向最大匹配算法(BIMM)
     *
     * @param text 待分词文本
     * @return 分词结果列表
     */
    public static List<String> bidirectionalMaximumMatching(String text) {
        // 获取正向和逆向分词结果
        List<String> forwardResults = forwardMaximumMatching(text);
        List<String> backwardResults = backwardMaximumMatching(text);

        // 如果正向和逆向分词结果词数不同，返回词数更少的结果
        if (forwardResults.size() != backwardResults.size()) {
            return forwardResults.size() < backwardResults.size() ? forwardResults : backwardResults;
        }

        // 如果词数相同，返回单字词数更少的结果
        int forwardSingleCharCount = countSingleCharWords(forwardResults);
        int backwardSingleCharCount = countSingleCharWords(backwardResults);

        return forwardSingleCharCount < backwardSingleCharCount ? forwardResults : backwardResults;
    }

    /**
     * 统计单字词的数量
     *
     * @param words 词语列表
     * @return 单字词数量
     */
    private static int countSingleCharWords(List<String> words) {
        return (int) words.stream()
                .filter(word -> word.length() == 1)
                .count();
    }

    /**
     * 获取分词的所有可能组合
     *
     * @param text 待分词文本
     * @return 所有可能的分词结果
     */
    public static Set<List<String>> getAllPossibleSegmentations(String text) {
        Set<List<String>> results = new HashSet<>();
        if (text == null || text.isEmpty()) {
            return results;
        }

        // 使用动态规划找出所有可能的分词组合
        findAllSegmentations(text, 0, new ArrayList<>(), results);
        return results;
    }

    /**
     * 递归查找所有可能的分词组合
     */
    private static void findAllSegmentations(String text, int start, List<String> current, Set<List<String>> results) {
        if (start >= text.length()) {
            results.add(new ArrayList<>(current));
            return;
        }

        for (int end = start + 1; end <= Math.min(start + MAX_WORD_LENGTH, text.length()); end++) {
            String word = text.substring(start, end);
            if (dictionary.contains(word)) {
                current.add(word);
                findAllSegmentations(text, end, current, results);
                current.remove(current.size() - 1);
            }
        }

        // 处理未登录词
        if (current.isEmpty() || start < text.length()) {
            String singleChar = text.substring(start, start + 1);
            current.add(singleChar);
            findAllSegmentations(text, start + 1, current, results);
            current.remove(current.size() - 1);
        }
    }

    /**
     * 评估分词结果
     *
     * @param segmentedWords 分词结果
     * @return 评分（越高越好）
     */
    public static double evaluateSegmentation(List<String> segmentedWords) {
        if (segmentedWords == null || segmentedWords.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;

        // 计算平均词长
        double avgLength = segmentedWords.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0.0);

        // 计算词典词的比例
        long dictWordCount = segmentedWords.stream()
                .filter(dictionary::contains)
                .count();
        double dictRatio = (double) dictWordCount / segmentedWords.size();

        // 计算单字词的比例（单字词比例越低越好）
        double singleCharRatio = (double) countSingleCharWords(segmentedWords) / segmentedWords.size();

        // 综合评分：词典词比例 * 0.5 + 平均词长 * 0.3 + (1 - 单字词比例) * 0.2
        score = dictRatio * 0.5 + (avgLength / MAX_WORD_LENGTH) * 0.3 + (1 - singleCharRatio) * 0.2;

        return score;
    }
}
