package com.steamthreat.controller;

import com.steamthreat.dto.ApiResponse;
import com.steamthreat.entity.ComponentEntity;
import com.steamthreat.service.ComponentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentService componentService;

    @GetMapping
    public ApiResponse<List<ComponentEntity>> list() {
        return ApiResponse.ok(componentService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ComponentEntity> get(@PathVariable Long id) {
        return componentService.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error(404, "组件不存在"));
    }

    /** 返回组件树结构（前端力导向图用） */
    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> tree() {
        return ApiResponse.ok(componentService.buildTree());
    }

    /** 返回平铺列表（前端攻击链流程图用） */
    @GetMapping("/roots")
    public ApiResponse<List<ComponentEntity>> roots() {
        return ApiResponse.ok(componentService.findRoots());
    }

    @GetMapping("/role/{role}")
    public ApiResponse<List<ComponentEntity>> byRole(@PathVariable String role) {
        return ApiResponse.ok(componentService.findAll().stream()
                .filter(c -> role.equals(c.getRole())).toList());
    }
}
