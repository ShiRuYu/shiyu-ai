package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.request.SetTimezoneRequest;
import com.shiyu.ai.auth.vo.TimezoneOptionVO;

import java.util.List;

public interface TimezoneService {
    List<TimezoneOptionVO> getTimezoneOptions();
    String getTimezone();
    boolean setTimezone(SetTimezoneRequest request);
}
