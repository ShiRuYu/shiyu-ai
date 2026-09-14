package com.shiyu.ai.iam.implementation.service.impl;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.iam.implementation.domain.enums.TimezoneEnum;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.port.repository.UserRepository;
import com.shiyu.ai.iam.implementation.request.SetTimezoneRequest;
import com.shiyu.ai.iam.implementation.service.TimezoneService;
import com.shiyu.ai.iam.implementation.vo.TimezoneOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code TimezoneServiceImpl} 实现平台模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Service
public class TimezoneServiceImpl implements TimezoneService {
    /**
     * 用户仓储，表示当前对象中的对应属性。
     */
    private final UserRepository userRepository;

    /**
     * {@code TimezoneServiceImpl} 创建并初始化当前类型实例。
     *
     * @param userRepository 参数值，用于执行当前操作。
     */
    public TimezoneServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * {@code getTimezoneOptions} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<TimezoneOptionVO> getTimezoneOptions() {
        return Arrays.stream(TimezoneEnum.values())
                .map(
                        timezone ->
                                TimezoneOptionVO.builder()
                                        .label(timezone.getLabel())
                                        .value(timezone.getValue())
                                        .build())
                .toList();
    }

    /**
     * {@code getTimezone} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String getTimezone(ActorContext actor) {
        if (actor == null) {
            throw new IllegalArgumentException("actor context is required");
        }
        Long userId = actor.userId().value();
        String current = TimezoneEnum.ASIA_SHANGHAI.getValue();
        UserBO user = userRepository.selectById(userId);
        if (user == null || user.getExtInfo() == null) {
            return current;
        }
        Map<String, Object> extInfo = JSONUtils.parseMap(user.getExtInfo());
        Object configured = extInfo == null ? null : extInfo.get("timezone");
        return configured == null ? current : String.valueOf(configured);
    }

    /**
     * {@code setTimezone} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean setTimezone(ActorContext actor, SetTimezoneRequest request) {
        if (request == null || !TimezoneEnum.isValid(request.getTimezone())) {
            return false;
        }
        if (actor == null) {
            throw new IllegalArgumentException("actor context is required");
        }
        Long userId = actor.userId().value();
        UserBO user = userRepository.selectById(userId);
        if (user == null) {
            return false;
        }
        Map<String, Object> extInfo = new HashMap<>();
        if (user.getExtInfo() != null) {
            Map<String, Object> existing = JSONUtils.parseMap(user.getExtInfo());
            if (existing != null) {
                extInfo.putAll(existing);
            }
        }
        extInfo.put("timezone", request.getTimezone());
        user.setExtInfo(JSONUtils.toJsonString(extInfo));
        return userRepository.update(user);
    }
}
