package com.steamthreat.controller;

import com.steamthreat.dto.ApiResponse;
import com.steamthreat.entity.Ioc;
import com.steamthreat.service.IocService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/iocs")
@RequiredArgsConstructor
public class IocController {

    private final IocService iocService;

    @GetMapping
    public ApiResponse<List<Ioc>> list() {
        return ApiResponse.ok(iocService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Ioc> get(@PathVariable Long id) {
        return iocService.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error(404, "IOC不存在"));
    }

    @PostMapping
    public ApiResponse<Ioc> create(@RequestBody Ioc ioc) {
        return ApiResponse.ok("IOC已录入", iocService.save(ioc));
    }

    @PutMapping("/{id}")
    public ApiResponse<Ioc> update(@PathVariable Long id, @RequestBody Ioc ioc) {
        if (!iocService.findById(id).isPresent()) {
            return ApiResponse.error(404, "IOC不存在");
        }
        ioc.setId(id);
        return ApiResponse.ok(iocService.save(ioc));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        iocService.deleteById(id);
        return ApiResponse.ok("IOC已删除", null);
    }

    @GetMapping("/type/{type}")
    public ApiResponse<List<Ioc>> byType(@PathVariable String type) {
        return ApiResponse.ok(iocService.findByType(type));
    }

    @GetMapping("/search")
    public ApiResponse<List<Ioc>> search(@RequestParam String keyword) {
        return ApiResponse.ok(iocService.search(keyword));
    }
}
