package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.request.SetTimezoneRequest;
import com.shiyu.ai.auth.vo.TimezoneOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

public interface TimezoneService {
    List<TimezoneOptionVO> getTimezoneOptions();
    String getTimezone(ActorContext actor);
    boolean setTimezone(ActorContext actor, SetTimezoneRequest request);
}
