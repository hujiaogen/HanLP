/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/13 13:13</create-date>
 *
 * <copyright file="CommonSynonymDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary;

import com.hankcs.hanlp.algoritm.BinarySearch;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.corpus.synonym.Synonym;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 一个没有指定资源位置的通用同义词词典
 *
 * @author hankcs
 */
public class CommonSynonymDictionaryEx
{
    static Logger logger = LoggerFactory.getLogger(CommonSynonymDictionaryEx.class);
    DoubleArrayTrie<Long[]> trie;

    private CommonSynonymDictionaryEx()
    {
    }

    public static CommonSynonymDictionaryEx create(InputStream inputStream)
    {
        CommonSynonymDictionaryEx dictionary = new CommonSynonymDictionaryEx();
        if (dictionary.load(inputStream))
        {
            return dictionary;
        }

        TreeSet<Float> set = new TreeSet<>();

        return null;
    }

    public boolean load(InputStream inputStream)
    {
        trie = new DoubleArrayTrie<>();
        TreeMap<String, Set<Long>> treeMap = new TreeMap<>();
        String line = null;
        try
        {
            BufferedReader bw = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((line = bw.readLine()) != null)
            {
                String[] args = line.split(" ");
                List<Synonym> synonymList = Synonym.create(args);
                for (Synonym synonym : synonymList)
                {
                    Set<Long> idSet = treeMap.get(synonym.realWord);
                    if (idSet == null)
                    {
                        idSet = new TreeSet<>();
                        treeMap.put(synonym.realWord, idSet);
                    }
                    idSet.add(synonym.id);
                }
            }
            bw.close();
            List<String> keyList = new ArrayList<>(treeMap.size());
            for (String key : treeMap.keySet())
            {
                keyList.add(key);
            }
            List<Long[]> valueList = new ArrayList<>(treeMap.size());
            for (Set<Long> idSet : treeMap.values())
            {
                valueList.add(idSet.toArray(new Long[0]));
            }
            int resultCode = trie.build(keyList, valueList);
            if (resultCode != 0)
            {
                logger.warn("构建{}失败，错误码{}", inputStream, resultCode);
                return false;
            }
        }
        catch (Exception e)
        {
            logger.warn("读取{}失败，可能由行{}造成", inputStream, line);
            logger.warn("具体信息", e);
            return false;
        }
        return true;
    }

    public Long[] get(String key)
    {
        return trie.get(key);
    }

    /**
     * 语义距离
     * @param a
     * @param b
     * @return
     */
    public long distance(String a, String b)
    {
        Long[] itemA = get(a);
        if (itemA == null) return Long.MAX_VALUE / 3;
        Long[] itemB = get(b);
        if (itemB == null) return Long.MAX_VALUE / 3;

        return BinarySearch.computeAverageDistance(itemA, itemB);
    }

    /**
     * 词典中的一个条目
     */
    public static class SynonymItem extends Synonym
    {
        /**
         * 条目的value，是key的同义词近义词列表
         */
        public Map<String, Synonym> synonymMap;

        public SynonymItem(Synonym entry, Map<String, Synonym> synonymMap)
        {
            super(entry.realWord, entry.id, entry.type);
            this.synonymMap = synonymMap;
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append(' ');
            sb.append(synonymMap);
            return sb.toString();
        }
    }
}
