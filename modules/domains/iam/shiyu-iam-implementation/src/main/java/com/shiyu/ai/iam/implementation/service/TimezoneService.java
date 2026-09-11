package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.iam.implementation.request.SetTimezoneRequest;
import com.shiyu.ai.iam.implementation.vo.TimezoneOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

public interface TimezoneService {
    List<TimezoneOptionVO> getTimezoneOptions();

    String getTimezone(ActorContext actor);

    boolean setTimezone(ActorContext actor, SetTimezoneRequest request);
}
