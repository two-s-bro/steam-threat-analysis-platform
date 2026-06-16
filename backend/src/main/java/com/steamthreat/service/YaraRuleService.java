package com.steamthreat.service;

import com.steamthreat.entity.YaraRule;
import com.steamthreat.repository.YaraRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;

@Service
@RequiredArgsConstructor
public class YaraRuleService {

    private final YaraRuleRepository yaraRuleRepository;

    public List<YaraRule> findAll() { return yaraRuleRepository.findAll(); }
    public Optional<YaraRule> findById(Long id) { return yaraRuleRepository.findById(id); }
    public YaraRule save(YaraRule rule) { return yaraRuleRepository.save(rule); }
    public void deleteById(Long id) { yaraRuleRepository.deleteById(id); }

    /**
     * 模拟 YARA 匹配: 对给定的文本/文件内容运行所有启用的规则
     * 实际 YARA 需要 yara-python，这里用正则/keyword 方式实现
     */
    public List<Map<String, Object>> matchAgainst(String content) {
        List<Map<String, Object>> results = new ArrayList<>();
        List<YaraRule> rules = yaraRuleRepository.findByEnabledTrue();

        for (YaraRule rule : rules) {
            // 从 YARA 规则体中提取 $string = "xxx" 或 $re = /xxx/
            List<String> patterns = extractPatterns(rule.getRuleBody());
            boolean matched = false;
            List<String> matchedPatterns = new ArrayList<>();

            for (String pattern : patterns) {
                try {
                    if (content.toLowerCase().contains(pattern.toLowerCase())) {
                        matched = true;
                        matchedPatterns.add(pattern);
                    }
                } catch (Exception ignored) {}
            }

            if (matched) {
                rule.setMatchCount(rule.getMatchCount() + 1);
                yaraRuleRepository.save(rule);

                Map<String, Object> m = new LinkedHashMap<>();
                m.put("ruleId", rule.getId());
                m.put("ruleName", rule.getRuleName());
                m.put("riskLevel", rule.getRiskLevel());
                m.put("targetType", rule.getTargetType());
                m.put("matchedPatterns", matchedPatterns);
                results.add(m);
            }
        }
        return results;
    }

    /** 从 YARA 规则体中提取匹配模式 */
    private List<String> extractPatterns(String ruleBody) {
        List<String> patterns = new ArrayList<>();
        // 提取 $xxx = "pattern"
        Matcher m1 = Pattern.compile("\\$\\w+\\s*=\\s*\"([^\"]+)\"").matcher(ruleBody);
        while (m1.find()) patterns.add(m1.group(1));
        // 提取 $xxx = /pattern/
        Matcher m2 = Pattern.compile("\\$\\w+\\s*=\\s*/([^/]+)/").matcher(ruleBody);
        while (m2.find()) patterns.add(m2.group(1));
        // 提取 nocase wide ascii 修饰符下的字符串
        Matcher m3 = Pattern.compile("\"([^\"]{4,})\"\\s+nocase", Pattern.CASE_INSENSITIVE).matcher(ruleBody);
        while (m3.find()) patterns.add(m3.group(1));
        return patterns;
    }

    public Map<String, Object> batchMatch(List<String> fileContents) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> allMatches = new ArrayList<>();
        for (int i = 0; i < fileContents.size(); i++) {
            List<Map<String, Object>> matches = matchAgainst(fileContents.get(i));
            if (!matches.isEmpty()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("fileIndex", i);
                entry.put("matches", matches);
                allMatches.add(entry);
            }
        }
        result.put("totalFiles", fileContents.size());
        result.put("matchedFiles", allMatches.size());
        result.put("results", allMatches);
        return result;
    }
}
