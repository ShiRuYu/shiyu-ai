package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.port.repository.UserRepository;
import com.shiyu.ai.auth.request.SetTimezoneRequest;
import com.shiyu.ai.auth.service.TimezoneService;
import com.shiyu.ai.auth.vo.TimezoneOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.auth.domain.enums.TimezoneEnum;
import com.shiyu.ai.common.core.utils.JSONUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimezoneServiceImpl implements TimezoneService {
    private final UserRepository userRepository;

    public TimezoneServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<TimezoneOptionVO> getTimezoneOptions() {
        return Arrays.stream(TimezoneEnum.values())
                .map(timezone -> TimezoneOptionVO.builder()
                        .label(timezone.getLabel()).value(timezone.getValue()).build())
                .toList();
    }

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
