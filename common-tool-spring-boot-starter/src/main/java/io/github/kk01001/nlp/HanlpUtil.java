package io.github.kk01001.nlp;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.suggest.Suggester;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description HanLP分词工具类，提供中文分词、关键词提取、文本摘要等功能
 */
@Slf4j
public class HanlpUtil {

    public static void main(String[] args) {
        for (int i = 0; i < 1; i++) {
            long start = System.currentTimeMillis();
            String text = "面向生产环境的多语种自然语言处理工具包，" +
                    "基于PyTorch和TensorFlow 2.x双引擎，目标是普及落地最前沿的NLP技术。" +
                    "HanLP具备功能完善、精度准确、性能高效、语料时新、架构清晰、可自定义的特点。";
            System.out.println(standardSegment(text));
            System.out.println(standardSegmentWithNature(text));
            System.out.println(nlpSegment(text));
            System.out.println(indexSegment(text));
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        }
    }

    /**
     * 索引分词
     */
    public static List<String> indexSegment(String text) {
        // 索引分词
        List<Term> termList = IndexTokenizer.segment(text);
        return termList.stream()
                .map(e -> {
                    return e.word;
                })
                .collect(Collectors.toList());
    }

    /**
     * 标准分词
     *
     * @param text 待分词文本
     * @return 分词结果列表
     */
    public static List<String> standardSegment(String text) {
        return HanLP.segment(text).stream()
                .map(e -> {
                    return e.word;
                })
                .collect(Collectors.toList());
    }

    /**
     * 标准分词（包含词性）
     *
     * @param text 待分词文本
     * @return 分词结果列表（包含词性）
     */
    public static List<Term> standardSegmentWithNature(String text) {
        return HanLP.segment(text);
    }

    /**
     * NLP分词（包含词性和命名实体识别）
     *
     * @param text 待分词文本
     * @return 分词结果列表
     */
    public static List<Term> nlpSegment(String text) {
        return HanLP.newSegment()
                .enableNameRecognize(true)
                .enablePlaceRecognize(true)
                .enableOrganizationRecognize(true)
                .enableCustomDictionary(true)
                .seg(text);
    }

    /**
     * 提取关键词
     *
     * @param text         文本
     * @param keywordCount 关键词数量
     * @return 关键词列表
     */
    public static List<String> extractKeywords(String text, int keywordCount) {
        return HanLP.extractKeyword(text, keywordCount);
    }

    /**
     * 提取摘要
     *
     * @param text          文本
     * @param sentenceCount 摘要句子数
     * @return 摘要句子列表
     */
    public static List<String> extractSummary(String text, int sentenceCount) {
        return HanLP.extractSummary(text, sentenceCount);
    }

    /**
     * 获取文本推荐（相似句子）
     *
     * @param text 文本
     * @param size 推荐数量
     * @return 相似句子列表
     */
    public static List<String> suggest(String text, int size) {
        Suggester suggester = new Suggester();
        suggester.addSentence(text);
        return suggester.suggest(text, size);
    }

    /**
     * 添加自定义词典
     *
     * @param word      词语
     * @param nature    词性
     * @param frequency 词频
     */
    public static void addCustomDictionary(String word, String nature, int frequency) {
        CustomDictionary.add(word, nature + " " + frequency);
        log.info("Added custom dictionary word: {}, nature: {}, frequency: {}", word, nature, frequency);
    }

    /**
     * 添加自定义词典（使用默认词性和词频）
     *
     * @param word 词语
     */
    public static void addCustomDictionary(String word) {
        CustomDictionary.add(word);
        log.info("Added custom dictionary word: {}", word);
    }

    /**
     * 删除自定义词典中的词语
     *
     * @param word 词语
     */
    public static void removeCustomDictionary(String word) {
        CustomDictionary.remove(word);
        log.info("Removed custom dictionary word: {}", word);
    }

    /**
     * 判断是否为自定义词语
     *
     * @param word 词语
     * @return 是否为自定义词语
     */
    public static boolean isCustomWord(String word) {
        return CustomDictionary.contains(word);
    }

    /**
     * 繁体转简体
     *
     * @param text 繁体文本
     * @return 简体文本
     */
    public static String convertToSimplifiedChinese(String text) {
        return HanLP.convertToSimplifiedChinese(text);
    }

    /**
     * 简体转繁体
     *
     * @param text 简体文本
     * @return 繁体文本
     */
    public static String convertToTraditionalChinese(String text) {
        return HanLP.convertToTraditionalChinese(text);
    }

    /**
     * 提取文本中的短语
     *
     * @param text 文本
     * @param size 短语数量
     * @return 短语列表
     */
    public static List<String> extractPhrase(String text, int size) {
        return HanLP.extractPhrase(text, size);
    }
} 