package com.steamthreat.service;

import com.steamthreat.entity.ComponentEntity;
import com.steamthreat.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComponentService {

    private final ComponentRepository componentRepository;

    public List<ComponentEntity> findAll() {
        return componentRepository.findAll();
    }

    public Optional<ComponentEntity> findById(Long id) {
        return componentRepository.findById(id);
    }

    public ComponentEntity save(ComponentEntity component) {
        return componentRepository.save(component);
    }

    public List<ComponentEntity> findRoots() {
        return componentRepository.findByParentIdIsNull();
    }

    /** 构建组件树（父子关系） */
    public List<Map<String, Object>> buildTree() {
        List<ComponentEntity> roots = componentRepository.findByParentIdIsNull();
        return roots.stream()
                .map(this::toTreeNode)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toTreeNode(ComponentEntity comp) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", comp.getId());
        node.put("name", comp.getName());
        node.put("role", comp.getRole());
        node.put("techStack", comp.getTechStack());
        node.put("attackPhase", comp.getAttackPhase());
        List<ComponentEntity> children = componentRepository.findByParentId(comp.getId());
        if (!children.isEmpty()) {
            node.put("children", children.stream().map(this::toTreeNode).collect(Collectors.toList()));
        }
        return node;
    }
}
