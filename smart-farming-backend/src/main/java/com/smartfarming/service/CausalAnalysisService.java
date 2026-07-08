package com.smartfarming.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfarming.entity.CausalRelation;
import com.smartfarming.mapper.CausalRelationMapper;
import com.smartfarming.task.AsyncTaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class CausalAnalysisService {

    @Autowired
    private CausalRelationMapper causalRelationMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AsyncTaskManager asyncTaskManager;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${python.causal.service.url}")
    private String pythonServiceUrl;

    // Redis 缓存 key 前缀
    private static final String CACHE_KEY_PREFIX = "causal:";
    // 缓存过期时间（分钟）
    private static final long CACHE_EXPIRE_MINUTES = 10;

    /**
     * 异步因果分析（使用 AsyncTaskManager）
     */
    public String analyzeCausalAsync(Long barnId) {
        return asyncTaskManager.submitTask(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("barn_id", barnId);

                ResponseEntity<Map> response = restTemplate.postForEntity(
                        pythonServiceUrl + "/api/causal/analyze",
                        request,
                        Map.class
                );

                // 缓存结果到 Redis
                if (redisTemplate != null && response.getBody() != null) {
                    String cacheKey = CACHE_KEY_PREFIX + barnId;
                    redisTemplate.opsForValue().set(cacheKey, response.getBody(),
                            CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                }
            } catch (Exception e) {
                throw new RuntimeException("因果分析失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 同步因果分析（带 Redis 缓存）
     */
    public Map<String, Object> analyzeCausal(Long barnId) {
        // 尝试从 Redis 获取缓存
        if (redisTemplate != null) {
            try {
                String cacheKey = CACHE_KEY_PREFIX + barnId;
                Object cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached instanceof Map) {
                    return (Map<String, Object>) cached;
                }
            } catch (Exception e) {
                // Redis 不可用时忽略
            }
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("barn_id", barnId);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonServiceUrl + "/api/causal/analyze",
                    request,
                    Map.class
            );

            Map<String, Object> result = response.getBody();

            // 缓存结果到 Redis
            if (redisTemplate != null && result != null) {
                String cacheKey = CACHE_KEY_PREFIX + barnId;
                redisTemplate.opsForValue().set(cacheKey, result,
                        CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }

            return result;
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("correlation_matrix", new HashMap<>());
            fallback.put("causal_graph", Map.of(
                    "nodes", List.of(
                            Map.of("name", "temperature", "itemStyle", Map.of("color", "#ff6b6b")),
                            Map.of("name", "humidity", "itemStyle", Map.of("color", "#4ecdc4")),
                            Map.of("name", "ammonia_level", "itemStyle", Map.of("color", "#ffd93d"))
                    ),
                    "edges", List.of()
            ));
            fallback.put("causal_paths", List.of());
            return fallback;
        }
    }

    /**
     * 获取因果关系列表
     */
    public List<CausalRelation> getCausalRelationsByBarnId(Long barnId) {
        LambdaQueryWrapper<CausalRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CausalRelation::getBarnId, barnId)
               .orderByDesc(CausalRelation::getTimestamp);
        return causalRelationMapper.selectList(wrapper);
    }

    /**
     * 保存因果关系
     */
    public void saveCausalRelation(CausalRelation relation) {
        relation.setTimestamp(LocalDateTime.now());
        causalRelationMapper.insert(relation);
    }
}
