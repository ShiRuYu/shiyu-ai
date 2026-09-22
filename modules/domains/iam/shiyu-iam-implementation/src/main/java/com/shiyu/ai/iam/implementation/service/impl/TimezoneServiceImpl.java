package com.shiyu.ai.iam.implementation.service.impl;

import com.shiyu.ai.common.foundation.utils.JSONUtils;
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
 * 提供 Timezone 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Service
public class TimezoneServiceImpl implements TimezoneService {
    /**
     * 用户仓储，表示当前对象中的对应属性。
     */
    private final UserRepository userRepository;

    /**
     * 执行 Timezone 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param userRepository 用于完成本次业务处理的 userRepository 参数。
     */
    public TimezoneServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 查询 Timezone 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 Timezone 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回 Timezone 相关操作生成的结果数据。
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
     * 更新或设置 Timezone 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回本次条件判断是否成立。
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
