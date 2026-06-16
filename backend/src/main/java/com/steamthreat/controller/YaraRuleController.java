package com.steamthreat.controller;

import com.steamthreat.dto.ApiResponse;
import com.steamthreat.entity.YaraRule;
import com.steamthreat.service.YaraRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/yara")
@RequiredArgsConstructor
public class YaraRuleController {

    private final YaraRuleService yaraService;

    @GetMapping
    public ApiResponse<List<YaraRule>> list() {
        return ApiResponse.ok(yaraService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<YaraRule> get(@PathVariable Long id) {
        return yaraService.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error(404, "规则不存在"));
    }

    @PostMapping
    public ApiResponse<YaraRule> create(@RequestBody YaraRule rule) {
        return ApiResponse.ok("规则已创建", yaraService.save(rule));
    }

    @PutMapping("/{id}")
    public ApiResponse<YaraRule> update(@PathVariable Long id, @RequestBody YaraRule rule) {
        if (!yaraService.findById(id).isPresent()) {
            return ApiResponse.error(404, "规则不存在");
        }
        rule.setId(id);
        return ApiResponse.ok(yaraService.save(rule));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        yaraService.deleteById(id);
        return ApiResponse.ok("规则已删除", null);
    }

    /** YARA 匹配测试 */
    @PostMapping("/match")
    public ApiResponse<List<Map<String, Object>>> match(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "");
        List<Map<String, Object>> results = yaraService.matchAgainst(content);
        return ApiResponse.ok("匹配完成，命中 " + results.size() + " 条规则", results);
    }
}
